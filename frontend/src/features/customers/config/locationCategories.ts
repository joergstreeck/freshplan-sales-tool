/**
 * FC-005 Location Categories Configuration
 * 
 * Zentrale Konfiguration für Location-Kategorien, Icons und Labels.
 * Externalisiert aus DetailedLocationsStep.tsx für bessere Wartbarkeit.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/steps/DetailedLocationsStep.tsx
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/03-location-management.md
 */

import React from 'react';
import {
  Restaurant as RestaurantIcon,
  LocalHospital as MedicalIcon,
  Kitchen as KitchenIcon,
  Business as BusinessIcon,
  Room as RoomIcon
} from '@mui/icons-material';
import { DetailedLocationCategory } from '../types/location.types';

/**
 * Icon mapping for location categories
 * 
 * @remarks
 * - Icons werden als Funktionen definiert die React-Elemente zurückgeben
 * - Verwendet Material-UI Icons für konsistentes Design
 * - Restaurant Icon wird für mehrere Food-Service Kategorien verwendet
 */
export const categoryIcons: Record<DetailedLocationCategory, () => React.ReactElement> = {
  restaurant: () => React.createElement(RestaurantIcon),
  cafeteria: () => React.createElement(RestaurantIcon),
  kiosk: () => React.createElement(RestaurantIcon),
  station: () => React.createElement(MedicalIcon),
  kitchen: () => React.createElement(KitchenIcon),
  storage: () => React.createElement(BusinessIcon),
  other: () => React.createElement(RoomIcon)
};

/**
 * Category labels in German
 * 
 * @remarks
 * - Deutsche Labels für die Benutzeroberfläche
 * - Konsistente Benennung über die gesamte Anwendung
 * - Station/Abteilung für flexiblen Einsatz in verschiedenen Branchen
 */
export const categoryLabels: Record<DetailedLocationCategory, string> = {
  restaurant: 'Restaurant',
  cafeteria: 'Cafeteria',
  kiosk: 'Kiosk',
  station: 'Station/Abteilung',
  kitchen: 'Küche',
  storage: 'Lager',
  other: 'Sonstiges'
};

/**
 * Helper function to get icon by category
 */
export const getCategoryIcon = (category: DetailedLocationCategory): React.ReactElement => {
  const iconFunc = categoryIcons[category] || categoryIcons.other;
  return iconFunc();
};

/**
 * Helper function to get label by category
 */
export const getCategoryLabel = (category: DetailedLocationCategory): string => {
  return categoryLabels[category] || categoryLabels.other;
};

/**
 * Available categories for select dropdowns
 */
export const availableCategories: Array<{
  value: DetailedLocationCategory;
  label: string;
  icon: React.ReactElement;
}> = Object.entries(categoryLabels).map(([value, label]) => ({
  value: value as DetailedLocationCategory,
  label,
  icon: categoryIcons[value as DetailedLocationCategory]()
}));