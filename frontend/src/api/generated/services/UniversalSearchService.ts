/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { SearchResults } from '../models/SearchResults';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class UniversalSearchService {
  /**
   * Quick Search
   * Lightweight search for autocomplete functionality
   * @returns any Quick search results
   * @throws ApiError
   */
  public static getApiSearchQuick({
    query,
    limit = 5,
  }: {
    query: string;
    limit?: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/search/quick',
      query: {
        limit: limit,
        query: query,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Universal Search
   * Search across customers and contacts OR leads and lead contacts with intelligent query analysis
   * @returns SearchResults Search results
   * @throws ApiError
   */
  public static getApiSearchUniversal({
    query,
    context = 'customers',
    includeContacts = true,
    includeInactive = false,
    limit = 20,
  }: {
    /**
     * Search query (min 2 characters)
     */
    query: string;
    /**
     * Search context: 'leads' for leads, otherwise customers
     */
    context?: string;
    /**
     * Include contacts in results
     */
    includeContacts?: boolean;
    /**
     * Include inactive customers/leads
     */
    includeInactive?: boolean;
    /**
     * Maximum results per entity type
     */
    limit?: number;
  }): CancelablePromise<SearchResults> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/search/universal',
      query: {
        context: context,
        includeContacts: includeContacts,
        includeInactive: includeInactive,
        limit: limit,
        query: query,
      },
      errors: {
        400: `Invalid search parameters`,
        401: `Unauthorized`,
        403: `Not Allowed`,
      },
    });
  }
}
