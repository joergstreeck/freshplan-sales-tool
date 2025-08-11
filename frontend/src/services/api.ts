// API Service for backend communication

import type { PingResponse, ApiError } from '../api-types';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const DEFAULT_TIMEOUT = 10000; // 10 seconds

export class ApiService {
  static async ping(token?: string): Promise<PingResponse> {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(`${API_URL}/api/ping`, {
        method: 'GET',
        headers,
        signal: AbortSignal.timeout(DEFAULT_TIMEOUT),
      });

      if (!response.ok) {
        const error: ApiError = {
          code: `HTTP_${response.status}`,
          message: response.statusText || 'API request failed',
          details: { status: response.status },
        };
        throw error;
      }

      return response.json();
    } catch (error: unknown) {
      // Handle timeout specifically
      if (
        error instanceof Error &&
        (error.name === 'TimeoutError' || error.name === 'AbortError')
      ) {
        const timeoutError: ApiError = {
          code: 'TIMEOUT',
          message: 'Request timeout - the server took too long to respond',
          details: { timeout: DEFAULT_TIMEOUT },
        };
        throw timeoutError;
      }
      // Re-throw other errors (including our ApiError from above)
      throw error;
    }
  }
}
