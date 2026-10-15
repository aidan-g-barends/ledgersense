package com.ledgersense.security;

import com.ledgersense.domain.Role;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public record CurrentUser(UUID id, UUID orgId, Role role) {

    public static CurrentUser from(Jwt jwt){
        return new CurrentUser(
                UUID.fromString(jwt.getSubject()),
                UUID.fromString(jwt.getClaimAsString("org")),
                Role.valueOf(jwt.getClaimAsString("role"))
        );
    }
}
