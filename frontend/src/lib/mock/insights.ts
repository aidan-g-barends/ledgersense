import type { InsightsSummary } from "@/lib/types";

// NEW: sample Overview data. Used ONLY while USE_MOCK is true in lib/insights.ts.
export const MOCK_SUMMARY: InsightsSummary = {
  month: "2026-10",
  moneyIn: 43900,
  moneyOut: 37985.25,
  net: 5914.75,
  moneyOutChangePct: 15.9,
  transactionCount: 84,
  months: [
    { month: "2026-05", moneyIn: 38500, moneyOut: 31240.5 },
    { month: "2026-06", moneyIn: 41200, moneyOut: 33980.1 },
    { month: "2026-07", moneyIn: 39800, moneyOut: 30115.75 },
    { month: "2026-08", moneyIn: 42650, moneyOut: 34820.4 },
    { month: "2026-09", moneyIn: 40100, moneyOut: 32760.9 },
    { month: "2026-10", moneyIn: 43900, moneyOut: 37985.25 },
  ],
  topCategories: [
    { categoryId: "mock-cat-1", name: "Rent", amount: 12500 },
    { categoryId: "mock-cat-2", name: "Stock & Supplies", amount: 8420.6 },
    { categoryId: "mock-cat-3", name: "Restaurants", amount: 4180 },
    { categoryId: "mock-cat-4", name: "Fuel & Travel", amount: 3650.4 },
    { categoryId: "mock-cat-5", name: "Software & Subscriptions", amount: 3297 },
    { categoryId: null, name: "Uncategorised", amount: 2210 },
  ],
};