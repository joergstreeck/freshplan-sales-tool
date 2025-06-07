// API Service for backend communication

import type { PingResponse, ApiError } from '../types/api';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export class ApiService {
  static async ping(token?: string): Promise<PingResponse> {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_URL}/api/ping`, {
      method: 'GET',
      headers,
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
  }
}
