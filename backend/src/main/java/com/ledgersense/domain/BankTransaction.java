package com.ledgersense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bank_transaction")
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "bank_account_id", nullable = false, updatable = false)
    private UUID bankAccountId;

    @Column(name = "import_batch_id", nullable = false, updatable = false)
    private UUID importBatchId;

    // The only thing that changes after import: which category it's in (nullable = uncategorised)
    @Column(name = "category_id")
    private UUID categoryId;

    // WHY LocalDate not Instant: banks give a DATE ("2026-10-19"), not a moment in time
    @Column(name = "posted_on", nullable = false, updatable = false)
    private LocalDate postedOn;

    @Column(nullable = false, length = 500, updatable = false)
    private String description;

    // WHY precision/scale: must match NUMERIC(14,2) or ddl validate fails
    @Column(nullable = false, precision = 14, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(precision = 14, scale = 2, updatable = false)
    private BigDecimal balance;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 64, updatable = false)
    private String fingerprint;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BankTransaction() {
    }

    private BankTransaction(Builder builder) {
        this.orgId = builder.orgId;
        this.bankAccountId = builder.bankAccountId;
        this.importBatchId = builder.importBatchId;
        this.categoryId = builder.categoryId;
        this.postedOn = builder.postedOn;
        this.description = builder.description;
        this.amount = builder.amount;
        this.balance = builder.balance;
        this.fingerprint = builder.fingerprint;
    }

    public void categorise(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isMoneyOut() {
        return amount.signum() < 0; // signum: -1, 0 or 1. Safer than comparing to BigDecimal.ZERO
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getBankAccountId() { return bankAccountId; }
    public UUID getImportBatchId() { return importBatchId; }
    public UUID getCategoryId() { return categoryId; }
    public LocalDate getPostedOn() { return postedOn; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalance() { return balance; }
    public String getFingerprint() { return fingerprint; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private UUID orgId;
        private UUID bankAccountId;
        private UUID importBatchId;
        private UUID categoryId;
        private LocalDate postedOn;
        private String description;
        private BigDecimal amount;
        private BigDecimal balance;
        private String fingerprint;

        public Builder setOrgId(UUID orgId) { this.orgId = orgId; return this; }
        public Builder setBankAccountId(UUID bankAccountId) { this.bankAccountId = bankAccountId; return this; }
        public Builder setImportBatchId(UUID importBatchId) { this.importBatchId = importBatchId; return this; }
        public Builder setCategoryId(UUID categoryId) { this.categoryId = categoryId; return this; }
        public Builder setPostedOn(LocalDate postedOn) { this.postedOn = postedOn; return this; }
        public Builder setDescription(String description) { this.description = description; return this; }
        public Builder setAmount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder setBalance(BigDecimal balance) { this.balance = balance; return this; }
        public Builder setFingerprint(String fingerprint) { this.fingerprint = fingerprint; return this; }

        public BankTransaction build() {
            return new BankTransaction(this);
        }
    }
}
