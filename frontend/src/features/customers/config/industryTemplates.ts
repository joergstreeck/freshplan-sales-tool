/**
 * FC-005 Industry Templates Configuration
 *
 * Branchenspezifische Vorlagen für die schnelle Einrichtung von Standorten.
 * Ermöglicht es, typische Standorte einer Branche mit einem Klick hinzuzufügen.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/steps/DetailedLocationsStep.tsx
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/03-location-management.md
 */

import { DetailedLocationCategory } from '../types/location.types';
import { IndustryType } from '../types/customer.types';

/**
 * Template for a location that can be quickly added
 */
export interface LocationTemplate {
  /** Name des Standorts */
  name: string;
  /** Kategorie des Standorts */
  category: DetailedLocationCategory;
  /** Optionale Beschreibung */
  description?: string;
  /** Optionale Kapazität (z.B. Sitzplätze) */
  capacity?: number;
}

/**
 * Industry-specific templates for quick setup
 *
 * @remarks
 * - Templates sind nach Branche gruppiert
 * - Decken typische Standorte jeder Branche ab
 * - Können vom Benutzer nach dem Import angepasst werden
 * - Neue Branchen können einfach hinzugefügt werden
 */
export const industryTemplates: Partial<Record<IndustryType, LocationTemplate[]>> = {
  hotel: [
    {
      name: 'Restaurant Haupthaus',
      category: 'restaurant',
      description: 'Hauptrestaurant des Hotels',
      capacity: 120,
    },
    {
      name: 'Frühstücksraum',
      category: 'restaurant',
      description: 'Separater Bereich für Frühstück',
      capacity: 80,
    },
    {
      name: 'Bar/Lounge',
      category: 'cafeteria',
      description: 'Hotelbar mit Lounge-Bereich',
    },
    {
      name: 'Poolbar',
      category: 'kiosk',
      description: 'Außenbar am Pool',
    },
    {
      name: 'Bankett/Konferenz',
      category: 'restaurant',
      description: 'Veranstaltungsbereich',
      capacity: 200,
    },
  ],

  krankenhaus: [
    {
      name: 'Cafeteria Haupteingang',
      category: 'cafeteria',
      description: 'Öffentliche Cafeteria im Eingangsbereich',
    },
    {
      name: 'Personalrestaurant',
      category: 'restaurant',
      description: 'Restaurant für Mitarbeiter',
    },
    {
      name: 'Station 1A',
      category: 'station',
      description: 'Allgemeinstation',
    },
    {
      name: 'Station 1B',
      category: 'station',
      description: 'Allgemeinstation',
    },
    {
      name: 'Station 2A',
      category: 'station',
      description: 'Chirurgische Station',
    },
    {
      name: 'Intensivstation',
      category: 'station',
      description: 'Intensivpflege',
    },
    {
      name: 'Kiosk Eingangsbereich',
      category: 'kiosk',
      description: 'Kleiner Verkaufsbereich',
    },
  ],

  seniorenresidenz: [
    {
      name: 'Speisesaal EG',
      category: 'restaurant',
      description: 'Hauptspeisesaal im Erdgeschoss',
      capacity: 60,
    },
    {
      name: 'Wohnbereich 1',
      category: 'station',
      description: 'Betreutes Wohnen',
    },
    {
      name: 'Wohnbereich 2',
      category: 'station',
      description: 'Betreutes Wohnen',
    },
    {
      name: 'Demenzbereich',
      category: 'station',
      description: 'Spezialisierter Pflegebereich',
    },
    {
      name: 'Cafeteria',
      category: 'cafeteria',
      description: 'Gemeinschaftsbereich mit Café',
    },
  ],

  restaurant: [
    {
      name: 'Hauptrestaurant',
      category: 'restaurant',
      description: 'Hauptgastraum',
      capacity: 80,
    },
    {
      name: 'Außenbereich',
      category: 'restaurant',
      description: 'Terrasse/Biergarten',
      capacity: 40,
    },
    {
      name: 'Bar',
      category: 'cafeteria',
      description: 'Barbereich',
    },
    {
      name: 'Privatraum',
      category: 'restaurant',
      description: 'Separater Raum für Veranstaltungen',
      capacity: 30,
    },
  ],

  betriebsrestaurant: [
    {
      name: 'Hauptkantine',
      category: 'restaurant',
      description: 'Hauptspeisebereich',
      capacity: 200,
    },
    {
      name: 'Salatbar',
      category: 'restaurant',
      description: 'Self-Service Salatbereich',
    },
    {
      name: 'Coffee Corner',
      category: 'cafeteria',
      description: 'Kaffee und Snacks',
    },
    {
      name: 'Executive Dining',
      category: 'restaurant',
      description: 'Separater Bereich für Führungskräfte',
      capacity: 20,
    },
  ],
};

/**
 * Get templates for a specific industry
 */
export const getIndustryTemplates = (industry: IndustryType): LocationTemplate[] => {
  return industryTemplates[industry] || [];
};

/**
 * Check if industry has templates available
 */
export const hasIndustryTemplates = (industry: IndustryType): boolean => {
  return Boolean(industryTemplates[industry]?.length);
};

/**
 * Get all industries with available templates
 */
export const getIndustriesWithTemplates = (): IndustryType[] => {
  return Object.keys(industryTemplates) as IndustryType[];
};
