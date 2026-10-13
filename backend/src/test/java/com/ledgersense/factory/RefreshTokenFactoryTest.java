package com.ledgersense.factory;

import com.ledgersense.domain.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenFactoryTest {

    private final String hash = "a".repeat(64);

    @Test
    void createRefreshToken() {
        RefreshToken token = RefreshTokenFactory.createRefreshToken(
                UUID.randomUUID(), hash, Instant.now().plus(7, ChronoUnit.DAYS));

        assertNotNull(token);
        assertFalse(token.isRevoked());
    }

    @Test
    void createRefreshTokenWhenAlreadyExpired() {
        RefreshToken token = RefreshTokenFactory.createRefreshToken(
                UUID.randomUUID(), hash, Instant.now().minus(1, ChronoUnit.MINUTES));

        assertNull(token);
    }
}