import 'react-i18next';
import type common from './locales/de/common.json';
import type calculator from './locales/de/calculator.json';
import type customers from './locales/de/customers.json';
import type navigation from './locales/de/navigation.json';
import type errors from './locales/de/errors.json';

declare module 'react-i18next' {
  interface CustomTypeOptions {
    defaultNS: 'common';
    resources: {
      common: typeof common;
      calculator: typeof calculator;
      customers: typeof customers;
      navigation: typeof navigation;
      errors: typeof errors;
    };
  }
}
