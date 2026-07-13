package com.caique.AdvancedCrud.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAddressRequest(
        @NotBlank @Pattern(regexp = "\\d{8}", message = "Zipcode must be 8 digits") String zipcode,
        @NotBlank @Size(max = 255) String street,
        @Size(max = 8) String number,
        @Size(max = 100) String complement,
        @NotBlank @Size(max = 100) String neighborhood,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "State must be 2 uppercase letters") String state
) {
}
