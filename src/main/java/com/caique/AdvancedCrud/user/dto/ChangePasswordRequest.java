package com.caique.AdvancedCrud.user.dto;

import com.caique.AdvancedCrud.shared.validations.MaxBytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8) @MaxBytes(72) String newPassword
) {
}
