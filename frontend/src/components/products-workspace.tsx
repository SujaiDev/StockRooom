"use client";

import { FormEvent, useEffect, useState } from "react";
import { ArrowUpRight, CirclePlus, Package, Search, TriangleAlert, X } from "lucide-react";
import { api, type Category, type Product } from "@/services/api";

export function ProductsWorkspace() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [search, setSearch] = useState("");
  const [lowOnly, setLowOnly] = useState(false);
  const [dialog, setDialog] = useState(false);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    Promise.all([api.products(search, lowOnly), api.categories()]).then(([page, categoryList]) => {
      setProducts(page.items);
      setCategories(categoryList);
      setError("");
    }).catch((cause) => setError(cause instanceof Error ? cause.message : "Could not load products"));
  }, [search, lowOnly, refresh]);

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    setSaving(true);
    setError("");
    try {
      await api.createProduct({ name: data.get("name"), sku: data.get("sku"), category_id: data.get("category_id") || null, unit_of_measure: data.get("unit_of_measure"), per_unit_cost: Number(data.get("per_unit_cost") || 0), reorder_point: Number(data.get("reorder_point") || 0), reorder_qty: Number(data.get("reorder_qty") || 0) });
      setDialog(false);
      setRefresh((value) => value + 1);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Could not create product"); }
    finally { setSaving(false); }
  }

  return <>
    <section className="page-heading"><div><div className="eyebrow"><span className="live-dot" /> CATALOG</div><h1>Products</h1><p>Search, filter, and maintain products and reorder thresholds.</p></div><button className="button button-primary" onClick={() => setDialog(true)}><CirclePlus size={17} /> New product</button></section>
    {error && <div role="alert" className="notice notice-error">{error}</div>}
    <section className="panel catalog-panel"><div className="catalog-toolbar"><label className="table-search catalog-search"><Search size={15} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search name or SKU" aria-label="Search products" /></label><label className="toggle-filter"><input type="checkbox" checked={lowOnly} onChange={(event) => setLowOnly(event.target.checked)} /><span>Low stock only</span><TriangleAlert size={14} /></label><span className="results-count">{products.length} products</span></div><div className="table-scroll"><table className="data-table"><thead><tr><th>PRODUCT</th><th>SKU</th><th>CATEGORY</th><th>ON HAND</th><th>REORDER AT</th><th>UNIT COST</th><th>STATUS</th></tr></thead><tbody>{products.map((product) => { const low = product.on_hand <= product.reorder_point; return <tr key={product.id}><td><div className="product-cell"><span className="product-icon product-pack"><Package size={17} /></span><strong>{product.name}</strong></div></td><td className="sku-cell">{product.sku}</td><td>{product.category_name ?? "Uncategorized"}</td><td><span className={`stock-value ${low ? "stock-low" : ""}`}><i />{product.on_hand} <small>{product.unit_of_measure ?? "units"}</small></span></td><td>{product.reorder_point}</td><td>{product.per_unit_cost == null ? "—" : `$${product.per_unit_cost.toFixed(2)}`}</td><td><span className={`status-tag ${low ? "status-warning" : "status-done"}`}>{low ? "Reorder" : "In stock"}</span></td></tr>; })}</tbody></table>{!products.length && <div className="empty-state">No products match this view.</div>}</div></section>
    {dialog && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setDialog(false); }}><section className="dialog" role="dialog" aria-modal="true" aria-labelledby="product-dialog-title"><div className="dialog-heading"><div><span className="dialog-kicker">PRODUCT CATALOG</span><h2 id="product-dialog-title">Create product</h2></div><button className="icon-button" aria-label="Close dialog" onClick={() => setDialog(false)}><X size={18} /></button></div><form className="dialog-form" onSubmit={create}><label>Product name<input name="name" required autoFocus /></label><label>SKU<input name="sku" required /></label><label>Category<select name="category_id" defaultValue=""><option value="">Uncategorized</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label><div className="form-grid"><label>Unit<input name="unit_of_measure" defaultValue="each" /></label><label>Unit cost<input name="per_unit_cost" type="number" min="0" step="0.01" defaultValue="0" /></label></div><div className="form-grid"><label>Reorder point<input name="reorder_point" type="number" min="0" defaultValue="0" /></label><label>Reorder quantity<input name="reorder_qty" type="number" min="0" defaultValue="0" /></label></div>{error && <p className="form-error">{error}</p>}<button className="button button-primary dialog-submit" type="submit" disabled={saving}>{saving ? "Creating…" : "Create product"}<ArrowUpRight size={15} /></button></form></section></div>}
  </>;
}