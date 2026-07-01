package com.caique.AdvancedCrud.user;

import com.caique.AdvancedCrud.shared.exceptions.EmailAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createUser(String name, String email, String password) {
        if(userRepository.existsByEmailAndDeletedAtIsNull(email)) {
           throw new EmailAlreadyExistsException(email);
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not seeded"));

        User user = new User(name, email, passwordEncoder.encode(password));
        user.getRoles().add(userRole);

        userRepository.save(user);
    }


}
