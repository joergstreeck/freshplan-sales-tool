/**
 * Field Theme System - Design Standard für FreshPlan
 * 
 * Definiert konsistente Feldgrößen für alle Formulare.
 * Sprint 2: Implementierung für CustomerOnboardingWizard
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/FIELD_THEME_SYSTEM_PROTOTYPE.md
 */

import type { GridSize } from '@mui/material';

export interface FieldSizeTheme {
  compact: GridSize;
  small: GridSize;
  medium: GridSize;
  large: GridSize;
  full: GridSize;
}

/**
 * VERBINDLICHER DESIGN-STANDARD FÜR FRESHFOODZ
 * 
 * Kategorien:
 * - compact: PLZ, Nummern, kurze IDs (2-3 Grid Units)
 * - small: Dropdowns, Status (3-4 Grid Units)
 * - medium: Standard Text, Telefon (4-6 Grid Units)
 * - large: E-Mail, Straße, Firma (8-10 Grid Units)
 * - full: Notizen, Beschreibungen (12 Grid Units)
 */
export const FIELD_THEME: FieldSizeTheme = {
  compact: { xs: 6, sm: 4, md: 2 },
  small: { xs: 12, sm: 6, md: 3 },
  medium: { xs: 12, sm: 6, md: 4 },
  large: { xs: 12, sm: 12, md: 8 },
  full: { xs: 12, sm: 12, md: 12 }
};

/**
 * Erweiterte Größen für spezielle Fälle (optional)
 * Wird in späteren Sprints erweitert
 */
export const FIELD_THEME_EXTENDED = {
  ...FIELD_THEME,
  xlarge: { xs: 12, sm: 12, md: 10 },
  tiny: { xs: 4, sm: 3, md: 1 }
};

/**
 * Type Guard für Theme-Kategorien
 */
export type FieldSizeKey = keyof FieldSizeTheme;

export function isValidFieldSize(size: string): size is FieldSizeKey {
  return size in FIELD_THEME;
}