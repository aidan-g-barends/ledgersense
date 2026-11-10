package com.ledgersense.repository;

import com.ledgersense.domain.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate; // NEW
import java.util.List;      //

import java.time.Instant;
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

    // NEW: one UPDATE for every transaction from a merchant.
    // flushAutomatically: save pending changes first. clearAutomatically: drop now-stale cached rows after.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update BankTransaction t set t.categoryId = :categoryId, t.updatedAt = :now " +
            "where t.orgId = :orgId and t.merchantId = :merchantId")
    int recategoriseMerchant(@Param("orgId") UUID orgId,
                             @Param("merchantId") UUID merchantId,
                             @Param("categoryId") UUID categoryId,
                             @Param("now") Instant now);

    // NEW: the detection "window": an org's transactions between two dates, oldest first
    List<BankTransaction> findAllByOrgIdAndPostedOnBetweenOrderByPostedOnAscCreatedAtAsc(
            UUID orgId, LocalDate from, LocalDate to);

    // NEW: a merchant's last 12 charges, for the investigation drawer's history chart
    List<BankTransaction> findTop12ByOrgIdAndMerchantIdOrderByPostedOnDescCreatedAtDesc(UUID orgId, UUID merchantId);

    // NEW: fallback when a row has no merchant: match the exact description instead
    List<BankTransaction> findTop12ByOrgIdAndDescriptionOrderByPostedOnDescCreatedAtDesc(UUID orgId, String description);
}



