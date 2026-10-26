package com.ledgersense.repository;

import com.ledgersense.domain.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// CHANGED: added JpaSpecificationExecutor. Gives us findAll(spec, pageable) for filtered paging.
public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID>,
        JpaSpecificationExecutor<BankTransaction> {

    Optional<BankTransaction> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByBankAccountIdAndFingerprint(UUID bankAccountId, String fingerprint);

    // NEW: "which of these fingerprints are already saved?" in ONE query.
    // WHY: calling existsBy... per row = 5000 queries for a 5000-row file (the N+1 problem).
    @Query("select t.fingerprint from BankTransaction t " +
            "where t.bankAccountId = :bankAccountId and t.fingerprint in :fingerprints")
    Set<String> findExistingFingerprints(@Param("bankAccountId") UUID bankAccountId,
                                         @Param("fingerprints") Collection<String> fingerprints);
}

