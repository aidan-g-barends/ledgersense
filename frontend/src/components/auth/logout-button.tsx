"use client";

import { LogOut } from "lucide-react";
import { useState } from "react";
import { api } from "@/lib/api";

export function LogoutButton() {
  const [busy, setBusy] = useState(false);

  async function handleLogout() {
    setBusy(true);
    try {
      await api("/auth/logout", { method: "POST" }); // revokes refresh token + clears cookies
    } finally {
      // WHY a full reload: wipes any data still held in memory from the old session
      // eslint-disable-next-line @next/next/no-location-assign-relative-destination -- full reload on purpose: clears in-memory data from the old session
      window.location.href = "/login";
    }
  }

  return (
    <button
      type="button"
      onClick={handleLogout}
      disabled={busy}
      className="inline-flex items-center gap-2 rounded-md px-3 py-1.5 text-sm text-fg/80 hover:bg-raised disabled:opacity-60"
    >
      <LogOut className="size-4" aria-hidden="true" />
      Log out
    </button>
  );
}