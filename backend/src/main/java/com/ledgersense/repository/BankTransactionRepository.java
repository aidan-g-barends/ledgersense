package com.ledgersense.repository;

import com.ledgersense.domain.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID> {

    Optional<BankTransaction> findByIdAndOrgId(UUID id, UUID orgId);

    // Used during import: "have we already saved this exact row?"
    boolean existsByBankAccountIdAndFingerprint(UUID bankAccountId, String fingerprint);

    // The paginated, filtered list query comes next session (Wed's plan slot)
}
