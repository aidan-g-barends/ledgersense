package com.ledgersense.service.impl;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.domain.AuditEvent;

import java.util.List;
import java.util.UUID;

public interface IAuditService {

    void record(AuditAction action, UUID orgId, UUID userId, UUID targetId, String detail);

    List<AuditEvent> latest(UUID orgId);
}