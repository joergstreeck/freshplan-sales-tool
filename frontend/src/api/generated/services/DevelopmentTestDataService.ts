/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class DevelopmentTestDataService {
  /**
   * Clean test data
   * Removes all test data from the database
   * @returns any Test data cleaned successfully
   * @throws ApiError
   */
  public static deleteApiDevTestDataClean(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/dev/test-data/clean',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        500: `Error cleaning test data`,
      },
    });
  }
  /**
   * Clean old test data
   * One-time cleanup to remove old test data without [TEST] prefix
   * @returns any Old test data cleaned successfully
   * @throws ApiError
   */
  public static deleteApiDevTestDataCleanOld(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/dev/test-data/clean-old',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Seed test data
   * Seeds the database with diverse test customers and related data
   * @returns any Test data seeded successfully
   * @throws ApiError
   */
  public static postApiDevTestDataSeed(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/dev/test-data/seed',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        500: `Error seeding test data`,
      },
    });
  }
  /**
   * Seed additional 14 test customers
   * Seeds 14 additional test customers to reach exactly 58 total
   * @returns any Additional test data seeded successfully
   * @throws ApiError
   */
  public static postApiDevTestDataSeedAdditional(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/dev/test-data/seed-additional',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        500: `Error seeding additional test data`,
      },
    });
  }
  /**
   * Seed comprehensive edge-case test data
   * Seeds the database with comprehensive test data covering all edge cases for thorough testing
   * @returns any Comprehensive test data seeded successfully
   * @throws ApiError
   */
  public static postApiDevTestDataSeedComprehensive(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/dev/test-data/seed-comprehensive',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        500: `Error seeding comprehensive test data`,
      },
    });
  }
  /**
   * Get test data statistics
   * Returns current count of test data in the database
   * @returns any Test data statistics
   * @throws ApiError
   */
  public static getApiDevTestDataStats(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/dev/test-data/stats',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
