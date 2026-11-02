"use client";

import { useEffect, useState } from "react";
import { api, type Me } from "@/lib/api";

// NEW: shows "which business am I in?" in the top bar (from GET /api/auth/me)
export function BusinessName() {
  const [name, setName] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    api<Me>("/auth/me")
      .then((me) => {
        if (!ignore) setName(me.organizationName);
      })
      .catch(() => {
        // Not important enough to show an error. The bar just stays empty.
      });
    return () => {
      ignore = true;
    };
  }, []);

  if (!name) return null;
  return <span className="hidden truncate text-sm text-fg-muted lg:inline">{name}</span>;
}