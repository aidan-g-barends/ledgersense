package com.ledgersense.domain;

// NEW: which detection rule raised the flag. Names match the frontend contract exactly.
public enum FlagRule {
    DUPLICATE_CHARGE,
    CATEGORY_OUTLIER,
    PRICE_INCREASE,
    NEW_MERCHANT
}