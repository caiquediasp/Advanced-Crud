package com.caique.AdvancedCrud.shared.exceptions;

import java.util.Objects;
import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Object id) {
        super("User with id " + id + " not found");
    }
}
