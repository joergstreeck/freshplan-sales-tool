/**
 * Validation utilities
 * Common validation functions for form inputs
 */

/**
 * Validate email address
 */
export function validateEmail(email: string): boolean | string {
  const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  
  if (!email) {
    return 'E-Mail-Adresse ist erforderlich';
  }
  
  if (!pattern.test(email)) {
    return 'Bitte geben Sie eine gültige E-Mail-Adresse ein';
  }
  
  return true;
}

/**
 * Validate phone number (German format)
 */
export function validatePhone(phone: string): boolean | string {
  // Remove spaces, dashes, and parentheses
  const cleaned = phone.replace(/[\s\-\(\)]/g, '');
  
  // Check if it's a valid German phone number
  const patterns = [
    /^(\+49|0049|0)[1-9]\d{9,10}$/,  // German landline or mobile
    /^(\+|00)[1-9]\d{8,14}$/         // International
  ];
  
  if (!phone) {
    return true; // Phone is optional
  }
  
  const isValid = patterns.some(pattern => pattern.test(cleaned));
  
  if (!isValid) {
    return 'Bitte geben Sie eine gültige Telefonnummer ein';
  }
  
  return true;
}

/**
 * Validate German postal code
 */
export function validatePostalCode(code: string): boolean | string {
  const pattern = /^\d{5}$/;
  
  if (!code) {
    return 'Postleitzahl ist erforderlich';
  }
  
  if (!pattern.test(code)) {
    return 'Bitte geben Sie eine gültige Postleitzahl ein (5 Ziffern)';
  }
  
  return true;
}

/**
 * Validate required field
 */
export function validateRequired(value: any): boolean | string {
  if (value === null || value === undefined || value === '') {
    return 'Dieses Feld ist erforderlich';
  }
  
  if (typeof value === 'string' && !value.trim()) {
    return 'Dieses Feld ist erforderlich';
  }
  
  return true;
}

/**
 * Validate minimum length
 */
export function validateMinLength(value: string, min: number): boolean | string {
  if (!value || value.length < min) {
    return `Mindestens ${min} Zeichen erforderlich`;
  }
  
  return true;
}

/**
 * Validate maximum length
 */
export function validateMaxLength(value: string, max: number): boolean | string {
  if (value && value.length > max) {
    return `Maximal ${max} Zeichen erlaubt`;
  }
  
  return true;
}

/**
 * Validate number range
 */
export function validateRange(value: number, min?: number, max?: number): boolean | string {
  if (min !== undefined && value < min) {
    return `Der Wert muss mindestens ${min} sein`;
  }
  
  if (max !== undefined && value > max) {
    return `Der Wert darf maximal ${max} sein`;
  }
  
  return true;
}

/**
 * Validate URL
 */
export function validateURL(url: string): boolean | string {
  try {
    new URL(url);
    return true;
  } catch {
    return 'Bitte geben Sie eine gültige URL ein';
  }
}

/**
 * Validate date
 */
export function validateDate(date: string): boolean | string {
  const parsed = new Date(date);
  
  if (isNaN(parsed.getTime())) {
    return 'Bitte geben Sie ein gültiges Datum ein';
  }
  
  return true;
}

/**
 * Validate future date
 */
export function validateFutureDate(date: string): boolean | string {
  const parsed = new Date(date);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  if (parsed < today) {
    return 'Das Datum muss in der Zukunft liegen';
  }
  
  return true;
}

/**
 * Validate IBAN (simplified)
 */
export function validateIBAN(iban: string): boolean | string {
  // Remove spaces
  const cleaned = iban.replace(/\s/g, '').toUpperCase();
  
  // Basic IBAN validation (length and pattern)
  const pattern = /^[A-Z]{2}\d{2}[A-Z0-9]+$/;
  
  if (!pattern.test(cleaned)) {
    return 'Bitte geben Sie eine gültige IBAN ein';
  }
  
  // Check length for common countries
  const lengths: Record<string, number> = {
    DE: 22, // Germany
    AT: 20, // Austria
    CH: 21  // Switzerland
  };
  
  const country = cleaned.substring(0, 2);
  const expectedLength = lengths[country];
  
  if (expectedLength && cleaned.length !== expectedLength) {
    return `Eine ${country}-IBAN muss ${expectedLength} Zeichen haben`;
  }
  
  return true;
}

/**
 * Validate company name
 */
export function validateCompanyName(name: string): boolean | string {
  if (!name || name.trim().length < 2) {
    return 'Der Firmenname muss mindestens 2 Zeichen lang sein';
  }
  
  // Check for suspicious patterns
  const suspicious = /[<>{}\\]/;
  if (suspicious.test(name)) {
    return 'Der Firmenname enthält ungültige Zeichen';
  }
  
  return true;
}

/**
 * Validate percentage
 */
export function validatePercentage(value: number): boolean | string {
  if (value < 0 || value > 100) {
    return 'Der Wert muss zwischen 0 und 100 liegen';
  }
  
  return true;
}

/**
 * Create custom validator
 */
export function createValidator(rules: {
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  min?: number;
  max?: number;
  pattern?: RegExp;
  custom?: (value: any) => boolean | string;
}) {
  return (value: any): boolean | string => {
    if (rules.required) {
      const requiredResult = validateRequired(value);
      if (requiredResult !== true) return requiredResult;
    }
    
    if (typeof value === 'string') {
      if (rules.minLength) {
        const minResult = validateMinLength(value, rules.minLength);
        if (minResult !== true) return minResult;
      }
      
      if (rules.maxLength) {
        const maxResult = validateMaxLength(value, rules.maxLength);
        if (maxResult !== true) return maxResult;
      }
      
      if (rules.pattern && !rules.pattern.test(value)) {
        return 'Der Wert entspricht nicht dem erforderlichen Format';
      }
    }
    
    if (typeof value === 'number') {
      const rangeResult = validateRange(value, rules.min, rules.max);
      if (rangeResult !== true) return rangeResult;
    }
    
    if (rules.custom) {
      return rules.custom(value);
    }
    
    return true;
  };
}