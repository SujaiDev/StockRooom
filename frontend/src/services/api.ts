const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

export type Page<T> = { items: T[]; page: number; limit: number; total: number; total_pages: number };
export type Category = { id: number; name: string; parent_id?: number | null };
export type Location = { id: number; warehouse_id?: number | null; warehouse_name?: string; name: string; code: string; is_internal: boolean; is_virtual: boolean };
export type Warehouse = { id: number; name: string; code: string; address?: string; locations?: Location[] };
export type Product = { id: number; name: string; sku: string; category_id?: number; category_name?: string; unit_of_measure?: string; per_unit_cost?: number; reorder_point: number; reorder_qty: number; on_hand: number; stock_by_location?: Array<{ location_id: number; location_name: string; location_code: string; warehouse_name?: string; on_hand: number }> };
export type Movement = { id: number; product_id: number; product_name: string; product_sku: string; location_from_name: string; location_to_name: string; movement_type: string; quantity: number; running_balance: number; created_at: string };
export type DocumentLine = { product_id: number; location_from_id?: number; location_to_id?: number; quantity: number };
export type InventoryDocument = { id: number; document_number: string; document_type: string; status: "DRAFT" | "DONE" | "CANCELLED"; note?: string; created_at: string; lines: Array<DocumentLine & { product_name: string; product_sku: string; location_from_name?: string; location_to_name?: string }> };
export type DashboardSummary = { product_count: number; units_on_hand: number; inventory_value: number; low_stock_products: number; draft_receipts: number; draft_deliveries: number; draft_transfers: number; draft_adjustments: number };

type ApiEnvelope<T> = { success: boolean; data?: T; error?: string; field_errors?: Record<string, string> };

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = typeof window === "undefined" ? null : localStorage.getItem("stockroom_token");
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      Accept: "application/json",
      ...(init.body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
    cache: "no-store",
  });
  const envelope = await response.json().catch(() => ({})) as ApiEnvelope<T>;
  if (!response.ok || !envelope.success) {
    const detail = envelope.field_errors ? Object.values(envelope.field_errors).join("; ") : envelope.error;
    throw new Error(detail || `Request failed (${response.status})`);
  }
  return envelope.data as T;
}

export const api = {
  login: (email: string, password: string) => request<{ token: string; user: { name: string; email: string; role: string } }>("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) }),
  signup: (name: string, email: string, password: string) => request<{ token: string; user: { name: string; email: string; role: string } }>("/auth/signup", { method: "POST", body: JSON.stringify({ name, email, password }) }),
  me: () => request<{ id: number; name: string; email: string; role: string }>("/auth/me"),
  dashboard: () => request<DashboardSummary>("/dashboard/summary"),
  products: (search = "", lowStock = false, categoryId?: number, warehouseId?: number) => request<Page<Product>>(`/products?page=1&limit=100${search ? `&search=${encodeURIComponent(search)}` : ""}${lowStock ? "&low_stock=true" : ""}${categoryId ? `&category_id=${categoryId}` : ""}${warehouseId ? `&warehouse_id=${warehouseId}` : ""}`),
  product: (id: number) => request<Product>(`/products/${id}`),
  createProduct: (payload: Record<string, unknown>) => request<Product>("/products", { method: "POST", body: JSON.stringify(payload) }),
  categories: () => request<Category[]>("/categories"),
  createCategory: (name: string) => request<Category>("/categories", { method: "POST", body: JSON.stringify({ name }) }),
  warehouses: () => request<Warehouse[]>("/warehouses"),
  createWarehouse: (payload: Record<string, unknown>) => request<Warehouse>("/warehouses", { method: "POST", body: JSON.stringify(payload) }),
  locations: (warehouseId?: number) => request<Location[]>(`/locations${warehouseId ? `?warehouse_id=${warehouseId}` : ""}`),
  createLocation: (payload: Record<string, unknown>) => request<Location>("/locations", { method: "POST", body: JSON.stringify(payload) }),
  ledger: (productId?: number) => request<Page<Movement>>(`/ledger?page=1&limit=100${productId ? `&product_id=${productId}` : ""}`),
  documents: (type: string) => request<Page<InventoryDocument>>(`/${type}?page=1&limit=100`),
  createDocument: (type: string, payload: { note?: string; lines: DocumentLine[] }) => request<InventoryDocument>(`/${type}`, { method: "POST", body: JSON.stringify(payload) }),
  validateDocument: (type: string, id: number) => request<InventoryDocument>(`/${type}/${id}/validate`, { method: "PUT" }),
  cancelDocument: (type: string, id: number) => request<InventoryDocument>(`/${type}/${id}/cancel`, { method: "POST" }),
};