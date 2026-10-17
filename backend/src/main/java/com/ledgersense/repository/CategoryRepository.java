package com.ledgersense.repository;

import com.ledgersense.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // Sorted EXPENSE first, then INCOME, then A to Z inside each
    List<Category> findAllByOrgIdOrderByTypeAscNameAsc(UUID orgId);

    Optional<Category> findByIdAndOrgId(UUID id, UUID orgId);

    // NEW: "IgnoreCase" in the method name makes Spring compare lower(name) = lower(?)
    // Matches the lower(name) unique index in V5.
    boolean existsByOrgIdAndNameIgnoreCase(UUID orgId, String name);
}
