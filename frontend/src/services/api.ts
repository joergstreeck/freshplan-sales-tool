// API Service for backend communication

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export interface PingResponse {
  message: string;
  timestamp: string;
  user?: string;
  dbTime?: string;
  dbStatus?: string;
}

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
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }

    return response.json();
  }
}
