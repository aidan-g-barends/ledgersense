import type { TimelinePoint } from "@/lib/types";

// NEW: builds fake daily data. Used ONLY while USE_MOCK is true in lib/insights.ts.

// Days that match the mock flags, so the red dots line up with the Attention Queue
const FLAGGED: Record<string, number> = { "2026-09-08": 1, "2026-10-05": 1, "2026-10-22": 1, "2026-10-23": 2 };

// WHY seeded: Math.random() changes every refresh. This gives the SAME "random" numbers
// for the same date every time, so the chart doesn't jump around.
function seeded(seed: number) {
  let s = seed;
  return () => {
    s = (s * 1664525 + 1013904223) % 4294967296; // a classic tiny random number generator (an LCG)
    return s / 4294967296; // a number from 0 to 1
  };
}

const round2 = (n: number) => Math.round(n * 100) / 100;

export function buildMockTimeline(days: number, lastDay = "2026-10-31"): TimelinePoint[] {
  const end = new Date(`${lastDay}T00:00:00Z`);
  const points: TimelinePoint[] = [];
  let running = 0;

  for (let i = days - 1; i >= 0; i--) {
    const day = new Date(end);
    day.setUTCDate(end.getUTCDate() - i);
    const date = day.toISOString().slice(0, 10);
    const random = seeded(Number(date.replaceAll("-", ""))); // seed = 20261005, etc.
    const dayOfMonth = day.getUTCDate();

    // Big client payment on the 25th, small ones now and then
    const moneyIn = dayOfMonth === 25 ? 38000 : random() < 0.15 ? round2(random() * 3000) : 0;
    // Rent on the 1st, plus everyday spending
    const moneyOut = round2((dayOfMonth === 1 ? 12500 : 0) + random() * 1400);

    running = round2(running + moneyIn - moneyOut);
    points.push({ date, moneyIn, moneyOut, runningNet: running, flagCount: FLAGGED[date] ?? 0 });
  }
  return points;
}