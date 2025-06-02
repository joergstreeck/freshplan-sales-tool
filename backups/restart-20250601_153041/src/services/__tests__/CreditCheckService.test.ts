/**
 * Tests for CreditCheckService
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { CreditCheckService } from '../CreditCheckService';
import type { CreditCheckRequest } from '../CreditCheckService';

describe('CreditCheckService', () => {
  let service: CreditCheckService;

  beforeEach(() => {
    service = CreditCheckService.getInstance();
  });

  describe('Credit Check', () => {
    const baseRequest: CreditCheckRequest = {
      companyName: 'Test GmbH',
      legalForm: 'gmbh',
      vatId: 'DE123456789',
      address: {
        street: 'Teststraße 1',
        postalCode: '12345',
        city: 'Berlin',
        country: 'DE'
      },
      industry: 'hotel',
      expectedVolume: 10000,
      contactPerson: {
        name: 'Max Mustermann',
        email: 'max@test.de',
        phone: '+49 30 12345678'
      }
    };

    it('should reject orders below minimum volume', async () => {
      const request = { ...baseRequest, expectedVolume: 3000 };
      const result = await service.checkCredit(request);

      expect(result.status).toBe('rejected');
      expect(result.message).toContain('Mindestbestellwert');
      expect(result.message).toContain('5.000€');
    });

    it('should approve normal volume orders', async () => {
      const request = { ...baseRequest, expectedVolume: 15000 };
      const result = await service.checkCredit(request);

      expect(result.status).toBe('approved');
      expect(result.creditLimit).toBe(15000);
      expect(result.rating).toBeLessThanOrEqual(3);
    });

    it('should require review for new customers with high volume', async () => {
      const request = { 
        ...baseRequest, 
        expectedVolume: 75000,
        registrationNumber: undefined // New customer
      };
      const result = await service.checkCredit(request);

      expect(result.status).toBe('review');
      expect(result.message).toContain('Geschäftsleitung');
    });

    it('should handle errors gracefully', async () => {
      // Test with invalid data that might cause an error
      const request = { ...baseRequest, expectedVolume: -1000 };
      const result = await service.checkCredit(request);

      // Should still return a valid response
      expect(result).toBeDefined();
      expect(result.timestamp).toBeInstanceOf(Date);
    });
  });

  describe('Rating Description', () => {
    it('should provide correct rating descriptions', () => {
      expect(service.getRatingDescription(1)).toEqual({ text: 'Sehr gut', color: 'success' });
      expect(service.getRatingDescription(5)).toEqual({ text: 'Befriedigend', color: 'warning' });
      expect(service.getRatingDescription(9)).toEqual({ text: 'Mangelhaft', color: 'danger' });
    });
  });

  describe('Payment Terms', () => {
    it('should recommend special payment terms for rejected customers', () => {
      const response = { 
        status: 'rejected' as const, 
        timestamp: new Date() 
      };
      
      expect(service.needsSpecialPaymentTerms(response)).toBe(true);
      expect(service.getRecommendedPaymentTerms(response)).toBe('Nur Vorkasse oder Barzahlung');
    });

    it('should recommend normal payment terms for good customers', () => {
      const response = { 
        status: 'approved' as const, 
        rating: 2,
        timestamp: new Date() 
      };
      
      expect(service.needsSpecialPaymentTerms(response)).toBe(false);
      expect(service.getRecommendedPaymentTerms(response)).toBe('30 Tage netto');
    });
  });

  describe('Management Approval', () => {
    it('should handle management approval requests', async () => {
      const customerData: CreditCheckRequest = {
        companyName: 'Test GmbH',
        legalForm: 'gmbh',
        vatId: 'DE123456789',
        address: {
          street: 'Teststraße 1',
          postalCode: '12345',
          city: 'Berlin',
          country: 'DE'
        },
        industry: 'hotel',
        expectedVolume: 50000,
        contactPerson: {
          name: 'Max Mustermann',
          email: 'max@test.de',
          phone: '+49 30 12345678'
        }
      };

      const request = {
        customerData,
        reason: 'Neukunde mit hohem Volumen',
        requestedBy: 'Test User',
        requestedLimit: 50000
      };

      const result = await service.requestManagementApproval(request);

      expect(result.success).toBe(true);
      expect(result.message).toContain('Geschäftsleitung');
    });
  });
});