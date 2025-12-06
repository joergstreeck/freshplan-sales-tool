/**
 * GDPR Components - Sprint 2.1.8
 *
 * Export aller DSGVO-Komponenten für Leads
 */

export { ContactBlockedBadge } from './ContactBlockedBadge';
export { GdprActionsMenu } from './GdprActionsMenu';
export { GdprDeleteDialog } from './GdprDeleteDialog';
export { GdprDeletedBadge } from './GdprDeletedBadge';

export {
  useGdprDataExport,
  useGdprDataRequests,
  useGdprDelete,
  useGdprDeletionLogs,
  useGdprRevokeConsent,
  useGdprContactAllowed,
} from './useGdprApi';

export type {
  GdprDataRequestDTO,
  GdprDeletionLogDTO,
  GdprDeleteRequest,
  ContactAllowedResponse,
  GdprLeadFields,
  GdprDeletionReasonType,
} from './types';

export { GDPR_DELETION_REASONS } from './types';
