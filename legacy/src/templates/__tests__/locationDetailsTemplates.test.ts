import { describe, it, expect } from 'vitest';
import {
  createLocationDetailCard,
  createLocationHeader,
  createLocationNameFields,
  createLocationAddressFields,
  createLocationContactFields,
  type LocationDetail
} from '../locationDetailsTemplates';

describe('Location Details Templates', () => {
  const mockLocation: LocationDetail = {
    id: 123456,
    name: 'Test Location',
    street: 'Teststraße 123',
    postalCode: '12345',
    city: 'Teststadt',
    contactName: 'Max Mustermann',
    contactPhone: '+49 123 456789',
    contactEmail: 'test@example.com',
    category: 'hauptstandort'
  };

  describe('createLocationDetailCard', () => {
    it('should create a complete location card with all sections', () => {
      const result = createLocationDetailCard(mockLocation, 1);
      
      expect(result).toContain('location-detail-card');
      expect(result).toContain(`data-location-id="${mockLocation.id}"`);
      expect(result).toContain('Standort'); // Should contain header
      expect(result).toContain('location-name'); // Should contain name fields
      expect(result).toContain('location-street'); // Should contain address fields
      expect(result).toContain('location-contact-name'); // Should contain contact fields
    });
  });

  describe('createLocationHeader', () => {
    it('should create header with correct index and remove button', () => {
      const result = createLocationHeader(3, 123456);
      
      expect(result).toContain('data-i18n="locationDetails.locationTitle"');
      expect(result).toContain('Standort</span> 3');
      expect(result).toContain('onclick="removeLocationDetail(123456)"');
      expect(result).toContain('data-i18n="common.remove"');
      expect(result).toContain('btn-danger');
    });
  });

  describe('createLocationNameFields', () => {
    it('should create name and category fields with i18n attributes', () => {
      const result = createLocationNameFields(mockLocation);
      
      expect(result).toContain('data-i18n="locationDetails.locationName"');
      expect(result).toContain('value="Test Location"');
      expect(result).toContain('data-field="name"');
      
      expect(result).toContain('data-i18n="locationDetails.category"');
      expect(result).toContain('data-i18n="common.pleaseSelect"');
      expect(result).toContain('data-i18n="locationDetails.mainLocation"');
      expect(result).toContain('data-i18n="locationDetails.branch"');
      expect(result).toContain('data-i18n="locationDetails.externalOffice"');
      expect(result).toContain('selected'); // hauptstandort should be selected
    });

    it('should handle empty category', () => {
      const locationWithoutCategory = { ...mockLocation, category: '' };
      const result = createLocationNameFields(locationWithoutCategory);
      
      expect(result).not.toContain('selected');
    });
  });

  describe('createLocationAddressFields', () => {
    it('should create address fields with i18n attributes', () => {
      const result = createLocationAddressFields(mockLocation);
      
      expect(result).toContain('data-i18n="locationDetails.streetAndNumber"');
      expect(result).toContain('value="Teststraße 123"');
      expect(result).toContain('data-field="street"');
      
      expect(result).toContain('data-i18n="locationDetails.postalCode"');
      expect(result).toContain('value="12345"');
      expect(result).toContain('maxlength="5"');
      
      expect(result).toContain('data-i18n="locationDetails.city"');
      expect(result).toContain('value="Teststadt"');
    });
  });

  describe('createLocationContactFields', () => {
    it('should create contact fields with i18n attributes', () => {
      const result = createLocationContactFields(mockLocation);
      
      expect(result).toContain('data-i18n="locationDetails.contactName"');
      expect(result).toContain('value="Max Mustermann"');
      
      expect(result).toContain('data-i18n="locationDetails.contactPhone"');
      expect(result).toContain('value="+49 123 456789"');
      expect(result).toContain('type="tel"');
      
      expect(result).toContain('data-i18n="locationDetails.contactEmail"');
      expect(result).toContain('value="test@example.com"');
      expect(result).toContain('type="email"');
    });
  });

  describe('XSS Protection', () => {
    it('should escape HTML in location values', () => {
      const maliciousLocation: LocationDetail = {
        ...mockLocation,
        name: '<script>alert("XSS")</script>',
        street: '<img src=x onerror=alert("XSS")>',
        contactName: '"><script>alert("XSS")</script>'
      };
      
      const result = createLocationDetailCard(maliciousLocation, 1);
      
      // Should escape dangerous characters
      expect(result).not.toContain('<script>');
      expect(result).not.toContain('onerror=');
      expect(result).toContain('&lt;script&gt;');
      expect(result).toContain('&lt;img');
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty location data', () => {
      const emptyLocation: LocationDetail = {
        id: 789,
        name: '',
        street: '',
        postalCode: '',
        city: '',
        contactName: '',
        contactPhone: '',
        contactEmail: '',
        category: ''
      };
      
      const result = createLocationDetailCard(emptyLocation, 1);
      
      expect(result).toContain('value=""');
      expect(result).not.toContain('selected'); // No category selected
    });
  });
});