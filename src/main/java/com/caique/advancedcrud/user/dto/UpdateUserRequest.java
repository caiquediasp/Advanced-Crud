package com.caique.advancedcrud.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank @Size(max = 150) String name,
        @Email @NotBlank @Size(max = 255) String email,
        String currentPassword
) {
}
