/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DataQualityMetricsDTO1 } from '../models/DataQualityMetricsDTO1';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class DataQualityService {
  /**
   * Get data freshness statistics
   * Returns statistics about data freshness levels
   * @returns any Data freshness statistics retrieved successfully
   * @throws ApiError
   */
  public static getApiContactInteractionsDataFreshnessStatistics(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contact-interactions/data-freshness/statistics',
      errors: {
        401: `User not authenticated`,
        403: `Not Allowed`,
        500: `Internal server error`,
      },
    });
  }
  /**
   * Get data quality metrics
   * Returns comprehensive data quality metrics including freshness, completeness, and coverage
   * @returns DataQualityMetricsDTO1 Data quality metrics retrieved successfully
   * @throws ApiError
   */
  public static getApiContactInteractionsDataQualityMetrics(): CancelablePromise<DataQualityMetricsDTO1> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contact-interactions/data-quality/metrics',
      errors: {
        401: `User not authenticated`,
        403: `User not authorized`,
        500: `Internal server error`,
      },
    });
  }
}
