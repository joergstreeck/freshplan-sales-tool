/**
 * Field Size Calculator - Intelligente Größenermittlung
 * 
 * Berechnet optimale Feldgrößen basierend auf:
 * 1. Expliziten sizeHints
 * 2. Field-Key-Mappings
 * 3. Field-Type
 * 4. MaxLength
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/FIELD_THEME_IMPLEMENTATION.md
 */

import type { FieldDefinition } from '../types/field.types';
import type { GridSize } from '@mui/material';
import { FIELD_THEME, FIELD_THEME_EXTENDED, type FieldSizeKey } from '../theme/fieldTheme';

/**
 * Sprint 2: Optimierte Mappings für CustomerOnboardingWizard
 * 
 * Diese Mappings sorgen für intuitive Feldgrößen basierend auf dem Feld-Key.
 * Wird in späteren Sprints für weitere Module erweitert.
 */
const CUSTOMER_FIELD_MAPPINGS: Record<string, FieldSizeKey> = {
  // Kompakte Felder (2-3 Grid)
  'customerNumber': 'medium',  // Von 3 auf 4, damit Zeile 1 besser balanciert ist
  'postalCode': 'compact',
  'plz': 'compact',
  'zipCode': 'compact',
  'houseNumber': 'compact',
  'hausnummer': 'compact',
  'numberOfLocations': 'compact',
  'numberOfEmployees': 'compact',
  
  // Kleine Felder (3-4 Grid)
  'salutation': 'small',
  'anrede': 'small',
  'legalForm': 'medium',  // Von 3 auf 4 für bessere Balance
  'industry': 'medium',    // Von 3 auf 4 
  'branche': 'medium',
  'chainCustomer': 'medium',  // Von 3 auf 4
  'contractType': 'small',
  
  // Mittlere Felder (4-6 Grid)
  'firstName': 'medium',
  'vorname': 'medium',
  'lastName': 'medium',
  'nachname': 'medium',
  'city': 'large',  // Von 4 auf 8, damit es mit PLZ eine volle Zeile ergibt
  'stadt': 'large',
  'phone': 'medium',
  'telefon': 'medium',
  'mobile': 'medium',
  'fax': 'medium',
  'country': 'medium',
  'land': 'medium',
  
  // Große Felder (8-10 Grid)
  'companyName': 'large',
  'firmenname': 'large',
  'email': 'large',
  'emailAddress': 'large',
  'street': 'large',
  'strasse': 'large',
  'address': 'large',
  'website': 'large',
  'webseite': 'large',
  'contactPerson': 'large',
  'ansprechpartner': 'large',
  
  // Volle Breite (12 Grid)
  'notes': 'full',
  'notizen': 'full',
  'description': 'full',
  'beschreibung': 'full',
  'comment': 'full',
  'kommentar': 'full',
  'internalNotes': 'full',
  'specialRequirements': 'full'
};

/**
 * Ermittelt die optimale Feldgröße basierend auf verschiedenen Kriterien
 * 
 * @param field - Die Felddefinition aus dem Field Catalog
 * @returns GridSize-Objekt mit responsive Breakpoints
 */
export function getFieldSize(field: FieldDefinition): GridSize {
  // 1. Expliziter sizeHint hat IMMER Vorrang (manuelles Override)
  if (field.sizeHint) {
    // Prüfe zuerst Standard-Theme
    if (field.sizeHint in FIELD_THEME) {
      return FIELD_THEME[field.sizeHint as FieldSizeKey];
    }
    // Dann erweiterte Größen
    if (field.sizeHint in FIELD_THEME_EXTENDED) {
      return FIELD_THEME_EXTENDED[field.sizeHint as keyof typeof FIELD_THEME_EXTENDED];
    }
  }
  
  // 2. Für Dropdowns: Dynamische Berechnung hat Vorrang
  if (field.fieldType === 'select' || field.fieldType === 'dropdown') {
    // Springe direkt zur dynamischen Berechnung für Dropdowns
    // und überspringe CUSTOMER_FIELD_MAPPINGS
  } else {
    // 3. Field-Key-basierte Mappings für alle anderen Feldtypen
    if (field.key in CUSTOMER_FIELD_MAPPINGS) {
      return FIELD_THEME[CUSTOMER_FIELD_MAPPINGS[field.key]];
    }
  }
  
  // 4. Auto-Berechnung basierend auf fieldType
  switch (field.fieldType) {
    case 'number':
      return FIELD_THEME.compact;
      
    case 'select':
    case 'dropdown':
      // Dynamische Größenberechnung basierend auf längster Option
      // Diese hat VORRANG vor expliziter size Property
      if (field.options && field.options.length > 0) {
        const longestLabel = Math.max(
          ...field.options.map(opt => opt.label.length),
          field.placeholder?.length || 0
        );
        
        // Größenberechnung basierend auf Textlänge
        // Angepasste Schwellenwerte für bessere Darstellung
        if (longestLabel <= 10) return FIELD_THEME.compact;    // 2-3 Grid
        if (longestLabel <= 20) return FIELD_THEME.small;      // 3-4 Grid
        if (longestLabel <= 35) return FIELD_THEME.medium;     // 4-6 Grid
        if (longestLabel <= 50) return FIELD_THEME.large;      // 8-10 Grid
        return FIELD_THEME.full;                                // 12 Grid
      }
      
      // Fallback: Berücksichtige explizite size Property
      if (field.size) {
        const sizeMapping: Record<string, FieldSizeKey> = {
          'compact': 'compact',
          'small': 'small',
          'medium': 'medium',
          'large': 'large',
          'xlarge': 'full'
        };
        if (field.size in sizeMapping) {
          return FIELD_THEME[sizeMapping[field.size]];
        }
      }
      
      // Letzter Fallback: Große Auswahllisten brauchen mehr Platz
      return field.options && field.options.length > 10 
        ? FIELD_THEME.medium 
        : FIELD_THEME.small;
        
    case 'email':
      return FIELD_THEME.large;
      
    case 'textarea':
    case 'multiline':
      return FIELD_THEME.full;
      
    case 'checkbox':
      return FIELD_THEME.compact;
      
    case 'radio':
      return field.options && field.options.length > 3
        ? FIELD_THEME.medium
        : FIELD_THEME.small;
      
    case 'date':
    case 'datetime':
      return FIELD_THEME.small;
      
    case 'text':
    default:
      // 4. Fallback: Basierend auf maxLength
      if (!field.maxLength) {
        return FIELD_THEME.medium; // Default für Text ohne Längenbeschränkung
      }
      
      if (field.maxLength <= 5) return FIELD_THEME.compact;
      if (field.maxLength <= 20) return FIELD_THEME.small;
      if (field.maxLength <= 50) return FIELD_THEME.medium;
      return FIELD_THEME.large;
  }
}

