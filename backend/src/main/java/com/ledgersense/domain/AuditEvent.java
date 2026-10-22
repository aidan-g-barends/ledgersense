package com.ledgersense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

// Immutable: every column is updatable = false. An audit log you can edit isn't an audit log.
@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", updatable = false)
    private UUID orgId;

    @Column(name = "user_id", updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, updatable = false)
    private AuditAction action;

    @Column(name = "target_id", updatable = false)
    private UUID targetId;

    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    @Column(length = 500, updatable = false)
    private String detail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuditEvent() {
    }

    private AuditEvent(Builder builder) {
        this.orgId = builder.orgId;
        this.userId = builder.userId;
        this.action = builder.action;
        this.targetId = builder.targetId;
        this.ipAddress = builder.ipAddress;
        this.detail = builder.detail;
    }

    public UUID getId() { return id; }
    public UUID getOrgId() { return orgId; }
    public UUID getUserId() { return userId; }
    public AuditAction getAction() { return action; }
    public UUID getTargetId() { return targetId; }
    public String getIpAddress() { return ipAddress; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }

    public static class Builder {
        private UUID orgId;
        private UUID userId;
        private AuditAction action;
        private UUID targetId;
        private String ipAddress;
        private String detail;

        public Builder setOrgId(UUID orgId) { this.orgId = orgId; return this; }
        public Builder setUserId(UUID userId) { this.userId = userId; return this; }
        public Builder setAction(AuditAction action) { this.action = action; return this; }
        public Builder setTargetId(UUID targetId) { this.targetId = targetId; return this; }
        public Builder setIpAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder setDetail(String detail) { this.detail = detail; return this; }

        public AuditEvent build() {
            return new AuditEvent(this);
        }
    }
}