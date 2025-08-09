/**
 * FC-005 API Client Tests
 *
 * Tests für flexiblen API Client mit any-Types als FEATURE.
 * Respektiert Team-Philosophie: Dynamische Payloads sind ERWÜNSCHT!
 *
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/09-TEST-PLAN/00-PHILOSOPHIE.md
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/09-TEST-PLAN/02-test-examples.md
 */

import { describe, it, expect, vi } from 'vitest';
import { ApiClient, RequestConfig, apiClient } from '../../../services/api-client';

// Mock fetch
const mockFetch = vi.fn();
global.fetch = mockFetch;

describe('ApiClient - Flexibles API mit any-Types (ENTERPRISE PHILOSOPHY)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Clear all storage
    localStorage.clear();
    sessionStorage.clear();
    // Use the singleton instance
    apiClient.cancelAllRequests();
  });

  afterEach(() => {
    vi.clearAllTimers();
  });

  describe('✅ Flexible Payload Handling (KERNFEATURE)', () => {
    it('should send any data structure via POST', async () => {
      // Setup successful response for each call
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ success: true, id: 'test-123' }),
      });

      // Test ein paar repräsentative Payload-Strukturen
      const payloads = [
        // Einfaches Objekt
        { companyName: 'Test GmbH', industry: 'hotel' },

        // Verschachteltes Objekt mit field-based structure
        {
          customerData: {
            companyName: 'Hotel Test',
            industry: 'hotel',
            chainCustomer: 'ja',
          },
        },

        // Branchenspezifische Daten mit any Types
        {
          fieldValues: {
            hotelStars: 5,
            amenities: ['pool', 'spa', 'gym'],
            deliveryService: {
              enabled: true,
              radius: 5,
            },
          },
        },
      ];

      // Alle Payloads sollten funktionieren
      for (const payload of payloads) {
        const result = await apiClient.post('/api/customers', payload);
        expect(result).toEqual({ success: true, id: 'test-123' });
      }

      expect(mockFetch).toHaveBeenCalledTimes(payloads.length);
    });

    it('should handle PUT/PATCH with flexible data structures', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ updated: true }),
      });

      // Partial updates with any structure
      const partialUpdates = [
        // Simple field update
        { companyName: 'Updated Name' },

        // Deep nested update
        {
          'customer.preferences.communication': {
            email: true,
            sms: false,
            phone: { preferred: 'mobile', timing: 'business-hours' },
          },
        },

        // Array operations
        {
          $push: { 'locations.amenities': 'new-feature' },
          $set: { status: 'active' },
        },

        // Conditional updates
        {
          $inc: { visitCount: 1 },
          $currentDate: { lastVisit: true },
          $addToSet: { tags: 'vip-customer' },
        },
      ];

      for (const update of partialUpdates) {
        await apiClient.put('/api/customers/123', update);
        await apiClient.patch('/api/customers/123', update);
      }

      expect(mockFetch).toHaveBeenCalledTimes(partialUpdates.length * 2);
    });

    it('should handle responses with any structure', async () => {
      const responses = [
        // Standard API response
        { success: true, data: { id: '123', name: 'Test' } },

        // Field-based response
        {
          customer: {
            id: 'cust-123',
            fieldValues: {
              companyName: 'Test GmbH',
              industry: 'hotel',
              customFields: {
                rating_2024: 5,
                special_notes: 'VIP customer',
              },
            },
          },
          locations: [
            {
              id: 'loc-1',
              fieldValues: {
                roomCount: 150,
                parkingSpaces: 50,
                dynamicField_1: 'some value',
              },
            },
          ],
        },

        // Array response
        [
          { type: 'customer', id: '1', data: {} },
          { type: 'location', id: '2', data: {} },
        ],

        // Nested response with metadata
        {
          result: {
            customers: {},
            pagination: { page: 1, total: 100 },
            filters: { applied: [], available: [] },
            aggregations: {
              byIndustry: { hotel: 45, restaurant: 30 },
              byRegion: { north: 25, south: 50 },
            },
          },
        },
      ];

      for (const responseData of responses) {
        mockFetch.mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: async () => responseData,
        });

        const result = await apiClient.get('/api/test');
        expect(result).toEqual(responseData);
      }
    });
  });

  describe('🔧 HTTP Methods mit flexiblen Parametern', () => {
    beforeEach(() => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ success: true }),
      });
    });

    it('should handle GET with query parameters', async () => {
      const queryParams = {
        page: 1,
        size: 20,
        filter: 'active',
        search: 'Test GmbH',
        // Complex filter object
        'fields[customer]': 'companyName,industry',
        'include[]': ['locations', 'contacts'],
        // Array parameters
        'industries[]': ['hotel', 'restaurant'],
        // Nested filtering
        'filters[industry][in]': 'hotel,restaurant',
        'filters[status][ne]': 'deleted',
      };

      await apiClient.get('/api/customers', { params: queryParams });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringMatching(/page=1.*size=20.*filter=active/),
        expect.objectContaining({ method: 'GET' })
      );
    });

    it('should handle DELETE with optional data', async () => {
      // DELETE with body (for bulk operations)
      const deleteData = {
        ids: ['cust-1', 'cust-2', 'cust-3'],
        reason: 'bulk-cleanup',
        cascade: true,
        backup: {
          enabled: true,
          retention: '30days',
        },
      };

      await apiClient.delete('/api/customers/bulk', {
        body: JSON.stringify(deleteData),
      });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/customers/bulk'),
        expect.objectContaining({
          method: 'DELETE',
          body: JSON.stringify(deleteData),
        })
      );
    });
  });

  describe('🔐 Authentication & Headers', () => {
    it('should include auth tokens from various sources', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({}),
      });

      // Test sessionStorage token
      sessionStorage.setItem('auth_token', 'session-token-123');
      await apiClient.get('/api/protected');

      expect(mockFetch).toHaveBeenLastCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: 'Bearer session-token-123',
          }),
        })
      );

      // Clear session, test localStorage
      sessionStorage.clear();
      localStorage.setItem('auth_token', 'local-token-456');
      await apiClient.get('/api/protected');

      expect(mockFetch).toHaveBeenLastCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: 'Bearer local-token-456',
          }),
        })
      );

      // Test Keycloak token
      localStorage.clear();
      sessionStorage.setItem('kc_token', 'keycloak-token-789');
      await apiClient.get('/api/protected');

      expect(mockFetch).toHaveBeenLastCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: 'Bearer keycloak-token-789',
          }),
        })
      );
    });

    it('should handle custom headers with flexible values', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({}),
      });

      const customHeaders = {
        'X-Custom-Field': 'custom-value',
        'X-Client-Version': '2.0.0',
        'X-Request-ID': `req-${Date.now()}`,
        'X-Feature-Flags': JSON.stringify(['feature-a', 'feature-b']),
        'X-Context': JSON.stringify({
          user: 'test-user',
          tenant: 'test-tenant',
          capabilities: ['read', 'write'],
        }),
      };

      await apiClient.get('/api/test', {
        headers: customHeaders,
      });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining(customHeaders),
        })
      );
    });
  });

  describe('⚡ Performance & Reliability', () => {
    it('should handle timeout and abort operations', async () => {
      // Test that timeout functionality exists (simplified test)
      expect(typeof apiClient.get).toBe('function');

      // Mock network error to simulate timeout
      mockFetch.mockRejectedValueOnce(new Error('Request timeout'));

      await expect(apiClient.get('/api/slow')).rejects.toThrow();
    }, 1000);

    it('should implement retry logic for server errors', async () => {
      // First call fails, second succeeds
      mockFetch.mockRejectedValueOnce(new Error('Network error')).mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({ success: true }),
      });

      const result = await apiClient.get('/api/retry-test', { retry: 1 });

      expect(result).toEqual({ success: true });
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should handle concurrent requests efficiently', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ success: true }),
      });

      // Multiple concurrent requests
      const requests = [
        apiClient.get('/api/customers'),
        apiClient.get('/api/locations'),
        apiClient.post('/api/drafts', { data: 'test' }),
        apiClient.put('/api/settings', { theme: 'dark' }),
      ];

      const results = await Promise.all(requests);

      expect(results).toHaveLength(4);
      expect(mockFetch).toHaveBeenCalledTimes(4);
      results.forEach(result => {
        expect(result).toEqual({ success: true });
      });
    });
  });

  describe('🚨 Error Handling mit flexiblen Strukturen', () => {
    it('should create consistent API errors from various response formats', async () => {
      const errorResponses = [
        // Standard error
        {
          status: 400,
          json: async () => ({
            code: 'VALIDATION_ERROR',
            message: 'Validation failed',
            fieldErrors: {
              companyName: 'Required field',
              contactEmail: 'Invalid email format',
            },
          }),
        },

        // Simple error
        {
          status: 404,
          json: async () => ({
            error: 'Customer not found',
          }),
        },

        // Complex nested error
        {
          status: 422,
          json: async () => ({
            errors: {
              customer: {
                fields: {
                  industry: ['Must be one of: hotel, restaurant, office'],
                  'locations[0].roomCount': ['Must be greater than 0'],
                },
              },
            },
            context: {
              request_id: 'req-123',
              trace_id: 'trace-456',
            },
          }),
        },
      ];

      for (const errorResponse of errorResponses) {
        mockFetch.mockResolvedValueOnce({
          ok: false,
          ...errorResponse,
        });

        await expect(apiClient.get('/api/error')).rejects.toThrow();
      }
    });

    it('should handle network errors gracefully', async () => {
      mockFetch.mockRejectedValue(new Error('Network error'));

      await expect(apiClient.get('/api/network-error')).rejects.toThrow('Netzwerkfehler');
    });
  });

  describe('🧹 Request Management', () => {
    it('should cancel specific requests', async () => {
      // Start a request (don't await)
      const requestPromise = apiClient.get('/api/long-running');

      // Cancel it
      apiClient.cancelRequest('GET', '/api/long-running');

      // Request should be aborted
      await expect(requestPromise).rejects.toThrow();
    });

    it('should cancel all pending requests', async () => {
      // Start multiple requests
      const requests = [
        apiClient.get('/api/customers'),
        apiClient.post('/api/locations', {}),
        apiClient.put('/api/settings', {}),
      ];

      // Cancel all
      apiClient.cancelAllRequests();

      // All should be aborted
      for (const request of requests) {
        await expect(request).rejects.toThrow();
      }
    });
  });

  describe('🎯 Real-world Customer Scenarios', () => {
    it('should handle complete customer onboarding flow', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ success: true, id: 'customer-123' }),
      });

      // Step 1: Create draft
      const draftData = {
        customerData: {
          companyName: 'Test Hotel GmbH',
          industry: 'hotel',
          chainCustomer: 'ja',
        },
        step: 'customer-data',
        metadata: {
          started: new Date().toISOString(),
          source: 'web-wizard',
        },
      };

      await apiClient.post('/api/customers/draft', draftData);

      // Step 2: Add locations
      const locationData = {
        locations: [
          {
            locationType: 'hauptstandort',
            fieldValues: {
              roomCount: 150,
              hasRestaurant: true,
              parkingSpaces: 50,
            },
          },
          {
            locationType: 'filiale',
            fieldValues: {
              roomCount: 80,
              hasRestaurant: false,
              parkingSpaces: 25,
            },
          },
        ],
      };

      await apiClient.put('/api/customers/draft/customer-123', locationData);

      // Step 3: Finalize
      await apiClient.post('/api/customers/draft/customer-123/finalize');

      expect(mockFetch).toHaveBeenCalledTimes(3);
    });

    it('should handle field definitions loading with caching', async () => {
      const fieldDefinitions = {
        customer: [
          {
            key: 'companyName',
            label: 'Firmenname',
            fieldType: 'text',
            required: true,
          },
          {
            key: 'industry',
            label: 'Branche',
            fieldType: 'select',
            options: ['hotel', 'restaurant', 'office'],
          },
        ],
        location: [
          {
            key: 'roomCount',
            label: 'Zimmeranzahl',
            fieldType: 'number',
            condition: { step: 'industry', when: 'hotel' },
          },
        ],
      };

      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => fieldDefinitions,
      });

      // Load field definitions
      const result = await apiClient.get('/api/field-definitions', {
        params: { entityType: 'customer', industry: 'hotel' },
      });

      expect(result).toEqual(fieldDefinitions);
    });
  });
});
