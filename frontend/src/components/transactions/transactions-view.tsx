"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { ChevronLeft, ChevronRight, Search } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { BANK_LABELS, type BankAccount, type Category, type Page, type Transaction } from "@/lib/types";
import { FormError } from "@/components/auth/form-error";
import { btnPrimary, btnSecondary, card, inputClass, labelCaps } from "@/lib/ui";
import { TransactionRow } from "./transaction-row";

const PAGE_SIZE = 50;
const NO_FILTERS = { q: "", bankAccountId: "", categoryId: "", from: "", to: "" };
type Filters = typeof NO_FILTERS;

type Result = { key: string; data?: Page<Transaction>; error?: string };

export function TransactionsView() {
  const [accounts, setAccounts] = useState<BankAccount[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [searchInput, setSearchInput] = useState("");
  const [filters, setFilters] = useState<Filters>(NO_FILTERS);
  const [page, setPage] = useState(0);
  const [reloadKey, setReloadKey] = useState(0);
  const [result, setResult] = useState<Result | null>(null);

  // Lookups for names in dropdowns and rows. Loaded once.
  useEffect(() => {
    let ignore = false;
    Promise.all([api<BankAccount[]>("/bank-accounts"), api<Category[]>("/categories")])
      .then(([a, c]) => {
        if (!ignore) {
          setAccounts(a);
          setCategories(c);
        }
      })
      .catch(() => {
        // Not fatal: the table still works, just without names in the filters
      });
    return () => {
      ignore = true;
    };
  }, []);

  // NEW: debounce. Only apply the search 300ms after the user STOPS typing.
  useEffect(() => {
    const timer = setTimeout(() => {
      const q = searchInput.trim();
      setFilters((f) => (f.q === q ? f : { ...f, q }));
      setPage(0);
    }, 300);
    return () => clearTimeout(timer); // typed again? cancel the old timer
  }, [searchInput]);

  // The exact query for the current filters + page, e.g. "page=0&size=50&q=spar"
  const query = useMemo(() => {
    const params = new URLSearchParams({ page: String(page), size: String(PAGE_SIZE) });
    for (const [key, value] of Object.entries(filters)) {
      if (value) params.set(key, value); // only send filters that are set
    }
    return params.toString();
  }, [filters, page]);

  const requestKey = `${query}#${reloadKey}`;

  useEffect(() => {
    let ignore = false;
    api<Page<Transaction>>(`/transactions?${query}`)
      .then((data) => {
        if (!ignore) setResult({ key: requestKey, data });
      })
      .catch((err) => {
        if (!ignore)
          setResult({ key: requestKey, error: err instanceof ApiError ? err.message : "Could not load transactions." });
      });
    // WHY: if filters change mid-request, the OLD response arrives late. ignore = true throws it away.
    return () => {
      ignore = true;
    };
  }, [query, requestKey]);

  // NEW: "loading" isn't stored, it's derived: the result we have is for a different request.
  const loading = result?.key !== requestKey;
  const data = result?.data;
  const hasFilters = Object.values(filters).some(Boolean);

  const categoriesById = useMemo(
    () => Object.fromEntries(categories.map((c) => [c.id, c])) as Record<string, Category>,
    [categories],
  );

  function updateFilter(key: keyof Filters, value: string) {
    setFilters((f) => ({ ...f, [key]: value }));
    setPage(0); // new filter = back to the first page
  }

  function clearFilters() {
    setSearchInput("");
    setFilters(NO_FILTERS);
    setPage(0);
  }

  const firstRow = data ? data.page * data.size + 1 : 0;
  const lastRow = data ? data.page * data.size + data.content.length : 0;

  return (
    <div className="space-y-4">
      {/* Filters */}
      <div className={`${card} grid gap-3 p-4 sm:grid-cols-2 lg:grid-cols-6`}>
        <div className="relative lg:col-span-2">
          <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-fg-faint" aria-hidden="true" />
          <input
            type="search"
            aria-label="Search descriptions"
            placeholder="Search, e.g. spar"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className={`${inputClass} pl-9`}
          />
        </div>
        <select
          aria-label="Bank account"
          value={filters.bankAccountId}
          onChange={(e) => updateFilter("bankAccountId", e.target.value)}
          className={inputClass}
        >
          <option value="">All accounts</option>
          {accounts.map((a) => (
            <option key={a.id} value={a.id}>
              {a.name} · {BANK_LABELS[a.bank]}
            </option>
          ))}
        </select>
        <select
          aria-label="Category"
          value={filters.categoryId}
          onChange={(e) => updateFilter("categoryId", e.target.value)}
          className={inputClass}
        >
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <input
          type="date"
          aria-label="From date"
          value={filters.from}
          onChange={(e) => updateFilter("from", e.target.value)}
          className={inputClass}
        />
        <input
          type="date"
          aria-label="To date"
          value={filters.to}
          onChange={(e) => updateFilter("to", e.target.value)}
          className={inputClass}
        />
      </div>

      {result?.error && <FormError message={result.error} />}

      {/* First load */}
      {!data && loading && (
        <div className={`${card} space-y-2 p-4`} aria-busy="true">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-8 animate-pulse rounded bg-sunken" />
          ))}
        </div>
      )}

      {/* Nothing imported yet */}
      {data && data.totalElements === 0 && !hasFilters && (
        <div className={`${card} space-y-3 p-10 text-center`}>
          <p className="text-sm font-medium text-fg">No transactions yet</p>
          <p className="text-sm text-fg-muted">Import a bank statement to see your spending here.</p>
          <Link href="/imports" className={btnPrimary}>
            Import a statement
          </Link>
        </div>
      )}

      {/* Filters matched nothing */}
      {data && data.totalElements === 0 && hasFilters && (
        <div className={`${card} space-y-3 p-10 text-center`}>
          <p className="text-sm text-fg-muted">No transactions match these filters.</p>
          <button type="button" onClick={clearFilters} className={btnSecondary}>
            Clear filters
          </button>
        </div>
      )}

      {/* The table. Dimmed while the next page/filter loads, so it doesn't jump. */}
      {data && data.totalElements > 0 && (
        <div className={`${card} overflow-hidden transition-opacity ${loading ? "opacity-60" : ""}`}>
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead className="bg-sunken">
                <tr>
                  <th className={`${labelCaps} px-4 py-2`}>Date</th>
                  <th className={`${labelCaps} px-4 py-2`}>Description</th>
                  <th className={`${labelCaps} px-4 py-2`}>Category</th>
                  <th className={`${labelCaps} px-4 py-2 text-right`}>Amount</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((tx) => (
                  <TransactionRow
                    key={tx.id}
                    tx={tx}
                    categories={categories}
                    categoriesById={categoriesById}
                    onChanged={() => setReloadKey((k) => k + 1)}
                  />
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between border-t border-line px-4 py-3">
            <p className="font-mono text-xs text-fg-muted">
              {firstRow}–{lastRow} of {data.totalElements}
            </p>
            <div className="flex gap-2">
              <button
                type="button"
                aria-label="Previous page"
                onClick={() => setPage((p) => p - 1)}
                disabled={page === 0 || loading}
                className={btnSecondary}
              >
                <ChevronLeft className="size-4" aria-hidden="true" />
              </button>
              <button
                type="button"
                aria-label="Next page"
                onClick={() => setPage((p) => p + 1)}
                disabled={page + 1 >= data.totalPages || loading}
                className={btnSecondary}
              >
                <ChevronRight className="size-4" aria-hidden="true" />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}