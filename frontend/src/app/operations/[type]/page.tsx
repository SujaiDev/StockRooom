import { notFound } from "next/navigation";
import { OperationsWorkspace } from "@/components/operations-workspace";
import { ProtectedPage } from "@/components/protected-page";

const operationTypes = ["receipts", "deliveries", "transfers", "adjustments"] as const;
type OperationType = typeof operationTypes[number];

export default async function OperationPage({ params }: { params: Promise<{ type: string }> }) {
  const { type } = await params;
  if (!operationTypes.includes(type as OperationType)) notFound();
  const titles: Record<OperationType, string> = { receipts: "Receipts", deliveries: "Deliveries", transfers: "Transfers", adjustments: "Adjustments" };
  return <ProtectedPage title={titles[type as OperationType]}><OperationsWorkspace collection={type as OperationType} /></ProtectedPage>;
}