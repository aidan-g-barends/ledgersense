package com.ledgersense.dto;

import com.ledgersense.domain.Category;
import com.ledgersense.domain.CategoryType;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, CategoryType type) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType());
    }
}
