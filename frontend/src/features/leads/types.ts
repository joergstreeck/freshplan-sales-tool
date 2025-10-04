// Sprint 2.1.5 Frontend Phase 2 - Progressive Profiling Types
// Backend-Schema aligned (V255-V257 Migrations)

export type LeadStage = 0 | 1 | 2;

export type LeadStatus = 'REGISTERED' | 'ACTIVE' | 'QUALIFIED' | 'CONVERTED' | 'LOST' | 'DELETED';

export type BusinessType = 'restaurant' | 'hotel' | 'catering' | 'canteen' | 'other';

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

  // Lead Source (Sprint 2.1.5)
  source?: LeadSource;

  // Protection & Progress Tracking (V255, V257)
  // Sprint 2.1.5: registeredAt nullable für Pre-Claim Detection (null = Pre-Claim)
  registeredAt?: string;
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

// Lead Activity Types (V258 - Sprint 2.1.5)
export type ActivityType =
  // Progress Activities (countsAsProgress = TRUE) - 5 Types
  | 'QUALIFIED_CALL'
  | 'MEETING'
  | 'DEMO'
  | 'ROI_PRESENTATION'
  | 'SAMPLE_SENT'

  // Non-Progress Activities (countsAsProgress = FALSE) - 5 Types
  | 'NOTE'
  | 'FOLLOW_UP'
  | 'EMAIL'
  | 'CALL'
  | 'SAMPLE_FEEDBACK'

  // System Activities (countsAsProgress = FALSE) - 3 Types (Sprint 2.1.5)
  | 'FIRST_CONTACT_DOCUMENTED'
  | 'EMAIL_RECEIVED'
  | 'LEAD_ASSIGNED';

/**
 * Progress-Mapping für UI (ActivityTimeline Filtering)
 * @see ACTIVITY_TYPES_PROGRESS_MAPPING.md
 * @see V258 Migration (Backend Constraint)
 */
export const ACTIVITY_PROGRESS_MAP: Record<ActivityType, boolean> = {
  // Progress = true (5)
  QUALIFIED_CALL: true,
  MEETING: true,
  DEMO: true,
  ROI_PRESENTATION: true,
  SAMPLE_SENT: true,

  // Non-Progress = false (8)
  NOTE: false,
  FOLLOW_UP: false,
  EMAIL: false,
  CALL: false,
  SAMPLE_FEEDBACK: false,
  FIRST_CONTACT_DOCUMENTED: false,
  EMAIL_RECEIVED: false,
  LEAD_ASSIGNED: false,
} as const;

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

/**
 * Lead-Herkunftsquellen (Sprint 2.1.5)
 * Bestimmt Pflichtfelder + DSGVO-Consent-Pflicht
 * @see FRONTEND_DELTA.md Section 2
 */
export type LeadSource =
  | 'MESSE' // Messe/Event
  | 'EMPFEHLUNG' // Partner/Kunde
  | 'TELEFON' // Cold Call
  | 'WEB_FORMULAR' // Website (Sprint 2.1.6)
  | 'PARTNER' // Partner-API (Sprint 2.1.6)
  | 'SONSTIGE'; // Fallback

/**
 * Erstkontakt-Kanäle (Sprint 2.1.5)
 * @see FRONTEND_DELTA.md Section 3
 */
export type FirstContactChannel =
  | 'MESSE' // Messestand/Event
  | 'PHONE' // Telefonat
  | 'EMAIL' // E-Mail
  | 'REFERRAL' // Empfehlung/Vorstellung
  | 'OTHER'; // Sonstige

export interface FirstContact {
  channel: FirstContactChannel;
  performedAt: string; // ISO-8601 DateTime
  notes: string; // min. 10 Zeichen
}

// RFC7807 Problem Details (Sprint 2.1.5 - mit extensions für Dedupe)
export interface DuplicateLead {
  leadId: string;
  companyName: string;
  city?: string;
  postalCode?: string;
  ownerUserId?: string;
}

export type Problem = {
  type?: string;
  title?: string;
  detail?: string;
  status?: number;
  errors?: Record<string, string[]>;

  // Sprint 2.1.5: Dedupe 409 Handling
  extensions?: {
    severity?: 'WARNING'; // Nur bei Soft Collisions
    duplicates?: DuplicateLead[];
  };
};
