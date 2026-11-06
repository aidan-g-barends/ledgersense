"use client";

type Option<T> = { value: T; label: string };

type Props<T extends string | number> = {
  options: readonly Option<T>[];
  value: T;
  onChange: (value: T) => void;
  label: string; // for screen readers: what this group of buttons controls
};

// NEW: the pill-style tab switcher, reusable anywhere
export function Segmented<T extends string | number>({ options, value, onChange, label }: Props<T>) {
  return (
    <div className="inline-flex rounded border border-line bg-sunken p-0.5" role="group" aria-label={label}>
      {options.map((option) => (
        <button
          key={String(option.value)}
          type="button"
          aria-pressed={value === option.value}
          onClick={() => onChange(option.value)}
          className={`rounded px-3 py-1.5 text-sm font-medium transition-colors ${
            value === option.value ? "bg-surface text-fg shadow-sm" : "text-fg-muted hover:text-fg"
          }`}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}