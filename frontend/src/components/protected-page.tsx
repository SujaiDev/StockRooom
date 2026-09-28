"use client";

import { useEffect, useState, type ReactNode } from "react";
import { usePathname, useRouter } from "next/navigation";
import Link from "next/link";
import { Activity, Boxes, Building2, ChevronDown, ClipboardList, LayoutDashboard, LogOut, Menu, Moon, Package, Sun, Warehouse, X } from "lucide-react";
import { api, type Warehouse as WarehouseType } from "@/services/api";

const primaryLinks = [
  { href: "/", label: "Overview", icon: LayoutDashboard },
  { href: "/products", label: "Products", icon: Package },
];
const operationLinks = [
  { href: "/operations/receipts", label: "Receipts" },
  { href: "/operations/deliveries", label: "Deliveries" },
  { href: "/operations/transfers", label: "Transfers" },
  { href: "/operations/adjustments", label: "Adjustments" },
  { href: "/operations/ledger", label: "Stock ledger" },
];

export function ProtectedPage({ title, children }: { title: string; children: ReactNode }) {
  const pathname = usePathname();
  const currentPath = pathname ?? "/";
  const router = useRouter();
  const [ready, setReady] = useState(false);
  const [user, setUser] = useState<{ name: string; email: string; role: string } | null>(null);
  const [warehouses, setWarehouses] = useState<WarehouseType[]>([]);
  const [warehouseId, setWarehouseId] = useState("ALL");
  const [dark, setDark] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [localDemo, setLocalDemo] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem("stockroom_token");
    if (!token) {
      router.replace("/login");
      return;
    }
    if (token === "local-demo-admin") {
      setLocalDemo(true);
      setUser(JSON.parse(localStorage.getItem("stockroom_demo_user") ?? "{\"name\":\"Demo Administrator\",\"email\":\"admin\",\"role\":\"INVENTORY_MANAGER\"}"));
      setReady(true);
      void api.warehouses().then(setWarehouses).catch(() => setWarehouses([]));
      return;
    }
    const savedTheme = localStorage.getItem("stockroom_theme") === "dark";
    setDark(savedTheme);
    document.documentElement.classList.toggle("dark", savedTheme);
    Promise.all([api.me(), api.warehouses()]).then(([account, list]) => {
      setUser(account);
      setWarehouses(list);
      setReady(true);
    }).catch(() => {
      localStorage.removeItem("stockroom_token");
      router.replace("/login");
    });
  }, [router]);

  function toggleTheme() {
    const next = !dark;
    setDark(next);
    localStorage.setItem("stockroom_theme", next ? "dark" : "light");
    document.documentElement.classList.toggle("dark", next);
  }

  function signOut() {
    localStorage.removeItem("stockroom_token");
    localStorage.removeItem("stockroom_demo_user");
    router.replace("/login");
  }

  if (!ready) return <div className="page-loading"><span className="live-dot" /> Connecting to Stockroom…</div>;

  return <div className={`app-shell ${dark ? "theme-dark" : ""}`}>
    {mobileOpen && <button className="sidebar-scrim" aria-label="Close navigation" onClick={() => setMobileOpen(false)} />}
    <aside className={`sidebar ${mobileOpen ? "sidebar-open" : ""}`}>
      <Link className="brand" href="/" onClick={() => setMobileOpen(false)}><span className="brand-mark"><Boxes size={20} /></span><span>stockroom<span className="brand-period">.</span></span></Link>
      <div className="workspace-label">WORKSPACE</div>
      <label className="warehouse-switch"><span className="warehouse-icon"><Warehouse size={17} /></span><span className="warehouse-copy"><strong>Active warehouse</strong><select aria-label="Select warehouse" value={warehouseId} onChange={(event) => setWarehouseId(event.target.value)}><option value="ALL">All warehouses</option>{warehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.name}</option>)}</select></span><ChevronDown size={15} /></label>
      <nav className="primary-nav" aria-label="Main navigation">
        <div className="nav-caption">MANAGE</div>
        {primaryLinks.map(({ href, label, icon: Icon }) => <Link key={href} className={`nav-link ${currentPath === href ? "nav-link-active" : ""}`} href={href} onClick={() => setMobileOpen(false)}><Icon size={18} /><span>{label}</span></Link>)}
        <div className="nav-caption nav-caption-spaced">OPERATIONS</div>
        <div className={`nav-link ${currentPath.startsWith("/operations") ? "nav-link-active" : ""}`}><ClipboardList size={18} /><span>Inventory flow</span></div>
        {operationLinks.map(({ href, label }) => <Link key={href} className={`nav-link nav-link-sub ${currentPath === href || (href !== "/operations/ledger" && currentPath.startsWith(href)) ? "nav-link-sub-active" : ""}`} href={href} onClick={() => setMobileOpen(false)}>{label}</Link>)}
        <div className="nav-caption nav-caption-spaced">WORKSPACE</div>
        <Link className={`nav-link ${currentPath.startsWith("/settings") ? "nav-link-active" : ""}`} href="/settings/warehouses" onClick={() => setMobileOpen(false)}><Building2 size={18} /><span>Warehouses</span></Link>
        <Link className="nav-link" href="/operations/ledger" onClick={() => setMobileOpen(false)}><Activity size={18} /><span>Reports & ledger</span></Link>
      </nav>
      <div className="sidebar-bottom"><button className="nav-link signout-link" onClick={signOut}><LogOut size={17} /><span>Sign out</span></button><div className="profile-row"><div className="avatar">{user?.name?.slice(0, 1).toUpperCase() ?? "S"}</div><div className="profile-copy"><strong>{user?.name}</strong><small>{user?.role?.toLowerCase().replaceAll("_", " ")}</small></div><span className="profile-status" title="Signed in" /></div></div>
    </aside>
    <main className="main-content">
      <header className="topbar"><button className="icon-button mobile-menu" aria-label="Open navigation" onClick={() => setMobileOpen(true)}><Menu size={19} /></button><div className="breadcrumb"><span>Stockroom</span><span className="breadcrumb-divider">/</span><strong>{title}</strong></div><div className="topbar-actions"><div className="topbar-date"><span className="live-dot" /> {localDemo ? "LOCAL DEMO" : "API connected"}</div><button className="icon-button theme-toggle" aria-label={dark ? "Switch to light theme" : "Switch to dark theme"} title={dark ? "Light theme" : "Dark theme"} onClick={toggleTheme}>{dark ? <Sun size={18} /> : <Moon size={18} />}</button><div className="avatar avatar-small">{user?.name?.slice(0, 1).toUpperCase() ?? "S"}</div></div></header>
      <div className="page-wrap">{children}<footer className="page-footer"><span>Stockroom · Inventory operations</span><span>{user?.email}</span></footer></div>
    </main>
  </div>;
}