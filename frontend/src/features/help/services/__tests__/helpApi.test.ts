/**
 * Unit Tests für helpApi.ts
 *
 * Sprint 2.1.8 - Issue #149
 * Testet die Format-Konvertierung von Backend zu Frontend
 *
 * Fokus:
 * - convertBackendResponse() Funktion
 * - Korrektes Mapping aller Felder
 * - Edge Cases (fehlende optionale Felder)
 *
 * @module Tests/Features/Help/helpApi
 * @since Sprint 2.1.8
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { HelpRequest, HelpType } from '../../types/help.types';

// Mock httpClient
vi.mock('../../../../lib/apiClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

import { httpClient } from '../../../../lib/apiClient';
import { helpApi } from '../helpApi';

// Backend Response Format (wie in helpApi.ts definiert)
interface BackendHelpResponse {
  id: string;
  feature: string;
  title: string;
  type: HelpType;
  content: string;
  videoUrl?: string;
  interactionData?: string;
  priority: number;
  struggleDetected: boolean;
  struggleType?: string;
  suggestionLevel: number;
  viewCount: number;
  helpfulnessRate: number;
}

describe('helpApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getHelpContent - Format-Konvertierung', () => {
    it('should convert backend response to frontend format correctly', async () => {
      // Arrange
      const mockBackendResponse: BackendHelpResponse = {
        id: 'help-123',
        feature: 'lead-import',
        title: 'Lead Import Hilfe',
        type: 'TOOLTIP',
        content: 'So importieren Sie Leads...',
        videoUrl: 'https://example.com/video.mp4',
        priority: 1,
        struggleDetected: false,
        suggestionLevel: 0,
        viewCount: 42,
        helpfulnessRate: 0.85,
      };

      const mockRequest: HelpRequest = {
        feature: 'lead-import',
        userId: 'user-1',
        userLevel: 'BEGINNER',
        userRoles: ['SALES'],
      };

      vi.mocked(httpClient.get).mockResolvedValueOnce({ data: mockBackendResponse });

      // Act
      const result = await helpApi.getHelpContent(mockRequest);

      // Assert
      expect(result.feature).toBe('lead-import');
      expect(result.helpContents).toHaveLength(1);

      const content = result.helpContents[0];
      expect(content.id).toBe('help-123');
      expect(content.feature).toBe('lead-import');
      expect(content.helpType).toBe('TOOLTIP');
      expect(content.title).toBe('Lead Import Hilfe');
      expect(content.shortContent).toBe('So importieren Sie Leads...');
      expect(content.mediumContent).toBe('So importieren Sie Leads...');
      expect(content.videoUrl).toBe('https://example.com/video.mp4');
      expect(content.priority).toBe(1);
      expect(content.targetUserLevel).toBe('BEGINNER');
      expect(content.targetRoles).toEqual(['SALES']);
      expect(content.viewCount).toBe(42);
      expect(content.isActive).toBe(true);
    });

    it('should handle missing optional fields gracefully', async () => {
      // Arrange
      const mockBackendResponse: BackendHelpResponse = {
        id: 'help-456',
        feature: 'customers',
        title: 'Kunden verwalten',
        type: 'MODAL',
        content: 'Kunden-Hilfe Text',
        priority: 2,
        struggleDetected: true,
        struggleType: 'REPEATED_FAILED_ATTEMPTS',
        suggestionLevel: 2,
        viewCount: 10,
        helpfulnessRate: 0.5,
      };

      const mockRequest: HelpRequest = {
        feature: 'customers',
      };

      vi.mocked(httpClient.get).mockResolvedValueOnce({ data: mockBackendResponse });

      // Act
      const result = await helpApi.getHelpContent(mockRequest);

      // Assert
      expect(result.context.userLevel).toBe('BEGINNER'); // Default
      expect(result.context.userRoles).toEqual([]); // Empty array default
      expect(result.struggleDetected).toBe(true);
      expect(result.struggleType).toBe('REPEATED_FAILED_ATTEMPTS');

      const content = result.helpContents[0];
      expect(content.videoUrl).toBeUndefined();
      expect(content.detailedContent).toBeUndefined();
    });

    it('should map suggestionLevel correctly (0=low, 1=medium, 2+=high)', async () => {
      const testCases = [
        { suggestionLevel: 0, expected: 'low' },
        { suggestionLevel: 1, expected: 'medium' },
        { suggestionLevel: 2, expected: 'high' },
        { suggestionLevel: 5, expected: 'high' },
      ];

      for (const { suggestionLevel, expected } of testCases) {
        const mockBackendResponse: BackendHelpResponse = {
          id: `help-${suggestionLevel}`,
          feature: 'test',
          title: 'Test',
          type: 'FAQ',
          content: 'Test content',
          priority: 1,
          struggleDetected: false,
          suggestionLevel,
          viewCount: 0,
          helpfulnessRate: 0,
        };

        vi.mocked(httpClient.get).mockResolvedValueOnce({ data: mockBackendResponse });

        const result = await helpApi.getHelpContent({ feature: 'test' });

        expect(result.suggestionLevel).toBe(expected);
      }
    });

    it('should default helpType to TOOLTIP when type is missing', async () => {
      // Arrange - Backend response ohne type
      const mockBackendResponse = {
        id: 'help-no-type',
        feature: 'test',
        title: 'Test ohne Typ',
        type: undefined as unknown as HelpType,
        content: 'Content',
        priority: 1,
        struggleDetected: false,
        suggestionLevel: 0,
        viewCount: 0,
        helpfulnessRate: 0,
      };

      vi.mocked(httpClient.get).mockResolvedValueOnce({ data: mockBackendResponse });

      // Act
      const result = await helpApi.getHelpContent({ feature: 'test' });

      // Assert
      expect(result.helpContents[0].helpType).toBe('TOOLTIP');
    });

    it('should build correct URL parameters', async () => {
      const mockRequest: HelpRequest = {
        feature: 'lead-import',
        userId: 'user-123',
        userLevel: 'EXPERT',
        userRoles: ['ADMIN', 'MANAGER'],
      };

      vi.mocked(httpClient.get).mockResolvedValueOnce({
        data: {
          id: 'test',
          feature: 'lead-import',
          title: 'Test',
          type: 'TOOLTIP',
          content: 'Test',
          priority: 1,
          struggleDetected: false,
          suggestionLevel: 0,
          viewCount: 0,
          helpfulnessRate: 0,
        },
      });

      await helpApi.getHelpContent(mockRequest);

      expect(httpClient.get).toHaveBeenCalledWith(
        expect.stringContaining('/api/help/content/lead-import?')
      );
      expect(httpClient.get).toHaveBeenCalledWith(expect.stringContaining('userId=user-123'));
      expect(httpClient.get).toHaveBeenCalledWith(expect.stringContaining('userLevel=EXPERT'));
      expect(httpClient.get).toHaveBeenCalledWith(expect.stringContaining('userRoles=ADMIN'));
      expect(httpClient.get).toHaveBeenCalledWith(expect.stringContaining('userRoles=MANAGER'));
    });
  });

  describe('checkHealth', () => {
    it('should return health status', async () => {
      const mockHealth = {
        status: 'UP',
        totalViews: 1000,
        activeContent: 50,
      };

      vi.mocked(httpClient.get).mockResolvedValueOnce({ data: mockHealth });

      const result = await helpApi.checkHealth();

      expect(result).toEqual(mockHealth);
      expect(httpClient.get).toHaveBeenCalledWith('/api/help/health');
    });
  });

  describe('searchHelp', () => {
    it('should search with query and userLevel', async () => {
      const mockResults = [{ id: '1', feature: 'test', helpType: 'FAQ', title: 'Result 1' }];

      vi.mocked(httpClient.get).mockResolvedValueOnce({ data: mockResults });

      await helpApi.searchHelp('import', 'BEGINNER');

      expect(httpClient.get).toHaveBeenCalledWith(
        expect.stringContaining('/api/help/search?query=import')
      );
      expect(httpClient.get).toHaveBeenCalledWith(expect.stringContaining('userLevel=BEGINNER'));
    });

    it('should search without userLevel', async () => {
      vi.mocked(httpClient.get).mockResolvedValueOnce({ data: [] });

      await helpApi.searchHelp('test');

      const call = vi.mocked(httpClient.get).mock.calls[0][0];
      expect(call).toContain('query=test');
      expect(call).not.toContain('userLevel');
    });
  });

  describe('submitFeedback', () => {
    it('should submit feedback correctly', async () => {
      vi.mocked(httpClient.post).mockResolvedValueOnce({ data: undefined });

      await helpApi.submitFeedback({
        helpContentId: 'help-123',
        helpful: true,
        comment: 'Sehr hilfreich!',
      });

      expect(httpClient.post).toHaveBeenCalledWith('/api/help/content/help-123/feedback', {
        helpful: true,
        comment: 'Sehr hilfreich!',
      });
    });
  });

  describe('trackView', () => {
    it('should track view correctly', async () => {
      vi.mocked(httpClient.post).mockResolvedValueOnce({ data: undefined });

      await helpApi.trackView('help-456');

      expect(httpClient.post).toHaveBeenCalledWith('/api/help/content/help-456/view');
    });
  });

  describe('getAnalytics', () => {
    it('should get analytics', async () => {
      const mockAnalytics = {
        totalViews: 5000,
        totalFeedback: 200,
        overallHelpfulnessRate: 0.78,
        mostRequestedTopics: [],
        mostHelpfulContent: [],
        contentNeedingImprovement: [],
        featureCoverage: 0.85,
        coverageGaps: ['new-feature'],
        helpRequestsByFeature: {},
        struggleDetectionsByType: {},
        averageResponseTime: 150,
        userSatisfactionScore: 4.2,
      };

      vi.mocked(httpClient.get).mockResolvedValueOnce({ data: mockAnalytics });

      const result = await helpApi.getAnalytics();

      expect(result).toEqual(mockAnalytics);
      expect(httpClient.get).toHaveBeenCalledWith('/api/help/analytics');
    });
  });

  describe('getFeatureAnalytics', () => {
    it('should get feature-specific analytics', async () => {
      vi.mocked(httpClient.get).mockResolvedValueOnce({ data: {} });

      await helpApi.getFeatureAnalytics('lead-import');

      expect(httpClient.get).toHaveBeenCalledWith('/api/help/analytics/lead-import');
    });
  });

  describe('reportStruggle', () => {
    it('should report struggle with context', async () => {
      vi.mocked(httpClient.post).mockResolvedValueOnce({ data: undefined });

      await helpApi.reportStruggle({
        feature: 'lead-import',
        type: 'REPEATED_FAILED_ATTEMPTS',
        context: { attemptCount: 5 },
      });

      expect(httpClient.post).toHaveBeenCalledWith('/api/help/struggle', {
        feature: 'lead-import',
        type: 'REPEATED_FAILED_ATTEMPTS',
        context: { attemptCount: 5 },
      });
    });
  });
});
