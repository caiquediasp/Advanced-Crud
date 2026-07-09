package com.caique.AdvancedCrud.address;

import com.caique.AdvancedCrud.address.dto.CreateAddressRequest;
import com.caique.AdvancedCrud.address.mapper.AddressMapper;
import com.caique.AdvancedCrud.shared.exceptions.AddressNotFoundException;
import com.caique.AdvancedCrud.user.User;
import com.caique.AdvancedCrud.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private User mockUserWithId(UUID publicId, Long internalId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(internalId);
        when(userRepository.findByPublicIdAndDeletedAtIsNull(publicId))
                .thenReturn(Optional.of(user));
        return user;
    }

    private CreateAddressRequest sampleCreateRequest() {
        return new CreateAddressRequest(
                "64015310", "Rua Teste", "100", null,
                "Centro", "Teresina", "PI");
    }

    @Test
    void create_firstAddress_marksAsPrimary() {
        UUID userPublicId = UUID.randomUUID();
        mockUserWithId(userPublicId, 1L);

        when(addressRepository.existsByUserId(1L)).thenReturn(false);

        addressService.create(userPublicId, sampleCreateRequest());

        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertThat(captor.getValue().isPrimary()).isTrue();
    }

    @Test
    void create_whenUserAlreadyHasAddress_notMarkedPrimary() {
        UUID userPublicId = UUID.randomUUID();
        mockUserWithId(userPublicId, 1L);

        when(addressRepository.existsByUserId(1L)).thenReturn(true);

        addressService.create(userPublicId, sampleCreateRequest());

        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertThat(captor.getValue().isPrimary()).isFalse();
    }

    @Test
    void setPrimary_unmarksPreviousPrimary() {
        UUID userPublicId = UUID.randomUUID();
        UUID targetPublicId = UUID.randomUUID();
        UUID previousPublicId = UUID.randomUUID();
        mockUserWithId(userPublicId, 1L);

        Address target = mock(Address.class);
        when(addressRepository.findByPublicIdAndUserId(targetPublicId, 1L))
                .thenReturn(Optional.of(target));

        Address previousPrimary = mock(Address.class);
        when(previousPrimary.getPublicId()).thenReturn(previousPublicId);
        when(addressRepository.findByUserIdAndPrimaryIsTrue(1L))
                .thenReturn(Optional.of(previousPrimary));

        addressService.setPrimary(userPublicId, targetPublicId);

        verify(previousPrimary).setPrimary(false);
        verify(target).setPrimary(true);
    }

    @Test
    void delete_primaryAddress_promotesOldestRemaining() {
        UUID userPublicId = UUID.randomUUID();
        UUID addressPublicId = UUID.randomUUID();
        mockUserWithId(userPublicId, 1L);

        Address primary = mock(Address.class);
        when(primary.isPrimary()).thenReturn(true);
        when(addressRepository.findByPublicIdAndUserId(addressPublicId, 1L))
                .thenReturn(Optional.of(primary));

        Address nextOldest = mock(Address.class);
        when(addressRepository.findFirstByUserIdOrderByCreatedAtAsc(1L))
                .thenReturn(Optional.of(nextOldest));

        addressService.delete(userPublicId, addressPublicId);

        verify(addressRepository).delete(primary);
        verify(nextOldest).setPrimary(true);
    }

    @Test
    void delete_nonPrimaryAddress_doesNotPromote() {
        UUID userPublicId = UUID.randomUUID();
        UUID addressPublicId = UUID.randomUUID();
        mockUserWithId(userPublicId, 1L);

        Address nonPrimary = mock(Address.class);
        when(nonPrimary.isPrimary()).thenReturn(false);
        when(addressRepository.findByPublicIdAndUserId(addressPublicId, 1L))
                .thenReturn(Optional.of(nonPrimary));

        addressService.delete(userPublicId, addressPublicId);

        verify(addressRepository).delete(nonPrimary);
        verify(addressRepository, never()).findFirstByUserIdOrderByCreatedAtAsc(anyLong());
    }

    @Test
    void getOne_addressOfAnotherUser_throwsAddressNotFound() {
        UUID userPublicId = UUID.randomUUID();
        UUID addressPublicId = UUID.randomUUID();
        mockUserWithId(userPublicId, 1L);

        when(addressRepository.findByPublicIdAndUserId(addressPublicId, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getOne(userPublicId, addressPublicId))
                .isInstanceOf(AddressNotFoundException.class);
    }
}