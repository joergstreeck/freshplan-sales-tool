/**
 * Event constants for the application
 * Centralized location for all event names to ensure consistency
 */

// Tab navigation events
export const TAB_EVENTS = {
  SWITCH: 'tab:switch',
  SWITCHED: 'tab:switched',
  BEFORE_SWITCH: 'tab:beforeSwitch',
  PROGRESS_UPDATE: 'tab:progressUpdate',
  ACCESS_DENIED: 'tab:accessDenied'
} as const;

// Calculator events
export const CALCULATOR_EVENTS = {
  UPDATE: 'calculator:update',
  CALCULATE: 'calculator:calculate',
  RESET: 'calculator:reset'
} as const;

// Customer events
export const CUSTOMER_EVENTS = {
  SAVE: 'customer:save',
  SAVED: 'customer:saved',
  VALIDATE: 'customer:validate',
  CLEAR: 'customer:clear'
} as const;

// Location events
export const LOCATION_EVENTS = {
  ADD: 'location:add',
  REMOVE: 'location:remove',
  UPDATE: 'location:update',
  SYNC_CHECK: 'location:syncCheck'
} as const;

// Credit check events
export const CREDIT_CHECK_EVENTS = {
  START: 'creditCheck:start',
  COMPLETE: 'creditCheck:complete',
  ERROR: 'creditCheck:error',
  REQUEST_APPROVAL: 'creditCheck:requestApproval'
} as const;

// Profile events
export const PROFILE_EVENTS = {
  GENERATE: 'profile:generate',
  GENERATED: 'profile:generated',
  UPDATE: 'profile:update'
} as const;

// PDF events
export const PDF_EVENTS = {
  GENERATE: 'pdf:generate',
  GENERATED: 'pdf:generated',
  DOWNLOAD: 'pdf:download',
  EMAIL: 'pdf:email'
} as const;

// Language events
export const LANGUAGE_EVENTS = {
  CHANGE: 'language:change',
  CHANGED: 'language:changed'
} as const;

// General app events
export const APP_EVENTS = {
  READY: 'app:ready',
  ERROR: 'app:error',
  LOADING: 'app:loading',
  LOADED: 'app:loaded'
} as const;

// Type exports for TypeScript
export type TabEventType = typeof TAB_EVENTS[keyof typeof TAB_EVENTS];
export type CalculatorEventType = typeof CALCULATOR_EVENTS[keyof typeof CALCULATOR_EVENTS];
export type CustomerEventType = typeof CUSTOMER_EVENTS[keyof typeof CUSTOMER_EVENTS];
export type LocationEventType = typeof LOCATION_EVENTS[keyof typeof LOCATION_EVENTS];
export type CreditCheckEventType = typeof CREDIT_CHECK_EVENTS[keyof typeof CREDIT_CHECK_EVENTS];
export type ProfileEventType = typeof PROFILE_EVENTS[keyof typeof PROFILE_EVENTS];
export type PDFEventType = typeof PDF_EVENTS[keyof typeof PDF_EVENTS];
export type LanguageEventType = typeof LANGUAGE_EVENTS[keyof typeof LANGUAGE_EVENTS];
export type AppEventType = typeof APP_EVENTS[keyof typeof APP_EVENTS];

// Event payload interfaces
export interface TabSwitchPayload {
  tab: string;
  previousTab?: string;
  source?: string;
}

export interface CalculatorUpdatePayload {
  orderValue?: number;
  leadTime?: number;
  pickup?: boolean;
  chain?: boolean;
}

export interface CustomerSavePayload {
  data: any;
  isValid: boolean;
}

export interface LocationUpdatePayload {
  id: string | number;
  data: any;
}

export interface CreditCheckResultPayload {
  status: 'approved' | 'rejected' | 'pending';
  limit?: number;
  notes?: string;
}

export interface LanguageChangePayload {
  language: 'de' | 'en';
  previousLanguage: 'de' | 'en';
}

export interface ErrorPayload {
  message: string;
  code?: string;
  details?: any;
}