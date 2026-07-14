package com.caique.advancedcrud.user;

import com.caique.advancedcrud.auth.token.RefreshTokenService;
import com.caique.advancedcrud.shared.exceptions.EmailAlreadyExistsException;
import com.caique.advancedcrud.shared.exceptions.InvalidPasswordException;
import com.caique.advancedcrud.shared.exceptions.RoleNotFoundException;
import com.caique.advancedcrud.shared.exceptions.SelfModificationException;
import com.caique.advancedcrud.user.dto.ChangePasswordRequest;
import com.caique.advancedcrud.user.dto.UpdateUserRequest;
import com.caique.advancedcrud.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_withDuplicateEmail_throwsEmailAlreadyExists() {
        when(userRepository.existsByEmailAndDeletedAtIsNull("dup@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                userService.createUser("Name", "dup@example.com", "password123"))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_withNewEmail_savesUser() {
        when(userRepository.existsByEmailAndDeletedAtIsNull("new@example.com"))
                .thenReturn(false);
        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(mock(Role.class)));
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        userService.createUser("Name", "new@example.com", "password123");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_withWrongCurrentPassword_throwsInvalidPassword() {
        UUID publicId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn("storedHash");
        when(userRepository.findByPublicIdAndDeletedAtIsNull(publicId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongCurrent", "storedHash"))
                .thenReturn(false);

        ChangePasswordRequest request =
                new ChangePasswordRequest("wrongCurrent", "newPassword123");

        assertThatThrownBy(() -> userService.changePassword(publicId, request))
                .isInstanceOf(InvalidPasswordException.class);

        verify(user, never()).setPasswordHash(anyString());
    }

    @Test
    void changePassword_withCorrectCurrentPassword_updatesHash() {
        UUID publicId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn("storedHash");
        when(userRepository.findByPublicIdAndDeletedAtIsNull(publicId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctCurrent", "storedHash"))
                .thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHash");

        ChangePasswordRequest request =
                new ChangePasswordRequest("correctCurrent", "newPassword123");

        userService.changePassword(publicId, request);

        verify(user).setPasswordHash("newHash");
    }

    @Test
    void updateProfile_changingToExistingEmail_throwsEmailAlreadyExists() {
        UUID publicId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("old@example.com");
        when(user.getPasswordHash()).thenReturn("storedHash");
        when(userRepository.findByPublicIdAndDeletedAtIsNull(publicId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctCurrent", "storedHash"))
                .thenReturn(true);
        when(userRepository.existsByEmailAndDeletedAtIsNull("taken@example.com"))
                .thenReturn(true);

        UpdateUserRequest request = new UpdateUserRequest("Name", "taken@example.com", "correctCurrent");

        assertThatThrownBy(() -> userService.updateProfile(publicId, request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void updateProfile_keepingSameEmail_doesNotCheckDuplicate() {
        UUID publicId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("same@example.com");
        when(userRepository.findByPublicIdAndDeletedAtIsNull(publicId))
                .thenReturn(Optional.of(user));

        UpdateUserRequest request = new UpdateUserRequest("New Name", "same@example.com", null);

        assertThatCode(() -> userService.updateProfile(publicId, request))
                .doesNotThrowAnyException();

        verify(userRepository, never()).existsByEmailAndDeletedAtIsNull("same@example.com");
        verify(user).setName("New Name");
        verify(refreshTokenService, never()).revokeAllSessions(any());
    }

    @Test
    void updateProfile_changingEmailWithoutPassword_throwsInvalidPassword() {
        UUID publicId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("old@example.com");
        when(userRepository.findByPublicIdAndDeletedAtIsNull(publicId))
                .thenReturn(Optional.of(user));

        UpdateUserRequest request = new UpdateUserRequest("Name", "new@example.com", null);

        assertThatThrownBy(() -> userService.updateProfile(publicId, request))
                .isInstanceOf(InvalidPasswordException.class);

        verify(user, never()).setEmail(anyString());
    }

    @Test
    void updateProfile_changingEmailWithWrongPassword_throwsInvalidPassword() {
        UUID publicId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("old@example.com");
        when(user.getPasswordHash()).thenReturn("storedHash");
        when(userRepository.findByPublicIdAndDeletedAtIsNull(publicId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongCurrent", "storedHash"))
                .thenReturn(false);

        UpdateUserRequest request = new UpdateUserRequest("Name", "new@example.com", "wrongCurrent");

        assertThatThrownBy(() -> userService.updateProfile(publicId, request))
                .isInstanceOf(InvalidPasswordException.class);

        verify(user, never()).setEmail(anyString());
    }

    @Test
    void updateProfile_changingEmailWithCorrectPassword_updatesAndRevokesSessions() {
        UUID publicId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("old@example.com");
        when(user.getPasswordHash()).thenReturn("storedHash");
        when(userRepository.findByPublicIdAndDeletedAtIsNull(publicId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctCurrent", "storedHash"))
                .thenReturn(true);
        when(userRepository.existsByEmailAndDeletedAtIsNull("new@example.com"))
                .thenReturn(false);

        UpdateUserRequest request = new UpdateUserRequest("Name", "new@example.com", "correctCurrent");

        userService.updateProfile(publicId, request);

        verify(user).setEmail("new@example.com");
        verify(refreshTokenService).revokeAllSessions(publicId);
    }

    @Test
    void deleteUser_callsSoftDelete() {
        UUID publicId = UUID.randomUUID();
        User user = mock(User.class);
        when(userRepository.findByPublicIdAndDeletedAtIsNull(publicId))
                .thenReturn(Optional.of(user));

        userService.deleteUser(publicId);

        verify(user).delete();
    }

    @Test
    void updateRoles_adminRemovingOwnAdminRole_throwsSelfModification() {
        UUID adminId = UUID.randomUUID();
        User user = mock(User.class);
        when(userRepository.findByPublicIdAndDeletedAtIsNull(adminId))
                .thenReturn(Optional.of(user));

        Set<String> roles = Set.of("ROLE_USER");
        assertThatThrownBy(() ->
                userService.updateRoles(adminId, adminId, roles))
                .isInstanceOf(SelfModificationException.class);
    }

    @Test
    void updateRoles_adminKeepingOwnAdminRole_succeeds() {
        UUID adminId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getRoles()).thenReturn(new java.util.HashSet<>());
        when(userRepository.findByPublicIdAndDeletedAtIsNull(adminId))
                .thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(mock(Role.class)));
        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(mock(Role.class)));

        Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_USER");
        assertThatCode(() ->
                userService.updateRoles(adminId, adminId, roles))
                .doesNotThrowAnyException();
    }

    @Test
    void updateRoles_withNonexistentRole_throwsRoleNotFound() {
        UUID adminId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        User user = mock(User.class);
        when(userRepository.findByPublicIdAndDeletedAtIsNull(targetId))
                .thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_FAKE"))
                .thenReturn(Optional.empty());

        Set<String> roles = Set.of("ROLE_FAKE");
        assertThatThrownBy(() ->
                userService.updateRoles(adminId, targetId, roles))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    void updateStatus_adminDisablingSelf_throwsSelfModification() {
        UUID adminId = UUID.randomUUID();
        User user = mock(User.class);
        when(userRepository.findByPublicIdAndDeletedAtIsNull(adminId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                userService.updateStatus(adminId, adminId, false))
                .isInstanceOf(SelfModificationException.class);

        verify(user, never()).setEnabled(anyBoolean());
    }

    @Test
    void updateStatus_disablingAnotherUser_succeeds() {
        UUID adminId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        User user = mock(User.class);
        when(userRepository.findByPublicIdAndDeletedAtIsNull(targetId))
                .thenReturn(Optional.of(user));

        userService.updateStatus(adminId, targetId, false);

        verify(user).setEnabled(false);
    }
}