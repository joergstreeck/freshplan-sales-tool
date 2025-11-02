/**
 * Shared Data Formatters
 *
 * Generic formatting utilities for consistent data display across the application.
 * Extracted from CustomerTable during Migration M1 (Sprint 2.1.7.7)
 *
 * @module dataFormatters
 * @since Sprint 2.1.7.7 (Migration M1)
 */

import { format as formatDateFns } from 'date-fns';
import { de } from 'date-fns/locale';

/**
 * Format number as currency (EUR, German locale)
 *
 * @example
 * formatCurrency(1234.56) // "1.234,56 €"
 * formatCurrency(undefined) // "-"
 */
export function formatCurrency(amount?: number | null): string {
  if (amount === null || amount === undefined || isNaN(amount)) {
    return '-';
  }

  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

/**
 * Format number as currency with no decimals
 *
 * @example
 * formatCurrencyCompact(1234.56) // "1.235 €"
 */
export function formatCurrencyCompact(amount?: number | null): string {
  if (amount === null || amount === undefined || isNaN(amount)) {
    return '-';
  }

  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
}

/**
 * Format date string as German date (dd.MM.yyyy)
 *
 * @example
 * formatDate('2025-11-01') // "01.11.2025"
 * formatDate(undefined) // "-"
 */
export function formatDate(dateString?: string | null): string {
  if (!dateString) return '-';

  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '-';

    return formatDateFns(date, 'dd.MM.yyyy', { locale: de });
  } catch {
    return '-';
  }
}

/**
 * Format date string as German date with time (dd.MM.yyyy HH:mm)
 *
 * @example
 * formatDateTime('2025-11-01T14:30:00') // "01.11.2025 14:30"
 */
export function formatDateTime(dateString?: string | null): string {
  if (!dateString) return '-';

  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '-';

    return formatDateFns(date, 'dd.MM.yyyy HH:mm', { locale: de });
  } catch {
    return '-';
  }
}

/**
 * Format number as percentage
 *
 * @example
 * formatPercentage(0.75) // "75%"
 * formatPercentage(0.333) // "33,3%"
 */
export function formatPercentage(value?: number | null, decimals = 1): string {
  if (value === null || value === undefined || isNaN(value)) {
    return '-';
  }

  return new Intl.NumberFormat('de-DE', {
    style: 'percent',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value);
}

/**
 * Format number with German locale
 *
 * @example
 * formatNumber(1234.56) // "1.234,56"
 */
export function formatNumber(value?: number | null, decimals = 2): string {
  if (value === null || value === undefined || isNaN(value)) {
    return '-';
  }

  return new Intl.NumberFormat('de-DE', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value);
}

/**
 * Format phone number (German style)
 *
 * @example
 * formatPhoneNumber('+49301234567') // "+49 30 123 4567"
 */
export function formatPhoneNumber(phone?: string | null): string {
  if (!phone) return '-';

  // Simple formatting - can be enhanced later
  return phone.replace(/(\+\d{2})(\d{2,3})(\d+)/, '$1 $2 $3');
}
