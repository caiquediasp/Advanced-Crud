package com.caique.advancedcrud.shared.exceptions;

import java.util.UUID;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(UUID publicId) {
        super("Address not found with id: " + publicId);
    }
}
