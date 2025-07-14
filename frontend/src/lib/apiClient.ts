// Shared API client with base configuration
import type { ApiError } from '../api-types';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

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
    } catch (error) {
      // Handle backend connection errors with helpful debug info
      if (error instanceof TypeError && error.message.includes('fetch')) {
        console.group('🚨 BACKEND CONNECTION FAILED');
        console.error('Failed to connect to backend:', url);
        console.log('');
        console.log('CLAUDE: Backend is probably down. Run these commands:');
        console.log('1. export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home');
        console.log('2. cd backend && nohup mvn quarkus:dev > ../logs/backend.log 2>&1 &');
        console.log('3. Wait 30 seconds, then refresh frontend');
        console.log('');
        console.log('OR use: ./scripts/backend-manager.sh start');
        console.log('');
        console.log('Problem: Backend timeout/startup issue (Java 17 required)');
        console.groupEnd();
        
        const connectionError: ApiError = {
          code: 'CONNECTION_FAILED',
          message: 'Backend not reachable - check console for fix commands',
          details: { url, originalError: error.message },
        };
        throw connectionError;
      }
      
      if (error instanceof Error && 'code' in error) {
        throw error; // Re-throw our custom ApiError
      }

      // Handle network errors
      const apiError: ApiError = {
        code: 'NETWORK_ERROR',
        message: error instanceof Error ? error.message : 'Network request failed',
        details: { originalError: error },
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
