"use client";

import { useEffect, useMemo, useState } from "react";
import { Trash2 } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import type { Category, CategoryRule } from "@/lib/types";
import { FormError } from "@/components/auth/form-error";
import { btnPrimary, card, inputClass } from "@/lib/ui";

// NEW: "description contains X -> category Y" rules (GET/POST/DELETE /api/category-rules)
export function RulesSection() {
  const [rules, setRules] = useState<CategoryRule[] | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [keyword, setKeyword] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    // WHY Promise.all: both requests run at the same time instead of one after the other
    Promise.all([api<CategoryRule[]>("/category-rules"), api<Category[]>("/categories")])
      .then(([r, c]) => {
        if (!ignore) {
          setRules(r);
          setCategories(c);
        }
      })
      .catch((err) => {
        if (!ignore) setError(err instanceof ApiError ? err.message : "Could not load your rules.");
      });
    return () => {
      ignore = true;
    };
  }, []);

  const categoryNames = useMemo(
    () => Object.fromEntries(categories.map((c) => [c.id, c.name])) as Record<string, string>,
    [categories],
  );

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const created = await api<CategoryRule>("/category-rules", {
        method: "POST",
        body: { keyword: keyword.trim(), categoryId },
      });
      setRules((list) => [...(list ?? []), created].sort((a, b) => a.keyword.localeCompare(b.keyword)));
      setKeyword("");
    } catch (err) {
      // e.g. 409 "A rule for this keyword already exists" from the backend
      setError(err instanceof ApiError ? err.message : "Could not add the rule.");
    } finally {
      setBusy(false);
    }
  }

  async function handleDelete(rule: CategoryRule) {
    setError(null);
    try {
      await api(`/category-rules/${rule.id}`, { method: "DELETE" });
      setRules((list) => (list ?? []).filter((r) => r.id !== rule.id));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not delete the rule.");
    }
  }

  return (
    <section aria-labelledby="rules-heading" className={`${card} space-y-4 p-5`}>
      <div>
        <h2 id="rules-heading" className="text-base font-semibold text-fg">Categorisation rules</h2>
        <p className="mt-1 text-sm text-fg-muted">
          When a description contains the keyword, new imports go into that category. To fix existing
          transactions, change them on the Transactions page.
        </p>
      </div>

      <form onSubmit={handleAdd} className="grid gap-2 sm:grid-cols-[1fr_1fr_auto]">
        <input
          aria-label="Keyword"
          placeholder="Keyword, e.g. engen"
          required
          minLength={2}
          maxLength={100}
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className={`${inputClass} font-mono uppercase`}
        />
        <select
          aria-label="Category"
          required
          value={categoryId}
          onChange={(e) => setCategoryId(e.target.value)}
          className={inputClass}
        >
          <option value="">Choose a category…</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <button type="submit" disabled={busy} className={btnPrimary}>
          Add rule
        </button>
      </form>

      {rules && rules.length === 0 && <p className="text-sm text-fg-muted">No rules yet.</p>}

      {rules && rules.length > 0 && (
        <ul className="divide-y divide-line-subtle rounded border border-line">
          {rules.map((rule) => (
            <li key={rule.id} className="flex items-center gap-3 px-3 py-2">
              <span className="font-mono text-xs font-semibold text-fg">{rule.keyword}</span>
              <span className="text-xs text-fg-faint" aria-hidden="true">→</span>
              <span className="min-w-0 flex-1 truncate text-sm text-fg-muted">
                {categoryNames[rule.categoryId] ?? "Unknown category"}
              </span>
              <button
                type="button"
                onClick={() => handleDelete(rule)}
                aria-label={`Delete rule ${rule.keyword}`}
                className="rounded p-1.5 text-fg-faint transition-colors hover:bg-critical/10 hover:text-critical"
              >
                <Trash2 className="size-4" aria-hidden="true" />
              </button>
            </li>
          ))}
        </ul>
      )}

      <FormError message={error} />
    </section>
  );
}