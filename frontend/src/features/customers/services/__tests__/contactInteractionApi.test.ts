import { contactInteractionApi } from '../contactInteractionApi';
import { httpClient } from '@/lib/apiClient';
import type {
  ContactInteractionDTO,
  WarmthScoreDTO,
  DataQualityMetricsDTO,
  BatchImportRequest
} from '../../types/intelligence.types';

// Mock the httpClient
import { vi } from 'vitest';
vi.mock('@/lib/apiClient');
const mockedHttpClient = httpClient as vi.Mocked<typeof httpClient>;

describe('Contact Interaction API Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Data Quality Metrics', () => {
    it('should fetch data quality metrics successfully', async () => {
      // Arrange
      const mockMetrics: DataQualityMetricsDTO = {
        totalContacts: 100,
        contactsWithInteractions: 75,
        averageInteractionsPerContact: 3.2,
        dataCompletenessScore: 80,
        contactsWithWarmthScore: 60,
        freshContacts: 50,
        agingContacts: 30,
        staleContacts: 15,
        criticalContacts: 5,
        showDataCollectionHints: true,
        criticalDataGaps: ['5 Kontakte über 1 Jahr nicht aktualisiert'],
        improvementSuggestions: ['Sofortige Überprüfung kritischer Kontakte'],
        overallDataQuality: 'GOOD',
        interactionCoverage: 75
      };

      mockedHttpClient.get.mockResolvedValue({ data: mockMetrics, status: 200, statusText: 'OK' });

      // Act
      const result = await contactInteractionApi.getDataQualityMetrics();

      // Assert
      expect(mockedHttpClient.get).toHaveBeenCalledWith('/api/contact-interactions/metrics/data-quality');
      expect(result).toEqual(mockMetrics);
    });

    it('should handle API errors gracefully', async () => {
      // Arrange
      const apiError = new Error('Network error');
      mockedHttpClient.get.mockRejectedValue(apiError);

      // Act & Assert
      await expect(contactInteractionApi.getDataQualityMetrics()).rejects.toThrow('Network error');
      expect(mockedHttpClient.get).toHaveBeenCalledWith('/api/contact-interactions/metrics/data-quality');
    });
  });

  describe('Contact Interactions CRUD', () => {
    const mockContactId = '123e4567-e89b-12d3-a456-426614174000';
    
    it('should create interaction successfully', async () => {
      // Arrange
      const newInteraction: ContactInteractionDTO = {
        contactId: mockContactId,
        type: 'EMAIL_SENT',
        timestamp: '2025-08-02T10:00:00',
        subject: 'Test Email',
        notes: 'Test content',
        sentimentScore: 0.8,
        engagementScore: 85
      };

      const createdInteraction: ContactInteractionDTO = {
        ...newInteraction,
        id: 'interaction-123',
        createdAt: '2025-08-02T10:00:00',
        createdBy: 'user-123'
      };

      mockedHttpClient.post.mockResolvedValue({ 
        data: createdInteraction, 
        status: 201, 
        statusText: 'Created' 
      });

      // Act
      const result = await contactInteractionApi.createInteraction(newInteraction);

      // Assert
      expect(mockedHttpClient.post).toHaveBeenCalledWith('/api/contact-interactions', newInteraction);
      expect(result).toEqual(createdInteraction);
    });

    it('should fetch contact interactions', async () => {
      // Arrange
      const mockInteractions: ContactInteractionDTO[] = [
        {
          id: 'int-1',
          contactId: mockContactId,
          type: 'EMAIL_SENT',
          timestamp: '2025-08-02T10:00:00',
          subject: 'First Email'
        },
        {
          id: 'int-2',
          contactId: mockContactId,
          type: 'EMAIL_RECEIVED',
          timestamp: '2025-08-02T11:00:00',
          subject: 'Response Email'
        }
      ];

      mockedHttpClient.get.mockResolvedValue({ 
        data: mockInteractions, 
        status: 200, 
        statusText: 'OK' 
      });

      // Act
      const result = await contactInteractionApi.getContactInteractions(mockContactId);

      // Assert
      expect(mockedHttpClient.get).toHaveBeenCalledWith(`/api/contact-interactions/contact/${mockContactId}`);
      expect(result).toEqual(mockInteractions);
    });

    it('should update interaction successfully', async () => {
      // Arrange
      const interactionId = 'interaction-123';
      const updates: Partial<ContactInteractionDTO> = {
        notes: 'Updated notes',
        sentimentScore: 0.9
      };

      const updatedInteraction: ContactInteractionDTO = {
        id: interactionId,
        contactId: mockContactId,
        type: 'EMAIL_SENT',
        timestamp: '2025-08-02T10:00:00',
        notes: 'Updated notes',
        sentimentScore: 0.9
      };

      mockedHttpClient.put.mockResolvedValue({ 
        data: updatedInteraction, 
        status: 200, 
        statusText: 'OK' 
      });

      // Act
      const result = await contactInteractionApi.updateInteraction(interactionId, updates);

      // Assert
      expect(mockedHttpClient.put).toHaveBeenCalledWith(`/api/contact-interactions/${interactionId}`, updates);
      expect(result).toEqual(updatedInteraction);
    });

    it('should delete interaction successfully', async () => {
      // Arrange
      const interactionId = 'interaction-123';
      mockedHttpClient.delete.mockResolvedValue({ 
        data: null, 
        status: 204, 
        statusText: 'No Content' 
      });

      // Act
      await contactInteractionApi.deleteInteraction(interactionId);

      // Assert
      expect(mockedHttpClient.delete).toHaveBeenCalledWith(`/api/contact-interactions/${interactionId}`);
    });
  });

  describe('Warmth Score Operations', () => {
    const mockContactId = '123e4567-e89b-12d3-a456-426614174000';

    it('should calculate warmth score successfully', async () => {
      // Arrange
      const mockWarmthScore: WarmthScoreDTO = {
        contactId: mockContactId,
        score: 75,
        confidence: 85,
        lastCalculated: '2025-08-02T10:00:00',
        factors: {
          frequency: 80,
          sentiment: 70,
          engagement: 75,
          response: 80
        },
        trend: 'INCREASING',
        recommendations: [
          'Kontakt ist sehr engagiert',
          'Guter Zeitpunkt für ein Angebot'
        ]
      };

      mockedHttpClient.post.mockResolvedValue({ 
        data: mockWarmthScore, 
        status: 200, 
        statusText: 'OK' 
      });

      // Act
      const result = await contactInteractionApi.calculateWarmthScore(mockContactId);

      // Assert
      expect(mockedHttpClient.post).toHaveBeenCalledWith(`/api/contact-interactions/warmth/${mockContactId}/calculate`);
      expect(result).toEqual(mockWarmthScore);
      expect(result.score).toBeGreaterThanOrEqual(0);
      expect(result.score).toBeLessThanOrEqual(100);
      expect(result.confidence).toBeGreaterThanOrEqual(0);
      expect(result.confidence).toBeLessThanOrEqual(100);
    });

    it('should get existing warmth score', async () => {
      // Arrange
      const mockWarmthScore: WarmthScoreDTO = {
        contactId: mockContactId,
        score: 65,
        confidence: 70,
        lastCalculated: '2025-08-01T15:30:00',
        factors: {
          frequency: 60,
          sentiment: 75,
          engagement: 65,
          response: 60
        },
        trend: 'STABLE',
        recommendations: ['Regelmäßigen Kontakt halten']
      };

      mockedHttpClient.get.mockResolvedValue({ 
        data: mockWarmthScore, 
        status: 200, 
        statusText: 'OK' 
      });

      // Act
      const result = await contactInteractionApi.getWarmthScore(mockContactId);

      // Assert
      expect(mockedHttpClient.get).toHaveBeenCalledWith(`/api/contact-interactions/warmth/${mockContactId}`);
      expect(result).toEqual(mockWarmthScore);
    });
  });

  describe('Specialized Recording Methods', () => {
    const mockContactId = '123e4567-e89b-12d3-a456-426614174000';

    it('should record note interaction', async () => {
      // Arrange
      const noteContent = 'Important meeting notes';
      const mockInteraction: ContactInteractionDTO = {
        id: 'note-123',
        contactId: mockContactId,
        type: 'NOTE_ADDED',
        timestamp: '2025-08-02T10:00:00',
        notes: noteContent
      };

      mockedHttpClient.post.mockResolvedValue({ 
        data: mockInteraction, 
        status: 201, 
        statusText: 'Created' 
      });

      // Act
      const result = await contactInteractionApi.recordNote(mockContactId, noteContent);

      // Assert
      expect(mockedHttpClient.post).toHaveBeenCalledWith(
        `/api/contact-interactions/note/${mockContactId}`, 
        { note: noteContent }
      );
      expect(result).toEqual(mockInteraction);
      expect(result.type).toBe('NOTE_ADDED');
      expect(result.notes).toBe(noteContent);
    });

    it('should record email interaction', async () => {
      // Arrange
      const mockInteraction: ContactInteractionDTO = {
        id: 'email-123',
        contactId: mockContactId,
        type: 'EMAIL_SENT',
        timestamp: '2025-08-02T10:00:00',
        subject: 'Follow-up Email',
        sentimentScore: 0.7
      };

      mockedHttpClient.post.mockResolvedValue({ 
        data: mockInteraction, 
        status: 201, 
        statusText: 'Created' 
      });

      // Act
      const result = await contactInteractionApi.recordEmail(
        mockContactId, 
        'SENT', 
        'Follow-up Email', 
        0.7
      );

      // Assert
      expect(mockedHttpClient.post).toHaveBeenCalledWith(
        `/api/contact-interactions/email/${mockContactId}`, 
        { 
          type: 'SENT', 
          subject: 'Follow-up Email', 
          sentiment: 0.7 
        }
      );
      expect(result).toEqual(mockInteraction);
    });

    it('should record call interaction', async () => {
      // Arrange
      const mockInteraction: ContactInteractionDTO = {
        id: 'call-123',
        contactId: mockContactId,
        type: 'CALL_OUTBOUND',
        timestamp: '2025-08-02T10:00:00',
        duration: 1800,
        outcome: 'Successful discussion'
      };

      mockedHttpClient.post.mockResolvedValue({ 
        data: mockInteraction, 
        status: 201, 
        statusText: 'Created' 
      });

      // Act
      const result = await contactInteractionApi.recordCall(
        mockContactId, 
        'OUTBOUND', 
        1800, 
        'Successful discussion'
      );

      // Assert
      expect(mockedHttpClient.post).toHaveBeenCalledWith(
        `/api/contact-interactions/call/${mockContactId}`, 
        { 
          type: 'OUTBOUND', 
          duration: 1800, 
          outcome: 'Successful discussion' 
        }
      );
      expect(result).toEqual(mockInteraction);
    });

    it('should record meeting interaction', async () => {
      // Arrange
      const mockInteraction: ContactInteractionDTO = {
        id: 'meeting-123',
        contactId: mockContactId,
        type: 'MEETING_COMPLETED',
        timestamp: '2025-08-02T10:00:00',
        duration: 3600,
        notes: 'Productive meeting with next steps defined'
      };

      mockedHttpClient.post.mockResolvedValue({ 
        data: mockInteraction, 
        status: 201, 
        statusText: 'Created' 
      });

      // Act
      const result = await contactInteractionApi.recordMeeting(
        mockContactId, 
        'COMPLETED', 
        3600, 
        'Productive meeting with next steps defined'
      );

      // Assert
      expect(mockedHttpClient.post).toHaveBeenCalledWith(
        `/api/contact-interactions/meeting/${mockContactId}`, 
        { 
          type: 'COMPLETED', 
          duration: 3600, 
          notes: 'Productive meeting with next steps defined' 
        }
      );
      expect(result).toEqual(mockInteraction);
    });
  });

  describe('Batch Operations', () => {
    it('should handle batch import successfully', async () => {
      // Arrange
      const batchRequest: BatchImportRequest = {
        interactions: [
          {
            contactId: 'contact-1',
            type: 'EMAIL_SENT',
            timestamp: '2025-08-01T10:00:00',
            subject: 'Imported Email 1'
          },
          {
            contactId: 'contact-2',
            type: 'CALL_OUTBOUND',
            timestamp: '2025-08-01T11:00:00',
            duration: 1200
          }
        ],
        source: 'outlook-import',
        importedBy: 'user-123'
      };

      const batchResult = { imported: 2, failed: 0 };

      mockedHttpClient.post.mockResolvedValue({ 
        data: batchResult, 
        status: 200, 
        statusText: 'OK' 
      });

      // Act
      const result = await contactInteractionApi.batchImport(batchRequest);

      // Assert
      expect(mockedHttpClient.post).toHaveBeenCalledWith('/api/contact-interactions/batch-import', batchRequest);
      expect(result).toEqual(batchResult);
      expect(result.imported).toBe(2);
      expect(result.failed).toBe(0);
    });

    it('should handle partial batch import failures', async () => {
      // Arrange
      const batchRequest: BatchImportRequest = {
        interactions: [
          {
            contactId: 'valid-contact',
            type: 'EMAIL_SENT',
            timestamp: '2025-08-01T10:00:00',
            subject: 'Valid Email'
          },
          {
            contactId: 'invalid-contact',
            type: 'EMAIL_SENT',
            timestamp: 'invalid-date',
            subject: 'Invalid Email'
          }
        ],
        source: 'csv-import',
        importedBy: 'user-123'
      };

      const batchResult = { imported: 1, failed: 1 };

      mockedHttpClient.post.mockResolvedValue({ 
        data: batchResult, 
        status: 200, 
        statusText: 'OK' 
      });

      // Act
      const result = await contactInteractionApi.batchImport(batchRequest);

      // Assert
      expect(result.imported).toBe(1);
      expect(result.failed).toBe(1);
    });
  });

  describe('Error Scenarios', () => {
    it('should handle 404 errors for non-existent contacts', async () => {
      // Arrange
      const nonExistentContactId = 'non-existent-id';
      mockedHttpClient.get.mockRejectedValue({
        code: 'HTTP_404',
        message: 'Contact not found'
      });

      // Act & Assert
      await expect(
        contactInteractionApi.getContactInteractions(nonExistentContactId)
      ).rejects.toMatchObject({
        code: 'HTTP_404',
        message: 'Contact not found'
      });
    });

    it('should handle validation errors on creation', async () => {
      // Arrange
      const invalidInteraction: ContactInteractionDTO = {
        contactId: '', // Invalid empty contact ID
        type: 'EMAIL_SENT',
        timestamp: '2025-08-02T10:00:00'
      };

      mockedHttpClient.post.mockRejectedValue({
        code: 'HTTP_400',
        message: 'Validation failed'
      });

      // Act & Assert
      await expect(
        contactInteractionApi.createInteraction(invalidInteraction)
      ).rejects.toMatchObject({
        code: 'HTTP_400',
        message: 'Validation failed'
      });
    });

    it('should handle network errors', async () => {
      // Arrange
      mockedHttpClient.get.mockRejectedValue({
        code: 'CONNECTION_FAILED',
        message: 'Backend not reachable'
      });

      // Act & Assert
      await expect(
        contactInteractionApi.getDataQualityMetrics()
      ).rejects.toMatchObject({
        code: 'CONNECTION_FAILED',
        message: 'Backend not reachable'
      });
    });
  });
});