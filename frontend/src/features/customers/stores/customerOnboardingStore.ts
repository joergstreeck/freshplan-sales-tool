/**
 * Customer Onboarding Store
 *
 * Zustand store for managing the customer onboarding wizard state.
 * Handles customer data, locations, validation, and draft persistence.
 *
 * Integration mit Dynamic Zod Schema Builder für Enterprise-Validation.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/02-state-management.md
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/validation/schemaBuilder.ts
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import { CustomerStatus } from '../types/customer.types';
import type { FieldDefinition } from '../types/field.types';
import type { Location, DetailedLocation } from '../types/location.types';
import type { Contact, ContactValidationError, CreateContactDTO } from '../types/contact.types';
import { buildFieldSchema, buildFormSchema, validateField, validateFields } from '../validation';
import { getVisibleFields } from '../utils/conditionEvaluator';
import type { LocationServiceData } from './customerOnboardingStore.extensions';

interface CustomerOnboardingState {
  // ===== Wizard State =====
  /** Current wizard step (0-based) */
  currentStep: number;
  /** Has unsaved changes */
  isDirty: boolean;
  /** Last auto-save timestamp */
  lastSaved: Date | null;
  /** Current draft ID */
  draftId: string | null;
  /** Loading state */
  isLoading: boolean;
  /** Saving state */
  isSaving: boolean;

  // ===== Data State =====
  /** Customer field values */
  customerData: Record<string, any>;
  /** Customer locations (for chain customers) */
  locations: Location[];
  /** Location field values by location ID */
  locationFieldValues: Record<string, Record<string, any>>;
  /** Detailed locations (sub-locations within locations) */
  detailedLocations: DetailedLocation[];
  /** Validation errors by field key */
  validationErrors: Record<string, string>;

  // ===== Contact State (Step 3) =====
  /** Customer contacts */
  contacts: Contact[];
  /** Primary contact ID */
  primaryContactId?: string;
  /** Contact validation errors by contact ID */
  contactValidationErrors: Record<string, ContactValidationError>;

  // ===== Field Definitions =====
  /** Customer field definitions */
  customerFields: FieldDefinition[];
  /** Location field definitions */
  locationFields: FieldDefinition[];

  // ===== Step 2 Extensions =====
  /** Selected location for service data entry */
  selectedLocationId: string | 'all';
  /** Apply service data to all locations */
  applyToAllLocations: boolean;
  /** Service data per location */
  locationServices: Record<string, LocationServiceData>;
  /** Completed location IDs */
  completedLocationIds: string[];

  // ===== Actions =====
  /** Set a customer field value */
  setCustomerField: (fieldKey: string, value: any) => void;
  /** Set location field value */
  setLocationField: (locationId: string, fieldKey: string, value: any) => void;
  /** Set current wizard step */
  setCurrentStep: (step: number) => void;
  /** Add a new location */
  addLocation: () => void;
  /** Remove a location */
  removeLocation: (locationId: string) => void;
  /** Update location */
  updateLocation: (locationId: string, updates: Partial<Location>) => void;
  /** Add detailed location */
  addDetailedLocation: (
    locationId: string,
    detailedLocation: Omit<DetailedLocation, 'id' | 'locationId' | 'createdAt' | 'updatedAt'>
  ) => void;
  /** Update detailed location */
  updateDetailedLocation: (detailedLocationId: string, updates: Partial<DetailedLocation>) => void;
  /** Remove detailed location */
  removeDetailedLocation: (detailedLocationId: string) => void;
  /** Add batch detailed locations */
  addBatchDetailedLocations: (
    locationId: string,
    detailedLocations: Omit<DetailedLocation, 'id' | 'locationId' | 'createdAt' | 'updatedAt'>[]
  ) => void;
  /** Validate a field with Dynamic Zod Schema Builder */
  validateField: (fieldKey: string) => Promise<void>;
  /** Validate current step with conditional field visibility */
  validateCurrentStep: () => Promise<boolean>;
  /** Can progress to next step */
  canProgressToNextStep: () => boolean;
  /** Save as draft */
  saveAsDraft: () => Promise<void>;
  /** Load draft */
  loadDraft: (draftId: string) => Promise<void>;
  /** Finalize customer */
  finalizeCustomer: () => Promise<void>;
  /** Reset store */
  reset: () => void;
  /** Set field definitions */
  setFieldDefinitions: (
    customerFields: FieldDefinition[],
    locationFields: FieldDefinition[]
  ) => void;

  // ===== Contact Actions (Step 3) =====
  /** Add a new contact */
  addContact: (contact?: Partial<CreateContactDTO>) => void;
  /** Update existing contact */
  updateContact: (id: string, updates: Partial<Contact>) => void;
  /** Remove contact */
  removeContact: (id: string) => void;
  /** Set primary contact */
  setPrimaryContact: (id: string) => void;
  /** Validate contact field */
  validateContactField: (contactId: string, fieldKey: string, value: any) => void;
  /** Validate all contacts */
  validateContacts: () => Promise<boolean>;
  /** Get contact by ID */
  getContact: (id: string) => Contact | undefined;

  // ===== Step 2 Extension Actions =====
  /** Set expected annual revenue */
  setExpectedRevenue: (amount: number) => void;
  /** Set selected location */
  setSelectedLocation: (locationId: string | 'all') => void;
  /** Set apply to all locations */
  setApplyToAll: (value: boolean) => void;
  /** Save location services */
  saveLocationServices: (data: LocationServiceData) => void;
  /** Get location services */
  getLocationServices: (locationId: string) => LocationServiceData;
  /** Mark location as completed */
  markLocationCompleted: (locationId: string) => void;
}

