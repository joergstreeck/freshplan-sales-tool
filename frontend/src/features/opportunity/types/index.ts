/**
 * Re-export all opportunity types from this module
 * Single point of entry for all opportunity-related types
 */

// Re-export stage enum from opportunity.types.ts
export { OpportunityStage } from './opportunity.types';

// Re-export from opportunity.types.ts (comprehensive types)
export type {
  IOpportunity,
  ICreateOpportunityRequest,
  IUpdateOpportunityRequest,
  IStageConfig,
  IDraggedOpportunity,
  IOpportunityError,
  OpportunityErrorType,
  PipelineOverviewResponse,
  StageDistribution,
  ConversionRate,
  PipelineFilters,
  ChangeStageRequest,
} from './opportunity.types';

// Simplified Opportunity interface for component compatibility
// This is the version used by current components
export interface Opportunity {
  /** Unique identifier */
  id: string;
  /** Opportunity name/title */
  name: string;
  /** Current pipeline stage */
  stage: OpportunityStage;
  /** Monetary value in EUR */
  value?: number;
  /** Win probability percentage (0-100) */
  probability?: number;
  /** Customer company name */
  customerName?: string;
  /** Assigned sales person name */
  assignedToName?: string;
  /** Expected close date ISO string */
  expectedCloseDate?: string;
  /** Description/notes */
  description?: string;
  /** Creation timestamp */
  createdAt: string;
  /** Last update timestamp */
  updatedAt: string;
}

// Legacy compatibility exports
/** @deprecated Use IOpportunity instead */
export type OpportunityResponse = IOpportunity;

/** @deprecated Use ICreateOpportunityRequest instead */
export type CreateOpportunityRequest = ICreateOpportunityRequest;

/** @deprecated Use IUpdateOpportunityRequest instead */
export type UpdateOpportunityRequest = IUpdateOpportunityRequest;
