package com.ledgersense.controller;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.domain.FlagStatus;
import com.ledgersense.dto.AnomalyFlagResponse;
import com.ledgersense.dto.BankTransactionResponse;
import com.ledgersense.dto.ReviewFlagRequest;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IAnomalyFlagService;
import com.ledgersense.service.impl.IAnomalyFlagService.FlagView;
import com.ledgersense.service.impl.IAuditService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// NEW: the three endpoints from the frontend contract
@RestController
@RequestMapping("/api/flags")
public class AnomalyFlagController {

    private final IAnomalyFlagService anomalyFlagService;
    private final IAuditService auditService;

    public AnomalyFlagController(IAnomalyFlagService anomalyFlagService, IAuditService auditService) {
        this.anomalyFlagService = anomalyFlagService;
        this.auditService = auditService;
    }

    // GET /api/flags?status=OPEN. A bad status ("banana") = 400 via your type-mismatch handler.
    @GetMapping
    public List<AnomalyFlagResponse> list(@AuthenticationPrincipal Jwt jwt,
                                          @RequestParam(defaultValue = "OPEN") FlagStatus status) {
        CurrentUser current = CurrentUser.from(jwt);
        return anomalyFlagService.list(current.orgId(), status).stream()
                .map(AnomalyFlagResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/review")
    public AnomalyFlagResponse review(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                      @Valid @RequestBody ReviewFlagRequest request) {
        CurrentUser current = CurrentUser.from(jwt);
        FlagView view = anomalyFlagService.review(id, current.orgId(), current.id(), request.status());

        auditService.record(AuditAction.FLAG_REVIEWED, current.orgId(), current.id(), id,
                "Marked " + request.status());

        return AnomalyFlagResponse.from(view);
    }

    @GetMapping("/{id}/history")
    public List<BankTransactionResponse> history(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        CurrentUser current = CurrentUser.from(jwt);
        return anomalyFlagService.history(id, current.orgId()).stream()
                .map(BankTransactionResponse::from)
                .toList();
    }
}