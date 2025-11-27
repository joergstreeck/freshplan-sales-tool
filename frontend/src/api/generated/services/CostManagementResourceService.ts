/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BudgetLimitRequest } from '../models/BudgetLimitRequest';
import type { SimulateTransactionRequest } from '../models/SimulateTransactionRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class CostManagementResourceService {
  /**
   * Create Budget Limit
   * @returns any OK
   * @throws ApiError
   */
  public static postApiCostManagementBudgetLimits({
    requestBody,
  }: {
    requestBody: BudgetLimitRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/cost-management/budget-limits',
      body: requestBody,
      mediaType: 'application/json',
    });
  }
  /**
   * Get Today Dashboard
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCostManagementDashboardToday(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/cost-management/dashboard/today',
    });
  }
  /**
   * Get Health Status
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCostManagementHealth(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/cost-management/health',
    });
  }
  /**
   * Simulate Transaction
   * @returns any OK
   * @throws ApiError
   */
  public static postApiCostManagementSimulateTransaction({
    requestBody,
  }: {
    requestBody: SimulateTransactionRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/cost-management/simulate-transaction',
      body: requestBody,
      mediaType: 'application/json',
    });
  }
  /**
   * Get Cost Statistics
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCostManagementStatistics({
    end,
    start,
  }: {
    end?: string;
    start?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/cost-management/statistics',
      query: {
        end: end,
        start: start,
      },
    });
  }
  /**
   * Trigger Budget Check
   * @returns any OK
   * @throws ApiError
   */
  public static postApiCostManagementTriggerBudgetCheck(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/cost-management/trigger-budget-check',
    });
  }
}
