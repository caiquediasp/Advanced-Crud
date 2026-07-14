package com.caique.advancedcrud.shared.errorlog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    boolean existsByEventId(UUID eventId);
}
