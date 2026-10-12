package com.ledgersense.dto;

import com.ledgersense.domain.AppUser;

import java.util.UUID;

public record RegisterResponse(UUID id, String email, UUID organizationId, String role) {

    public static RegisterResponse from (AppUser appUser){
        return new RegisterResponse(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getOrgId(),
                appUser.getRole().name()
        );
    }
}
