"use client";

import { Download } from "lucide-react";
import type { AnomalyFlag, FlagRule, FlagStatus } from "@/lib/types";
import { flagsToCsv } from "@/lib/flag-csv";
import { downloadText } from "@/lib/download";
import { btnSecondary, inputClass } from "@/lib/ui";
import { RULE_LABELS } from "./flag-styles";

export type RuleFilter = FlagRule | "ALL";

// WHY the cast: Object.keys always says string[], but we know these keys are FlagRules
const RULES = Object.keys(RULE_LABELS) as FlagRule[];

type Props = {
  rule: RuleFilter;
  onRuleChange: (rule: RuleFilter) => void;
  flags: AnomalyFlag[]; // what's on screen right now (after filtering)
  status: FlagStatus;
};

// NEW: filter by rule + export what you see
export function AttentionToolbar({ rule, onRuleChange, flags, status }: Props) {
  function exportCsv() {
    const today = new Date().toISOString().slice(0, 10);
    downloadText(`ledgersense-flags-${status.toLowerCase()}-${today}.csv`, flagsToCsv(flags));
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      <label htmlFor="rule-filter" className="sr-only">Filter by rule</label>
      {/* WHY the wrapper: inputClass has w-full, so the width is set on a parent instead */}
      <div className="w-44">
        <select id="rule-filter" value={rule} onChange={(e) => onRuleChange(e.target.value as RuleFilter)} className={inputClass}>
          <option value="ALL">All rules</option>
          {RULES.map((r) => (
            <option key={r} value={r}>{RULE_LABELS[r]}</option>
          ))}
        </select>
      </div>
      <button type="button" onClick={exportCsv} disabled={flags.length === 0} className={btnSecondary}>
        <Download className="size-4" aria-hidden="true" />
        Export CSV
      </button>
    </div>
  );
}