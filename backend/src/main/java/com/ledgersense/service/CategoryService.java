package com.ledgersense.service;

import com.ledgersense.domain.Category;
import com.ledgersense.domain.CategoryType;
import com.ledgersense.exception.DuplicateResourceException;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.CategoryFactory;
import com.ledgersense.repository.CategoryRepository;
import com.ledgersense.service.impl.ICategoryService;
import com.ledgersense.util.Helper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService implements ICategoryService {

    // NEW: starter categories for a South African small business
    static final List<String> DEFAULT_EXPENSES = List.of(
            "Bank Fees", "Fuel & Transport", "Groceries", "Insurance", "Marketing",
            "Professional Services", "Rent", "Salaries & Wages", "SARS & Tax",
            "Software & Subscriptions", "Stock & Supplies", "Telecoms & Internet",
            "Utilities", "Other Expense");

    static final List<String> DEFAULT_INCOME = List.of(
            "Sales", "Interest Received", "Refunds", "Other Income");

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public Category create(UUID orgId, String name, CategoryType type) {
        Category category = CategoryFactory.createCategory(orgId, name, type);
        if (category == null) {
            throw new IllegalArgumentException("Invalid category details");
        }
        if (categoryRepository.existsByOrgIdAndNameIgnoreCase(orgId, category.getName())) {
            throw new DuplicateResourceException("A category with this name already exists");
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> readAll(UUID orgId) {
        return categoryRepository.findAllByOrgIdOrderByTypeAscNameAsc(orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public Category read(UUID id, UUID orgId) {
        return categoryRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Override
    @Transactional
    public Category rename(UUID id, UUID orgId, String newName) {
        if (Helper.isNullOrBlank(newName) || !Helper.isWithinLength(newName.strip(), 50)) {
            throw new IllegalArgumentException("Invalid category name");
        }
        String cleanName = newName.strip();
        Category category = read(id, orgId);

        // WHY the equalsIgnoreCase check: renaming "fuel" to "Fuel" is the SAME category,
        // so it must be allowed. Only block if ANOTHER category already has that name.
        boolean onlyCaseChanged = category.getName().equalsIgnoreCase(cleanName);
        if (!onlyCaseChanged && categoryRepository.existsByOrgIdAndNameIgnoreCase(orgId, cleanName)) {
            throw new DuplicateResourceException("A category with this name already exists");
        }
        category.rename(cleanName);
        return category;
    }

    @Override
    @Transactional
    public void seedDefaults(UUID orgId) {
        List<Category> defaults = new ArrayList<>();
        for (String name : DEFAULT_EXPENSES) {
            defaults.add(CategoryFactory.createCategory(orgId, name, CategoryType.EXPENSE));
        }
        for (String name : DEFAULT_INCOME) {
            defaults.add(CategoryFactory.createCategory(orgId, name, CategoryType.INCOME));
        }
        // WHY saveAll: one call instead of 18 separate save() calls
        categoryRepository.saveAll(defaults);
    }
}
