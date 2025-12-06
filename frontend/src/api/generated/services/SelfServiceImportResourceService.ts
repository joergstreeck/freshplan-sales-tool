/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ImportExecuteRequest } from '../models/ImportExecuteRequest';
import type { ImportPreviewRequest } from '../models/ImportPreviewRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class SelfServiceImportResourceService {
  /**
   * Get Quota
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeadsImportQuota(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/import/quota',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Upload File
   * @returns any OK
   * @throws ApiError
   */
  public static postApiLeadsImportUpload({
    formData,
  }: {
    formData: {
      file?: Blob;
    };
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/leads/import/upload',
      formData: formData,
      mediaType: 'multipart/form-data',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Execute
   * @returns any OK
   * @throws ApiError
   */
  public static postApiLeadsImportExecute({
    uploadId,
    requestBody,
  }: {
    uploadId: string;
    requestBody: ImportExecuteRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/leads/import/{uploadId}/execute',
      path: {
        uploadId: uploadId,
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
   * Preview
   * @returns any OK
   * @throws ApiError
   */
  public static postApiLeadsImportPreview({
    uploadId,
    requestBody,
  }: {
    uploadId: string;
    requestBody: ImportPreviewRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/leads/import/{uploadId}/preview',
      path: {
        uploadId: uploadId,
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
