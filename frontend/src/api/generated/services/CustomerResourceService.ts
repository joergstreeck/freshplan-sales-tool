/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ActivateCustomerRequest } from '../models/ActivateCustomerRequest';
import type { AddChildCustomerRequest } from '../models/AddChildCustomerRequest';
import type { ChangeStatusRequest } from '../models/ChangeStatusRequest';
import type { CheckDuplicatesRequest } from '../models/CheckDuplicatesRequest';
import type { ContactRequest } from '../models/ContactRequest';
import type { CreateBranchRequest } from '../models/CreateBranchRequest';
import type { CreateCustomerRequest } from '../models/CreateCustomerRequest';
import type { CustomerListResponse } from '../models/CustomerListResponse';
import type { CustomerResponse } from '../models/CustomerResponse';
import type { CustomerStatus } from '../models/CustomerStatus';
import type { HierarchyMetrics } from '../models/HierarchyMetrics';
import type { MergeCustomersRequest } from '../models/MergeCustomersRequest';
import type { UpdateCustomerRequest } from '../models/UpdateCustomerRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class CustomerResourceService {
  /**
   * Get All Customers
   * @returns CustomerListResponse Paginated list of customers
   * @throws ApiError
   */
  public static getApiCustomers({
    page,
    size = 20,
    status,
  }: {
    page?: number;
    size?: number;
    status?: CustomerStatus;
  }): CancelablePromise<CustomerListResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers',
      query: {
        page: page,
        size: size,
        status: status,
      },
      errors: {
        401: `Unauthorized - authentication required`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Create Customer
   * @returns CustomerResponse Customer created successfully
   * @throws ApiError
   */
  public static postApiCustomers({
    requestBody,
  }: {
    requestBody: CreateCustomerRequest;
  }): CancelablePromise<CustomerResponse> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid request data`,
        401: `Unauthorized - authentication required`,
        403: `Forbidden - admin or manager role required`,
      },
    });
  }
  /**
   * Get Customers At Risk
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCustomersAnalyticsRiskAssessment({
    minRiskScore = 70,
    page,
    size = 20,
  }: {
    minRiskScore?: number;
    page?: number;
    size?: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/analytics/risk-assessment',
      query: {
        minRiskScore: minRiskScore,
        page: page,
        size: size,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Check Duplicates
   * @returns any OK
   * @throws ApiError
   */
  public static postApiCustomersCheckDuplicates({
    requestBody,
  }: {
    requestBody: CheckDuplicatesRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/check-duplicates',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Dashboard Data
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCustomersDashboard(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/dashboard',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Customer Opportunities
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCustomersOpportunities({
    customerId,
  }: {
    customerId: UUID;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{customerId}/opportunities',
      path: {
        customerId: customerId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Branches
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCustomersBranches({
    headquarterId,
  }: {
    headquarterId: UUID;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{headquarterId}/branches',
      path: {
        headquarterId: headquarterId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Create Branch
   * @returns CustomerResponse Branch created successfully
   * @throws ApiError
   */
  public static postApiCustomersBranches({
    headquarterId,
    requestBody,
  }: {
    headquarterId: UUID;
    requestBody: CreateBranchRequest;
  }): CancelablePromise<CustomerResponse> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{headquarterId}/branches',
      path: {
        headquarterId: headquarterId,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid request - parent is not HEADQUARTER`,
        401: `Not Authorized`,
        403: `Forbidden - admin or manager role required`,
        404: `Parent customer not found`,
      },
    });
  }
  /**
   * Update Customer
   * @returns any OK
   * @throws ApiError
   */
  public static putApiCustomers({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: UpdateCustomerRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/customers/{id}',
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
   * Get Customer
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCustomers1({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{id}',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Delete Customer
   * @returns any OK
   * @throws ApiError
   */
  public static deleteApiCustomers({
    id,
    reason = 'No reason provided',
  }: {
    id: UUID;
    reason?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/customers/{id}',
      path: {
        id: id,
      },
      query: {
        reason: reason,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Activate Customer
   * @returns any OK
   * @throws ApiError
   */
  public static putApiCustomersActivate({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: ActivateCustomerRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/customers/{id}/activate',
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
   * Create Contact
   * @returns any OK
   * @throws ApiError
   */
  public static postApiCustomersContacts({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: ContactRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{id}/contacts',
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
   * Update Contact
   * @returns any OK
   * @throws ApiError
   */
  public static putApiCustomersContacts({
    contactId,
    id,
    requestBody,
  }: {
    contactId: UUID;
    id: UUID;
    requestBody: ContactRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/customers/{id}/contacts/{contactId}',
      path: {
        contactId: contactId,
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
   * Get Customer Hierarchy
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCustomersHierarchy({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{id}/hierarchy',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Hierarchy Metrics
   * @returns HierarchyMetrics Hierarchy metrics retrieved successfully
   * @throws ApiError
   */
  public static getApiCustomersHierarchyMetrics({
    id,
  }: {
    id: UUID;
  }): CancelablePromise<HierarchyMetrics> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{id}/hierarchy/metrics',
      path: {
        id: id,
      },
      errors: {
        400: `Customer is not a HEADQUARTER (InvalidHierarchyException)`,
        401: `Unauthorized - authentication required`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
  /**
   * Get Customer Locations
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCustomersLocations({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{id}/locations',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Restore Customer
   * @returns any OK
   * @throws ApiError
   */
  public static putApiCustomersRestore({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/customers/{id}/restore',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Revenue Metrics
   * @returns any OK
   * @throws ApiError
   */
  public static getApiCustomersRevenueMetrics({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{id}/revenue-metrics',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Change Customer Status
   * @returns any OK
   * @throws ApiError
   */
  public static putApiCustomersStatus({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: ChangeStatusRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/customers/{id}/status',
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
   * Add Child Customer
   * @returns any OK
   * @throws ApiError
   */
  public static postApiCustomersChildren({
    parentId,
    requestBody,
  }: {
    parentId: UUID;
    requestBody: AddChildCustomerRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{parentId}/children',
      path: {
        parentId: parentId,
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
   * Merge Customers
   * @returns any OK
   * @throws ApiError
   */
  public static postApiCustomersMerge({
    targetId,
    requestBody,
  }: {
    targetId: UUID;
    requestBody: MergeCustomersRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{targetId}/merge',
      path: {
        targetId: targetId,
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
