"use client";

import { FormEvent, useEffect, useState } from "react";
import { ArrowDownLeft, ArrowLeftRight, ArrowUpRight, Check, CirclePlus, ClipboardList, LoaderCircle, Package, X } from "lucide-react";
import { api, type InventoryDocument, type Location, type Product, type Warehouse } from "@/services/api";

const configs = {
  receipts: { title: "Inbound receipts", singular: "Receipt", detail: "Record vendor stock arriving at a warehouse.", icon: ArrowDownLeft },
  deliveries: { title: "Outbound deliveries", singular: "Delivery", detail: "Dispatch available stock to a customer.", icon: ArrowUpRight },
  transfers: { title: "Internal transfers", singular: "Transfer", detail: "Move stock between physical warehouse locations.", icon: ArrowLeftRight },
  adjustments: { title: "Stock adjustments", singular: "Adjustment", detail: "Reconcile physical counts against recorded stock.", icon: ClipboardList },
} as const;
type Collection = keyof typeof configs;

export function OperationsWorkspace({ collection }: { collection: Collection }) {
  const config = configs[collection];
  const Icon = config.icon;
  const [documents, setDocuments] = useState<InventoryDocument[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [locations, setLocations] = useState<Location[]>([]);
  const [open, setOpen] = useState(false);
  const [adjustmentDirection, setAdjustmentDirection] = useState<"gain" | "loss">("gain");
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    setLoading(true);
    Promise.all([api.documents(collection), api.products(), api.warehouses(), api.locations()]).then(([documentPage, productPage, warehouseList, locationList]) => {
      setDocuments(documentPage.items);
      setProducts(productPage.items);
      setWarehouses(warehouseList);
      setLocations(locationList.filter((location) => !location.is_virtual));
      setError("");
    }).catch((cause) => setError(cause instanceof Error ? cause.message : "Could not load operations"))
      .finally(() => setLoading(false));
  }, [collection, refresh]);

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const productId = Number(data.get("product_id"));
    const quantity = Number(data.get("quantity"));
    const fromId = data.get("location_from_id") ? Number(data.get("location_from_id")) : undefined;
    const toId = data.get("location_to_id") ? Number(data.get("location_to_id")) : undefined;
    setError("");
    try {
      await api.createDocument(collection, { note: String(data.get("note") ?? ""), lines: [{ product_id: productId, ...(fromId ? { location_from_id: fromId } : {}), ...(toId ? { location_to_id: toId } : {}), quantity }] });
      setOpen(false);
      setNotice(`${config.singular} draft created.`);
      setRefresh((value) => value + 1);
    } catch (cause) { setError(cause instanceof Error ? cause.message : `Could not create ${config.singular.toLowerCase()}`); }
  }

  async function changeStatus(document: InventoryDocument, action: "validate" | "cancel") {
    setBusyId(document.id);
    setError("");
    try {
      if (action === "validate") await api.validateDocument(collection, document.id);
      else await api.cancelDocument(collection, document.id);
      setNotice(`${document.document_number} ${action === "validate" ? "validated" : "cancelled"}.`);
      setRefresh((value) => value + 1);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Could not update document"); }
    finally { setBusyId(null); }
  }

  return <>
    <section className="page-heading"><div><div className="eyebrow"><span className="live-dot" /> INVENTORY FLOW</div><h1>{config.title}</h1><p>{config.detail}</p></div><button className="button button-primary" onClick={() => setOpen(true)}><CirclePlus size={17} /> New {config.singular.toLowerCase()}</button></section>
    {error && <div className="notice notice-error" role="alert">{error}</div>}{notice && <div className="notice notice-success" role="status">{notice}<button aria-label="Dismiss" onClick={() => setNotice("")}><X size={14} /></button></div>}
    <section className="panel catalog-panel"><div className="catalog-toolbar"><div className="section-heading"><strong>Documents</strong><span>{documents.length} records</span></div><span className="category-tag">Newest first</span></div><div className="table-scroll"><table className="data-table"><thead><tr><th>REFERENCE</th><th>CREATED</th><th>LINES</th><th>STATUS</th><th>NOTE</th><th>ACTIONS</th></tr></thead><tbody>{documents.map((document) => <tr key={document.id}><td className="sku-cell">{document.document_number}</td><td>{new Date(document.created_at).toLocaleString()}</td><td>{document.lines.length} · {document.lines.reduce((sum, line) => sum + line.quantity, 0)} units</td><td><span className={`status-tag ${document.status === "DONE" ? "status-done" : document.status === "CANCELLED" ? "status-cancelled" : "status-warning"}`}>{document.status}</span></td><td>{document.note || "—"}</td><td><div className="row-actions">{document.status === "DRAFT" && <><button className="small-action action-confirm" onClick={() => changeStatus(document, "validate")} disabled={busyId === document.id}>{busyId === document.id ? <LoaderCircle size={14} className="spin" /> : <Check size={14} />} Validate</button><button className="small-action action-cancel" onClick={() => changeStatus(document, "cancel")} disabled={busyId === document.id}><X size={14} /> Cancel</button></>}</div></td></tr>)}</tbody></table>{loading ? <div className="empty-state">Loading documents…</div> : documents.length === 0 ? <div className="empty-state">No {config.title.toLowerCase()} yet. Create a draft to begin.</div> : null}</div></section>
    {open && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setOpen(false); }}><section className="dialog" role="dialog" aria-modal="true" aria-labelledby="document-dialog-title"><div className="dialog-heading"><div><span className="dialog-kicker">{config.title.toUpperCase()}</span><h2 id="document-dialog-title">Create {config.singular.toLowerCase()} draft</h2></div><button className="icon-button" aria-label="Close dialog" onClick={() => setOpen(false)}><X size={18} /></button></div><form className="dialog-form" onSubmit={create}><label>Product<select name="product_id" required defaultValue=""><option value="" disabled>Select product</option>{products.map((product) => <option value={product.id} key={product.id}>{product.name} · {product.sku} · {product.on_hand} on hand</option>)}</select></label>{collection === "adjustments" ? <><label>Adjustment direction<select value={adjustmentDirection} onChange={(event) => setAdjustmentDirection(event.target.value as "gain" | "loss")}><option value="gain">Stock gain</option><option value="loss">Stock loss</option></select></label><label>Physical location<select name={adjustmentDirection === "gain" ? "location_to_id" : "location_from_id"} required defaultValue=""><option value="" disabled>Select location</option>{locations.map((location) => <option key={location.id} value={location.id}>{location.name} ({location.code})</option>)}</select></label></> : <>{collection !== "receipts" && <label>Source location<select name="location_from_id" required defaultValue=""><option value="" disabled>Select source</option>{locations.map((location) => <option key={location.id} value={location.id}>{location.name} ({location.code})</option>)}</select></label>}{collection !== "deliveries" && <label>Destination location<select name="location_to_id" required defaultValue=""><option value="" disabled>Select destination</option>{locations.map((location) => <option key={location.id} value={location.id}>{location.name} ({location.code})</option>)}</select></label>}</>}<label>Quantity<input name="quantity" type="number" min="0.0001" step="0.0001" required /></label><label>Note<input name="note" maxLength={500} placeholder="Optional reference or reason" /></label>{error && <p className="form-error">{error}</p>}<button className="button button-primary dialog-submit" type="submit"><Package size={15} /> Create draft</button></form></section></div>}
  </>;
}