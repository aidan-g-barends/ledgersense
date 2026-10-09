package com.ledgersense.controller;

import com.ledgersense.domain.Organization;
import com.ledgersense.dto.CreateOrganizationRequest;
import com.ledgersense.dto.OrganizationResponse;
import com.ledgersense.dto.RenameOrganizationRequest;
import com.ledgersense.service.OrganizationService;
import com.ledgersense.service.impl.IOrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private IOrganizationService organizationService;

    public OrganizationController(IOrganizationService organizationService){
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest request) {
        Organization org = organizationService.create(request.name(), request.baseCurrency());
        return ResponseEntity
                .created(URI.create("/api/organizations/" + org.getId()))
                .body(OrganizationResponse.from(org));
    }

    // GET /api/organizations/{id} instead of /read/{id}
    @GetMapping("/{id}")
    public OrganizationResponse read(@PathVariable UUID id) {
        return OrganizationResponse.from(organizationService.read(id));
    }

    // PATCH = update PART of something (just the name)
    @PatchMapping("/{id}/name")
    public OrganizationResponse rename(@PathVariable UUID id,
                                       @Valid @RequestBody RenameOrganizationRequest request) {
        return OrganizationResponse.from(organizationService.rename(id, request.name()));
    }
}

