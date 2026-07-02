package com.caique.AdvancedCrud.user;

import com.caique.AdvancedCrud.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UserResponse> findAll(Pageable pageable) {
        return userService.listAll(pageable);
    }

    @GetMapping("/{publicId}")
    public UserResponse getByPublicId(@PathVariable UUID publicId) {
        return userService.getByPublicIdAdmin(publicId);
    }

}
