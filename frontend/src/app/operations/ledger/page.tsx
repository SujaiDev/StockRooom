import { LedgerWorkspace } from "@/components/ledger-workspace";
import { ProtectedPage } from "@/components/protected-page";

export default function LedgerPage() {
  return <ProtectedPage title="Stock ledger"><LedgerWorkspace /></ProtectedPage>;
}