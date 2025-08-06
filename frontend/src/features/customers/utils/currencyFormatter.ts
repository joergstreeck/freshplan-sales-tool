/**
 * Utility functions für EUR-Formatierung
 */

/**
 * Formatiert eine Zahl als EUR-Währung
 * @param value - Der numerische Wert
 * @returns Formatierter String (z.B. "250.000 €")
 */
export const formatEUR = (value: number | string | null | undefined): string => {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  
  const numValue = typeof value === 'string' ? parseFloat(value) : value;
  
  if (isNaN(numValue)) {
    return '';
  }
  
  // Formatierung mit deutschem Locale
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(numValue);
};

/**
 * Parst einen formatierten EUR-String zurück zu einer Zahl
 * @param formattedValue - Der formatierte String (z.B. "250.000 €")
 * @returns Numerischer Wert
 */
export const parseEUR = (formattedValue: string): number => {
  if (!formattedValue) return 0;
  
  // Entferne alle nicht-numerischen Zeichen außer Komma
  const cleanValue = formattedValue
    .replace(/[^\d,]/g, '')
    .replace(',', '.');
  
  const parsed = parseFloat(cleanValue);
  return isNaN(parsed) ? 0 : parsed;
};

/**
 * Formatiert während der Eingabe (ohne €-Symbol)
 * @param value - Der eingegebene Wert
 * @returns Formatierter String mit Tausender-Punkten
 */
export const formatEURWhileTyping = (value: string): string => {
  // Entferne alle nicht-numerischen Zeichen
  const cleanValue = value.replace(/\D/g, '');
  
  if (!cleanValue) return '';
  
  // Formatiere mit Tausender-Punkten
  const numValue = parseInt(cleanValue, 10);
  return new Intl.NumberFormat('de-DE').format(numValue);
};

/**
 * Validiert ob ein Wert eine gültige Währungseingabe ist
 * @param value - Der zu validierende Wert
 * @returns true wenn gültig
 */
export const isValidCurrencyInput = (value: string): boolean => {
  // Erlaube nur Zahlen und Formatierungs-Zeichen
  return /^[\d\s.]*$/.test(value);
};