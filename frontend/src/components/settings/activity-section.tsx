"use client";

import { useCallback, useEffect, useState } from "react";
import { RefreshCw } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import type { AuditEvent } from "@/lib/types";
import { formatDateTime } from "@/lib/money";
import { FormError } from "@/components/auth/form-error";
import { btnSecondary, card } from "@/lib/ui";

// NEW: backend action codes -> words a business owner understands
const ACTION_LABELS: Record<string, string> = {
  REGISTER: "Account created",
  LOGIN_SUCCESS: "Signed in",
  ORGANIZATION_RENAMED: "Business renamed",
  BANK_ACCOUNT_CREATED: "Bank account added",
  BANK_ACCOUNT_RENAMED: "Bank account renamed",
  CATEGORY_CREATED: "Category added",
  CATEGORY_RENAMED: "Category renamed",
  CATEGORY_RULE_CREATED: "Rule added",
  CATEGORY_RULE_DELETED: "Rule deleted",
  IMPORT_COMPLETED: "Statement imported",
  TRANSACTION_RECATEGORISED: "Transaction recategorised",
};

// NEW: read-only audit trail (GET /api/audit-events, newest 50)
export function ActivitySection() {
  const [events, setEvents] = useState<AuditEvent[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // WHY useCallback: the same function is used on mount AND by the Refresh button
  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setEvents(await api<AuditEvent[]>("/audit-events"));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load activity.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let ignore = false;
    api<AuditEvent[]>("/audit-events")
      .then((list) => {
        if (!ignore) setEvents(list);
      })
      .catch((err) => {
        if (!ignore) setError(err instanceof ApiError ? err.message : "Could not load activity.");
      });
    return () => {
      ignore = true;
    };
  }, []);

  return (
    <section aria-labelledby="activity-heading" className={`${card} space-y-4 p-5`}>
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 id="activity-heading" className="text-base font-semibold text-fg">Activity</h2>
          <p className="mt-1 text-sm text-fg-muted">The last 50 changes and sign-ins on this business. This log can&apos;t be edited.</p>
        </div>
        <button type="button" onClick={load} disabled={loading} aria-label="Refresh activity" className={btnSecondary}>
          <RefreshCw className={`size-4 ${loading ? "animate-spin" : ""}`} aria-hidden="true" />
        </button>
      </div>

      <FormError message={error} />

      {events && events.length === 0 && <p className="text-sm text-fg-muted">No activity yet.</p>}

      {events && events.length > 0 && (
        <ul className="divide-y divide-line-subtle rounded border border-line">
          {events.map((e) => (
            <li key={e.id} className="flex flex-wrap items-baseline gap-x-3 gap-y-1 px-3 py-2">
              <span className="text-sm font-medium text-fg">{ACTION_LABELS[e.action] ?? e.action}</span>
              {e.detail && <span className="min-w-0 flex-1 truncate text-sm text-fg-muted">{e.detail}</span>}
              <span className="ml-auto flex items-baseline gap-3 font-mono text-xs text-fg-faint">
                {e.ipAddress && <span>{e.ipAddress}</span>}
                <time dateTime={e.createdAt}>{formatDateTime(e.createdAt)}</time>
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}