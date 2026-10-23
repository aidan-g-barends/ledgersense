// NEW: one labelled input, reused by both forms. Label + input are linked by id (accessibility).
type FieldProps = {
  id: string;
  label: string;
  type?: string;
  autoComplete?: string;
  value: string;
  onChange: (value: string) => void;
  hint?: string;
};

export function Field({ id, label, type = "text", autoComplete, value, onChange, hint }: FieldProps) {
  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-sm font-medium text-fg">
        {label}
      </label>
      <input
        id={id}
        name={id}
        type={type}
        autoComplete={autoComplete}
        required
        value={value}
        onChange={(e) => onChange(e.target.value)}
        aria-describedby={hint ? `${id}-hint` : undefined}
        className="w-full rounded-md border border-line bg-canvas px-3 py-2 text-sm text-fg outline-none focus:border-primary focus:ring-2 focus:ring-primary/30"
      />
      {hint && (
        <p id={`${id}-hint`} className="text-xs text-fg/60">
          {hint}
        </p>
      )}
    </div>
  );
}