/**
 * MSW handlers for tests
 */

import { http, HttpResponse } from 'msw';

export const handlers = [
  // Mock API endpoints for testing
  http.post('/api/credit-check', () => {
    return HttpResponse.json({
      status: 'approved',
      creditLimit: 10000,
      rating: 3,
      ratingText: 'Gute Bonität',
      message: 'Automatisch genehmigt',
      timestamp: new Date().toISOString()
    });
  }),

  http.post('/api/management-approval', () => {
    return HttpResponse.json({
      success: true,
      message: 'Anfrage wurde an die Geschäftsleitung gesendet.'
    });
  })
];