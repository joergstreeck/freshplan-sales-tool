/**
 * Debug Helper für Field Theme System
 *
 * Zeigt die berechneten Feldgrößen für alle Felder im CustomerOnboardingWizard
 */

import { getFieldSize, debugFieldLayout } from './fieldSizeCalculator';
import customerFieldCatalog from '../data/fieldCatalog.json';

export function debugCustomerFieldTheme() {
  // Base Fields
  customerFieldCatalog.customer.base.forEach(field => {
    const calculatedSize = getFieldSize(field);
    const hasGridSize = !!field.gridSize;
  });

  // Debug Layout

  // Summary
  const fieldsWithGridSize = customerFieldCatalog.customer.base.filter(f => f.gridSize).length;
  const totalFields = customerFieldCatalog.customer.base.length;

  // Comparison
  customerFieldCatalog.customer.base.forEach(field => {
    if (field.gridSize) {
      const calculated = getFieldSize(field);
      const current = field.gridSize.md;
      const calcMd = calculated.md;

      if (current !== calcMd) {
        // Size difference detected
      }
    }
  });
}

// Export für Browser Console
if (typeof window !== 'undefined') {
  (window as unknown).debugFieldTheme = debugCustomerFieldTheme;
}
