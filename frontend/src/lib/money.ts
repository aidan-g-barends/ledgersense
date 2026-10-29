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