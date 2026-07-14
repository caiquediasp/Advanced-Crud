package com.caique.advancedcrud.shared.exceptions;

public class SelfModificationException extends RuntimeException {
    public SelfModificationException(String message) {
        super(message);
    }
}
