"use client";

import { FormEvent, useEffect, useState } from "react";
import { Building2, MapPin, Plus, Warehouse } from "lucide-react";
import { api, type Warehouse as WarehouseType } from "@/services/api";

export function WarehouseSettings() {
  const [warehouses, setWarehouses] = useState<WarehouseType[]>([]);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    api.warehouses().then(setWarehouses).catch((cause) => setError(cause instanceof Error ? cause.message : "Could not load warehouses"));
  }, [refresh]);

  async function createWarehouse(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
      await api.createWarehouse({ name: data.get("name"), code: data.get("code"), address: data.get("address") });
      setNotice("Warehouse created.");
      form.reset();
      setRefresh((value) => value + 1);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Could not create warehouse"); }
  }

  async function createLocation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
      await api.createLocation({ warehouse_id: Number(data.get("warehouse_id")), name: data.get("name"), code: data.get("code") });
      setNotice("Location created.");
      form.reset();
      setRefresh((value) => value + 1);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "Could not create location"); }
  }

  return <>
    <section className="page-heading"><div><div className="eyebrow"><span className="live-dot" /> FACILITY SETUP</div><h1>Warehouses & locations</h1><p>Manage physical facilities and their internal storage locations.</p></div></section>
    {error && <div className="notice notice-error" role="alert">{error}<button onClick={() => setError("")}>Dismiss</button></div>}{notice && <div className="notice notice-success" role="status">{notice}<button onClick={() => setNotice("")}>Dismiss</button></div>}
    <div className="settings-grid"><section className="panel settings-panel"><div className="panel-heading"><div><h2>Create warehouse</h2><p>Add a facility to organize stock</p></div><span className="metric-icon metric-mint"><Building2 size={17} /></span></div><form className="dialog-form inline-form" onSubmit={createWarehouse}><label>Warehouse name<input name="name" placeholder="North Distribution Center" required /></label><label>Code<input name="code" placeholder="WH-NORTH" required /></label><label>Address<input name="address" placeholder="Optional address" /></label><button className="button button-primary" type="submit"><Plus size={15} /> Add warehouse</button></form></section>
      <section className="panel settings-panel"><div className="panel-heading"><div><h2>Create location</h2><p>Add an internal stock location to a facility</p></div><span className="metric-icon metric-blue"><MapPin size={17} /></span></div><form className="dialog-form inline-form" onSubmit={createLocation}><label>Warehouse<select name="warehouse_id" required defaultValue=""><option value="" disabled>Select warehouse</option>{warehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.name}</option>)}</select></label><label>Location name<input name="name" placeholder="Aisle A · Bin 01" required /></label><label>Location code<input name="code" placeholder="A-01-01" required /></label><button className="button button-primary" type="submit"><Plus size={15} /> Add location</button></form></section></div>
    <section className="warehouse-list">{warehouses.map((warehouse) => <article className="panel warehouse-card" key={warehouse.id}><div className="warehouse-card-head"><span className="warehouse-icon"><Warehouse size={18} /></span><div><h2>{warehouse.name}</h2><p>{warehouse.code}{warehouse.address ? ` · ${warehouse.address}` : ""}</p></div><span className="location-count">{warehouse.locations?.length ?? 0} locations</span></div><div className="location-chips">{warehouse.locations?.length ? warehouse.locations.map((location) => <span className="location-chip" key={location.id}><MapPin size={13} />{location.name}<small>{location.code}</small></span>) : <span className="empty-state">No locations yet.</span>}</div></article>)}</section>
  </>;
}