/**
 * Base Validation Schemas
 *
 * Wiederverwendbare Zod-Schemas für allgemeine Validierungen.
 * Speziell angepasst für deutsche Standards.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-validation.md
 */

import { z } from 'zod';

/**
 * Deutsche Postleitzahl (5 Ziffern)
 */
export const germanPostalCodeSchema = z
  .string()
  .regex(/^[0-9]{5}$/, 'PLZ muss genau 5 Ziffern haben')
  .describe('Deutsche Postleitzahl');

/**
 * Deutsche/Internationale Telefonnummer
 * Erlaubt: +49 123 456789, 0123-456789, (0123) 456789
 */
export const germanPhoneSchema = z
  .string()
  .regex(/^[\d\s\-+()/]+$/, 'Ungültige Telefonnummer')
  .refine(val => {
    const digitsOnly = val.replace(/\D/g, '');
    return digitsOnly.length >= 10 && digitsOnly.length <= 15;
  }, 'Telefonnummer muss zwischen 10 und 15 Ziffern haben')
  .transform(val => val.trim())
  .describe('Telefonnummer');

/**
 * E-Mail Adresse
 * Normalisiert zu lowercase
 */
export const emailSchema = z
  .string()
  .email('Ungültige E-Mail-Adresse')
  .toLowerCase()
  .trim()
  .describe('E-Mail Adresse');

/**
 * URL/Website
 * Optional, erlaubt leeren String
 */
export const urlSchema = z
  .string()
  .trim()
  .refine(val => {
    if (!val) return true; // Leer ist erlaubt
    try {
      new URL(val.startsWith('http') ? val : `https://${val}`);
      return true;
    } catch {
      return false;
    }
  }, 'Ungültige URL')
  .transform(val => {
    if (!val) return '';
    // Füge https:// hinzu wenn kein Protokoll
    return val.startsWith('http') ? val : `https://${val}`;
  })
  .describe('Website URL');

/**
 * Firmenname
 * Mit Sicherheits-Checks gegen HTML/Script Injection
 */
export const companyNameSchema = z
  .string()
  .min(2, 'Firmenname muss mindestens 2 Zeichen haben')
  .max(100, 'Firmenname darf maximal 100 Zeichen haben')
  .trim()
  .refine(val => !val.match(/[<>]/), 'Firmenname darf keine HTML-Zeichen enthalten')
  .refine(val => !val.toLowerCase().includes('script'), 'Ungültiger Firmenname')
  .describe('Firmenname');

/**
 * Allgemeine Textfelder
 */
export const textFieldSchema = (minLength = 0, maxLength = 255) =>
  z
    .string()
    .min(minLength, minLength > 0 ? `Mindestens ${minLength} Zeichen erforderlich` : undefined)
    .max(maxLength, `Maximal ${maxLength} Zeichen erlaubt`)
    .trim();

/**
 * Zahlenfelder mit Range
 */
export const numberFieldSchema = (min?: number, max?: number) => {
  let schema = z.number({
    required_error: 'Bitte geben Sie eine Zahl ein',
    invalid_type_error: 'Muss eine Zahl sein',
  });

  if (min !== undefined) {
    schema = schema.min(min, `Wert muss mindestens ${min} sein`);
  }
  if (max !== undefined) {
    schema = schema.max(max, `Wert darf maximal ${max} sein`);
  }

  return schema;
};

/**
 * Positive Ganzzahl (z.B. für Anzahl-Felder)
 */
export const positiveIntegerSchema = z
  .number()
  .int('Muss eine ganze Zahl sein')
  .positive('Muss größer als 0 sein');

/**
 * Optionale positive Ganzzahl
 */
export const optionalPositiveIntegerSchema = z
  .number()
  .int('Muss eine ganze Zahl sein')
  .positive('Muss größer als 0 sein')
  .optional()
  .nullable();

/**
 * Helper für optional positive integer mit max constraint
 */
