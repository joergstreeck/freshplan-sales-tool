/**
 * Utility functions for formatting values in the UI
 */

/**
 * Format a number as currency in EUR
 */
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(amount);
}

/**
 * Format a number as percentage
 */
export function formatPercentage(value: number, decimals: number = 1): string {
  return new Intl.NumberFormat('de-DE', {
    style: 'percent',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value / 100);
}

/**
 * Format a number with thousand separators
 */
export function formatNumber(value: number): string {
  return new Intl.NumberFormat('de-DE').format(value);
}

/**
 * Format days as text
 */
export function formatDays(days: number): string {
  if (days === 0) return 'Sofort';
  if (days === 1) return '1 Tag';
  return `${days} Tage`;
}

/**
 * Format boolean values as German text
 */
export function formatBoolean(
  value: boolean,
  trueText: string = 'Ja',
  falseText: string = 'Nein'
): string {
  return value ? trueText : falseText;
}
