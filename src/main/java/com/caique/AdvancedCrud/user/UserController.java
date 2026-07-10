package com.caique.AdvancedCrud.user;

import com.caique.AdvancedCrud.auth.token.RefreshTokenService;
import com.caique.AdvancedCrud.user.dto.ChangePasswordRequest;
import com.caique.AdvancedCrud.user.dto.UpdateUserRequest;
import com.caique.AdvancedCrud.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public UserController(UserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        UUID publicId = UUID.fromString(jwt.getSubject());
        return userService.getByPublicId(publicId);
    }

    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt
            , @Valid @RequestBody UpdateUserRequest request) {

        UUID publicId = UUID.fromString(jwt.getSubject());
        return userService.updateProfile(publicId, request);
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Jwt jwt
            , @Valid @RequestBody ChangePasswordRequest request) {
        UUID publicId = UUID.fromString(jwt.getSubject());
        userService.changePassword(publicId, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal Jwt jwt) {
        UUID publicId = UUID.fromString(jwt.getSubject());
        userService.deleteUser(publicId);
    }

    @PostMapping("/me/logout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userService.logout(userId);
    }

}
