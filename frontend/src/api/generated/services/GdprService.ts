/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContactAllowedResponse } from '../models/ContactAllowedResponse';
import type { GdprDataRequestDTO } from '../models/GdprDataRequestDTO';
import type { GdprDeleteRequest } from '../models/GdprDeleteRequest';
import type { GdprDeletionLogDTO } from '../models/GdprDeletionLogDTO';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class GdprService {
  /**
   * DSGVO-Löschung (Art. 17)
   * Führt eine DSGVO-konforme Löschung durch (Soft-Delete + PII-Anonymisierung). Die personenbezogenen Daten werden anonymisiert, der Audit-Trail bleibt erhalten.
   * @returns void
   * @throws ApiError
   */
  public static deleteApiGdprLeads({
    id,
    requestBody,
  }: {
    /**
     * Lead ID
     */
    id: number;
    requestBody: GdprDeleteRequest;
  }): CancelablePromise<void> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/gdpr/leads/{id}',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Ungültige Anfrage`,
        404: `Lead nicht gefunden`,
        409: `Löschung blockiert (z.B. offene Opportunities)`,
      },
    });
  }
  /**
   * Kontakt-Erlaubnis prüfen
   * Prüft ob ein Lead kontaktiert werden darf (nicht gesperrt, nicht gelöscht).
   * @returns ContactAllowedResponse OK
   * @throws ApiError
   */
  public static getApiGdprLeadsContactAllowed({
    id,
  }: {
    /**
     * Lead ID
     */
    id: number;
  }): CancelablePromise<ContactAllowedResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/gdpr/leads/{id}/contact-allowed',
      path: {
        id: id,
      },
    });
  }
  /**
   * DSGVO Datenexport (Art. 15)
   * Generiert einen PDF-Export mit allen personenbezogenen Daten eines Leads. Nur für Manager und Admins (Datenschutzbeauftragte) zugänglich.
   * @returns any PDF erfolgreich generiert
   * @throws ApiError
   */
  public static getApiGdprLeadsDataExport({
    id,
  }: {
    /**
     * Lead ID
     */
    id: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/gdpr/leads/{id}/data-export',
      path: {
        id: id,
      },
      errors: {
        403: `Keine Berechtigung`,
        404: `Lead nicht gefunden`,
      },
    });
  }
  /**
   * Datenexport-Anfragen auflisten
   * Listet alle Art. 15 Datenexport-Anfragen für einen Lead auf.
   * @returns GdprDataRequestDTO OK
   * @throws ApiError
   */
  public static getApiGdprLeadsDataRequests({
    id,
  }: {
    /**
     * Lead ID
     */
    id: number;
  }): CancelablePromise<Array<GdprDataRequestDTO>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/gdpr/leads/{id}/data-requests',
      path: {
        id: id,
      },
    });
  }
  /**
   * Löschprotokolle auflisten
   * Listet alle Art. 17 DSGVO-Löschprotokolle für einen Lead auf.
   * @returns GdprDeletionLogDTO OK
   * @throws ApiError
   */
  public static getApiGdprLeadsDeletionLogs({
    id,
  }: {
    /**
     * Lead ID
     */
    id: number;
  }): CancelablePromise<Array<GdprDeletionLogDTO>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/gdpr/leads/{id}/deletion-logs',
      path: {
        id: id,
      },
    });
  }
  /**
   * Einwilligung widerrufen (Art. 7.3)
   * Widerruft die Einwilligung des Leads. Nach Widerruf ist keine Kontaktaufnahme mehr erlaubt. Alle Rollen können diese Aktion ausführen (Kunde hat Recht!).
   * @returns void
   * @throws ApiError
   */
  public static postApiGdprLeadsRevokeConsent({
    id,
  }: {
    /**
     * Lead ID
     */
    id: number;
  }): CancelablePromise<void> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/gdpr/leads/{id}/revoke-consent',
      path: {
        id: id,
      },
      errors: {
        400: `Bereits widerrufen`,
        404: `Lead nicht gefunden`,
      },
    });
  }
}
