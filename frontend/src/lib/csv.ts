// NEW: builds CSV text safely.
const FORMULA_START = /^[=+\-@\t\r]/;

export function csvCell(value: string | number | null | undefined): string {
  if (value === null || value === undefined) return "";
  // WHY numbers skip the guard: -299 IS a number, Excel should treat it as one
  if (typeof value === "number") return String(value);
  const safe = FORMULA_START.test(value) ? `'${value}` : value;
  // Always quote text, and double any quotes inside: say "hi"  ->  "say ""hi"""
  return `"${safe.replaceAll('"', '""')}"`;
}

export function toCsv(header: string[], rows: (string | number | null)[][]): string {
  // WHY \r\n: the official CSV line ending, and what Excel expects
  return [header, ...rows].map((row) => row.map(csvCell).join(",")).join("\r\n");
}