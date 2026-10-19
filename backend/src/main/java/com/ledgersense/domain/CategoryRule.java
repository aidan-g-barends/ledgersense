package com.ledgersense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

// WHY no setters, no rename, no updated_at: a rule is immutable.
// Want a different keyword or category? Delete it and make a new one. Simpler, fewer bugs.
@Entity
@Table(name = "category_rule")
public class CategoryRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "category_id", nullable = false, updatable = false)
    private UUID categoryId;

    @Column(nullable = false, length = 100, updatable = false)
    private String keyword;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CategoryRule() {
    }

    private CategoryRule(Builder builder) {
        this.orgId = builder.orgId;
        this.categoryId = builder.categoryId;
        this.keyword = builder.keyword;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getCategoryId() { return categoryId; }
    public String getKeyword() { return keyword; }
    public Instant getCreatedAt() { return createdAt; }

    public static class Builder {
        private UUID orgId;
        private UUID categoryId;
        private String keyword;

        public Builder setOrgId(UUID orgId) { this.orgId = orgId; return this; }
        public Builder setCategoryId(UUID categoryId) { this.categoryId = categoryId; return this; }
        public Builder setKeyword(String keyword) { this.keyword = keyword; return this; }

        public CategoryRule build() {
            return new CategoryRule(this);
        }
    }
}