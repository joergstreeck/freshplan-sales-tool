/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class XentralResourceService {
  /**
   * Get Customers
   * @returns any OK
   * @throws ApiError
   */
  public static getApiXentralCustomers({
    salesRepId,
  }: {
    salesRepId?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/xentral/customers',
      query: {
        salesRepId: salesRepId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Customer By Id
   * @returns any OK
   * @throws ApiError
   */
  public static getApiXentralCustomers1({
    xentralId,
  }: {
    xentralId: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/xentral/customers/{xentralId}',
      path: {
        xentralId: xentralId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Sales Reps
   * @returns any OK
   * @throws ApiError
   */
  public static getApiXentralEmployeesSalesReps(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/xentral/employees/sales-reps',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Invoices
   * @returns any OK
   * @throws ApiError
   */
  public static getApiXentralInvoices({
    customerId,
  }: {
    customerId?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/xentral/invoices',
      query: {
        customerId: customerId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Test Connection
   * @returns any OK
   * @throws ApiError
   */
  public static getApiXentralTestConnection(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/xentral/test-connection',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
