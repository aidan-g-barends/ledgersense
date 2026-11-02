"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { Organization } from "@/lib/types";
import { FormError } from "@/components/auth/form-error";
import { btnPrimary, card, inputClass } from "@/lib/ui";

// NEW: view + rename the business (GET /api/organization, PATCH /api/organization/name)
export function BusinessSection() {
  const [org, setOrg] = useState<Organization | null>(null);
  const [name, setName] = useState("");
  const [busy, setBusy] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    api<Organization>("/organization")
      .then((o) => {
        if (!ignore) {
          setOrg(o);
          setName(o.name);
        }
      })
      .catch((err) => {
        if (!ignore) setError(err instanceof ApiError ? err.message : "Could not load your business details.");
      });
    return () => {
      ignore = true;
    };
  }, []);

  // WHY: only enable Save when there's a real, non-empty change
  const changed = org !== null && name.trim() !== "" && name.trim() !== org.name;

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    if (!changed) return;
    setBusy(true);
    setError(null);
    setSaved(false);
    try {
      const updated = await api<Organization>("/organization/name", {
        method: "PATCH",
        body: { name: name.trim() },
      });
      setOrg(updated);
      setName(updated.name);
      setSaved(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not save the new name.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <section aria-labelledby="business-heading" className={`${card} space-y-4 p-5`}>
      <div>
        <h2 id="business-heading" className="text-base font-semibold text-fg">Business</h2>
        <p className="mt-1 text-sm text-fg-muted">The name shown across LedgerSense.</p>
      </div>

      <form onSubmit={handleSave} className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <div className="min-w-0 flex-1 space-y-1">
          <label htmlFor="business-name" className="text-xs font-medium text-fg-muted">Business name</label>
          <input
            id="business-name"
            value={name}
            maxLength={150}
            disabled={!org}
            onChange={(e) => {
              setName(e.target.value);
              setSaved(false);
            }}
            className={inputClass}
          />
        </div>
        <button type="submit" disabled={!changed || busy} className={btnPrimary}>
          Save
        </button>
      </form>

      {org && (
        <p className="text-xs text-fg-faint">
          Base currency: <span className="font-mono">{org.baseCurrency}</span>
        </p>
      )}
      {saved && <p role="status" className="text-xs text-positive">Saved.</p>}
      <FormError message={error} />
    </section>
  );
}