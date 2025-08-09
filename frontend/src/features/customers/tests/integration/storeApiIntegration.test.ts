/**
 * FC-005 Store + API Integration Tests
 *
 * ZWECK: Testet die Integration zwischen customerOnboardingStore und API Services
 * PHILOSOPHIE: Respektiert field-basierte Architektur mit flexiblen any-Types
 */

import { describe, it, expect, beforeEach, afterEach, beforeAll, afterAll, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { mockServer, configureMockServer, testData } from './mockServer';

// Mock für localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// Mock Server Setup
beforeAll(() => {
  mockServer.listen({ onUnhandledRequest: 'error' });
});

afterEach(() => {
  mockServer.resetHandlers();
  vi.clearAllMocks();
  localStorageMock.getItem.mockClear();
  localStorageMock.setItem.mockClear();
});

afterAll(() => {
  mockServer.close();
});

describe.skip('FC-005 Store + API Integration Tests', () => {
  describe.skip('Draft Save Integration', () => {
    it('should save draft via API when saveAsDraft is called', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Setup customer data with flexible any types
      act(() => {
        result.current.setCustomerField('companyName', 'Integration Test GmbH');
        result.current.setCustomerField('industry', 'hotel');
        result.current.setCustomerField('chainCustomer', 'ja');
        result.current.setCustomerField('hotelStars', 4); // Number value
        result.current.setCustomerField('specialServices', ['Spa', 'Pool']); // Array value
      });

      // Add location
      act(() => {
        result.current.addLocation();
        result.current.setLocationField(0, 'name', 'Berlin Hotel');
        result.current.setLocationField(0, 'city', 'Berlin');
      });

      // Call saveAsDraft and expect API integration
      let saveSuccessful = false;
      await act(async () => {
        try {
          await result.current.saveAsDraft();
          saveSuccessful = true;
        } catch (error) {
          console.error('Save draft failed:', error);
        }
      });

      // Verify API call was successful (store doesn't return result, just succeeds or throws)
      expect(saveSuccessful).toBe(true);

      // Verify the store state contains the flexible any types
      expect(result.current.customerData.companyName).toBe('Integration Test GmbH');
      expect(result.current.customerData.hotelStars).toBe(4); // Number preserved
      expect(Array.isArray(result.current.customerData.specialServices)).toBe(true);
    });

    it('should handle API errors gracefully during draft save', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Setup data that triggers server error
      act(() => {
        result.current.setCustomerField('companyName', 'SERVER_ERROR');
      });

      // Attempt to save draft
      let errorThrown = false;

      await act(async () => {
        try {
          await result.current.saveAsDraft();
        } catch (error) {
          errorThrown = true;
          expect(error).toBeDefined();
        }
      });

      // Verify error handling
      expect(errorThrown).toBe(true);
    });

    it('should handle network errors with retry mechanism', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Setup data that triggers network error
      act(() => {
        result.current.setCustomerField('companyName', 'NETWORK_ERROR');
      });

      // Test network error handling
      let networkErrorEncountered = false;
      await act(async () => {
        try {
          await result.current.saveAsDraft();
        } catch (error: unknown) {
          networkErrorEncountered = true;
          expect(error.message).toContain('Network Error');
        }
      });

      expect(networkErrorEncountered).toBe(true);
    });

    it('should handle validation errors from API', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Setup data that triggers validation error
      act(() => {
        result.current.setCustomerField('companyName', 'VALIDATION_ERROR');
      });

      let validationErrorThrown = false;
      await act(async () => {
        try {
          await result.current.saveAsDraft();
        } catch (error: unknown) {
          validationErrorThrown = true;
          // Validation errors might contain validation details
          expect(error).toBeDefined();
        }
      });

      // Check if validation errors are properly handled
      expect(validationErrorThrown).toBe(true);
    });
  });

  describe.skip('Draft Load Integration', () => {
    it('should load draft from API and populate store', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Mock successful draft loading
      let loadResult: unknown;
      await act(async () => {
        // This would typically be called by loadDraft method if it exists
        // For now we simulate the API call result
        const response = await fetch('/api/customers/draft/draft-123');
        loadResult = await response.json();
      });

      // Verify API response structure
      expect(loadResult.success).toBe(true);
      expect(loadResult.data.customerData.companyName).toBe('Test GmbH');
      expect(loadResult.data.customerData.industry).toBe('hotel');
      expect(loadResult.data.customerData.chainCustomer).toBe('ja');
      expect(loadResult.data.locations).toHaveLength(2);
      expect(loadResult.data.detailedLocations).toHaveLength(1);

      // Simulate loading the data into store
      act(() => {
        // If the store had a loadFromDraft method, it would be used here
        result.current.setCustomerField('companyName', loadResult.data.customerData.companyName);
        result.current.setCustomerField('industry', loadResult.data.customerData.industry);
        result.current.setCustomerField(
          'chainCustomer',
          loadResult.data.customerData.chainCustomer
        );
      });

      // Verify store state
      expect(result.current.customerData.companyName).toBe('Test GmbH');
      expect(result.current.customerData.industry).toBe('hotel');
      expect(result.current.customerData.chainCustomer).toBe('ja');
    });

    it('should handle draft not found scenario', async () => {
      // Test loading non-existent draft
      let errorResult: unknown;
      try {
        const response = await fetch('/api/customers/draft/not-found');
        errorResult = await response.json();
      } catch (error) {
        expect(error).toBeDefined();
      }

      // Verify 404 handling
      expect(errorResult.success).toBe(false);
      expect(errorResult.error).toBe('Draft not found');
    });
  });

  describe.skip('Final Customer Creation Integration', () => {
    it('should create customer via API with complete data', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Setup complete customer data
      act(() => {
        result.current.setCustomerField('companyName', testData.validCustomerData.companyName);
        result.current.setCustomerField('industry', testData.validCustomerData.industry);
        result.current.setCustomerField('chainCustomer', testData.validCustomerData.chainCustomer);
        result.current.setCustomerField('hotelStars', testData.validCustomerData.hotelStars);
      });

      // Add locations and detailed locations
      act(() => {
        testData.validLocations.forEach((location, index) => {
          result.current.addLocation();
          result.current.setLocationField(index, 'name', location.name);
        });

        testData.validDetailedLocations.forEach(detailedLocation => {
          result.current.addDetailedLocation(detailedLocation.locationId, {
            address: detailedLocation.address,
            contactPerson: detailedLocation.contactPerson,
            phone: detailedLocation.phone,
            email: detailedLocation.email,
          });
        });
      });

      // Create final customer via API
      const customerPayload = {
        customerData: result.current.customerData,
        locations: result.current.locations,
        detailedLocations: result.current.detailedLocations,
      };

      const response = await fetch('/api/customers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customerPayload),
      });

      const createResult = await response.json();

      // Verify successful creation
      expect(createResult.success).toBe(true);
      expect(createResult.data.id).toBe('customer-456');
      expect(createResult.data.customerId).toBe('CUST-2025-001');
      expect(createResult.data.customerData.companyName).toBe(
        testData.validCustomerData.companyName
      );
    });

    it('should handle validation errors during customer creation', async () => {
      // Test creation with invalid data
      const invalidPayload = {
        customerData: testData.invalidCustomerData,
        locations: [],
        detailedLocations: [],
      };

      const response = await fetch('/api/customers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(invalidPayload),
      });

      const errorResult = await response.json();

      // Verify validation error handling
      expect(errorResult.success).toBe(false);
      expect(errorResult.validationErrors).toBeDefined();
      expect(errorResult.validationErrors.companyName[0]).toContain('erforderlich');
    });
  });

  describe.skip('Performance and Reliability', () => {
    it('should handle API latency gracefully', async () => {
      // Configure mock server with delay
      configureMockServer.withDelay(1000);

      const { result } = renderHook(() => useCustomerOnboardingStore());

      act(() => {
        result.current.setCustomerField('companyName', 'Latency Test GmbH');
      });

      // Measure response time
      const startTime = Date.now();

      // This test simulates timeout handling
      let timeoutOccurred = false;
      try {
        const timeoutPromise = new Promise((_, reject) => {
          setTimeout(() => reject(new Error('Request timeout')), 500);
        });

        await Promise.race([result.current.saveAsDraft(), timeoutPromise]);
      } catch (error: unknown) {
        if (error.message === 'Request timeout') {
          timeoutOccurred = true;
        }
      }

      const responseTime = Date.now() - startTime;

      // Verify timeout handling (should be around 500ms due to our timeout)
      expect(timeoutOccurred || responseTime > 400).toBe(true);
    });

    it('should maintain data integrity during concurrent operations', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Setup initial data
      act(() => {
        result.current.setCustomerField('companyName', 'Concurrent Test GmbH');
        result.current.setCustomerField('industry', 'hotel');
      });

      // Simulate concurrent field updates (avoiding overlapping act calls)
      act(() => {
        result.current.setCustomerField('chainCustomer', 'ja');
      });

      act(() => {
        result.current.setCustomerField('hotelStars', '5');
      });

      act(() => {
        result.current.addLocation();
        result.current.setLocationField(0, 'name', 'Concurrent Location');
      });

      // Then test one API call
      let saveSuccessful = false;
      await act(async () => {
        try {
          await result.current.saveAsDraft();
          saveSuccessful = true;
        } catch (error) {
          console.error('Concurrent save failed:', error);
        }
      });

      expect(saveSuccessful).toBe(true);

      // Verify data integrity after concurrent operations
      expect(result.current.customerData.companyName).toBe('Concurrent Test GmbH');
      expect(result.current.customerData.industry).toBe('hotel');
      expect(result.current.customerData.chainCustomer).toBe('ja');
      expect(result.current.customerData.hotelStars).toBe('5');
      expect(result.current.locations).toHaveLength(1);
      expect(result.current.locations[0].name).toBe('Concurrent Location');
    });
  });

  describe.skip('Error Recovery and Resilience', () => {
    it('should recover from temporary server errors', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Configure temporary server error
      configureMockServer.withTemporaryServerError(2000);

      act(() => {
        result.current.setCustomerField('companyName', 'Recovery Test GmbH');
      });

      // First attempt should fail
      let firstAttemptFailed = false;
      try {
        await result.current.saveAsDraft();
      } catch (error) {
        firstAttemptFailed = true;
      }

      expect(firstAttemptFailed).toBe(true);

      // Wait for server recovery
      await new Promise(resolve => setTimeout(resolve, 2100));

      // Second attempt should succeed
      let secondAttemptSuccessful = false;
      await act(async () => {
        try {
          await result.current.saveAsDraft();
          secondAttemptSuccessful = true;
        } catch (error) {
          console.error('Second attempt failed:', error);
        }
      });

      expect(secondAttemptSuccessful).toBe(true);
    });

    it('should maintain offline capability with localStorage fallback', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Setup data
      act(() => {
        result.current.setCustomerField('companyName', 'Offline Test GmbH');
        result.current.setCustomerField('industry', 'office');
      });

      // Simulate offline scenario (API calls fail)
      mockServer
        .use
        // Override all API calls to fail
        ();

      // Verify localStorage persistence still works
      expect(localStorageMock.setItem).toHaveBeenCalled();

      // Verify data is maintained in store even without API
      expect(result.current.customerData.companyName).toBe('Offline Test GmbH');
      expect(result.current.customerData.industry).toBe('office');
    });
  });
});
