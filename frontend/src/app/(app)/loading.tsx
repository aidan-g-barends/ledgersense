// NEW: shown instantly while Next loads a page in this section
export default function AppLoading() {
  return (
    <div className="space-y-4" aria-busy="true" aria-label="Loading">
      <div className="h-7 w-48 animate-pulse rounded bg-sunken" />
      <div className="h-4 w-80 max-w-full animate-pulse rounded bg-sunken" />
      <div className="h-64 animate-pulse rounded-lg bg-sunken" />
    </div>
  );
}