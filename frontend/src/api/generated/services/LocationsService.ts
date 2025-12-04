/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ServiceFieldGroup } from '../models/ServiceFieldGroup';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class LocationsService {
  /**
   * Get location service schema by industry
   * @returns ServiceFieldGroup Service field groups for the specified industry
   * @throws ApiError
   */
  public static getApiLocationsServiceSchema({
    industry,
  }: {
    /**
     * Industry type
     */
    industry: string;
  }): CancelablePromise<ServiceFieldGroup> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/locations/service-schema',
      query: {
        industry: industry,
      },
    });
  }
}
