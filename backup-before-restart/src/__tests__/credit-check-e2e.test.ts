/**
 * End-to-End test for Credit Check functionality
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { CreditCheckService } from '../services/CreditCheckService';
import type { CreditCheckRequest, CreditCheckResponse } from '../services/CreditCheckService';

describe('Credit Check E2E Flow', () => {
  let creditCheckService: CreditCheckService;

  beforeEach(() => {
    creditCheckService = CreditCheckService.getInstance();
  });

  it('should handle complete new customer workflow', async () => {
    // 1. New customer with low volume - should be rejected
    const lowVolumeCustomer: CreditCheckRequest = {
      companyName: 'Kleine GmbH',
      legalForm: 'gmbh',
      vatId: 'DE987654321',
      address: {
        street: 'Kleinstraße 1',
        postalCode: '54321',
        city: 'München',
        country: 'DE'
      },
      industry: 'restaurant',
      expectedVolume: 3000,
      contactPerson: {
        name: 'Peter Klein',
        email: 'peter@kleine-gmbh.de',
        phone: '+49 89 12345678'
      }
    };

    const lowVolumeResult = await creditCheckService.checkCredit(lowVolumeCustomer);
    
    expect(lowVolumeResult.status).toBe('rejected');
    expect(lowVolumeResult.message).toContain('Mindestbestellwert');
    expect(creditCheckService.needsSpecialPaymentTerms(lowVolumeResult)).toBe(true);
    expect(creditCheckService.getRecommendedPaymentTerms(lowVolumeResult)).toBe('Nur Vorkasse oder Barzahlung');

    // 2. New customer with normal volume - should be approved
    const normalVolumeCustomer: CreditCheckRequest = {
      ...lowVolumeCustomer,
      companyName: 'Normal GmbH',
      expectedVolume: 15000
    };

    const normalResult = await creditCheckService.checkCredit(normalVolumeCustomer);
    
    expect(normalResult.status).toBe('approved');
    expect(normalResult.creditLimit).toBe(15000);
    expect(creditCheckService.needsSpecialPaymentTerms(normalResult)).toBe(false);

    // 3. New customer with high volume - should require review
    const highVolumeCustomer: CreditCheckRequest = {
      ...lowVolumeCustomer,
      companyName: 'Große GmbH',
      expectedVolume: 75000,
      registrationNumber: undefined // Explicitly new customer
    };

    const highVolumeResult = await creditCheckService.checkCredit(highVolumeCustomer);
    
    expect(highVolumeResult.status).toBe('review');
    expect(highVolumeResult.message).toContain('Geschäftsleitung');
    
    // 4. Request management approval
    const approvalRequest = {
      customerData: highVolumeCustomer,
      reason: 'Neukunde mit hohem Volumen',
      requestedBy: 'Vertrieb',
      requestedLimit: 75000
    };

    const approvalResult = await creditCheckService.requestManagementApproval(approvalRequest);
    
    expect(approvalResult.success).toBe(true);
    expect(approvalResult.message).toContain('24 Stunden');
  });

  it('should correctly categorize customers by risk', async () => {
    const testCases: Array<{
      volume: number;
      isNew: boolean;
      expectedStatus: CreditCheckResponse['status'];
      expectedSpecialTerms: boolean;
    }> = [
      { volume: 2000, isNew: true, expectedStatus: 'rejected', expectedSpecialTerms: true },
      { volume: 10000, isNew: true, expectedStatus: 'approved', expectedSpecialTerms: false },
      { volume: 60000, isNew: true, expectedStatus: 'review', expectedSpecialTerms: true },
      { volume: 10000, isNew: false, expectedStatus: 'approved', expectedSpecialTerms: false },
      { volume: 30000, isNew: false, expectedStatus: 'approved', expectedSpecialTerms: false },
    ];

    for (const testCase of testCases) {
      const customer: CreditCheckRequest = {
        companyName: `Test ${testCase.volume} GmbH`,
        legalForm: 'gmbh',
        vatId: 'DE123456789',
        registrationNumber: testCase.isNew ? undefined : 'HRB 12345',
        address: {
          street: 'Teststraße 1',
          postalCode: '12345',
          city: 'Berlin',
          country: 'DE'
        },
        industry: 'hotel',
        expectedVolume: testCase.volume,
        contactPerson: {
          name: 'Test Person',
          email: 'test@example.com',
          phone: '+49 30 12345678'
        }
      };

      const result = await creditCheckService.checkCredit(customer);
      
      expect(result.status).toBe(testCase.expectedStatus);
      expect(creditCheckService.needsSpecialPaymentTerms(result)).toBe(testCase.expectedSpecialTerms);
    }
  });

  it('should provide appropriate payment term recommendations', async () => {
    const ratings = [1, 3, 5, 7, 9];
    const expectedTerms = [
      '30 Tage netto',
      '30 Tage netto',
      '14 Tage netto',
      'Nur Vorkasse oder Barzahlung',
      'Nur Vorkasse oder Barzahlung'
    ];

    ratings.forEach((rating, index) => {
      const response: CreditCheckResponse = {
        status: 'approved',
        rating,
        timestamp: new Date()
      };

      const recommendedTerms = creditCheckService.getRecommendedPaymentTerms(response);
      expect(recommendedTerms).toBe(expectedTerms[index]);
    });
  });

  it('should handle edge cases gracefully', async () => {
    // Test with minimum valid volume
    const minVolume: CreditCheckRequest = {
      companyName: 'Min GmbH',
      legalForm: 'gmbh',
      vatId: 'DE123456789',
      address: {
        street: 'Minstraße 1',
        postalCode: '12345',
        city: 'Berlin',
        country: 'DE'
      },
      industry: 'hotel',
      expectedVolume: 5000,
      contactPerson: {
        name: 'Min Person',
        email: 'min@example.com',
        phone: '+49 30 12345678'
      }
    };

    const minResult = await creditCheckService.checkCredit(minVolume);
    expect(minResult.status).toBe('approved');
    expect(minResult.creditLimit).toBe(5000);

    // Test with exactly the threshold for review
    const thresholdVolume: CreditCheckRequest = {
      ...minVolume,
      companyName: 'Threshold GmbH',
      expectedVolume: 50001,
      registrationNumber: undefined
    };

    const thresholdResult = await creditCheckService.checkCredit(thresholdVolume);
    expect(thresholdResult.status).toBe('review');
  });
});