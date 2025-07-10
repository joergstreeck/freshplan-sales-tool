import { useQuery } from '@tanstack/react-query';
import { customerApi } from '../api/customerApi';

export function useCustomerDetails(customerId?: string) {
  return useQuery({
    queryKey: ['customer', customerId],
    queryFn: () => {
      if (!customerId) throw new Error('No customer ID provided');
      return customerApi.getCustomer(customerId);
    },
    enabled: !!customerId,
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
}