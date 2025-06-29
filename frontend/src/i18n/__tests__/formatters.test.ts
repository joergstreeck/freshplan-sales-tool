import { describe, test, expect } from 'vitest';
import {
  formatCurrency,
  formatNumber,
  formatDate,
  formatDateTime,
  formatPercent,
} from '../formatters';

describe('i18n Formatters', () => {
  describe('formatCurrency', () => {
    test('formats currency in German locale', () => {
      const result = formatCurrency(1500, 'de');
      // Note: Intl.NumberFormat uses non-breaking space
      expect(result).toMatch(/1\.500,00\s*€/);
    });

    test('formats currency in English locale', () => {
      const result = formatCurrency(1500, 'en');
      expect(result).toBe('€1,500.00');
    });

    test('handles decimal values correctly', () => {
      expect(formatCurrency(1500.5, 'de')).toMatch(/1\.500,50\s*€/);
      expect(formatCurrency(1500.5, 'en')).toBe('€1,500.50');
    });

    test('handles large numbers', () => {
      expect(formatCurrency(1000000, 'de')).toMatch(/1\.000\.000,00\s*€/);
      expect(formatCurrency(1000000, 'en')).toBe('€1,000,000.00');
    });
  });

  describe('formatNumber', () => {
    test('formats numbers in German locale', () => {
      expect(formatNumber(1500, 'de')).toBe('1.500');
      expect(formatNumber(1500.5, 'de')).toBe('1.500,5');
    });

    test('formats numbers in English locale', () => {
      expect(formatNumber(1500, 'en')).toBe('1,500');
      expect(formatNumber(1500.5, 'en')).toBe('1,500.5');
    });
  });

  describe('formatDate', () => {
    test('formats date in German locale', () => {
      const date = new Date('2025-06-29');
      const result = formatDate(date, 'de');
      expect(result).toMatch(/29\.06\.2025/);
    });

    test('formats date in English locale', () => {
      const date = new Date('2025-06-29');
      const result = formatDate(date, 'en');
      expect(result).toMatch(/06\/29\/2025/);
    });
  });

  describe('formatDateTime', () => {
    test('formats date and time in German locale', () => {
      const date = new Date('2025-06-29T14:30:00');
      const result = formatDateTime(date, 'de');
      expect(result).toMatch(/29\.06\.2025/);
      expect(result).toMatch(/14:30/);
    });

    test('formats date and time in English locale', () => {
      const date = new Date('2025-06-29T14:30:00');
      const result = formatDateTime(date, 'en');
      expect(result).toMatch(/06\/29\/2025/);
      // English locale might use 12-hour format (2:30 PM) or 24-hour format
      expect(result).toMatch(/(14:30|2:30\s*PM)/);
    });
  });

  describe('formatPercent', () => {
    test('formats percentage in German locale', () => {
      expect(formatPercent(15, 'de')).toMatch(/15\s*%/);
      expect(formatPercent(15.5, 'de')).toMatch(/15,5\s*%/);
    });

    test('formats percentage in English locale', () => {
      expect(formatPercent(15, 'en')).toBe('15%');
      expect(formatPercent(15.5, 'en')).toBe('15.5%');
    });
  });
});
