package com.ledgersense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "import_batch")
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "bank_account_id", nullable = false, updatable = false)
    private UUID bankAccountId;

    @Column(name = "file_name", nullable = false, length = 255, updatable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImportStatus status;

    @Column(name = "rows_total", nullable = false)
    private int rowsTotal;

    @Column(name = "rows_imported", nullable = false)
    private int rowsImported;

    @Column(name = "rows_skipped", nullable = false)
    private int rowsSkipped;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ImportBatch() {
    }

    private ImportBatch(Builder builder) {
        this.orgId = builder.orgId;
        this.bankAccountId = builder.bankAccountId;
        this.fileName = builder.fileName;
        this.status = builder.status;
    }

    // NEW: behaviour lives on the entity (DDD). The import service calls this at the end.
    public void complete(int total, int imported, int skipped, Instant now) {
        this.rowsTotal = total;
        this.rowsImported = imported;
        this.rowsSkipped = skipped;
        this.status = ImportStatus.COMPLETED;
        this.completedAt = now;
    }

    public void fail(Instant now) {
        this.status = ImportStatus.FAILED;
        this.completedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getBankAccountId() { return bankAccountId; }
    public String getFileName() { return fileName; }
    public ImportStatus getStatus() { return status; }
    public int getRowsTotal() { return rowsTotal; }
    public int getRowsImported() { return rowsImported; }
    public int getRowsSkipped() { return rowsSkipped; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }

    public static class Builder {
        private UUID orgId;
        private UUID bankAccountId;
        private String fileName;
        private ImportStatus status;

        public Builder setOrgId(UUID orgId) { this.orgId = orgId; return this; }
        public Builder setBankAccountId(UUID bankAccountId) { this.bankAccountId = bankAccountId; return this; }
        public Builder setFileName(String fileName) { this.fileName = fileName; return this; }
        public Builder setStatus(ImportStatus status) { this.status = status; return this; }

        public ImportBatch build() {
            return new ImportBatch(this);
        }
    }
}
