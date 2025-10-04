import { useQuery } from '@tanstack/react-query';
import { listLeads } from '../api';

/**
 * Hook to fetch all leads
 *
 * @returns Query result with leads data
 */
export function useLeads() {
  return useQuery({
    queryKey: ['leads'],
    queryFn: listLeads,
    staleTime: 30000, // 30 seconds
  });
}
