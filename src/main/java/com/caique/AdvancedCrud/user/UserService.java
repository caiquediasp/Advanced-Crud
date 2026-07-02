package com.caique.AdvancedCrud.user;

import com.caique.AdvancedCrud.shared.exceptions.EmailAlreadyExistsException;
import com.caique.AdvancedCrud.shared.exceptions.InvalidPasswordException;
import com.caique.AdvancedCrud.shared.exceptions.UserNotFoundException;
import com.caique.AdvancedCrud.user.dto.ChangePasswordRequest;
import com.caique.AdvancedCrud.user.dto.UpdateUserRequest;
import com.caique.AdvancedCrud.user.dto.UserResponse;
import com.caique.AdvancedCrud.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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
    }

    @Transactional
    public void deleteUser(UUID publicId) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new UserNotFoundException(publicId));

        user.delete();
    }

}
