"use client";

import { createContext, useCallback, useContext, useMemo, useRef, useState, type ReactNode } from "react";
import { CircleAlert, CircleCheck, X } from "lucide-react";

type Tone = "success" | "error";
type Toast = { id: number; message: string; tone: Tone };
type ToastApi = { success: (message: string) => void; error: (message: string) => void };

const ToastContext = createContext<ToastApi | null>(null);

// NEW: wrap the app in this once, then call useToast() anywhere inside it
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const nextId = useRef(0); // WHY a ref: changing it shouldn't re-render anything

  const dismiss = useCallback((id: number) => {
    setToasts((all) => all.filter((t) => t.id !== id));
  }, []);

  const show = useCallback(
    (message: string, tone: Tone) => {
      const id = ++nextId.current;
      setToasts((all) => [...all.slice(-2), { id, message, tone }]); // keep at most 3 on screen
      setTimeout(() => dismiss(id), 4000);
    },
    [dismiss],
  );

  // WHY useMemo: the same object every render, so components using it don't re-render for nothing
  const api = useMemo<ToastApi>(
    () => ({ success: (m) => show(m, "success"), error: (m) => show(m, "error") }),
    [show],
  );

  return (
    <ToastContext value={api}>
      {children}
      {/* aria-live: screen readers read new toasts out loud */}
      <div aria-live="polite" className="pointer-events-none fixed bottom-4 right-4 z-50 flex w-80 max-w-[calc(100vw-2rem)] flex-col gap-2">
        {toasts.map((t) => (
          <div key={t.id} className="pointer-events-auto flex items-start gap-2 rounded border border-line bg-raised p-3 text-sm text-fg shadow-lg">
            {t.tone === "success" ? (
              <CircleCheck className="mt-0.5 size-4 shrink-0 text-positive" aria-hidden="true" />
            ) : (
              <CircleAlert className="mt-0.5 size-4 shrink-0 text-critical" aria-hidden="true" />
            )}
            <p className="flex-1">{t.message}</p>
            <button type="button" onClick={() => dismiss(t.id)} aria-label="Dismiss" className="rounded p-0.5 text-fg-faint hover:text-fg">
              <X className="size-3.5" aria-hidden="true" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext>
  );
}

export function useToast(): ToastApi {
  const ctx = useContext(ToastContext);
  // WHY throw: forgetting the provider should fail loudly, not silently do nothing
  if (!ctx) throw new Error("useToast must be used inside <ToastProvider>");
  return ctx;
}