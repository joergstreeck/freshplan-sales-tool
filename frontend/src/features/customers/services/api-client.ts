/**
 * API Client Base für Customer Management
 *
 * Erweitert die globale API-Klasse um Customer-spezifische Features.
 * Implementiert automatische Error-Handling, Retry-Logic und Caching.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-api-integration.md
 */

import { ApiError } from '../types/api.types';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export interface RequestConfig extends RequestInit {
  params?: Record<string, string | number | boolean | undefined>;
  retry?: number;
  timeout?: number;
}

export class ApiClient {
  private static instance: ApiClient;
  private abortControllers = new Map<string, AbortController>();

  static getInstance(): ApiClient {
    if (!ApiClient.instance) {
      ApiClient.instance = new ApiClient();
    }
    return ApiClient.instance;
  }

  private constructor() {}

  /**
   * Basis HTTP Request mit Error Handling
   */
  private async request<T>(endpoint: string, config: RequestConfig = {}): Promise<T> {
    const { params, retry = 0, timeout = 30000, ...fetchConfig } = config;

    // Build URL with params
    const url = new URL(`${API_URL}${endpoint}`);
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          url.searchParams.append(key, String(value));
        }
      });
    }

    // Setup abort controller for timeout
    const abortController = new AbortController();
    const timeoutId = setTimeout(() => abortController.abort(), timeout);

    // Store controller for manual cancellation
    const requestKey = `${fetchConfig.method || 'GET'} ${endpoint}`;
    this.abortControllers.set(requestKey, abortController);

    try {
      // Get auth token from localStorage/sessionStorage
      const token = this.getAuthToken();

      const response = await fetch(url.toString(), {
        ...fetchConfig,
        signal: abortController.signal,
        headers: {
          'Content-Type': 'application/json',
          ...(token && { Authorization: `Bearer ${token}` }),
          ...fetchConfig.headers,
        },
      });

      clearTimeout(timeoutId);
      this.abortControllers.delete(requestKey);

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw this.createApiError(response.status, errorData);
      }

      // Handle empty responses
      if (response.status === 204) {
        return {} as T;
      }

      return await response.json();
    } catch (error) {
      clearTimeout(timeoutId);
      this.abortControllers.delete(requestKey);

      // Retry logic
      if (retry > 0 && this.shouldRetry(error)) {
        await this.delay(1000 * (3 - retry)); // Exponential backoff
        return this.request<T>(endpoint, { ...config, retry: retry - 1 });
      }

      throw this.handleError(error);
    }
  }

  /**
   * GET Request
   */
  async get<T>(endpoint: string, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, { ...config, method: 'GET' });
  }

  /**
   * POST Request
   */
  async post<T>(endpoint: string, data?: any, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, {
      ...config,
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  /**
   * PUT Request
   */
  async put<T>(endpoint: string, data?: any, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, {
      ...config,
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  /**
   * DELETE Request
   */
  async delete<T>(endpoint: string, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, { ...config, method: 'DELETE' });
  }

  /**
   * PATCH Request
   */
  async patch<T>(endpoint: string, data?: any, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, {
      ...config,
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  /**
   * Cancel a pending request
   */
  cancelRequest(method: string, endpoint: string): void {
    const key = `${method} ${endpoint}`;
    const controller = this.abortControllers.get(key);
    if (controller) {
      controller.abort();
      this.abortControllers.delete(key);
    }
  }

  /**
   * Cancel all pending requests
   */
  cancelAllRequests(): void {
    this.abortControllers.forEach(controller => controller.abort());
    this.abortControllers.clear();
  }

  /**
   * Get auth token from storage
   */
  private getAuthToken(): string | null {
    // Check session storage first (temporary session)
    const sessionToken = sessionStorage.getItem('auth_token');
    if (sessionToken) return sessionToken;

    // Check local storage (remember me)
    const localToken = localStorage.getItem('auth_token');
    if (localToken) return localToken;

    // Check Keycloak token if integrated
    const keycloakToken = sessionStorage.getItem('kc_token');
    if (keycloakToken) return keycloakToken;

    return null;
  }

  /**
   * Create standardized API error
   */
  private createApiError(status: number, data: any): ApiError {
    return {
      status,
      code: data.code || `HTTP_${status}`,
      message: data.message || this.getDefaultErrorMessage(status),
      fieldErrors: data.fieldErrors,
      timestamp: data.timestamp || new Date().toISOString(),
    };
  }

  /**
   * Get default error message for status code
   */
  private getDefaultErrorMessage(status: number): string {
    switch (status) {
      case 400:
        return 'Ungültige Anfrage';
      case 401:
        return 'Nicht autorisiert';
      case 403:
        return 'Zugriff verweigert';
      case 404:
        return 'Ressource nicht gefunden';
      case 409:
        return 'Konflikt - Ressource existiert bereits';
      case 422:
        return 'Validierungsfehler';
      case 500:
        return 'Interner Serverfehler';
      case 503:
        return 'Service nicht verfügbar';
      default:
        return `Fehler ${status}`;
    }
  }

  /**
   * Determine if error should trigger retry
   */
  private shouldRetry(error: any): boolean {
    if (error.name === 'AbortError') return false;
    if (error.status && error.status < 500) return false;
    return true;
  }

  /**
   * Handle errors consistently
   */
  private handleError(error: any): never {
    if (error.name === 'AbortError') {
      throw this.createApiError(0, {
        code: 'REQUEST_ABORTED',
        message: 'Anfrage wurde abgebrochen',
      });
    }

    if (error.status) {
      throw error; // Already an ApiError
    }

    throw this.createApiError(0, {
      code: 'NETWORK_ERROR',
      message: 'Netzwerkfehler - Bitte prüfen Sie Ihre Verbindung',
    });
  }

  /**
   * Delay helper for retry logic
   */
  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

// Export singleton instance
export const apiClient = ApiClient.getInstance();
