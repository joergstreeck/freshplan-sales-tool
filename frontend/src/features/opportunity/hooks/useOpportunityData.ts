import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { opportunityApi } from '../services/opportunityApi';
import {
  OpportunityResponse,
  CreateOpportunityRequest,
  UpdateOpportunityRequest,
  ChangeStageRequest,
  PipelineFilters,
  OpportunityStage,
} from '../types/opportunity.types';

// Query Keys für Cache Management
export const opportunityKeys = {
  all: ['opportunities'] as const,
  lists: () => [...opportunityKeys.all, 'list'] as const,
  list: (filters?: PipelineFilters) => [...opportunityKeys.lists(), filters] as const,
  details: () => [...opportunityKeys.all, 'detail'] as const,
  detail: (id: string) => [...opportunityKeys.details(), id] as const,
  pipeline: () => [...opportunityKeys.all, 'pipeline'] as const,
  pipelineOverview: (filters?: PipelineFilters) =>
    [...opportunityKeys.pipeline(), 'overview', filters] as const,
  byStage: (stage: OpportunityStage) => [...opportunityKeys.all, 'stage', stage] as const,
};

// Hook: Alle Opportunities laden
export function useOpportunities(filters?: PipelineFilters) {
  return useQuery({
    queryKey: opportunityKeys.list(filters),
    queryFn: () => opportunityApi.getAll(filters),
    staleTime: 5 * 60 * 1000, // 5 Minuten
    gcTime: 10 * 60 * 1000, // 10 Minuten Cache
  });
}

// Hook: Pipeline Overview mit Statistiken
export function usePipelineOverview(filters?: PipelineFilters) {
  return useQuery({
    queryKey: opportunityKeys.pipelineOverview(filters),
    queryFn: () => opportunityApi.getPipelineOverview(filters),
    staleTime: 2 * 60 * 1000, // 2 Minuten (Statistiken aktueller)
    gcTime: 5 * 60 * 1000,
  });
}

// Hook: Opportunities by Stage (für Kanban Columns)
export function useOpportunitiesByStage(stage: OpportunityStage) {
  return useQuery({
    queryKey: opportunityKeys.byStage(stage),
    queryFn: () => opportunityApi.getByStage(stage),
    staleTime: 3 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
}

// Hook: Einzelne Opportunity
export function useOpportunity(id: string) {
  return useQuery({
    queryKey: opportunityKeys.detail(id),
    queryFn: () => opportunityApi.getById(id),
    enabled: !!id, // Nur laden wenn ID vorhanden
    staleTime: 5 * 60 * 1000,
  });
}

// Hook: Opportunity erstellen
export function useCreateOpportunity() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateOpportunityRequest) => opportunityApi.create(request),
    onSuccess: newOpportunity => {
      // Cache invalidieren
      queryClient.invalidateQueries({ queryKey: opportunityKeys.lists() });
      queryClient.invalidateQueries({ queryKey: opportunityKeys.pipeline() });
      queryClient.invalidateQueries({ queryKey: opportunityKeys.byStage(newOpportunity.stage) });

      // Neue Opportunity direkt in Cache setzen
      queryClient.setQueryData(opportunityKeys.detail(newOpportunity.id), newOpportunity);
    },
  });
}

// Hook: Opportunity aktualisieren
export function useUpdateOpportunity() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, ...request }: UpdateOpportunityRequest & { id: string }) =>
      opportunityApi.update(id, request),
    onSuccess: updatedOpportunity => {
      // Spezifische Caches invalidieren
      queryClient.invalidateQueries({ queryKey: opportunityKeys.lists() });
      queryClient.invalidateQueries({ queryKey: opportunityKeys.pipeline() });
      queryClient.invalidateQueries({
        queryKey: opportunityKeys.byStage(updatedOpportunity.stage),
      });

      // Detail-Cache aktualisieren
      queryClient.setQueryData(opportunityKeys.detail(updatedOpportunity.id), updatedOpportunity);
    },
  });
}

// Hook: Opportunity Stage ändern (für Drag & Drop)
export function useChangeOpportunityStage() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, ...request }: ChangeStageRequest & { id: string }) =>
      opportunityApi.changeStage(id, request),
    onMutate: async ({ id, newStage }) => {
      // Optimistic Update für bessere UX
      await queryClient.cancelQueries({ queryKey: opportunityKeys.detail(id) });

      const previousOpportunity = queryClient.getQueryData<OpportunityResponse>(
        opportunityKeys.detail(id)
      );

      if (previousOpportunity) {
        queryClient.setQueryData(opportunityKeys.detail(id), {
          ...previousOpportunity,
          stage: newStage,
        });
      }

      return { previousOpportunity };
    },
    onError: (err, { id }, context) => {
      // Rollback bei Fehler
      if (context?.previousOpportunity) {
        queryClient.setQueryData(opportunityKeys.detail(id), context.previousOpportunity);
      }
    },
    onSuccess: (updatedOpportunity, { id }) => {
      // Cache komplett refreshen für Stage-Änderungen
      queryClient.invalidateQueries({ queryKey: opportunityKeys.lists() });
      queryClient.invalidateQueries({ queryKey: opportunityKeys.pipeline() });

      // Alle Stage-spezifischen Caches invalidieren
      Object.values(OpportunityStage).forEach(stage => {
        queryClient.invalidateQueries({ queryKey: opportunityKeys.byStage(stage) });
      });

      queryClient.setQueryData(opportunityKeys.detail(id), updatedOpportunity);
    },
  });
}

// Hook: Opportunity löschen
export function useDeleteOpportunity() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => opportunityApi.delete(id),
    onSuccess: (_, deletedId) => {
      // Alle relevanten Caches invalidieren
      queryClient.invalidateQueries({ queryKey: opportunityKeys.lists() });
      queryClient.invalidateQueries({ queryKey: opportunityKeys.pipeline() });

      // Stage-Caches invalidieren
      Object.values(OpportunityStage).forEach(stage => {
        queryClient.invalidateQueries({ queryKey: opportunityKeys.byStage(stage) });
      });

      // Detail-Cache entfernen
      queryClient.removeQueries({ queryKey: opportunityKeys.detail(deletedId) });
    },
  });
}

// Helper Hook: Prefetch für bessere Performance
export function usePrefetchOpportunity() {
  const queryClient = useQueryClient();

  return (id: string) => {
    queryClient.prefetchQuery({
      queryKey: opportunityKeys.detail(id),
      queryFn: () => opportunityApi.getById(id),
      staleTime: 5 * 60 * 1000,
    });
  };
}
