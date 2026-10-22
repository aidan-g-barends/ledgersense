package com.ledgersense.service;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.domain.AuditEvent;
import com.ledgersense.factory.AuditEventFactory;
import com.ledgersense.repository.AuditEventRepository;
import com.ledgersense.security.ClientIp;
import com.ledgersense.service.impl.IAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService implements IAuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    // NEW: REQUIRES_NEW = always run in a brand-new transaction.
    // WHY: if the caller's transaction rolls back, this audit row still commits.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditAction action, UUID orgId, UUID userId, UUID targetId, String detail) {
        AuditEvent event = AuditEventFactory.createAuditEvent(
                action, orgId, userId, targetId, ClientIp.current(), detail);
        if (event != null) {
            auditEventRepository.save(event);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> latest(UUID orgId) {
        return auditEventRepository.findTop50ByOrgIdOrderByCreatedAtDesc(orgId);
    }
}