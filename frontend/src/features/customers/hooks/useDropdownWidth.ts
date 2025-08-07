/**
 * useDropdownWidth Hook
 *
 * Berechnet die optimale Breite für Dropdown-Felder basierend auf der längsten Option.
 * Teil der Dropdown Auto-Width Theme-Integration.
 *
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DROPDOWN_AUTO_WIDTH_PLAN.md
 */

import { useMemo } from 'react';
import type { FieldOption } from '../types/field.types';

interface UseDropdownWidthProps {
  /** Dropdown-Optionen */
  options?: FieldOption[];
  /** Placeholder-Text */
  placeholder?: string;
  /** Minimum-Breite in Pixel (default: 200) */
  minWidth?: number;
  /** Maximum-Breite in Pixel (default: 500) */
  maxWidth?: number;
  /** Pixel pro Zeichen (default: 8) */
  charWidth?: number;
  /** Zusätzliches Padding für Icon/Arrow (default: 80) */
  padding?: number;
}

interface UseDropdownWidthResult {
  /** Berechnete Breite in Pixel */
  width: number;
  /** CSS-Variable-Wert */
  cssVar: string;
  /** Style-Objekt für direkte Anwendung */
  style: {
    '--dropdown-width': string;
    minWidth: string;
    maxWidth: string;
  };
}

/**
 * Hook zur Berechnung der optimalen Dropdown-Breite
 *
 * @example
 * ```tsx
 * const { width, style } = useDropdownWidth({
 *   options: field.options,
 *   placeholder: field.placeholder
 * });
 *
 * return <FormControl sx={style}>...</FormControl>
 * ```
 */
export const useDropdownWidth = ({
  options = [],
  placeholder = '',
  minWidth = 200,
  maxWidth = 500,
  charWidth = 8,
  padding = 80,
}: UseDropdownWidthProps): UseDropdownWidthResult => {
  const calculatedWidth = useMemo(() => {
    // Keine Optionen = Minimum-Breite
    if (!options || options.length === 0) {
      return minWidth;
    }

    // Finde längsten Text (Optionen + Placeholder)
    const longestLabel = Math.max(
      ...options.map(opt => opt.label.length),
      placeholder.length,
      15 // Minimum für "Bitte wählen..."
    );

    // Berechne Breite: Zeichen * Pixel + Padding
    const width = longestLabel * charWidth + padding;

    // Begrenze auf Min/Max
    return Math.min(Math.max(width, minWidth), maxWidth);
  }, [options, placeholder, minWidth, maxWidth, charWidth, padding]);

  return {
    width: calculatedWidth,
    cssVar: `${calculatedWidth}px`,
    style: {
      '--dropdown-width': `${calculatedWidth}px`,
      minWidth: `${minWidth}px`,
      maxWidth: `${maxWidth}px`,
    },
  };
};

/**
 * Utility-Funktion für statische Berechnung (ohne Hook)
 * Nützlich für Server-Side Rendering oder Tests
 */
export const calculateDropdownWidth = (
  options: FieldOption[],
  placeholder = '',
  config = {}
): number => {
  const {
    minWidth = 200,
    maxWidth = 500,
    charWidth = 8,
    padding = 80,
  } = config as Partial<UseDropdownWidthProps>;

  if (!options || options.length === 0) {
    return minWidth;
  }

  const longestLabel = Math.max(...options.map(opt => opt.label.length), placeholder.length, 15);

  const width = longestLabel * charWidth + padding;
  return Math.min(Math.max(width, minWidth), maxWidth);
};
