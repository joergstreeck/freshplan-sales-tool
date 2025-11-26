/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CalculatorRequest } from '../models/CalculatorRequest';
import type { CalculatorResponse } from '../models/CalculatorResponse';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class CalculatorService {
  /**
   * Calculate discount
   * Calculates discount based on order value, lead time, and other parameters
   * @returns CalculatorResponse Calculation successful
   * @throws ApiError
   */
  public static postApiCalculatorCalculate({
    requestBody,
  }: {
    /**
     * Calculation parameters
     */
    requestBody: CalculatorRequest;
  }): CancelablePromise<CalculatorResponse> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/calculator/calculate',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid input parameters`,
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get discount rules
   * Returns the current discount rules configuration
   * @returns any Rules retrieved successfully
   * @throws ApiError
   */
  public static getApiCalculatorRules(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/calculator/rules',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
