import { describe, expect, it } from "vitest";
import { formatMonth, formatRelative } from "./money";

describe("formatMonth", () => {
  it("formats short and long", () => {
    expect(formatMonth("2026-10")).toBe("Oct");
    expect(formatMonth("2026-10", "long")).toBe("October 2026");
  });
});

describe("formatRelative", () => {
  const now = new Date("2026-11-06T10:00:00Z");

  it("uses friendly words", () => {
    expect(formatRelative("2026-11-06T09:59:30Z", now)).toBe("just now");
    expect(formatRelative("2026-11-05T10:00:00Z", now)).toBe("yesterday");
    expect(formatRelative("2026-11-04T10:00:00Z", now)).toBe("2 days ago");
    expect(formatRelative("2026-10-30T10:00:00Z", now)).toBe("last week");
  });
});
