/**
 * Customer Contacts Hook
 *
 * Sprint 2.1.7.2 D11.1 (Hotfix): Contact API Integration
 *
 * Fetches contacts for a specific customer from the backend API.
 * Backend: GET /api/customers/{id}/contacts (ContactResource.java:32)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useQuery } from '@tanstack/react-query';
import { httpClient } from '../../../lib/apiClient';
import type { Contact } from '../../customers/components/detail/ContactEditDialog';

export function useCustomerContacts(customerId: string) {
  return useQuery({
    queryKey: ['customers', customerId, 'contacts'],
    queryFn: async () => {
      if (!customerId) throw new Error('No customer ID provided');
      const response = await httpClient.get<Contact[]>(`/api/customers/${customerId}/contacts`);
      return response.data;
    },
    enabled: !!customerId,
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
}
