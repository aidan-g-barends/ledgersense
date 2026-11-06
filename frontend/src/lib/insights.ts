import { api } from "@/lib/api";
import type { InsightsSummary } from "@/lib/types";
import { MOCK_SUMMARY } from "@/lib/mock/insights";

// NEW: its own switch, separate from flags. Flags go live this weekend, insights later.
export const USE_MOCK = true;

export async function getInsightsSummary(): Promise<InsightsSummary> {
  if (!USE_MOCK) {
    return api<InsightsSummary>("/insights/summary");
  }
  await new Promise((resolve) => setTimeout(resolve, 300));
  return structuredClone(MOCK_SUMMARY);
}