/**
 * Opportunity API Service
 * Kommunikation mit Backend /api/opportunities
 */

import { apiClient } from '../../../lib/apiClient';
import {
  OpportunityResponse,
  CreateOpportunityRequest,
  UpdateOpportunityRequest,
  ChangeStageRequest,
  PipelineOverviewResponse,
  PipelineFilters,
  OpportunityStage
} from '../types/opportunity.types';

const OPPORTUNITY_BASE_URL = '/api/opportunities';

export const opportunityApi = {
  // CRUD Operations
  async getAll(filters?: PipelineFilters, page = 0, size = 50): Promise<OpportunityResponse[]> {
    const params = new URLSearchParams();
    
    if (filters?.assignedToId) params.append('assignedToId', filters.assignedToId);
    if (filters?.customerId) params.append('customerId', filters.customerId);
    if (filters?.stage) params.append('stage', filters.stage);
    if (filters?.valueMin) params.append('valueMin', filters.valueMin.toString());
    if (filters?.valueMax) params.append('valueMax', filters.valueMax.toString());
    if (filters?.expectedCloseDateFrom) params.append('expectedCloseDateFrom', filters.expectedCloseDateFrom);
    if (filters?.expectedCloseDateTo) params.append('expectedCloseDateTo', filters.expectedCloseDateTo);
    
    params.append('page', page.toString());
    params.append('size', size.toString());

    const response = await apiClient.get(`${OPPORTUNITY_BASE_URL}?${params.toString()}`);
    return response.data;
  },

  async getById(id: string): Promise<OpportunityResponse> {
    const response = await apiClient.get(`${OPPORTUNITY_BASE_URL}/${id}`);
    return response.data;
  },

  async create(request: CreateOpportunityRequest): Promise<OpportunityResponse> {
    const response = await apiClient.post(OPPORTUNITY_BASE_URL, request);
    return response.data;
  },

  async update(id: string, request: UpdateOpportunityRequest): Promise<OpportunityResponse> {
    const response = await apiClient.put(`${OPPORTUNITY_BASE_URL}/${id}`, request);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await apiClient.delete(`${OPPORTUNITY_BASE_URL}/${id}`);
  },

  // Stage Management
  async changeStage(id: string, request: ChangeStageRequest): Promise<OpportunityResponse> {
    const response = await apiClient.patch(`${OPPORTUNITY_BASE_URL}/${id}/stage`, request);
    return response.data;
  },

  // Pipeline Analytics
  async getPipelineOverview(filters?: PipelineFilters): Promise<PipelineOverviewResponse> {
    const params = new URLSearchParams();
    
    if (filters?.assignedToId) params.append('assignedToId', filters.assignedToId);
    if (filters?.customerId) params.append('customerId', filters.customerId);
    if (filters?.stage) params.append('stage', filters.stage);
    if (filters?.valueMin) params.append('valueMin', filters.valueMin.toString());
    if (filters?.valueMax) params.append('valueMax', filters.valueMax.toString());
    if (filters?.expectedCloseDateFrom) params.append('expectedCloseDateFrom', filters.expectedCloseDateFrom);
    if (filters?.expectedCloseDateTo) params.append('expectedCloseDateTo', filters.expectedCloseDateTo);

    const queryString = params.toString();
    const url = `${OPPORTUNITY_BASE_URL}/pipeline/overview${queryString ? `?${queryString}` : ''}`;
    
    const response = await apiClient.get(url);
    return response.data;
  },

  // Convenience Methods
  async getByStage(stage: OpportunityStage, filters?: Omit<PipelineFilters, 'stage'>): Promise<OpportunityResponse[]> {
    return this.getAll({ ...filters, stage });
  },

  async getByAssignedUser(assignedToId: string, filters?: Omit<PipelineFilters, 'assignedToId'>): Promise<OpportunityResponse[]> {
    return this.getAll({ ...filters, assignedToId });
  }
};