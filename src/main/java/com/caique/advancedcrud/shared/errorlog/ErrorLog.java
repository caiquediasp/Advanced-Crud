package com.caique.advancedcrud.shared.errorlog;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "error_logs")
@Getter
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "exception_type", nullable = false)
    private String exceptionType;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "request_path", length = 500)
    private String requestPath;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected ErrorLog() {
    }

    public ErrorLog(UUID eventId, String exceptionType, String message,
                    String requestPath, String stackTrace, Instant occurredAt) {
        this.eventId = eventId;
        this.exceptionType = exceptionType;
        this.message = message;
        this.requestPath = requestPath;
        this.stackTrace = stackTrace;
        this.occurredAt = occurredAt;
    }


}
