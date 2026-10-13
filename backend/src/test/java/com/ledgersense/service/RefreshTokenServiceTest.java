package com.ledgersense.service;

import com.ledgersense.domain.RefreshToken;
import com.ledgersense.exception.InvalidCredentialsException;
import com.ledgersense.factory.RefreshTokenFactory;
import com.ledgersense.repository.IRefreshTokenRepository;
import com.ledgersense.security.TokenHasher;
import com.ledgersense.service.impl.IRefreshTokenService.RotationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, 7);
    }

    private RefreshToken tokenFor(String raw) {
        return RefreshTokenFactory.createRefreshToken(
                userId, TokenHasher.sha256Hex(raw), Instant.now().plus(7, ChronoUnit.DAYS));
    }

    @Test
    void rotate() {
        RefreshToken current = tokenFor("old-raw-token");
        Mockito.when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex("old-raw-token")))
                .thenReturn(Optional.of(current));

        RotationResult result = refreshTokenService.rotate("old-raw-token");

        assertEquals(userId, result.userId());
        assertNotEquals("old-raw-token", result.newRawToken());
        assertTrue(current.isRevoked());
    }

    // A revoked token used again = theft. Every token for that user must be revoked.
    @Test
    void rotate_whenTokenReused() {
        RefreshToken current = tokenFor("old-raw-token");
        current.revoke(Instant.now());
        Mockito.when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex("old-raw-token")))
                .thenReturn(Optional.of(current));

        assertThrows(InvalidCredentialsException.class, () -> {
            refreshTokenService.rotate("old-raw-token");
        });

        Mockito.verify(refreshTokenRepository).revokeAllForUser(Mockito.eq(userId), Mockito.any());
    }
}