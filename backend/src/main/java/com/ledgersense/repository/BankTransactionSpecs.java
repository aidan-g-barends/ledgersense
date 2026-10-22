package com.ledgersense.repository;

import com.ledgersense.domain.BankTransaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

// NEW: each method is one reusable WHERE condition. The service glues together the ones it needs.
// root = the table row, cb = CriteriaBuilder (builds SQL pieces safely, no string concatenation)
public final class BankTransactionSpecs {

    private BankTransactionSpecs() {
    }

    public static Specification<BankTransaction> inOrg(UUID orgId) {
        return (root, query, cb) -> cb.equal(root.get("orgId"), orgId);
    }

    public static Specification<BankTransaction> inAccount(UUID bankAccountId) {
        return (root, query, cb) -> cb.equal(root.get("bankAccountId"), bankAccountId);
    }

    public static Specification<BankTransaction> inCategory(UUID categoryId) {
        return (root, query, cb) -> cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<BankTransaction> postedOnOrAfter(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("postedOn"), from);
    }

    public static Specification<BankTransaction> postedOnOrBefore(LocalDate to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("postedOn"), to);
    }

    public static Specification<BankTransaction> descriptionContains(String text) {
        // WHY escape: in LIKE, % means "anything" and _ means "any one char".
        // Without escaping, a search for "%" would match every single row.
        String escaped = text.strip().toUpperCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return (root, query, cb) ->
                cb.like(cb.upper(root.get("description")), "%" + escaped + "%", '\\');
    }
}
