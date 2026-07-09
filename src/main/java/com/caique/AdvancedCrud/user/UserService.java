package com.caique.AdvancedCrud.user;

import com.caique.AdvancedCrud.auth.token.RefreshTokenService;
import com.caique.AdvancedCrud.shared.exceptions.*;
import com.caique.AdvancedCrud.user.dto.ChangePasswordRequest;
import com.caique.AdvancedCrud.user.dto.UpdateUserRequest;
import com.caique.AdvancedCrud.user.dto.UserResponse;
import com.caique.AdvancedCrud.user.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void createUser(String name, String email, String password) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not seeded"));

        User user = new User(name, email, passwordEncoder.encode(password));
        user.getRoles().add(userRole);

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getByPublicId(UUID publicId) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new UserNotFoundException(publicId));

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID publicId, UpdateUserRequest request) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new UserNotFoundException(publicId));

        if (!user.getEmail().equals(request.email())
                && userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        user.setName(request.name());
        user.setEmail(request.email());
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(UUID publicId, ChangePasswordRequest request) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new UserNotFoundException(publicId));

        if(!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokenService.revokeAllSessions(publicId);
    }

    @Transactional
    public void deleteUser(UUID publicId) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new UserNotFoundException(publicId));

        user.delete();
        refreshTokenService.revokeAllSessions(publicId);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listAll(Pageable pageable) {
        return userRepository.findAllByDeletedAtIsNull(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getByPublicIdAdmin(UUID publicId) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new UserNotFoundException(publicId));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateRoles(UUID adminId, UUID targetPublicId, Set<String> roles) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(targetPublicId)
                .orElseThrow(() -> new UserNotFoundException(targetPublicId));

        if(adminId.equals(targetPublicId) && !roles.contains("ROLE_ADMIN")) {
            throw new SelfModificationException("Admin cannot remove their own ADMIN role");
        }

        Set<Role> userRoles = roles.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new RoleNotFoundException(name)))
                .collect(Collectors.toSet());

        user.getRoles().clear();
        user.getRoles().addAll(userRoles);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateStatus(UUID adminId, UUID targetPublicId, boolean enabled) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(targetPublicId)
                .orElseThrow(() -> new UserNotFoundException(targetPublicId));

        if(adminId.equals(targetPublicId) && !enabled) {
            throw new SelfModificationException("Admin cannot disable themselves");
        }

        user.setEnabled(enabled);
        return userMapper.toResponse(user);
    }

}