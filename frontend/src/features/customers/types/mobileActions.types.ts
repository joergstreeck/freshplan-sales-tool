/**
 * Mobile Actions Type Definitions
 * 
 * Types for mobile-optimized contact actions and swipe gestures.
 * Part of FC-005 Contact Management UI - Mobile Actions.
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MOBILE_CONTACT_ACTIONS.md
 */

import type { ReactNode } from 'react';

/**
 * Quick action definition for mobile interactions
 */
export interface QuickAction {
  id: string;
  type: 'call' | 'email' | 'whatsapp' | 'sms' | 'calendar' | 'note' | 'meeting';
  label: string;
  icon: ReactNode;
  color: string;
  primary?: boolean;      // User-defined primary action
  contextual?: boolean;   // Based on contact data availability
  urgency?: 'high' | 'medium' | 'low';
  enabled?: boolean;      // Whether action is currently available
}

/**
 * Swipe action configuration
 */
export interface SwipeActions {
  left: QuickAction | null;   // Swipe left action
  right: QuickAction | null;  // Swipe right action
}

/**
 * Contact action configuration per contact
 */
export interface ContactActionConfig {
  contactId: string;
  primaryAction: QuickAction;
  secondaryActions: QuickAction[];
  swipeActions: SwipeActions;
  lastUsedAction?: string;
  actionHistory: ActionHistory[];
}

/**
 * Action execution history
 */
export interface ActionHistory {
  actionId: string;
  actionType: string;
  timestamp: Date;
  outcome: 'successful' | 'failed' | 'pending';
  responseTime?: number;
  errorMessage?: string;
}

/**
 * Mobile gesture configuration
 */
export interface GestureConfig {
  swipeThreshold: number;  // Minimum distance for swipe (pixels)
  swipeVelocity: number;   // Minimum velocity for swipe
  hapticFeedback: boolean; // Enable haptic feedback
  showInstructions: boolean; // Show swipe instructions
}

/**
 * Contact intelligence for smart suggestions
 */
export interface ContactIntelligence {
  warmthScore: number;     // 0-100
  freshnessLevel: 'fresh' | 'stale' | 'critical';
  trendDirection: 'improving' | 'stable' | 'declining';
  lastInteractionDays: number;
  preferredChannel?: 'call' | 'email' | 'whatsapp';
  bestCallTime?: string;   // e.g., "morning", "afternoon"
}

/**
 * Default swipe actions for new contacts
 */
export const DEFAULT_SWIPE_ACTIONS: SwipeActions = {
  left: {
    id: 'email-default',
    type: 'email',
    label: 'E-Mail',
    icon: null, // Will be set by component
    color: '#2196F3',
    contextual: true,
  },
  right: {
    id: 'call-default',
    type: 'call',
    label: 'Anrufen',
    icon: null, // Will be set by component
    color: '#4CAF50',
    contextual: true,
  },
};

/**
 * Mobile action priorities
 */
export const ACTION_PRIORITIES = {
  birthday: 100,
  urgent_reconnect: 90,
  momentum_follow: 80,
  scheduled_followup: 70,
  standard_call: 50,
  standard_email: 40,
  whatsapp: 45,
  note: 30,
};

/**
 * Action type to color mapping
 */
export const ACTION_COLORS = {
  call: '#4CAF50',
  email: '#2196F3',
  whatsapp: '#25D366',
  sms: '#9C27B0',
  calendar: '#FF9800',
  note: '#607D8B',
  meeting: '#795548',
} as const;

/**
 * Offline action queue item
 */
export interface QueuedAction {
  id: string;
  action: QuickAction;
  contactId: string;
  contactData: unknown; // Minimal contact data for offline execution
  timestamp: Date;
  retryCount: number;
  maxRetries: number;
}

/**
 * Mobile device capabilities
 */
export interface DeviceCapabilities {
  hasPhone: boolean;
  hasSMS: boolean;
  hasEmail: boolean;
  hasWhatsApp: boolean;
  hasCalendar: boolean;
  hasHaptic: boolean;
  isOnline: boolean;
  isMobile: boolean;
}

/**
 * Action execution result
 */
export interface ActionResult {
  success: boolean;
  actionId: string;
  timestamp: Date;
  message?: string;
  error?: Error;
  requiresRetry?: boolean;
}