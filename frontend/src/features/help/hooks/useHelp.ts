import { useCallback, useEffect, useMemo } from 'react';
import { useHelpStore } from '../stores/helpStore';
import type { HelpContent } from '../types/help.types';

interface UseHelpOptions {
  feature: string;
  autoLoad?: boolean;
  userId?: string;
}

export const useHelp = ({ feature, autoLoad = false, userId }: UseHelpOptions) => {
  const {
    currentHelp,
    loading,
    error,
    loadHelpContent,
    openTooltip,
    openModal,
    startTour,
    submitFeedback,
    searchHelp
  } = useHelpStore();
  
  // Auto-load help content if requested
  useEffect(() => {
    if (autoLoad) {
      loadHelpContent(feature, userId);
    }
  }, [feature, userId, autoLoad, loadHelpContent]);
  
  // Get help content for this feature
  const helpContent = useMemo(() => 
    currentHelp?.helpContents.filter(h => h.feature === feature) || [],
    [currentHelp, feature]
  );
  
  // Get specific help types
  const tooltip = helpContent.find(h => h.helpType === 'TOOLTIP');
  const tour = helpContent.find(h => h.helpType === 'TOUR');
  const faq = helpContent.filter(h => h.helpType === 'FAQ');
  const videos = helpContent.filter(h => h.helpType === 'VIDEO');
  
  // Convenience methods
  const showTooltip = useCallback(() => {
    if (!helpContent.length) {
      loadHelpContent(feature, userId);
    }
    openTooltip(feature);
  }, [feature, userId, helpContent.length, loadHelpContent, openTooltip]);
  
  const showHelp = useCallback((content?: HelpContent) => {
    if (content) {
      openModal(content);
    } else if (tooltip) {
      openModal(tooltip);
    } else if (helpContent.length > 0) {
      openModal(helpContent[0]);
    } else {
      // Load and then show
      loadHelpContent(feature, userId).then(() => {
        const help = useHelpStore.getState().currentHelp?.helpContents[0];
        if (help) openModal(help);
      });
    }
  }, [feature, userId, tooltip, helpContent, loadHelpContent, openModal]);
  
  const showTour = useCallback(() => {
    if (!tour) {
      loadHelpContent(feature, userId);
    }
    startTour(feature);
  }, [feature, userId, tour, loadHelpContent, startTour]);
  
  return {
    // State
    loading,
    error,
    hasHelp: helpContent.length > 0,
    
    // Content
    helpContent,
    tooltip,
    tour,
    faq,
    videos,
    
    // Actions
    showTooltip,
    showHelp,
    showTour,
    submitFeedback,
    searchHelp,
    
    // Context
    context: currentHelp?.context,
    suggestions: currentHelp?.suggestions
  };
};