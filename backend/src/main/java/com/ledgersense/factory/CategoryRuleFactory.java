package com.ledgersense.factory;

import com.ledgersense.domain.CategoryRule;
import com.ledgersense.util.Helper;

import java.util.Locale;
import java.util.UUID;

public class CategoryRuleFactory {
    public static CategoryRule create(UUID orgId, UUID categoryId, String keyword){
        if(orgId == null || categoryId == null || Helper.isNullOrEmpty(keyword)){
            return null;
        }

        // WHY uppercase: bank descriptions are mostly UPPERCASE, and we compare in uppercase.
        // Storing it uppercase means "woolworths" and "WOOLWORTHS" are the same rule.
        String cleanKeyword = keyword.strip().toUpperCase(Locale.ROOT);

        // WHY min 2: a 1-letter keyword like "A" would match almost every transaction
        if (cleanKeyword.length() < 2 || !Helper.isWithinLength(cleanKeyword, 100)) {
            return null;
        }

        return new CategoryRule.Builder()
                .setOrgId(orgId)
                .setCategoryId(categoryId)
                .setKeyword(cleanKeyword)
                .build();
    }
}
