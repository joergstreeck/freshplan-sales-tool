/**
 * Field Catalog Extensions for Contact Management (Step 3)
 * 
 * Extends the field catalog with contact-specific fields following
 * the Theme Architecture requirements.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/THEME_ARCHITECTURE.md
 */

import type { FieldDefinition } from '../types/field.types';
import { 
  SALUTATIONS, 
  ACADEMIC_TITLES, 
  DECISION_LEVELS,
  FAMILY_STATUS_OPTIONS,
  CONTACT_ROLES
} from '../types/contact.types';

/**
 * Contact field extensions for dynamic field renderer
 */
export const contactFieldExtensions: Record<string, FieldDefinition> = {
  // ===== Basic Info Fields =====
  contactSalutation: {
    key: 'contactSalutation',
    label: 'Anrede',
    entityType: 'customer',
    fieldType: 'select',
    required: true,
    category: 'contact',
    options: SALUTATIONS.map(s => ({ value: s.value, label: s.label })),
    sizeHint: 'klein',
    helpText: 'Wählen Sie die passende Anrede'
  },
  
  contactTitle: {
    key: 'contactTitle',
    label: 'Titel',
    entityType: 'customer',
    fieldType: 'select',
    required: false,
    category: 'contact',
    options: ACADEMIC_TITLES.map(t => ({ value: t.value, label: t.label })),
    sizeHint: 'klein',
    placeholder: 'Akademischer Titel',
    allowCustomValue: true // Allow entering custom titles
  },
  
  contactFirstName: {
    key: 'contactFirstName',
    label: 'Vorname',
    entityType: 'customer',
    fieldType: 'text',
    required: true,
    category: 'contact',
    sizeHint: 'mittel',
    placeholder: 'z.B. Max',
    maxLength: 100
  },
  
  contactLastName: {
    key: 'contactLastName',
    label: 'Nachname',
    entityType: 'customer',
    fieldType: 'text',
    required: true,
    category: 'contact',
    sizeHint: 'mittel',
    placeholder: 'z.B. Mustermann',
    maxLength: 100
  },
  
  // ===== Professional Info =====
  contactPosition: {
    key: 'contactPosition',
    label: 'Position/Funktion',
    entityType: 'customer',
    fieldType: 'select',
    required: false,
    category: 'contact',
    sizeHint: 'groß',
    options: [
      { value: 'geschaeftsfuehrer', label: 'Geschäftsführer' },
      { value: 'direktor', label: 'Direktor' },
      { value: 'inhaber', label: 'Inhaber' },
      { value: 'vorstand', label: 'Vorstand' },
      { value: 'hoteldirektor', label: 'Hoteldirektor' },
      { value: 'fb_manager', label: 'F&B Manager' },
      { value: 'kuechenchef', label: 'Küchenchef' },
      { value: 'einkaufsleiter', label: 'Einkaufsleiter' },
      { value: 'betriebsleiter', label: 'Betriebsleiter' },
      { value: 'verwaltungsdirektor', label: 'Verwaltungsdirektor' },
      { value: 'kuechenleitung', label: 'Küchenleitung' },
      { value: 'verpflegungsmanager', label: 'Verpflegungsmanager' },
      { value: 'kantinenchef', label: 'Kantinenchef' },
      { value: 'gastronomiemanager', label: 'Gastronomiemanager' },
      { value: 'einkaeufer', label: 'Einkäufer' },
      { value: 'prokurist', label: 'Prokurist' },
      { value: 'assistent_gf', label: 'Assistent der Geschäftsführung' },
      { value: 'other', label: 'Andere Position' }
    ],
    allowCustomValue: true,
    helpText: 'Position im Unternehmen'
  },
  
  contactDecisionLevel: {
    key: 'contactDecisionLevel',
    label: 'Entscheidungsebene',
    entityType: 'customer',
    fieldType: 'select',
    required: false,
    category: 'contact',
    sizeHint: 'mittel',
    options: DECISION_LEVELS.map(d => ({ value: d.value, label: d.label })),
    helpText: 'Rolle im Entscheidungsprozess'
  },
  
  // ===== Contact Details =====
  contactEmail: {
    key: 'contactEmail',
    label: 'E-Mail',
    entityType: 'customer',
    fieldType: 'email',
    required: false,
    category: 'contact',
    sizeHint: 'groß',
    placeholder: 'name@firma.de',
    validation: {
      pattern: '^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$',
      message: 'Bitte geben Sie eine gültige E-Mail-Adresse ein'
    }
  },
  
  contactPhone: {
    key: 'contactPhone',
    label: 'Telefon (Festnetz)',
    entityType: 'customer',
    fieldType: 'tel',
    required: false,
    category: 'contact',
    sizeHint: 'mittel',
    placeholder: 'z.B. 030 12345678',
    helpText: 'Festnetznummer mit Vorwahl'
  },
  
  contactMobile: {
    key: 'contactMobile',
    label: 'Mobilnummer',
    entityType: 'customer',
    fieldType: 'tel',
    required: false,
    category: 'contact',
    sizeHint: 'mittel',
    placeholder: 'z.B. 0171 1234567',
    helpText: 'Mobilnummer für direkte Erreichbarkeit'
  },
  
  // ===== Responsibility Fields =====
  contactResponsibilityScope: {
    key: 'contactResponsibilityScope',
    label: 'Zuständigkeitsbereich',
    entityType: 'customer',
    fieldType: 'radio',
    required: true,
    category: 'contact',
    sizeHint: 'groß',
    options: [
      { value: 'all', label: 'Alle Standorte' },
      { value: 'specific', label: 'Bestimmte Standorte' }
    ],
    defaultValue: 'all',
    helpText: 'Ist der Kontakt für alle oder nur bestimmte Standorte zuständig?'
  },
  
  contactIsPrimary: {
    key: 'contactIsPrimary',
    label: 'Hauptansprechpartner',
    entityType: 'customer',
    fieldType: 'checkbox',
    required: false,
    category: 'contact',
    sizeHint: 'klein',
    helpText: 'Nur ein Kontakt kann Hauptansprechpartner sein'
  },
  
  // ===== Relationship Data (Beziehungsebene) =====
  contactBirthday: {
    key: 'contactBirthday',
    label: 'Geburtstag',
    entityType: 'customer',
    fieldType: 'date',
    required: false,
    category: 'relationship',
    sizeHint: 'klein',
    helpText: 'Für persönliche Glückwünsche',
    validation: {
      maxDate: 'today'
    }
  },
  
  contactFamilyStatus: {
    key: 'contactFamilyStatus',
    label: 'Familienstand',
    entityType: 'customer',
    fieldType: 'select',
    required: false,
    category: 'relationship',
    sizeHint: 'mittel',
    options: FAMILY_STATUS_OPTIONS.map(f => ({ value: f.value, label: f.label }))
  },
  
  contactChildrenCount: {
    key: 'contactChildrenCount',
    label: 'Anzahl Kinder',
    entityType: 'customer',
    fieldType: 'number',
    required: false,
    category: 'relationship',
    sizeHint: 'klein',
    min: 0,
    max: 20,
    placeholder: '0'
  },
  
  contactHobbies: {
    key: 'contactHobbies',
    label: 'Hobbys & Interessen',
    entityType: 'customer',
    fieldType: 'multiselect',
    required: false,
    category: 'relationship',
    sizeHint: 'groß',
    options: [
      { value: 'golf', label: 'Golf' },
      { value: 'tennis', label: 'Tennis' },
      { value: 'fussball', label: 'Fußball' },
      { value: 'kochen', label: 'Kochen' },
      { value: 'wein', label: 'Wein' },
      { value: 'reisen', label: 'Reisen' },
      { value: 'lesen', label: 'Lesen' },
      { value: 'theater', label: 'Theater' },
      { value: 'musik', label: 'Musik' },
      { value: 'kunst', label: 'Kunst' },
      { value: 'fotografie', label: 'Fotografie' },
      { value: 'wandern', label: 'Wandern' },
      { value: 'radfahren', label: 'Radfahren' },
      { value: 'segeln', label: 'Segeln' },
      { value: 'ski', label: 'Ski' },
      { value: 'fitness', label: 'Fitness' }
    ],
    allowCustomValue: true,
    helpText: 'Gemeinsame Interessen fördern die Beziehung'
  },
  
  contactPersonalNotes: {
    key: 'contactPersonalNotes',
    label: 'Persönliche Notizen',
    entityType: 'customer',
    fieldType: 'textarea',
    required: false,
    category: 'relationship',
    sizeHint: 'groß',
    rows: 3,
    maxLength: 500,
    placeholder: 'z.B. Lieblingsrestaurant, besondere Vorlieben, Gesprächsthemen...',
    helpText: 'Vertrauliche Notizen zur Beziehungspflege'
  },
  
  // ===== Roles =====
  contactRoles: {
    key: 'contactRoles',
    label: 'Rollen im Unternehmen',
    entityType: 'customer',
    fieldType: 'multiselect',
    required: false,
    category: 'contact',
    sizeHint: 'groß',
    options: CONTACT_ROLES.map(r => ({ value: r.value, label: r.label })),
    helpText: 'Mehrfachauswahl möglich'
  }
};

