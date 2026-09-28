import { ProtectedPage } from "@/components/protected-page";
import { WarehouseSettings } from "@/components/warehouse-settings";

export default function WarehousesPage() {
  return <ProtectedPage title="Warehouses"><WarehouseSettings /></ProtectedPage>;
}