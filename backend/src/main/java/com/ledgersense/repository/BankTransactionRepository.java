package com.ledgersense.repository;

import com.ledgersense.domain.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

// CHANGED: added JpaSpecificationExecutor. Gives us findAll(spec, pageable) for filtered paging.
public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID>,
        JpaSpecificationExecutor<BankTransaction> {

    Optional<BankTransaction> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByBankAccountIdAndFingerprint(UUID bankAccountId, String fingerprint);
}
