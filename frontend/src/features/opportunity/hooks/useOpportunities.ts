/**
 * React Hook für Opportunity Data Management
 * @module useOpportunities
 * @description Enterprise-grade Hook für Opportunity CRUD Operations mit React Query
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { opportunityApi } from '../services/opportunityApi';
import { logger } from '../../../lib/logger';
import {
  type IOpportunity,
  type CreateOpportunityRequest,
  type UpdateOpportunityRequest,
  type ChangeStageRequest,
  type PipelineFilters,
  type OpportunityStage,
  type Opportunity,
} from '../types';

// Query Keys für React Query Cache Management
export const opportunityKeys = {
  all: ['opportunities'] as const,
  lists: () => [...opportunityKeys.all, 'list'] as const,
  list: (filters?: PipelineFilters) => [...opportunityKeys.lists(), filters] as const,
  details: () => [...opportunityKeys.all, 'detail'] as const,
  detail: (id: string) => [...opportunityKeys.details(), id] as const,
  pipeline: () => [...opportunityKeys.all, 'pipeline'] as const,
  pipelineOverview: (filters?: PipelineFilters) =>
    [...opportunityKeys.pipeline(), 'overview', filters] as const,
};

/**
 * Maps Backend IOpportunity to Frontend Opportunity type
 * @param backendOpportunity Backend opportunity data
 * @returns Frontend-compatible opportunity
 */
function mapBackendToFrontend(backendOpportunity: IOpportunity): Opportunity {
  return {
    id: backendOpportunity.id,
    name: backendOpportunity.name,
    stage: backendOpportunity.stage,
    value: backendOpportunity.value || backendOpportunity.expectedValue, // Map expectedValue to value
    probability: backendOpportunity.probability,
    customerName: backendOpportunity.customerName,
    assignedToName: backendOpportunity.assignedToName,
    expectedCloseDate: backendOpportunity.expectedCloseDate,
    description: backendOpportunity.description,
    createdAt: backendOpportunity.createdAt,
    updatedAt: backendOpportunity.updatedAt,
  };
}

/**
 * Hook für das Laden aller Opportunities mit optionalen Filtern
 * @param filters Optionale Filter für die Opportunity-Liste
 * @param enabled Ob die Query aktiv sein soll (default: true)
 * @returns React Query result mit Opportunity-Liste
 */
export function useOpportunities(filters?: PipelineFilters, enabled = true) {
  return useQuery({
    queryKey: opportunityKeys.list(filters),
    queryFn: async () => {
      logger.debug('Fetching opportunities with filters:', filters);
      const startTime = performance.now();

      try {
        const backendOpportunities = await opportunityApi.getAll(filters);
        const frontendOpportunities = backendOpportunities.map(mapBackendToFrontend);

        const duration = performance.now() - startTime;
        logger.info(
          `Opportunities loaded successfully: ${frontendOpportunities.length} items in ${duration.toFixed(2)}ms`
        );

        return frontendOpportunities;
      } catch (error) {
        logger.error('Failed to fetch opportunities:', error);
        throw error;
      }
    },
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes (cacheTime renamed to gcTime in React Query v5)
    retry: (failureCount, error) => {
      // Retry max 3 times, but not for 4xx errors
      if (failureCount >= 3) return false;
      if (error instanceof Error && error.message.includes('4')) return false;
      return true;
    },
  });
}

/**
 * Hook für das Laden einer einzelnen Opportunity
 * @param id Opportunity ID
 * @param enabled Ob die Query aktiv sein soll (default: true)
 * @returns React Query result mit einzelner Opportunity
 */
export function useOpportunity(id: string, enabled = true) {
  return useQuery({
    queryKey: opportunityKeys.detail(id),
    queryFn: async () => {
      logger.debug(`Fetching opportunity ${id}`);
      try {
        const backendOpportunity = await opportunityApi.getById(id);
        return mapBackendToFrontend(backendOpportunity);
      } catch (error) {
        logger.error(`Failed to fetch opportunity ${id}:`, error);
        throw error;
      }
    },
    enabled: enabled && !!id,
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: 2,
  });
}

/**
 * Hook für Opportunity-Erstellung
 * @returns Mutation für das Erstellen von Opportunities
 */
export function useCreateOpportunity() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (request: CreateOpportunityRequest) => {
      logger.debug('Creating opportunity:', request.name);
      try {
        const backendOpportunity = await opportunityApi.create(request);
        const frontendOpportunity = mapBackendToFrontend(backendOpportunity);
        logger.info(`Opportunity created: ${frontendOpportunity.name} (${frontendOpportunity.id})`);
        return frontendOpportunity;
      } catch (error) {
        logger.error('Failed to create opportunity:', error);
        throw error;
      }
    },
    onSuccess: () => {
      // Invalidate und refetch opportunity list
      queryClient.invalidateQueries({ queryKey: opportunityKeys.lists() });
      queryClient.invalidateQueries({ queryKey: opportunityKeys.pipeline() });
    },
  });
}

