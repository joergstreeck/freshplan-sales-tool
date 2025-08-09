/**
 * Enterprise-grade TypeScript Types für Opportunity Pipeline
 *
 * @module OpportunityTypes
 * @description Zentrale Type-Definitionen für das Opportunity Management System.
 *              Basiert auf Backend API v1.0 Definitionen.
 * @since 2.0.0
 * @author FreshPlan Team
 */

/**
 * Sales pipeline stages enumeration
 * @enum {string}
 * @description Definiert alle möglichen Stages im Sales Pipeline Prozess.
 *              Die Werte müssen mit dem Backend synchron bleiben.
 */
export enum OpportunityStage {
  /** Neuer, unqualifizierter Lead */
  NEW_LEAD = 'NEW_LEAD',
  /** Lead wird qualifiziert */
  QUALIFICATION = 'QUALIFICATION',
  /** Bedarfsanalyse läuft */
  NEEDS_ANALYSIS = 'NEEDS_ANALYSIS',
  /** Angebot wurde erstellt */
  PROPOSAL = 'PROPOSAL',
  /** In Verhandlung */
  NEGOTIATION = 'NEGOTIATION',
  /** Erfolgreich abgeschlossen */
  CLOSED_WON = 'CLOSED_WON',
  /** Verloren */
  CLOSED_LOST = 'CLOSED_LOST',
  /** Vertragsverlängerung */
  RENEWAL = 'RENEWAL',
}

/**
 * Opportunity entity representing a sales opportunity in the pipeline
 * @interface IOpportunity
 * @description Vollständige Opportunity-Entität wie vom Backend geliefert.
 *              Alle Felder sind immutable nach Erhalt.
 */
export interface IOpportunity {
  /** Unique identifier (UUID v4) */
  readonly id: string;
  /** Opportunity name/title (max 255 chars) */
  readonly name: string;
  /** Detailed description (max 4000 chars) */
  readonly description?: string;
  /** Monetary value in EUR (min 0) */
  readonly value?: number;
  /** Expected value in EUR (fallback when value is null) */
  readonly expectedValue?: number;
  /** Probability of closing (0-100) */
  readonly probability: number;
  /** Expected close date in ISO 8601 format */
  readonly expectedCloseDate?: string;
  /** Current stage in sales pipeline */
  readonly stage: OpportunityStage;
  /** Timestamp when stage was last changed */
  readonly stageChangedAt: string;
  /** Customer reference */
  readonly customerId?: string;
  /** Customer display name (denormalized for performance) */
  readonly customerName?: string;
  /** Assigned sales representative ID */
  readonly assignedToId: string;
  /** Assigned sales representative name (denormalized) */
  readonly assignedToName: string;
  /** Audit fields */
  readonly createdAt: string;
  readonly updatedAt: string;
  /** Extended audit fields for enterprise compliance */
  readonly createdBy?: string;
  readonly updatedBy?: string;
  /** Version for optimistic locking */
  readonly version?: number;
}

/** @deprecated Use IOpportunity instead */
export type OpportunityResponse = IOpportunity;

/**
 * Request payload for creating a new opportunity
 * @interface ICreateOpportunityRequest
 * @description Validierte Request-Struktur für neue Opportunities.
 *              Alle Felder werden serverseitig validiert.
 */
export interface ICreateOpportunityRequest {
  /** @minLength 1 @maxLength 255 */
  name: string;
  /** @maxLength 4000 */
  description?: string;
  /** @minimum 0 @multipleOf 0.01 */
  value?: number;
  /** @minimum 0 @maximum 100 */
  probability?: number;
  /** @format date */
  expectedCloseDate?: string;
  /** @format uuid */
  customerId?: string;
  /** @format uuid */
  assignedToId?: string;
}

/** @deprecated Use ICreateOpportunityRequest instead */
export type CreateOpportunityRequest = ICreateOpportunityRequest;

/**
 * Request payload for updating an existing opportunity
 * @interface IUpdateOpportunityRequest
 * @description Partial update request. Nur übermittelte Felder werden aktualisiert.
 */
