package com.ledgersense.repository;

import com.ledgersense.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findsByTokenHash(String tokenHash);

    // NEW: @Query = write the query yourself when the method name can't express it.
    // @Modifying = this query CHANGES data (UPDATE), not just reads.
    // One UPDATE revokes every active token for that user (used for reuse detection).
    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.userId = :userId and t.revokedAt is null")
    int revokeAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
