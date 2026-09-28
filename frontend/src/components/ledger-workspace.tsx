"use client";

import { useEffect, useState } from "react";
import { Activity, ArrowDownLeft, ArrowLeftRight, ArrowUpRight, Search, SlidersHorizontal } from "lucide-react";
import { api, type Movement } from "@/services/api";

export function LedgerWorkspace() {
  const [entries, setEntries] = useState<Movement[]>([]);
  const [search, setSearch] = useState("");
  const [type, setType] = useState("ALL");
  const [error, setError] = useState("");

  useEffect(() => {
    api.ledger().then((page) => setEntries(page.items)).catch((cause) => setError(cause instanceof Error ? cause.message : "Could not load stock ledger"));
  }, []);

  const visible = entries.filter((entry) => {
    const matchesType = type === "ALL" || entry.movement_type === type;
    const text = `${entry.product_name} ${entry.product_sku} ${entry.location_from_name} ${entry.location_to_name}`.toLowerCase();
    return matchesType && text.includes(search.toLowerCase());
  });

  return <>
    <section className="page-heading"><div><div className="eyebrow"><span className="live-dot" /> TRACEABILITY</div><h1>Stock ledger</h1><p>Auditable record of completed stock movements and running balances.</p></div><span className="ledger-count"><Activity size={15} /> {entries.length} movements</span></section>
    {error && <div className="notice notice-error" role="alert">{error}</div>}
    <section className="panel catalog-panel"><div className="catalog-toolbar"><label className="table-search catalog-search"><Search size={15} /><input aria-label="Search ledger" placeholder="Search product, SKU, or location" value={search} onChange={(event) => setSearch(event.target.value)} /></label><label className="select-filter"><SlidersHorizontal size={15} /><select aria-label="Filter movement type" value={type} onChange={(event) => setType(event.target.value)}><option value="ALL">All movement types</option><option value="RECEIPT">Receipts</option><option value="DELIVERY">Deliveries</option><option value="TRANSFER">Transfers</option><option value="ADJUSTMENT">Adjustments</option></select></label><span className="results-count">{visible.length} entries</span></div><div className="table-scroll"><table className="data-table"><thead><tr><th>DATE</th><th>PRODUCT</th><th>MOVEMENT</th><th>FROM</th><th>TO</th><th>QTY</th><th>RUNNING BALANCE</th></tr></thead><tbody>{visible.map((entry) => <tr key={entry.id}><td>{new Date(entry.created_at).toLocaleString()}</td><td><div className="product-cell"><span className="product-icon product-equip"><Activity size={16} /></span><span><strong>{entry.product_name}</strong><small className="sku-cell">{entry.product_sku}</small></span></div></td><td><MovementTag type={entry.movement_type} /></td><td>{entry.location_from_name}</td><td>{entry.location_to_name}</td><td className="cost-cell">{entry.quantity}</td><td className="stock-value">{entry.running_balance}</td></tr>)}</tbody></table>{!visible.length && <div className="empty-state">{entries.length ? "No ledger entries match these filters." : "No completed stock movements yet."}</div>}</div></section>
  </>;
}

function MovementTag({ type }: { type: string }) {
  const icon = type === "RECEIPT" ? <ArrowDownLeft size={13} /> : type === "DELIVERY" ? <ArrowUpRight size={13} /> : type === "TRANSFER" ? <ArrowLeftRight size={13} /> : <Activity size={13} />;
  return <span className={`status-tag movement-${type.toLowerCase()}`}>{icon}{type}</span>;
}