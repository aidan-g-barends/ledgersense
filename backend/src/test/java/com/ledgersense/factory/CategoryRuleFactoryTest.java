package com.ledgersense.factory;

import com.ledgersense.domain.CategoryRule;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoryRuleFactoryTest {

    private final UUID orgId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @Test
    void createCategoryRule() {
        CategoryRule rule = CategoryRuleFactory.createCategoryRule(orgId, categoryId, "  woolworths ");

        assertNotNull(rule);
        assertEquals("WOOLWORTHS", rule.getKeyword());
    }

    @Test
    void createCategoryRule_withOneLetterKeyword() {
        CategoryRule rule = CategoryRuleFactory.createCategoryRule(orgId, categoryId, "a");

        assertNull(rule);
    }

    @Test
    void createCategoryRule_withoutCategory() {
        CategoryRule rule = CategoryRuleFactory.createCategoryRule(orgId, null, "WOOLWORTHS");

        assertNull(rule);
    }
}