/**
 * Get all contact field keys
 */
export const getContactFieldKeys = (): string[] => {
  return Object.keys(contactFieldExtensions);
};

/**
 * Get required contact fields
 */
export const getRequiredContactFields = (): FieldDefinition[] => {
  return Object.values(contactFieldExtensions).filter(field => field.required);
};

/**
 * Get contact fields by category
 */
export const getContactFieldsByCategory = (category: 'contact' | 'relationship'): FieldDefinition[] => {
  return Object.values(contactFieldExtensions).filter(field => field.category === category);
};

/**
 * Field groups for organized display
 */
export const contactFieldGroups = {
  basicInfo: [
    'contactSalutation',
    'contactTitle', 
    'contactFirstName',
    'contactLastName'
  ],
  professionalInfo: [
    'contactPosition',
    'contactDecisionLevel',
    'contactRoles'
  ],
  contactDetails: [
    'contactEmail',
    'contactPhone',
    'contactMobile'
  ],
  responsibility: [
    'contactResponsibilityScope',
    'contactIsPrimary'
  ],
  relationshipData: [
    'contactBirthday',
    'contactFamilyStatus',
    'contactChildrenCount',
    'contactHobbies',
    'contactPersonalNotes'
  ]
};

/**
 * Get fields for a specific group
 */
export const getContactFieldsForGroup = (group: keyof typeof contactFieldGroups): FieldDefinition[] => {
  const fieldKeys = contactFieldGroups[group];
  return fieldKeys.map(key => contactFieldExtensions[key]).filter(Boolean);
};