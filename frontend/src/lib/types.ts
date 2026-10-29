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