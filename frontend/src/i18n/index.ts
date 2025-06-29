import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// Import der Übersetzungen
import deCommon from './locales/de/common.json';
import deCalculator from './locales/de/calculator.json';
import deCustomers from './locales/de/customers.json';
import deNavigation from './locales/de/navigation.json';
import deErrors from './locales/de/errors.json';
import deLocations from './locales/de/locations.json';
import deLocationDetails from './locales/de/locationDetails.json';

import enCommon from './locales/en/common.json';
import enCalculator from './locales/en/calculator.json';
import enCustomers from './locales/en/customers.json';
import enNavigation from './locales/en/navigation.json';
import enErrors from './locales/en/errors.json';
import enLocations from './locales/en/locations.json';
import enLocationDetails from './locales/en/locationDetails.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: 'de',
    debug: import.meta.env.DEV,
    interpolation: {
      escapeValue: false, // React schützt bereits vor XSS
    },
    resources: {
      de: {
        common: deCommon,
        calculator: deCalculator,
        customers: deCustomers,
        navigation: deNavigation,
        errors: deErrors,
        locations: deLocations,
        locationDetails: deLocationDetails,
      },
      en: {
        common: enCommon,
        calculator: enCalculator,
        customers: enCustomers,
        navigation: enNavigation,
        errors: enErrors,
        locations: enLocations,
        locationDetails: enLocationDetails,
      },
    },
    detection: {
      order: ['localStorage', 'navigator', 'htmlTag'],
      caches: ['localStorage'],
    },
  });

export default i18n;
