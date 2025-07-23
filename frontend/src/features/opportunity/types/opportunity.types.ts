/**
 * TypeScript Types für Opportunity Pipeline
 * Basiert auf Backend API Definitionen
 */

export enum OpportunityStage {
  NEW_LEAD = "NEW_LEAD",
  QUALIFICATION = "QUALIFICATION", 
  NEEDS_ANALYSIS = "NEEDS_ANALYSIS",
  PROPOSAL = "PROPOSAL",
  NEGOTIATION = "NEGOTIATION",
  CLOSED_WON = "CLOSED_WON",
  CLOSED_LOST = "CLOSED_LOST"
}

export interface OpportunityResponse {
  id: string;
  name: string;
  description?: string;
  value?: number;
  probability: number;
  expectedCloseDate?: string;
  stage: OpportunityStage;
  stageChangedAt: string;
  customerId?: string;
  customerName?: string;
  assignedToId: string;
  assignedToName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOpportunityRequest {
  name: string;
  description?: string;
  value?: number;
  probability?: number;
  expectedCloseDate?: string;
  customerId?: string;
  assignedToId?: string;
}

export interface UpdateOpportunityRequest {
  name?: string;
  description?: string;
  value?: number;
  probability?: number;
  expectedCloseDate?: string;
  customerId?: string;
  assignedToId?: string;
}

export interface ChangeStageRequest {
  stage: OpportunityStage;
  customProbability?: number;
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

export interface StageConfig {
  stage: OpportunityStage;
  label: string;
  color: string;
  description: string;
  allowedNextStages: OpportunityStage[];
  defaultProbability: number;
}

// Drag & Drop Interface
export interface DraggedOpportunity {
  opportunity: OpportunityResponse;
  sourceStage: OpportunityStage;
}