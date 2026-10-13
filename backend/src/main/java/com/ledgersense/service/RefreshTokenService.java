package com.ledgersense.service;

import com.ledgersense.domain.RefreshToken;
import com.ledgersense.exception.InvalidCredentialsException;
import com.ledgersense.factory.RefreshTokenFactory;
import com.ledgersense.repository.IRefreshTokenRepository;
import com.ledgersense.security.TokenHasher;
import com.ledgersense.service.impl.IRefreshTokenService;
import com.ledgersense.util.Helper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService implements IRefreshTokenService {

    // SecureRandom = cryptographically strong. NEVER use java.util.Random for tokens (it's predictable).
    private static final SecureRandom RANDOM = new SecureRandom();

    private final IRefreshTokenRepository refreshTokenRepository;
    private final Duration ttl;

    public RefreshTokenService(IRefreshTokenRepository refreshTokenRepository, @Value("${app.jwt.refresh-token-days:7}") long refreshTokenDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.ttl = Duration.ofDays(refreshTokenDays);
    }

    // Creates a random token, stores its HASH, returns the RAW value (for the cookie)
    @Override
    @Transactional
    public String issue(UUID userId) {
        byte[] bytes = new byte[32];               // 32 bytes = 256 random bits
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = RefreshTokenFactory.createRefreshToken(
                userId, TokenHasher.sha256Hex(raw), Instant.now().plus(ttl));
        if (token == null) {
            throw new IllegalArgumentException("Could not create refresh token");
        }

        refreshTokenRepository.save(token);
        return raw;
    }

    // IMPORTANT: noRollbackFor. By default, throwing an exception ROLLS BACK the transaction.
    // In the reuse case we revoke all tokens and THEN throw, and we need that revoke to stay saved.
    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public RotationResult rotate(String rawToken) {
        if (Helper.isNullOrEmpty(rawToken)) {
            throw new InvalidCredentialsException("Session expired, please log in again");
        }

        RefreshToken current = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(rawToken))
                .orElseThrow(() -> new InvalidCredentialsException("Session expired, please log in again"));

        Instant now = Instant.now();

        // REUSE DETECTION: a revoked token being used means someone else has a copy of it.
        // We can't tell who is real, so kill ALL sessions for this user. They just log in again.
        if (current.isRevoked()) {
            refreshTokenRepository.revokeAllForUser(current.getUserId(), now);
            throw new InvalidCredentialsException("Session expired, please log in again");
        }

        if (current.isExpired(now)) {
            throw new InvalidCredentialsException("Session expired, please log in again");
        }

        // ROTATION: the old token dies, a new one is born
        current.revoke(now);
        String newRaw = issue(current.getUserId());

        return new RotationResult(current.getUserId(), newRaw);
    }

    // Logout. A missing or unknown token is fine: there's nothing to revoke.
    @Override
    @Transactional
    public void revoke(String rawToken) {
        if (Helper.isNullOrEmpty(rawToken)) {
            return;
        }
        refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(rawToken))
                .ifPresent(token -> token.revoke(Instant.now()));
    }

    @Override
    public Duration getTtl() {
        return ttl;
    }
}