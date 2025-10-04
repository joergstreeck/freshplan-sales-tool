/**
 * LeadsPage - Lead-Management Interface
 *
 * Lifecycle-Context-Architektur (ADR-006):
 * Wiederverwendung der CustomersPageV2-Komponente mit Lead-Kontext.
 * Leads sind eine Lifecycle-Phase, kein separates Entity.
 *
 * @module LeadsPage
 * @since Sprint 2.1.5
 */

import { CustomersPageV2 } from './CustomersPageV2';

export default function LeadsPage() {
  return (
    <CustomersPageV2
      title="Lead-Management"
      createButtonLabel="Lead erfassen"
      context="leads"
    />
  );
}
