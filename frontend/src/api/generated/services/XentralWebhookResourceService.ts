/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { XentralOrderWebhookRequest } from '../models/XentralOrderWebhookRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class XentralWebhookResourceService {
  /**
   * Handle Order Delivered
   * @returns any OK
   * @throws ApiError
   */
  public static postApiWebhooksXentralOrderDelivered({
    requestBody,
  }: {
    requestBody: XentralOrderWebhookRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/webhooks/xentral/order-delivered',
      body: requestBody,
      mediaType: 'application/json',
    });
  }
}
