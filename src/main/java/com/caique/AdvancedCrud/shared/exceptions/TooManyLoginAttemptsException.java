package com.caique.AdvancedCrud.shared.exceptions;

public class TooManyLoginAttemptsException extends RuntimeException {
    public TooManyLoginAttemptsException() {
        super("Too many login attempts. Try again later.");
    }
}
