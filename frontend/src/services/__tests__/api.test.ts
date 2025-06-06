import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ApiService } from '../api';

// Mock fetch
global.fetch = vi.fn();

describe('ApiService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('ping', () => {
    it('should call /api/ping endpoint', async () => {
      const mockResponse = {
        message: 'pong',
        timestamp: '2025-01-05T20:00:00Z',
        user: 'testuser',
      };

      vi.mocked(global.fetch).mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      } as Response);

      const result = await ApiService.ping('test-token');

      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/ping', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          Authorization: 'Bearer test-token',
        },
      });

      expect(result).toEqual(mockResponse);
    });

    it('should work without token', async () => {
      const mockResponse = {
        message: 'pong',
        timestamp: '2025-01-05T20:00:00Z',
      };

      vi.mocked(global.fetch).mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      } as Response);

      await ApiService.ping();

      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/api/ping', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
    });

    it('should throw error on non-ok response', async () => {
      vi.mocked(global.fetch).mockResolvedValueOnce({
        ok: false,
        status: 401,
        statusText: 'Unauthorized',
      } as Response);

      await expect(ApiService.ping()).rejects.toThrow('API Error: 401 Unauthorized');
    });
  });
});
