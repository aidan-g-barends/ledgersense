import type { AnomalyFlag, Transaction } from "@/lib/types";

// NEW: sample flags matching the anomalies planted in demo-absa-statement.csv.
// Used ONLY while USE_MOCK is true in lib/flags.ts. Delete this file once the backend is live.

function tx(id: string, postedOn: string, description: string, amount: number): Transaction {
  return { id, postedOn, description, amount, balance: null, bankAccountId: "mock-account", categoryId: null, merchantId: null };
}

const cloudHost = tx("mock-tx-1", "2026-10-05", "CLOUD HOST LTD ****5678", -299);
const seaview = tx("mock-tx-2", "2026-10-23", "Seaview Grill ****1234", -2850);
const designTools = tx("mock-tx-3", "2026-10-22", "DESIGN TOOLS INC ****5678", -449);
const officeSupplies = tx("mock-tx-4", "2026-09-08", "Office Supplies Hub ****5678", -1249.99);

export const MOCK_FLAGS: AnomalyFlag[] = [
  {
    id: "mock-flag-1",
    rule: "DUPLICATE_CHARGE",
    severity: "CRITICAL",
    status: "OPEN",
    title: "Possible duplicate charge",
    explanation: "Charged twice on the same day for the same amount. This merchant usually bills once a month.",
    evidence: [
      { label: "Usual pattern", value: "1 charge per month" },
      { label: "Detected", value: "2 charges on 5 Oct" },
      { label: "Each charge", value: "R 299,00" },
    ],
    transaction: cloudHost,
    createdAt: "2026-10-30T08:00:00Z",
    reviewedAt: null,
  },
  {
    id: "mock-flag-2",
    rule: "CATEGORY_OUTLIER",
    severity: "WARNING",
    status: "OPEN",
    title: "Unusually large payment",
    explanation: "Much higher than what you normally spend at restaurants.",
    evidence: [
      { label: "Typical charge", value: "R 120 to R 450" },
      { label: "This charge", value: "R 2 850,00" },
      { label: "Outlier score", value: "6.4 (flagged above 3.5)" },
    ],
    transaction: seaview,
    createdAt: "2026-10-30T08:00:00Z",
    reviewedAt: null,
  },
  {
    id: "mock-flag-3",
    rule: "PRICE_INCREASE",
    severity: "WARNING",
    status: "OPEN",
    title: "Subscription price went up",
    explanation: "This recurring charge is higher than last month.",
    evidence: [
      { label: "Last month", value: "R 349,00 on 22 Sep" },
      { label: "This month", value: "R 449,00" },
      { label: "Change", value: "+R 100,00 (+28.7%)" },
    ],
    transaction: designTools,
    createdAt: "2026-10-30T08:00:00Z",
    reviewedAt: null,
  },
  {
    id: "mock-flag-4",
    rule: "NEW_MERCHANT",
    severity: "INFO",
    status: "OPEN",
    title: "First payment to a new merchant",
    explanation: "You haven't paid this merchant before.",
    evidence: [{ label: "First seen", value: "23 Oct 2026" }],
    transaction: seaview,
    createdAt: "2026-10-30T08:00:00Z",
    reviewedAt: null,
  },
  {
    id: "mock-flag-5",
    rule: "CATEGORY_OUTLIER",
    severity: "WARNING",
    status: "LEGITIMATE",
    title: "Unusually large payment",
    explanation: "Higher than your usual office supplies spend.",
    evidence: [
      { label: "Typical charge", value: "R 300 to R 450" },
      { label: "This charge", value: "R 1 249,99" },
    ],
    transaction: officeSupplies,
    createdAt: "2026-09-10T08:00:00Z",
    reviewedAt: "2026-09-11T09:30:00Z",
  },
];