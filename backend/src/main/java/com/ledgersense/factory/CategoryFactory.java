package com.ledgersense.factory;

import com.ledgersense.domain.Category;
import com.ledgersense.domain.CategoryType;
import com.ledgersense.util.Helper;

import java.util.UUID;

public class CategoryFactory {

    public static Category createCategory(UUID orgId, String name, CategoryType type) {
        if (orgId == null || type == null) {
            return null;
        }
        if (Helper.isNullOrBlank(name)) {
            return null;
        }
        String cleanName = name.strip(); // clean first, then validate the clean value
        if (!Helper.isWithinLength(cleanName, 50)) {
            return null;
        }

        return new Category.Builder()
                .setOrgId(orgId)
                .setName(cleanName)
                .setType(type)
                .build();
    }
}
