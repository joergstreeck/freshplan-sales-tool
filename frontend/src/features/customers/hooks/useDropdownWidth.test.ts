/**
 * Tests für useDropdownWidth Hook
 */

import { describe, it, expect } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useDropdownWidth, calculateDropdownWidth } from './useDropdownWidth';
import type { FieldOption } from '../types/field.types';

describe('useDropdownWidth', () => {
  const sampleOptions: FieldOption[] = [
    { value: 'short', label: 'Kurz' },
    { value: 'medium', label: 'Mittel - offen für Alternativen' },
    { value: 'long', label: 'Sehr langer Text der die Breite bestimmt' },
  ];

  describe('Hook Funktionalität', () => {
    it('berechnet Breite basierend auf längster Option', () => {
      const { result } = renderHook(() => useDropdownWidth({ options: sampleOptions }));

      // Längste Option: "Sehr langer Text der die Breite bestimmt" (40 Zeichen)
      // 40 * 8 + 80 = 400px
      expect(result.current.width).toBe(400);
      expect(result.current.cssVar).toBe('400px');
    });

    it('berücksichtigt Placeholder-Text', () => {
      const shortOptions: FieldOption[] = [
        { value: '1', label: 'A' },
        { value: '2', label: 'B' },
      ];

      const { result } = renderHook(() =>
        useDropdownWidth({
          options: shortOptions,
          placeholder: 'Sehr langer Placeholder Text hier',
        })
      );

      // Placeholder ist länger als Optionen
      // "Sehr langer Placeholder Text hier" (33 Zeichen)
      // 33 * 8 + 80 = 344px
      expect(result.current.width).toBe(344);
    });

    it('verwendet Minimum-Breite bei leeren Optionen', () => {
      const { result } = renderHook(() => useDropdownWidth({ options: [] }));

      expect(result.current.width).toBe(200); // Default minimum
    });

    it('begrenzt auf Maximum-Breite', () => {
      const veryLongOptions: FieldOption[] = [
        {
          value: 'very_long',
          label:
            'Extrem langer Text der definitiv über die maximale Breite hinausgeht und gekürzt werden sollte',
        },
      ];

      const { result } = renderHook(() =>
        useDropdownWidth({ options: veryLongOptions, maxWidth: 300 })
      );

      expect(result.current.width).toBe(300); // Begrenzt auf maxWidth
    });

    it('verwendet konfigurierbare Parameter', () => {
      const { result } = renderHook(() =>
        useDropdownWidth({
          options: [{ value: '1', label: 'Test' }], // 4 Zeichen
          charWidth: 10, // Breitere Zeichen
          padding: 100, // Mehr Padding
          minWidth: 150,
          maxWidth: 600,
        })
      );

      // 15 * 10 + 100 = 250px (minimum von 15 Zeichen wird verwendet)
      expect(result.current.width).toBe(250);
    });
  });

  describe('CSS Style Object', () => {
    it('erstellt korrektes Style-Objekt', () => {
      const { result } = renderHook(() =>
        useDropdownWidth({
          options: [{ value: '1', label: 'Medium Text' }],
          minWidth: 180,
          maxWidth: 400,
        })
      );

      expect(result.current.style).toEqual({
        '--dropdown-width': `${result.current.width}px`,
        minWidth: '180px',
        maxWidth: '400px',
      });
    });
  });

  describe('calculateDropdownWidth Utility', () => {
    it('berechnet Breite ohne React Hook', () => {
      const width = calculateDropdownWidth(sampleOptions);

      // Gleiche Berechnung wie oben: 40 * 8 + 80 = 400px
      expect(width).toBe(400);
    });

    it('funktioniert mit leeren Optionen', () => {
      const width = calculateDropdownWidth([]);
      expect(width).toBe(200); // Default minimum
    });

    it('verwendet Custom-Konfiguration', () => {
      const width = calculateDropdownWidth([{ value: '1', label: 'Test' }], 'Placeholder', {
        charWidth: 12,
        padding: 60,
        minWidth: 250,
        maxWidth: 450,
      });

      // "Placeholder" (11 Zeichen) ist länger als "Test" (4 Zeichen)
      // 11 * 12 + 60 = 192px, aber minimum ist 250px
      expect(width).toBe(250);
    });
  });

  describe('Edge Cases', () => {
    it('behandelt undefined options', () => {
      const { result } = renderHook(() => useDropdownWidth({ options: undefined }));

      expect(result.current.width).toBe(200);
    });

    it('behandelt leeren Placeholder', () => {
      const { result } = renderHook(() =>
        useDropdownWidth({
          options: [{ value: '1', label: 'Test' }],
          placeholder: '',
        })
      );

      // Verwendet Minimum von 15 Zeichen für "Bitte wählen..."
      // 15 * 8 + 80 = 200px (minimum wird verwendet)
      expect(result.current.width).toBe(200);
    });

    it('reagiert auf Options-Änderungen', () => {
      const { result, rerender } = renderHook(props => useDropdownWidth(props), {
        initialProps: {
          options: [{ value: '1', label: 'Kurz' }],
        },
      });

      const initialWidth = result.current.width;

      // Ändere zu längeren Optionen
      rerender({
        options: [{ value: '1', label: 'Sehr viel längerer Text' }],
      });

      expect(result.current.width).toBeGreaterThan(initialWidth);
    });
  });

  describe('Sprint 2 spezifische Fälle', () => {
    it('berechnet korrekte Breiten für Filialstruktur-Felder', () => {
      const testCases = [
        {
          field: 'financingType',
          options: [
            { value: 'private', label: 'Privatwirtschaftlich' },
            { value: 'public', label: 'Öffentlich finanziert' },
            { value: 'mixed', label: 'Mischfinanzierung' },
          ],
          expectedMinWidth: 248, // "Öffentlich finanziert" = 21 * 8 + 80 = 248
        },
        {
          field: 'switchWillingness',
          options: [
            { value: 'high', label: 'Hoch - aktiv auf der Suche' },
            { value: 'medium', label: 'Mittel - offen für Alternativen' },
            { value: 'low', label: 'Niedrig - zufrieden' },
          ],
          expectedMinWidth: 320, // "Mittel - offen für Alternativen" = 32 * 8 + 80 = 336
        },
        {
          field: 'decisionTimeline',
          options: [
            { value: 'immediate', label: 'Sofortige Entscheidung möglich' },
            { value: '1_month', label: 'Innerhalb 1 Monat' },
          ],
          expectedMinWidth: 320, // "Sofortige Entscheidung möglich" = 30 * 8 + 80 = 320
        },
      ];

      testCases.forEach(({ options, expectedMinWidth }) => {
        const width = calculateDropdownWidth(options);
        expect(width).toBeGreaterThanOrEqual(expectedMinWidth);
      });
    });
  });
});
