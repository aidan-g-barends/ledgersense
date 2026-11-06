import { api } from "@/lib/api";
import type { InsightsSummary, TimelinePoint } from "@/lib/types"; // CHANGED: + TimelinePoint
import { MOCK_SUMMARY } from "@/lib/mock/insights";
import { buildMockTimeline } from "@/lib/mock/timeline"; // NEW

export const USE_MOCK = true;

// NEW: one shared fake delay instead of repeating the setTimeout line
function wait(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function getInsightsSummary(): Promise<InsightsSummary> {
  if (!USE_MOCK) {
    return api<InsightsSummary>("/insights/summary");
  }
  await wait(300);
  return structuredClone(MOCK_SUMMARY);
}

// NEW
export async function getTimeline(days: number): Promise<TimelinePoint[]> {
  if (!USE_MOCK) {
    return api<TimelinePoint[]>(`/insights/timeline?days=${days}`);
  }
  await wait(300);
  return buildMockTimeline(days);
}