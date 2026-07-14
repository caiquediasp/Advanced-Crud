package com.caique.AdvancedCrud.shared.errorlog;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record CriticalErrorEvent(
        UUID eventId,
        String exceptionType,
        String message,
        String requestPath,
        String stackTrace,
        Instant occurredAt
) implements Serializable {
}
