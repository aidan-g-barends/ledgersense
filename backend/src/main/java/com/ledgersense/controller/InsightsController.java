package com.ledgersense.controller;

import com.ledgersense.dto.InsightsSummaryResponse;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IInsightsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// NEW: read-only numbers for the Overview. The Timeline endpoint joins this controller tomorrow.
@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final IInsightsService insightsService;

    public InsightsController(IInsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/summary")
    public InsightsSummaryResponse summary(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser current = CurrentUser.from(jwt);
        return InsightsSummaryResponse.from(insightsService.summary(current.orgId()));
    }
}