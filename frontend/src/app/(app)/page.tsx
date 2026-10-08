import { ThemeToggle } from "@/components/theme/theme-toggle";
import { formatZAR } from "@/lib/format";

const STATUS_STYLES = {
  verified: "border-positive/25 bg-positive/10 text-positive",
  review: "border-warning/25 bg-warning/10 text-warning",
  critical: "border-critical/30 bg-critical/10 text-critical",
  info: "border-primary/25 bg-primary/10 text-primary",
} as const;

const SURFACES = [
  { name: "canvas", className: "bg-canvas" },
  { name: "sunken", className: "bg-sunken" },
  { name: "surface", className: "bg-surface" },
  { name: "raised", className: "bg-raised" },
] as const;

export default function Home() {
  return (
    <main className="mx-auto max-w-3xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <div>
          <p className="font-mono text-[10px] font-semibold uppercase tracking-[0.08em] text-fg-faint">
            Design system check
          </p>
          <h1 className="text-2xl font-semibold tracking-tight">LedgerSense tokens</h1>
        </div>
        <ThemeToggle />
      </header>

      <section className="rounded-lg border border-line bg-surface p-4">
        <p className="font-mono text-[10px] font-semibold uppercase tracking-[0.08em] text-fg-muted">
          Operating cash balance
        </p>
        <p className="mt-2 font-mono text-3xl font-semibold tabular-nums">
          {formatZAR(284920.45)}
        </p>
        <p className="mt-1 text-sm text-fg-muted">Last import: 3 Oct 2026, 412 rows</p>
      </section>

      <section className="flex flex-wrap gap-2">
        {Object.entries(STATUS_STYLES).map(([label, classes]) => (
          <span
            key={label}
            className={`inline-flex h-5 items-center rounded border px-2 font-mono text-[11px] uppercase ${classes}`}
          >
            {label}
          </span>
        ))}
      </section>

      <section className="grid grid-cols-2 gap-2 sm:grid-cols-4">
        {SURFACES.map((s) => (
          <div key={s.name} className={`h-16 rounded-lg border border-line p-2 ${s.className}`}>
            <span className="font-mono text-[11px] text-fg-muted">{s.name}</span>
          </div>
        ))}
      </section>

      <button
        type="button"
        className="h-9 rounded bg-primary px-4 text-sm font-medium text-white hover:bg-primary-hover"
      >
        Import CSV
      </button>
    </main>
  );
}