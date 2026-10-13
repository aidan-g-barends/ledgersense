package com.ledgersense.factory;

import com.ledgersense.domain.RefreshToken;
import com.ledgersense.util.Helper;

import java.time.Instant;
import java.util.UUID;

public class RefreshTokenFactory {

    public static RefreshToken createRefreshToken(UUID userId, String tokenHash, Instant expiresAt) {
        if (userId == null) {
            return null;
        }
        // A SHA-256 hex hash is always exactly 64 characters
        if (Helper.isNullOrEmpty(tokenHash) || tokenHash.length() != 64) {
            return null;
        }
        // An already-expired token makes no sense
        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            return null;
        }

        return new RefreshToken.Builder()
                .setUserId(userId)
                .setTokenHash(tokenHash)
                .setExpiresAt(expiresAt)
                .build();
    }
}