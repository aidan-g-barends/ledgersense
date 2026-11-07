import type { AnomalyFlag } from "@/lib/types";
import { toCsv } from "@/lib/csv";
import { RULE_LABELS } from "@/components/attention/flag-styles";

// NEW: flags as CSV rows, e.g. disputed charges to send to your bank.
// Descriptions are already card-masked by the backend's DescriptionSanitizer.
export function flagsToCsv(flags: AnomalyFlag[]): string {
  return toCsv(
    ["Date", "Description", "Amount", "Rule", "Severity", "Status", "Why flagged", "Reviewed at"],
    flags.map((f) => [
      f.transaction.postedOn,
      f.transaction.description,
      f.transaction.amount,
      RULE_LABELS[f.rule],
      f.severity,
      f.status,
      f.explanation,
      f.reviewedAt,
    ]),
  );
}