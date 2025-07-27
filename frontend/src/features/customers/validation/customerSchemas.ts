/**
 * Customer Validation Schemas
 * 
 * Zod-Schemas für Customer-Entities mit industrie-spezifischen Erweiterungen.
 * Unterstützt dynamische Schema-Generierung basierend auf Field Definitions.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-validation.md
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/02-data-model.md
 */

import { z } from 'zod';
import {
  companyNameSchema,
  emailSchema,
  germanPhoneSchema,
  urlSchema,
  streetSchema,
  germanPostalCodeSchema,
  citySchema,
  positiveIntegerSchema,
  optionalPositiveIntegerSchema,
  optionalPositiveIntegerWithMaxSchema,
  contactPersonSchema,
  euroAmountSchema,
  percentageSchema
} from './baseSchemas';

/**
 * Basis Customer Schema
 * Enthält alle Pflicht- und Standard-Felder
 */
export const customerBaseSchema = z.object({
  // Pflichtfelder
  companyName: companyNameSchema,
  industry: z.enum([
    'hotel',
    'krankenhaus',
    'seniorenresidenz',
    'restaurant',
    'betriebsrestaurant'
  ], {
    required_error: 'Bitte wählen Sie eine Branche',
    invalid_type_error: 'Ungültige Branche'
  }),
  chainCustomer: z.enum(['ja', 'nein'], {
    required_error: 'Bitte wählen Sie ob es sich um einen Ketten-Kunden handelt'
  }),
  
  // Kontaktinformationen (optional)
  email: emailSchema.optional().or(z.literal('')),
  phone: germanPhoneSchema.optional().or(z.literal('')),
  website: urlSchema.optional().or(z.literal('')),
  
  // Adresse (optional, aber wenn eine ausgefüllt, dann alle)
  street: streetSchema.optional().or(z.literal('')),
  postalCode: germanPostalCodeSchema.optional().or(z.literal('')),
  city: citySchema.optional().or(z.literal('')),
  
  // Zusätzliche Informationen
  notes: z.string().max(1000, 'Notizen dürfen maximal 1000 Zeichen haben').optional()
});

/**
 * Hotel-spezifische Felder
 */
export const hotelFieldsSchema = z.object({
  starRating: z.enum(['1', '2', '3', '4', '5'], {
    required_error: 'Bitte wählen Sie die Sterne-Kategorie'
  }),
  roomCount: positiveIntegerSchema
    .max(9999, 'Anzahl Zimmer darf maximal 9999 sein')
    .describe('Anzahl Zimmer'),
  restaurantSeats: optionalPositiveIntegerWithMaxSchema(9999, 'Anzahl Sitzplätze darf maximal 9999 sein')
    .describe('Restaurant Sitzplätze'),
  breakfastGuests: optionalPositiveIntegerSchema
    .describe('Durchschnittliche Frühstücksgäste pro Tag'),
  hasConferenceRooms: z.boolean().optional(),
  conferenceCapacity: optionalPositiveIntegerSchema
    .describe('Maximale Konferenz-Kapazität')
});

/**
 * Krankenhaus-spezifische Felder
 */
export const hospitalFieldsSchema = z.object({
  hospitalType: z.enum(['university', 'general', 'specialized'], {
    required_error: 'Bitte wählen Sie den Krankenhaustyp'
  }),
  bedCount: positiveIntegerSchema
    .max(9999, 'Anzahl Betten darf maximal 9999 sein')
    .describe('Anzahl Betten'),
  departments: z.array(z.string()).min(1, 'Mindestens eine Abteilung erforderlich'),
  patientMealsPerDay: positiveIntegerSchema
    .describe('Patientenmahlzeiten pro Tag'),
  staffMealsPerDay: optionalPositiveIntegerSchema
    .describe('Mitarbeitermahlzeiten pro Tag'),
  hasEmergency: z.boolean().describe('Notaufnahme vorhanden'),
  specialDietRequirements: z.array(z.string()).optional()
});

/**
 * Seniorenresidenz-spezifische Felder
 */
export const seniorResidenceFieldsSchema = z.object({
  residenceType: z.enum(['assisted_living', 'nursing_care', 'mixed'], {
    required_error: 'Bitte wählen Sie den Typ der Einrichtung'
  }),
  residentCount: positiveIntegerSchema
    .max(999, 'Anzahl Bewohner darf maximal 999 sein')
    .describe('Anzahl Bewohner'),
  careLevel: z.array(z.enum(['1', '2', '3', '4', '5'])).min(1, 'Mindestens eine Pflegestufe'),
  mealsPerResident: z.number()
    .min(1)
    .max(5)
    .describe('Mahlzeiten pro Bewohner pro Tag'),
  hasDementiaCare: z.boolean().optional(),
  specialNutritionNeeds: percentageSchema
    .describe('Anteil Bewohner mit speziellen Ernährungsbedürfnissen')
    .optional()
});

