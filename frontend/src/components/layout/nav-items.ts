import {
  FileUp,
  LayoutDashboard,
  ReceiptText,
  Settings,
  TriangleAlert,
  type LucideIcon,
} from "lucide-react";

export type NavItem = {
  href: string;
  label: string;
  icon: LucideIcon;
};

export const NAV_ITEMS: readonly NavItem[] = [
  { href: "/", label: "Overview", icon: LayoutDashboard },
  { href: "/attention", label: "Attention Queue", icon: TriangleAlert },
  { href: "/transactions", label: "Transactions", icon: ReceiptText },
  { href: "/imports", label: "Imports", icon: FileUp },
  { href: "/settings", label: "Settings", icon: Settings },
];
