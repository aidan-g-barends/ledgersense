"use client";

import { Eye, EyeOff } from "lucide-react";
import { useState } from "react";

type FieldProps = {
  id: string;
  label: string;
  type?: string;
  autoComplete?: string;
  value: string;
  onChange: (value: string) => void;
  hint?: string;
  revealable?: boolean; // NEW: adds a show/hide button for passwords
};

export function Field({ id, label, type = "text", autoComplete, value, onChange, hint, revealable }: FieldProps) {
  const [visible, setVisible] = useState(false);
  // WHY: revealable fields switch between "password" (dots) and "text" (readable)
  const inputType = revealable ? (visible ? "text" : "password") : type;

  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-sm font-medium text-fg">
        {label}
      </label>

      <div className="relative">
        <input
          id={id}
          name={id}
          type={inputType}
          autoComplete={autoComplete}
          required
          value={value}
          onChange={(e) => onChange(e.target.value)}
          aria-describedby={hint ? `${id}-hint` : undefined}
          className={`h-10 w-full rounded border border-line bg-surface px-3 text-sm text-fg outline-none transition-colors placeholder:text-fg-faint hover:border-line-strong focus:border-primary focus:ring-1 focus:ring-primary ${
            revealable ? "pr-10" : ""
          }`}
        />
        {revealable && (
          <button
            type="button"
            onClick={() => setVisible((v) => !v)}
            aria-label={visible ? "Hide password" : "Show password"}
            aria-pressed={visible}
            className="absolute inset-y-0 right-0 flex w-10 items-center justify-center text-fg-faint transition-colors hover:text-fg"
          >
            {visible ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
          </button>
        )}
      </div>

      {hint && (
        <p id={`${id}-hint`} className="text-xs text-fg-faint">
          {hint}
        </p>
      )}
    </div>
  );
}