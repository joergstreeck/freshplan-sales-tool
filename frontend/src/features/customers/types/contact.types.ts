/**
 * Contact Type Definitions
 *
 * Types for managing customer contacts and their relationships.
 * Supports Sprint 2 Step 3 with multi-contact management and relationship intelligence.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/BACKEND_CONTACT.md
 */

export interface Contact {
  id: string;
  customerId: string;

  // Basic Info
  salutation?: 'Herr' | 'Frau' | 'Divers';
  title?: string;
  firstName: string;
  lastName: string;
  position?: string;
  decisionLevel?: 'entscheider' | 'mitentscheider' | 'einflussnehmer' | 'nutzer' | 'gatekeeper';

  // Contact Info
  email?: string;
  phone?: string;
  mobile?: string;

  // Flags
  isPrimary: boolean;
  isActive: boolean;

  // Soft Delete fields
  deletedAt?: string;
  deletedBy?: string;
  deletionReason?: string;

  // Responsibility
  responsibilityScope: 'all' | 'specific';
  assignedLocationIds?: string[];

  // Relationship Data (Beziehungsebene)
  birthday?: string; // ISO date string
  hobbies?: string[];
  familyStatus?: string;
  childrenCount?: number;
  personalNotes?: string;

  // Roles (from sprint2/step3 planning)
  roles?: string[];

  // Audit fields
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

// Legacy support
export type ContactWithResponsibility = Contact; // This type is now merged into Contact

/**
 * DTO for creating a new contact
 */
export interface CreateContactDTO {
  // Basic Info
  salutation?: 'Herr' | 'Frau' | 'Divers';
  title?: string;
  firstName: string;
  lastName: string;
  position?: string;
  decisionLevel?: 'entscheider' | 'mitentscheider' | 'einflussnehmer' | 'nutzer' | 'gatekeeper';

  // Contact Info
  email?: string;
  phone?: string;
  mobile?: string;

  // Flags
  isPrimary?: boolean;

  // Responsibility
  responsibilityScope?: 'all' | 'specific';
  assignedLocationIds?: string[];

  // Relationship Data
  birthday?: string;
  hobbies?: string[];
  familyStatus?: string;
  childrenCount?: number;
  personalNotes?: string;

  // Roles
  roles?: string[];
}

/**
 * DTO for updating a contact
 */
export type UpdateContactDTO = Partial<CreateContactDTO>; // Allow partial updates

/**
 * Contact validation errors
 */
export interface ContactValidationError {
  contactId: string;
  fieldErrors: Record<string, string>;
}

/**
 * Decision levels for contacts
 */
export const DECISION_LEVELS = [
  { value: 'entscheider', label: 'Entscheider' },
  { value: 'mitentscheider', label: 'Mitentscheider' },
  { value: 'einflussnehmer', label: 'Einflussnehmer' },
  { value: 'nutzer', label: 'Nutzer' },
  { value: 'gatekeeper', label: 'Gatekeeper' },
] as const;

/**
 * Salutation options
 */
export const SALUTATIONS = [
  { value: 'Herr', label: 'Herr' },
  { value: 'Frau', label: 'Frau' },
  { value: 'Divers', label: 'Divers' },
] as const;

/**
 * Common academic titles
 */
export const ACADEMIC_TITLES = [
  { value: '', label: 'Kein Titel' },
  { value: 'Dr.', label: 'Dr.' },
  { value: 'Prof.', label: 'Prof.' },
  { value: 'Prof. Dr.', label: 'Prof. Dr.' },
  { value: 'Dipl.-Ing.', label: 'Dipl.-Ing.' },
  { value: 'Dipl.-Kfm.', label: 'Dipl.-Kfm.' },
  { value: 'MBA', label: 'MBA' },
] as const;

/**
 * Family status options
 */
export const FAMILY_STATUS_OPTIONS = [
  { value: 'ledig', label: 'Ledig' },
  { value: 'verheiratet', label: 'Verheiratet' },
  { value: 'geschieden', label: 'Geschieden' },
  { value: 'verwitwet', label: 'Verwitwet' },
  { value: 'lebenspartnerschaft', label: 'Eingetragene Lebenspartnerschaft' },
] as const;

/**
 * Common hobbies for relationship building
 */
export const COMMON_HOBBIES = [
  'Golf',
  'Tennis',
  'Fußball',
  'Kochen',
  'Wein',
  'Reisen',
  'Lesen',
  'Theater',
  'Musik',
  'Kunst',
  'Fotografie',
  'Wandern',
  'Radfahren',
  'Segeln',
  'Ski',
  'Fitness',
] as const;

/**
 * Contact roles in customer organization
 */
export const CONTACT_ROLES = [
  { value: 'geschaeftsfuehrung', label: 'Geschäftsführung' },
  { value: 'einkauf', label: 'Einkauf' },
  { value: 'kueche', label: 'Küche' },
  { value: 'verwaltung', label: 'Verwaltung' },
  { value: 'qualitaet', label: 'Qualitätsmanagement' },
  { value: 'buchhaltung', label: 'Buchhaltung' },
  { value: 'marketing', label: 'Marketing' },
  { value: 'personal', label: 'Personal' },
  { value: 'technik', label: 'Technik' },
  { value: 'sonstiges', label: 'Sonstiges' },
] as const;

/**
 * Type guards
 */
export const isContact = (obj: unknown): obj is Contact => {
  return (
    typeof obj === 'object' &&
    obj !== null &&
    'id' in obj &&
    'customerId' in obj &&
    'firstName' in obj &&
    'lastName' in obj &&
    'isPrimary' in obj &&
    'isActive' in obj &&
    'responsibilityScope' in obj
  );
};

/**
 * Helper to get full name
 */
export const getContactFullName = (contact: Contact): string => {
  const parts = [];
  if (contact.salutation) parts.push(contact.salutation);
  if (contact.title) parts.push(contact.title);
  parts.push(contact.firstName);
  parts.push(contact.lastName);
  return parts.join(' ');
};

/**
 * Helper to get display name for lists
 */
export const getContactDisplayName = (contact: Contact): string => {
  return `${contact.firstName} ${contact.lastName}`;
};

/**
 * Helper to format contact for primary badge
 */
export const getContactBadgeInfo = (contact: Contact): string => {
  const parts = [];
  if (contact.isPrimary) parts.push('Hauptansprechpartner');
  if (contact.position) parts.push(contact.position);
  return parts.join(' • ');
};

export type DecisionLevel = Contact['decisionLevel'];
export type Salutation = Contact['salutation'];
