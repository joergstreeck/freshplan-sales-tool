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
import { FieldDefinition } from '../types/field.types';
import { Location, DetailedLocation } from '../types/location.types';
import { 
  buildFieldSchema, 
  buildFormSchema, 
  validateField, 
  validateFields
} from '../validation';
import { getVisibleFields } from '../utils/conditionEvaluator';

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
  
  // ===== Field Definitions =====
  /** Customer field definitions */
  customerFields: FieldDefinition[];
  /** Location field definitions */
  locationFields: FieldDefinition[];
  
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
  addDetailedLocation: (locationId: string, detailedLocation: Omit<DetailedLocation, 'id' | 'locationId' | 'createdAt' | 'updatedAt'>) => void;
  /** Update detailed location */
  updateDetailedLocation: (detailedLocationId: string, updates: Partial<DetailedLocation>) => void;
  /** Remove detailed location */
  removeDetailedLocation: (detailedLocationId: string) => void;
  /** Add batch detailed locations */
  addBatchDetailedLocations: (locationId: string, detailedLocations: Omit<DetailedLocation, 'id' | 'locationId' | 'createdAt' | 'updatedAt'>[]) => void;
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
  setFieldDefinitions: (customerFields: FieldDefinition[], locationFields: FieldDefinition[]) => void;
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
      customerFields: [],
      locationFields: [],
      
      // ===== Actions =====
      setCustomerField: (fieldKey, value) => {
        set((state) => {
          state.customerData[fieldKey] = value;
          state.isDirty = true;
          // Clear validation error when field is updated
          delete state.validationErrors[fieldKey];
        });
      },
      
      setLocationField: (locationId, fieldKey, value) => {
        set((state) => {
          if (!state.locationFieldValues[locationId]) {
            state.locationFieldValues[locationId] = {};
          }
          state.locationFieldValues[locationId][fieldKey] = value;
          state.isDirty = true;
        });
      },
      
      setCurrentStep: (step) => {
        set((state) => {
          state.currentStep = step;
        });
      },
      
      addLocation: () => {
        set((state) => {
          const newLocation: Location = {
            id: `temp-${Date.now()}`,
            customerId: state.draftId || '',
            locationType: 'LIEFERADRESSE',
            sortOrder: state.locations.length,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          };
          state.locations.push(newLocation);
          state.locationFieldValues[newLocation.id] = {};
          state.isDirty = true;
        });
      },
      
      removeLocation: (locationId) => {
        set((state) => {
          state.locations = state.locations.filter(loc => loc.id !== locationId);
          delete state.locationFieldValues[locationId];
          state.isDirty = true;
        });
      },
      
      updateLocation: (locationId, updates) => {
        set((state) => {
          const locationIndex = state.locations.findIndex(loc => loc.id === locationId);
          if (locationIndex !== -1) {
            state.locations[locationIndex] = {
              ...state.locations[locationIndex],
              ...updates,
              updatedAt: new Date().toISOString()
            };
            state.isDirty = true;
          }
        });
      },
      
      addDetailedLocation: (locationId, detailedLocation) => {
        set((state) => {
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
            updatedAt: new Date().toISOString()
          };
          
          state.detailedLocations.push(newDetailedLocation);
          state.isDirty = true;
        });
      },
      
      updateDetailedLocation: (detailedLocationId, updates) => {
        set((state) => {
          const index = state.detailedLocations.findIndex(dl => dl.id === detailedLocationId);
          if (index !== -1) {
            state.detailedLocations[index] = {
              ...state.detailedLocations[index],
              ...updates,
              updatedAt: new Date().toISOString()
            };
            state.isDirty = true;
          }
        });
      },
      
      removeDetailedLocation: (detailedLocationId) => {
        set((state) => {
          state.detailedLocations = state.detailedLocations.filter(
            dl => dl.id !== detailedLocationId
          );
          state.isDirty = true;
        });
      },
      
      addBatchDetailedLocations: (locationId, detailedLocations) => {
        set((state) => {
          const startOrder = state.detailedLocations.filter(dl => dl.locationId === locationId).length;
          
          const newDetailedLocations = detailedLocations.map((dl, index) => ({
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
            updatedAt: new Date().toISOString()
          } as DetailedLocation));
          
          state.detailedLocations.push(...newDetailedLocations);
          state.isDirty = true;
        });
      },
      
      validateField: async (fieldKey) => {
        const state = get();
        const field = state.customerFields.find(f => f.key === fieldKey);
        if (!field) return;
        
        const value = state.customerData[fieldKey];
        
        // NEW: Use Dynamic Zod Schema Builder for validation
        try {
          const result = await validateField(field, value);
          
          set((draft) => {
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
          set((draft) => {
            draft.validationErrors[fieldKey] = `${field.label} konnte nicht validiert werden`;
          });
        }
      },
      
      validateCurrentStep: async () => {
        const state = get();
        
        // NEW: Dynamic validation with conditional field visibility
        let fieldsToValidate: FieldDefinition[] = [];
        
        if (state.currentStep === 0) {
          // Get visible customer fields for current step with conditional logic
          const visibleFields = getVisibleFields(
            state.customerFields, 
            state.customerData,
            'customer' // currentStep parameter 
          );
          fieldsToValidate = visibleFields.filter(field => field.required);
        } else if (state.currentStep === 1) {
          // Location fields validation if applicable
          fieldsToValidate = []; // Will be extended when location validation is needed
        }
        
        // NEW: Use Dynamic Zod Schema Builder for step validation
        try {
          const fieldsWithValues = fieldsToValidate.map(field => ({
            field,
            value: state.customerData[field.key]
          }));
          
          const validationErrors = await validateFields(fieldsWithValues);
          const errors = Object.fromEntries(validationErrors);
          
          set((draft) => {
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
          
          set((draft) => {
            draft.validationErrors = errors;
          });
          
          return Object.keys(errors).length === 0;
        }
      },
      
      canProgressToNextStep: () => {
        const state = get();
        
        // Step 0: Customer data must be valid
        if (state.currentStep === 0) {
          const requiredFields = state.customerFields.filter(f => f.required);
          return requiredFields.every(field => {
            const value = state.customerData[field.key];
            return value !== undefined && value !== '' && value !== null;
          });
        }
        
        // Step 1: At least one location for chain customers
        if (state.currentStep === 1) {
          return state.locations.length > 0;
        }
        
        return true;
      },
      
      saveAsDraft: async () => {
        set((state) => {
          state.isSaving = true;
        });
        
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 500));
        
        set((state) => {
          state.lastSaved = new Date();
          state.isDirty = false;
          state.isSaving = false;
          if (!state.draftId) {
            state.draftId = `draft-${Date.now()}`;
          }
        });
      },
      
      loadDraft: async (draftId) => {
        set((state) => {
          state.isLoading = true;
        });
        
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 500));
        
        set((state) => {
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
        
        set((draft) => {
          draft.isSaving = true;
        });
        
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // Reset after successful save
        get().reset();
      },
      
      reset: () => {
        set((state) => {
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
        });
      },
      
      setFieldDefinitions: (customerFields, locationFields) => {
        set((state) => {
          state.customerFields = customerFields;
          state.locationFields = locationFields;
        });
      }
    })),
    {
      name: STORAGE_KEY,
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        customerData: state.customerData,
        locations: state.locations,
        locationFieldValues: state.locationFieldValues,
        detailedLocations: state.detailedLocations,
        draftId: state.draftId,
        lastSaved: state.lastSaved
      })
    }
  )
);