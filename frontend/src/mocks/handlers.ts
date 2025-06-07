import { http, HttpResponse } from 'msw';
import { calculatorHandlers } from './calculatorHandlers';

export const handlers = [
  // Mock API endpoints
  http.get('http://localhost:8080/api/ping', () => {
    return HttpResponse.json({
      message: 'pong',
      timestamp: new Date().toISOString(),
      user: 'mock-user',
    });
  }),

  // Calculator API handlers
  ...calculatorHandlers,
];
