/**
 * Debug Helper für Field Theme System
 *
 * Zeigt die berechneten Feldgrößen für alle Felder im CustomerOnboardingWizard
 */

import { getFieldSize, debugFieldLayout } from './fieldSizeCalculator';
import customerFieldCatalog from '../data/fieldCatalog.json';

export function debugCustomerFieldTheme() {
  console.log('=== FIELD THEME DEBUG ===');
  console.log('CustomerOnboardingWizard Field Sizes:\n');

  // Base Fields
  console.log('BASE FIELDS:');
  customerFieldCatalog.customer.base.forEach(field => {
    const calculatedSize = getFieldSize(field);
    const hasGridSize = !!field.gridSize;

    console.log(`${field.key}:`);
    console.log(`  Label: ${field.label}`);
    console.log(`  Type: ${field.fieldType}`);
    console.log(`  MaxLength: ${field.maxLength || 'N/A'}`);
    console.log(`  Current gridSize: ${hasGridSize ? `md=${field.gridSize.md}` : 'none'}`);
    console.log(`  Calculated size: md=${calculatedSize.md}`);
    console.log(`  Would use: ${hasGridSize ? 'gridSize (existing)' : 'calculated'}`);
    console.log('');
  });

  // Debug Layout
  console.log('\nLAYOUT VISUALIZATION:');
  console.log(debugFieldLayout(customerFieldCatalog.customer.base));

  // Summary
  const fieldsWithGridSize = customerFieldCatalog.customer.base.filter(f => f.gridSize).length;
  const totalFields = customerFieldCatalog.customer.base.length;

  console.log('\nSUMMARY:');
  console.log(`Total fields: ${totalFields}`);
  console.log(`Fields with gridSize: ${fieldsWithGridSize}`);
  console.log(`Fields using theme: ${totalFields - fieldsWithGridSize}`);

  // Comparison
  console.log('\n=== BEFORE/AFTER COMPARISON ===');
  console.log('Field changes if we remove gridSize:');
  customerFieldCatalog.customer.base.forEach(field => {
    if (field.gridSize) {
      const calculated = getFieldSize(field);
      const current = field.gridSize.md;
      const calcMd = calculated.md;

      if (current !== calcMd) {
        console.log(
          `${field.key}: ${current} → ${calcMd} (${calcMd > current ? '+' : ''}${calcMd - current})`
        );
      }
    }
  });
}

// Export für Browser Console
if (typeof window !== 'undefined') {
  (window as unknown).debugFieldTheme = debugCustomerFieldTheme;
}
