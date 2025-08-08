import React, { useEffect } from 'react';
import { HelpModal } from './HelpModal';
import { HelpTour } from './HelpTour';
import { ProactiveHelp } from './ProactiveHelp';
import { useHelpStore } from '../stores/helpStore';

interface HelpProviderProps {
  children: React.ReactNode;
}

export const HelpProvider: React.FC<HelpProviderProps> = ({ children }) => {
  const { detectStruggle } = useHelpStore();

  // Track user actions for struggle detection
  useEffect(() => {
    const actions: Array<{ type: string; timestamp: number; success: boolean }> = [];

    const trackAction = (type: string, success: boolean) => {
      actions.push({
        type,
        timestamp: Date.now(),
        success,
      });

      // Keep only last 10 actions
      if (actions.length > 10) {
        actions.shift();
      }

      // Analyze pattern
      detectStruggle({
        feature: window.location.pathname,
        actions: [...actions],
      });
    };

    // Track navigation
    const handleNavigation = () => {
      trackAction('navigation', true);
    };

    // Track form submissions
    const handleFormSubmit = (e: Event) => {
      const form = e.target as HTMLFormElement;
      const isValid = form.checkValidity();
      trackAction('form-submit', isValid);
    };

    // Track clicks on error messages
    const handleErrorClick = (e: Event) => {
      const target = e.target as HTMLElement;
      if (target.closest('.MuiAlert-root')) {
        trackAction('error-interaction', false);
      }
    };

    // Add event listeners
    window.addEventListener('popstate', handleNavigation);
    document.addEventListener('submit', handleFormSubmit);
    document.addEventListener('click', handleErrorClick);

    return () => {
      window.removeEventListener('popstate', handleNavigation);
      document.removeEventListener('submit', handleFormSubmit);
      document.removeEventListener('click', handleErrorClick);
    };
  }, [detectStruggle]);

  return (
    <>
      {children}
      <HelpModal />
      <HelpTour />
      <ProactiveHelp />
    </>
  );
};
