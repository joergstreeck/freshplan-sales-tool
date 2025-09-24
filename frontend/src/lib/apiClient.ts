// Shared API client with base configuration
import type { ApiError } from '../api-types';

// In development, use relative URL to work with Vite proxy
// In production, use the environment variable
const API_URL =
  import.meta.env.VITE_API_URL || (import.meta.env.DEV ? '' : 'http://localhost:8080');

interface ApiResponse<T = unknown> {
  data: T;
  status: number;
  statusText: string;
}

class ApiClient {
  private baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
    const url = `${this.baseURL}${endpoint}`;

    const defaultHeaders: HeadersInit = {
      'Content-Type': 'application/json',
    };

    // Add auth token if available
    const token = this.getAuthToken();
    if (token) {
      defaultHeaders['Authorization'] = `Bearer ${token}`;
    }

    const config: RequestInit = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
    };

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        const error: ApiError = {
          code: `HTTP_${response.status}`,
          message: response.statusText || 'API request failed',
          details: { status: response.status },
        };
        throw error;
      }

      // Handle 204 No Content responses
      let data: T | null = null;
      if (response.status !== 204 && response.headers.get('content-length') !== '0') {
        data = await response.json();
      }

      return {
        data: data as T,
        status: response.status,
        statusText: response.statusText,
      };
    } catch (_error) {
      // Handle backend connection errors with helpful debug info
      if (_error instanceof TypeError && _error.message.includes('fetch')) {
        const connectionError: ApiError = {
          code: 'CONNECTION_FAILED',
          message: 'Backend not reachable - please check if backend is running',
          details: { url, originalError: _error.message },
        };
        throw connectionError;
      }

      if (_error instanceof Error && 'code' in _error) {
        throw _error; // Re-throw our custom ApiError
      }

      // Handle network errors
      const apiError: ApiError = {
        code: 'NETWORK_ERROR',
        message: _error instanceof Error ? _error.message : 'Network request failed',
        details: { originalError: _error },
      };
      throw apiError;
    }
  }

  private getAuthToken(): string | null {
    // For development: Check localStorage for mock token
    if (import.meta.env.DEV) {
      const token = localStorage.getItem('auth-token');
      if (token) {
        return token;
      }
    }
    // TODO: Integrate with AuthContext once Keycloak is ready
    return null;
  }

  async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: 'GET' });
  }

  async post<T>(endpoint: string, data?: unknown): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async put<T>(endpoint: string, data?: unknown): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }
}

// Singleton instance
export const httpClient = new ApiClient(API_URL);
