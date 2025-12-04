/**
 * DSGVO API Hooks - Sprint 2.1.8
 *
 * React Query Hooks für DSGVO-Compliance Endpoints
 */

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../../../../api/client';
import type {
  GdprDataRequestDTO,
  GdprDeletionLogDTO,
  GdprDeleteRequest,
  ContactAllowedResponse,
} from './types';

const GDPR_API_BASE = '/api/gdpr/leads';

/**
 * Art. 15: Datenexport (PDF Download)
 */
export function useGdprDataExport() {
  return useMutation({
    mutationFn: async (leadId: number) => {
      const response = await api.get(`${GDPR_API_BASE}/${leadId}/data-export`, {
        responseType: 'blob',
      });
      return response.data as Blob;
    },
    onSuccess: (blob, leadId) => {
      // Download PDF
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `dsgvo-auskunft-lead-${leadId}-${Date.now()}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    },
  });
}

/**
 * Art. 15: Datenexport-Anfragen auflisten
 */
export function useGdprDataRequests(leadId: number) {
  return useQuery({
    queryKey: ['gdpr', 'data-requests', leadId],
    queryFn: async () => {
      const response = await api.get<GdprDataRequestDTO[]>(
        `${GDPR_API_BASE}/${leadId}/data-requests`
      );
      return response.data;
    },
    enabled: !!leadId,
  });
}

/**
 * Art. 17: DSGVO-Löschung
 */
export function useGdprDelete() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ leadId, reason }: { leadId: number; reason: string }) => {
      const request: GdprDeleteRequest = { reason };
      await api.delete(`${GDPR_API_BASE}/${leadId}`, { data: request });
    },
    onSuccess: (_, { leadId }) => {
      // Invalidate lead queries
      queryClient.invalidateQueries({ queryKey: ['leads'] });
      queryClient.invalidateQueries({ queryKey: ['lead', leadId] });
      queryClient.invalidateQueries({ queryKey: ['gdpr', 'deletion-logs', leadId] });
    },
  });
}

/**
 * Art. 17: Löschprotokolle auflisten
 */
export function useGdprDeletionLogs(leadId: number) {
  return useQuery({
    queryKey: ['gdpr', 'deletion-logs', leadId],
    queryFn: async () => {
      const response = await api.get<GdprDeletionLogDTO[]>(
        `${GDPR_API_BASE}/${leadId}/deletion-logs`
      );
      return response.data;
    },
    enabled: !!leadId,
  });
}

/**
 * Art. 7.3: Einwilligung widerrufen
 */
export function useGdprRevokeConsent() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (leadId: number) => {
      await api.post(`${GDPR_API_BASE}/${leadId}/revoke-consent`);
    },
    onSuccess: (_, leadId) => {
      // Invalidate lead queries
      queryClient.invalidateQueries({ queryKey: ['leads'] });
      queryClient.invalidateQueries({ queryKey: ['lead', leadId] });
    },
  });
}

/**
 * Kontakt-Erlaubnis prüfen
 */
export function useGdprContactAllowed(leadId: number) {
  return useQuery({
    queryKey: ['gdpr', 'contact-allowed', leadId],
    queryFn: async () => {
      const response = await api.get<ContactAllowedResponse>(
        `${GDPR_API_BASE}/${leadId}/contact-allowed`
      );
      return response.data;
    },
    enabled: !!leadId,
  });
}
