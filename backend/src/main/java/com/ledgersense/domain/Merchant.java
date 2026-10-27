package com.ledgersense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "merchant")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(nullable = false, length = 100, updatable = false)
    private String name;

    // The only thing that changes: the category the user told us to remember
    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "first_seen_on", nullable = false, updatable = false)
    private LocalDate firstSeenOn;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Merchant() {
    }

    private Merchant(Builder builder) {
        this.orgId = builder.orgId;
        this.name = builder.name;
        this.firstSeenOn = builder.firstSeenOn;
    }

    public void assignCategory(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public String getName() { return name; }
    public UUID getCategoryId() { return categoryId; }
    public LocalDate getFirstSeenOn() { return firstSeenOn; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private UUID orgId;
        private String name;
        private LocalDate firstSeenOn;

        public Builder setOrgId(UUID orgId) { this.orgId = orgId; return this; }
        public Builder setName(String name) { this.name = name; return this; }
        public Builder setFirstSeenOn(LocalDate firstSeenOn) { this.firstSeenOn = firstSeenOn; return this; }

        public Merchant build() {
            return new Merchant(this);
        }
    }
}