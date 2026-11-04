"use client";

import { useEffect, useRef, type ReactNode } from "react";
import { X } from "lucide-react";

type Props = {
  open: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
};

// NEW: a right-hand side panel built on the native <dialog>, like your MobileNav.
// The browser gives us Esc-to-close, focus trapping and the backdrop for free.
export function Drawer({ open, onClose, title, children }: Props) {
  const ref = useRef<HTMLDialogElement>(null);

  // WHY an effect: <dialog> is opened with a METHOD (showModal), not a prop.
  // This keeps the real dialog in sync with our `open` prop.
  useEffect(() => {
    const dialog = ref.current;
    if (!dialog) return;
    if (open && !dialog.open) dialog.showModal();
    if (!open && dialog.open) dialog.close();
  }, [open]);

  return (
    <dialog
      ref={ref}
      aria-label={title}
      onClose={onClose} // fires on Esc too, so the parent always hears about it
      onClick={(event) => {
        if (event.target === event.currentTarget) onClose(); // click on the backdrop = close
      }}
      className="m-0 ml-auto h-dvh max-h-none w-[28rem] max-w-[92vw] border-l border-line bg-canvas p-0 text-fg backdrop:bg-[rgb(0_0_0/0.6)]"
    >
      <div className="flex h-full flex-col">
        <div className="flex h-14 shrink-0 items-center justify-between border-b border-line px-4">
          <h2 className="text-sm font-semibold">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            aria-label={`Close ${title.toLowerCase()}`}
            className="inline-flex size-8 items-center justify-center rounded border border-line text-fg-muted transition-colors hover:border-line-strong hover:text-fg focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
          >
            <X className="size-4" aria-hidden="true" />
          </button>
        </div>
        <div className="flex-1 overflow-y-auto p-4">{children}</div>
      </div>
    </dialog>
  );
}