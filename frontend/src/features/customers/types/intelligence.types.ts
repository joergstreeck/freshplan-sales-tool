/**
 * Types for Data Strategy Intelligence features
 */

// Interaction types matching backend enum
export type InteractionType =
  | 'EMAIL_SENT'
  | 'EMAIL_RECEIVED'
  | 'CALL_OUTBOUND'
  | 'CALL_INBOUND'
  | 'MEETING_SCHEDULED'
  | 'MEETING_COMPLETED'
  | 'NOTE_ADDED'
  | 'DOCUMENT_SHARED'
  | 'CONTRACT_SENT'
  | 'CONTRACT_SIGNED'
  | 'QUOTE_SENT'
  | 'QUOTE_ACCEPTED'
  | 'VISIT_SCHEDULED'
  | 'VISIT_COMPLETED'
  | 'OTHER';

// Contact Interaction DTO
export interface ContactInteractionDTO {
  id?: string;
  contactId: string;
  type: InteractionType;
  timestamp: string;
  duration?: number;
  subject?: string;
  notes?: string;
  sentimentScore?: number; // -1.0 to +1.0
  engagementScore?: number; // 0-100
  initiatedBy?: 'CUSTOMER' | 'SALES' | 'SYSTEM';
  channel?: string;
  outcome?: string;
  nextAction?: string;
  nextActionDate?: string;
  createdBy?: string;
  createdAt?: string;
}

// Warmth Score DTO
export interface WarmthScoreDTO {
  contactId: string;
  score: number; // 0-100
  confidence: number; // 0-100
  lastCalculated: string;
  factors: {
    frequency: number;
    sentiment: number;
    engagement: number;
    response: number;
  };
  trend: 'INCREASING' | 'STABLE' | 'DECREASING';
  recommendations: string[];
}

// Data Quality Metrics DTO
export interface DataQualityMetricsDTO {
  // Contact metrics
  totalContacts: number;
  contactsWithInteractions: number;
  averageInteractionsPerContact: number;
  dataCompletenessScore: number; // 0-100%

  // Intelligence metrics
  contactsWithWarmthScore: number;
  warmthScoreConfidence?: number; // Average confidence
  suggestionsAcceptanceRate?: number;
  predictionAccuracy?: number;

  // Data freshness
  freshContacts: number; // < 90 days
  agingContacts: number; // 90-180 days
  staleContacts: number; // 180-365 days
  criticalContacts: number; // > 365 days

  // Recommendations
  showDataCollectionHints: boolean;
  criticalDataGaps: string[];
  improvementSuggestions: string[];

  // Calculated fields
  overallDataQuality: 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR' | 'CRITICAL' | 'UNKNOWN';
  interactionCoverage: number; // percentage
}

// Batch import request
export interface BatchImportRequest {
  interactions: ContactInteractionDTO[];
  source: string;
  importedBy: string;
}

// Data quality levels for progressive enhancement
export interface DataQualityLevel {
  name: 'BOOTSTRAP' | 'LEARNING' | 'INTELLIGENT';
  duration: string;
  features: string[];
  accuracy: string;
}

// Contact Intelligence Summary
export interface ContactIntelligenceSummary {
  contactId: string;
  warmthScore?: number;
  warmthConfidence?: number;
  lastInteractionDate?: string;
  interactionCount: number;
  dataQualityLevel: DataQualityLevel['name'];
  nextBestAction?: string;
  riskIndicators: string[];
}

// Data Freshness Level Enum from Backend
export interface DataFreshnessLevel {
  key: 'fresh' | 'aging' | 'stale' | 'critical';
  description: string;
  severity: 'success' | 'info' | 'warning' | 'error';
  maxDays: number;
  muiColor: string;
  displayName: string;
}

// Data Quality Score from Backend
export interface DataQualityScore {
  score: number; // 0-100
  recommendations: string[];
  freshnessLevel: DataFreshnessLevel;
  lastCalculated: string; // ISO DateTime
  overallAssessment: string;
  updatePriority: 'HIGH' | 'MEDIUM' | 'LOW' | 'NONE';
}

// Freshness Statistics
export interface FreshnessStatistics {
  FRESH: number;
  AGING: number;
  STALE: number;
  CRITICAL: number;
}

// Update Wizard Props
export interface ContactUpdateWizardProps {
  contact: Contact;
  qualityScore?: DataQualityScore;
  onSuccess?: (updatedContact: Contact) => void;
  onCancel?: () => void;
}

// Bulk Update Request
export interface BulkUpdateRequest {
  contactIds: string[];
  fieldsToUpdate: Record<string, any>;
  reason: string;
}
