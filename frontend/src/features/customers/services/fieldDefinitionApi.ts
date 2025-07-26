/**
 * Field Definition API Service
 * 
 * Service für Field Definition Management.
 * Cached Field Definitions für optimale Performance.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/02-field-system.md
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/03-field-rendering.md
 */

import { apiClient } from './api-client';
import {
  GetFieldDefinitionsRequest,
  FieldDefinitionsResponse,
} from '../types/api.types';
import { FieldDefinition, EntityType } from '../types/field.types';

export class FieldDefinitionApi {
  private readonly basePath = '/api/field-definitions';
  private cache = new Map<string, FieldDefinition[]>();
  private cacheExpiry = new Map<string, number>();
  private readonly CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
  
  /**
   * Get field definitions for entity type and industry
   * GET /api/field-definitions
   * 
   * Cached for performance - field definitions change rarely
   */
  async getFieldDefinitions(
    entityType: EntityType,
    industry?: string,
    forceRefresh: boolean = false
  ): Promise<FieldDefinition[]> {
    const cacheKey = this.getCacheKey(entityType, industry);
    
    // Check cache first
    if (!forceRefresh && this.isCacheValid(cacheKey)) {
      return this.cache.get(cacheKey)!;
    }
    
    // Fetch from API
    const request: GetFieldDefinitionsRequest = {
      entityType,
      industry,
      includeInactive: false,
    };
    
    const response = await apiClient.get<FieldDefinitionsResponse>(
      this.basePath,
      {
        params: {
          entityType: request.entityType,
          industry: request.industry,
          includeInactive: request.includeInactive,
        },
        retry: 2,
      }
    );
    
    // Transform response to flat array
    const definitions = this.flattenFieldDefinitions(response, industry);
    
    // Update cache
    this.cache.set(cacheKey, definitions);
    this.cacheExpiry.set(cacheKey, Date.now() + this.CACHE_DURATION);
    
    return definitions;
  }
  
  /**
   * Get all available industries
   * GET /api/field-definitions/industries
   */
  async getIndustries(): Promise<string[]> {
    const cacheKey = 'industries';
    
    if (this.isCacheValid(cacheKey)) {
      return this.cache.get(cacheKey) as any;
    }
    
    const industries = await apiClient.get<string[]>(
      `${this.basePath}/industries`,
      { retry: 2 }
    );
    
    this.cache.set(cacheKey, industries as any);
    this.cacheExpiry.set(cacheKey, Date.now() + this.CACHE_DURATION);
    
    return industries;
  }
  
  /**
   * Get field definition by ID
   * GET /api/field-definitions/{fieldId}
   */
  async getFieldDefinition(fieldId: string): Promise<FieldDefinition> {
    const cacheKey = `field-${fieldId}`;
    
    if (this.isCacheValid(cacheKey)) {
      return this.cache.get(cacheKey)![0];
    }
    
    const definition = await apiClient.get<FieldDefinition>(
      `${this.basePath}/${fieldId}`,
      { retry: 2 }
    );
    
    this.cache.set(cacheKey, [definition]);
    this.cacheExpiry.set(cacheKey, Date.now() + this.CACHE_DURATION);
    
    return definition;
  }
  
  /**
   * Validate field value against definition
   * POST /api/field-definitions/validate
   */
  async validateFieldValue(
    fieldId: string,
    value: any
  ): Promise<{ valid: boolean; errors?: string[] }> {
    return apiClient.post(
      `${this.basePath}/validate`,
      { fieldId, value }
    );
  }
  
  /**
   * Get field dependencies
   * GET /api/field-definitions/{fieldId}/dependencies
   */
  async getFieldDependencies(fieldId: string): Promise<{
    dependsOn: string[];
    dependentFields: string[];
  }> {
    return apiClient.get(
      `${this.basePath}/${fieldId}/dependencies`,
      { retry: 2 }
    );
  }
  
  /**
   * Clear cache - useful after field definition updates
   */
  clearCache(): void {
    this.cache.clear();
    this.cacheExpiry.clear();
  }
  
  /**
   * Clear specific cache entry
   */
  clearCacheEntry(entityType: EntityType, industry?: string): void {
    const cacheKey = this.getCacheKey(entityType, industry);
    this.cache.delete(cacheKey);
    this.cacheExpiry.delete(cacheKey);
  }
  
  /**
   * Get all cached field definitions (for debugging)
   */
  getCachedDefinitions(): Map<string, FieldDefinition[]> {
    return new Map(this.cache);
  }
  
  /**
   * Generate cache key
   */
  private getCacheKey(entityType: EntityType, industry?: string): string {
    return `${entityType}-${industry || 'all'}`;
  }
  
  /**
   * Check if cache is still valid
   */
  private isCacheValid(cacheKey: string): boolean {
    const expiry = this.cacheExpiry.get(cacheKey);
    return expiry ? expiry > Date.now() : false;
  }
  
  /**
   * Flatten field definitions response
   */
  private flattenFieldDefinitions(
    response: FieldDefinitionsResponse,
    industry?: string
  ): FieldDefinition[] {
    const fields: FieldDefinition[] = [...response.baseFields] as any;
    
    // Add industry-specific fields if industry is specified
    if (industry && response.industryFields[industry]) {
      fields.push(...(response.industryFields[industry] as any));
    }
    
    // Sort by displayOrder
    return fields.sort((a, b) => (a.displayOrder || 999) - (b.displayOrder || 999));
  }
  
  /**
   * Preload common field definitions
   * Useful for initial app load
   */
  async preloadCommonDefinitions(): Promise<void> {
    const preloadTasks = [
      this.getFieldDefinitions(EntityType.CUSTOMER),
      this.getFieldDefinitions(EntityType.LOCATION),
      this.getIndustries(),
    ];
    
    await Promise.all(preloadTasks);
  }
}

// Export singleton instance
export const fieldDefinitionApi = new FieldDefinitionApi();