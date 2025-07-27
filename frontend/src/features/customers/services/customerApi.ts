/**
 * Customer API Service
 * 
 * Service für alle Customer-bezogenen API-Calls.
 * Implementiert die Customer Management REST API.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/03-rest-api.md
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-api-integration.md
 */

import { apiClient } from './api-client';
import {
  CustomerWithFields,
  CustomerDraftResponse,
  CustomerSearchResponse,
  CreateCustomerDraftRequest,
  UpdateCustomerDraftRequest,
  FinalizeCustomerDraftRequest,
  CustomerSearchRequest,
  CustomerExportRequest,
  CustomerImportRequest,
  ImportResultResponse,
  PaginatedResponse,
  CustomerListItem,
} from '../types/api.types';
import { Customer } from '../types/customer.types';

export class CustomerApi {
  private readonly basePath = '/api/customers';
  
  /**
   * Create a new customer draft
   * POST /api/customers/drafts
   */
  async createDraft(request: CreateCustomerDraftRequest = {}): Promise<CustomerDraftResponse> {
    return apiClient.post<CustomerDraftResponse>(
      `${this.basePath}/drafts`,
      request,
      { retry: 2 }
    );
  }
  
  /**
   * Get customer draft by ID
   * GET /api/customers/drafts/{draftId}
   */
  async getDraft(draftId: string): Promise<CustomerDraftResponse> {
    return apiClient.get<CustomerDraftResponse>(
      `${this.basePath}/drafts/${draftId}`,
      { retry: 2 }
    );
  }
  
  /**
   * Update customer draft
   * PUT /api/customers/drafts/{draftId}
   */
  async updateDraft(
    draftId: string,
    request: UpdateCustomerDraftRequest
  ): Promise<CustomerDraftResponse> {
    return apiClient.put<CustomerDraftResponse>(
      `${this.basePath}/drafts/${draftId}`,
      request,
      { retry: 1 }
    );
  }
  
  /**
   * Delete customer draft
   * DELETE /api/customers/drafts/{draftId}
   */
  async deleteDraft(draftId: string): Promise<void> {
    return apiClient.delete<void>(`${this.basePath}/drafts/${draftId}`);
  }
  
  /**
   * Finalize customer draft and create active customer
   * POST /api/customers/drafts/{draftId}/finalize
   */
  async finalizeDraft(
    draftId: string,
    request: FinalizeCustomerDraftRequest = {}
  ): Promise<Customer> {
    return apiClient.post<Customer>(
      `${this.basePath}/drafts/${draftId}/finalize`,
      request,
      { retry: 2 }
    );
  }
  
  /**
   * Get all drafts for current user
   * GET /api/customers/drafts
   */
  async getUserDrafts(): Promise<CustomerDraftResponse[]> {
    return apiClient.get<CustomerDraftResponse[]>(
      `${this.basePath}/drafts`,
      { retry: 2 }
    );
  }
  
  /**
   * Search customers with pagination and filters
   * GET /api/customers
   */
  async searchCustomers(request: CustomerSearchRequest): Promise<CustomerSearchResponse> {
    return apiClient.get<CustomerSearchResponse>(
      this.basePath,
      {
        params: {
          page: request.page,
          size: request.size,
          sort: request.sort,
          searchTerm: request.searchTerm,
          status: request.status,
          industry: request.industry,
          assignedTo: request.assignedTo,
          createdFrom: request.createdFrom,
          createdTo: request.createdTo,
          hasLocations: request.hasLocations,
          fieldFilters: request.fieldFilters ? JSON.stringify(request.fieldFilters) : undefined,
        },
        retry: 2,
      }
    );
  }
  
  /**
   * Get customer by ID with all fields
   * GET /api/customers/{customerId}
   */
  async getCustomer(customerId: string): Promise<CustomerWithFields> {
    return apiClient.get<CustomerWithFields>(
      `${this.basePath}/${customerId}`,
      { retry: 2 }
    );
  }
  
  /**
   * Update customer
   * PUT /api/customers/{customerId}
   */
  async updateCustomer(
    customerId: string,
    fieldValues: Record<string, any>
  ): Promise<CustomerWithFields> {
    return apiClient.put<CustomerWithFields>(
      `${this.basePath}/${customerId}`,
      { fieldValues },
      { retry: 1 }
    );
  }
  
  /**
   * Deactivate customer
   * POST /api/customers/{customerId}/deactivate
   */
  async deactivateCustomer(customerId: string, reason?: string): Promise<void> {
    return apiClient.post<void>(
      `${this.basePath}/${customerId}/deactivate`,
      { reason }
    );
  }
  
  /**
   * Reactivate customer
   * POST /api/customers/{customerId}/reactivate
   */
  async reactivateCustomer(customerId: string): Promise<void> {
    return apiClient.post<void>(`${this.basePath}/${customerId}/reactivate`);
  }
  
  /**
   * Get customer statistics
   * GET /api/customers/statistics
   */
  async getStatistics(): Promise<{
    totalCustomers: number;
    activeCustomers: number;
    draftCustomers: number;
    customersByIndustry: Record<string, number>;
    customersByMonth: Array<{ month: string; count: number }>;
  }> {
    return apiClient.get(`${this.basePath}/statistics`, { retry: 2 });
  }
  
  /**
   * Export customers
   * POST /api/customers/export
   */
  async exportCustomers(request: CustomerExportRequest): Promise<Blob> {
    const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}${this.basePath}/export`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${sessionStorage.getItem('auth_token') || localStorage.getItem('auth_token')}`,
      },
      body: JSON.stringify(request),
    });
    
    if (!response.ok) {
      throw new Error(`Export failed: ${response.statusText}`);
    }
    
    return response.blob();
  }
  
  /**
   * Import customers
   * POST /api/customers/import
   */
  async importCustomers(request: CustomerImportRequest): Promise<ImportResultResponse> {
    return apiClient.post<ImportResultResponse>(
      `${this.basePath}/import`,
      request,
      { timeout: 60000 } // 1 minute timeout for large imports
    );
  }
  
  /**
   * Validate import file
   * POST /api/customers/import/validate
   */
  async validateImport(file: File): Promise<{
    valid: boolean;
    errors?: string[];
    preview?: Array<Record<string, any>>;
  }> {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}${this.basePath}/import/validate`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${sessionStorage.getItem('auth_token') || localStorage.getItem('auth_token')}`,
      },
      body: formData,
    });
    
    if (!response.ok) {
      throw new Error(`Validation failed: ${response.statusText}`);
    }
    
    return response.json();
  }
  
  /**
   * Get recent customers for dashboard
   * GET /api/customers/recent
   */
  async getRecentCustomers(limit: number = 10): Promise<CustomerListItem[]> {
    return apiClient.get<CustomerListItem[]>(
      `${this.basePath}/recent`,
      { params: { limit }, retry: 2 }
    );
  }
  
  /**
   * Bulk update customers
   * POST /api/customers/bulk-update
   */
  async bulkUpdate(
    customerIds: string[],
    fieldValues: Record<string, any>
  ): Promise<{ updated: number; failed: number; errors?: Array<{ id: string; error: string }> }> {
    return apiClient.post(
      `${this.basePath}/bulk-update`,
      { customerIds, fieldValues },
      { timeout: 30000 }
    );
  }
}

// Export singleton instance
export const customerApi = new CustomerApi();