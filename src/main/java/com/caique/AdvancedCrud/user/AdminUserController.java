package com.caique.AdvancedCrud.user;

import com.caique.AdvancedCrud.user.dto.UpdateRolesRequest;
import com.caique.AdvancedCrud.user.dto.UpdateStatusRequest;
import com.caique.AdvancedCrud.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{publicId}/roles")
    public UserResponse updateRoles(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable UUID publicId,
                                    @Valid @RequestBody UpdateRolesRequest request) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        return userService.updateRoles(adminId, publicId, request.roles());
    }

    @PatchMapping("/{publicId}/status")
    public UserResponse updateStatus(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable UUID publicId,
                                     @Valid @RequestBody UpdateStatusRequest request) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        return userService.updateStatus(adminId, publicId, request.enabled());
    }

}
