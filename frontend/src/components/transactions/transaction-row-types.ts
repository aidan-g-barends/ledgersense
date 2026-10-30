import type { Category, Transaction } from "@/lib/types";

export type TransactionRowProps = {
  tx: Transaction;
  categories: Category[];
  categoriesById: Record<string, Category>;
  onChanged: () => void;
};