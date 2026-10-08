import Link from "next/link";
import { FileUp } from "lucide-react";
import { ThemeToggle } from "@/components/theme/theme-toggle";
import { MobileNav } from "./mobile-nav";

export function AppHeader() {
  return (
    <header className="flex h-14 shrink-0 items-center gap-3 border-b border-line bg-canvas px-4 lg:px-6">
      <MobileNav />
      <span className="text-sm font-semibold tracking-tight lg:hidden">LedgerSense</span>

      <div className="ml-auto flex items-center gap-2">
        <Link
          href="/imports"
          className="inline-flex h-8 items-center gap-2 rounded bg-primary px-3 text-sm font-medium text-white transition-colors hover:bg-primary-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
        >
          <FileUp className="size-4" aria-hidden="true" />
          Import CSV
        </Link>
        <ThemeToggle />
      </div>
    </header>
  );
}