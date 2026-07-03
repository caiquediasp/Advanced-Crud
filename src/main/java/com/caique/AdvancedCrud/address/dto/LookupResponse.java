package com.caique.AdvancedCrud.address.dto;

public record LookupResponse(
        String zipcode,
        String street,
        String neighborhood,
        String city,
        String state
) {
}
