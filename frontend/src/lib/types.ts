// NEW: TypeScript shapes of what the backend sends. One place, used everywhere.

export type Bank = "FNB" | "ABSA" | "STANDARD_BANK" | "NEDBANK" | "CAPITEC" | "OTHER";

export const BANK_LABELS: Record<Bank, string> = {
  FNB: "FNB",
  ABSA: "Absa",
  STANDARD_BANK: "Standard Bank",
  NEDBANK: "Nedbank",
  CAPITEC: "Capitec",
  OTHER: "Other",
};

export type BankAccount = {
  id: string;
  name: string;
  bank: Bank;
  accountLast4: string;
  currency: string;
  createdAt: string;
  hasCsvPreset: boolean;
};

export type Category = { id: string; name: string; type: "EXPENSE" | "INCOME" };

export type ParsedRow = {
  rowNumber: number;
  postedOn: string;
  description: string;
  amount: number;
  balance: number | null;
};

export type RowError = { rowNumber: number; message: string };

export type ImportPreview = {
  totalRows: number;
  validRows: number;
  errorCount: number;
  rows: ParsedRow[];
  errors: RowError[];
};

export type ImportResult = {
  batchId: string;
  fileName: string;
  totalRows: number;
  imported: number;
  duplicates: number;
  errorCount: number;
  errors: RowError[];
};

export type ImportBatch = {
  id: string;
  bankAccountId: string;
  fileName: string;
  status: "PENDING" | "COMPLETED" | "FAILED";
  rowsTotal: number;
  rowsImported: number;
  rowsSkipped: number;
  createdAt: string;
};

export type Transaction = {
  id: string;
  postedOn: string;
  description: string;
  amount: number;
  balance: number | null;
  bankAccountId: string;
  categoryId: string | null;
  merchantId: string | null;
};

export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

// NEW
export type Organization = {
  id: string;
  name: string;
  baseCurrency: string;
  createdAt: string 
};

// NEW
export type CategoryRule = {
   id: string;
   keyword: string;
    categoryId: string 
  };

  // NEW: one row of the audit log (GET /api/audit-events)
export type AuditEvent = {
  id: string;
  action: string;
  userId: string | null;
  targetId: string | null;
  ipAddress: string | null;
  detail: string | null;
  createdAt: string;
};

// ============================================================
// NEW: ANOMALY FLAGS. This is the API contract the backend will implement:
//   GET   /api/flags?status=OPEN|LEGITIMATE|DISPUTED  -> AnomalyFlag[]
//   PATCH /api/flags/{id}/review  { status: "LEGITIMATE" | "DISPUTED" }  -> AnomalyFlag
// ============================================================
export type FlagRule = "DUPLICATE_CHARGE" | "CATEGORY_OUTLIER" | "PRICE_INCREASE" | "NEW_MERCHANT";
export type FlagSeverity = "CRITICAL" | "WARNING" | "INFO";
export type FlagStatus = "OPEN" | "LEGITIMATE" | "DISPUTED";
export type ReviewDecision = Exclude<FlagStatus, "OPEN">; // you can't review something back to OPEN

export type AnomalyFlag = {
  id: string;
  rule: FlagRule;
  severity: FlagSeverity;
  status: FlagStatus;
  title: string;                                 // "Possible duplicate charge"
  explanation: string;                           // one plain sentence: WHY it was flagged
  evidence: { label: string; value: string }[];  // the numbers behind it, in display order
  transaction: Transaction;                      // the flagged transaction, embedded
  createdAt: string;
  reviewedAt: string | null;
};
