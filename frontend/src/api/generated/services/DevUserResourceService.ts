/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateUserRequest } from '../models/CreateUserRequest';
import type { UpdateUserRequest } from '../models/UpdateUserRequest';
import type { UpdateUserRolesRequest } from '../models/UpdateUserRolesRequest';
import type { UserResponse } from '../models/UserResponse';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class DevUserResourceService {
  /**
   * List Users
   * @returns any OK
   * @throws ApiError
   */
  public static getApiUsers({
    enabledOnly = false,
    page,
    search,
    size = 20,
  }: {
    enabledOnly?: boolean;
    page?: number;
    search?: string;
    size?: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/users',
      query: {
        enabledOnly: enabledOnly,
        page: page,
        search: search,
        size: size,
      },
    });
  }
  /**
   * Create User
   * @returns any OK
   * @throws ApiError
   */
  public static postApiUsers({
    requestBody,
  }: {
    requestBody: CreateUserRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/users',
      body: requestBody,
      mediaType: 'application/json',
    });
  }
  /**
   * Update User
   * @returns UserResponse OK
   * @throws ApiError
   */
  public static putApiUsers({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: UpdateUserRequest;
  }): CancelablePromise<UserResponse> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/users/{id}',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
    });
  }
  /**
   * Get User
   * @returns UserResponse OK
   * @throws ApiError
   */
  public static getApiUsers1({ id }: { id: UUID }): CancelablePromise<UserResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/users/{id}',
      path: {
        id: id,
      },
    });
  }
  /**
   * Delete User
   * @returns any OK
   * @throws ApiError
   */
  public static deleteApiUsers({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/users/{id}',
      path: {
        id: id,
      },
    });
  }
  /**
   * Disable User
   * @returns any OK
   * @throws ApiError
   */
  public static putApiUsersDisable({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/users/{id}/disable',
      path: {
        id: id,
      },
    });
  }
  /**
   * Enable User
   * @returns any OK
   * @throws ApiError
   */
  public static putApiUsersEnable({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/users/{id}/enable',
      path: {
        id: id,
      },
    });
  }
  /**
   * Update User Roles
   * @returns UserResponse OK
   * @throws ApiError
   */
  public static putApiUsersRoles({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: UpdateUserRolesRequest;
  }): CancelablePromise<UserResponse> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/users/{id}/roles',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
    });
  }
}
