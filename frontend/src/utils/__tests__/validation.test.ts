/**
 * Validation Utils Tests
 *
 * Tests for email, phone, postal code, and general field validation.
 */

import { describe, it, expect } from 'vitest';
import {
  isValidEmail,
  isValidGermanPostalCode,
  isValidPhoneNumber,
  isRequired,
  isValidNumber,
  validationMessages,
} from '../validation';

describe('Validation Utils', () => {
  describe('isValidEmail', () => {
    it('should accept valid email addresses', () => {
      expect(isValidEmail('test@example.com')).toBe(true);
      expect(isValidEmail('user.name@domain.co.uk')).toBe(true);
      expect(isValidEmail('firstname+lastname@company.de')).toBe(true);
      expect(isValidEmail('a@b.c')).toBe(true);
    });

    it('should reject invalid email addresses', () => {
      expect(isValidEmail('invalid')).toBe(false);
      expect(isValidEmail('invalid@')).toBe(false);
      expect(isValidEmail('@domain.com')).toBe(false);
      expect(isValidEmail('test @example.com')).toBe(false);
      expect(isValidEmail('test@example')).toBe(false);
      expect(isValidEmail('')).toBe(false);
    });

    it('should reject emails with spaces', () => {
      expect(isValidEmail('test @example.com')).toBe(false);
      expect(isValidEmail('test@ example.com')).toBe(false);
      expect(isValidEmail(' test@example.com')).toBe(false);
    });
  });

  describe('isValidGermanPostalCode', () => {
    it('should accept valid 5-digit German postal codes', () => {
      expect(isValidGermanPostalCode('10115')).toBe(true); // Berlin
      expect(isValidGermanPostalCode('80331')).toBe(true); // München
      expect(isValidGermanPostalCode('20095')).toBe(true); // Hamburg
      expect(isValidGermanPostalCode('01067')).toBe(true); // Dresden
    });

    it('should reject invalid postal codes', () => {
      expect(isValidGermanPostalCode('1234')).toBe(false); // Too short
      expect(isValidGermanPostalCode('123456')).toBe(false); // Too long
      expect(isValidGermanPostalCode('abcde')).toBe(false); // Letters
      expect(isValidGermanPostalCode('1234a')).toBe(false); // Mixed
      expect(isValidGermanPostalCode('')).toBe(false); // Empty
      expect(isValidGermanPostalCode('12 345')).toBe(false); // With space
    });
  });

  describe('isValidPhoneNumber', () => {
    it('should accept valid phone numbers', () => {
      expect(isValidPhoneNumber('+49 123 456789')).toBe(true);
      expect(isValidPhoneNumber('0123456789')).toBe(true);
      expect(isValidPhoneNumber('+49-123-456789')).toBe(true);
      expect(isValidPhoneNumber('+49 (123) 456789')).toBe(true);
      expect(isValidPhoneNumber('+1234567')).toBe(true); // Min 7 digits
      expect(isValidPhoneNumber('+123456789012345')).toBe(true); // Max 15 digits
    });

    it('should reject invalid phone numbers', () => {
      expect(isValidPhoneNumber('123456')).toBe(false); // Too short
      expect(isValidPhoneNumber('+1234567890123456')).toBe(false); // Too long
      expect(isValidPhoneNumber('abc123')).toBe(false); // Contains letters
      expect(isValidPhoneNumber('')).toBe(false); // Empty
      expect(isValidPhoneNumber('++')).toBe(false); // Multiple +
    });

    it('should handle phone numbers with formatting', () => {
      expect(isValidPhoneNumber('(030) 12345678')).toBe(true);
      expect(isValidPhoneNumber('030-12345678')).toBe(true);
      expect(isValidPhoneNumber('030 123 456 78')).toBe(true);
    });
  });

  describe('isRequired', () => {
    it('should accept non-empty strings', () => {
      expect(isRequired('test')).toBe(true);
      expect(isRequired('a')).toBe(true);
      expect(isRequired(' test ')).toBe(true); // Trimmed to 'test'
    });

    it('should reject empty strings', () => {
      expect(isRequired('')).toBe(false);
      expect(isRequired('   ')).toBe(false); // Only whitespace
      expect(isRequired('\t')).toBe(false);
      expect(isRequired('\n')).toBe(false);
    });
  });

  describe('isValidNumber', () => {
    it('should accept valid positive numbers', () => {
      expect(isValidNumber('0')).toBe(true);
      expect(isValidNumber('42')).toBe(true);
      expect(isValidNumber('123.45')).toBe(true);
      expect(isValidNumber('1000')).toBe(true);
    });

    it('should reject negative numbers', () => {
      expect(isValidNumber('-1')).toBe(false);
      expect(isValidNumber('-123.45')).toBe(false);
    });

    it('should reject non-numeric strings', () => {
      expect(isValidNumber('abc')).toBe(false);
      expect(isValidNumber('12abc')).toBe(false);
      expect(isValidNumber('')).toBe(false);
      expect(isValidNumber(' ')).toBe(false);
    });

    it('should handle edge cases', () => {
      expect(isValidNumber('0.0')).toBe(true);
      expect(isValidNumber('000')).toBe(true); // Parses to 0
    });
  });

  describe('validationMessages', () => {
    it('should have German messages for all validation types', () => {
      expect(validationMessages.email.de).toBe('Bitte geben Sie eine gültige E-Mail-Adresse ein');
      expect(validationMessages.postalCode.de).toBe(
        'Bitte geben Sie eine gültige 5-stellige PLZ ein'
      );
      expect(validationMessages.phone.de).toBe('Bitte geben Sie eine gültige Telefonnummer ein');
      expect(validationMessages.required.de).toBe('Dieses Feld ist erforderlich');
      expect(validationMessages.number.de).toBe('Bitte geben Sie eine gültige Zahl ein');
    });

    it('should have English messages for all validation types', () => {
      expect(validationMessages.email.en).toBe('Please enter a valid email address');
      expect(validationMessages.postalCode.en).toBe('Please enter a valid 5-digit postal code');
      expect(validationMessages.phone.en).toBe('Please enter a valid phone number');
      expect(validationMessages.required.en).toBe('This field is required');
      expect(validationMessages.number.en).toBe('Please enter a valid number');
    });

    it('should have all keys for both languages', () => {
      const keys = ['email', 'postalCode', 'phone', 'required', 'number'];
      keys.forEach(key => {
        expect(validationMessages[key as keyof typeof validationMessages]).toHaveProperty('de');
        expect(validationMessages[key as keyof typeof validationMessages]).toHaveProperty('en');
      });
    });
  });
});
