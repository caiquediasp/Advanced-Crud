package com.caique.AdvancedCrud.shared.exceptions;

public class SelfModificationException extends RuntimeException {
    public SelfModificationException(String message) {
        super(message);
    }
}
