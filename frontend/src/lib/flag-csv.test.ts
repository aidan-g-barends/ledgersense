import { describe, expect, it } from "vitest";
import { flagsToCsv } from "./flag-csv";
import { MOCK_FLAGS } from "./mock/flags";

describe("flagsToCsv", () => {
  it("writes a header and one line per flag", () => {
    const lines = flagsToCsv([MOCK_FLAGS[0]]).split("\r\n");
    expect(lines).toHaveLength(2);
    expect(lines[1].startsWith(`"2026-10-05","CLOUD HOST LTD ****5678",-299,"Duplicate charge"`)).toBe(true);
  });
});