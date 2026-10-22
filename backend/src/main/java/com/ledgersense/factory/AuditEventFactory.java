package com.ledgersense.factory;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.domain.AuditEvent;

import java.util.UUID;

public class AuditEventFactory {

    public static AuditEvent createAuditEvent(AuditAction action, UUID orgId, UUID userId,
                                              UUID targetId, String ipAddress, String detail) {
        // Only the action is required
        if (action == null) {
            return null;
        }
        // WHY clean instead of reject: an audit row should never be lost over a cosmetic field.
        // A weird IP gets dropped, and a long detail gets cut to fit.
        String cleanIp = (ipAddress == null || ipAddress.length() > 45) ? null : ipAddress;
        String cleanDetail = null;
        if (detail != null && !detail.isBlank()) {
            String stripped = detail.strip();
            cleanDetail = stripped.length() > 500 ? stripped.substring(0, 500) : stripped;
        }

        return new AuditEvent.Builder()
                .setAction(action)
                .setOrgId(orgId)
                .setUserId(userId)
                .setTargetId(targetId)
                .setIpAddress(cleanIp)
                .setDetail(cleanDetail)
                .build();
    }
}