import { api } from "@/lib/api";
import type { AnomalyFlag, FlagStatus, ReviewDecision } from "@/lib/types";
import { MOCK_FLAGS } from "@/lib/mock/flags";

// CHANGED: the backend exists now. The Attention Queue, drawer and Overview preview all go live.
export const USE_MOCK = false;

// In-memory copy so reviews "stick" while you click around (resets on page refresh)
let mockStore: AnomalyFlag[] = structuredClone(MOCK_FLAGS);

// Fake network delay, so loading states show up exactly as they will for real
function wait(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function listFlags(status: FlagStatus): Promise<AnomalyFlag[]> {
  if (!USE_MOCK) {
    return api<AnomalyFlag[]>(`/flags?status=${status}`);
  }
  await wait(300);
  return mockStore.filter((f) => f.status === status);
}

export async function reviewFlag(id: string, decision: ReviewDecision): Promise<AnomalyFlag> {
  if (!USE_MOCK) {
    return api<AnomalyFlag>(`/flags/${id}/review`, { method: "PATCH", body: { status: decision } });
  }
  await wait(300);
  mockStore = mockStore.map((f) =>
    f.id === id ? { ...f, status: decision, reviewedAt: new Date().toISOString() } : f,
  );
  const updated = mockStore.find((f) => f.id === id);
  if (!updated) throw new Error("Flag not found");
  return updated;
}