const STORAGE_KEY = 'customer-onboarding-draft';

/**
 * Customer Onboarding Store
 *
 * Central state management for the customer onboarding wizard.
 * Persists draft data to localStorage for recovery.
 */
export const useCustomerOnboardingStore = create<CustomerOnboardingState>()(
  persist(
    immer((set, get) => ({
      // ===== Initial State =====
      currentStep: 0,
      isDirty: false,
      lastSaved: null,
      draftId: null,
      isLoading: false,
      isSaving: false,
      customerData: {},
      locations: [],
      locationFieldValues: {},
      detailedLocations: [],
      validationErrors: {},
      contacts: [],
      primaryContactId: undefined,
      contactValidationErrors: {},
      customerFields: [],
      locationFields: [],
      selectedLocationId: 'all',
      applyToAllLocations: false,
      locationServices: {},
      completedLocationIds: [],

      // ===== Actions =====
      setCustomerField: (fieldKey, value) => {
        set(state => {
          state.customerData[fieldKey] = value;
          state.isDirty = true;
          // Clear validation error when field is updated
          delete state.validationErrors[fieldKey];
        });
      },

      setLocationField: (locationId, fieldKey, value) => {
        set(state => {
          if (!state.locationFieldValues[locationId]) {
            state.locationFieldValues[locationId] = {};
          }
          state.locationFieldValues[locationId][fieldKey] = value;
          state.isDirty = true;
        });
      },

      setCurrentStep: step => {
        set(state => {
          state.currentStep = step;
        });
      },

      addLocation: () => {
        set(state => {
          const newLocation: Location = {
            id: `temp-${Date.now()}`,
            customerId: state.draftId || '',
            locationType: 'LIEFERADRESSE',
            sortOrder: state.locations.length,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          };
          state.locations.push(newLocation);
          state.locationFieldValues[newLocation.id] = {};
          state.isDirty = true;
        });
      },

      removeLocation: locationId => {
        set(state => {
          state.locations = state.locations.filter(loc => loc.id !== locationId);
          delete state.locationFieldValues[locationId];
          state.isDirty = true;
        });
      },

      updateLocation: (locationId, updates) => {
        set(state => {
          const locationIndex = state.locations.findIndex(loc => loc.id === locationId);
          if (locationIndex !== -1) {
            state.locations[locationIndex] = {
              ...state.locations[locationIndex],
              ...updates,
              updatedAt: new Date().toISOString(),
            };
            state.isDirty = true;
          }
        });
      },

      addDetailedLocation: (locationId, detailedLocation) => {
        set(state => {
          const newDetailedLocation: DetailedLocation = {
            id: `dl-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
            locationId,
            name: detailedLocation.name,
            category: detailedLocation.category || 'other',
            floor: detailedLocation.floor,
            capacity: detailedLocation.capacity,
            operatingHours: detailedLocation.operatingHours,
            responsiblePerson: detailedLocation.responsiblePerson,
            internalPhone: detailedLocation.internalPhone,
            specialRequirements: detailedLocation.specialRequirements,
            sortOrder: state.detailedLocations.filter(dl => dl.locationId === locationId).length,
            street: detailedLocation.street,
            postalCode: detailedLocation.postalCode,
            city: detailedLocation.city,
            notes: detailedLocation.notes,
            isActive: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          };

          state.detailedLocations.push(newDetailedLocation);
          state.isDirty = true;
        });
      },

      updateDetailedLocation: (detailedLocationId, updates) => {
        set(state => {
          const index = state.detailedLocations.findIndex(dl => dl.id === detailedLocationId);
          if (index !== -1) {
            state.detailedLocations[index] = {
              ...state.detailedLocations[index],
              ...updates,
              updatedAt: new Date().toISOString(),
            };
            state.isDirty = true;
          }
        });
      },

      removeDetailedLocation: detailedLocationId => {
        set(state => {
          state.detailedLocations = state.detailedLocations.filter(
            dl => dl.id !== detailedLocationId
          );
          state.isDirty = true;
        });
      },

      addBatchDetailedLocations: (locationId, detailedLocations) => {
        set(state => {
          const startOrder = state.detailedLocations.filter(
            dl => dl.locationId === locationId
          ).length;

          const newDetailedLocations = detailedLocations.map(
            (dl, index) =>
              ({
                id: `dl-${Date.now()}-${index}-${Math.random().toString(36).substr(2, 9)}`,
                locationId,
                name: dl.name,
                category: dl.category || 'other',
                floor: dl.floor,
                capacity: dl.capacity,
                operatingHours: dl.operatingHours,
                responsiblePerson: dl.responsiblePerson,
                internalPhone: dl.internalPhone,
                specialRequirements: dl.specialRequirements,
                sortOrder: startOrder + index,
                street: dl.street,
                postalCode: dl.postalCode,
                city: dl.city,
                notes: dl.notes,
                isActive: true,
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString(),
              }) as DetailedLocation
          );

          state.detailedLocations.push(...newDetailedLocations);
          state.isDirty = true;
        });
      },

      validateField: async fieldKey => {
        const state = get();
        const field = state.customerFields.find(f => f.key === fieldKey);
        if (!field) return;

        const value = state.customerData[fieldKey];

        // NEW: Use Dynamic Zod Schema Builder for validation
        try {
          const result = await validateField(field, value);

          set(draft => {
            if (result.isValid) {
              // Clear error if validation passes
              delete draft.validationErrors[fieldKey];
            } else {
              // Set dynamic validation error
              draft.validationErrors[fieldKey] = result.error || `${field.label} ist ungültig`;
            }
          });
        } catch (error) {
          // Fallback for unexpected errors (respecting Enterprise Flexibility Philosophy)
          console.warn(`Validation error for field ${fieldKey}:`, error);
          set(draft => {
            draft.validationErrors[fieldKey] = `${field.label} konnte nicht validiert werden`;
          });
        }
      },

      validateCurrentStep: async () => {
        const state = get();

        // NEW: Dynamic validation with conditional field visibility
        let fieldsToValidate: FieldDefinition[] = [];

        if (state.currentStep === 0) {
          // Only validate fields that are shown in Step 1
          const step1Fields = [
            'companyName',
            'legalForm',
            'industry',
            'chainCustomer',
            'financingType',
            'street',
            'postalCode',
            'city',
          ];

          // Get visible customer fields for current step with conditional logic
          const visibleFields = getVisibleFields(
            state.customerFields,
            state.customerData,
            'customer' // currentStep parameter
          );
          fieldsToValidate = visibleFields.filter(
            field => field.required && step1Fields.includes(field.key)
          );
        } else if (state.currentStep === 1) {
          // Location fields validation if applicable
          fieldsToValidate = []; // Will be extended when location validation is needed
        } else if (state.currentStep === 2) {
          // Step 3: Contact validation
          return await get().validateContacts();
        }

        // NEW: Use Dynamic Zod Schema Builder for step validation
        try {
          const fieldsWithValues = fieldsToValidate.map(field => ({
            field,
            value: state.customerData[field.key],
          }));

          const validationErrors = await validateFields(fieldsWithValues);
          const errors = Object.fromEntries(validationErrors);

          set(draft => {
            draft.validationErrors = errors;
          });

          return validationErrors.size === 0;
        } catch (error) {
          // Fallback for unexpected errors (Enterprise Flexibility Philosophy)
          console.warn('Step validation error:', error);

          // Basic fallback validation
          const errors: Record<string, string> = {};
          fieldsToValidate.forEach(field => {
            const value = state.customerData[field.key];
            if (!value || value === '') {
              errors[field.key] = `${field.label} ist erforderlich`;
            }
          });

          set(draft => {
            draft.validationErrors = errors;
          });

          return Object.keys(errors).length === 0;
        }
      },

      canProgressToNextStep: () => {
        const state = get();

        // Step 0: Customer data must be valid
        if (state.currentStep === 0) {
          // Only validate fields that are shown in Step 1
          const step1Fields = [
            'companyName',
            'legalForm',
            'industry',
            'chainCustomer',
            'financingType',
            'street',
            'postalCode',
            'city',
          ];

          const requiredFields = state.customerFields.filter(
            f => f.required && step1Fields.includes(f.key)
          );

          console.log(
            'Step 0 - Required fields for Step 1:',
            requiredFields.map(f => ({
              key: f.key,
              label: f.label,
              value: state.customerData[f.key],
              isValid:
                state.customerData[f.key] !== undefined &&
                state.customerData[f.key] !== '' &&
                state.customerData[f.key] !== null,
            }))
          );

          return requiredFields.every(field => {
            const value = state.customerData[field.key];
            return value !== undefined && value !== '' && value !== null;
          });
        }

        // Step 1: Herausforderungen & Potenzial
        // Hier ist das expectedAnnualRevenue ein Pflichtfeld
        if (state.currentStep === 1) {
          const revenueValue = state.customerData.expectedAnnualRevenue;
          return revenueValue !== undefined && revenueValue !== null && revenueValue > 0;
        }

        // Step 2: Ansprechpartner - keine Pflichtfelder in Step 3
        if (state.currentStep === 2) {
          // Step 3 ist optional, daher immer true
          return true;
        }

        // Step 3: Angebot & Services
        if (state.currentStep === 3) {
          // Keine Pflichtfelder, aber wir könnten prüfen ob mindestens ein Service ausgewählt wurde
          return true;
        }

        return true;
      },

      saveAsDraft: async () => {
        set(state => {
          state.isSaving = true;
        });

        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 500));

        set(state => {
          state.lastSaved = new Date();
          state.isDirty = false;
          state.isSaving = false;
          if (!state.draftId) {
            state.draftId = `draft-${Date.now()}`;
          }
        });
      },

      loadDraft: async draftId => {
        set(state => {
          state.isLoading = true;
        });

        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 500));

        set(state => {
          state.draftId = draftId;
          state.isLoading = false;
        });
      },

      finalizeCustomer: async () => {
        const state = get();

        // Validate all required fields
        if (!state.validateCurrentStep()) {
          throw new Error('Validation failed');
        }

        set(draft => {
          draft.isSaving = true;
        });

        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000));

        // Reset after successful save
        get().reset();
      },

      reset: () => {
        set(state => {
          state.currentStep = 0;
          state.isDirty = false;
          state.lastSaved = null;
          state.draftId = null;
          state.isLoading = false;
          state.isSaving = false;
          state.customerData = {};
          state.locations = [];
          state.locationFieldValues = {};
          state.detailedLocations = [];
          state.validationErrors = {};
          state.contacts = [];
          state.primaryContactId = undefined;
          state.contactValidationErrors = {};
          state.selectedLocationId = 'all';
          state.applyToAllLocations = false;
          state.locationServices = {};
          state.completedLocationIds = [];
        });
      },

      setFieldDefinitions: (customerFields, locationFields) => {
        set(state => {
          state.customerFields = customerFields;
          state.locationFields = locationFields;
        });
      },

      // ===== Contact Actions (Step 3) =====
      addContact: contact => {
        set(state => {
          const newContact: Contact = {
            id: `temp-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
            customerId: state.draftId || '',
            firstName: contact?.firstName || '',
            lastName: contact?.lastName || '',
            salutation: contact?.salutation,
            title: contact?.title,
            position: contact?.position,
            decisionLevel: contact?.decisionLevel,
            email: contact?.email,
            phone: contact?.phone,
            mobile: contact?.mobile,
            isPrimary: state.contacts.length === 0, // First contact is primary by default
            isActive: true,
            responsibilityScope: contact?.responsibilityScope || 'all',
            assignedLocationIds: contact?.assignedLocationIds || [],
            birthday: contact?.birthday,
            hobbies: contact?.hobbies || [],
            familyStatus: contact?.familyStatus,
            childrenCount: contact?.childrenCount,
            personalNotes: contact?.personalNotes,
            roles: contact?.roles || [],
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          };

          state.contacts.push(newContact);
          if (newContact.isPrimary) {
            state.primaryContactId = newContact.id;
          }
          state.isDirty = true;
        });
      },

      updateContact: (id, updates) => {
        set(state => {
          const index = state.contacts.findIndex(c => c.id === id);
          if (index !== -1) {
            state.contacts[index] = {
              ...state.contacts[index],
              ...updates,
              updatedAt: new Date().toISOString(),
            };

            // Handle primary contact changes
            if (updates.isPrimary === true) {
              // Make all other contacts non-primary
              state.contacts.forEach((c, i) => {
                if (i !== index) {
                  state.contacts[i].isPrimary = false;
                }
              });
              state.primaryContactId = id;
            } else if (updates.isPrimary === false && state.primaryContactId === id) {
              state.primaryContactId = undefined;
            }

            state.isDirty = true;
          }
        });
      },

      removeContact: id => {
        set(state => {
          const wasPrimary = state.contacts.find(c => c.id === id)?.isPrimary;
          state.contacts = state.contacts.filter(c => c.id !== id);

          // If primary contact was removed, make the first contact primary
          if (wasPrimary && state.contacts.length > 0) {
            state.contacts[0].isPrimary = true;
            state.primaryContactId = state.contacts[0].id;
          } else if (state.contacts.length === 0) {
            state.primaryContactId = undefined;
          }

          // Remove validation errors for this contact
          delete state.contactValidationErrors[id];

          state.isDirty = true;
        });
      },

      setPrimaryContact: id => {
        set(state => {
          state.contacts.forEach(contact => {
            contact.isPrimary = contact.id === id;
          });
          state.primaryContactId = id;
          state.isDirty = true;
        });
      },

      validateContactField: async (contactId, fieldKey, value) => {
        set(state => {
          if (!state.contactValidationErrors[contactId]) {
            state.contactValidationErrors[contactId] = {
              contactId,
              fieldErrors: {},
            };
          }

          // Basic validation for required fields
          if (fieldKey === 'firstName' || fieldKey === 'lastName') {
            if (!value || value.trim() === '') {
              state.contactValidationErrors[contactId].fieldErrors[fieldKey] =
                fieldKey === 'firstName' ? 'Vorname ist erforderlich' : 'Nachname ist erforderlich';
            } else {
              delete state.contactValidationErrors[contactId].fieldErrors[fieldKey];
            }
          }

          // Email validation
          if (fieldKey === 'email' && value) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(value)) {
              state.contactValidationErrors[contactId].fieldErrors[fieldKey] =
                'Ungültige E-Mail-Adresse';
            } else {
              delete state.contactValidationErrors[contactId].fieldErrors[fieldKey];
            }
          }

          // Phone validation
          if ((fieldKey === 'phone' || fieldKey === 'mobile') && value) {
            const phoneRegex = /^[\d\s\-+()/]+$/;
            if (!phoneRegex.test(value)) {
              state.contactValidationErrors[contactId].fieldErrors[fieldKey] =
                'Ungültige Telefonnummer';
            } else {
              delete state.contactValidationErrors[contactId].fieldErrors[fieldKey];
            }
          }

          // Clean up if no errors
          if (Object.keys(state.contactValidationErrors[contactId].fieldErrors).length === 0) {
            delete state.contactValidationErrors[contactId];
          }
        });
      },

      validateContacts: async () => {
        const state = get();
        let isValid = true;

        // Must have at least one contact
        if (state.contacts.length === 0) {
          return false;
        }

        // Must have exactly one primary contact
        const primaryContacts = state.contacts.filter(c => c.isPrimary);
        if (primaryContacts.length !== 1) {
          isValid = false;
        }

        // Validate each contact
        for (const contact of state.contacts) {
          // Required fields
          if (!contact.firstName || !contact.lastName) {
            isValid = false;
            await get().validateContactField(contact.id, 'firstName', contact.firstName);
            await get().validateContactField(contact.id, 'lastName', contact.lastName);
          }

          // Optional field validation
          if (contact.email) {
            await get().validateContactField(contact.id, 'email', contact.email);
          }
          if (contact.phone) {
            await get().validateContactField(contact.id, 'phone', contact.phone);
          }
          if (contact.mobile) {
            await get().validateContactField(contact.id, 'mobile', contact.mobile);
          }
        }

        return isValid && Object.keys(state.contactValidationErrors).length === 0;
      },

      getContact: id => {
        return get().contacts.find(c => c.id === id);
      },

      // ===== Step 2 Extension Actions =====
      setExpectedRevenue: amount => {
        set(state => {
          state.customerData.expectedAnnualRevenue = amount;
          state.isDirty = true;
        });
      },

      setSelectedLocation: locationId => {
        set(state => {
          state.selectedLocationId = locationId;
        });
      },

      setApplyToAll: value => {
        set(state => {
          state.applyToAllLocations = value;
        });
      },

      saveLocationServices: data => {
        set(state => {
          const { selectedLocationId, applyToAllLocations, locations } = state;

          if (selectedLocationId === 'all' || applyToAllLocations) {
            // Speichere für alle Standorte
            locations.forEach(loc => {
              state.locationServices[loc.id] = { ...data };
            });
            state.locationServices['all'] = { ...data };
          } else {
            // Speichere nur für ausgewählten Standort
            state.locationServices[selectedLocationId] = { ...data };
          }

          // Markiere als abgeschlossen
          if (
            selectedLocationId !== 'all' &&
            !state.completedLocationIds.includes(selectedLocationId)
          ) {
            state.completedLocationIds.push(selectedLocationId);
          }

          state.isDirty = true;
        });
      },

      getLocationServices: locationId => {
        const state = get();

        if (state.applyToAllLocations && state.locationServices['all']) {
          return state.locationServices['all'];
        }

        return state.locationServices[locationId] || {};
      },

      markLocationCompleted: locationId => {
        set(state => {
          if (!state.completedLocationIds.includes(locationId)) {
            state.completedLocationIds.push(locationId);
          }
        });
      },
    })),
    {
      name: STORAGE_KEY,
      storage: createJSONStorage(() => localStorage),
      partialize: state => ({
        customerData: state.customerData,
        locations: state.locations,
        locationFieldValues: state.locationFieldValues,
        detailedLocations: state.detailedLocations,
        contacts: state.contacts,
        primaryContactId: state.primaryContactId,
        draftId: state.draftId,
        lastSaved: state.lastSaved,
      }),
    }
  )
);
