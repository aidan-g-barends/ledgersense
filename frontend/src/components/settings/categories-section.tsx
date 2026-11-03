"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { Category } from "@/lib/types";
import { FormError } from "@/components/auth/form-error";
import { btnPrimary, card, inputClass, labelCaps } from "@/lib/ui";
import { InlineRename } from "./inline-rename";

// Same order as the backend: expenses first, then A to Z
function sortCategories(list: Category[]): Category[] {
  return [...list].sort((a, b) => a.type.localeCompare(b.type) || a.name.localeCompare(b.name));
}

// NEW: list, add and rename categories (GET/POST /api/categories, PATCH /api/categories/{id}/name)
export function CategoriesSection() {
  const [categories, setCategories] = useState<Category[] | null>(null);
  const [name, setName] = useState("");
  const [type, setType] = useState<Category["type"]>("EXPENSE");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    api<Category[]>("/categories")
      .then((list) => {
        if (!ignore) setCategories(list);
      })
      .catch((err) => {
        if (!ignore) setError(err instanceof ApiError ? err.message : "Could not load categories.");
      });
    return () => {
      ignore = true;
    };
  }, []);

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const created = await api<Category>("/categories", { method: "POST", body: { name: name.trim(), type } });
      setCategories((list) => sortCategories([...(list ?? []), created]));
      setName("");
    } catch (err) {
      // e.g. 409 "A category with this name already exists" (case-insensitive check in the backend)
      setError(err instanceof ApiError ? err.message : "Could not add the category.");
    } finally {
      setBusy(false);
    }
  }

  // Passed to InlineRename. Throwing here makes InlineRename show the error.
  async function rename(id: string, newName: string) {
    const updated = await api<Category>(`/categories/${id}/name`, { method: "PATCH", body: { name: newName } });
    setCategories((list) => sortCategories((list ?? []).map((c) => (c.id === id ? updated : c))));
  }

  const groups: { title: string; type: Category["type"] }[] = [
    { title: "Expenses", type: "EXPENSE" },
    { title: "Income", type: "INCOME" },
  ];

  return (
    <section aria-labelledby="categories-heading" className={`${card} space-y-4 p-5`}>
      <div>
        <h2 id="categories-heading" className="text-base font-semibold text-fg">Categories</h2>
        <p className="mt-1 text-sm text-fg-muted">
          Rename the defaults to match how you think about your business, or add your own.
        </p>
      </div>

      <form onSubmit={handleAdd} className="grid gap-2 sm:grid-cols-[1fr_10rem_auto]">
        <input
          aria-label="New category name"
          placeholder="e.g. Equipment"
          required
          maxLength={50}
          value={name}
          onChange={(e) => setName(e.target.value)}
          className={inputClass}
        />
        <select
          aria-label="Category type"
          value={type}
          onChange={(e) => setType(e.target.value as Category["type"])}
          className={inputClass}
        >
          <option value="EXPENSE">Expense</option>
          <option value="INCOME">Income</option>
        </select>
        <button type="submit" disabled={busy} className={btnPrimary}>
          Add category
        </button>
      </form>

      <FormError message={error} />

      {categories && (
        <div className="grid gap-4 sm:grid-cols-2">
          {groups.map((group) => (
            <div key={group.type} className="min-w-0 space-y-2">
              <p className={labelCaps}>{group.title}</p>
              <ul className="divide-y divide-line-subtle rounded border border-line">
                {categories
                  .filter((c) => c.type === group.type)
                  .map((c) => (
                    <li key={c.id} className="flex items-center px-3 py-1.5">
                      <InlineRename
                        value={c.name}
                        label={c.name}
                        maxLength={50}
                        onSave={(newName) => rename(c.id, newName)}
                      />
                    </li>
                  ))}
              </ul>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}