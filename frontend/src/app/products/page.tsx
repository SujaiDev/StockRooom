import { ProtectedPage } from "@/components/protected-page";
import { ProductsWorkspace } from "@/components/products-workspace";

export default function ProductsPage() {
  return <ProtectedPage title="Products"><ProductsWorkspace /></ProtectedPage>;
}