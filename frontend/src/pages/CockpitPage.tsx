/**
 * Cockpit Page with Authenticated Layout
 */
import { AuthenticatedLayout } from '../components/layout/AuthenticatedLayout';
import { SalesCockpitMUI } from '../features/cockpit/components/SalesCockpitMUI';

export function CockpitPage() {
  return (
    <AuthenticatedLayout>
      <SalesCockpitMUI />
    </AuthenticatedLayout>
  );
}