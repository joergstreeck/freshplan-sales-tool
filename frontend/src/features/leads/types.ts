// Sprint 2.1.5 Frontend Phase 2 - Progressive Profiling Types
// Backend-Schema aligned (V255-V257 Migrations)

export type LeadStage = 0 | 1 | 2;

export type LeadStatus =
  | 'REGISTERED'
  | 'ACTIVE'
  | 'REMINDER'
  | 'GRACE_PERIOD'
  | 'QUALIFIED'
  | 'CONVERTED'
  | 'LOST'
  | 'EXPIRED'
  | 'DELETED';

// Sprint 2.1.6 Phase 4: Lead Status Labels (German)
// Sprint 2.1.5: REMINDER/GRACE_PERIOD/EXPIRED hinzugefügt (Handelsvertreter-Schutz)
export const leadStatusLabels: Record<LeadStatus, string> = {
  REGISTERED: 'Vormerkung',
  ACTIVE: 'Aktiv',
  REMINDER: 'Erinnerung versendet',
  GRACE_PERIOD: 'Nachfrist',
  QUALIFIED: 'Qualifiziert',
  CONVERTED: 'Konvertiert',
  LOST: 'Verloren',
  EXPIRED: 'Schutz abgelaufen',
  DELETED: 'Gelöscht',
};

// Sprint 2.1.6 Phase 4: Lead Status Colors
export const leadStatusColors: Record<LeadStatus, string> = {
  REGISTERED: 'info.main', // Blue
  ACTIVE: 'success.main', // Green
  REMINDER: 'warning.light', // Amber (Warning)
  GRACE_PERIOD: 'warning.main', // Orange (Urgent Warning)
  QUALIFIED: 'info.light', // Cyan
  CONVERTED: 'secondary.light', // Purple
  LOST: 'error.main', // Red
  EXPIRED: 'grey.700', // Brown (Inactive)
  DELETED: 'grey.500', // Gray
};

// Sprint 2.1.6: Harmonized with Customer.industry (9 values instead of 5)
// Values fetched from GET /api/enums/business-types (no hardcoding)
export type BusinessType =
  | 'RESTAURANT'
  | 'HOTEL'
  | 'CATERING'
  | 'KANTINE'
  | 'GROSSHANDEL'
  | 'LEH'
  | 'BILDUNG'
  | 'GESUNDHEIT'
  | 'SONSTIGES';

export type Territory = {
  id: string;
  name: string;
  countryCode: string;
};

// Sprint 2.1.6 Phase 5+: Structured Contact Data (Request DTO - simplified)
export type LeadContact = {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
};

