/**
 * Formatters Tests
 *
 * Tests for date/time/currency formatting utilities.
 * Different from formatting.ts - focuses on date/time and abbreviated numbers.
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import {
  formatCurrency,
  formatDate,
  formatDateTime,
  daysSince,
  formatPercent,
  formatNumber,
} from '../formatters';

describe('Formatters', () => {
  // Save original Date for restoration
  let originalDate: DateConstructor;

  beforeEach(() => {
    originalDate = global.Date;
  });

  afterEach(() => {
    global.Date = originalDate;
  });

  describe('formatCurrency', () => {
    it('should format positive numbers as EUR currency without decimals', () => {
      expect(formatCurrency(1000)).toBe('1.000\u00a0€');
      expect(formatCurrency(42)).toBe('42\u00a0€');
      expect(formatCurrency(1234567)).toBe('1.234.567\u00a0€');
    });

    it('should format zero as currency', () => {
      expect(formatCurrency(0)).toBe('0\u00a0€');
    });

    it('should format negative numbers as currency', () => {
      expect(formatCurrency(-100)).toBe('-100\u00a0€');
      expect(formatCurrency(-1234)).toBe('-1.234\u00a0€');
    });

    it('should return "-" for undefined values', () => {
      expect(formatCurrency(undefined)).toBe('-');
    });

    it('should return "-" for null values', () => {
      expect(formatCurrency(null)).toBe('-');
    });

    it('should round decimal values to whole numbers', () => {
      expect(formatCurrency(123.99)).toBe('124\u00a0€');
      expect(formatCurrency(123.01)).toBe('123\u00a0€');
      expect(formatCurrency(123.5)).toBe('124\u00a0€');
    });
  });

  describe('formatDate', () => {
    it('should format Date objects as DD.MM.YYYY', () => {
      const date = new Date('2025-03-15T10:30:00Z');
      const formatted = formatDate(date);
      expect(formatted).toMatch(/^\d{2}\.\d{2}\.\d{4}$/);
      expect(formatted).toBe('15.03.2025');
    });

    it('should format ISO date strings as DD.MM.YYYY', () => {
      expect(formatDate('2025-01-01T12:00:00Z')).toBe('01.01.2025');
      // Use midday to avoid timezone issues that could shift to next/prev day
      expect(formatDate('2024-12-31T12:00:00Z')).toBe('31.12.2024');
    });

    it('should return "-" for undefined', () => {
      expect(formatDate(undefined)).toBe('-');
    });

    it('should return "-" for null', () => {
      expect(formatDate(null)).toBe('-');
    });

    it('should return "-" for invalid date strings', () => {
      expect(formatDate('not-a-date')).toBe('-');
      expect(formatDate('2025-99-99')).toBe('-');
      expect(formatDate('')).toBe('-');
    });

    it('should handle different valid date formats', () => {
      expect(formatDate('2025-06-15')).toBe('15.06.2025');
      expect(formatDate(new Date(2025, 5, 15))).toBe('15.06.2025'); // Month is 0-indexed
    });
  });

  describe('formatDateTime', () => {
    it('should format Date objects with time as DD.MM.YYYY, HH:MM', () => {
      const date = new Date('2025-03-15T14:30:00Z');
      const formatted = formatDateTime(date);
      expect(formatted).toMatch(/^\d{2}\.\d{2}\.\d{4},\s\d{2}:\d{2}$/);
    });

    it('should format ISO date strings with time', () => {
      const formatted = formatDateTime('2025-01-01T12:00:00Z');
      expect(formatted).toMatch(/^\d{2}\.\d{2}\.\d{4},\s\d{2}:\d{2}$/);
    });

    it('should handle midnight (00:00)', () => {
      const formatted = formatDateTime('2025-01-01T00:00:00Z');
      expect(formatted).toMatch(/^\d{2}\.\d{2}\.\d{4},\s\d{2}:\d{2}$/);
    });

    it('should handle end of day (23:59)', () => {
      const formatted = formatDateTime('2025-01-01T23:59:00Z');
      expect(formatted).toMatch(/^\d{2}\.\d{2}\.\d{4},\s\d{2}:\d{2}$/);
    });
  });

  describe('daysSince', () => {
    it('should calculate days since a past date', () => {
      // Mock current date to 2025-10-05 12:00:00
      const mockNow = new Date('2025-10-05T12:00:00Z');
      vi.setSystemTime(mockNow);

      const pastDate = new Date('2025-10-01T12:00:00Z');
      expect(daysSince(pastDate)).toBe(4);
    });

    it('should calculate days for same day as 1 (ceil behavior)', () => {
      const mockNow = new Date('2025-10-05T12:00:00Z');
      vi.setSystemTime(mockNow);

      const sameDay = new Date('2025-10-05T14:00:00Z');
      expect(daysSince(sameDay)).toBe(1); // Math.ceil of fractional day
    });

    it('should handle dates one day apart', () => {
      const mockNow = new Date('2025-10-05T12:00:00Z');
      vi.setSystemTime(mockNow);

      const yesterday = new Date('2025-10-04T12:00:00Z');
      expect(daysSince(yesterday)).toBe(1);
    });

    it('should handle dates exactly 7 days apart', () => {
      const mockNow = new Date('2025-10-05T12:00:00Z');
      vi.setSystemTime(mockNow);

      const weekAgo = new Date('2025-09-28T12:00:00Z');
      expect(daysSince(weekAgo)).toBe(7);
    });

    it('should handle dates 30 days apart', () => {
      const mockNow = new Date('2025-10-05T12:00:00Z');
      vi.setSystemTime(mockNow);

      const monthAgo = new Date('2025-09-05T12:00:00Z');
      expect(daysSince(monthAgo)).toBe(30);
    });

    it('should use Math.abs for future dates (positive result)', () => {
      const mockNow = new Date('2025-10-05T12:00:00Z');
      vi.setSystemTime(mockNow);

      const futureDate = new Date('2025-10-10T12:00:00Z');
      expect(daysSince(futureDate)).toBe(5); // abs() makes it positive
    });
  });

  describe('formatPercent', () => {
    it('should format whole numbers as percentage', () => {
      expect(formatPercent(50)).toBe('50%');
      expect(formatPercent(100)).toBe('100%');
      expect(formatPercent(0)).toBe('0%');
    });

    it('should format decimal numbers as percentage', () => {
      expect(formatPercent(33.5)).toBe('33.5%');
      expect(formatPercent(66.67)).toBe('66.67%');
      expect(formatPercent(0.5)).toBe('0.5%');
    });

    it('should handle values over 100%', () => {
      expect(formatPercent(150)).toBe('150%');
      expect(formatPercent(999)).toBe('999%');
    });

    it('should handle negative percentages', () => {
      expect(formatPercent(-10)).toBe('-10%');
      expect(formatPercent(-50.5)).toBe('-50.5%');
    });
  });

  describe('formatNumber', () => {
    it('should format numbers under 1000 without abbreviation', () => {
      expect(formatNumber(0)).toBe('0');
      expect(formatNumber(42)).toBe('42');
      expect(formatNumber(999)).toBe('999');
    });

    it('should format thousands with K abbreviation', () => {
      expect(formatNumber(1000)).toBe('1.0K');
      expect(formatNumber(1500)).toBe('1.5K');
      expect(formatNumber(2000)).toBe('2.0K');
      expect(formatNumber(999000)).toBe('999.0K');
    });

    it('should format millions with M abbreviation', () => {
      expect(formatNumber(1000000)).toBe('1.0M');
      expect(formatNumber(1500000)).toBe('1.5M');
      expect(formatNumber(2000000)).toBe('2.0M');
      expect(formatNumber(999999999)).toBe('1000.0M');
    });

    it('should round to 1 decimal place', () => {
      expect(formatNumber(1234)).toBe('1.2K'); // 1.234 → 1.2
      expect(formatNumber(1567)).toBe('1.6K'); // 1.567 → 1.6
      expect(formatNumber(1999)).toBe('2.0K'); // 1.999 → 2.0
    });

    it('should handle edge case at 1000 boundary', () => {
      expect(formatNumber(999)).toBe('999');
      expect(formatNumber(1000)).toBe('1.0K');
    });

    it('should handle edge case at 1000000 boundary', () => {
      expect(formatNumber(999999)).toBe('1000.0K');
      expect(formatNumber(1000000)).toBe('1.0M');
    });
  });

  describe('Edge Cases & Integration', () => {
    it('should handle very large currency values', () => {
      expect(formatCurrency(999999999)).toBe('999.999.999\u00a0€');
    });

    it('should handle very old dates', () => {
      const oldDate = new Date('1900-01-01T00:00:00Z');
      const formatted = formatDate(oldDate);
      expect(formatted).toMatch(/^\d{2}\.\d{2}\.\d{4}$/);
    });

    it('should handle far future dates', () => {
      const futureDate = new Date('2099-12-31T23:59:59Z');
      const formatted = formatDate(futureDate);
      expect(formatted).toMatch(/^\d{2}\.\d{2}\.\d{4}$/);
    });

    it('should format zero values consistently', () => {
      expect(formatCurrency(0)).toBe('0\u00a0€');
      expect(formatPercent(0)).toBe('0%');
      expect(formatNumber(0)).toBe('0');
    });
  });
});