/**
 * Hook für Opportunity-Updates
 * @returns Mutation für das Updaten von Opportunities
 */
export function useUpdateOpportunity() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, request }: { id: string; request: UpdateOpportunityRequest }) => {
      logger.debug(`Updating opportunity ${id}:`, request);
      try {
        const backendOpportunity = await opportunityApi.update(id, request);
        const frontendOpportunity = mapBackendToFrontend(backendOpportunity);
        logger.info(`Opportunity updated: ${frontendOpportunity.name} (${id})`);
        return frontendOpportunity;
      } catch (error) {
        logger.error(`Failed to update opportunity ${id}:`, error);
        throw error;
      }
    },
    onSuccess: updatedOpportunity => {
      // Update specific opportunity in cache
      queryClient.setQueryData(opportunityKeys.detail(updatedOpportunity.id), updatedOpportunity);
      // Invalidate lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: opportunityKeys.lists() });
      queryClient.invalidateQueries({ queryKey: opportunityKeys.pipeline() });
    },
  });
}

/**
 * Hook für Stage-Änderungen (Drag & Drop)
 * @returns Mutation für Stage-Änderungen
 */
export function useChangeOpportunityStage() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, request }: { id: string; request: ChangeStageRequest }) => {
      logger.debug(`Changing stage for opportunity ${id} to ${request.newStage}`);
      try {
        const backendOpportunity = await opportunityApi.changeStage(id, request);
        const frontendOpportunity = mapBackendToFrontend(backendOpportunity);
        logger.info(`Opportunity stage changed: ${frontendOpportunity.name} → ${request.newStage}`);
        return frontendOpportunity;
      } catch (error) {
        logger.error(`Failed to change stage for opportunity ${id}:`, error);
        throw error;
      }
    },
    onSuccess: updatedOpportunity => {
      // Optimistic updates
      queryClient.setQueryData(opportunityKeys.detail(updatedOpportunity.id), updatedOpportunity);
      // Invalidate lists für UI refresh
      queryClient.invalidateQueries({ queryKey: opportunityKeys.lists() });
      queryClient.invalidateQueries({ queryKey: opportunityKeys.pipeline() });
    },
  });
}

/**
 * Hook für Opportunity-Löschung
 * @returns Mutation für das Löschen von Opportunities
 */
export function useDeleteOpportunity() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      logger.debug(`Deleting opportunity ${id}`);
      try {
        await opportunityApi.delete(id);
        logger.info(`Opportunity deleted: ${id}`);
        return id;
      } catch (error) {
        logger.error(`Failed to delete opportunity ${id}:`, error);
        throw error;
      }
    },
    onSuccess: deletedId => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: opportunityKeys.detail(deletedId) });
      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: opportunityKeys.lists() });
      queryClient.invalidateQueries({ queryKey: opportunityKeys.pipeline() });
    },
  });
}

/**
 * Hook für Pipeline Overview Analytics
 * @param filters Optionale Filter für Pipeline-Übersicht
 * @param enabled Ob die Query aktiv sein soll (default: true)
 * @returns React Query result mit Pipeline-Übersicht
 */
export function usePipelineOverview(filters?: PipelineFilters, enabled = true) {
  return useQuery({
    queryKey: opportunityKeys.pipelineOverview(filters),
    queryFn: async () => {
      logger.debug('Fetching pipeline overview with filters:', filters);
      try {
        const overview = await opportunityApi.getPipelineOverview(filters);
        logger.info('Pipeline overview loaded successfully');
        return overview;
      } catch (error) {
        logger.error('Failed to fetch pipeline overview:', error);
        throw error;
      }
    },
    enabled,
    staleTime: 2 * 60 * 1000, // 2 minutes - Pipeline data changes frequently
    retry: 2,
  });
}

/**
 * Convenience Hook für Opportunities nach Stage
 * @param stage Gewünschte Stage
 * @param filters Zusätzliche Filter (ohne stage)
 * @param enabled Ob die Query aktiv sein soll (default: true)
 * @returns React Query result mit gefilterten Opportunities
 */
export function useOpportunitiesByStage(
  stage: OpportunityStage,
  filters?: Omit<PipelineFilters, 'stage'>,
  enabled = true
) {
  return useOpportunities({ ...filters, stage }, enabled);
}
