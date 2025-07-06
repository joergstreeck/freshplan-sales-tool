/**
 * Mock data for development and testing
 */

import type { PriorityTask, TriageItem } from '../types';

export const mockTasks: PriorityTask[] = [
  {
    id: '1',
    title: 'Angebot für Müller GmbH nachfassen',
    type: 'call',
    customerName: 'Müller GmbH',
    priority: 'high',
    completed: false
  },
  {
    id: '2',
    title: 'Vertragsverlängerung Schmidt AG',
    type: 'email',
    customerName: 'Schmidt AG',
    priority: 'high',
    completed: false
  },
  {
    id: '3',
    title: 'Quartalsgespräch mit FreshFood',
    type: 'appointment',
    customerName: 'FreshFood GmbH',
    priority: 'medium',
    completed: false
  }
];

export const mockTriageItems: TriageItem[] = [
  {
    id: 't1',
    from: 'info@neuekunden.de',
    subject: 'Anfrage Catering-Service',
    content: 'Sehr geehrte Damen und Herren, wir sind auf der Suche nach...',
    receivedAt: new Date('2025-01-15T09:00:00Z'),
    type: 'email',
    processed: false
  },
  {
    id: 't2',
    from: 'bestellung@restaurant-xyz.de',
    subject: 'Interesse an Ihrem Angebot',
    content: 'Guten Tag, wir haben Ihre Webseite gesehen und würden gerne...',
    receivedAt: new Date('2025-01-15T09:00:00Z'),
    type: 'email',
    processed: false
  }
];