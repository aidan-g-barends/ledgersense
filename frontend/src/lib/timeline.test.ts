import { describe, expect, it } from "vitest";
import { buildMockTimeline } from "./timeline";

describe("buildMockTimeline", () => {
  it("returns one point per day, oldest first", () => {
    const points = buildMockTimeline(30);
    expect(points).toHaveLength(30);
    expect(points[0].date).toBe("2026-10-02");
    expect(points[29].date).toBe("2026-10-31");
  });

  it("gives the same day the same amounts whatever the range", () => {
    const last30 = buildMockTimeline(30);
    const last90 = buildMockTimeline(90).slice(-30);
    // WHY: proves the seed is per DATE, so switching 30/90 days doesn't reshuffle the chart
    expect(last90.map((p) => p.moneyOut)).toEqual(last30.map((p) => p.moneyOut));
  });
});