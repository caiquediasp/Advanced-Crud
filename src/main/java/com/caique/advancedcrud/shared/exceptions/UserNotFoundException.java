package com.caique.advancedcrud.shared.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Object id) {
        super("User with id " + id + " not found");
    }
}
