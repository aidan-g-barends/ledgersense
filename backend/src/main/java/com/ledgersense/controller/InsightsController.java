package com.ledgersense.controller;

import com.ledgersense.dto.InsightsSummaryResponse;
import com.ledgersense.dto.TimelinePointResponse;                  // NEW
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IInsightsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;      // NEW
import org.springframework.web.bind.annotation.RestController;

import java.util.List;                                              // NEW

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

    // NEW: GET /api/insights/timeline?days=90. "banana" = 400 (type mismatch), days=5 = 400 (range check).
    @GetMapping("/timeline")
    public List<TimelinePointResponse> timeline(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam(defaultValue = "90") int days) {
        CurrentUser current = CurrentUser.from(jwt);
        return insightsService.timeline(current.orgId(), days).stream()
                .map(TimelinePointResponse::from)
                .toList();
    }
}