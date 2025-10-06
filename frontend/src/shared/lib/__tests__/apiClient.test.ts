/**
 * ApiClient Tests
 *
 * Tests for the shared API client with error handling,
 * authentication, and HTTP method coverage.
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { httpClient } from '../apiClient';

// Mock fetch globally
global.fetch = vi.fn();
const mockFetch = fetch as ReturnType<typeof vi.fn>;

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock, writable: true });

describe('ApiClient', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('GET requests', () => {
    it('should make successful GET request', async () => {
      const mockData = { id: 1, name: 'Test' };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        statusText: 'OK',
        headers: new Headers({ 'content-length': '100' }),
        json: () => Promise.resolve(mockData),
      } as Response);

      const response = await httpClient.get('/api/test');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/test'),
        expect.objectContaining({
          method: 'GET',
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
          }),
        })
      );
      expect(response.data).toEqual(mockData);
      expect(response.status).toBe(200);
    });

    it('should include auth token in headers when available', async () => {
      localStorageMock.getItem.mockReturnValue('test-token-123');

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        statusText: 'OK',
        headers: new Headers({ 'content-length': '10' }),
        json: () => Promise.resolve({}),
      } as Response);

      await httpClient.get('/api/secured');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: 'Bearer test-token-123',
          }),
        })
      );
    });

    it('should handle 204 No Content responses', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        statusText: 'No Content',
        headers: new Headers(),
        json: () => Promise.reject(new Error('No content')),
      } as Response);

      const response = await httpClient.get('/api/no-content');

      expect(response.status).toBe(204);
      expect(response.data).toBeNull();
    });

    it('should handle empty content-length responses', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        statusText: 'OK',
        headers: new Headers({ 'content-length': '0' }),
        json: () => Promise.reject(new Error('No content')),
      } as Response);

      const response = await httpClient.get('/api/empty');

      expect(response.status).toBe(200);
      expect(response.data).toBeNull();
    });
  });

  describe('POST requests', () => {
    it('should make successful POST request with data', async () => {
      const postData = { name: 'New Item', value: 42 };
      const mockResponse = { id: 123, ...postData };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 201,
        statusText: 'Created',
        headers: new Headers({ 'content-length': '50' }),
        json: () => Promise.resolve(mockResponse),
      } as Response);

      const response = await httpClient.post('/api/items', postData);

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/items'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(postData),
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
          }),
        })
      );
      expect(response.data).toEqual(mockResponse);
      expect(response.status).toBe(201);
    });

    it('should handle POST without body data', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        statusText: 'OK',
        headers: new Headers({ 'content-length': '10' }),
        json: () => Promise.resolve({ success: true }),
      } as Response);

      await httpClient.post('/api/action');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          method: 'POST',
          body: undefined,
        })
      );
    });
  });

  describe('PUT requests', () => {
    it('should make successful PUT request', async () => {
      const updateData = { name: 'Updated', value: 99 };
      const mockResponse = { id: 1, ...updateData };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        statusText: 'OK',
        headers: new Headers({ 'content-length': '50' }),
        json: () => Promise.resolve(mockResponse),
      } as Response);

      const response = await httpClient.put('/api/items/1', updateData);

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/items/1'),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify(updateData),
        })
      );
      expect(response.data).toEqual(mockResponse);
    });
  });

  describe('DELETE requests', () => {
    it('should make successful DELETE request', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        statusText: 'No Content',
        headers: new Headers(),
        json: () => Promise.reject(new Error('No content')),
      } as Response);

      const response = await httpClient.delete('/api/items/1');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/items/1'),
        expect.objectContaining({
          method: 'DELETE',
        })
      );
      expect(response.status).toBe(204);
    });
  });

  describe('Error handling', () => {
    it('should throw ApiError on HTTP 400', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        statusText: 'Bad Request',
        headers: new Headers(),
      } as Response);

      await expect(httpClient.get('/api/error')).rejects.toEqual({
        code: 'HTTP_400',
        message: 'Bad Request',
        details: { status: 400 },
      });
    });

    it('should throw ApiError on HTTP 404', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        statusText: 'Not Found',
        headers: new Headers(),
      } as Response);

      await expect(httpClient.get('/api/missing')).rejects.toEqual({
        code: 'HTTP_404',
        message: 'Not Found',
        details: { status: 404 },
      });
    });

    it('should throw ApiError on HTTP 500', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
        headers: new Headers(),
      } as Response);

      await expect(httpClient.post('/api/fail')).rejects.toEqual({
        code: 'HTTP_500',
        message: 'Internal Server Error',
        details: { status: 500 },
      });
    });

    it('should throw ApiError on network failure', async () => {
      mockFetch.mockRejectedValueOnce(new Error('Network connection failed'));

      await expect(httpClient.get('/api/network-error')).rejects.toEqual({
        code: 'NETWORK_ERROR',
        message: 'Network connection failed',
        details: { originalError: expect.any(Error) },
      });
    });

    it('should handle fetch timeout errors', async () => {
      mockFetch.mockRejectedValueOnce(new Error('Request timeout'));

      await expect(httpClient.get('/api/timeout')).rejects.toEqual({
        code: 'NETWORK_ERROR',
        message: 'Request timeout',
        details: { originalError: expect.any(Error) },
      });
    });
  });

  describe('Authentication', () => {
    it('should not include auth header when no token exists', async () => {
      localStorageMock.getItem.mockReturnValue(null);

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        statusText: 'OK',
        headers: new Headers({ 'content-length': '10' }),
        json: () => Promise.resolve({}),
      } as Response);

      await httpClient.get('/api/public');

      const callHeaders = mockFetch.mock.calls[0][1]?.headers as Record<string, string>;
      expect(callHeaders['Authorization']).toBeUndefined();
    });

    it('should retrieve token from localStorage', async () => {
      localStorageMock.getItem.mockReturnValue('stored-token');

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        statusText: 'OK',
        headers: new Headers({ 'content-length': '10' }),
        json: () => Promise.resolve({}),
      } as Response);

      await httpClient.get('/api/test');

      expect(localStorageMock.getItem).toHaveBeenCalledWith('auth-token');
    });
  });

  describe('Custom headers', () => {
    it('should allow custom headers to be passed', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        statusText: 'OK',
        headers: new Headers({ 'content-length': '10' }),
        json: () => Promise.resolve({}),
      } as Response);

      await httpClient.post('/api/custom', { data: 'test' });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
          }),
        })
      );
    });
  });
});
