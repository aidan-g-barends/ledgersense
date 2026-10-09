package com.ledgersense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at" , nullable = false)
    private Instant updatedAt;

    protected AppUser(){}

    private AppUser(Builder builder){
        this.orgId = builder.orgId;
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.role = builder.role;
    }

    // Business method instead of a setter: called after a successful login
    public void recordLogin(Instant at){
        this.lastLoginAt = at;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", orgId=" + orgId +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", role=" + role +
                ", lastLoginAt=" + lastLoginAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public static class Builder{
        private UUID orgId;
        private String email;
        private String passwordHash;
        private Role role;


    public Builder setOrgId(UUID orgId) {
        this.orgId = orgId;
        return this;
    }

    public Builder setEmail(String email) {
        this.email = email;
        return this;
    }

    public Builder setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public Builder setRole(Role role) {
        this.role = role;
        return this;
    }

    public AppUser build(){
     return new AppUser(this);
    }
  }
}
