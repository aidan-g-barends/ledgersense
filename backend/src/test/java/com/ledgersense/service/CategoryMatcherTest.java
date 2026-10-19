package com.ledgersense.service;

import com.ledgersense.domain.CategoryRule;
import com.ledgersense.factory.CategoryRuleFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMatcherTest {

    private final UUID orgId = UUID.randomUUID();
    private final UUID groceries = UUID.randomUUID();
    private final UUID transport = UUID.randomUUID();

    private final List<CategoryRule> rules = List.of(
            CategoryRuleFactory.createCategoryRule(orgId, groceries, "WOOLWORTHS"),
            CategoryRuleFactory.createCategoryRule(orgId, transport, "UBER"),
            CategoryRuleFactory.createCategoryRule(orgId, groceries, "UBER EATS"));

    @Test
    void match() {
        assertEquals(groceries, CategoryMatcher.match("POS PURCHASE Woolworths 4821 CPT", rules));
    }

    @Test
    void match_longestKeywordWins() {
        assertEquals(groceries, CategoryMatcher.match("UBER EATS ORDER 123", rules));
    }

    @Test
    void match_noRule() {
        assertNull(CategoryMatcher.match("SOME RANDOM SHOP", rules));
    }
}