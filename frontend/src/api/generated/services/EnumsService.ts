/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { EnumValue } from '../models/EnumValue';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class EnumsService {
  /**
   * Get all Activity Outcome enum values
   * @returns EnumValue List of Activity Outcome values
   * @throws ApiError
   */
  public static getApiEnumsActivityOutcomes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/activity-outcomes',
    });
  }
  /**
   * Get user-selectable Activity Type enum values
   * @returns EnumValue List of user-selectable Activity Type values (10 types)
   * @throws ApiError
   */
  public static getApiEnumsActivityTypes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/activity-types',
    });
  }
  /**
   * Get all Budget Availability enum values
   * @returns EnumValue List of Budget Availability values
   * @throws ApiError
   */
  public static getApiEnumsBudgetAvailability(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/budget-availability',
    });
  }
  /**
   * Get all Business Type enum values
   * @returns EnumValue List of Business Type values
   * @throws ApiError
   */
  public static getApiEnumsBusinessTypes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/business-types',
    });
  }
  /**
   * Get all Contact Role enum values
   * @returns EnumValue List of Contact Role values
   * @throws ApiError
   */
  public static getApiEnumsContactRoles(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/contact-roles',
    });
  }
  /**
   * Get all Country Code enum values
   * @returns EnumValue List of Country Code values
   * @throws ApiError
   */
  public static getApiEnumsCountryCodes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/country-codes',
    });
  }
  /**
   * Get all Customer Status enum values
   * @returns EnumValue List of Customer Status values
   * @throws ApiError
   */
  public static getApiEnumsCustomerStatus(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/customer-status',
    });
  }
  /**
   * Get all Customer Type enum values
   * @returns EnumValue List of Customer Type values
   * @throws ApiError
   */
  public static getApiEnumsCustomerTypes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/customer-types',
    });
  }
  /**
   * Get all Deal Size enum values
   * @returns EnumValue List of Deal Size values
   * @throws ApiError
   */
  public static getApiEnumsDealSizes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/deal-sizes',
    });
  }
  /**
   * Get all Decision Level enum values
   * @returns EnumValue List of Decision Level values
   * @throws ApiError
   */
  public static getApiEnumsDecisionLevels(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/decision-levels',
    });
  }
  /**
   * Get all Decision Maker Access enum values
   * @returns EnumValue List of Decision Maker Access values
   * @throws ApiError
   */
  public static getApiEnumsDecisionMakerAccess(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/decision-maker-access',
    });
  }
  /**
   * Get all Delivery Condition enum values
   * @returns EnumValue List of Delivery Condition values
   * @throws ApiError
   */
  public static getApiEnumsDeliveryConditions(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/delivery-conditions',
    });
  }
  /**
   * Get all Expansion Plan enum values
   * @returns EnumValue List of Expansion Plan values
   * @throws ApiError
   */
  public static getApiEnumsExpansionPlan(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/expansion-plan',
    });
  }
  /**
   * Get all Financing Type enum values
   * @returns EnumValue List of Financing Type values
   * @throws ApiError
   */
  public static getApiEnumsFinancingTypes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/financing-types',
    });
  }
  /**
   * Get all Kitchen Size enum values
   * @returns EnumValue List of Kitchen Size values
   * @throws ApiError
   */
  public static getApiEnumsKitchenSizes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/kitchen-sizes',
    });
  }
  /**
   * Get all Lead Source enum values
   * @returns EnumValue List of Lead Source values
   * @throws ApiError
   */
  public static getApiEnumsLeadSources(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/lead-sources',
    });
  }
  /**
   * Get all Legal Form enum values
   * @returns EnumValue List of Legal Form values
   * @throws ApiError
   */
  public static getApiEnumsLegalForms(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/legal-forms',
    });
  }
  /**
   * Get all Opportunity Type enum values
   * @returns EnumValue List of Opportunity Type values
   * @throws ApiError
   */
  public static getApiEnumsOpportunityTypes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/opportunity-types',
    });
  }
  /**
   * Get all Payment Terms enum values
   * @returns EnumValue List of Payment Terms values
   * @throws ApiError
   */
  public static getApiEnumsPaymentTerms(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/payment-terms',
    });
  }
  /**
   * Get all Relationship Status enum values
   * @returns EnumValue List of Relationship Status values
   * @throws ApiError
   */
  public static getApiEnumsRelationshipStatus(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/relationship-status',
    });
  }
  /**
   * Get all Responsibility Scope enum values
   * @returns EnumValue List of Responsibility Scope values
   * @throws ApiError
   */
  public static getApiEnumsResponsibilityScopes(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/responsibility-scopes',
    });
  }
  /**
   * Get all Salutation enum values
   * @returns EnumValue List of Salutation values
   * @throws ApiError
   */
  public static getApiEnumsSalutations(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/salutations',
    });
  }
  /**
   * Get all Title enum values
   * @returns EnumValue List of Title values
   * @throws ApiError
   */
  public static getApiEnumsTitles(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/titles',
    });
  }
  /**
   * Get all Urgency Level enum values
   * @returns EnumValue List of Urgency Level values
   * @throws ApiError
   */
  public static getApiEnumsUrgencyLevels(): CancelablePromise<EnumValue> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/enums/urgency-levels',
    });
  }
}
