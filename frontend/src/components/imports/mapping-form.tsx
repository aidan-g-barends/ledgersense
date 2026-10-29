"use client";

import { inputClass } from "@/lib/ui";

// NEW: the column layout for banks WITHOUT a preset.
// Inputs are strings because <input> values are always strings.
export type Mapping = {
  delimiter: "," | ";" | "TAB";
  skipLines: string;
  hasHeader: boolean;
  datePattern: string;
  dateColumn: string;
  descriptionColumn: string;
  amountStyle: "single" | "split";
  amountColumn: string;
  debitColumn: string;
  creditColumn: string;
  balanceColumn: string;
};

export const EMPTY_MAPPING: Mapping = {
  delimiter: ",",
  skipLines: "0",
  hasHeader: true,
  datePattern: "dd/MM/yyyy",
  dateColumn: "1",
  descriptionColumn: "2",
  amountStyle: "single",
  amountColumn: "3",
  debitColumn: "",
  creditColumn: "",
  balanceColumn: "",
};

const DATE_FORMATS = [
  { value: "dd/MM/yyyy", example: "26/09/2026" },
  { value: "yyyy/MM/dd", example: "2026/09/26" },
  { value: "yyyy-MM-dd", example: "2026-09-26" },
  { value: "dd MMM yyyy", example: "26 Sep 2026" },
  { value: "ddMMMyyyy", example: "26Sep2026" },
  { value: "MM/dd/yyyy", example: "09/26/2026" },
];

// WHY: people count columns from 1, Java counts from 0. Convert at the edge.
function toIndex(column: string): string {
  return String(Number(column) - 1);
}

export function mappingProblem(m: Mapping): string | null {
  const required =
    m.amountStyle === "single"
      ? [m.dateColumn, m.descriptionColumn, m.amountColumn]
      : [m.dateColumn, m.descriptionColumn, m.debitColumn, m.creditColumn];
  if (required.some((c) => !c || Number(c) < 1)) {
    return "Fill in every required column number (1 or higher).";
  }
  return null;
}

export function appendMapping(form: FormData, m: Mapping) {
  form.append("delimiter", m.delimiter);
  form.append("skipLines", m.skipLines || "0");
  form.append("hasHeader", String(m.hasHeader));
  form.append("datePattern", m.datePattern);
  form.append("dateColumn", toIndex(m.dateColumn));
  form.append("descriptionColumn", toIndex(m.descriptionColumn));
  if (m.amountStyle === "single") {
    form.append("amountColumn", toIndex(m.amountColumn));
  } else {
    form.append("debitColumn", toIndex(m.debitColumn));
    form.append("creditColumn", toIndex(m.creditColumn));
  }
  if (m.balanceColumn) form.append("balanceColumn", toIndex(m.balanceColumn));
}

function ColumnInput({
  id,
  label,
  value,
  onChange,
  optional,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (v: string) => void;
  optional?: boolean;
}) {
  return (
    <div className="space-y-1">
      <label htmlFor={id} className="text-xs font-medium text-fg-muted">
        {label}
        {optional && <span className="text-fg-faint"> (optional)</span>}
      </label>
      <input
        id={id}
        type="number"
        min={1}
        inputMode="numeric"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className={`${inputClass} font-mono`}
      />
    </div>
  );
}

export function MappingForm({ value, onChange }: { value: Mapping; onChange: (m: Mapping) => void }) {
  // Generic setter: set("dateColumn", "2") updates just that field
  function set<K extends keyof Mapping>(key: K, v: Mapping[K]) {
    onChange({ ...value, [key]: v });
  }

  return (
    <div className="space-y-4 rounded border border-line bg-sunken p-4">
      <p className="text-xs text-fg-muted">
        Open your CSV in Notepad and count columns from the left, starting at 1.
      </p>

      <div className="grid gap-3 sm:grid-cols-3">
        <div className="space-y-1">
          <label htmlFor="delimiter" className="text-xs font-medium text-fg-muted">Separator</label>
          <select
            id="delimiter"
            value={value.delimiter}
            onChange={(e) => set("delimiter", e.target.value as Mapping["delimiter"])}
            className={inputClass}
          >
            <option value=",">Comma ( , )</option>
            <option value=";">Semicolon ( ; )</option>
            <option value="TAB">Tab</option>
          </select>
        </div>
        <ColumnInput id="skipLines" label="Info lines above the data" value={value.skipLines} onChange={(v) => set("skipLines", v)} />
        <div className="space-y-1">
          <label htmlFor="datePattern" className="text-xs font-medium text-fg-muted">Date format</label>
          <select
            id="datePattern"
            value={value.datePattern}
            onChange={(e) => set("datePattern", e.target.value)}
            className={inputClass}
          >
            {DATE_FORMATS.map((f) => (
              <option key={f.value} value={f.value}>
                {f.example}
              </option>
            ))}
          </select>
        </div>
      </div>

      <label className="flex items-center gap-2 text-sm text-fg">
        <input type="checkbox" checked={value.hasHeader} onChange={(e) => set("hasHeader", e.target.checked)} />
        The first data line is a header (Date, Description, …)
      </label>

      <div className="grid gap-3 sm:grid-cols-2">
        <ColumnInput id="dateColumn" label="Date column" value={value.dateColumn} onChange={(v) => set("dateColumn", v)} />
        <ColumnInput id="descriptionColumn" label="Description column" value={value.descriptionColumn} onChange={(v) => set("descriptionColumn", v)} />
      </div>

      <fieldset className="space-y-2">
        <legend className="text-xs font-medium text-fg-muted">How are amounts shown?</legend>
        <div className="flex flex-wrap gap-4 text-sm text-fg">
          <label className="flex items-center gap-2">
            <input type="radio" checked={value.amountStyle === "single"} onChange={() => set("amountStyle", "single")} />
            One column, minus = money out
          </label>
          <label className="flex items-center gap-2">
            <input type="radio" checked={value.amountStyle === "split"} onChange={() => set("amountStyle", "split")} />
            Separate debit and credit columns
          </label>
        </div>
      </fieldset>

      <div className="grid gap-3 sm:grid-cols-3">
        {value.amountStyle === "single" ? (
          <ColumnInput id="amountColumn" label="Amount column" value={value.amountColumn} onChange={(v) => set("amountColumn", v)} />
        ) : (
          <>
            <ColumnInput id="debitColumn" label="Debit (money out) column" value={value.debitColumn} onChange={(v) => set("debitColumn", v)} />
            <ColumnInput id="creditColumn" label="Credit (money in) column" value={value.creditColumn} onChange={(v) => set("creditColumn", v)} />
          </>
        )}
        <ColumnInput id="balanceColumn" label="Balance column" value={value.balanceColumn} onChange={(v) => set("balanceColumn", v)} optional />
      </div>
    </div>
  );
}