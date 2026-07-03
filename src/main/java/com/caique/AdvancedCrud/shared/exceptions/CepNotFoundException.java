package com.caique.AdvancedCrud.shared.exceptions;

public class CepNotFoundException extends RuntimeException {
    public CepNotFoundException(String cep) {
        super("CEP not found: " + cep);
    }
}
