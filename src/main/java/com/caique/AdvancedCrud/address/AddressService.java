package com.caique.AdvancedCrud.address;

import com.caique.AdvancedCrud.address.dto.AddressResponse;
import com.caique.AdvancedCrud.address.dto.CreateAddressRequest;
import com.caique.AdvancedCrud.address.dto.UpdateAddressRequest;
import com.caique.AdvancedCrud.address.mapper.AddressMapper;
import com.caique.AdvancedCrud.shared.exceptions.AddressNotFoundException;
import com.caique.AdvancedCrud.shared.exceptions.UserNotFoundException;
import com.caique.AdvancedCrud.user.User;
import com.caique.AdvancedCrud.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    public AddressService(AddressRepository addressRepository,
                          UserRepository userRepository,
                          AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.addressMapper = addressMapper;
    }

    @Transactional
    public AddressResponse create(UUID userPublicId, CreateAddressRequest request) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(userPublicId)
                .orElseThrow(() -> new UserNotFoundException(userPublicId));

        Address address = new Address(
                user, request.zipcode(), request.street(), request.number(),
                request.complement(), request.neighborhood(), request.city(), request.state());

        boolean isFirst = !addressRepository.existsByUserId(user.getId());
        if (isFirst) {
            address.setPrimary(true);
        }

        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    @Transactional(readOnly = true)
    public Page<AddressResponse> listMyAddresses(UUID userPublicId, Pageable pageable) {
        return addressRepository.findByUser_PublicIdAndUser_DeletedAtIsNull(userPublicId, pageable)
                .map(addressMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AddressResponse getOne(UUID userPublicId, UUID addressPublicId) {
        return addressMapper.toResponse(findOwnedAddress(userPublicId, addressPublicId));
    }

    @Transactional
    public AddressResponse update(UUID userPublicId, UUID addressPublicId, UpdateAddressRequest request) {
        Address address = findOwnedAddress(userPublicId, addressPublicId);

        address.setZipcode(request.zipcode());
        address.setStreet(request.street());
        address.setNumber(request.number());
        address.setComplement(request.complement());
        address.setNeighborhood(request.neighborhood());
        address.setCity(request.city());
        address.setState(request.state());

        return addressMapper.toResponse(address);
    }

    @Transactional
    public void delete(UUID userPublicId, UUID addressPublicId) {
        Address address = findOwnedAddress(userPublicId, addressPublicId);

        boolean wasPrimary = address.isPrimary();
        addressRepository.delete(address);

        if (wasPrimary) {
            addressRepository.findFirstByUser_PublicIdOrderByCreatedAtAsc(userPublicId)
                    .ifPresent(next -> next.setPrimary(true));
        }
    }

    @Transactional
    public AddressResponse setPrimary(UUID userPublicId, UUID addressPublicId) {
        Address target = findOwnedAddress(userPublicId, addressPublicId);

        addressRepository.findByUser_PublicIdAndPrimaryIsTrue(userPublicId)
                .ifPresent(current -> {
                    if (!current.getPublicId().equals(addressPublicId)) {
                        current.setPrimary(false);
                        addressRepository.flush();
                    }
                });

        target.setPrimary(true);
        return addressMapper.toResponse(target);
    }

    private Address findOwnedAddress(UUID userPublicId, UUID addressPublicId) {
        return addressRepository
                .findByPublicIdAndUser_PublicIdAndUser_DeletedAtIsNull(addressPublicId, userPublicId)
                .orElseThrow(() -> new AddressNotFoundException(addressPublicId));
    }

}
