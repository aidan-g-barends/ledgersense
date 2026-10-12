package com.ledgersense.dto;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Organization;

import java.util.UUID;

// "Who am I?" The frontend calls this to show the user's name, org, etc.
public record MeResponse(UUID userId, String email, String role,
                         UUID organizationId, String organizationName, String baseCurrency) {

    public static MeResponse from(AppUser user, Organization org) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                org.getId(),
                org.getName(),
                String.valueOf(org.getBaseCurrency()));
    }
}