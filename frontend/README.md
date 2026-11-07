# LedgerSense frontend

Next.js 16 + TypeScript + Tailwind v4 dashboard for LedgerSense: import South African bank CSVs, auto-categorise spending, and review flagged anomalies.

## Run it

```bash
npm install
npm run dev      # http://localhost:3000 (the backend must be running, see ../dev.sh)
npm run lint
npm test         # Vitest unit tests
npm run build
```

`/api/*` is proxied to the Spring Boot backend (see `next.config.ts`), so the browser only ever talks to one origin.

## Structure

```
src/
  app/(auth)/        login, register
  app/(app)/         Overview, Timeline, Attention Queue, Transactions, Imports, Settings
  components/        UI grouped by feature (attention, overview, timeline, imports, ...)
  lib/api.ts         fetch wrapper: CSRF header, silent refresh on 401
  lib/flags.ts       flags client      (USE_MOCK switch)
  lib/insights.ts    insights client   (USE_MOCK switch)
  lib/mock/          sample data used while a backend endpoint is still being built
```

## Contract-first

The API shapes live in `src/lib/types.ts`. New screens are built against mock data in exactly that shape, then the backend is built to match. Going live is one line: set `USE_MOCK = false` in the client file and delete its mock.

## Security notes

- Auth uses cookies plus a CSRF token (`XSRF-TOKEN` cookie, `X-XSRF-TOKEN` header). No tokens in `localStorage`.
- Route guarding runs in `src/proxy.ts`.
- CSV exports guard against formula injection (`src/lib/csv.ts`).
- Error pages never show raw error messages.