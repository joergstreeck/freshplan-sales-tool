/**
 * Simple Customer Validator
 * Validates customer data according to business rules
 */

import type { CustomerData, ValidationError } from '../types';

export interface ICustomerValidator {
  validate(data: Partial<CustomerData>): ValidationError[];
}

export class SimpleCustomerValidator implements ICustomerValidator {
  validate(data: Partial<CustomerData>): ValidationError[] {
    const errors: ValidationError[] = [];

    // Pflichtfelder
    if (!data.companyName?.trim()) {
      errors.push({
        field: 'companyName',
        message: 'Firmenname ist erforderlich'
      });
    }

    if (!data.contactEmail?.trim()) {
      errors.push({
        field: 'contactEmail',
        message: 'E-Mail ist erforderlich'
      });
    }

    // E-Mail Format
    if (data.contactEmail && !this.isValidEmail(data.contactEmail)) {
      errors.push({
        field: 'contactEmail',
        message: 'Bitte geben Sie eine gültige E-Mail-Adresse ein'
      });
    }

    // PLZ Validierung (5 Ziffern für Deutschland)
    if (data.postalCode && !/^\d{5}$/.test(data.postalCode)) {
      errors.push({
        field: 'postalCode',
        message: 'PLZ muss 5 Ziffern haben'
      });
    }

    // Telefonnummer (optional, aber wenn vorhanden dann gültig)
    if (data.contactPhone && !this.isValidPhone(data.contactPhone)) {
      errors.push({
        field: 'contactPhone',
        message: 'Bitte geben Sie eine gültige Telefonnummer ein'
      });
    }

    // Kundentyp-spezifische Validierung
    if (data.customerType === 'neukunde' && !data.customerStatus) {
      errors.push({
        field: 'customerStatus',
        message: 'Kundenstatus muss für Neukunden ausgewählt werden'
      });
    }

    return errors;
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  private isValidPhone(phone: string): boolean {
    // Erlaubt verschiedene Telefonnummern-Formate
    const phoneRegex = /^[\d\s\-\+\(\)]+$/;
    return phoneRegex.test(phone) && phone.replace(/\D/g, '').length >= 10;
  }
}