/**
 * Restaurant-spezifische Felder
 */
export const restaurantFieldsSchema = z.object({
  restaurantType: z.enum(['fine_dining', 'casual', 'fast_food', 'cafe', 'other']),
  seatCount: positiveIntegerSchema
    .max(999, 'Anzahl Sitzplätze darf maximal 999 sein')
    .describe('Anzahl Sitzplätze'),
  averageGuestsPerDay: positiveIntegerSchema
    .describe('Durchschnittliche Gäste pro Tag'),
  cuisineType: z.string().min(2).max(50),
  hasDelivery: z.boolean(),
  hasCatering: z.boolean(),
  openingDays: z.number().min(1).max(7).describe('Öffnungstage pro Woche'),
  averageCheckSize: euroAmountSchema.optional()
});

/**
 * Betriebsrestaurant-spezifische Felder
 */
export const companyRestaurantFieldsSchema = z.object({
  employeeCount: positiveIntegerSchema
    .max(99999, 'Anzahl Mitarbeiter darf maximal 99999 sein')
    .describe('Anzahl Mitarbeiter im Unternehmen'),
  dailyLunchGuests: positiveIntegerSchema
    .describe('Durchschnittliche Mittagsgäste pro Tag'),
  operatingDays: z.number().min(1).max(7).describe('Betriebstage pro Woche'),
  hasBreakfastService: z.boolean(),
  hasSnackService: z.boolean(),
  subsidyPercentage: percentageSchema
    .describe('Subventionierung durch Arbeitgeber in %')
    .optional(),
  externalGuestsAllowed: z.boolean()
});

/**
 * Dynamischer Customer Schema basierend auf Industry
 */
export const getCustomerSchema = (industry?: string) => {
  let schema = customerBaseSchema;
  
  switch (industry) {
    case 'hotel':
      schema = schema.merge(hotelFieldsSchema);
      break;
    case 'krankenhaus':
      schema = schema.merge(hospitalFieldsSchema);
      break;
    case 'seniorenresidenz':
      schema = schema.merge(seniorResidenceFieldsSchema);
      break;
    case 'restaurant':
      schema = schema.merge(restaurantFieldsSchema);
      break;
    case 'betriebsrestaurant':
      schema = schema.merge(companyRestaurantFieldsSchema);
      break;
  }
  
  return schema;
};

/**
 * Customer Draft Schema (erlaubt partielle Daten)
 */
export const getCustomerDraftSchema = (industry?: string) => {
  const fullSchema = getCustomerSchema(industry);
  // Macht alle Felder außer companyName optional für Drafts
  return fullSchema.partial().required({ companyName: true });
};

/**
 * Schema für Customer-Suche/Filter
 */
export const customerSearchSchema = z.object({
  searchTerm: z.string().optional(),
  status: z.enum(['DRAFT', 'ACTIVE', 'INACTIVE']).optional(),
  industry: z.string().optional(),
  assignedTo: z.string().uuid().optional(),
  createdFrom: z.string().datetime().optional(),
  createdTo: z.string().datetime().optional(),
  hasLocations: z.boolean().optional(),
  page: z.number().min(0).default(0),
  size: z.number().min(1).max(100).default(20),
  sort: z.string().optional()
});

/**
 * Type Exports
 */
export type CustomerBase = z.infer<typeof customerBaseSchema>;
export type CustomerHotel = z.infer<typeof customerBaseSchema> & z.infer<typeof hotelFieldsSchema>;
export type CustomerHospital = z.infer<typeof customerBaseSchema> & z.infer<typeof hospitalFieldsSchema>;
export type CustomerSeniorResidence = z.infer<typeof customerBaseSchema> & z.infer<typeof seniorResidenceFieldsSchema>;
export type CustomerRestaurant = z.infer<typeof customerBaseSchema> & z.infer<typeof restaurantFieldsSchema>;
export type CustomerCompanyRestaurant = z.infer<typeof customerBaseSchema> & z.infer<typeof companyRestaurantFieldsSchema>;

export type CustomerData = 
  | CustomerBase
  | CustomerHotel
  | CustomerHospital
  | CustomerSeniorResidence
  | CustomerRestaurant
  | CustomerCompanyRestaurant;