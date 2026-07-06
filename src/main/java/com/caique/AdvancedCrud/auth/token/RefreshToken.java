package com.caique.AdvancedCrud.auth.token;

import java.io.Serializable;
import java.util.UUID;

public record RefreshToken(
        UUID tokenId,
        UUID familyId,
        UUID userId,
        boolean used
) implements Serializable {
}
