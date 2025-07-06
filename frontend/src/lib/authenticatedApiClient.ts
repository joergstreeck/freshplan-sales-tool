/**
 * Authenticated API Client
 * Axios instance with automatic token injection and error handling
 */
import axios, { AxiosInstance } from 'axios';
import { authUtils } from './keycloak';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Create axios instance
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  async config => {
    // Get token from Keycloak instance
    const token = authUtils.getToken();

    if (token) {
      // Check if token needs refresh (with 5 minute buffer)
      const needsRefresh = authUtils.isTokenExpired();
      if (needsRefresh) {
        try {
          await authUtils.updateToken(300); // 5 minute buffer
          const newToken = authUtils.getToken();
          if (newToken) {
            config.headers.Authorization = `Bearer ${newToken}`;
          }
        } catch (error) {
          console.error('Failed to refresh token:', error);
          // Let the request continue, will be caught by 401 handler
        }
      } else {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      console.error('API returned 401 Unauthorized:', {
        url: error.config?.url,
        method: error.config?.method,
      });
      
      // Dispatch custom event for global error handling
      const event = new CustomEvent('auth-error', {
        detail: {
          type: 'unauthorized',
          message: 'Ihre Sitzung ist abgelaufen. Bitte melden Sie sich erneut an.',
        },
      });
      window.dispatchEvent(event);
      
      // Keycloak will handle the redirect to login
      authUtils.login();
    }
    return Promise.reject(error);
  }
);

export default apiClient;
