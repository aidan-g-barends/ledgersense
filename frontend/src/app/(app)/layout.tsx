import type { ReactNode } from "react";
import { AppHeader } from "@/components/layout/app-header";
import { Sidebar } from "@/components/layout/sidebar";
import { ToastProvider } from "@/components/ui/toast-provider"; // NEW


export default function AppLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex h-dvh">
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded focus:bg-primary focus:px-3 focus:py-2 focus:text-sm focus:text-white"
      >
        Skip to main content
      </a>

 return (
    <ToastProvider>{/* NEW: every page in the app can now show toasts */}
      {/* ...everything that was already here stays exactly the same... */}
    </ToastProvider>
  );
      <Sidebar />

      <div className="flex min-w-0 flex-1 flex-col">
        <AppHeader />
        <main id="main-content" className="flex-1 overflow-y-auto p-4 lg:p-6">
          {children}
        </main>
      </div>
    </div>
  );
}