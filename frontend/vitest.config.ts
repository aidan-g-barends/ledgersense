import { defineConfig } from "vitest/config";
import { fileURLToPath } from "node:url";

// NEW: unit tests for plain functions (formatters, CSV, mock builders). No browser needed.
export default defineConfig({
  resolve: {
    // WHY: so tests understand "@/lib/..." the same way Next does
    alias: { "@": fileURLToPath(new URL("./src", import.meta.url)) },
  },
  test: {
    environment: "node",
    include: ["src/**/*.test.ts"],
  },
});