/**
 * Formatting utilities
 * Number, date, and currency formatting
 */

/**
 * Format currency (EUR)
 */
export function formatCurrency(
  value: number, 
  options?: {
    decimals?: number;
    includeSymbol?: boolean;
    locale?: string;
  }
): string {
  const {
    decimals = 0,
    includeSymbol = true,
    locale = 'de-DE'
  } = options || {};

  const formatted = new Intl.NumberFormat(locale, {
    style: includeSymbol ? 'currency' : 'decimal',
    currency: 'EUR',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(value);

  return formatted;
}

/**
 * Format number with locale
 */
export function formatNumber(
  value: number,
  options?: {
    decimals?: number;
    locale?: string;
  }
): string {
  const {
    decimals = 0,
    locale = 'de-DE'
  } = options || {};

  return new Intl.NumberFormat(locale, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(value);
}

/**
 * Format percentage
 */
export function formatPercent(
  value: number,
  options?: {
    decimals?: number;
    locale?: string;
  }
): string {
  const {
    decimals = 0,
    locale = 'de-DE'
  } = options || {};

  return new Intl.NumberFormat(locale, {
    style: 'percent',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(value);
}

/**
 * Format date
 */
export function formatDate(
  date: Date | string | number,
  options?: {
    format?: 'short' | 'medium' | 'long' | 'full';
    locale?: string;
  }
): string {
  const {
    format = 'medium',
    locale = 'de-DE'
  } = options || {};

  const dateObj = typeof date === 'string' || typeof date === 'number' 
    ? new Date(date) 
    : date;

  const formatOptions: Intl.DateTimeFormatOptions = {
    short: { day: '2-digit', month: '2-digit', year: 'numeric' } as const,
    medium: { day: '2-digit', month: 'short', year: 'numeric' } as const,
    long: { day: 'numeric', month: 'long', year: 'numeric' } as const,
    full: { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' } as const
  }[format];

  return new Intl.DateTimeFormat(locale, formatOptions).format(dateObj);
}

/**
 * Format date and time
 */
export function formatDateTime(
  date: Date | string | number,
  options?: {
    dateFormat?: 'short' | 'medium' | 'long';
    timeFormat?: 'short' | 'medium';
    locale?: string;
  }
): string {
  const {
    dateFormat = 'medium',
    timeFormat = 'short',
    locale = 'de-DE'
  } = options || {};

  const dateObj = typeof date === 'string' || typeof date === 'number' 
    ? new Date(date) 
    : date;

  const dateOptions: Intl.DateTimeFormatOptions = {
    short: { day: '2-digit', month: '2-digit', year: 'numeric' } as const,
    medium: { day: '2-digit', month: 'short', year: 'numeric' } as const,
    long: { day: 'numeric', month: 'long', year: 'numeric' } as const
  }[dateFormat];

  const timeOptions: Intl.DateTimeFormatOptions = {
    short: { hour: '2-digit', minute: '2-digit' } as const,
    medium: { hour: '2-digit', minute: '2-digit', second: '2-digit' } as const
  }[timeFormat];

  const formatOptions = { ...dateOptions, ...timeOptions };

  return new Intl.DateTimeFormat(locale, formatOptions).format(dateObj);
}

/**
 * Format relative time
 */
export function formatRelativeTime(
  date: Date | string | number,
  options?: {
    locale?: string;
    numeric?: 'always' | 'auto';
  }
): string {
  const {
    locale = 'de-DE',
    numeric = 'auto'
  } = options || {};

  const dateObj = typeof date === 'string' || typeof date === 'number' 
    ? new Date(date) 
    : date;

  const now = new Date();
  const diffInSeconds = Math.floor((dateObj.getTime() - now.getTime()) / 1000);
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  const diffInHours = Math.floor(diffInMinutes / 60);
  const diffInDays = Math.floor(diffInHours / 24);
  const diffInWeeks = Math.floor(diffInDays / 7);
  const diffInMonths = Math.floor(diffInDays / 30);
  const diffInYears = Math.floor(diffInDays / 365);

  const rtf = new Intl.RelativeTimeFormat(locale, { numeric });

  if (Math.abs(diffInSeconds) < 60) {
    return rtf.format(diffInSeconds, 'second');
  } else if (Math.abs(diffInMinutes) < 60) {
    return rtf.format(diffInMinutes, 'minute');
  } else if (Math.abs(diffInHours) < 24) {
    return rtf.format(diffInHours, 'hour');
  } else if (Math.abs(diffInDays) < 7) {
    return rtf.format(diffInDays, 'day');
  } else if (Math.abs(diffInWeeks) < 4) {
    return rtf.format(diffInWeeks, 'week');
  } else if (Math.abs(diffInMonths) < 12) {
    return rtf.format(diffInMonths, 'month');
  } else {
    return rtf.format(diffInYears, 'year');
  }
}

/**
 * Format file size
 */
export function formatFileSize(bytes: number, decimals: number = 2): string {
  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];

  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

/**
 * Format phone number
 */
export function formatPhone(phone: string): string {
  // Remove all non-digits
  const cleaned = phone.replace(/\D/g, '');
  
  // German mobile number
  if (cleaned.match(/^49[0-9]{10,11}$/)) {
    const number = cleaned.substring(2);
    return `+49 ${number.substring(0, 3)} ${number.substring(3, 7)} ${number.substring(7)}`;
  }
  
  // German landline
  if (cleaned.match(/^49[0-9]{8,10}$/)) {
    const number = cleaned.substring(2);
    const area = number.substring(0, number.length - 6);
    const rest = number.substring(area.length);
    return `+49 ${area} ${rest.substring(0, 3)} ${rest.substring(3)}`;
  }
  
  // Local German number
  if (cleaned.match(/^0[0-9]{9,11}$/)) {
    const area = cleaned.substring(1, cleaned.length - 6);
    const rest = cleaned.substring(area.length + 1);
    return `0${area} ${rest.substring(0, 3)} ${rest.substring(3)}`;
  }
  
  // Return as-is if no pattern matches
  return phone;
}

/**
 * Format company name (capitalize properly)
 */
export function formatCompanyName(name: string): string {
  // List of words that should stay lowercase
  const lowercase = ['und', 'oder', 'der', 'die', 'das', 'für', 'mit', 'bei'];
  
  // List of abbreviations that should stay uppercase
  const uppercase = ['GmbH', 'AG', 'KG', 'OHG', 'UG', 'Co', 'KGaA'];
  
  return name
    .split(' ')
    .map((word, index) => {
      // First word is always capitalized
      if (index === 0) {
        return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
      }
      
      // Check if it's a known abbreviation
      const upperWord = word.toUpperCase();
      if (uppercase.some(abbr => abbr.toUpperCase() === upperWord)) {
        return uppercase.find(abbr => abbr.toUpperCase() === upperWord)!;
      }
      
      // Check if it should stay lowercase
      if (lowercase.includes(word.toLowerCase())) {
        return word.toLowerCase();
      }
      
      // Otherwise capitalize first letter
      return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
    })
    .join(' ');
}

/**
 * Truncate text with ellipsis
 */
export function truncate(
  text: string, 
  length: number, 
  suffix: string = '...'
): string {
  if (text.length <= length) return text;
  return text.substring(0, length - suffix.length) + suffix;
}

/**
 * Pluralize German words
 */
export function pluralize(count: number, singular: string, plural?: string): string {
  if (count === 1) {
    return `${count} ${singular}`;
  }
  
  // Use provided plural or add 'e' for simple pluralization
  const pluralForm = plural || `${singular}e`;
  return `${count} ${pluralForm}`;
}