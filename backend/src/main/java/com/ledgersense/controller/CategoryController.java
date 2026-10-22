package com.ledgersense.controller;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.domain.Category;
import com.ledgersense.dto.CategoryResponse;
import com.ledgersense.dto.CreateCategoryRequest;
import com.ledgersense.dto.RenameCategoryRequest;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IAuditService;
import com.ledgersense.service.impl.ICategoryService;
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
@RequestMapping("/api/categories")
public class CategoryController {

    private final ICategoryService categoryService;
    private final IAuditService auditService;

    public CategoryController(ICategoryService categoryService, IAuditService auditService) {
        this.categoryService = categoryService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                   @Valid @RequestBody CreateCategoryRequest request) {
        CurrentUser current = CurrentUser.from(jwt);
        Category category = categoryService.create(current.orgId(), request.name(), request.type());
        auditService.record(AuditAction.CATEGORY_CREATED, current.orgId(), current.id(),
                category.getId(), category.getName()); // NEW

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(category.getId()).toUri();
        return ResponseEntity.created(location).body(CategoryResponse.from(category));
    }

    @GetMapping
    public List<CategoryResponse> readAll(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser current = CurrentUser.from(jwt);
        return categoryService.readAll(current.orgId()).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoryResponse read(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        CurrentUser current = CurrentUser.from(jwt);
        return CategoryResponse.from(categoryService.read(id, current.orgId()));
    }

    @PatchMapping("/{id}/name")
    public CategoryResponse rename(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                   @Valid @RequestBody RenameCategoryRequest request) {
        CurrentUser current = CurrentUser.from(jwt);
        Category category = categoryService.rename(id, current.orgId(), request.name()); // CHANGED
        auditService.record(AuditAction.CATEGORY_RENAMED, current.orgId(), current.id(),
                id, category.getName()); // NEW
        return CategoryResponse.from(category);
    }

    // No DELETE yet, on purpose: once transactions point at categories, deleting one
    // needs a rule (move them to "Other"?). We'll add it with the Transaction slice.
}
