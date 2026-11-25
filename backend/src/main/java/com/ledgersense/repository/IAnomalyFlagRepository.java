package com.ledgersense.repository;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.FlagStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;      // NEW
import org.springframework.data.repository.query.Param;    // NEW
import java.time.LocalDate;                                 // NEW
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAnomalyFlagRepository extends JpaRepository<AnomalyFlag, UUID> {

    // WHY Top200: a safety cap so one huge org can't make the queue page return 50 000 rows
    List<AnomalyFlag> findTop200ByOrgIdAndStatusOrderByCreatedAtDesc(UUID orgId, FlagStatus status);

    Optional<AnomalyFlag> findByIdAndOrgId(UUID id, UUID orgId);

    // NEW: one date per flag, for flags on transactions posted between two dates.
    // WHY only dates: the timeline just counts flags per day, so loading whole flags would be wasted work.
    // "from AnomalyFlag f, BankTransaction t where f.transactionId = t.id" = a join on our UUID column.
    @Query("select t.postedOn from AnomalyFlag f, BankTransaction t " +
            "where f.transactionId = t.id and f.orgId = :orgId and t.postedOn between :from and :to")
    List<LocalDate> findFlaggedDays(@Param("orgId") UUID orgId,
                                    @Param("from") LocalDate from,
                                    @Param("to") LocalDate to);
}