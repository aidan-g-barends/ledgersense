package com.ledgersense.controller;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.domain.CategoryRule;
import com.ledgersense.dto.CategoryRuleResponse;
import com.ledgersense.dto.CreateCategoryRuleRequest;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IAuditService;
import com.ledgersense.service.impl.ICategoryRuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/category-rules")
public class CategoryRuleController {

    private final ICategoryRuleService categoryRuleService;
    private final IAuditService auditService;

    public CategoryRuleController(ICategoryRuleService categoryRuleService, IAuditService auditService) {
        this.categoryRuleService = categoryRuleService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<CategoryRuleResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                       @Valid @RequestBody CreateCategoryRuleRequest request) {
        CurrentUser current = CurrentUser.from(jwt);
        CategoryRule rule = categoryRuleService.create(current.orgId(), request.categoryId(), request.keyword());
        auditService.record(AuditAction.CATEGORY_RULE_CREATED, current.orgId(), current.id(),
                rule.getId(), rule.getKeyword()); // NEW

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(rule.getId()).toUri();
        return ResponseEntity.created(location).body(CategoryRuleResponse.from(rule));
    }

    @GetMapping
    public List<CategoryRuleResponse> readAll(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser current = CurrentUser.from(jwt);
        return categoryRuleService.readAll(current.orgId()).stream()
                .map(CategoryRuleResponse::from)
                .toList();
    }

    // NEW: first DELETE endpoint. 204 No Content = "done, nothing to send back"
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        CurrentUser current = CurrentUser.from(jwt);
        categoryRuleService.delete(id, current.orgId());
        auditService.record(AuditAction.CATEGORY_RULE_DELETED, current.orgId(), current.id(),
                id, null); // NEW
        return ResponseEntity.noContent().build();
    }
}