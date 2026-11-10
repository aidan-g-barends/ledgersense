package com.ledgersense.repository;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.FlagStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAnomalyFlagRepository extends JpaRepository<AnomalyFlag, UUID> {

    // WHY Top200: a safety cap so one huge org can't make the queue page return 50 000 rows
    List<AnomalyFlag> findTop200ByOrgIdAndStatusOrderByCreatedAtDesc(UUID orgId, FlagStatus status);

    Optional<AnomalyFlag> findByIdAndOrgId(UUID id, UUID orgId);
}