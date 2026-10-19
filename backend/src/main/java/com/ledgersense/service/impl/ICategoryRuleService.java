package com.ledgersense.service.impl;

import com.ledgersense.domain.CategoryRule;

import java.util.List;
import java.util.UUID;

public interface ICategoryRuleService {

    CategoryRule create(UUID orgId, UUID categoryId, String keyword);

    List<CategoryRule> readAll(UUID orgId);

    void delete(UUID id, UUID orgId);
}