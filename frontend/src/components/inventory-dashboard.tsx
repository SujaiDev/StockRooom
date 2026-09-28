"use client";

import { useMemo, useState } from "react";
import {
  Activity,
  ArrowDownLeft,
  ArrowUpRight,
  Bell,
  Boxes,
  ChevronDown,
  CircleHelp,
  ClipboardList,
  Clock3,
  LayoutDashboard,
  MapPin,
  Menu,
  Package,
  Plus,
  Search,
  Settings2,
  SlidersHorizontal,
  Warehouse,
  X,
} from "lucide-react";

type InventoryItem = {
  name: string;
  sku: string;
  category: string;
  quantity: number;
  reorderAt: number;
  location: string;
  value: number;
};

const initialItems: InventoryItem[] = [
  { name: "Wireless barcode scanner", sku: "SCN-2048", category: "Equipment", quantity: 42, reorderAt: 18, location: "A-01-04", value: 84 },
  { name: "Thermal label roll, 4 × 6 in", sku: "LBL-1102", category: "Packaging", quantity: 16, reorderAt: 24, location: "B-03-02", value: 8.5 },
  { name: "Shipping carton, medium", sku: "PKG-3810", category: "Packaging", quantity: 128, reorderAt: 40, location: "B-01-01", value: 2.15 },
  { name: "Handheld terminal battery", sku: "PWR-0091", category: "Equipment", quantity: 9, reorderAt: 12, location: "A-02-06", value: 36 },
  { name: "Pallet wrap, clear", sku: "PKG-1477", category: "Supplies", quantity: 73, reorderAt: 20, location: "C-04-03", value: 14.75 },
];

const navItems = [
  { label: "Overview", icon: LayoutDashboard, active: true, href: "#overview" },
  { label: "Products", icon: Package, active: false, href: "#products" },
  { label: "Operations", icon: ClipboardList, active: false, href: "#operations" },
  { label: "Inventory", icon: Boxes, active: false, href: "#inventory" },
];

const week = [
  { day: "Mon", received: 52, shipped: 37 },
  { day: "Tue", received: 68, shipped: 45 },
  { day: "Wed", received: 44, shipped: 58 },
  { day: "Thu", received: 79, shipped: 51 },
  { day: "Fri", received: 61, shipped: 73 },
  { day: "Sat", received: 35, shipped: 28 },
  { day: "Sun", received: 22, shipped: 18 },
];

