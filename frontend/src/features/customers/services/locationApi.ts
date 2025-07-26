/**
 * Location API Service
 * 
 * Service für alle Location-bezogenen API-Calls.
 * Verwaltet Standorte und DetailedLocations.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/03-rest-api.md
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/02-data-model.md
 */

import { apiClient } from './api-client';
import {
  CreateLocationRequest,
  UpdateLocationRequest,
  LocationResponse,
  PaginatedResponse,
} from '../types/api.types';
import {
  Location,
  LocationWithFields,
  DetailedLocation,
  DetailedLocationBatch,
} from '../types/location.types';

export class LocationApi {
  private readonly basePath = '/api/locations';
  
  /**
   * Create new location for customer
   * POST /api/locations
   */
  async createLocation(request: CreateLocationRequest): Promise<LocationResponse> {
    return apiClient.post<LocationResponse>(
      this.basePath,
      request,
      { retry: 2 }
    );
  }
  
  /**
   * Get location by ID
   * GET /api/locations/{locationId}
   */
  async getLocation(locationId: string): Promise<LocationResponse> {
    return apiClient.get<LocationResponse>(
      `${this.basePath}/${locationId}`,
      { retry: 2 }
    );
  }
  
  /**
   * Update location
   * PUT /api/locations/{locationId}
   */
  async updateLocation(
    locationId: string,
    request: UpdateLocationRequest
  ): Promise<LocationResponse> {
    return apiClient.put<LocationResponse>(
      `${this.basePath}/${locationId}`,
      request,
      { retry: 1 }
    );
  }
  
  /**
   * Delete location
   * DELETE /api/locations/{locationId}
   */
  async deleteLocation(locationId: string): Promise<void> {
    return apiClient.delete<void>(`${this.basePath}/${locationId}`);
  }
  
  /**
   * Get all locations for customer
   * GET /api/customers/{customerId}/locations
   */
  async getCustomerLocations(
    customerId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<LocationWithFields>> {
    return apiClient.get<PaginatedResponse<LocationWithFields>>(
      `/api/customers/${customerId}/locations`,
      {
        params: { page, size },
        retry: 2,
      }
    );
  }
  
  /**
   * Bulk create locations for customer
   * POST /api/customers/{customerId}/locations/bulk
   */
  async bulkCreateLocations(
    customerId: string,
    locations: Array<{ fieldValues: Record<string, any> }>
  ): Promise<LocationResponse[]> {
    return apiClient.post<LocationResponse[]>(
      `/api/customers/${customerId}/locations/bulk`,
      { locations },
      { timeout: 30000, retry: 1 }
    );
  }
  
  /**
   * Get detailed locations for a location
   * GET /api/locations/{locationId}/detailed-locations
   */
  async getDetailedLocations(
    locationId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<DetailedLocation>> {
    return apiClient.get<PaginatedResponse<DetailedLocation>>(
      `${this.basePath}/${locationId}/detailed-locations`,
      {
        params: { page, size },
        retry: 2,
      }
    );
  }
  
  /**
   * Create detailed location
   * POST /api/locations/{locationId}/detailed-locations
   */
  async createDetailedLocation(
    locationId: string,
    data: Partial<DetailedLocation>
  ): Promise<DetailedLocation> {
    return apiClient.post<DetailedLocation>(
      `${this.basePath}/${locationId}/detailed-locations`,
      data,
      { retry: 2 }
    );
  }
  
  /**
   * Update detailed location
   * PUT /api/locations/{locationId}/detailed-locations/{detailedLocationId}
   */
  async updateDetailedLocation(
    locationId: string,
    detailedLocationId: string,
    data: Partial<DetailedLocation>
  ): Promise<DetailedLocation> {
    return apiClient.put<DetailedLocation>(
      `${this.basePath}/${locationId}/detailed-locations/${detailedLocationId}`,
      data,
      { retry: 1 }
    );
  }
  
  /**
   * Delete detailed location
   * DELETE /api/locations/{locationId}/detailed-locations/{detailedLocationId}
   */
  async deleteDetailedLocation(
    locationId: string,
    detailedLocationId: string
  ): Promise<void> {
    return apiClient.delete<void>(
      `${this.basePath}/${locationId}/detailed-locations/${detailedLocationId}`
    );
  }
  
  /**
   * Bulk create detailed locations
   * POST /api/locations/{locationId}/detailed-locations/bulk
   */
  async bulkCreateDetailedLocations(
    locationId: string,
    batch: DetailedLocationBatch
  ): Promise<DetailedLocation[]> {
    return apiClient.post<DetailedLocation[]>(
      `${this.basePath}/${locationId}/detailed-locations/bulk`,
      batch,
      { timeout: 30000, retry: 1 }
    );
  }
  
  /**
   * Search locations across all customers
   * GET /api/locations/search
   */
  async searchLocations(params: {
    searchTerm?: string;
    city?: string;
    postalCode?: string;
    country?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<LocationWithFields>> {
    return apiClient.get<PaginatedResponse<LocationWithFields>>(
      `${this.basePath}/search`,
      {
        params: {
          searchTerm: params.searchTerm,
          city: params.city,
          postalCode: params.postalCode,
          country: params.country,
          page: params.page || 0,
          size: params.size || 20,
        },
        retry: 2,
      }
    );
  }
  
  /**
   * Get location statistics
   * GET /api/locations/statistics
   */
  async getStatistics(): Promise<{
    totalLocations: number;
    locationsByCountry: Record<string, number>;
    averageLocationsPerCustomer: number;
    mostCommonCities: Array<{ city: string; count: number }>;
  }> {
    return apiClient.get(`${this.basePath}/statistics`, { retry: 2 });
  }
  
  /**
   * Validate address
   * POST /api/locations/validate-address
   */
  async validateAddress(address: {
    street?: string;
    city?: string;
    postalCode?: string;
    country?: string;
  }): Promise<{
    valid: boolean;
    suggestions?: Array<{
      street: string;
      city: string;
      postalCode: string;
      country: string;
      confidence: number;
    }>;
  }> {
    return apiClient.post(
      `${this.basePath}/validate-address`,
      address
    );
  }
  
  /**
   * Geocode address
   * POST /api/locations/geocode
   */
  async geocodeAddress(address: {
    street?: string;
    city?: string;
    postalCode?: string;
    country?: string;
  }): Promise<{
    latitude: number;
    longitude: number;
    accuracy: string;
  }> {
    return apiClient.post(
      `${this.basePath}/geocode`,
      address
    );
  }
}

// Export singleton instance
export const locationApi = new LocationApi();