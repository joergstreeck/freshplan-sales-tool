/**
 * Adaptive Field Calculator
 *
 * Berechnet optimale Feldbreiten basierend auf:
 * - Gemessenem Text
 * - Field Type
 * - Field Key
 * - Definierte Maximalbreiten
 */

import type { FieldDefinition } from '../../../types/field.types';

// Maximale Breiten pro Field Key
const FIELD_MAX_WIDTHS: Record<string, number> = {
  // Kompakte Felder
  postalCode: 80,
  plz: 80,
  zipCode: 80,
  customerNumber: 120,
  numberOfLocations: 100,
  numberOfEmployees: 100,
  houseNumber: 80,
  hausnummer: 80,

  // Kleine Felder
  salutation: 150,
  anrede: 150,
  contractType: 180,

  // Mittlere Felder
  firstName: 200,
  vorname: 200,
  lastName: 200,
  nachname: 200,
  phone: 200,
  telefon: 200,
  mobile: 200,
  fax: 200,
  legalForm: 250,
  industry: 250,
  branche: 250,
  chainCustomer: 200,
  country: 200,
  land: 200,

  // Große Felder
  email: 400,
  emailAddress: 400,
  companyName: 500,
  firmenname: 500,
  street: 400,
  strasse: 400,
  address: 400,
  city: 300,
  stadt: 300,
  website: 400,
  webseite: 400,
  contactPerson: 400,
  ansprechpartner: 400,

  // Volle Breite
  notes: 9999,
  notizen: 9999,
  description: 9999,
  beschreibung: 9999,
  comment: 9999,
  kommentar: 9999,
  internalNotes: 9999,
  specialRequirements: 9999,
};

// Konstanten für Berechnung
const CHAR_AVERAGE_WIDTH = 8; // Durchschnittliche Zeichen-Breite in px
const PADDING_BUFFER = 40; // Input padding + borders
const MIN_FIELD_WIDTH = 80; // Absolute Mindestbreite

/**
 * Berechnet die optimale Feldbreite
 *
 * @param field - Felddefinition
 * @param measuredTextWidth - Gemessene Breite des Textes
 * @returns Optimale Breite in Pixeln
 */
export function calculateFieldWidth(field: FieldDefinition, measuredTextWidth: number): number {
  // 1. Basis-Breite aus gemessenem Text
  let width = measuredTextWidth + PADDING_BUFFER;

  // 2. Minimum aus Field-Typ
  const minWidth = getMinWidthForField(field);
  width = Math.max(width, minWidth);

  // 3. Maximum aus Field-Key oder Typ
  const maxWidth = FIELD_MAX_WIDTHS[field.key] || getMaxWidthForType(field.fieldType);
  width = Math.min(width, maxWidth);

  return width;
}

/**
 * Ermittelt Mindestbreite basierend auf Feld
 */
function getMinWidthForField(field: FieldDefinition): number {
  // Spezielle Mindestbreiten für bestimmte Keys
  if (field.key in FIELD_MAX_WIDTHS) {
    // Mindestbreite ist 50% der Maximalbreite
    return Math.max(MIN_FIELD_WIDTH, FIELD_MAX_WIDTHS[field.key] * 0.5);
  }

  // Fallback auf Typ-basierte Mindestbreite
  return getMinWidthForType(field.fieldType);
}

/**
 * Mindestbreite nach Feldtyp
 */
function getMinWidthForType(fieldType: string): number {
  switch (fieldType) {
    case 'number':
      return 80;
    case 'email':
      return 200;
    case 'select':
    case 'dropdown':
      return 150;
    case 'date':
    case 'datetime':
      return 140;
    case 'checkbox':
      return 120;
    case 'textarea':
    case 'multiline':
      return 300;
    default:
      return 120;
  }
}

/**
 * Maximalbreite nach Feldtyp
 */
function getMaxWidthForType(fieldType: string): number {
  switch (fieldType) {
    case 'number':
      return 120;
    case 'email':
      return 400;
    case 'select':
    case 'dropdown':
      return 300;
    case 'date':
    case 'datetime':
      return 180;
    case 'checkbox':
      return 200;
    case 'textarea':
    case 'multiline':
      return 9999; // Volle Breite
    case 'text':
    default:
      return 300;
  }
}

/**
 * Gibt die CSS-Klasse für die Grid-Größe zurück
 */
export function getFieldSizeClass(field: FieldDefinition): string {
  const maxWidth = FIELD_MAX_WIDTHS[field.key] || getMaxWidthForType(field.fieldType);

  if (maxWidth <= 120) return 'compact';
  if (maxWidth <= 200) return 'small';
  if (maxWidth <= 300) return 'medium';
  if (maxWidth <= 500) return 'large';
  return 'full';
}

/**
 * Debug-Helper für Entwicklung
 */
export function debugFieldWidth(field: FieldDefinition, value: string): void {
  const measuredWidth = value.length * CHAR_AVERAGE_WIDTH;
  const _calculatedWidth = calculateFieldWidth(field, measuredWidth);
  const _sizeClass = getFieldSizeClass(field);

  // Size class calculated
}
