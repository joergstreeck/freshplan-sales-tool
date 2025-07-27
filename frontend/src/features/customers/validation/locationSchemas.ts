/**
 * Location Validation Schemas
 * 
 * Zod-Schemas für Location und DetailedLocation Entities.
 * Unterstützt die hierarchische Struktur von Standorten.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/02-data-model.md
 */

import { z } from 'zod';
import {
  streetSchema,
  germanPostalCodeSchema,
  citySchema,
  contactPersonSchema,
  germanPhoneSchema,
  emailSchema,
  positiveIntegerSchema,
  optionalPositiveIntegerSchema,
  optionalPositiveIntegerWithMaxSchema
} from './baseSchemas';

/**
 * Location Type Enum
 */
export const locationTypeEnum = z.enum([
  'hauptstandort',
  'filiale',
  'aussenstelle',
  'niederlassung',
  'produktionsstaette'
], {
  required_error: 'Bitte wählen Sie einen Standorttyp',
  invalid_type_error: 'Ungültiger Standorttyp'
});

/**
 * Basis Location Schema
 */
export const locationBaseSchema = z.object({
  // Pflichtfelder
  locationType: locationTypeEnum,
  name: z.string()
    .min(2, 'Standortname muss mindestens 2 Zeichen haben')
    .max(100, 'Standortname darf maximal 100 Zeichen haben')
    .trim(),
  
  // Optionale Adressfelder
  street: streetSchema.optional().or(z.literal('')),
  postalCode: germanPostalCodeSchema.optional().or(z.literal('')),
  city: citySchema.optional().or(z.literal('')),
  
  // Kontaktinformationen
  contactPerson: contactPersonSchema.optional().or(z.literal('')),
  contactPhone: germanPhoneSchema.optional().or(z.literal('')),
  contactEmail: emailSchema.optional().or(z.literal('')),
  
  // Zusatzinformationen
  notes: z.string().max(500).optional()
});

/**
 * Hotel Location spezifische Felder
 */
export const hotelLocationSchema = locationBaseSchema.extend({
  roomCount: optionalPositiveIntegerWithMaxSchema(999, 'Anzahl Zimmer darf maximal 999 sein'),
  restaurantSeats: optionalPositiveIntegerSchema,
  conferenceRooms: optionalPositiveIntegerSchema,
  parkingSpaces: optionalPositiveIntegerSchema
});

/**
 * Krankenhaus Location spezifische Felder
 */
export const hospitalLocationSchema = locationBaseSchema.extend({
  bedCount: positiveIntegerSchema
    .max(999, 'Anzahl Betten darf maximal 999 sein'),
  departments: z.array(z.string()).optional(),
  hasEmergency: z.boolean().optional(),
  kitchenCapacity: optionalPositiveIntegerSchema
    .describe('Küchenkapazität Mahlzeiten/Tag')
});

/**
 * Seniorenresidenz Location spezifische Felder
 */
export const seniorResidenceLocationSchema = locationBaseSchema.extend({
  residentCount: positiveIntegerSchema
    .max(999, 'Anzahl Bewohner darf maximal 999 sein'),
  careUnits: optionalPositiveIntegerSchema,
  commonRooms: optionalPositiveIntegerSchema,
  kitchenType: z.enum(['own', 'central', 'catered']).optional()
});

/**
 * Restaurant Location spezifische Felder
 */
export const restaurantLocationSchema = locationBaseSchema.extend({
  seatCount: positiveIntegerSchema
    .max(999, 'Anzahl Sitzplätze darf maximal 999 sein'),
  outdoorSeats: optionalPositiveIntegerSchema,
  kitchenSize: optionalPositiveIntegerSchema
    .describe('Küchengröße in m²'),
  hasBar: z.boolean().optional()
});

/**
 * Betriebsrestaurant Location spezifische Felder
 */
