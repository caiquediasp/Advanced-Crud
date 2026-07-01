package com.caique.AdvancedCrud.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn) {
}
