package com.ledgersense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryType type;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Category() {
    }

    private Category(Builder builder) {
        this.orgId = builder.orgId;
        this.name = builder.name;
        this.type = builder.type;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private UUID orgId;
        private String name;
        private CategoryType type;

        public Builder setOrgId(UUID orgId) { this.orgId = orgId; return this; }
        public Builder setName(String name) { this.name = name; return this; }
        public Builder setType(CategoryType type) { this.type = type; return this; }

        public Category build() {
            return new Category(this);
        }
    }
}