/**
 * Customer Contacts API Hooks
 *
 * Sprint 2.1.7.7 - Technical Debt Fix (Sprint 2.1.7.2 Phase 4 Gap)
 *
 * React Query Hooks für Customer Contact CRUD Operations:
 * - GET /api/customers/{customerId}/contacts
 * - POST /api/customers/{customerId}/contacts
 * - PUT /api/customers/{customerId}/contacts/{contactId}
 * - DELETE /api/customers/{customerId}/contacts/{contactId}
 *
 * Backend API: ContactResource.java
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { httpClient } from '../../../lib/apiClient';
import type { Contact } from '../components/detail/ContactEditDialog';

/**
 * Fetch all contacts for a customer
 *
 * @param customerId - Customer UUID
 * @returns React Query result with Contact[]
 *
 * @example
 * const { data: contacts, isLoading } = useCustomerContacts(customerId);
 */
export function useCustomerContacts(customerId: string) {
  return useQuery({
    queryKey: ['customers', customerId, 'contacts'],
    queryFn: async () => {
      const response = await httpClient.get<Contact[]>(
        `/api/customers/${customerId}/contacts`
      );
      return response.data;
    },
    enabled: !!customerId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

/**
 * Create a new contact for a customer
 *
 * @param customerId - Customer UUID
 * @returns React Query mutation
 *
 * @example
 * const createContact = useCreateCustomerContact(customerId);
 * await createContact.mutateAsync({ firstName: 'John', lastName: 'Doe', ... });
 */
export function useCreateCustomerContact(customerId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (contact: Partial<Contact>) => {
      const response = await httpClient.post<Contact>(
        `/api/customers/${customerId}/contacts`,
        contact
      );
      return response.data;
    },
    onSuccess: () => {
      // Invalidate contacts list to trigger refetch
      queryClient.invalidateQueries({
        queryKey: ['customers', customerId, 'contacts'],
      });
    },
  });
}

/**
 * Update an existing contact
 *
 * @param customerId - Customer UUID
 * @param contactId - Contact UUID
 * @returns React Query mutation
 *
 * @example
 * const updateContact = useUpdateCustomerContact(customerId, contactId);
 * await updateContact.mutateAsync({ decisionLevel: 'MANAGER' });
 */
export function useUpdateCustomerContact(customerId: string, contactId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (updates: Partial<Contact>) => {
      const response = await httpClient.put<Contact>(
        `/api/customers/${customerId}/contacts/${contactId}`,
        updates
      );
      return response.data;
    },
    onSuccess: () => {
      // Invalidate contacts list to trigger refetch
      queryClient.invalidateQueries({
        queryKey: ['customers', customerId, 'contacts'],
      });
    },
  });
}

/**
 * Delete a contact
 *
 * @param customerId - Customer UUID
 * @returns React Query mutation
 *
 * @example
 * const deleteContact = useDeleteCustomerContact(customerId);
 * await deleteContact.mutateAsync(contactId);
 */
export function useDeleteCustomerContact(customerId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (contactId: string) => {
      await httpClient.delete(`/api/customers/${customerId}/contacts/${contactId}`);
    },
    onSuccess: () => {
      // Invalidate contacts list to trigger refetch
      queryClient.invalidateQueries({
        queryKey: ['customers', customerId, 'contacts'],
      });
    },
  });
}
