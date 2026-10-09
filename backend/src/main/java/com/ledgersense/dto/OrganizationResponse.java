package com.ledgersense.dto;

import com.ledgersense.domain.Organization;
import java.time.Instant;
import java.util.UUID;

// NEW: what we SEND BACK. We choose the fields, so nothing internal leaks.
public record OrganizationResponse(UUID id, String name, String baseCurrency, Instant createdAt) {

    // Converts entity -> DTO in one place
    public static OrganizationResponse from(Organization org) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                String.valueOf(org.getBaseCurrency()),
                org.getCreatedAt());
    }
}
