import { httpClient } from '@/lib/apiClient';
import type {
  ContactInteractionDTO,
  WarmthScoreDTO,
  DataQualityMetricsDTO,
  BatchImportRequest,
  ContactIntelligenceSummary,
  DataFreshnessLevel,
  DataQualityScore,
  FreshnessStatistics
} from '../types/intelligence.types';
import type { Contact } from '../types/contact.types';

export const contactInteractionApi = {
  /**
   * Create a new interaction
   */
  createInteraction: async (interaction: ContactInteractionDTO): Promise<ContactInteractionDTO> => {
    const response = await httpClient.post('/api/contact-interactions', interaction);
    return response.data;
  },

  /**
   * Get interactions for a contact
   */
  getContactInteractions: async (contactId: string): Promise<ContactInteractionDTO[]> => {
    const response = await httpClient.get(`/api/contact-interactions/contact/${contactId}`);
    return response.data;
  },

  /**
   * Get warmth score for a contact
   */
  getWarmthScore: async (contactId: string): Promise<WarmthScoreDTO> => {
    const response = await httpClient.get(`/api/contact-interactions/warmth/${contactId}`);
    return response.data;
  },

  /**
   * Calculate warmth score (triggers recalculation)
   */
  calculateWarmthScore: async (contactId: string): Promise<WarmthScoreDTO> => {
    const response = await httpClient.post(`/api/contact-interactions/warmth/${contactId}/calculate`);
    return response.data;
  },

  /**
   * Get data quality metrics
   */
  getDataQualityMetrics: async (): Promise<DataQualityMetricsDTO> => {
    const response = await httpClient.get('/api/contact-interactions/data-quality/metrics');
    return response.data;
  },

  /**
   * Record a note as interaction
   */
  recordNote: async (contactId: string, note: string): Promise<ContactInteractionDTO> => {
    const response = await httpClient.post(`/api/contact-interactions/note/${contactId}`, {
      note
    });
    return response.data;
  },

  /**
   * Record an email interaction
   */
  recordEmail: async (
    contactId: string,
    type: 'SENT' | 'RECEIVED',
    subject: string,
    sentiment?: number
  ): Promise<ContactInteractionDTO> => {
    const response = await httpClient.post(`/api/contact-interactions/email/${contactId}`, {
      type,
      subject,
      sentiment
    });
    return response.data;
  },

  /**
   * Record a call interaction
   */
  recordCall: async (
    contactId: string,
    type: 'OUTBOUND' | 'INBOUND',
    duration: number,
    outcome?: string
  ): Promise<ContactInteractionDTO> => {
    const response = await httpClient.post(`/api/contact-interactions/call/${contactId}`, {
      type,
      duration,
      outcome
    });
    return response.data;
  },

  /**
   * Record a meeting interaction
   */
  recordMeeting: async (
    contactId: string,
    type: 'SCHEDULED' | 'COMPLETED',
    duration?: number,
    notes?: string
  ): Promise<ContactInteractionDTO> => {
    const response = await httpClient.post(`/api/contact-interactions/meeting/${contactId}`, {
      type,
      duration,
      notes
    });
    return response.data;
  },

  /**
   * Batch import interactions
   */
  batchImport: async (request: BatchImportRequest): Promise<{ imported: number; failed: number }> => {
    const response = await httpClient.post('/api/contact-interactions/batch-import', request);
    return response.data;
  },

  /**
   * Get intelligence summary for a contact
   */
  getContactIntelligence: async (contactId: string): Promise<ContactIntelligenceSummary> => {
    const response = await httpClient.get(`/api/contact-interactions/intelligence/${contactId}`);
    return response.data;
  },

  /**
   * Update interaction
   */
  updateInteraction: async (
    interactionId: string,
    updates: Partial<ContactInteractionDTO>
  ): Promise<ContactInteractionDTO> => {
    const response = await httpClient.put(`/api/contact-interactions/${interactionId}`, updates);
    return response.data;
  },

  /**
   * Delete interaction
   */
  deleteInteraction: async (interactionId: string): Promise<void> => {
    await httpClient.delete(`/api/contact-interactions/${interactionId}`);
  },

  // ===============================
  // Data Freshness Tracking Methods
  // ===============================

  /**
   * Get freshness level for a specific contact
   */
  getContactFreshnessLevel: async (contactId: string): Promise<DataFreshnessLevel> => {
    const response = await httpClient.get(`/api/contact-interactions/freshness/${contactId}`);
    return response.data;
  },

  /**
   * Get comprehensive data quality score for a contact
   */
  getContactQualityScore: async (contactId: string): Promise<DataQualityScore> => {
    const response = await httpClient.get(`/api/contact-interactions/quality-score/${contactId}`);
    return response.data;
  },

  /**
   * Get freshness statistics for all contacts
   */
  getFreshnessStatistics: async (): Promise<FreshnessStatistics> => {
    const response = await httpClient.get('/api/contact-interactions/statistics/freshness');
    return response.data;
  },

  /**
   * Get contacts filtered by freshness level
   */
  getContactsByFreshnessLevel: async (level: 'fresh' | 'aging' | 'stale' | 'critical'): Promise<Contact[]> => {
    const response = await httpClient.get(`/api/contact-interactions/contacts-by-freshness/${level}`);
    return response.data;
  },

  /**
   * Trigger manual data hygiene check
   */
  triggerHygieneCheck: async (): Promise<{ message: string }> => {
    const response = await httpClient.post('/api/contact-interactions/trigger-hygiene-check');
    return response.data;
  },

  // ===============================
  // Bulk Operations
  // ===============================

  /**
   * Get all stale contacts (convenience method)
   */
  getStaleContacts: async (): Promise<Contact[]> => {
    return contactInteractionApi.getContactsByFreshnessLevel('stale');
  },

  /**
   * Get all critical contacts (convenience method)
   */
  getCriticalContacts: async (): Promise<Contact[]> => {
    return contactInteractionApi.getContactsByFreshnessLevel('critical');
  },

  /**
   * Get contacts that need updates (stale + critical)
   */
  getContactsNeedingUpdate: async (): Promise<{ stale: Contact[], critical: Contact[] }> => {
    const [stale, critical] = await Promise.all([
      contactInteractionApi.getStaleContacts(),
      contactInteractionApi.getCriticalContacts()
    ]);
    
    return { stale, critical };
  }
};