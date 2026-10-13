package com.ledgersense.service.impl;

import java.time.Duration;
import java.util.UUID;

public interface IRefreshTokenService {

    // NEW: a record inside the interface. Rotation returns TWO things: who, and the new token.
    record RotationResult(UUID userId, String newRawToken) {
    }

    String issue(UUID userId);

    RotationResult rotate(String rawToken);

    void revoke(String rawToken);

    Duration getTtl();
}
