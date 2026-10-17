package com.ledgersense.service.impl;

import com.ledgersense.domain.Category;
import com.ledgersense.domain.CategoryType;

import java.util.List;
import java.util.UUID;

public interface ICategoryService {

    Category create(UUID orgId, String name, CategoryType type);

    List<Category> readAll(UUID orgId);

    Category read(UUID id, UUID orgId);

    Category rename(UUID id, UUID orgId, String newName);

    void seedDefaults(UUID orgId);
}
