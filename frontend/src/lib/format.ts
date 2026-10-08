const zarFormatter = new Intl.NumberFormat("en-ZA", {
  style: "currency",
  currency: "ZAR",
});

export function formatZAR(amount: number): string {
  return zarFormatter.format(amount);
}