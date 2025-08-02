// Components
export { HelpTooltip } from './components/HelpTooltip';
export { HelpModal } from './components/HelpModal';
export { HelpTour } from './components/HelpTour';
export { ProactiveHelp } from './components/ProactiveHelp';
export { HelpProvider } from './components/HelpProvider';

// Hooks
export { useHelp } from './hooks/useHelp';

// Store
export { useHelpStore } from './stores/helpStore';

// Types
export type {
  HelpContent,
  HelpType,
  UserLevel,
  StruggleType,
  HelpRequest,
  HelpResponse,
  HelpAnalytics,
  HelpFeedback,
  UserStruggle
} from './types/help.types';

// API
export { helpApi } from './services/helpApi';