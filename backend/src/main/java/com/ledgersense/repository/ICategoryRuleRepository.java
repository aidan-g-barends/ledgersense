package com.ledgersense.repository;

import com.ledgersense.domain.CategoryRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICategoryRuleRepository extends JpaRepository <CategoryRule, UUID> {

    List<CategoryRule> findByOrgId(UUID orgId);

    Optional<CategoryRule> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByOrgIdAndKeyword(UUID orgId, String keyword);
}
