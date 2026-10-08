import Link from "next/link";
import { NavLinks } from "./nav-links";

export function Sidebar() {
  return (
    <aside className="hidden w-60 shrink-0 flex-col border-r border-line bg-canvas lg:flex">
      <div className="flex h-14 items-center border-b border-line px-4">
        <Link href="/" className="text-sm font-semibold tracking-tight">
          LedgerSense
        </Link>
      </div>
      <nav aria-label="Main" className="flex-1 overflow-y-auto p-3">
        <NavLinks />
      </nav>
    </aside>
  );
}