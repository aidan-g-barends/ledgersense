package com.ledgersense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "anomaly_flag")
public class AnomalyFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 30, updatable = false)
    private FlagRule rule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private FlagSeverity severity;

    // The only things that change after creation: status + who reviewed it, and when
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private FlagStatus status;

    @Column(nullable = false, length = 100, updatable = false)
    private String title;

    @Column(nullable = false, length = 500, updatable = false)
    private String explanation;

    // WHY EAGER + BatchSize: open-in-view is off, so evidence must be loaded before the service returns.
    // BatchSize loads the evidence for up to 100 flags in ONE extra query (not one per flag = N+1).
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "anomaly_flag_evidence", joinColumns = @JoinColumn(name = "flag_id"))
    @OrderColumn(name = "sort_order")
    @BatchSize(size = 100)
    private List<FlagEvidence> evidence = new ArrayList<>();

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AnomalyFlag() {
    }

    private AnomalyFlag(Builder builder) {
        this.orgId = builder.orgId;
        this.transactionId = builder.transactionId;
        this.rule = builder.rule;
        this.severity = builder.severity;
        this.title = builder.title;
        this.explanation = builder.explanation;
        this.evidence = new ArrayList<>(builder.evidence);
        this.status = FlagStatus.OPEN; // every flag starts open
    }

    // WHY the check lives HERE: the entity guards its own rules, so no caller can break them
    public void review(FlagStatus decision, UUID reviewedBy, Instant reviewedAt) {
        if (decision == null || decision == FlagStatus.OPEN) {
            throw new IllegalArgumentException("A flag can only be marked LEGITIMATE or DISPUTED");
        }
        this.status = decision;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getTransactionId() { return transactionId; }
    public FlagRule getRule() { return rule; }
    public FlagSeverity getSeverity() { return severity; }
    public FlagStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public String getExplanation() { return explanation; }
    public List<FlagEvidence> getEvidence() { return List.copyOf(evidence); } // read-only copy
    public UUID getReviewedBy() { return reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private UUID orgId;
        private UUID transactionId;
        private FlagRule rule;
        private FlagSeverity severity;
        private String title;
        private String explanation;
        private List<FlagEvidence> evidence = new ArrayList<>();

        public Builder setOrgId(UUID orgId) { this.orgId = orgId; return this; }
        public Builder setTransactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder setRule(FlagRule rule) { this.rule = rule; return this; }
        public Builder setSeverity(FlagSeverity severity) { this.severity = severity; return this; }
        public Builder setTitle(String title) { this.title = title; return this; }
        public Builder setExplanation(String explanation) { this.explanation = explanation; return this; }
        public Builder setEvidence(List<FlagEvidence> evidence) { this.evidence = evidence; return this; }

        public AnomalyFlag build() {
            return new AnomalyFlag(this);
        }
    }
}