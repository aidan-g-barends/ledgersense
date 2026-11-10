package com.ledgersense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// NEW: one "label: value" line of evidence, e.g. "Each charge: R 299,00"
@Embeddable
public class FlagEvidence {

    @Column(nullable = false, length = 50)
    private String label;

    // WHY name = "detail": "value" is a keyword in some databases, so we avoid it as a column name
    @Column(name = "detail", nullable = false, length = 100)
    private String value;

    protected FlagEvidence() {
    }

    public FlagEvidence(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public String getValue() { return value; }
}