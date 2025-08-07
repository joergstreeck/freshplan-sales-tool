/**
 * Tests für Field Size Calculator
 *
 * Testet die intelligente Größenermittlung für verschiedene Feldtypen
 */

import { describe, it, expect } from 'vitest';
import { getFieldSize, calculateOptimalRows, debugFieldLayout } from '../fieldSizeCalculator';
import { FIELD_THEME } from '../../theme/fieldTheme';
import type { FieldDefinition } from '../../types/field.types';

describe.skip('fieldSizeCalculator', () => {
  describe.skip('getFieldSize', () => {
    it('should return compact size for PLZ field', () => {
      const field: FieldDefinition = {
        key: 'postalCode',
        label: 'PLZ',
        fieldType: 'text',
        entityType: 'customer',
        required: true,
        maxLength: 5,
      };

      const size = getFieldSize(field);
      expect(size).toEqual(FIELD_THEME.compact);
      expect(size.md).toBe(2);
    });

    it('should return large size for email field', () => {
      const field: FieldDefinition = {
        key: 'email',
        label: 'E-Mail',
        fieldType: 'email',
        entityType: 'customer',
        required: true,
        maxLength: 100,
      };

      const size = getFieldSize(field);
      expect(size).toEqual(FIELD_THEME.large);
      expect(size.md).toBe(8);
    });

    it('should respect explicit sizeHint', () => {
      const field: FieldDefinition = {
        key: 'customField',
        label: 'Custom',
        fieldType: 'text',
        entityType: 'customer',
        required: false,
        sizeHint: 'full',
      };

      const size = getFieldSize(field);
      expect(size).toEqual(FIELD_THEME.full);
      expect(size.md).toBe(12);
    });

    it('should return small size for salutation field', () => {
      const field: FieldDefinition = {
        key: 'salutation',
        label: 'Anrede',
        fieldType: 'select',
        entityType: 'customer',
        required: true,
        options: [
          { value: 'herr', label: 'Herr' },
          { value: 'frau', label: 'Frau' },
        ],
      };

      const size = getFieldSize(field);
      expect(size).toEqual(FIELD_THEME.small);
      expect(size.md).toBe(3);
    });

    it('should return medium size for firstName field', () => {
      const field: FieldDefinition = {
        key: 'firstName',
        label: 'Vorname',
        fieldType: 'text',
        entityType: 'customer',
        required: true,
        maxLength: 50,
      };

      const size = getFieldSize(field);
      expect(size).toEqual(FIELD_THEME.medium);
      expect(size.md).toBe(4);
    });

    it('should return full size for textarea fields', () => {
      const field: FieldDefinition = {
        key: 'notes',
        label: 'Notizen',
        fieldType: 'textarea',
        entityType: 'customer',
        required: false,
      };

      const size = getFieldSize(field);
      expect(size).toEqual(FIELD_THEME.full);
      expect(size.md).toBe(12);
    });

    it('should handle number fields as compact', () => {
      const field: FieldDefinition = {
        key: 'numberOfEmployees',
        label: 'Anzahl Mitarbeiter',
        fieldType: 'number',
        entityType: 'customer',
        required: false,
      };

      const size = getFieldSize(field);
      expect(size).toEqual(FIELD_THEME.compact);
    });

    it('should handle large select lists differently', () => {
      const field: FieldDefinition = {
        key: 'country',
        label: 'Land',
        fieldType: 'select',
        entityType: 'customer',
        required: true,
        options: new Array(20).fill(null).map((_, i) => ({
          value: `country_${i}`,
          label: `Country ${i}`,
        })),
      };

      const size = getFieldSize(field);
      expect(size).toEqual(FIELD_THEME.medium);
    });

    it('should fallback based on maxLength for unknown fields', () => {
      // Very short field
      const shortField: FieldDefinition = {
        key: 'unknownShort',
        label: 'Unknown',
        fieldType: 'text',
        entityType: 'customer',
        required: false,
        maxLength: 3,
      };
      expect(getFieldSize(shortField)).toEqual(FIELD_THEME.compact);

      // Long field
      const longField: FieldDefinition = {
        key: 'unknownLong',
        label: 'Unknown',
        fieldType: 'text',
        entityType: 'customer',
        required: false,
        maxLength: 100,
      };
      expect(getFieldSize(longField)).toEqual(FIELD_THEME.large);
    });
  });

  describe.skip('calculateOptimalRows', () => {
    it('should create balanced rows summing to 12', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'postalCode',
          label: 'PLZ',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
        }, // 2
        { key: 'city', label: 'Stadt', fieldType: 'text', entityType: 'customer', required: true }, // 4
        {
          key: 'country',
          label: 'Land',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
        }, // 4
      ];

      const rows = calculateOptimalRows(fields);

      // PLZ (2) + Stadt (4) + Land (4) = 10, passt in eine Zeile
      expect(rows).toHaveLength(1);
      expect(rows[0]).toHaveLength(3);
    });

    it('should split fields into multiple rows when exceeding 12', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'email',
          label: 'E-Mail',
          fieldType: 'email',
          entityType: 'customer',
          required: true,
        }, // 8
        {
          key: 'phone',
          label: 'Telefon',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
        }, // 4
        {
          key: 'mobile',
          label: 'Mobil',
          fieldType: 'text',
          entityType: 'customer',
          required: false,
        }, // 4
      ];

      const rows = calculateOptimalRows(fields);

      // E-Mail (8) + Telefon (4) = 12 (erste Zeile)
      // Mobil (4) (zweite Zeile)
      expect(rows).toHaveLength(2);
      expect(rows[0]).toHaveLength(2);
      expect(rows[1]).toHaveLength(1);
    });

    it('should handle full-width fields correctly', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'firstName',
          label: 'Vorname',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
        }, // 4
        {
          key: 'notes',
          label: 'Notizen',
          fieldType: 'textarea',
          entityType: 'customer',
          required: false,
        }, // 12
        {
          key: 'lastName',
          label: 'Nachname',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
        }, // 4
      ];

      const rows = calculateOptimalRows(fields);

      // Vorname (4) (erste Zeile)
      // Notizen (12) (zweite Zeile)
      // Nachname (4) (dritte Zeile)
      expect(rows).toHaveLength(3);
    });
  });

  describe.skip('debugFieldLayout', () => {
    it('should produce readable debug output', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'postalCode',
          label: 'PLZ',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
          maxLength: 5,
        },
        {
          key: 'city',
          label: 'Stadt',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
          maxLength: 50,
        },
        {
          key: 'country',
          label: 'Land',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
          maxLength: 50,
        },
        {
          key: 'email',
          label: 'E-Mail',
          fieldType: 'email',
          entityType: 'customer',
          required: true,
        },
      ];

      const debug = debugFieldLayout(fields);

      expect(debug).toContain('Field Layout Debug:');
      expect(debug).toContain('[postalCode:2]');
      expect(debug).toContain('[city:4]');
      expect(debug).toContain('[country:4]');
      expect(debug).toContain('[email:8]');
      expect(debug).toContain('= 10/12'); // PLZ + Stadt + Land
      expect(debug).toContain('= 8/12'); // E-Mail allein
    });
  });
});