/**
 * Helper für Field Groups - berechnet optimale Zeilen
 * Sorgt dafür, dass Zeilen immer auf 12 Grid Units summieren
 * 
 * @param fields - Array von Felddefinitionen
 * @returns Array von Zeilen mit optimaler Feldverteilung
 */
export function calculateOptimalRows(fields: FieldDefinition[]): FieldDefinition[][] {
  const rows: FieldDefinition[][] = [];
  let currentRow: FieldDefinition[] = [];
  let currentRowSize = 0;
  
  fields.forEach(field => {
    const size = getFieldSize(field);
    const fieldSize = size.md || 12; // Desktop-Größe als Basis
    
    // Neue Zeile beginnen, wenn 12 überschritten würde
    if (currentRowSize + fieldSize > 12) {
      if (currentRow.length > 0) {
        rows.push(currentRow);
      }
      currentRow = [field];
      currentRowSize = fieldSize;
    } else {
      currentRow.push(field);
      currentRowSize += fieldSize;
    }
  });
  
  // Letzte Zeile hinzufügen
  if (currentRow.length > 0) {
    rows.push(currentRow);
  }
  
  return rows;
}

/**
 * Debug-Helper: Gibt eine visuelle Darstellung der Zeilen aus
 * Nützlich während der Entwicklung
 */
export function debugFieldLayout(fields: FieldDefinition[]): string {
  const rows = calculateOptimalRows(fields);
  let output = 'Field Layout Debug:\n';
  
  rows.forEach((row, index) => {
    output += `Row ${index + 1}: `;
    let totalSize = 0;
    
    row.forEach(field => {
      const size = getFieldSize(field);
      const fieldSize = size.md || 12;
      totalSize += fieldSize;
      output += `[${field.key}:${fieldSize}] `;
    });
    
    output += `= ${totalSize}/12 ${totalSize === 12 ? '✅' : '⚠️'}\n`;
  });
  
  return output;
}

/**
 * Debug-Helper für Dropdown-Größenberechnung
 * Zeigt die berechneten Größen für alle Dropdown-Felder
 */
export function debugDropdownSizes(fields: FieldDefinition[]): void {
  console.log('=== Dropdown Size Debug ===');
  
  fields.forEach(field => {
    if (field.fieldType === 'select' || field.fieldType === 'dropdown') {
      const size = getFieldSize(field);
      const gridSize = size.md || 12;
      
      // Längste Option finden
      let longestLabel = 0;
      if (field.options) {
        longestLabel = Math.max(
          ...field.options.map(opt => opt.label.length),
          field.placeholder?.length || 0
        );
      }
      
      console.log(`Field: ${field.key}`);
      console.log(`  - Label: "${field.label}"`);
      console.log(`  - Longest option: ${longestLabel} chars`);
      console.log(`  - Calculated grid size: ${gridSize}`);
      console.log(`  - Has explicit size: ${field.size || 'no'}`);
      console.log(`  - In CUSTOMER_FIELD_MAPPINGS: ${field.key in CUSTOMER_FIELD_MAPPINGS ? 'yes' : 'no'}`);
      
      if (field.options && field.options.length > 0) {
        console.log(`  - Options:`);
        field.options.forEach(opt => {
          console.log(`    * "${opt.label}" (${opt.label.length} chars)`);
        });
      }
      console.log('');
    }
  });
}