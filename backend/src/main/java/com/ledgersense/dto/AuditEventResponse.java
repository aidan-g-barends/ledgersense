package com.ledgersense.dto;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.domain.AuditEvent;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(UUID id, AuditAction action, UUID userId, UUID targetId,
                                 String ipAddress, String detail, Instant createdAt) {

    public static AuditEventResponse from(AuditEvent e) {
        return new AuditEventResponse(e.getId(), e.getAction(), e.getUserId(), e.getTargetId(),
                e.getIpAddress(), e.getDetail(), e.getCreatedAt());
    }
}