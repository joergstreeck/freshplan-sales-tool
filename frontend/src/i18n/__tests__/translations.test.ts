import { describe, test, expect } from 'vitest';
import deCommon from '../locales/de/common.json';
import deCalculator from '../locales/de/calculator.json';
import deCustomers from '../locales/de/customers.json';
import deNavigation from '../locales/de/navigation.json';
import deErrors from '../locales/de/errors.json';

import enCommon from '../locales/en/common.json';
import enCalculator from '../locales/en/calculator.json';
import enCustomers from '../locales/en/customers.json';
import enNavigation from '../locales/en/navigation.json';
import enErrors from '../locales/en/errors.json';

describe('Translation Files', () => {
  const getAllKeys = (obj: Record<string, unknown>, prefix = ''): string[] => {
    let keys: string[] = [];

    for (const key in obj) {
      const fullKey = prefix ? `${prefix}.${key}` : key;

      if (typeof obj[key] === 'object' && obj[key] !== null) {
        keys = keys.concat(getAllKeys(obj[key], fullKey));
      } else {
        keys.push(fullKey);
      }
    }

    return keys;
  };

  describe('Common translations', () => {
    test('all German keys have English translations', () => {
      const deKeys = getAllKeys(deCommon).sort();
      const enKeys = getAllKeys(enCommon).sort();

      expect(enKeys).toEqual(deKeys);
    });

    test('no empty translations', () => {
      const checkEmpty = (obj: Record<string, unknown>, path = ''): void => {
        for (const key in obj) {
          const fullPath = path ? `${path}.${key}` : key;
          if (typeof obj[key] === 'string') {
            expect(obj[key], `Empty translation at ${fullPath}`).not.toBe('');
          } else if (typeof obj[key] === 'object') {
            checkEmpty(obj[key], fullPath);
          }
        }
      };

      checkEmpty(deCommon);
      checkEmpty(enCommon);
    });
  });

  describe('Calculator translations', () => {
    test('all German keys have English translations', () => {
      const deKeys = getAllKeys(deCalculator).sort();
      const enKeys = getAllKeys(enCalculator).sort();

      expect(enKeys).toEqual(deKeys);
    });
  });

  describe('Customers translations', () => {
    test('all German keys have English translations', () => {
      const deKeys = getAllKeys(deCustomers).sort();
      const enKeys = getAllKeys(enCustomers).sort();

      expect(enKeys).toEqual(deKeys);
    });
  });

  describe('Navigation translations', () => {
    test('all German keys have English translations', () => {
      const deKeys = getAllKeys(deNavigation).sort();
      const enKeys = getAllKeys(enNavigation).sort();

      expect(enKeys).toEqual(deKeys);
    });
  });

  describe('Error translations', () => {
    test('all German keys have English translations', () => {
      const deKeys = getAllKeys(deErrors).sort();
      const enKeys = getAllKeys(enErrors).sort();

      expect(enKeys).toEqual(deKeys);
    });
  });

  describe('Interpolation variables', () => {
    test('same interpolation variables in both languages', () => {
      const findInterpolations = (str: string): string[] => {
        const matches = str.match(/{{(\w+)}}/g);
        return matches ? matches.sort() : [];
      };

      const checkInterpolations = (
        deObj: Record<string, unknown>,
        enObj: Record<string, unknown>,
        path = ''
      ): void => {
        for (const key in deObj) {
          const fullPath = path ? `${path}.${key}` : key;

          if (typeof deObj[key] === 'string' && typeof enObj[key] === 'string') {
            const deVars = findInterpolations(deObj[key]);
            const enVars = findInterpolations(enObj[key]);

            expect(enVars, `Different interpolation variables at ${fullPath}`).toEqual(deVars);
          } else if (typeof deObj[key] === 'object' && typeof enObj[key] === 'object') {
            checkInterpolations(deObj[key], enObj[key], fullPath);
          }
        }
      };

      checkInterpolations(deCommon, enCommon);
      checkInterpolations(deCalculator, enCalculator);
    });
  });
});
