/**
 * ApiClient Tests
 *
 * Tests for the shared API client with error handling,
 * authentication, and HTTP method coverage.
 */

import { describe, it, expect, vi, beforeEach, afterEach, beforeAll, afterAll } from 'vitest';
import { httpClient } from '../apiClient';
import { server } from '@/mocks/server';
import { http, HttpResponse } from 'msw';

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock, writable: true });

describe('ApiClient', () => {
  beforeAll(() => {
    // Start MSW server
    server.listen({ onUnhandledRequest: 'error' });
  });

  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
  });

  afterEach(() => {
    server.resetHandlers();
  });

  afterAll(() => {
    server.close();
  });

  describe('GET requests', () => {
    it('should make successful GET request', async () => {
      const mockData = { id: 1, name: 'Test' };

      server.use(
        http.get('http://localhost:8080/api/test', () => {
          return HttpResponse.json(mockData);
        })
      );

      const response = await httpClient.get('/api/test');

      expect(response.data).toEqual(mockData);
      expect(response.status).toBe(200);
    });

    it('should include auth token in headers when available', async () => {
      localStorageMock.getItem.mockReturnValue('test-token-123');

      let receivedHeaders: Headers | undefined;

      server.use(
        http.get('http://localhost:8080/api/secured', ({ request }) => {
          receivedHeaders = request.headers;
          return HttpResponse.json({ success: true });
        })
      );

      await httpClient.get('/api/secured');

      expect(receivedHeaders?.get('Authorization')).toBe('Bearer test-token-123');
    });

    it('should handle 204 No Content responses', async () => {
      server.use(
        http.get('http://localhost:8080/api/no-content', () => {
          return new HttpResponse(null, { status: 204 });
        })
      );

      const response = await httpClient.get('/api/no-content');

      expect(response.status).toBe(204);
      expect(response.data).toBeNull();
    });

    it('should handle empty content-length responses', async () => {
      server.use(
        http.get('http://localhost:8080/api/empty', () => {
          return new HttpResponse(null, {
            status: 200,
            headers: { 'content-length': '0' },
          });
        })
      );

      const response = await httpClient.get('/api/empty');

      expect(response.status).toBe(200);
      expect(response.data).toBeNull();
    });
  });

  describe('POST requests', () => {
    it('should make successful POST request with data', async () => {
      const postData = { name: 'New Item', value: 42 };
      const mockResponse = { id: 123, ...postData };

      server.use(
        http.post('http://localhost:8080/api/items', async ({ request }) => {
          const body = await request.json();
          expect(body).toEqual(postData);
          return HttpResponse.json(mockResponse, { status: 201 });
        })
      );

      const response = await httpClient.post('/api/items', postData);

      expect(response.data).toEqual(mockResponse);
      expect(response.status).toBe(201);
    });

    it('should handle POST without body data', async () => {
      server.use(
        http.post('http://localhost:8080/api/action', () => {
          return HttpResponse.json({ success: true });
        })
      );

      const response = await httpClient.post('/api/action');

      expect(response.data).toEqual({ success: true });
      expect(response.status).toBe(200);
    });
  });

  describe('PUT requests', () => {
    it('should make successful PUT request', async () => {
      const updateData = { name: 'Updated', value: 99 };
      const mockResponse = { id: 1, ...updateData };

      server.use(
        http.put('http://localhost:8080/api/items/1', async ({ request }) => {
          const body = await request.json();
          expect(body).toEqual(updateData);
          return HttpResponse.json(mockResponse);
        })
      );

      const response = await httpClient.put('/api/items/1', updateData);

      expect(response.data).toEqual(mockResponse);
      expect(response.status).toBe(200);
    });
  });

  describe('DELETE requests', () => {
    it('should make successful DELETE request', async () => {
      server.use(
        http.delete('http://localhost:8080/api/items/1', () => {
          return new HttpResponse(null, { status: 204 });
        })
      );

      const response = await httpClient.delete('/api/items/1');

      expect(response.status).toBe(204);
      expect(response.data).toBeNull();
    });
  });

  describe('Error handling', () => {
    it('should throw ApiError on HTTP 400', async () => {
      server.use(
        http.get('http://localhost:8080/api/error', () => {
          return new HttpResponse(null, {
            status: 400,
            statusText: 'Bad Request',
          });
        })
      );

      await expect(httpClient.get('/api/error')).rejects.toEqual({
        code: 'HTTP_400',
        message: 'Bad Request',
        details: { status: 400 },
      });
    });

    it('should throw ApiError on HTTP 404', async () => {
      server.use(
        http.get('http://localhost:8080/api/missing', () => {
          return new HttpResponse(null, {
            status: 404,
            statusText: 'Not Found',
          });
        })
      );

      await expect(httpClient.get('/api/missing')).rejects.toEqual({
        code: 'HTTP_404',
        message: 'Not Found',
        details: { status: 404 },
      });
    });

    it('should throw ApiError on HTTP 500', async () => {
      server.use(
        http.post('http://localhost:8080/api/fail', () => {
          return new HttpResponse(null, {
            status: 500,
            statusText: 'Internal Server Error',
          });
        })
      );

      await expect(httpClient.post('/api/fail')).rejects.toEqual({
        code: 'HTTP_500',
        message: 'Internal Server Error',
        details: { status: 500 },
      });
    });

    it('should throw ApiError on network failure', async () => {
      server.use(
        http.get('http://localhost:8080/api/network-error', () => {
          return HttpResponse.error();
        })
      );

      await expect(httpClient.get('/api/network-error')).rejects.toEqual({
        code: 'NETWORK_ERROR',
        message: expect.any(String),
        details: { originalError: expect.any(Error) },
      });
    });

    it('should handle fetch timeout errors', async () => {
      server.use(
        http.get('http://localhost:8080/api/timeout', () => {
          return HttpResponse.error();
        })
      );

      await expect(httpClient.get('/api/timeout')).rejects.toEqual({
        code: 'NETWORK_ERROR',
        message: expect.any(String),
        details: { originalError: expect.any(Error) },
      });
    });
  });

  describe('Authentication', () => {
    it('should not include auth header when no token exists', async () => {
      localStorageMock.getItem.mockReturnValue(null);

      let receivedHeaders: Headers | undefined;

      server.use(
        http.get('http://localhost:8080/api/public', ({ request }) => {
          receivedHeaders = request.headers;
          return HttpResponse.json({});
        })
      );

      await httpClient.get('/api/public');

      expect(receivedHeaders?.get('Authorization')).toBeNull();
    });

    it('should retrieve token from localStorage', async () => {
      localStorageMock.getItem.mockReturnValue('stored-token');

      server.use(
        http.get('http://localhost:8080/api/test', () => {
          return HttpResponse.json({});
        })
      );

      await httpClient.get('/api/test');

      expect(localStorageMock.getItem).toHaveBeenCalledWith('auth-token');
    });
  });

  describe('Custom headers', () => {
    it('should allow custom headers to be passed', async () => {
      let receivedHeaders: Headers | undefined;

      server.use(
        http.post('http://localhost:8080/api/custom', ({ request }) => {
          receivedHeaders = request.headers;
          return HttpResponse.json({});
        })
      );

      await httpClient.post('/api/custom', { data: 'test' });

      expect(receivedHeaders?.get('Content-Type')).toBe('application/json');
    });
  });
});
