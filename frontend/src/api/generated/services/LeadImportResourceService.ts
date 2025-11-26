/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LeadImportRequest } from '../models/LeadImportRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class LeadImportResourceService {
  /**
   * Import Leads
   * @returns any OK
   * @throws ApiError
   */
  public static postApiAdminMigrationLeadsImport({
    requestBody,
  }: {
    requestBody: LeadImportRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/admin/migration/leads/import',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
