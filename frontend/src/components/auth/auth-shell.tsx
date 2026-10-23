import { FileSpreadsheet, ScanSearch, ShieldAlert, Tags } from "lucide-react";

// NEW: the full-page layout for login + register.
// WHY a component instead of a route-group layout: it works wherever the page file lives.
export function AuthShell({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: React.ReactNode;
}) {
  return (
    <div className="grid min-h-dvh bg-canvas lg:grid-cols-2">
      <BrandPanel />

      <main className="flex items-center justify-center px-4 py-12 sm:px-8">
        <div className="w-full max-w-sm">
          {/* Only on small screens: the brand panel is hidden there */}
          <Logo className="mb-10 lg:hidden" />

          <h1 className="text-2xl font-semibold tracking-tight text-fg">{title}</h1>
          <p className="mt-1.5 text-sm text-fg-muted">{subtitle}</p>

          <div className="mt-8">{children}</div>
        </div>
      </main>
    </div>
  );
}

function Logo({ className = "" }: { className?: string }) {
  return (
    <div className={`flex items-center gap-2.5 ${className}`}>
      <span className="flex size-8 items-center justify-center rounded-md bg-primary text-white">
        <ScanSearch className="size-4" aria-hidden="true" />
      </span>
      <span className="text-base font-semibold tracking-tight text-fg">LedgerSense</span>
    </div>
  );
}

const FEATURES = [
  { icon: FileSpreadsheet, text: "Import statements from any South African bank" },
  { icon: Tags, text: "Spending sorted into categories automatically" },
  { icon: ShieldAlert, text: "Duplicates, spikes and new payees flagged for you" },
];

function BrandPanel() {
  return (
    <aside className="relative hidden flex-col justify-between overflow-hidden border-r border-line bg-sunken p-10 lg:flex">
      {/* Faint grid background, fading out towards the edges. Pure decoration. */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0"
        style={{
          backgroundImage:
            "linear-gradient(var(--line-subtle) 1px, transparent 1px), linear-gradient(90deg, var(--line-subtle) 1px, transparent 1px)",
          backgroundSize: "32px 32px",
          maskImage: "radial-gradient(ellipse at 30% 40%, black 20%, transparent 75%)",
        }}
      />

      <Logo className="relative" />

      <div className="relative max-w-md space-y-8">
        <div className="space-y-3">
          <p className="font-mono text-[10px] font-semibold uppercase tracking-[0.08em] text-primary">
            Finance intelligence
          </p>
          <h2 className="text-3xl font-semibold tracking-tight text-fg">
            Catch the charges you&apos;d otherwise miss.
          </h2>
          <p className="text-sm leading-relaxed text-fg-muted">
            Import your bank statements, let LedgerSense sort the spending, and get flagged the
            moment something looks off.
          </p>
        </div>

        <AnomalyPreview />

        <ul className="space-y-3">
          {FEATURES.map(({ icon: Icon, text }) => (
            <li key={text} className="flex items-center gap-3 text-sm text-fg-muted">
              <Icon className="size-4 shrink-0 text-primary" aria-hidden="true" />
              {text}
            </li>
          ))}
        </ul>
      </div>

      <p className="relative font-mono text-[11px] text-fg-faint">
        Built for South African small businesses · ZAR
      </p>
    </aside>
  );
}

// A static mock of what a flag looks like inside the app. aria-hidden: it's a picture, not real data.
function AnomalyPreview() {
  return (
    <div aria-hidden="true" className="rounded-lg border border-line bg-surface p-4">
      <div className="flex items-center justify-between">
        <span className="rounded border border-critical/30 bg-critical/10 px-1.5 py-0.5 font-mono text-[10px] font-semibold uppercase tracking-[0.08em] text-critical">
          Potential duplicate
        </span>
        <span className="font-mono text-[11px] text-fg-faint">19 Oct · 14:22</span>
      </div>

      <div className="mt-3 flex items-baseline justify-between gap-4">
        <p className="text-sm font-medium text-fg">Cloud hosting subscription</p>
        <p className="font-mono text-sm font-semibold tabular-nums text-critical">−R 4 850,00</p>
      </div>
      <p className="mt-1 text-xs text-fg-muted">
        Charged twice, 8 minutes apart. Usually billed once a month.
      </p>

      <div className="mt-4 flex gap-2">
        <span className="rounded border border-critical/40 px-2.5 py-1 text-xs font-medium text-critical">
          Dispute
        </span>
        <span className="rounded border border-line px-2.5 py-1 text-xs font-medium text-fg-muted">
          Mark legitimate
        </span>
      </div>
    </div>
  );
}