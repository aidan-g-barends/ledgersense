package com.ledgersense.repository;

import com.ledgersense.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    // "Top50" in the method name = LIMIT 50. Newest first.
    List<AuditEvent> findTop50ByOrgIdOrderByCreatedAtDesc(UUID orgId);
}