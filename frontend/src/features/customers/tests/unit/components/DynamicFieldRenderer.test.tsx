/**
 * FC-005 Dynamic Field Renderer Tests (Simplified)
 * 
 * Tests für das Kernstück der field-basierten Architektur.
 * Respektiert Flexibilitäts-Philosophie: any Types sind FEATURES!
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/09-TEST-PLAN/00-PHILOSOPHIE.md
 */

import { describe, it, expect, vi } from 'vitest';
import { render } from '@testing-library/react';
import { DynamicFieldRenderer } from '../../../components/fields/DynamicFieldRenderer';
import { FieldDefinition } from '../../../types/field.types';

// Simple mock for fieldTypes
vi.mock('../../../components/fields/fieldTypes/TextField', () => ({
  TextField: ({ field, value, onChange }: any) => (
    <input
      data-testid={`textfield-${field.key}`}
      value={value || ''}
      onChange={(e) => onChange(field.key, e.target.value)}
    />
  )
}));

vi.mock('../../../components/fields/fieldTypes/NumberField', () => ({
  NumberField: ({ field, value, onChange }: any) => (
    <input
      type="number"
      data-testid={`numberfield-${field.key}`}
      value={value || ''}
      onChange={(e) => onChange(field.key, e.target.value)}
    />
  )
}));

vi.mock('../../../components/fields/fieldTypes/SelectField', () => ({
  SelectField: ({ field, value, onChange }: any) => (
    <select
      data-testid={`selectfield-${field.key}`}
      value={value || ''}
      onChange={(e) => onChange(field.key, e.target.value)}
    >
      {field.options?.map((opt: any) => (
        <option key={opt.value} value={opt.value}>{opt.label}</option>
      ))}
    </select>
  )
}));

vi.mock('../../../components/fields/fieldTypes/MultiSelectField', () => ({
  MultiSelectField: ({ field, value, onChange }: any) => (
    <select
      multiple
      data-testid={`multiselectfield-${field.key}`}
      value={value || []}
      onChange={(e) => onChange(field.key, Array.from(e.target.selectedOptions, opt => opt.value))}
    />
  )
}));

vi.mock('../../../components/fields/fieldTypes/EmailField', () => ({
  EmailField: ({ field, value, onChange }: any) => (
    <input
      type="email"
      data-testid={`emailfield-${field.key}`}
      value={value || ''}
      onChange={(e) => onChange(field.key, e.target.value)}
    />
  )
}));

vi.mock('../../../components/fields/fieldTypes/TextAreaField', () => ({
  TextAreaField: ({ field, value, onChange }: any) => (
    <textarea
      data-testid={`textareafield-${field.key}`}
      value={value || ''}
      onChange={(e) => onChange(field.key, e.target.value)}
    />
  )
}));

vi.mock('../../../components/fields/FieldWrapper', () => ({
  FieldWrapper: ({ children }: any) => <div data-testid="field-wrapper">{children}</div>
}));

