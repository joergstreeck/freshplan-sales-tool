import { describe, it, expect, vi, beforeEach } from 'vitest';
import { listLeads, createLead, toProblem } from '../api';

// Mock fetch
global.fetch = vi.fn();
const mockFetch = fetch as ReturnType<typeof vi.fn>;

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock });

describe('Leads API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080');
  });

  describe('listLeads', () => {
    it('makes correct API call', async () => {
      const mockLeads = [
        { id: '1', name: 'Lead 1', email: 'lead1@test.com' },
        { id: '2', name: 'Lead 2', email: 'lead2@test.com' },
      ];

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockLeads),
      } as Response);

      const result = await listLeads();

      expect(mockFetch).toHaveBeenCalledWith('http://localhost:8080/api/leads', {
        headers: { Accept: 'application/json' },
        credentials: 'include',
      });
      expect(result).toEqual(mockLeads);
    });

    it('includes auth header when token exists', async () => {
      localStorageMock.getItem.mockReturnValue('test-token');

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve([]),
      } as Response);

      await listLeads();

      expect(mockFetch).toHaveBeenCalledWith('http://localhost:8080/api/leads', {
        headers: {
          Accept: 'application/json',
          Authorization: 'Bearer test-token',
        },
        credentials: 'include',
      });
    });

    it('throws problem on API error', async () => {
      const errorResponse = {
        title: 'Unauthorized',
        status: 401,
        detail: 'Invalid token',
      };

      mockFetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve(errorResponse),
      } as Response);

      await expect(listLeads()).rejects.toEqual(errorResponse);
    });
  });

  describe('createLead', () => {
    it('makes correct API call with all fields', async () => {
      const payload = { name: 'New Lead', email: 'new@test.com' };
      const mockResponse = { id: '123', ...payload };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      } as Response);

      const result = await createLead(payload);

      expect(mockFetch).toHaveBeenCalledWith('http://localhost:8080/api/leads', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(payload),
        credentials: 'include',
      });
      expect(result).toEqual(mockResponse);
    });

    it('makes correct API call without email', async () => {
      const payload = { name: 'New Lead' };
      const mockResponse = { id: '123', ...payload };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      } as Response);

      await createLead(payload);

      expect(mockFetch).toHaveBeenCalledWith('http://localhost:8080/api/leads', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(payload),
        credentials: 'include',
      });
    });

    it('throws validation error with field details', async () => {
      const errorResponse = {
        title: 'Validation Failed',
        status: 400,
        errors: {
          name: ['Name is required'],
          email: ['Invalid email format'],
        },
      };

      mockFetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve(errorResponse),
      } as Response);

      await expect(createLead({ name: '', email: 'invalid' })).rejects.toEqual(errorResponse);
    });
  });

  describe('toProblem', () => {
    it('parses JSON error response', async () => {
      const errorData = { title: 'Error', detail: 'Something went wrong' };
      const response = {
        json: () => Promise.resolve(errorData),
      } as Response;

      const result = await toProblem(response);

      expect(result).toEqual(errorData);
    });

    it('handles non-JSON response', async () => {
      const response = {
        statusText: 'Internal Server Error',
        status: 500,
        json: () => Promise.reject(new Error('Not JSON')),
      } as Response;

      const result = await toProblem(response);

      expect(result).toEqual({
        title: 'Internal Server Error',
        status: 500,
      });
    });
  });
});
