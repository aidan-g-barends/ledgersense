import type { FlagRule, FlagSeverity } from "@/lib/types";

// NEW: one place for how each severity and rule LOOKS and is NAMED

export const SEVERITY_STYLES: Record<FlagSeverity, { stripe: string; chip: string; label: string }> = {
  CRITICAL: {
    stripe: "border-l-critical",
    chip: "border-critical/30 bg-critical/10 text-critical",
    label: "Critical",
  },
  WARNING: {
    stripe: "border-l-warning",
    chip: "border-warning/30 bg-warning/10 text-warning",
    label: "Review",
  },
  INFO: {
    stripe: "border-l-primary",
    chip: "border-primary/30 bg-primary/10 text-primary",
    label: "Info",
  },
};

export const RULE_LABELS: Record<FlagRule, string> = {
  DUPLICATE_CHARGE: "Duplicate charge",
  CATEGORY_OUTLIER: "Unusual amount",
  PRICE_INCREASE: "Price increase",
  NEW_MERCHANT: "New merchant",
};

// Sort order: critical first
export const SEVERITY_RANK: Record<FlagSeverity, number> = { CRITICAL: 0, WARNING: 1, INFO: 2 };