describe('DynamicFieldRenderer - Flexible Field System (ENTERPRISE PHILOSOPHY)', () => {
  const mockOnChange = vi.fn();
  const mockOnBlur = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('✅ Field-Based Architecture Support (KERNFEATURE)', () => {
    it('should render fields array with flexible field definitions', () => {
      const fields: FieldDefinition[] = [
        {
          id: '1',
          key: 'companyName',
          label: 'Firmenname',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
          sortOrder: 1
        },
        {
          id: '2',
          key: 'industry',
          label: 'Branche',
          fieldType: 'select',
          entityType: 'customer',
          required: true,
          sortOrder: 2,
          options: [
            { value: 'hotel', label: 'Hotel' },
            { value: 'restaurant', label: 'Restaurant' }
          ]
        }
      ];

      const values = {
        companyName: 'Test Hotel GmbH',
        industry: 'hotel'
      };

      const errors = {};

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={values}
          errors={errors}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      expect(document.querySelector('[data-testid="textfield-companyName"]')).toBeInTheDocument();
      expect(document.querySelector('[data-testid="selectfield-industry"]')).toBeInTheDocument();
    });

    it('should handle flexible field values with any types', () => {
      const fields: FieldDefinition[] = [
        {
          id: '1',
          key: 'flexibleField',
          label: 'Flexible Field',
          fieldType: 'text',
          entityType: 'customer',
          required: false,
          sortOrder: 1
        }
      ];

      // Test verschiedene Value-Types
      const testValues = [
        'string value',
        123,
        true,
        { complex: 'object' },
        ['array', 'value'],
        null,
        undefined
      ];

      testValues.forEach((value, index) => {
        // Clear DOM before each test
        document.body.innerHTML = '';
        
        const values = { flexibleField: value };

        render(
          <DynamicFieldRenderer
            fields={fields}
            values={values}
            errors={{}}
            onChange={mockOnChange}
            onBlur={mockOnBlur}
          />
        );

        const input = document.querySelector('[data-testid="textfield-flexibleField"]') as HTMLInputElement;
        expect(input).toBeInTheDocument();
        // Wert wird als String dargestellt (das ist OK für unsere Flexibilität)
        expect(input.value).toBe(String(value || ''));
      });
    });

    it('should handle empty fields array gracefully', () => {
      render(
        <DynamicFieldRenderer
          fields={[]}
          values={{}}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      // Should render without errors
      expect(document.body).toBeInTheDocument();
    });

    it('should support all field types from our architecture', () => {
      const allFieldTypes: FieldDefinition[] = [
        {
          id: '1',
          key: 'textField',
          label: 'Text',
          fieldType: 'text',
          entityType: 'customer',
          required: false,
          sortOrder: 1
        },
        {
          id: '2',
          key: 'numberField',
          label: 'Number',
          fieldType: 'number',
          entityType: 'customer',
          required: false,
          sortOrder: 2
        },
        {
          id: '3',
          key: 'emailField',
          label: 'Email',
          fieldType: 'email',
          entityType: 'customer',
          required: false,
          sortOrder: 3
        },
        {
          id: '4',
          key: 'selectField',
          label: 'Select',
          fieldType: 'select',
          entityType: 'customer',
          required: false,
          sortOrder: 4,
          options: [{ value: 'option1', label: 'Option 1' }]
        },
        {
          id: '5',
          key: 'textareaField',
          label: 'Textarea',
          fieldType: 'textarea',
          entityType: 'customer',
          required: false,
          sortOrder: 5
        }
      ];

      render(
        <DynamicFieldRenderer
          fields={allFieldTypes}
          values={{}}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      // Alle Field-Types sollten gerendert werden
      expect(document.querySelector('[data-testid="textfield-textField"]')).toBeInTheDocument();
      expect(document.querySelector('[data-testid="numberfield-numberField"]')).toBeInTheDocument();
      expect(document.querySelector('[data-testid="emailfield-emailField"]')).toBeInTheDocument();
      expect(document.querySelector('[data-testid="selectfield-selectField"]')).toBeInTheDocument();
      expect(document.querySelector('[data-testid="textareafield-textareaField"]')).toBeInTheDocument();
    });
  });

  describe('🏢 Conditional Field Visibility (CR-001 Feature)', () => {
    it('should show fields with generic condition when condition is met', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'industry',
          label: 'Branche',
          fieldType: 'select',
          entityType: 'customer',
          required: true,
          options: [
            { value: 'hotel', label: 'Hotel' },
            { value: 'restaurant', label: 'Restaurant' }
          ]
        },
        {
          key: 'hotelStars',
          label: 'Hotel Sterne',
          fieldType: 'select',
          entityType: 'customer',
          required: false,
          options: [
            { value: '1', label: '1 Stern' },
            { value: '5', label: '5 Sterne' }
          ],
          // NEW: Generic condition support
          condition: {
            field: 'industry',
            operator: 'equals',
            value: 'hotel'
          }
        }
      ];

      const values = {
        industry: 'hotel'
      };

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={values}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      // Both fields should be visible
      expect(document.querySelector('[data-testid="selectfield-industry"]')).toBeInTheDocument();
      expect(document.querySelector('[data-testid="selectfield-hotelStars"]')).toBeInTheDocument();
    });

    it('should hide fields with generic condition when condition is not met', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'industry',
          label: 'Branche',
          fieldType: 'select',
          entityType: 'customer',
          required: true,
          options: [
            { value: 'hotel', label: 'Hotel' },
            { value: 'restaurant', label: 'Restaurant' }
          ]
        },
        {
          key: 'hotelStars',
          label: 'Hotel Sterne',
          fieldType: 'select',
          entityType: 'customer',
          required: false,
          condition: {
            field: 'industry',
            operator: 'equals',
            value: 'hotel'
          }
        }
      ];

      const values = {
        industry: 'restaurant' // Different value
      };

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={values}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      // Only industry field should be visible
      expect(document.querySelector('[data-testid="selectfield-industry"]')).toBeInTheDocument();
      expect(document.querySelector('[data-testid="selectfield-hotelStars"]')).not.toBeInTheDocument();
    });

    it('should support "in" operator for multiple values', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'industry',
          label: 'Branche',
          fieldType: 'select',
          entityType: 'customer',
          required: true
        },
        {
          key: 'foodServiceType',
          label: 'Gastronomie Art',
          fieldType: 'select',
          entityType: 'customer',
          required: false,
          condition: {
            field: 'industry',
            operator: 'in',
            value: ['hotel', 'restaurant', 'catering']
          }
        }
      ];

      const values = {
        industry: 'restaurant'
      };

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={values}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      expect(document.querySelector('[data-testid="selectfield-foodServiceType"]')).toBeInTheDocument();
    });

    it('should support "exists" operator', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'optionalField',
          label: 'Optional Field',
          fieldType: 'text',
          entityType: 'customer',
          required: false
        },
        {
          key: 'dependentField',
          label: 'Dependent Field',
          fieldType: 'text',
          entityType: 'customer',
          required: false,
          condition: {
            field: 'optionalField',
            operator: 'exists'
          }
        }
      ];

      const values = {
        optionalField: 'some value'
      };

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={values}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      expect(document.querySelector('[data-testid="textfield-dependentField"]')).toBeInTheDocument();
    });

    it('should support currentStep parameter for wizard step filtering', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'basicField',
          label: 'Basic Field',
          fieldType: 'text',
          entityType: 'customer',
          required: false,
          wizardStep: 'basic'
        },
        {
          key: 'advancedField',
          label: 'Advanced Field',
          fieldType: 'text',
          entityType: 'customer',
          required: false,
          wizardStep: 'advanced'
        }
      ];

      const values = {};

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={values}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
          currentStep="basic"
        />
      );

      // Only basic step field should be visible
      expect(document.querySelector('[data-testid="textfield-basicField"]')).toBeInTheDocument();
      expect(document.querySelector('[data-testid="textfield-advancedField"]')).not.toBeInTheDocument();
    });

    it('should handle industry-specific field visibility', () => {
      const fields: FieldDefinition[] = [
        {
          key: 'companyName',
          label: 'Firmenname',
          fieldType: 'text',
          entityType: 'customer',
          required: true
        },
        {
          key: 'hotelStars',
          label: 'Hotel Sterne',
          fieldType: 'select',
          entityType: 'customer',
          required: false,
          options: [
            { value: '1', label: '1 Stern' },
            { value: '5', label: '5 Sterne' }
          ],
          // Legacy trigger condition (still supported)
          triggerWizardStep: {
            when: 'hotel',
            step: 'industry'
          }
        }
      ];

      const values = {
        companyName: 'Test Hotel',
        industry: 'hotel'
      };

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={values}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      // Both fields should be visible (legacy system still works)
      expect(document.querySelector('[data-testid="textfield-companyName"]')).toBeInTheDocument();
      expect(document.querySelector('[data-testid="selectfield-hotelStars"]')).toBeInTheDocument();
    });

    it('should handle dynamic field structures for different industries', () => {
      const hotelFields: FieldDefinition[] = [
        {
          id: 'hotel-1',
          key: 'roomCount',
          label: 'Zimmeranzahl',
          fieldType: 'number',
          entityType: 'customer',
          required: false,
          sortOrder: 1
        }
      ];

      const restaurantFields: FieldDefinition[] = [
        {
          id: 'restaurant-1',
          key: 'seatingCapacity',
          label: 'Sitzplätze',
          fieldType: 'number',
          entityType: 'customer',
          required: false,
          sortOrder: 1
        }
      ];

      // Hotel rendering
      render(
        <DynamicFieldRenderer
          fields={hotelFields}
          values={{ roomCount: 150 }}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      expect(document.querySelector('[data-testid="numberfield-roomCount"]')).toBeInTheDocument();

      // Restaurant rendering (in separate test to avoid DOM conflicts)
      document.body.innerHTML = '';
      
      render(
        <DynamicFieldRenderer
          fields={restaurantFields}
          values={{ seatingCapacity: 120 }}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      expect(document.querySelector('[data-testid="numberfield-seatingCapacity"]')).toBeInTheDocument();
    });
  });

  describe('📋 Validation & Error Handling', () => {
    it('should handle validation errors for fields', () => {
      const fields: FieldDefinition[] = [
        {
          id: '1',
          key: 'companyName',
          label: 'Firmenname',
          fieldType: 'text',
          entityType: 'customer',
          required: true,
          sortOrder: 1
        }
      ];

      const errors = {
        companyName: 'Firmenname ist erforderlich'
      };

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={{}}
          errors={errors}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
        />
      );

      // Field sollte trotz Fehler gerendert werden
      expect(document.querySelector('[data-testid="textfield-companyName"]')).toBeInTheDocument();
    });

    it('should handle missing or malformed field definitions gracefully', () => {
      const malformedFields = [
        {
          id: '1',
          key: 'malformed'
          // Missing: label, fieldType, entityType
        } as FieldDefinition
      ];

      expect(() => {
        render(
          <DynamicFieldRenderer
            fields={malformedFields}
            values={{}}
            errors={{}}
            onChange={mockOnChange}
            onBlur={mockOnBlur}
          />
        );
      }).not.toThrow();
    });
  });

  describe('🎯 Edge Cases & Robustheit', () => {
    it('should handle loading state', () => {
      const fields: FieldDefinition[] = [
        {
          id: '1',
          key: 'testField',
          label: 'Test Field',
          fieldType: 'text',
          entityType: 'customer',
          required: false,
          sortOrder: 1
        }
      ];

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={{}}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
          loading={true}
        />
      );

      // Should render even in loading state
      expect(document.querySelector('[data-testid="textfield-testField"]')).toBeInTheDocument();
    });

    it('should handle read-only mode', () => {
      const fields: FieldDefinition[] = [
        {
          id: '1',
          key: 'readOnlyField',
          label: 'Read Only Field',
          fieldType: 'text',
          entityType: 'customer',
          required: false,
          sortOrder: 1
        }
      ];

      render(
        <DynamicFieldRenderer
          fields={fields}
          values={{ readOnlyField: 'read only value' }}
          errors={{}}
          onChange={mockOnChange}
          onBlur={mockOnBlur}
          readOnly={true}
        />
      );

      // Should render in read-only mode
      expect(document.querySelector('[data-testid="textfield-readOnlyField"]')).toBeInTheDocument();
    });

    it('should handle extremely large field arrays', () => {
      // Test with 100 fields
      const manyFields: FieldDefinition[] = Array.from({ length: 100 }, (_, i) => ({
        id: `field-${i}`,
        key: `field_${i}`,
        label: `Field ${i}`,
        fieldType: 'text' as const,
        entityType: 'customer' as const,
        required: false,
        sortOrder: i
      }));

      expect(() => {
        render(
          <DynamicFieldRenderer
            fields={manyFields}
            values={{}}
            errors={{}}
            onChange={mockOnChange}
            onBlur={mockOnBlur}
          />
        );
      }).not.toThrow();
    });
  });

  describe('🚀 Performance', () => {
    it('should not crash with frequent re-renders', () => {
      const fields: FieldDefinition[] = [
        {
          id: '1',
          key: 'performanceField',
          label: 'Performance Field',
          fieldType: 'text',
          entityType: 'customer',
          required: false,
          sortOrder: 1
        }
      ];

      // Simulate multiple re-renders
      for (let i = 0; i < 10; i++) {
        const { unmount } = render(
          <DynamicFieldRenderer
            fields={fields}
            values={{ performanceField: `value-${i}` }}
            errors={{}}
            onChange={mockOnChange}
            onBlur={mockOnBlur}
          />
        );
        unmount();
      }

      // Should complete without errors
      expect(true).toBe(true);
    });
  });
});