export interface IUpdateOpportunityRequest {
  /** @minLength 1 @maxLength 255 */
  name?: string;
  /** @maxLength 4000 */
  description?: string;
  /** @minimum 0 @multipleOf 0.01 */
  value?: number;
  /** @minimum 0 @maximum 100 */
  probability?: number;
  /** @format date */
  expectedCloseDate?: string;
  /** @format uuid */
  customerId?: string;
  /** @format uuid */
  assignedToId?: string;
  /** Version for optimistic locking */
  version?: number;
}

/** @deprecated Use IUpdateOpportunityRequest instead */
export type UpdateOpportunityRequest = IUpdateOpportunityRequest;

/**
 * Request payload for changing opportunity stage
 * @interface ChangeStageRequest
 * @description Used for stage transitions in the sales pipeline
 */
export interface ChangeStageRequest {
  /** The new stage to transition to */
  newStage: OpportunityStage;
  /** Optional reason for stage change (audit trail) */
  reason?: string;
}

export interface PipelineOverviewResponse {
  totalOpportunities: number;
  totalValue: number;
  averageValue: number;
  stageDistribution: StageDistribution[];
  conversionRates: ConversionRate[];
}

export interface StageDistribution {
  stage: OpportunityStage;
  count: number;
  totalValue: number;
  averageValue: number;
}

export interface ConversionRate {
  fromStage: OpportunityStage;
  toStage: OpportunityStage;
  rate: number;
  count: number;
}

export interface PipelineFilters {
  assignedToId?: string;
  customerId?: string;
  stage?: OpportunityStage;
  valueMin?: number;
  valueMax?: number;
  expectedCloseDateFrom?: string;
  expectedCloseDateTo?: string;
}

/**
 * Configuration for opportunity stage visualization and behavior
 * @interface IStageConfig
 * @description Definiert Darstellung und Geschäftsregeln pro Stage
 */
export interface IStageConfig {
  /** Stage identifier */
  readonly stage: OpportunityStage;
  /** Display label (i18n key) */
  readonly label: string;
  /** Primary color (hex) */
  readonly color: string;
  /** Background color (hex) */
  readonly bgColor?: string;
  /** Stage description (i18n key) */
  readonly description: string;
  /** Valid next stages for transitions */
  readonly allowedNextStages: ReadonlyArray<OpportunityStage>;
  /** Default probability when entering stage */
  readonly defaultProbability: number;
  /** Material icon name */
  readonly icon?: string;
  /** Sort order in pipeline */
  readonly sortOrder: number;
  /** Whether stage is active in pipeline */
  readonly isActive: boolean;
}

/** @deprecated Use IStageConfig instead */
export type StageConfig = IStageConfig;

/**
 * Drag & Drop context for opportunity cards
 * @interface IDraggedOpportunity
 * @description Context-Daten während Drag & Drop Operationen
 */
export interface IDraggedOpportunity {
  /** The opportunity being dragged */
  readonly opportunity: IOpportunity;
  /** Source stage before drag */
  readonly sourceStage: OpportunityStage;
  /** Target stage (set on drop) */
  targetStage?: OpportunityStage;
  /** Drag start timestamp */
  readonly dragStartedAt: number;
}

/** @deprecated Use IDraggedOpportunity instead */
export type DraggedOpportunity = IDraggedOpportunity;

/**
 * Error types for opportunity operations
 * @enum {string}
 */
export enum OpportunityErrorType {
  /** Network or connection error */
  NETWORK_ERROR = 'NETWORK_ERROR',
  /** Validation error from backend */
  VALIDATION_ERROR = 'VALIDATION_ERROR',
  /** Authorization/permission error */
  AUTHORIZATION_ERROR = 'AUTHORIZATION_ERROR',
  /** Optimistic locking conflict */
  VERSION_CONFLICT = 'VERSION_CONFLICT',
  /** Generic server error */
  SERVER_ERROR = 'SERVER_ERROR',
  /** Client-side error */
  CLIENT_ERROR = 'CLIENT_ERROR',
}

/**
 * Error response structure
 * @interface IOpportunityError
 */
export interface IOpportunityError {
  readonly type: OpportunityErrorType;
  readonly message: string;
  readonly details?: Record<string, unknown>;
  readonly timestamp: string;
  readonly traceId?: string;
}
