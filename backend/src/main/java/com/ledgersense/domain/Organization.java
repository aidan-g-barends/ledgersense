package com.ledgersense.domain;

// NEW: explicit imports instead of jakarta.persistence.*
// WHY: you can see exactly where each class comes from, and you avoid name clashes.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "organization")
public class Organization {

    public static final int MAX_NAME_LENGTH = 150;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    // NEW: @JdbcTypeCode(SqlTypes.CHAR)
    // WHY: the column is CHAR(3), but Hibernate assumes Strings are VARCHAR(255).
    // Without this, ddl-auto: validate sees the mismatch and the app refuses to start.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    // NEW: createdAt field + @CreationTimestamp
    // WHY: Hibernate fills this automatically when the row is first inserted.
    // updatable = false means it can never change after that.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // NEW: @UpdateTimestamp
    // WHY: without it, updatedAt is null on insert and the DB's NOT NULL rule rejects
    // the row. Hibernate now sets it on every insert AND every update.
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Organization() {

    }


    private Organization(Builder builder) {
        this.name = validName(builder.name);
        this.baseCurrency = toCurrency(builder.baseCurrency).getCurrencyCode();
    }

    // NEW: a business method instead of a setter.

    public void rename(String newName) {
        this.name = validName(newName);
    }

    // NEW: name rules in one place.
    // WHY: both the constructor and rename() call this, so the rules can't drift apart.
    private static String validName(String name) {
        // isBlank() catches null-like input: "", "   ", tabs
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Organization name must not be blank");
        }
        // strip() removes leading/trailing spaces ("  Acme " -> "Acme")
        String trimmed = name.strip();
        // check length AFTER stripping, so spaces don't count toward the limit
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Organization name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    // NEW: currency validation.
    // WHY: stops fake codes like "RAND" or "banana" ever reaching the database.
    private static Currency toCurrency(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Currency code must not be null");
        }
        try {
            // Currency.getInstance() is Java's built-in list of real ISO currencies.
            // toUpperCase lets "zar" work. Locale.ROOT avoids locale bugs
            // (e.g. in Turkish, "i".toUpperCase() is not "I").
            return Currency.getInstance(code.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // Re-throw with a clearer message, keeping the original as the cause.
            throw new IllegalArgumentException("Unknown currency code: " + code, e);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // CHANGED: returns Currency instead of String.
    // WHY: the DB stores text, but the rest of the app gets a real type,
    // so nobody can mix up a currency with any other string.
    public Currency getBaseCurrency() {
        return Currency.getInstance(baseCurrency);
    }

    // NEW: getter for the new createdAt field.
    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Organization{id=" + id + ", name='" + name + "', baseCurrency='" + baseCurrency + "'}";
    }

    public static class Builder {
        // CHANGED: only the fields a caller is allowed to choose.
        // REMOVED: id and updatedAt. Hibernate generates them; letting callers
        // set them could create duplicate IDs or fake timestamps.
        private String name;
        private String baseCurrency;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setBaseCurrency(String baseCurrency) {
            this.baseCurrency = baseCurrency;
            return this;
        }

        // REMOVED: copy().
        // WHY: copy-and-rebuild suits immutable objects. JPA entities are "managed":
        // you load one and change it with methods like rename(). A copy with the
        // same ID could overwrite fields you didn't copy (like createdAt).

        public Organization build() {
            // Validation happens inside the constructor, so build() throws
            // IllegalArgumentException if the data is invalid.
            return new Organization(this);
        }
    }
}