import { describe, it, expect, vi, beforeEach, afterEach, beforeAll, afterAll } from 'vitest';
import { listLeads, createLead, toProblem } from '../api';
import { server } from '@/mocks/server';
import { http, HttpResponse } from 'msw';

describe('Leads API', () => {
  beforeAll(() => {
    server.listen({ onUnhandledRequest: 'bypass' });
  });

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    sessionStorage.clear();
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080');
  });

  afterEach(() => {
    server.resetHandlers();
  });

  afterAll(() => {
    server.close();
  });

  describe('listLeads', () => {
    it('makes correct API call', async () => {
      const mockLeads = [
        { id: '1', name: 'Lead 1', email: 'lead1@test.com' },
        { id: '2', name: 'Lead 2', email: 'lead2@test.com' },
      ];

      server.use(
        http.get('http://localhost:8080/api/leads', () => {
          return HttpResponse.json({ data: mockLeads });
        })
      );

      const result = await listLeads();

      expect(result).toEqual(mockLeads);
    });

    it('includes auth header when token exists', async () => {
      localStorage.setItem('token', 'test-token');

      let receivedAuthHeader: string | null = null;

      server.use(
        http.get('http://localhost:8080/api/leads', ({ request }) => {
          receivedAuthHeader = request.headers.get('Authorization');
          return HttpResponse.json({ data: [] });
        })
      );

      await listLeads();

      expect(receivedAuthHeader).toBe('Bearer test-token');
    });

    it('throws problem on API error', async () => {
      const errorResponse = {
        title: 'Unauthorized',
        status: 401,
        detail: 'Invalid token',
      };

      server.use(
        http.get('http://localhost:8080/api/leads', () => {
          return HttpResponse.json(errorResponse, { status: 401 });
        })
      );

      await expect(listLeads()).rejects.toEqual(errorResponse);
    });
  });

  describe('createLead', () => {
    it('makes correct API call with all fields', async () => {
      const payload = { name: 'New Lead', email: 'new@test.com' };
      const mockResponse = { id: '123', ...payload };

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          const body = await request.json();
          expect(body).toEqual(payload);
          return HttpResponse.json(mockResponse);
        })
      );

      const result = await createLead(payload);

      expect(result).toEqual(mockResponse);
    });

    it('makes correct API call without email', async () => {
      const payload = { name: 'New Lead' };
      const mockResponse = { id: '123', ...payload };

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          const body = await request.json();
          expect(body).toEqual(payload);
          return HttpResponse.json(mockResponse);
        })
      );

      await createLead(payload);
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

      server.use(
        http.post('http://localhost:8080/api/leads', () => {
          return HttpResponse.json(errorResponse, { status: 400 });
        })
      );

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
