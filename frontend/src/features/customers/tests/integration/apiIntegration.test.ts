/**
 * FC-005 API Integration Tests (Simplified)
 *
 * ZWECK: Testet die API-Integration ohne komplexe Store-Abhängigkeiten
 * PHILOSOPHIE: Fokus auf API Contract und Response Handling
 */

import { describe, it, expect, beforeEach, afterEach, beforeAll, afterAll, vi } from 'vitest';
import { mockServer, testData } from './mockServer';

// Mock Server Setup
beforeAll(() => {
  mockServer.listen({ onUnhandledRequest: 'error' });
});

afterEach(() => {
  mockServer.resetHandlers();
  vi.clearAllMocks();
});

afterAll(() => {
  mockServer.close();
});

// Simulate API Client (simplified version of what the real store would use)
class TestApiClient {
  async post<T>(endpoint: string, data: any): Promise<T> {
    const response = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return await response.json();
  }

  async get<T>(endpoint: string): Promise<T> {
    const response = await fetch(endpoint);

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return await response.json();
  }
}

const apiClient = new TestApiClient();

describe('FC-005 API Integration Tests (Simplified)', () => {
  describe('Customer Draft API Integration', () => {
    it('should save customer draft with flexible any types', async () => {
      const draftData = {
        customerData: {
          companyName: 'API Test GmbH',
          industry: 'hotel',
          chainCustomer: 'ja',
          hotelStars: 4, // Number value
          amenities: ['Pool', 'Spa', 'Gym'], // Array value
          preferences: {
            // Object value
            newsletter: true,
            marketing: false,
          },
        },
        locations: [
          { id: 'loc-1', name: 'Berlin' },
          { id: 'loc-2', name: 'Hamburg' },
        ],
        detailedLocations: [],
      };

      const result = await apiClient.post<any>('/api/customers/draft', draftData);

      expect(result.success).toBe(true);
      expect(result.data.draftId).toBe('draft-123');
      expect(result.data.customerData.companyName).toBe('API Test GmbH');
      expect(result.data.customerData.hotelStars).toBe(4); // Number preserved
      expect(Array.isArray(result.data.customerData.amenities)).toBe(true);
      expect(result.data.customerData.preferences.newsletter).toBe(true);
    });

    it('should handle validation errors from draft save', async () => {
      const invalidData = {
        customerData: {
          companyName: 'VALIDATION_ERROR', // Triggers validation error in mock
        },
      };

      try {
        await apiClient.post('/api/customers/draft', invalidData);
        expect.fail('Should have thrown validation error');
      } catch (error: any) {
        expect(error.message).toContain('HTTP 400');
      }
    });

    it('should handle server errors during draft save', async () => {
      const serverErrorData = {
        customerData: {
          companyName: 'SERVER_ERROR', // Triggers server error in mock
        },
      };

      try {
        await apiClient.post('/api/customers/draft', serverErrorData);
        expect.fail('Should have thrown server error');
      } catch (error: any) {
        expect(error.message).toContain('HTTP 500');
      }
    });

    it('should handle network errors during draft save', async () => {
      const networkErrorData = {
        customerData: {
          companyName: 'NETWORK_ERROR', // Triggers network error in mock
        },
      };

      try {
        await apiClient.post('/api/customers/draft', networkErrorData);
        expect.fail('Should have thrown network error');
      } catch (error: any) {
        expect(error).toBeDefined();
      }
    });
  });

  describe('Customer Draft Loading Integration', () => {
    it('should load existing draft with all data intact', async () => {
      const result = await apiClient.get<any>('/api/customers/draft/draft-123');

      expect(result.success).toBe(true);
      expect(result.data.draftId).toBe('draft-123');
      expect(result.data.customerData.companyName).toBe('Test GmbH');
      expect(result.data.customerData.industry).toBe('hotel');
      expect(result.data.customerData.chainCustomer).toBe('ja');
      expect(result.data.locations).toHaveLength(2);
      expect(result.data.detailedLocations).toHaveLength(1);

      // Verify location structure
      expect(result.data.locations[0]).toHaveProperty('id');
      expect(result.data.locations[0]).toHaveProperty('name');

      // Verify detailed location structure
      expect(result.data.detailedLocations[0]).toHaveProperty('id');
      expect(result.data.detailedLocations[0]).toHaveProperty('locationId');
      expect(result.data.detailedLocations[0]).toHaveProperty('address');
      expect(result.data.detailedLocations[0]).toHaveProperty('contactPerson');
    });

    it('should handle draft not found scenario', async () => {
      try {
        await apiClient.get('/api/customers/draft/not-found');
        expect.fail('Should have thrown not found error');
      } catch (error: any) {
        expect(error.message).toContain('HTTP 404');
      }
    });
  });

  describe('Final Customer Creation Integration', () => {
    it('should create final customer with complete data', async () => {
      const customerData = {
        customerData: testData.validCustomerData,
        locations: testData.validLocations,
        detailedLocations: testData.validDetailedLocations,
      };

      const result = await apiClient.post<any>('/api/customers', customerData);

      expect(result.success).toBe(true);
      expect(result.data.id).toBe('customer-456');
      expect(result.data.customerId).toBe('CUST-2025-001');
      expect(result.data.customerData.companyName).toBe(testData.validCustomerData.companyName);
      expect(result.data.locations).toEqual(testData.validLocations);
      expect(result.data.detailedLocations).toEqual(testData.validDetailedLocations);
      expect(result.data.createdAt).toBeDefined();
    });

    it('should validate required fields during customer creation', async () => {
      const invalidCustomerData = {
        customerData: testData.invalidCustomerData,
        locations: [],
        detailedLocations: [],
      };

      try {
        await apiClient.post('/api/customers', invalidCustomerData);
        expect.fail('Should have thrown validation error');
      } catch (error: any) {
        expect(error.message).toContain('HTTP 400');
      }
    });
  });

  describe('Field Definitions API Integration', () => {
    it('should load field definitions for customer entity', async () => {
      const result = await apiClient.get<any>('/api/field-definitions?entityType=customer');

      expect(result.success).toBe(true);
      expect(Array.isArray(result.data)).toBe(true);
      expect(result.data.length).toBeGreaterThan(0);

      // Verify field structure
      const companyNameField = result.data.find((f: any) => f.key === 'companyName');
      expect(companyNameField).toBeDefined();
      expect(companyNameField.fieldType).toBe('text');
      expect(companyNameField.required).toBe(true);
      expect(companyNameField.validationRules).toBeDefined();
    });

    it('should filter field definitions by industry', async () => {
      const hotelFields = await apiClient.get<any>(
        '/api/field-definitions?entityType=customer&industry=hotel'
      );

      expect(hotelFields.success).toBe(true);

      // Should include hotel-specific fields
      const hotelStarsField = hotelFields.data.find((f: any) => f.key === 'hotelStars');
      expect(hotelStarsField).toBeDefined();
      expect(hotelStarsField.industryFilter).toBe('hotel');

      // Verify general fields are still included
      expect(hotelFields.data.some((f: any) => f.key === 'companyName')).toBe(true);
      expect(hotelFields.data.some((f: any) => f.key === 'industry')).toBe(true);
    });

    it('should provide wizard step triggers in field definitions', async () => {
      const result = await apiClient.get<any>('/api/field-definitions?entityType=customer');

      const chainCustomerField = result.data.find((f: any) => f.key === 'chainCustomer');
      expect(chainCustomerField).toBeDefined();
      expect(chainCustomerField.triggerWizardStep).toBeDefined();
      expect(chainCustomerField.triggerWizardStep.step).toBe('locations');
      expect(chainCustomerField.triggerWizardStep.when).toBe('ja');
    });
  });

  describe('API Error Handling and Resilience', () => {
    it('should handle multiple rapid API calls', async () => {
      const startTime = Date.now();

      // Make multiple concurrent API calls
      const promises = [
        apiClient.get('/api/field-definitions?entityType=customer'),
        apiClient.get('/api/field-definitions?entityType=customer&industry=hotel'),
        apiClient.get('/api/customers/draft/draft-123'),
        apiClient.get('/api/health'),
        apiClient.post('/api/customers/draft', {
          customerData: { companyName: 'Concurrent Test' },
        }),
      ];

      const results = await Promise.all(promises);

      const endTime = Date.now();
      const totalTime = endTime - startTime;

      // All requests should complete successfully
      expect(results.length).toBe(5);
      results.forEach(result => {
        expect(result).toBeDefined();
      });

      // Should complete within reasonable time
      expect(totalTime).toBeLessThan(3000);
    });

    it('should maintain data integrity in API responses', async () => {
      // Test that complex data structures are preserved
      const complexData = {
        customerData: {
          companyName: 'Complex Data Test GmbH',
          customFields: {
            nested: {
              deep: {
                value: 'preserved',
              },
            },
            array: [1, 'two', { three: 3 }],
            nullValue: null,
            booleanTrue: true,
            booleanFalse: false,
            numberZero: 0,
            emptyString: '',
          },
        },
      };

      const result = await apiClient.post<any>('/api/customers/draft', complexData);

      expect(result.success).toBe(true);

      // Verify complex data preservation
      const savedData = result.data.customerData;
      expect(savedData.customFields.nested.deep.value).toBe('preserved');
      expect(savedData.customFields.array).toEqual([1, 'two', { three: 3 }]);
      expect(savedData.customFields.nullValue).toBeNull();
      expect(savedData.customFields.booleanTrue).toBe(true);
      expect(savedData.customFields.booleanFalse).toBe(false);
      expect(savedData.customFields.numberZero).toBe(0);
      expect(savedData.customFields.emptyString).toBe('');
    });

    it('should handle health check for API availability', async () => {
      const result = await apiClient.get<any>('/api/health');

      expect(result.status).toBe('ok');
      expect(result.timestamp).toBeDefined();

      // Verify timestamp is recent
      const timestamp = new Date(result.timestamp);
      const now = new Date();
      const timeDiff = now.getTime() - timestamp.getTime();
      expect(timeDiff).toBeLessThan(10000); // Within 10 seconds
    });
  });

  describe('Real-World Integration Scenarios', () => {
    it('should support complete customer onboarding workflow', async () => {
      // Step 1: Load field definitions
      const fieldDefs = await apiClient.get<any>(
        '/api/field-definitions?entityType=customer&industry=hotel'
      );
      expect(fieldDefs.success).toBe(true);

      // Step 2: Save initial draft
      const initialDraft = {
        customerData: {
          companyName: 'Workflow Test Hotel',
          industry: 'hotel',
        },
      };
      const draftResult = await apiClient.post<any>('/api/customers/draft', initialDraft);
      expect(draftResult.success).toBe(true);

      // Step 3: Update draft with more data
      const updatedDraft = {
        customerData: {
          ...initialDraft.customerData,
          chainCustomer: 'ja',
          hotelStars: '4',
        },
        locations: [{ id: 'loc-1', name: 'Berlin Hotel' }],
      };
      const updateResult = await apiClient.post<any>('/api/customers/draft', updatedDraft);
      expect(updateResult.success).toBe(true);

      // Step 4: Finalize customer
      const finalCustomer = {
        ...updatedDraft,
        detailedLocations: [
          {
            id: 'detail-1',
            locationId: 'loc-1',
            address: 'Hotel Str. 123',
            contactPerson: 'Max Manager',
            phone: '+49 30 123456',
          },
        ],
      };
      const finalResult = await apiClient.post<any>('/api/customers', finalCustomer);
      expect(finalResult.success).toBe(true);
      expect(finalResult.data.id).toBeDefined();
      expect(finalResult.data.customerId).toBeDefined();
    });

    it('should handle chain customer vs single customer scenarios', async () => {
      // Single customer scenario
      const singleCustomer = {
        customerData: {
          companyName: 'Single Location Restaurant',
          industry: 'hotel',
          chainCustomer: 'nein',
        },
        locations: [], // Empty for single customer
        detailedLocations: [],
      };

      const singleResult = await apiClient.post<any>('/api/customers', singleCustomer);
      expect(singleResult.success).toBe(true);

      // Chain customer scenario
      const chainCustomer = {
        customerData: {
          companyName: 'Chain Restaurant Group',
          industry: 'hotel',
          chainCustomer: 'ja',
        },
        locations: [
          { id: 'loc-1', name: 'Location Berlin' },
          { id: 'loc-2', name: 'Location Munich' },
        ],
        detailedLocations: [
          {
            id: 'detail-1',
            locationId: 'loc-1',
            address: 'Berlin Address',
            contactPerson: 'Berlin Manager',
            phone: '+49 30 111111',
          },
          {
            id: 'detail-2',
            locationId: 'loc-2',
            address: 'Munich Address',
            contactPerson: 'Munich Manager',
            phone: '+49 89 222222',
          },
        ],
      };

      const chainResult = await apiClient.post<any>('/api/customers', chainCustomer);
      expect(chainResult.success).toBe(true);
      expect(chainResult.data.locations).toHaveLength(2);
      expect(chainResult.data.detailedLocations).toHaveLength(2);
    });
  });
});
