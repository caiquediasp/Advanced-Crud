package com.caique.AdvancedCrud.address.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AddressResponse(
        UUID publicId,
        String zipcode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        boolean primary,
        OffsetDateTime createdAt
) {
}
