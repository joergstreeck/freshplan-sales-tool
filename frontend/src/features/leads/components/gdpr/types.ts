/**
 * DSGVO Types - Sprint 2.1.8
 *
 * Types für DSGVO-Compliance Features (Art. 15, 17, 7.3)
 */

export interface GdprDataRequestDTO {
  id: number;
  entityType: 'LEAD' | 'CUSTOMER';
  entityId: number;
  requestedBy: string;
  requestedAt: string;
  pdfGenerated: boolean;
  pdfGeneratedAt?: string;
}

export interface GdprDeletionLogDTO {
  id: number;
  entityType: 'LEAD' | 'CUSTOMER';
  entityId: number;
  deletedBy: string;
  deletedAt: string;
  deletionReason: string;
  originalDataHash?: string;
}

export interface GdprDeleteRequest {
  reason: string;
}

export interface ContactAllowedResponse {
  leadId: number;
  contactAllowed: boolean;
}

export interface GdprLeadFields {
  consentRevokedAt?: string;
  consentRevokedBy?: string;
  contactBlocked?: boolean;
  gdprDeleted?: boolean;
  gdprDeletedAt?: string;
  gdprDeletedBy?: string;
  gdprDeletionReason?: string;
}

/**
 * Mögliche DSGVO-Löschgründe
 */
export const GDPR_DELETION_REASONS = [
  {
    value: 'ART_17_REQUEST',
    label: 'Betroffener hat Löschung beantragt (Art. 17)',
  },
  {
    value: 'DATA_NOT_REQUIRED',
    label: 'Daten nicht mehr erforderlich (Art. 17 Abs. 1a)',
  },
  {
    value: 'CONSENT_WITHDRAWN',
    label: 'Einwilligung widerrufen (Art. 17 Abs. 1b)',
  },
  {
    value: 'OTHER',
    label: 'Anderer Grund',
  },
] as const;

export type GdprDeletionReasonType = (typeof GDPR_DELETION_REASONS)[number]['value'];