export const companyRestaurantLocationSchema = locationBaseSchema.extend({
  seatCount: positiveIntegerSchema,
  servingLines: optionalPositiveIntegerSchema
    .describe('Anzahl Ausgabelinien'),
  kitchenCapacity: optionalPositiveIntegerSchema
    .describe('Max. Mahlzeiten pro Service')
});

/**
 * Detailed Location Schema
 * Für Sub-Locations innerhalb eines Standorts
 */
export const detailedLocationSchema = z.object({
  // Identifikation
  name: z.string()
    .min(2, 'Name muss mindestens 2 Zeichen haben')
    .max(100, 'Name darf maximal 100 Zeichen haben')
    .trim()
    .describe('z.B. "Restaurant Erdgeschoss", "Station 3B"'),
  
  category: z.enum([
    'restaurant',
    'cafeteria',
    'kiosk',
    'station',
    'kitchen',
    'storage',
    'other'
  ]).describe('Art der Detail-Location'),
  
  floor: z.string().max(20).optional()
    .describe('Stockwerk/Ebene'),
  
  // Kapazitäten
  capacity: optionalPositiveIntegerSchema
    .describe('Kapazität (Personen/Betten/etc.)'),
  
  // Betriebszeiten
  operatingHours: z.string().max(100).optional()
    .describe('z.B. "Mo-Fr 11:30-14:00"'),
  
  // Kontakt
  responsiblePerson: contactPersonSchema.optional(),
  internalPhone: z.string().max(20).optional(),
  
  // Spezielle Anforderungen
  specialRequirements: z.array(z.string()).optional()
    .describe('z.B. ["Koscher", "Halal", "Allergenfreie Küche"]'),
  
  // Zusatzinformationen
  notes: z.string().max(500).optional()
});

/**
 * Batch-Import Schema für mehrere DetailedLocations
 */
export const detailedLocationBatchSchema = z.object({
  locations: z.array(detailedLocationSchema)
    .min(1, 'Mindestens eine Detail-Location erforderlich')
    .max(100, 'Maximal 100 Detail-Locations pro Import'),
  
  copyToAll: z.boolean().optional()
    .describe('Diese Detail-Locations zu allen Standorten hinzufügen')
});

/**
 * Dynamischer Location Schema basierend auf Industry
 */
export const getLocationSchema = (industry?: string) => {
  switch (industry) {
    case 'hotel':
      return hotelLocationSchema;
    case 'krankenhaus':
      return hospitalLocationSchema;
    case 'seniorenresidenz':
      return seniorResidenceLocationSchema;
    case 'restaurant':
      return restaurantLocationSchema;
    case 'betriebsrestaurant':
      return companyRestaurantLocationSchema;
    default:
      return locationBaseSchema;
  }
};

/**
 * Adress-Validierung (Cross-Field)
 * Wenn ein Adressfeld ausgefüllt ist, müssen alle ausgefüllt sein
 */
export const validateCompleteAddress = (data: any) => {
  const { street, postalCode, city } = data;
  const hasAnyAddress = street || postalCode || city;
  const hasCompleteAddress = street && postalCode && city;
  
  if (hasAnyAddress && !hasCompleteAddress) {
    return {
      isValid: false,
      error: 'Bitte geben Sie eine vollständige Adresse ein (Straße, PLZ und Ort)'
    };
  }
  
  return { isValid: true };
};

/**
 * Type Exports
 */
export type LocationBase = z.infer<typeof locationBaseSchema>;
export type LocationHotel = z.infer<typeof hotelLocationSchema>;
export type LocationHospital = z.infer<typeof hospitalLocationSchema>;
export type LocationSeniorResidence = z.infer<typeof seniorResidenceLocationSchema>;
export type LocationRestaurant = z.infer<typeof restaurantLocationSchema>;
export type LocationCompanyRestaurant = z.infer<typeof companyRestaurantLocationSchema>;
export type DetailedLocation = z.infer<typeof detailedLocationSchema>;

export type LocationData = 
  | LocationBase
  | LocationHotel
  | LocationHospital
  | LocationSeniorResidence
  | LocationRestaurant
  | LocationCompanyRestaurant;