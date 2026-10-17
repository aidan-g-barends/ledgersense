package com.ledgersense.factory;

import com.ledgersense.domain.Category;
import com.ledgersense.domain.CategoryType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoryFactoryTest {

    private final UUID orgId = UUID.randomUUID();

    @Test
    void createCategory() {
        Category category = CategoryFactory.createCategory(orgId, "  Fuel  ", CategoryType.EXPENSE);

        assertNotNull(category);
        assertEquals("Fuel", category.getName());
    }

    @Test
    void createCategory_withBlankName() {
        Category category = CategoryFactory.createCategory(orgId, "   ", CategoryType.EXPENSE);

        assertNull(category);
    }

    @Test
    void createCategory_withoutType() {
        Category category = CategoryFactory.createCategory(orgId, "Fuel", null);

        assertNull(category);
    }
}
