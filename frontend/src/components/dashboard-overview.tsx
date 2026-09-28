"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { Activity, ArrowDownLeft, ArrowLeftRight, ArrowUpRight, Boxes, ClipboardList, Package, Plus, TriangleAlert, Warehouse } from "lucide-react";
import { api, type DashboardSummary, type Product } from "@/services/api";

const weeklyBars = [
  { day: "Mon", received: 52, shipped: 37 }, { day: "Tue", received: 68, shipped: 45 },
  { day: "Wed", received: 44, shipped: 58 }, { day: "Thu", received: 79, shipped: 51 },
  { day: "Fri", received: 61, shipped: 73 }, { day: "Sat", received: 35, shipped: 28 }, { day: "Sun", received: 22, shipped: 18 },
];

export function DashboardOverview() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([api.dashboard(), api.products()]).then(([stats, catalog]) => {
      setSummary(stats);
      setProducts(catalog.items);
    }).catch((cause) => setError(localStorage.getItem("stockroom_token") === "local-demo-admin"
      ? "Local demo session is active. Connect the backend database to load live inventory figures."
      : cause instanceof Error ? cause.message : "Could not load inventory overview"));
  }, []);

  const lowStock = products.filter((product) => product.on_hand <= product.reorder_point).slice(0, 4);
  const metrics = [
    { label: "Stock on hand", value: summary ? summary.units_on_hand.toLocaleString() : "—", note: "physical units", icon: Boxes, tone: "mint" },
    { label: "Inventory value", value: summary ? `$${summary.inventory_value.toLocaleString(undefined, { maximumFractionDigits: 2 })}` : "—", note: "current stock value", icon: Activity, tone: "blue" },
    { label: "Low stock items", value: summary ? String(summary.low_stock_products).padStart(2, "0") : "—", note: "at or below reorder point", icon: TriangleAlert, tone: "coral" },
    { label: "Open operations", value: summary ? String(summary.draft_receipts + summary.draft_deliveries + summary.draft_transfers + summary.draft_adjustments) : "—", note: "draft documents", icon: ClipboardList, tone: "gold" },
  ];

  return <>
    <section className="page-heading"><div><div className="eyebrow"><span className="live-dot" /> LIVE INVENTORY</div><h1>Warehouse overview</h1><p>Current stock position and work awaiting action.</p></div><div className="heading-actions"><Link className="button button-secondary" href="/operations/receipts"><ArrowDownLeft size={16} /> Receive stock</Link><Link className="button button-primary" href="/products"><Plus size={17} /> Manage products</Link></div></section>
    {error && <div className="notice notice-error" role="alert">{error}</div>}
    <section className="metric-grid" aria-label="Inventory summary">{metrics.map(({ label, value, note, icon: Icon, tone }) => <article className="metric-card" key={label}><div className="metric-top"><span className={`metric-icon metric-${tone}`}><Icon size={18} /></span><span className="metric-label">{label}</span></div><div className="metric-value">{value}</div><div className="metric-foot">{note}</div></article>)}</section>
    <section className="insights-grid"><article className="panel movement-panel"><div className="panel-heading"><div><h2>Weekly movement</h2><p>Illustrative trend until movement analytics are exposed by the API</p></div><span className="category-tag">THIS WEEK</span></div><div className="chart-legend"><span><i className="legend-received" /> Receipts</span><span><i className="legend-shipped" /> Deliveries</span></div><div className="bar-chart" role="img" aria-label="Illustrative weekly receipt and delivery chart"><div className="bar-groups">{weeklyBars.map((day) => <div className="bar-group" key={day.day}><div className="bar-pair"><span className="bar bar-received" style={{ height: `${day.received}%` }} /><span className="bar bar-shipped" style={{ height: `${day.shipped}%` }} /></div><span className="bar-label">{day.day}</span></div>)}</div></div></article>
      <article className="panel alerts-panel"><div className="panel-heading"><div><h2>Needs attention</h2><p>Products at or below their reorder point</p></div><span className="alert-total">{summary?.low_stock_products ?? "—"}</span></div><div className="alert-list">{lowStock.length ? lowStock.map((product) => <div className="alert-item" key={product.id}><div className="alert-item-top"><span className="alert-product-icon"><Package size={16} /></span><div className="alert-copy"><strong>{product.name}</strong><small>{product.sku}</small></div><span className="alert-quantity">{product.on_hand} / {product.reorder_point}</span></div><div className="alert-progress"><span style={{ width: `${product.reorder_point ? Math.min(100, product.on_hand / product.reorder_point * 100) : 0}%` }} /></div></div>) : <p className="empty-state">{summary ? "No low-stock products." : "Loading stock alerts…"}</p>}</div><Link className="panel-footer-link" href="/products?low_stock=true">Review product catalog <ArrowUpRight size={14} /></Link></article></section>
    <section className="workflow-grid"><WorkflowCard href="/operations/receipts" title="Inbound receipts" count={summary?.draft_receipts} detail="Receive vendor stock" icon={<ArrowDownLeft size={18} />} tone="mint" /><WorkflowCard href="/operations/deliveries" title="Outbound deliveries" count={summary?.draft_deliveries} detail="Pick, pack and dispatch" icon={<ArrowUpRight size={18} />} tone="blue" /><WorkflowCard href="/operations/transfers" title="Internal transfers" count={summary?.draft_transfers} detail="Move stock between bins" icon={<ArrowLeftRight size={18} />} tone="gold" /><WorkflowCard href="/operations/adjustments" title="Stock adjustments" count={summary?.draft_adjustments} detail="Reconcile physical counts" icon={<ClipboardList size={18} />} tone="coral" /></section>
    <section className="bottom-strip"><div className="bottom-note"><span className="bottom-icon"><Warehouse size={16} /></span><span><strong>{summary?.product_count ?? "—"} active products</strong><small>Across all configured warehouses</small></span></div><Link className="bottom-link" href="/operations/ledger">Open stock ledger <ArrowUpRight size={14} /></Link></section>
  </>;
}

function WorkflowCard({ href, title, count, detail, icon, tone }: { href: string; title: string; count?: number; detail: string; icon: React.ReactNode; tone: string }) {
  return <Link href={href} className="workflow-card"><div className={`workflow-icon metric-${tone}`}>{icon}</div><div className="workflow-copy"><strong>{title}</strong><small>{detail}</small></div><span className="workflow-count">{count ?? "—"}</span></Link>;
}