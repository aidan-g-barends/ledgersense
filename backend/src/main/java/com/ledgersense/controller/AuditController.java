package com.ledgersense.controller;

import com.ledgersense.dto.AuditEventResponse;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IAuditService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-events")
public class AuditController {

    private final IAuditService auditService;

    public AuditController(IAuditService auditService) {
        this.auditService = auditService;
    }

    // Read-only on purpose: no POST, PATCH or DELETE. The app writes audit rows, never the user.
    @GetMapping
    public List<AuditEventResponse> latest(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser current = CurrentUser.from(jwt);
        return auditService.latest(current.orgId()).stream()
                .map(AuditEventResponse::from)
                .toList();
    }
}