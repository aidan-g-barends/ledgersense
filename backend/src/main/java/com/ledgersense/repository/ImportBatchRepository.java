package com.ledgersense.repository;

import com.ledgersense.domain.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, UUID> {

    List<ImportBatch> findAllByOrgIdOrderByCreatedAtDesc(UUID orgId);

    Optional<ImportBatch> findByIdAndOrgId(UUID id, UUID orgId);
}
