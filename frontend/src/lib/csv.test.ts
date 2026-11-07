import { describe, expect, it } from "vitest";
import { csvCell, toCsv } from "./csv";

describe("csvCell", () => {
  it("neutralises formulas", () => {
    expect(csvCell("=1+1")).toBe(`"'=1+1"`);
    expect(csvCell("@SUM(A1)")).toBe(`"'@SUM(A1)"`);
  });

  it("escapes quotes and keeps numbers raw", () => {
    expect(csvCell('say "hi"')).toBe(`"say ""hi"""`);
    expect(csvCell(-299)).toBe("-299");
    expect(csvCell(null)).toBe("");
  });
});

describe("toCsv", () => {
  it("joins rows with CRLF", () => {
    expect(toCsv(["Amount"], [[10], [20]])).toBe(`"Amount"\r\n10\r\n20`);
  });
});