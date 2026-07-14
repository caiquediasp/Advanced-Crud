package com.caique.advancedcrud.user.dto;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID publicId,
        String name,
        String email,
        Set<String> roles,
        OffsetDateTime createdAt
) {
}
