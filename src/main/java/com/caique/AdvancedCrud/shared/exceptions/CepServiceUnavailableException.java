package com.caique.AdvancedCrud.shared.exceptions;

public class CepServiceUnavailableException extends RuntimeException {
    public CepServiceUnavailableException() {
        super("CEP lookup service is temporarily unavailable");
    }
}
