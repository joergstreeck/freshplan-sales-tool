/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class AdminJobsResourceService {
  /**
   * Trigger Sales Rep Sync
   * @returns any OK
   * @throws ApiError
   */
  public static postApiAdminJobsSyncSalesReps(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/admin/jobs/sync-sales-reps',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
