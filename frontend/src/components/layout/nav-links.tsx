"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { NAV_ITEMS } from "./nav-items";

function isActive(pathname: string, href: string): boolean {
  if (href === "/") return pathname === "/";
  return pathname === href || pathname.startsWith(`${href}/`);
}

export function NavLinks({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname();

  return (
    <ul className="space-y-0.5">
      {NAV_ITEMS.map(({ href, label, icon: Icon }) => (
        <li key={href}>
          <Link
            href={href}
            onClick={onNavigate}
            aria-current={isActive(pathname, href) ? "page" : undefined}
            className="flex h-9 items-center gap-3 rounded-r border-l-2 border-transparent px-3 text-sm text-fg-muted transition-colors hover:bg-raised hover:text-fg focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary aria-[current=page]:border-primary aria-[current=page]:bg-raised aria-[current=page]:font-medium aria-[current=page]:text-fg"
          >
            <Icon className="size-4 shrink-0" aria-hidden="true" />
            {label}
          </Link>
        </li>
      ))}
    </ul>
  );
}