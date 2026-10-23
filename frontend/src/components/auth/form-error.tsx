import { CircleAlert } from "lucide-react";

// NEW: one consistent error box for both forms. role="alert" = screen readers announce it.
export function FormError({ message }: { message: string | null }) {
  if (!message) return null;
  return (
    <div
      role="alert"
      className="flex items-start gap-2 rounded border border-critical/30 bg-critical/10 px-3 py-2.5 text-sm text-critical"
    >
      <CircleAlert className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}