// Sprint 2.1.6 Phase 5+: Full Contact DTO (Response from Backend - ADR-007 100% Parity)
export type LeadContactDTO = {
  id: string;
  leadId: string;

  // Basic Info
  salutation?: string; // 'herr' | 'frau' | 'divers'
  title?: string;
  firstName: string;
  lastName: string;
  position?: string;
  decisionLevel?: string; // 'executive' | 'manager' | 'operational' | 'influencer'

  // Contact Info
  email?: string;
  phone?: string;
  mobile?: string;

  // Flags
  primary: boolean;
  active: boolean;

  // Relationship Data (CRM Intelligence)
  birthday?: string; // ISO 8601 date
  hobbies?: string;
  familyStatus?: string; // 'single' | 'married' | 'divorced' | 'widowed'
  childrenCount?: number;
  personalNotes?: string;

  // Intelligence Data (Sales Excellence)
  warmthScore?: number; // 0-100
  warmthConfidence?: number; // 0-100
  lastInteractionDate?: string; // ISO 8601 timestamp
  interactionCount?: number;

  // Data Quality
  dataQualityScore?: number; // 0-100
  dataQualityRecommendations?: string;

  // Legacy Compatibility
  isDecisionMaker?: boolean;
  isDeleted?: boolean;

  // Computed Fields
  fullName?: string; // "Dr. Maria Schmidt"
  displayName?: string; // "Frau Dr. Maria Schmidt"

  // Audit Fields
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
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
  contact?: LeadContact; // For requests (simple)
  contacts?: LeadContactDTO[]; // Sprint 2.1.6 Phase 5+: For responses (full data)
  contactPerson?: string; // Legacy field (deprecated)
  email?: string; // Legacy field (deprecated)
  emailNormalized?: string;
  phone?: string; // Legacy field (deprecated)
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

  // Protection & Progress Tracking (V255, V257, V274)
  // Variante B (2025-10-08): registered_at IMMER gesetzt (Audit Trail)
  registeredAt: string; // NOT NULL - wann wurde Lead erfasst?

  // Variante B: NULL = Pre-Claim aktiv (10 Tage Frist für Erstkontakt)
  firstContactDocumentedAt?: string; // NULL → Pre-Claim, NOT NULL → Vollschutz

  protectionUntil?: string;
  progressDeadline?: string;
  progressWarningSentAt?: string;
  lastActivityAt?: string;

  // Stop-the-Clock (Sprint 2.1.5 Backend, UI in 2.1.6)
  clockStoppedAt?: string;
  stopReason?: string;

  // Lead Scoring (Sprint 2.1.6 Phase 4 - ADR-006 Phase 2 - V269/V271)
  leadScore?: number; // 0-100 points (total weighted score)

  // Sprint 2.1.6+: Lead Scoring System - 4 Dimensions
  budgetConfirmed?: boolean; // Revenue scoring input
  dealSize?: 'SMALL' | 'MEDIUM' | 'LARGE' | 'ENTERPRISE'; // Revenue scoring input

  // Score Cache (calculated by LeadScoringService)
  painScore?: number; // 0-100 points (25% weight)
  revenueScore?: number; // 0-100 points (25% weight)
  fitScore?: number; // 0-100 points (25% weight)
  engagementScore?: number; // 0-100 points (25% weight)

  // Branch/Chain information (Sprint 2.1.6 Phase 5+ - V277)
  branchCount?: number; // Anzahl Filialen/Standorte (Default: 1)
  isChain?: boolean; // Kettenbetrieb ja/nein

  // Pain Scoring System V3 (Sprint 2.1.6 Phase 5+ - V278)
  // OPERATIONAL PAINS (35 Punkte max)
  painStaffShortage?: boolean; // Personalmangel (+10)
  painHighCosts?: boolean; // Hoher Kostendruck (+7)
  painFoodWaste?: boolean; // Food Waste/Überproduktion (+7)
  painQualityInconsistency?: boolean; // Interne Qualitätsinkonsistenz (+6, -4 mit Staff)
  painTimePressure?: boolean; // Zeitdruck/Effizienz (+5)

  // SWITCHING PAINS (21 Punkte max)
  painSupplierQuality?: boolean; // Qualitätsprobleme beim Lieferanten (+10)
  painUnreliableDelivery?: boolean; // Unzuverlässige Lieferzeiten (+8)
  painPoorService?: boolean; // Schlechter Service/Support (+3)

  painNotes?: string; // Freitext für Details

  // Urgency Dimension (separate von Pain)
  urgencyLevel?: UrgencyLevel; // NORMAL(0), MEDIUM(5), HIGH(10), EMERGENCY(25)
  multiPainBonus?: number; // Auto-calculated: +10 bei 4+ Pains

  // Relationship Dimension (Sprint 2.1.6 Phase 5+ - V280)
  relationshipStatus?: RelationshipStatus;
  decisionMakerAccess?: DecisionMakerAccess;
  competitorInUse?: string; // Aktueller Wettbewerber (falls bekannt)
  internalChampionName?: string; // Name des internen Champions (falls vorhanden)

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
 * Urgency Level (Sprint 2.1.6 Phase 5+ - V278)
 * Separate Dimension für Zeitdruck (nicht Pain!)
 */
export type UrgencyLevel = 'NORMAL' | 'MEDIUM' | 'HIGH' | 'EMERGENCY';

export const urgencyLevelLabels: Record<UrgencyLevel, string> = {
  NORMAL: 'Normal (6+ Monate)',
  MEDIUM: 'Mittel (1-3 Monate)',
  HIGH: 'Hoch (nächsten Monat)',
  EMERGENCY: 'Notfall (sofort)',
};

export const urgencyLevelColors: Record<UrgencyLevel, string> = {
  NORMAL: 'grey.500', // Gray
  MEDIUM: 'info.main', // Blue
  HIGH: 'warning.main', // Orange
  EMERGENCY: 'error.main', // Red
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

/**
 * Relationship Status (Sprint 2.1.6 Phase 5+ - V280)
 * Beziehungsqualität zum Lead-Kontakt (40% Gewicht im Engagement-Score)
 */
export type RelationshipStatus =
  | 'COLD'
  | 'CONTACTED'
  | 'ENGAGED_SKEPTICAL'
  | 'ENGAGED_POSITIVE'
  | 'TRUSTED'
  | 'ADVOCATE';

export const relationshipStatusLabels: Record<RelationshipStatus, string> = {
  COLD: 'Kein Kontakt',
  CONTACTED: 'Erstkontakt hergestellt',
  ENGAGED_SKEPTICAL: 'Mehrere Touchpoints, skeptisch',
  ENGAGED_POSITIVE: 'Mehrere Touchpoints, positiv',
  TRUSTED: 'Vertrauensbasis vorhanden',
  ADVOCATE: 'Kämpft aktiv für uns',
};

export const relationshipStatusPoints: Record<RelationshipStatus, number> = {
  COLD: 0,
  CONTACTED: 5,
  ENGAGED_SKEPTICAL: 8,
  ENGAGED_POSITIVE: 12,
  TRUSTED: 17,
  ADVOCATE: 25,
};

/**
 * Decision Maker Access (Sprint 2.1.6 Phase 5+ - V280)
 * Zugang zum Entscheider (60% Gewicht im Engagement-Score)
 */
export type DecisionMakerAccess =
  | 'UNKNOWN'
  | 'BLOCKED'
  | 'INDIRECT'
  | 'DIRECT'
  | 'IS_DECISION_MAKER';

export const decisionMakerAccessLabels: Record<DecisionMakerAccess, string> = {
  UNKNOWN: 'Noch nicht identifiziert',
  BLOCKED: 'Entscheider bekannt, Zugang blockiert',
  INDIRECT: 'Zugang über Dritte (Assistent, Mitarbeiter, Partner)',
  DIRECT: 'Direkter Kontakt zum Entscheider',
  IS_DECISION_MAKER: 'Unser Kontakt IST der Entscheider',
};

export const decisionMakerAccessPoints: Record<DecisionMakerAccess, number> = {
  UNKNOWN: 0,
  BLOCKED: -3,
  INDIRECT: 10,
  DIRECT: 20,
  IS_DECISION_MAKER: 25,
};
