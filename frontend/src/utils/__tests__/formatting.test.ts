/**
 * Formatting Utils Tests
 *
 * Tests for currency, number, percentage, and text formatting utilities.
 */

import { describe, it, expect } from 'vitest';
import {
  formatCurrency,
  formatPercentage,
  formatNumber,
  formatDays,
  formatBoolean,
} from '../formatting';

describe('Formatting Utils', () => {
  describe('formatCurrency', () => {
    it('should format whole numbers as EUR currency', () => {
      expect(formatCurrency(1000)).toBe('1.000\u00a0€');
      expect(formatCurrency(0)).toBe('0\u00a0€');
      expect(formatCurrency(42)).toBe('42\u00a0€');
    });

    it('should format decimal numbers with up to 2 decimals', () => {
      expect(formatCurrency(1234.56)).toBe('1.234,56\u00a0€');
      expect(formatCurrency(99.99)).toBe('99,99\u00a0€');
      expect(formatCurrency(0.01)).toBe('0,01\u00a0€');
    });

    it('should handle large numbers', () => {
      expect(formatCurrency(1000000)).toBe('1.000.000\u00a0€');
      expect(formatCurrency(1234567.89)).toBe('1.234.567,89\u00a0€');
    });

    it('should handle negative numbers', () => {
      expect(formatCurrency(-100)).toBe('-100\u00a0€');
      expect(formatCurrency(-1234.56)).toBe('-1.234,56\u00a0€');
    });

    it('should round to maximum 2 decimals', () => {
      expect(formatCurrency(12.999)).toBe('13\u00a0€');
      expect(formatCurrency(12.345)).toBe('12,35\u00a0€');
    });
  });

  describe('formatPercentage', () => {
    it('should format numbers as percentages with default 1 decimal', () => {
      expect(formatPercentage(50)).toBe('50,0\u00a0%');
      expect(formatPercentage(100)).toBe('100,0\u00a0%');
      expect(formatPercentage(0)).toBe('0,0\u00a0%');
    });

    it('should format with custom decimal places', () => {
      expect(formatPercentage(33.33, 2)).toBe('33,33\u00a0%');
      expect(formatPercentage(66.667, 3)).toBe('66,667\u00a0%');
      expect(formatPercentage(50, 0)).toBe('50\u00a0%');
    });

    it('should handle small percentages', () => {
      expect(formatPercentage(0.5, 1)).toBe('0,5\u00a0%');
      expect(formatPercentage(0.01, 2)).toBe('0,01\u00a0%');
    });

    it('should handle values over 100%', () => {
      expect(formatPercentage(150, 1)).toBe('150,0\u00a0%');
      expect(formatPercentage(250.5, 1)).toBe('250,5\u00a0%');
    });
  });

  describe('formatNumber', () => {
    it('should format numbers with thousand separators', () => {
      expect(formatNumber(1000)).toBe('1.000');
      expect(formatNumber(1000000)).toBe('1.000.000');
      expect(formatNumber(1234567)).toBe('1.234.567');
    });

    it('should handle small numbers without separators', () => {
      expect(formatNumber(0)).toBe('0');
      expect(formatNumber(42)).toBe('42');
      expect(formatNumber(999)).toBe('999');
    });

    it('should handle decimals with German formatting', () => {
      expect(formatNumber(1234.56)).toBe('1.234,56');
      expect(formatNumber(0.99)).toBe('0,99');
    });

    it('should handle negative numbers', () => {
      expect(formatNumber(-1000)).toBe('-1.000');
      expect(formatNumber(-123.45)).toBe('-123,45');
    });
  });

  describe('formatDays', () => {
    it('should format 0 days as "Sofort"', () => {
      expect(formatDays(0)).toBe('Sofort');
    });

    it('should format 1 day as "1 Tag"', () => {
      expect(formatDays(1)).toBe('1 Tag');
    });

    it('should format multiple days as "X Tage"', () => {
      expect(formatDays(2)).toBe('2 Tage');
      expect(formatDays(7)).toBe('7 Tage');
      expect(formatDays(30)).toBe('30 Tage');
      expect(formatDays(365)).toBe('365 Tage');
    });

    it('should handle negative days (edge case)', () => {
      expect(formatDays(-1)).toBe('-1 Tage');
      expect(formatDays(-5)).toBe('-5 Tage');
    });
  });

  describe('formatBoolean', () => {
    it('should format true as "Ja" by default', () => {
      expect(formatBoolean(true)).toBe('Ja');
    });

    it('should format false as "Nein" by default', () => {
      expect(formatBoolean(false)).toBe('Nein');
    });

    it('should use custom text for true values', () => {
      expect(formatBoolean(true, 'Aktiv', 'Inaktiv')).toBe('Aktiv');
      expect(formatBoolean(true, 'Yes', 'No')).toBe('Yes');
      expect(formatBoolean(true, 'On', 'Off')).toBe('On');
    });

    it('should use custom text for false values', () => {
      expect(formatBoolean(false, 'Aktiv', 'Inaktiv')).toBe('Inaktiv');
      expect(formatBoolean(false, 'Yes', 'No')).toBe('No');
      expect(formatBoolean(false, 'On', 'Off')).toBe('Off');
    });

    it('should handle empty strings as custom text', () => {
      expect(formatBoolean(true, '', 'No')).toBe('');
      expect(formatBoolean(false, 'Yes', '')).toBe('');
    });
  });
});
