"use client";

import { useRef } from "react";
import { Menu, X } from "lucide-react";
import { NavLinks } from "./nav-links";

const iconButton =
  "inline-flex size-8 items-center justify-center rounded border border-line text-fg-muted transition-colors hover:border-line-strong hover:text-fg focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary";

export function MobileNav() {
  const dialogRef = useRef<HTMLDialogElement>(null);

  const open = () => dialogRef.current?.showModal();
  const close = () => dialogRef.current?.close();

  return (
    <>
      <button
        type="button"
        onClick={open}
        aria-label="Open navigation"
        className={`${iconButton} lg:hidden`}
      >
        <Menu className="size-4" aria-hidden="true" />
      </button>

      <dialog
        ref={dialogRef}
        aria-label="Navigation"
        onClick={(event) => {
          if (event.target === event.currentTarget) close();
        }}
        className="m-0 h-dvh max-h-none w-72 max-w-[85vw] border-r border-line bg-canvas p-0 text-fg backdrop:bg-[rgb(0_0_0/0.6)]"
      >
        <div className="flex h-full flex-col">
          <div className="flex h-14 items-center justify-between border-b border-line px-4">
            <span className="text-sm font-semibold tracking-tight">LedgerSense</span>
            <button type="button" onClick={close} aria-label="Close navigation" className={iconButton}>
              <X className="size-4" aria-hidden="true" />
            </button>
          </div>
          <nav aria-label="Main" className="p-3">
            <NavLinks onNavigate={close} />
          </nav>
        </div>
      </dialog>
    </>
  );
}