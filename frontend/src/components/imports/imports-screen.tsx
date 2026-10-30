"use client";

import { useState } from "react";
import { ImportWizard } from "./import-wizard";
import { ImportHistory } from "./import-history";

// NEW: client wrapper so a finished import can tell the history to reload
export function ImportsScreen() {
  const [refreshKey, setRefreshKey] = useState(0);

  return (
    <div className="space-y-6">
      <ImportWizard onImported={() => setRefreshKey((k) => k + 1)} />
      <ImportHistory refreshKey={refreshKey} />
    </div>
  );
}