/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { Territory } from '../models/Territory';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class TerritoriesService {
  /**
   * Get all territories
   * Returns all configured territories
   * @returns Territory OK
   * @throws ApiError
   */
  public static getApiTerritories(): CancelablePromise<Array<Territory>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/territories',
    });
  }
  /**
   * Get territory by country
   * Determines territory based on country code
   * @returns any OK
   * @throws ApiError
   */
  public static getApiTerritoriesByCountry({
    countryCode,
  }: {
    countryCode: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/territories/by-country/{countryCode}',
      path: {
        countryCode: countryCode,
      },
    });
  }
  /**
   * Get territory by code
   * Returns a specific territory by code
   * @returns any OK
   * @throws ApiError
   */
  public static getApiTerritories1({ code }: { code: string }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/territories/{code}',
      path: {
        code: code,
      },
    });
  }
}
