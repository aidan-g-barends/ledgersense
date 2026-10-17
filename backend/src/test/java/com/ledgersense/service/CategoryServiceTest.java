package com.ledgersense.service;

import com.ledgersense.domain.Category;
import com.ledgersense.domain.CategoryType;
import com.ledgersense.exception.DuplicateResourceException;
import com.ledgersense.factory.CategoryFactory;
import com.ledgersense.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository);
    }

    @Test
    void create() {
        Category category = CategoryFactory.createCategory(orgId, "Equipment", CategoryType.EXPENSE);
        when(categoryRepository.existsByOrgIdAndNameIgnoreCase(orgId, "Equipment")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category created = categoryService.create(orgId, "Equipment", CategoryType.EXPENSE);

        assertEquals("Equipment", created.getName());
    }

    @Test
    void create_whenDuplicate() {
        when(categoryRepository.existsByOrgIdAndNameIgnoreCase(orgId, "Fuel")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> categoryService.create(orgId, "Fuel", CategoryType.EXPENSE));
    }

    @Test
    void seedDefaults() {
        categoryService.seedDefaults(orgId);

        verify(categoryRepository).saveAll(anyList());
    }
}
