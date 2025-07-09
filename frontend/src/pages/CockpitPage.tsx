/**
 * Cockpit Page with Authenticated Layout
 */
import { AuthenticatedLayout } from '../components/layout/AuthenticatedLayout';
import { SalesCockpit } from '../features/cockpit/components/SalesCockpit';

export function CockpitPage() {
  return (
    <AuthenticatedLayout>
      <SalesCockpit />
    </AuthenticatedLayout>
  );
}