export function InventoryDashboard() {
  const [items, setItems] = useState(initialItems);
  const [query, setQuery] = useState("");
  const [dialog, setDialog] = useState<"product" | "movement" | null>(null);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const filteredItems = useMemo(
    () => items.filter((item) => `${item.name} ${item.sku} ${item.location}`.toLowerCase().includes(query.toLowerCase())),
    [items, query],
  );
  const lowStockCount = items.filter((item) => item.quantity <= item.reorderAt).length + 6;

  function addProduct(formData: FormData) {
    const name = String(formData.get("name") ?? "").trim();
    const sku = String(formData.get("sku") ?? "").trim();
    const quantity = Number(formData.get("quantity"));
    if (!name || !sku || !Number.isFinite(quantity)) return;
    setItems((current) => [
      { name, sku, quantity, category: "New item", reorderAt: 10, location: "Unassigned", value: 0 },
      ...current,
    ]);
    setDialog(null);
  }

  function recordMovement(formData: FormData) {
    const sku = String(formData.get("sku") ?? "");
    const quantity = Number(formData.get("quantity"));
    const direction = String(formData.get("direction"));
    if (!sku || !Number.isFinite(quantity) || quantity <= 0) return;
    setItems((current) => current.map((item) => item.sku === sku
      ? { ...item, quantity: Math.max(0, item.quantity + (direction === "receive" ? quantity : -quantity)) }
      : item));
    setDialog(null);
  }

  return (
    <div className="app-shell" id="overview">
      <aside className={`sidebar ${mobileNavOpen ? "sidebar-open" : ""}`}>
        <a className="brand" href="#overview" aria-label="Stockroom overview">
          <span className="brand-mark"><Boxes size={20} strokeWidth={2.2} /></span>
          <span>stockroom<span className="brand-period">.</span></span>
        </a>
        <div className="workspace-label">WORKSPACE</div>
        <div className="warehouse-switch">
          <span className="warehouse-icon"><Warehouse size={17} /></span>
          <span className="warehouse-copy"><strong>North warehouse</strong><small>Seattle, WA</small></span>
          <ChevronDown size={15} />
        </div>
        <nav className="primary-nav" aria-label="Main navigation">
          <div className="nav-caption">MANAGE</div>
          {navItems.map(({ label, icon: Icon, active, href }) => (
            <a className={`nav-link ${active ? "nav-link-active" : ""}`} href={href} key={label} onClick={() => setMobileNavOpen(false)}>
              <Icon size={18} strokeWidth={1.8} /><span>{label}</span>
              {label === "Operations" && <span className="nav-count">4</span>}
            </a>
          ))}
          <div className="nav-caption nav-caption-spaced">WORKSPACE</div>
          <a className="nav-link" href="#locations"><MapPin size={18} strokeWidth={1.8} /><span>Locations</span></a>
          <a className="nav-link" href="#reports"><Activity size={18} strokeWidth={1.8} /><span>Reports</span></a>
        </nav>
        <div className="sidebar-bottom">
          <div className="plan-card">
            <div className="plan-card-icon"><CircleHelp size={16} /></div>
            <strong>Need a hand?</strong>
            <span>Visit the help center for quick answers.</span>
            <a href="#help">Open help center <ArrowUpRight size={13} /></a>
          </div>
          <a className="nav-link settings-link" href="#settings"><Settings2 size={18} strokeWidth={1.8} /><span>Settings</span></a>
          <div className="profile-row">
            <div className="avatar">JM</div>
            <div className="profile-copy"><strong>Jordan Miller</strong><small>Inventory manager</small></div>
            <ChevronDown size={15} />
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <button className="icon-button mobile-menu" aria-label="Open navigation" onClick={() => setMobileNavOpen(!mobileNavOpen)}><Menu size={19} /></button>
          <div className="breadcrumb"><span>Workspace</span><span className="breadcrumb-divider">/</span><strong>Overview</strong></div>
          <div className="topbar-actions">
            <label className="global-search"><Search size={16} /><input aria-label="Search inventory" placeholder="Search anything..." value={query} onChange={(event) => setQuery(event.target.value)} /><kbd>⌘ K</kbd></label>
            <button className="icon-button notification-button" aria-label="Notifications"><Bell size={18} /><i /></button>
            <div className="topbar-date">Monday, Oct 14</div>
          </div>
        </header>

        <div className="page-wrap">
          <section className="page-heading">
            <div>
              <div className="eyebrow"><span className="live-dot" /> LIVE INVENTORY</div>
              <h1>Good morning, Jordan <span>✳</span></h1>
              <p>Here&apos;s what&apos;s happening across your warehouse today.</p>
            </div>
            <div className="heading-actions">
              <button className="button button-secondary" onClick={() => setDialog("movement")}><ArrowDownLeft size={16} /> Record movement</button>
              <button className="button button-primary" onClick={() => setDialog("product")}><Plus size={17} /> Add product</button>
            </div>
          </section>

          <section className="metric-grid" aria-label="Inventory summary">
            <MetricCard label="Stock on hand" value="24,680" suffix="units" change="8.2%" detail="vs. last month" icon={<Boxes size={18} />} tone="mint" trend="up" />
            <MetricCard label="Inventory value" value="$184,290" change="4.6%" detail="vs. last month" icon={<Activity size={18} />} tone="blue" trend="up" />
            <MetricCard label="Low stock items" value={String(lowStockCount).padStart(2, "0")} detail="Need replenishment" icon={<Package size={18} />} tone="coral" trend="alert" />
            <MetricCard label="Open operations" value="12" detail="3 awaiting action" icon={<ClipboardList size={18} />} tone="gold" trend="neutral" />
          </section>

          <section className="insights-grid">
            <article className="panel movement-panel" id="operations">
              <div className="panel-heading">
                <div><h2>Stock movement</h2><p>Items received and shipped this week</p></div>
                <button className="select-button">This week <ChevronDown size={14} /></button>
              </div>
              <div className="chart-legend"><span><i className="legend-received" /> Received</span><span><i className="legend-shipped" /> Shipped</span><strong>+148 <small>net units</small></strong></div>
              <div className="bar-chart" role="img" aria-label="Weekly stock movement chart">
                <div className="chart-gridline"><span>100</span></div><div className="chart-gridline"><span>75</span></div><div className="chart-gridline"><span>50</span></div><div className="chart-gridline"><span>25</span></div>
                <div className="bar-groups">{week.map((day) => <div className="bar-group" key={day.day}>
                  <div className="bar-pair"><span className="bar bar-received" style={{ height: `${day.received}%` }} /><span className="bar bar-shipped" style={{ height: `${day.shipped}%` }} /></div>
                  <span className="bar-label">{day.day}</span>
                </div>)}</div>
              </div>
            </article>

            <article className="panel alerts-panel">
              <div className="panel-heading">
                <div><h2>Needs attention</h2><p>Items below reorder point</p></div>
                <span className="alert-total">{lowStockCount}</span>
              </div>
              <div className="alert-list">
                <AlertItem name="Handheld terminal battery" sku="PWR-0091" quantity="9 left" threshold="Reorder at 12" progress={22} />
                <AlertItem name="Thermal label roll, 4 × 6 in" sku="LBL-1102" quantity="16 left" threshold="Reorder at 24" progress={39} />
                <AlertItem name="Packing tape, kraft" sku="PKG-2218" quantity="18 left" threshold="Reorder at 25" progress={48} />
              </div>
              <a className="panel-footer-link" href="#products">Review all low stock <ArrowUpRight size={14} /></a>
            </article>
          </section>

          <section className="panel inventory-panel" id="products">
            <div className="panel-heading inventory-heading">
              <div><h2>Product inventory</h2><p>A quick look at stock across North warehouse</p></div>
              <div className="table-actions"><label className="table-search"><Search size={15} /><input aria-label="Filter products" placeholder="Filter products" value={query} onChange={(event) => setQuery(event.target.value)} /></label><button className="filter-button"><SlidersHorizontal size={15} /><span>Filters</span></button></div>
            </div>
            <div className="table-scroll">
              <table>
                <thead><tr><th>PRODUCT</th><th>SKU</th><th>CATEGORY</th><th>ON HAND</th><th>LOCATION</th><th>UNIT COST</th><th /></tr></thead>
                <tbody>{filteredItems.map((item) => {
                  const low = item.quantity <= item.reorderAt;
                  return <tr key={item.sku}>
                    <td><div className="product-cell"><span className={`product-icon product-${item.category === "Packaging" ? "pack" : item.category === "Equipment" ? "equip" : "supply"}`}><Package size={17} /></span><strong>{item.name}</strong></div></td>
                    <td className="sku-cell">{item.sku}</td>
                    <td><span className="category-tag">{item.category}</span></td>
                    <td><span className={`stock-value ${low ? "stock-low" : ""}`}><i />{item.quantity} <small>units</small></span></td>
                    <td className="location-cell">{item.location}</td>
                    <td className="cost-cell">${item.value.toFixed(2)}</td>
                    <td><button className="row-menu" aria-label={`More actions for ${item.name}`}>···</button></td>
                  </tr>;
                })}</tbody>
              </table>
              {filteredItems.length === 0 && <div className="empty-state">No products match “{query}”.</div>}
            </div>
            <div className="table-pagination"><span>Showing <strong>{filteredItems.length}</strong> of <strong>{items.length}</strong> products</span><div><button disabled>Previous</button><button>Next <ArrowUpRight size={13} /></button></div></div>
          </section>

          <section className="bottom-strip" id="inventory">
            <div className="bottom-note"><span className="bottom-icon"><Clock3 size={16} /></span><span><strong>Last synced 2 minutes ago</strong><small>All warehouse data is up to date</small></span></div>
            <div className="bottom-note"><span className="bottom-icon bottom-icon-teal"><Warehouse size={16} /></span><span><strong>3 active warehouses</strong><small>North, Central, and South</small></span></div>
            <button className="bottom-link">View system status <ArrowUpRight size={14} /></button>
          </section>
          <footer className="page-footer"><span>© 2025 Stockroom</span><span>Inventory that moves with you.</span></footer>
        </div>
      </main>

      {dialog && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setDialog(null); }}>
        <section className="dialog" role="dialog" aria-modal="true" aria-labelledby="dialog-title">
          <div className="dialog-heading"><div><span className="dialog-kicker">NORTH WAREHOUSE</span><h2 id="dialog-title">{dialog === "product" ? "Add a product" : "Record stock movement"}</h2></div><button className="icon-button" aria-label="Close dialog" onClick={() => setDialog(null)}><X size={18} /></button></div>
          <form action={dialog === "product" ? addProduct : recordMovement} className="dialog-form">
            {dialog === "product" ? <>
              <label>Product name<input name="name" placeholder="e.g. Shipping carton, large" required autoFocus /></label>
              <label>SKU<input name="sku" placeholder="e.g. PKG-2204" required /></label>
              <label>Starting quantity<input name="quantity" type="number" min="0" defaultValue="0" required /></label>
              <button className="button button-primary dialog-submit" type="submit"><Plus size={16} /> Create product</button>
            </> : <>
              <label>Product<select name="sku" required defaultValue=""><option value="" disabled>Select a product</option>{items.map((item) => <option key={item.sku} value={item.sku}>{item.name} ({item.sku})</option>)}</select></label>
              <label>Movement type<select name="direction"><option value="receive">Receipt (add stock)</option><option value="ship">Delivery (remove stock)</option></select></label>
              <label>Quantity<input name="quantity" type="number" min="1" placeholder="0" required /></label>
              <button className="button button-primary dialog-submit" type="submit"><ArrowDownLeft size={16} /> Save movement</button>
            </>}
          </form>
        </section>
      </div>}
      {mobileNavOpen && <button aria-label="Close navigation" className="sidebar-scrim" onClick={() => setMobileNavOpen(false)} />}
    </div>
  );
}

