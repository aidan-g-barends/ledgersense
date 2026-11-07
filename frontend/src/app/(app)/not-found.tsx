import Link from "next/link";
import { btnPrimary } from "@/lib/ui";

// NEW: the 404 page. The wording matches the backend: another org's data is "not found", never "forbidden".
export default function NotFound() {
  return (
    <main className="flex min-h-dvh flex-col items-center justify-center gap-3 bg-canvas px-4 text-center">
      <p className="font-mono text-xs text-fg-faint">404</p>
      <h1 className="text-xl font-semibold text-fg">Page not found</h1>
      <p className="max-w-sm text-sm text-fg-muted">That page doesn&apos;t exist, or you don&apos;t have access to it.</p>
      <Link href="/" className={btnPrimary}>Back to Overview</Link>
    </main>
  );
}