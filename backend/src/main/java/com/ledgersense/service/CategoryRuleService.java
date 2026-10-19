package com.ledgersense.service;

import com.ledgersense.domain.CategoryRule;
import com.ledgersense.exception.DuplicateResourceException;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.CategoryRuleFactory;
import com.ledgersense.repository.ICategoryRuleRepository;
import com.ledgersense.service.impl.ICategoryRuleService;
import com.ledgersense.service.impl.ICategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryRuleService implements ICategoryRuleService {

    private final ICategoryRuleRepository categoryRuleRepository;
    private final ICategoryService categoryService;

    public CategoryRuleService(ICategoryRuleRepository categoryRuleRepository,
                               ICategoryService categoryService) {
        this.categoryRuleRepository = categoryRuleRepository;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional
    public CategoryRule create(UUID orgId, UUID categoryId, String keyword) {
        CategoryRule rule = CategoryRuleFactory.createCategoryRule(orgId, categoryId, keyword);
        if (rule == null) {
            throw new IllegalArgumentException("Invalid rule details");
        }

        // NEW: THE security check of the day. categoryId came from the client.
        // read(id, orgId) throws 404 if that category isn't in THIS org.
        // Without this line, org A could attach rules to org B's categories.
        categoryService.read(categoryId, orgId);

        if (categoryRuleRepository.existsByOrgIdAndKeyword(orgId, rule.getKeyword())) {
            throw new DuplicateResourceException("A rule for this keyword already exists");
        }
        return categoryRuleRepository.save(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryRule> readAll(UUID orgId) {
        return categoryRuleRepository.findByOrgId(orgId);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID orgId) {
        CategoryRule rule = categoryRuleRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found"));
        categoryRuleRepository.delete(rule);
    }
}