/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContactInteractionDTO } from '../models/ContactInteractionDTO';
import type { DataQualityMetricsDTO } from '../models/DataQualityMetricsDTO';
import type { UUID } from '../models/UUID';
import type { WarmthScoreDTO } from '../models/WarmthScoreDTO';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ContactInteractionsService {
  /**
   * Create a new contact interaction
   * @returns any Interaction created successfully
   * @throws ApiError
   */
  public static postApiContactInteractions({
    requestBody,
  }: {
    requestBody: ContactInteractionDTO;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/contact-interactions',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid interaction data`,
        404: `Contact not found`,
      },
    });
  }
  /**
   * Get interactions for a specific contact
   * @returns ContactInteractionDTO List of interactions
   * @throws ApiError
   */
  public static getApiContactInteractionsContact({
    contactId,
    page,
    size = 20,
  }: {
    /**
     * Contact ID
     */
    contactId: UUID;
    /**
     * Page number (0-based)
     */
    page?: number;
    /**
     * Page size
     */
    size?: number;
  }): CancelablePromise<Array<ContactInteractionDTO>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contact-interactions/contact/{contactId}',
      path: {
        contactId: contactId,
      },
      query: {
        page: page,
        size: size,
      },
      errors: {
        404: `Contact not found`,
      },
    });
  }
  /**
   * Quick create a note interaction
   * @returns any Note created successfully
   * @throws ApiError
   */
  public static postApiContactInteractionsContactNote({
    contactId,
    requestBody,
    xUserId,
  }: {
    /**
     * Contact ID
     */
    contactId: UUID;
    requestBody: string;
    /**
     * User creating the note
     */
    xUserId?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/contact-interactions/contact/{contactId}/note',
      path: {
        contactId: contactId,
      },
      headers: {
        'X-User-Id': xUserId,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        404: `Contact not found`,
      },
    });
  }
  /**
   * Calculate warmth score for a contact
   * @returns WarmthScoreDTO Warmth score calculated
   * @throws ApiError
   */
  public static getApiContactInteractionsContactWarmthScore({
    contactId,
  }: {
    /**
     * Contact ID
     */
    contactId: UUID;
  }): CancelablePromise<WarmthScoreDTO> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contact-interactions/contact/{contactId}/warmth-score',
      path: {
        contactId: contactId,
      },
      errors: {
        404: `Contact not found`,
      },
    });
  }
  /**
   * Get contacts filtered by freshness level
   * @returns any Contacts retrieved successfully
   * @throws ApiError
   */
  public static getApiContactInteractionsContactsByFreshness({
    level,
  }: {
    level: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contact-interactions/contacts-by-freshness/{level}',
      path: {
        level: level,
      },
      errors: {
        400: `Invalid freshness level`,
      },
    });
  }
  /**
   * Get data freshness level for a contact
   * @returns any Freshness level retrieved successfully
   * @throws ApiError
   */
  public static getApiContactInteractionsFreshness({
    contactId,
  }: {
    contactId: UUID;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contact-interactions/freshness/{contactId}',
      path: {
        contactId: contactId,
      },
      errors: {
        404: `Contact not found`,
      },
    });
  }
  /**
   * Batch import historical interactions
   * @returns any Import completed
   * @throws ApiError
   */
  public static postApiContactInteractionsImportBatch({
    requestBody,
  }: {
    requestBody: Array<ContactInteractionDTO>;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/contact-interactions/import/batch',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid import data`,
        500: `Import failed`,
      },
    });
  }
  /**
   * Get data quality metrics for intelligence features
   * @returns DataQualityMetricsDTO Data quality metrics
   * @throws ApiError
   */
  public static getApiContactInteractionsMetricsDataQuality(): CancelablePromise<DataQualityMetricsDTO> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contact-interactions/metrics/data-quality',
    });
  }
  /**
   * Get comprehensive data quality score for a contact
   * @returns any Quality score calculated successfully
   * @throws ApiError
   */
  public static getApiContactInteractionsQualityScore({
    contactId,
  }: {
    contactId: UUID;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contact-interactions/quality-score/{contactId}',
      path: {
        contactId: contactId,
      },
      errors: {
        404: `Contact not found`,
      },
    });
  }
  /**
   * Get data freshness statistics
   * @returns any Freshness statistics retrieved successfully
   * @throws ApiError
   */
  public static getApiContactInteractionsStatisticsFreshness(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contact-interactions/statistics/freshness',
    });
  }
  /**
   * Manually trigger data hygiene check
   * @returns any Hygiene check completed successfully
   * @throws ApiError
   */
  public static postApiContactInteractionsTriggerHygieneCheck(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/contact-interactions/trigger-hygiene-check',
    });
  }
}
