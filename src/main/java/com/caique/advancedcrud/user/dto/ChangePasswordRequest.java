package com.caique.advancedcrud.user.dto;

import com.caique.advancedcrud.shared.validations.MaxBytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8) @MaxBytes(72) String newPassword
) {
}
