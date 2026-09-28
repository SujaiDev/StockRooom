import { DashboardOverview } from "@/components/dashboard-overview";
import { ProtectedPage } from "@/components/protected-page";

export default function Home() {
  return <ProtectedPage title="Overview"><DashboardOverview /></ProtectedPage>;
}