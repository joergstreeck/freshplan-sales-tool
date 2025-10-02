// Sprint 2.1.5 Frontend Phase 2 - Progressive Profiling Types
// Backend-Schema aligned (V255-V257 Migrations)

export type LeadStage = 0 | 1 | 2;

export type LeadStatus =
  | 'REGISTERED'
  | 'ACTIVE'
  | 'QUALIFIED'
  | 'CONVERTED'
  | 'LOST'
  | 'DELETED';

export type BusinessType =
  | 'restaurant'
  | 'hotel'
  | 'catering'
  | 'canteen'
  | 'other';

export type Territory = {
  id: string;
  name: string;
  countryCode: string;
};

export type LeadContact = {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
};

export type Lead = {
  id: string;

  // Progressive Profiling Stage (V255)
  stage: LeadStage;

  // Company Information (Stage 0)
  companyName: string;
  companyNameNormalized?: string;
  city?: string;
  postalCode?: string;
  street?: string;
  countryCode?: string;
  territory?: Territory;

  // Contact Information (Stage 1)
  contact?: LeadContact;
  contactPerson?: string; // Legacy field
  email?: string; // Legacy field
  emailNormalized?: string;
  phone?: string; // Legacy field
  phoneE164?: string;

  // DSGVO Compliance (Stage 1)
  consentGivenAt?: string; // ISO 8601 timestamp

  // Business Details (Stage 2)
  businessType?: BusinessType;
  industry?: string;
  kitchenSize?: 'small' | 'medium' | 'large';
  employeeCount?: number;
  estimatedVolume?: number;
  website?: string;
  websiteDomain?: string;

  // Lead Status & Ownership
  status: LeadStatus;
  ownerUserId: string;
  collaboratorUserIds?: string[];

  // Protection & Progress Tracking (V255, V257)
  registeredAt: string;
  protectionUntil?: string;
  progressDeadline?: string;
  progressWarningSentAt?: string;
  lastActivityAt?: string;

  // Stop-the-Clock (Sprint 2.1.5 Backend, UI in 2.1.6)
  clockStoppedAt?: string;
  stopReason?: string;

  // Timestamps
  createdAt?: string;
  updatedAt?: string;

  // Legacy fields for backward compatibility
  name?: string; // Maps to companyName
};

// LeadWizard Form Values (Progressive Profiling)
export type LeadFormStage0 = {
  companyName: string;
  city?: string;
  postalCode?: string;
  businessType?: BusinessType;
};

export type LeadFormStage1 = LeadFormStage0 & {
  contact: {
    firstName?: string;
    lastName?: string;
    email?: string;
    phone?: string;
  };
  consentGiven: boolean; // UI state, maps to consentGivenAt
};

export type LeadFormStage2 = LeadFormStage1 & {
  estimatedVolume?: number;
  kitchenSize?: 'small' | 'medium' | 'large';
  employeeCount?: number;
  website?: string;
  industry?: string;
};

// Lead Activity Types (V256)
export type ActivityType =
  // countsAsProgress = TRUE (5 types)
  | 'QUALIFIED_CALL'
  | 'MEETING'
  | 'DEMO'
  | 'ROI_PRESENTATION'
  | 'SAMPLE_SENT'
  // countsAsProgress = FALSE (5 types)
  | 'NOTE'
  | 'FOLLOW_UP'
  | 'EMAIL'
  | 'CALL'
  | 'SAMPLE_FEEDBACK';

export type LeadActivity = {
  id: string;
  leadId: string;
  userId: string;
  activityType: ActivityType;
  activityDate: string; // ISO 8601
  description?: string;

  // Progress Tracking (V256)
  countsAsProgress: boolean;
  summary?: string;
  outcome?: string;
  nextAction?: string;
  nextActionDate?: string; // ISO 8601 date only
  performedBy?: string;

  // Legacy fields
  isMeaningfulContact?: boolean;
  resetsTimer?: boolean;

  // Timestamps
  createdAt: string;
};

// Protection Status for LeadProtectionBadge
export type ProtectionStatus = 'protected' | 'warning' | 'expired';

export type LeadProtectionInfo = {
  status: ProtectionStatus;
  protectionUntil?: string;
  progressDeadline?: string;
  daysUntilExpiry?: number;
  warningMessage?: string;
};

// RFC7807 Problem Details
export type Problem = {
  type?: string;
  title?: string;
  detail?: string;
  status?: number;
  errors?: Record<string, string[]>;
};
