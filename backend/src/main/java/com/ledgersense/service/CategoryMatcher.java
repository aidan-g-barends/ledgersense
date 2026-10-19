package com.ledgersense.service;

import com.ledgersense.domain.CategoryRule;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class CategoryMatcher {

    private CategoryMatcher(){}

    public static UUID match(String description, List<CategoryRule> rules) {
        if (description == null || rules == null) {
            return null;
        }

        String text = description.toUpperCase(Locale.ROOT);

        CategoryRule best = null;

        for (CategoryRule rule : rules) {
            if (text.contains(rule.getKeyword())) {
                if (best == null || rule.getKeyword().length() > best.getKeyword().length()) {
                    best = rule;
                }
            }
        }
        return best == null ? null : best.getCategoryId();

    }
}
