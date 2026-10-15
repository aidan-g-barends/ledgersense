package com.ledgersense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="bank_account")
public class BankAccount {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @Column(name="org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(nullable = false,updatable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 30, updatable = false)
    private Bank bank;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name="account_last4", nullable = false, length = 4 , updatable = false)
    private String accountLast4;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BankAccount(){}

    private BankAccount(Builder builder){
        this.orgId = builder.orgId;
        this.name = builder.name;
        this.bank = builder.bank;
        this.accountLast4 = builder.accountLast4;
        this.currency = builder.currency;
    }

    public void rename(String newName){
        this.name = newName;
    }

    public static class Builder{

        private UUID orgId;
        private String name;
        private Bank bank;
        private String accountLast4;
        private String currency;

        public Builder setOrgId(UUID orgId) {
            this.orgId = orgId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setBank(Bank bank) {
            this.bank = bank;
            return this;
        }

        public Builder setAccountLast4(String accountLast4) {
            this.accountLast4 = accountLast4;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public BankAccount build(){
            return new BankAccount(this);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public String getName() {
        return name;
    }

    public Bank getBank() {
        return bank;
    }

    public String getAccountLast4() {
        return accountLast4;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