function MetricCard({ label, value, suffix, change, detail, icon, tone, trend }: {
  label: string; value: string; suffix?: string; change?: string; detail: string; icon: React.ReactNode; tone: string; trend: "up" | "alert" | "neutral";
}) {
  return <article className="metric-card">
    <div className="metric-top"><span className={`metric-icon metric-${tone}`}>{icon}</span><span className="metric-label">{label}</span><button className="metric-more" aria-label={`${label} details`}>···</button></div>
    <div className="metric-value">{value}{suffix && <small> {suffix}</small>}</div>
    <div className="metric-foot">{trend === "up" ? <span className="change-positive"><ArrowUpRight size={13} /> {change}</span> : trend === "alert" ? <span className="change-alert"><span /> Action needed</span> : <span className="change-neutral"><Activity size={13} /> Today</span>}<span>{detail}</span></div>
  </article>;
}

function AlertItem({ name, sku, quantity, threshold, progress }: { name: string; sku: string; quantity: string; threshold: string; progress: number }) {
  return <div className="alert-item">
    <div className="alert-item-top"><span className="alert-product-icon"><Package size={16} /></span><div className="alert-copy"><strong>{name}</strong><small>{sku}</small></div><span className="alert-quantity">{quantity}</span></div>
    <div className="alert-progress"><span style={{ width: `${progress}%` }} /></div>
    <div className="alert-threshold">{threshold}</div>
  </div>;
}