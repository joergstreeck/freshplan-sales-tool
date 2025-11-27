/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BusinessModelDto } from '../models/BusinessModelDto';
import type { ChainStructureDto } from '../models/ChainStructureDto';
import type { PotentialCalculationRequest } from '../models/PotentialCalculationRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class CustomerChainResourceService {
  /**
   * Update Business Model
   * @returns any OK
   * @throws ApiError
   */
  public static patchApiV1CustomersBusinessModel({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: BusinessModelDto;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PATCH',
      url: '/api/v1/customers/{id}/business-model',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Calculate Potential
   * @returns any OK
   * @throws ApiError
   */
  public static postApiV1CustomersCalculatePotential({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: PotentialCalculationRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/v1/customers/{id}/calculate-potential',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update Chain Structure
   * @returns any OK
   * @throws ApiError
   */
  public static patchApiV1CustomersChainStructure({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: ChainStructureDto;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PATCH',
      url: '/api/v1/customers/{id}/chain-structure',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
