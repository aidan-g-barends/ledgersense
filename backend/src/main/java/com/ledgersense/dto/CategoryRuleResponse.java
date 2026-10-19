package com.ledgersense.dto;

import com.ledgersense.domain.CategoryRule;

import java.util.UUID;

public record CategoryRuleResponse(UUID id, String keyword, UUID categoryId) {

    public static CategoryRuleResponse from(CategoryRule rule) {
        return new CategoryRuleResponse(rule.getId(), rule.getKeyword(), rule.getCategoryId());
    }
}