export const optionalPositiveIntegerWithMaxSchema = (max: number, message?: string) =>
  z
    .number()
    .int('Muss eine ganze Zahl sein')
    .positive('Muss größer als 0 sein')
    .max(max, message || `Wert darf maximal ${max} sein`)
    .optional()
    .nullable();

/**
 * Datums-String (ISO format)
 */
export const dateStringSchema = z
  .string()
  .refine(val => !isNaN(Date.parse(val)), 'Ungültiges Datum')
  .describe('Datum');

/**
 * Kontaktperson Name
 */
export const contactPersonSchema = z
  .string()
  .min(2, 'Name muss mindestens 2 Zeichen haben')
  .max(100, 'Name darf maximal 100 Zeichen haben')
  .trim()
  .refine(
    val => /^[a-zA-ZäöüÄÖÜß\s\-.]+$/.test(val),
    'Name darf nur Buchstaben, Leerzeichen und Bindestriche enthalten'
  )
  .describe('Kontaktperson');

/**
 * Straße und Hausnummer
 */
export const streetSchema = z
  .string()
  .min(3, 'Straße muss mindestens 3 Zeichen haben')
  .max(100, 'Straße darf maximal 100 Zeichen haben')
  .trim()
  .describe('Straße und Hausnummer');

/**
 * Stadt/Ort
 */
export const citySchema = z
  .string()
  .min(2, 'Ort muss mindestens 2 Zeichen haben')
  .max(50, 'Ort darf maximal 50 Zeichen haben')
  .trim()
  .refine(
    val => /^[a-zA-ZäöüÄÖÜß\s\-.]+$/.test(val),
    'Ort darf nur Buchstaben, Leerzeichen und Bindestriche enthalten'
  )
  .describe('Stadt/Ort');

/**
 * Multi-Select Array Schema
 */
export const multiSelectSchema = (options: string[]) =>
  z
    .array(z.enum(options as [string, ...string[]]))
    .min(1, 'Bitte wählen Sie mindestens eine Option')
    .describe('Mehrfachauswahl');

/**
 * Prozent-Wert (0-100)
 */
export const percentageSchema = z
  .number()
  .min(0, 'Prozent muss zwischen 0 und 100 liegen')
  .max(100, 'Prozent muss zwischen 0 und 100 liegen')
  .describe('Prozentwert');

/**
 * Euro-Betrag (positive Zahl mit max 2 Nachkommastellen)
 */
export const euroAmountSchema = z
  .number()
  .positive('Betrag muss größer als 0 sein')
  .multipleOf(0.01, 'Maximal 2 Nachkommastellen erlaubt')
  .describe('Euro-Betrag');

/**
 * IBAN Validierung (vereinfacht)
 */
export const ibanSchema = z
  .string()
  .trim()
  .toUpperCase()
  .regex(/^[A-Z]{2}[0-9]{2}[A-Z0-9]+$/, 'Ungültige IBAN')
  .refine(
    val => val.length >= 15 && val.length <= 34,
    'IBAN muss zwischen 15 und 34 Zeichen lang sein'
  )
  .describe('IBAN');

/**
 * BIC/SWIFT Code
 */
export const bicSchema = z
  .string()
  .trim()
  .toUpperCase()
  .regex(/^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$/, 'Ungültiger BIC/SWIFT Code')
  .describe('BIC/SWIFT');

/**
 * Steuernummer (vereinfacht)
 */
export const taxNumberSchema = z
  .string()
  .trim()
  .regex(/^[0-9/-]+$/, 'Ungültige Steuernummer')
  .min(10, 'Steuernummer zu kurz')
  .max(20, 'Steuernummer zu lang')
  .describe('Steuernummer');

/**
 * Handelsregisternummer
 */
export const tradeRegisterSchema = z
  .string()
  .trim()
  .regex(/^(HRA|HRB)\s*[0-9]+(\s*[A-Z]{1,3})?$/, 'Ungültige Handelsregisternummer (z.B. HRB 12345)')
  .describe('Handelsregisternummer');
