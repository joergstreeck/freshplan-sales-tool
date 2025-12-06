import { httpClient } from '../../../lib/apiClient';
import type {
  HelpContent,
  HelpRequest,
  HelpResponse,
  HelpAnalytics,
  HelpFeedback,
  HelpType,
} from '../types/help.types';

// Backend Response Format (unterschiedlich von Frontend HelpResponse)
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

// Konvertiert Backend-Format zu Frontend-Format
function convertBackendResponse(backend: BackendHelpResponse, request: HelpRequest): HelpResponse {
  const helpContent: HelpContent = {
    id: backend.id,
    feature: backend.feature,
    helpType: backend.type || 'TOOLTIP',
    title: backend.title,
    shortContent: backend.content,
    mediumContent: backend.content,
    detailedContent: undefined,
    videoUrl: backend.videoUrl,
    priority: backend.priority,
    targetUserLevel: request.userLevel || 'BEGINNER',
    targetRoles: request.userRoles,
    viewCount: backend.viewCount,
    helpfulCount: 0,
    notHelpfulCount: 0,
    isActive: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  return {
    feature: backend.feature,
    helpContents: [helpContent],
    context: {
      userLevel: request.userLevel || 'BEGINNER',
      userRoles: request.userRoles || [],
      isFirstTime: false,
      previousInteractions: backend.viewCount,
    },
    struggleDetected: backend.struggleDetected,
    struggleType: backend.struggleType,
    suggestionLevel:
      backend.suggestionLevel === 0 ? 'low' : backend.suggestionLevel === 1 ? 'medium' : 'high',
  };
}

export const helpApi = {
  // Health Check
  checkHealth: async (): Promise<{ status: string; totalViews: number; activeContent: number }> => {
    const response = await httpClient.get<{
      status: string;
      totalViews: number;
      activeContent: number;
    }>('/api/help/health');
    return response.data;
  },

  // Get contextual help for a feature
  getHelpContent: async (request: HelpRequest): Promise<HelpResponse> => {
    const params = new URLSearchParams();
    params.append('feature', request.feature);
    if (request.userId) params.append('userId', request.userId);
    if (request.userLevel) params.append('userLevel', request.userLevel);
    if (request.userRoles?.length) {
      request.userRoles.forEach(role => params.append('userRoles', role));
    }

    const response = await httpClient.get<BackendHelpResponse>(
      `/api/help/content/${request.feature}?${params.toString()}`
    );

    // Konvertiere Backend-Format zu Frontend-Format
    return convertBackendResponse(response.data, request);
  },

  // Search help content
  searchHelp: async (query: string, userLevel?: string): Promise<HelpContent[]> => {
    const params = new URLSearchParams();
    params.append('query', query);
    if (userLevel) params.append('userLevel', userLevel);

    const response = await httpClient.get<HelpContent[]>(`/api/help/search?${params.toString()}`);
    return response.data;
  },

  // Submit feedback
  submitFeedback: async (feedback: HelpFeedback): Promise<void> => {
    await httpClient.post(`/api/help/content/${feedback.helpContentId}/feedback`, {
      helpful: feedback.helpful,
      comment: feedback.comment,
    });
  },

  // Track help view
  trackView: async (helpContentId: string): Promise<void> => {
    await httpClient.post(`/api/help/content/${helpContentId}/view`);
  },

  // Get analytics
  getAnalytics: async (): Promise<HelpAnalytics> => {
    const response = await httpClient.get<HelpAnalytics>('/api/help/analytics');
    return response.data;
  },

  // Get feature-specific analytics
  getFeatureAnalytics: async (feature: string): Promise<HelpAnalytics> => {
    const response = await httpClient.get<HelpAnalytics>(`/api/help/analytics/${feature}`);
    return response.data;
  },

  // Report user struggle
  reportStruggle: async (struggle: {
    feature: string;
    type: string;
    context?: Record<string, unknown>;
  }): Promise<void> => {
    await httpClient.post('/api/help/struggle', struggle);
  },
};
