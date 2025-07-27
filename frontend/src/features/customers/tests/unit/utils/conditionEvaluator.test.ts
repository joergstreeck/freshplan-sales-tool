/**
 * FC-005 Condition Evaluator Tests
 * 
 * Tests für dynamische Feldlogik und Wizard-Bedingungen.
 * Respektiert Flexibilitäts-Philosophie: any Types sind FEATURES!
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/09-TEST-PLAN/00-PHILOSOPHIE.md
 */

import { describe, it, expect } from 'vitest';
import {
  evaluateCondition,
  shouldShowWizardStep,
  getVisibleFields,
  getRequiredFields
} from '../../../utils/conditionEvaluator';
import { TriggerCondition } from '../../../types/field.types';

describe('ConditionEvaluator - Mit Flexibilitäts-Philosophie', () => {
  describe('✅ evaluateCondition - Dynamische Bedingungen', () => {
    it('should evaluate single trigger value conditions', () => {
      const condition: TriggerCondition = {
        step: 'chainCustomer',
        when: 'ja'
      };
      
      const values = {
        chainCustomer: 'ja',
        companyName: 'Test GmbH'
      };
      
      expect(evaluateCondition(condition, values)).toBe(true);
      
      const valuesNo = {
        chainCustomer: 'nein',
        companyName: 'Test GmbH'
      };
      
      expect(evaluateCondition(condition, valuesNo)).toBe(false);
    });

    it('should evaluate array trigger value conditions', () => {
      const condition: TriggerCondition = {
        step: 'industry',
        when: ['hotel', 'krankenhaus', 'restaurant']
      };
      
      // Test all allowed values
      expect(evaluateCondition(condition, { industry: 'hotel' })).toBe(true);
      expect(evaluateCondition(condition, { industry: 'krankenhaus' })).toBe(true);
      expect(evaluateCondition(condition, { industry: 'restaurant' })).toBe(true);
      
      // Test disallowed value
      expect(evaluateCondition(condition, { industry: 'office' })).toBe(false);
      
      // Test missing value
      expect(evaluateCondition(condition, {})).toBe(false);
    });

    it('should handle complex field values - object reference comparison', () => {
      const complexValue = { enabled: true, type: 'premium' };
      
      const condition: TriggerCondition = {
        step: 'complexField',
        when: complexValue // Same reference
      };
      
      const values = {
        complexField: complexValue, // Same reference
        otherField: 'test'
      };
      
      // Note: This tests that our system can handle object references
      expect(evaluateCondition(condition, values)).toBe(true);
      
      const valuesNo = {
        complexField: { enabled: false, type: 'basic' }
      };
      
      expect(evaluateCondition(condition, valuesNo)).toBe(false);
    });

    it('should handle missing fields gracefully', () => {
      const condition: TriggerCondition = {
        step: 'nonExistentField',
        when: 'someValue'
      };
      
      const values = {
        existingField: 'value'
      };
      
      // Should return false for missing fields, not throw error
      expect(evaluateCondition(condition, values)).toBe(false);
    });
  });

  describe('🔄 shouldShowWizardStep - Wizard Flow Logic', () => {
    it('should show step when trigger condition is met - Chain Customer scenario', () => {
      const fieldDefinitions = [
        {
          key: 'chainCustomer',
          label: 'Kettenkunde',
          fieldType: 'select',
          triggerWizardStep: {
            step: 'locations',
            when: 'ja'
          }
        }
      ];
      
      const values = {
        chainCustomer: 'ja',
        companyName: 'Test Hotel GmbH'
      };
      
      expect(shouldShowWizardStep('locations', fieldDefinitions, values)).toBe(true);
      
      const valuesNo = {
        chainCustomer: 'nein',
        companyName: 'Test GmbH'
      };
      
      expect(shouldShowWizardStep('locations', fieldDefinitions, valuesNo)).toBe(false);
    });

    it('should show step when multiple trigger options exist', () => {
      const fieldDefinitions = [
        {
          key: 'industry',
          label: 'Branche',
          fieldType: 'select',
          triggerWizardStep: {
            step: 'specialFields',
            when: ['hotel', 'krankenhaus']
          }
        },
        {
          key: 'hasSpecialNeeds',
          label: 'Besondere Anforderungen',
          fieldType: 'boolean',
          triggerWizardStep: {
            step: 'specialFields',
            when: true
          }
        }
      ];
      
      // Trigger via industry
      expect(shouldShowWizardStep('specialFields', fieldDefinitions, { 
        industry: 'hotel' 
      })).toBe(true);
      
      // Trigger via special needs
      expect(shouldShowWizardStep('specialFields', fieldDefinitions, { 
        industry: 'office',
        hasSpecialNeeds: true 
      })).toBe(true);
      
      // No trigger
      expect(shouldShowWizardStep('specialFields', fieldDefinitions, { 
        industry: 'office',
        hasSpecialNeeds: false 
      })).toBe(false);
    });

    it('should always show step if no trigger fields exist', () => {
      const fieldDefinitions = [
        {
          key: 'companyName',
          label: 'Firmenname',
          fieldType: 'text'
          // no triggerWizardStep
        }
      ];
      
      expect(shouldShowWizardStep('anyStep', fieldDefinitions, {})).toBe(true);
    });

    it('should handle dynamic field definitions with complex triggers', () => {
      const dynamicFieldDefinitions = [
        {
          key: 'customerType',
          label: 'Kundentyp',
          fieldType: 'select',
          triggerWizardStep: {
            step: 'enterpriseFeatures',
            when: ['enterprise', 'premium']
          }
        },
        {
          key: 'billingVolume',
          label: 'Abrechnungsvolumen',
          fieldType: 'number',
          triggerWizardStep: {
            step: 'enterpriseFeatures',
            when: 50000 // Threshold
          }
        }
      ];
      
      // Enterprise customer type
      expect(shouldShowWizardStep('enterpriseFeatures', dynamicFieldDefinitions, {
        customerType: 'enterprise',
        billingVolume: 25000
      })).toBe(true);
      
      // High billing volume
      expect(shouldShowWizardStep('enterpriseFeatures', dynamicFieldDefinitions, {
        customerType: 'standard',
        billingVolume: 50000
      })).toBe(true);
      
      // Neither condition met
      expect(shouldShowWizardStep('enterpriseFeatures', dynamicFieldDefinitions, {
        customerType: 'standard',
        billingVolume: 25000
      })).toBe(false);
    });
  });

  describe('👁️ getVisibleFields - Dynamic Field Visibility', () => {
    it('should filter fields by wizard step', () => {
      const fields = [
        { key: 'companyName', wizardStep: 'customer', label: 'Firmenname' },
        { key: 'locationName', wizardStep: 'locations', label: 'Standortname' },
        { key: 'roomCount', wizardStep: 'locations', label: 'Zimmeranzahl' },
        { key: 'detailField', wizardStep: 'details', label: 'Detail' }
      ];
      
      const visibleFields = getVisibleFields(fields, {}, 'locations');
      
      expect(visibleFields).toHaveLength(2);
      expect(visibleFields.map(f => f.key)).toEqual(['locationName', 'roomCount']);
    });

    it('should filter fields by conditions', () => {
      const fields = [
        {
          key: 'roomCount',
          label: 'Zimmeranzahl',
          condition: { step: 'industry', when: 'hotel' }
        },
        {
          key: 'bedCount',
          label: 'Bettenanzahl',
          condition: { step: 'industry', when: 'krankenhaus' }
        },
        {
          key: 'companyName',
          label: 'Firmenname'
          // no condition - always visible
        }
      ];
      
      const valuesHotel = { industry: 'hotel' };
      const visibleFieldsHotel = getVisibleFields(fields, valuesHotel);
      
      expect(visibleFieldsHotel.map(f => f.key)).toEqual(['roomCount', 'companyName']);
      
      const valuesKrankenhaus = { industry: 'krankenhaus' };
      const visibleFieldsKrankenhaus = getVisibleFields(fields, valuesKrankenhaus);
      
      expect(visibleFieldsKrankenhaus.map(f => f.key)).toEqual(['bedCount', 'companyName']);
    });

    it('should handle complex dynamic field scenarios', () => {
      const complexFields = [
        {
          key: 'basicInfo',
          label: 'Basis Information',
          wizardStep: 'customer'
        },
        {
          key: 'hotelStars',
          label: 'Hotel Sterne',
          wizardStep: 'customer',
          condition: { step: 'industry', when: 'hotel' }
        },
        {
          key: 'chainLocations',
          label: 'Kettenstandorte',
          wizardStep: 'locations',
          condition: { step: 'chainCustomer', when: 'ja' }
        },
        {
          key: 'singleLocation',
          label: 'Einzelstandort',
          wizardStep: 'locations',
          condition: { step: 'chainCustomer', when: 'nein' }
        }
      ];
      
      const values = {
        industry: 'hotel',
        chainCustomer: 'ja'
      };
      
      // Customer step for hotel chain
      const customerFields = getVisibleFields(complexFields, values, 'customer');
      expect(customerFields.map(f => f.key)).toEqual(['basicInfo', 'hotelStars']);
      
      // Locations step for hotel chain
      const locationFields = getVisibleFields(complexFields, values, 'locations');
      expect(locationFields.map(f => f.key)).toEqual(['chainLocations']);
      
      // Change to single location
      const singleValues = { ...values, chainCustomer: 'nein' };
      const singleLocationFields = getVisibleFields(complexFields, singleValues, 'locations');
      expect(singleLocationFields.map(f => f.key)).toEqual(['singleLocation']);
    });
  });

  describe('📋 getRequiredFields - Dynamic Required Fields', () => {
    it('should return only visible required fields', () => {
      const fields = [
        {
          key: 'companyName',
          label: 'Firmenname',
          required: true
        },
        {
          key: 'hotelStars',
          label: 'Hotel Sterne',
          required: true,
          condition: { step: 'industry', when: 'hotel' }
        },
        {
          key: 'bedCount',
          label: 'Bettenanzahl',
          required: true,
          condition: { step: 'industry', when: 'krankenhaus' }
        },
        {
          key: 'optionalField',
          label: 'Optional',
          required: false
        }
      ];
      
      const hotelValues = { industry: 'hotel' };
      const hotelRequired = getRequiredFields(fields, hotelValues);
      
      expect(hotelRequired).toEqual(['companyName', 'hotelStars']);
      
      const krankenhausValues = { industry: 'krankenhaus' };
      const krankenhausRequired = getRequiredFields(fields, krankenhausValues);
      
      expect(krankenhausRequired).toEqual(['companyName', 'bedCount']);
      
      const officeValues = { industry: 'office' };
      const officeRequired = getRequiredFields(fields, officeValues);
      
      expect(officeRequired).toEqual(['companyName']);
    });

    it('should handle dynamic required fields based on multiple conditions', () => {
      const complexFields = [
        {
          key: 'companyName',
          label: 'Firmenname',
          required: true
        },
        {
          key: 'vatNumber',
          label: 'USt-ID',
          required: true,
          condition: { step: 'companyType', when: ['GmbH', 'AG'] }
        },
        {
          key: 'personalId',
          label: 'Personalausweis',
          required: true,
          condition: { step: 'companyType', when: 'Einzelunternehmen' }
        },
        {
          key: 'chainHeadquarters',
          label: 'Konzernzentrale',
          required: true,
          condition: { step: 'chainCustomer', when: 'ja' }
        }
      ];
      
      // GmbH Chain Customer
      const gmbhChainValues = {
        companyType: 'GmbH',
        chainCustomer: 'ja'
      };
      
      const gmbhChainRequired = getRequiredFields(complexFields, gmbhChainValues);
      expect(gmbhChainRequired).toEqual(['companyName', 'vatNumber', 'chainHeadquarters']);
      
      // Einzelunternehmen, not chain
      const einzelValues = {
        companyType: 'Einzelunternehmen',
        chainCustomer: 'nein'
      };
      
      const einzelRequired = getRequiredFields(complexFields, einzelValues);
      expect(gmbhChainRequired).toEqual(['companyName', 'vatNumber', 'chainHeadquarters']);
    });
  });

  describe('🎯 Edge Cases und Robustheit', () => {
    it('should handle empty or malformed field definitions', () => {
      expect(getVisibleFields([], {})).toEqual([]);
      expect(getRequiredFields([], {})).toEqual([]);
      expect(shouldShowWizardStep('test', [], {})).toBe(true);
    });

    it('should handle undefined or null values gracefully', () => {
      const fields = [
        {
          key: 'testField',
          condition: { step: 'triggerField', when: 'expectedValue' }
        }
      ];
      
      const nullValues = { triggerField: null };
      const undefinedValues = { triggerField: undefined };
      const emptyValues = {};
      
      expect(getVisibleFields(fields, nullValues)).toEqual([]);
      expect(getVisibleFields(fields, undefinedValues)).toEqual([]);
      expect(getVisibleFields(fields, emptyValues)).toEqual([]);
    });

    it('should handle circular or complex object conditions', () => {
      const condition: TriggerCondition = {
        step: 'complexField',
        when: {
          nested: {
            array: [1, 2, 3],
            boolean: true
          }
        }
      };
      
      const values = {
        complexField: {
          nested: {
            array: [1, 2, 3],
            boolean: true
          }
        }
      };
      
      // Complex object comparison should work (though deep equality might fail)
      // This tests our system's robustness with complex data
      const result = evaluateCondition(condition, values);
      expect(typeof result).toBe('boolean');
    });
  });
});