import { LoaderCircle } from "lucide-react";

// NEW: primary button with a spinner while the request runs
export function SubmitButton({ busy, children, busyText }: { busy: boolean; children: React.ReactNode; busyText: string }) {
  return (
    <button
      type="submit"
      disabled={busy}
      className="flex h-10 w-full items-center justify-center gap-2 rounded bg-primary text-sm font-medium text-white transition-colors hover:bg-primary-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary disabled:cursor-not-allowed disabled:opacity-60"
    >
      {busy && <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />}
      {busy ? busyText : children}
    </button>
  );
}