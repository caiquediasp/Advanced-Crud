package com.caique.AdvancedCrud.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email
) {
}
