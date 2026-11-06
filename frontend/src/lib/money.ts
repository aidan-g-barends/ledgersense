// NEW: one formatter for money and dates, South African style ("R 1 234,56", "26 Sep 2026")
const zar = new Intl.NumberFormat("en-ZA", { style: "currency", currency: "ZAR" });

export function formatMoney(amount: number): string {
  return zar.format(amount);
}

// "2026-09-26" -> "26 Sep 2026". T00:00:00 stops the browser shifting it a day by timezone.
export function formatDate(isoDate: string): string {
  return new Date(`${isoDate}T00:00:00`).toLocaleDateString("en-ZA", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export function formatDateTime(isoInstant: string): string {
  return new Date(isoInstant).toLocaleString("en-ZA", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}

// NEW: "2026-10" -> "Oct", or "October 2026" with "long"
export function formatMonth(month: string, style: "short" | "long" = "short"): string {
  const options: Intl.DateTimeFormatOptions =
    style === "short" ? { month: "short", timeZone: "UTC" } : { month: "long", year: "numeric", timeZone: "UTC" };
  // WHY UTC on both sides: "2026-10-01" in local time can slip to 30 Sep in some timezones
  return new Intl.DateTimeFormat("en-ZA", options).format(new Date(`${month}-01T00:00:00Z`));
}

// NEW: "2 days ago", "yesterday", "last week". `now` is a parameter so tests can fix the time.
const relative = new Intl.RelativeTimeFormat("en-ZA", { numeric: "auto" }); // "auto" = "yesterday", not "1 day ago"

const UNITS: [Intl.RelativeTimeFormatUnit, number][] = [
  ["year", 31_536_000],
  ["month", 2_592_000],
  ["week", 604_800],
  ["day", 86_400],
  ["hour", 3_600],
  ["minute", 60],
];

export function formatRelative(iso: string, now: Date = new Date()): string {
  const seconds = Math.round((new Date(iso).getTime() - now.getTime()) / 1000); // negative = in the past
  // Biggest unit first: 9 days is "last week", not "9 days ago"
  for (const [unit, size] of UNITS) {
    if (Math.abs(seconds) >= size) return relative.format(Math.round(seconds / size), unit);
  }
  return "just now";
}