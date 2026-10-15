package com.ledgersense.controller;

import com.ledgersense.dto.OrganizationResponse;
import com.ledgersense.dto.RenameOrganizationRequest;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IOrganizationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization") // CHANGED: singular, no {id}. "My" organization.
public class OrganizationController {

    private final IOrganizationService organizationService;

    public OrganizationController(IOrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // CHANGED: no POST anymore. Orgs are only created through /api/auth/register.

    @GetMapping
    public OrganizationResponse read(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser current = CurrentUser.from(jwt); // NEW: orgId comes from the token
        return OrganizationResponse.from(organizationService.read(current.orgId()));
    }

    @PatchMapping("/name")
    public OrganizationResponse rename(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody RenameOrganizationRequest request) {
        CurrentUser current = CurrentUser.from(jwt); // NEW
        return OrganizationResponse.from(organizationService.rename(current.orgId(), request.name()));
    }
}