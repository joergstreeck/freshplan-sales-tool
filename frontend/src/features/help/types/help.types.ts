// Help System Types gemäß Backend-Implementierung und Sprint2/Step3 Architektur

export type HelpType = 
  | 'TOOLTIP'
  | 'TOUR'
  | 'MODAL'
  | 'VIDEO'
  | 'FAQ'
  | 'ANNOUNCEMENT';

export type UserLevel = 
  | 'BEGINNER' 
  | 'INTERMEDIATE' 
  | 'EXPERT';

export type StruggleType = 
  | 'REPEATED_FAILED_ATTEMPTS'
  | 'RAPID_NAVIGATION_CHANGES'
  | 'LONG_IDLE_AFTER_START'
  | 'ABANDONED_WORKFLOW_PATTERN'
  | 'COMPLEX_FORM_STRUGGLE';

export interface HelpContent {
  id: string;
  feature: string;
  helpType: HelpType;
  title: string;
  shortContent: string;
  mediumContent?: string;
  detailedContent?: string;
  videoUrl?: string;
  priority: number;
  targetUserLevel: UserLevel;
  targetRoles?: string[];
  viewCount: number;
  helpfulCount: number;
  notHelpfulCount: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface HelpRequest {
  feature: string;
  userId?: string;
  userLevel?: UserLevel;
  userRoles?: string[];
  context?: Record<string, unknown>;
}

export interface HelpResponse {
  feature: string;
  helpContents: HelpContent[];
  context: {
    userLevel: UserLevel;
    userRoles: string[];
    isFirstTime: boolean;
    previousInteractions: number;
  };
  suggestions?: HelpSuggestion[];
  struggleDetected?: boolean;
  struggleType?: string;
  suggestionLevel?: 'low' | 'medium' | 'high';
}

export interface HelpSuggestion {
  type: 'video' | 'tour' | 'faq' | 'support';
  label: string;
  action: () => void;
  icon?: React.ReactNode;
}

export interface HelpAnalytics {
  totalViews: number;
  totalFeedback: number;
  overallHelpfulnessRate: number;
  mostRequestedTopics: HelpTopicStats[];
  mostHelpfulContent: HelpContent[];
  contentNeedingImprovement: HelpContent[];
  featureCoverage: number;
  coverageGaps: string[];
  helpRequestsByFeature: Record<string, number>;
  struggleDetectionsByType: Record<string, number>;
  averageResponseTime: number;
  userSatisfactionScore: number;
}

export interface HelpTopicStats {
  id: string;
  title: string;
  feature: string;
  requests: number;
  helpfulRate: number;
}

export interface HelpFeedback {
  helpContentId: string;
  helpful: boolean;
  comment?: string;
}

export interface UserStruggle {
  type: StruggleType;
  feature: string;
  severity: 'low' | 'medium' | 'high';
  attemptCount?: number;
  timeSpent?: number;
  context?: Record<string, unknown>;
}