package com.caique.AdvancedCrud.auth.dto;

import com.caique.AdvancedCrud.shared.validations.MaxBytes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 150) String name,
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 8) @MaxBytes(72) String password) {
}
