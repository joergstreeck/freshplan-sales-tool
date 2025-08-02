import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';
import type { 
  HelpContent, 
  HelpResponse, 
  HelpAnalytics,
  UserStruggle,
  StruggleType 
} from '../types/help.types';
import { helpApi } from '../services/helpApi';

interface HelpState {
  // State
  currentHelp: HelpResponse | null;
  helpHistory: HelpContent[];
  analytics: HelpAnalytics | null;
  struggles: UserStruggle[];
  loading: boolean;
  error: string | null;
  
  // UI State
  tooltipOpen: Record<string, boolean>;
  modalOpen: boolean;
  modalContent: HelpContent | null;
  tourActive: boolean;
  tourStep: number;
  
  // Actions
  loadHelpContent: (feature: string, userId?: string) => Promise<void>;
  searchHelp: (query: string) => Promise<HelpContent[]>;
  submitFeedback: (helpId: string, helpful: boolean, comment?: string) => Promise<void>;
  trackView: (helpId: string) => Promise<void>;
  loadAnalytics: () => Promise<void>;
  
  // UI Actions
  openTooltip: (feature: string) => void;
  closeTooltip: (feature: string) => void;
  openModal: (content: HelpContent) => void;
  closeModal: () => void;
  startTour: (feature: string) => void;
  nextTourStep: () => void;
  previousTourStep: () => void;
  endTour: () => void;
  
  // Struggle Detection
  detectStruggle: (pattern: UserActionPattern) => void;
  reportStruggle: (struggle: UserStruggle) => Promise<void>;
  clearStruggles: () => void;
}

interface UserActionPattern {
  feature: string;
  actions: Array<{
    type: string;
    timestamp: number;
    success: boolean;
  }>;
}

export const useHelpStore = create<HelpState>()(
  immer((set, get) => ({
    // Initial State
    currentHelp: null,
    helpHistory: [],
    analytics: null,
    struggles: [],
    loading: false,
    error: null,
    tooltipOpen: {},
    modalOpen: false,
    modalContent: null,
    tourActive: false,
    tourStep: 0,
    
    // Load help content for a feature
    loadHelpContent: async (feature, userId) => {
      set((state) => {
        state.loading = true;
        state.error = null;
      });
      
      try {
        const response = await helpApi.getHelpContent({
          feature,
          userId,
          userLevel: 'BEGINNER', // TODO: Get from user context
          userRoles: ['sales'] // TODO: Get from user context
        });
        
        set((state) => {
          state.currentHelp = response;
          // Add to history (avoid duplicates)
          response.helpContents.forEach(content => {
            if (!state.helpHistory.find(h => h.id === content.id)) {
              state.helpHistory.push(content);
            }
          });
          state.loading = false;
        });
      } catch (error: any) {
        set((state) => {
          state.error = error.message || 'Failed to load help content';
          state.loading = false;
        });
      }
    },
    
    // Search help content
    searchHelp: async (query) => {
      try {
        const results = await helpApi.searchHelp(query);
        return results;
      } catch (error) {
        console.error('Help search failed:', error);
        return [];
      }
    },
    
    // Submit feedback
    submitFeedback: async (helpId, helpful, comment) => {
      try {
        await helpApi.submitFeedback({
          helpContentId: helpId,
          helpful,
          comment
        });
        
        // Update local state
        set((state) => {
          const content = state.helpHistory.find(h => h.id === helpId);
          if (content) {
            if (helpful) {
              content.helpfulCount++;
            } else {
              content.notHelpfulCount++;
            }
          }
        });
      } catch (error) {
        console.error('Failed to submit feedback:', error);
      }
    },
    
    // Track view
    trackView: async (helpId) => {
      try {
        await helpApi.trackView(helpId);
        
        // Update local view count
        set((state) => {
          const content = state.helpHistory.find(h => h.id === helpId);
          if (content) {
            content.viewCount++;
          }
        });
      } catch (error) {
        console.error('Failed to track view:', error);
      }
    },
    
    // Load analytics
    loadAnalytics: async () => {
      try {
        const analytics = await helpApi.getAnalytics();
        set((state) => {
          state.analytics = analytics;
        });
      } catch (error) {
        console.error('Failed to load analytics:', error);
      }
    },
    
    // UI Actions
    openTooltip: (feature) => {
      set((state) => {
        state.tooltipOpen[feature] = true;
      });
    },
    
    closeTooltip: (feature) => {
      set((state) => {
        state.tooltipOpen[feature] = false;
      });
    },
    
    openModal: (content) => {
      set((state) => {
        state.modalOpen = true;
        state.modalContent = content;
      });
      
      // Track view when modal opens
      get().trackView(content.id);
    },
    
    closeModal: () => {
      set((state) => {
        state.modalOpen = false;
        state.modalContent = null;
      });
    },
    
    startTour: (feature) => {
      set((state) => {
        state.tourActive = true;
        state.tourStep = 0;
      });
      
      // Load tour content
      get().loadHelpContent(feature);
    },
    
    nextTourStep: () => {
      set((state) => {
        const tourContent = state.currentHelp?.helpContents.filter(
          h => h.helpType === 'TOUR'
        ) || [];
        
        if (state.tourStep < tourContent.length - 1) {
          state.tourStep++;
        } else {
          state.tourActive = false;
        }
      });
    },
    
    previousTourStep: () => {
      set((state) => {
        if (state.tourStep > 0) {
          state.tourStep--;
        }
      });
    },
    
    endTour: () => {
      set((state) => {
        state.tourActive = false;
        state.tourStep = 0;
      });
    },
    
    // Struggle Detection
    detectStruggle: (pattern) => {
      const { actions } = pattern;
      if (actions.length < 3) return;
      
      // Check for repeated failures
      const recentFailures = actions.slice(-5).filter(a => !a.success);
      if (recentFailures.length >= 3) {
        get().reportStruggle({
          type: 'REPEATED_FAILED_ATTEMPTS',
          feature: pattern.feature,
          severity: 'high',
          attemptCount: recentFailures.length
        });
      }
      
      // Check for rapid navigation (5+ actions in 10 seconds)
      const recentActions = actions.slice(-5);
      const timeSpan = recentActions[recentActions.length - 1].timestamp - recentActions[0].timestamp;
      if (timeSpan < 10000 && recentActions.length >= 5) {
        get().reportStruggle({
          type: 'RAPID_NAVIGATION_CHANGES',
          feature: pattern.feature,
          severity: 'medium'
        });
      }
    },
    
    reportStruggle: async (struggle) => {
      set((state) => {
        state.struggles.push(struggle);
      });
      
      try {
        await helpApi.reportStruggle({
          feature: struggle.feature,
          type: struggle.type,
          context: struggle.context
        });
      } catch (error) {
        console.error('Failed to report struggle:', error);
      }
    },
    
    clearStruggles: () => {
      set((state) => {
        state.struggles = [];
      });
    }
  }))
);