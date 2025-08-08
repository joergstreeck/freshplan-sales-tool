import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { useFocusListStore } from '../store/focusListStore';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export interface Customer {
  id: string;
  companyName: string;
  customerNumber: string;
  tradingName?: string;
  status: 'LEAD' | 'AKTIV' | 'INAKTIV' | 'GESPERRT';
  industry?: string;
  riskScore: number;
  lastContactDate?: string;
  expectedAnnualVolume?: number;
  assignedTo?: string;
  tags?: string[];
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export const useCustomerSearch = () => {
  const { getSearchRequest, page, pageSize } = useFocusListStore();

  return useQuery<PagedResponse<Customer>>({
    queryKey: ['customers', 'search', getSearchRequest(), page, pageSize],
    queryFn: async () => {
      const searchRequest = getSearchRequest();
      const response = await fetch(
        `${API_URL}/api/customers/search?page=${page}&size=${pageSize}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(searchRequest),
        }
      );

      if (!response.ok) {
        throw new Error(`API request failed: ${response.statusText}`);
      }

      return response.json();
    },
    placeholderData: keepPreviousData,
    staleTime: 30000, // 30 Sekunden
    gcTime: 5 * 60 * 1000, // 5 Minuten (früher cacheTime)
  });
};

// Hook für Quick Stats
export const useCustomerStats = () => {
  return useQuery({
    queryKey: ['customers', 'stats'],
    queryFn: async () => {
      // TODO: Implementiere Stats-Endpoint im Backend
      // Vorerst Mock-Daten
      return {
        totalCustomers: 0,
        activeCustomers: 0,
        newLeads: 0,
        highRiskCustomers: 0,
      };
    },
    staleTime: 60000, // 1 Minute
  });
};

// Utility Hook für Filter-Vorschläge
export const useFilterSuggestions = (field: string, query: string) => {
  return useQuery({
    queryKey: ['customers', 'suggestions', field, query],
    queryFn: async () => {
      // TODO: Implementiere Suggestions-Endpoint im Backend
      // Vorerst leeres Array
      return [];
    },
    enabled: query.length > 2,
    staleTime: 60000,
  });
};
