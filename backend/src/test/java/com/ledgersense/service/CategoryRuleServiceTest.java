package com.ledgersense.service;

import com.ledgersense.exception.DuplicateResourceException;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.repository.ICategoryRuleRepository;
import com.ledgersense.service.impl.ICategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryRuleServiceTest {

    @Mock
    private ICategoryRuleRepository categoryRuleRepository;

    @Mock
    private ICategoryService categoryService;

    private CategoryRuleService categoryRuleService;
    private final UUID orgId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        categoryRuleService = new CategoryRuleService(categoryRuleRepository, categoryService);
    }

    // NEW: the category belongs to another org, so read() throws and no rule is created
    @Test
    void create_whenCategoryFromOtherOrg() {
        when(categoryService.read(categoryId, orgId))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> categoryRuleService.create(orgId, categoryId, "WOOLWORTHS"));
    }

    @Test
    void create_whenDuplicate() {
        when(categoryRuleRepository.existsByOrgIdAndKeyword(orgId, "WOOLWORTHS")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> categoryRuleService.create(orgId, categoryId, "woolworths"));
    }
}