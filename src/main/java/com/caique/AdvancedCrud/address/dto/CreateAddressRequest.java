package com.caique.AdvancedCrud.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAddressRequest(
        @NotBlank @Pattern(regexp = "\\d{8}", message = "Zipcode must be 8 digits") String zipcode,
        @NotBlank String street,
        @Size(max = 8) String number,
        String complement,
        @NotBlank String neighborhood,
        @NotBlank String city,
        @NotBlank @Size(min = 2, max = 2) String state
) {
}
