import { httpClient } from '../../../lib/apiClient';
import type { CustomerListResponse, CustomerResponse } from '../types/customer.types';

interface Contact {
  id: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  position?: string;
}

export const customerApi = {
  // Get single customer by ID
  getCustomer: async (customerId: string): Promise<CustomerResponse> => {
    const response = await httpClient.get<CustomerResponse>(`/api/customers/${customerId}`);
    return response.data;
  },

  // Get paginated list of customers
  getCustomers: async (
    page = 0,
    size = 20,
    sortBy = 'companyName'
  ): Promise<CustomerListResponse> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort: sortBy,
    });
    const response = await httpClient.get<CustomerListResponse>(`/api/customers?${params}`);
    return response.data;
  },

  // Get customers by status
  getCustomersByStatus: async (
    status: string,
    page = 0,
    size = 20
  ): Promise<CustomerListResponse> => {
    const params = new URLSearchParams({
      status,
      page: page.toString(),
      size: size.toString(),
    });
    const response = await httpClient.get<CustomerListResponse>(`/api/customers?${params}`);
    return response.data;
  },

  // Search customers
  searchCustomers: async (query: string, page = 0, size = 20): Promise<CustomerListResponse> => {
    const params = new URLSearchParams({
      q: query,
      page: page.toString(),
      size: size.toString(),
    });
    const response = await httpClient.get<CustomerListResponse>(`/api/customers/search?${params}`);
    return response.data;
  },

  // Get contacts for a customer
  getCustomerContacts: async (customerId: string): Promise<Contact[]> => {
    const response = await httpClient.get<Contact[]>(`/api/customers/${customerId}/contacts`);
    return response.data;
  },
};
