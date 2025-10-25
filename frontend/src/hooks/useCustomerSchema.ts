import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';
import type { CustomerCardSchema } from '../types/customer-schema';

/**
 * Fetch customer card schema from backend API
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * GET /api/customers/schema
 * Returns: Array of 7 CustomerCardSchema objects defining all customer cards
 *
 * This is the **Single Source of Truth** for customer UI structure.
 * Backend defines:
 * - Which fields exist
 * - Field types and validation
 * - Section grouping
 * - Display order
 *
 * Frontend just renders what backend defines → No more fieldCatalog.json!
 */
async function fetchCustomerSchema(): Promise<CustomerCardSchema[]> {
  const response = await fetch(`${BASE_URL}/api/customers/schema`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch customer schema: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch customer card schema
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * Usage:
 * ```tsx
 * const { data: cardSchemas, isLoading, error } = useCustomerSchema();
 *
 * if (isLoading) return <CircularProgress />;
 * if (error) return <Alert severity="error">{error.message}</Alert>;
 *
 * return cardSchemas.map(schema => (
 *   <DynamicCustomerCard key={schema.cardId} schema={schema} customerId={id} />
 * ));
 * ```
 *
 * @returns Query result with array of 7 card schemas
 */
export function useCustomerSchema() {
  return useQuery({
    queryKey: ['customers', 'schema'],
    queryFn: fetchCustomerSchema,
    staleTime: 30 * 60 * 1000, // 30 minutes (schema changes rarely)
    gcTime: 60 * 60 * 1000, // 60 minutes (was cacheTime in v4)
  });
}
