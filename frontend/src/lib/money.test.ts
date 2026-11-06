import { describe, expect, it } from "vitest";
import { formatMonth } from "./money";

describe("formatMonth", () => {
  it("formats short and long", () => {
    expect(formatMonth("2026-10")).toBe("Oct");
    expect(formatMonth("2026-10", "long")).toBe("October 2026");
  });
});