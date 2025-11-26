/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContactDTO } from '../models/ContactDTO';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ContactsService {
  /**
   * Get all contacts for a customer
   * @returns ContactDTO List of contacts
   * @throws ApiError
   */
  public static getApiCustomersContacts({
    customerId,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
  }): CancelablePromise<Array<ContactDTO>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{customerId}/contacts',
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
   * Create a new contact for a customer
   * @returns any Contact created successfully
   * @throws ApiError
   */
  public static postApiCustomersContacts({
    customerId,
    requestBody,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
    requestBody: ContactDTO;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{customerId}/contacts',
      path: {
        customerId: customerId,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid contact data`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
  /**
   * Get contacts with upcoming birthdays
   * @returns ContactDTO List of contacts with birthdays
   * @throws ApiError
   */
  public static getApiCustomersContactsBirthdays({
    days = 7,
  }: {
    /**
     * Days ahead to check
     */
    days?: number;
  }): CancelablePromise<Array<ContactDTO>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{customerId}/contacts/birthdays',
      query: {
        days: days,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Check if email is already in use
   * @returns any Email availability check result
   * @throws ApiError
   */
  public static postApiCustomersContactsCheckEmail({
    email,
    excludeId,
  }: {
    /**
     * Email to check
     */
    email: string;
    /**
     * Contact ID to exclude from check
     */
    excludeId?: UUID;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{customerId}/contacts/check-email',
      query: {
        email: email,
        excludeId: excludeId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update a contact
   * @returns ContactDTO Contact updated successfully
   * @throws ApiError
   */
  public static putApiCustomersContacts({
    contactId,
    customerId,
    requestBody,
  }: {
    /**
     * Contact ID
     */
    contactId: UUID;
    /**
     * Customer ID
     */
    customerId: UUID;
    requestBody: ContactDTO;
  }): CancelablePromise<ContactDTO> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/customers/{customerId}/contacts/{contactId}',
      path: {
        contactId: contactId,
        customerId: customerId,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid contact data`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Contact not found`,
      },
    });
  }
  /**
   * Get a specific contact
   * @returns ContactDTO Contact found
   * @throws ApiError
   */
  public static getApiCustomersContacts1({
    contactId,
    customerId,
  }: {
    /**
     * Contact ID
     */
    contactId: UUID;
    /**
     * Customer ID
     */
    customerId: UUID;
  }): CancelablePromise<ContactDTO> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{customerId}/contacts/{contactId}',
      path: {
        contactId: contactId,
        customerId: customerId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Contact not found`,
      },
    });
  }
  /**
   * Delete a contact (soft delete)
   * @returns void
   * @throws ApiError
   */
  public static deleteApiCustomersContacts({
    contactId,
    customerId,
  }: {
    /**
     * Contact ID
     */
    contactId: UUID;
    /**
     * Customer ID
     */
    customerId: UUID;
  }): CancelablePromise<void> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/customers/{customerId}/contacts/{contactId}',
      path: {
        contactId: contactId,
        customerId: customerId,
      },
      errors: {
        400: `Cannot delete primary contact if others exist`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Contact not found`,
      },
    });
  }
  /**
   * Set a contact as primary
   * @returns void
   * @throws ApiError
   */
  public static putApiCustomersContactsSetPrimary({
    contactId,
    customerId,
  }: {
    /**
     * Contact ID
     */
    contactId: UUID;
    /**
     * Customer ID
     */
    customerId: UUID;
  }): CancelablePromise<void> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/customers/{customerId}/contacts/{contactId}/set-primary',
      path: {
        contactId: contactId,
        customerId: customerId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Contact not found`,
      },
    });
  }
  /**
   * Get all contacts assigned to a location
   * @returns ContactDTO List of contacts
   * @throws ApiError
   */
  public static getApiLocationsContacts({
    locationId,
  }: {
    /**
     * Location ID
     */
    locationId: UUID;
  }): CancelablePromise<Array<ContactDTO>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/locations/{locationId}/contacts',
      path: {
        locationId: locationId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
