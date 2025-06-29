// Email validation
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// German postal code validation (5 digits)
export const isValidGermanPostalCode = (plz: string): boolean => {
  const plzRegex = /^[0-9]{5}$/;
  return plzRegex.test(plz);
};

// Phone number validation (flexible, allows common formats)
export const isValidPhoneNumber = (phone: string): boolean => {
  // Remove spaces, dashes, parentheses
  const cleaned = phone.replace(/[\s\-()]/g, '');
  // Check if it contains only numbers and optional + at start
  const phoneRegex = /^\+?[0-9]{7,15}$/;
  return phoneRegex.test(cleaned);
};

// Required field validation
export const isRequired = (value: string): boolean => {
  return value.trim().length > 0;
};

// Number validation
export const isValidNumber = (value: string): boolean => {
  return !isNaN(Number(value)) && Number(value) >= 0;
};

// Validation messages
export const validationMessages = {
  email: {
    de: 'Bitte geben Sie eine gültige E-Mail-Adresse ein',
    en: 'Please enter a valid email address',
  },
  postalCode: {
    de: 'Bitte geben Sie eine gültige 5-stellige PLZ ein',
    en: 'Please enter a valid 5-digit postal code',
  },
  phone: {
    de: 'Bitte geben Sie eine gültige Telefonnummer ein',
    en: 'Please enter a valid phone number',
  },
  required: {
    de: 'Dieses Feld ist erforderlich',
    en: 'This field is required',
  },
  number: {
    de: 'Bitte geben Sie eine gültige Zahl ein',
    en: 'Please enter a valid number',
  },
};
