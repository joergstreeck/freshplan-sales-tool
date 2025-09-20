---
Navigation: [⬅️ Previous](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md) | [🏠 Home](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [➡️ Next](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/02-integration-tests.md)
Parent: [📁 Testing](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)
Related: [🔗 Backend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md) | [🔗 Frontend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)
---

# 🧪 FC-005 UNIT TESTS

**Fokus:** Business Logic & Component Testing  
**Coverage-Ziel:** 80-90%  
**Frameworks:** JUnit 5, Vitest, React Testing Library  

## 📋 Backend Unit Tests

### Field Value Service Tests

```java
@QuarkusTest
class FieldValueServiceTest {
    
    @InjectMock
    FieldValueRepository fieldValueRepository;
    
    @InjectMock
    FieldDefinitionService fieldDefinitionService;
    
    @InjectMock
    ValidationService validationService;
    
    @Inject
    FieldValueService fieldValueService;
    
    @Test
    void testCreateFieldValue_Success() {
        // Given
        UUID entityId = UUID.randomUUID();
        FieldValueRequest request = new FieldValueRequest("companyName", "Test GmbH");
        FieldDefinition definition = createFieldDefinition("companyName", "text");
        
        when(fieldDefinitionService.getFieldDefinition("companyName"))
            .thenReturn(Optional.of(definition));
        when(fieldValueRepository.findByEntityAndField(entityId, "companyName"))
            .thenReturn(Optional.empty());
        
        // When
        FieldValue result = fieldValueService.createFieldValue(
            entityId, 
            EntityType.CUSTOMER, 
            request
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("Test GmbH");
        verify(validationService).validateFieldValue(definition, "Test GmbH");
    }
    
    @Test
    void testCreateFieldValue_ValidationFailure() {
        // Given
        FieldDefinition definition = createFieldDefinition("email", "email");
        when(fieldDefinitionService.getFieldDefinition("email"))
            .thenReturn(Optional.of(definition));
        doThrow(new ValidationException("Invalid email"))
            .when(validationService).validateFieldValue(any(), eq("invalid"));
        
        // When/Then
        assertThrows(ValidationException.class, () -> 
            fieldValueService.createFieldValue(
                UUID.randomUUID(),
                EntityType.CUSTOMER,
                new FieldValueRequest("email", "invalid")
            )
        );
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"hotel", "krankenhaus", "restaurant"})
    void testIndustrySpecificFields(String industry) {
        // Test that correct fields are returned per industry
        List<FieldDefinition> fields = fieldDefinitionService
            .getFieldsByIndustry(EntityType.LOCATION, industry);
            
        assertThat(fields).isNotEmpty();
        assertThat(fields).allMatch(f -> 
            f.getIndustries().contains(industry)
        );
    }
}
```

### Customer Service Tests

```java
@QuarkusTest
@TestProfile(CustomerTestProfile.class)
class CustomerServiceTest {
    
    @Inject
    CustomerService customerService;
    
    @InjectMock
    Event<CustomerCreatedEvent> customerCreatedEvent;
    
    @Test
    @TestTransaction
    @TestSecurity(user = "testuser", roles = "sales")
    void testCreateCustomerDraft() {
        // Given
        CreateCustomerDraftRequest request = CreateCustomerDraftRequest.builder()
            .fieldValues(List.of(
                new FieldValueRequest("companyName", "Test GmbH"),
                new FieldValueRequest("industry", "hotel"),
                new FieldValueRequest("chainCustomer", "ja")
            ))
            .build();
        
        // When
        CustomerDraftResponse response = customerService.createDraft(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CustomerStatus.DRAFT);
        assertThat(response.getFieldValues())
            .containsEntry("companyName", "Test GmbH")
            .containsEntry("industry", "hotel");
    }
    
    @Test
    @TestTransaction
    void testFinalizeDraft_MissingRequiredFields() {
        // Given - Draft without required fields
        Customer draft = createDraftCustomer();
        
        // When/Then
        assertThrows(MissingRequiredFieldException.class, () ->
            customerService.finalizeDraft(draft.getId())
        );
    }
    
    @Test
    @TestTransaction
    void testFinalizeDraft_Success() {
        // Given
        Customer draft = createCompleteCustomerDraft();
        ArgumentCaptor<CustomerCreatedEvent> eventCaptor = 
            ArgumentCaptor.forClass(CustomerCreatedEvent.class);
        
        // When
        CustomerResponse response = customerService.finalizeDraft(draft.getId());
        
        // Then
        assertThat(response.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        verify(customerCreatedEvent).fire(eventCaptor.capture());
        
        CustomerCreatedEvent event = eventCaptor.getValue();
        assertThat(event.getCustomerId()).isEqualTo(draft.getId());
        assertThat(event.isChainCustomer()).isTrue();
    }
}
```

## 📋 Frontend Unit Tests

### Store Tests

```typescript
// __tests__/stores/customerOnboardingStore.test.ts
import { act, renderHook } from '@testing-library/react';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';

describe('customerOnboardingStore', () => {
  beforeEach(() => {
    // Reset store before each test
    useCustomerOnboardingStore.getState().reset();
  });
  
  describe('Field Management', () => {
    it('should update field values and mark as dirty', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());
      
      act(() => {
        result.current.setFieldValue('companyName', 'Test GmbH');
      });
      
      expect(result.current.customerData.get('companyName')).toBe('Test GmbH');
      expect(result.current.isDirty).toBe(true);
    });
    
    it('should clear validation errors when field is updated', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());
      
      // Set initial error
      act(() => {
        result.current.validationErrors.set('email', 'Invalid email');
      });
      
      // Update field
      act(() => {
        result.current.setFieldValue('email', 'valid@example.com');
      });
      
      expect(result.current.validationErrors.has('email')).toBe(false);
    });
    
    it('should handle chain customer trigger correctly', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());
      
      // Add locations
      act(() => {
        result.current.addLocation({
          id: '1',
          locationType: 'hauptstandort',
          fieldValues: new Map(),
          detailedLocations: []
        });
      });
      
      expect(result.current.locations).toHaveLength(1);
      
      // Change chainCustomer to 'nein'
      act(() => {
        result.current.setFieldValue('chainCustomer', 'nein');
      });
      
      // Locations should be cleared
      expect(result.current.locations).toHaveLength(0);
    });
  });
  
  describe('Step Navigation', () => {
    it('should validate step before progression', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());
      
      // Try to validate empty step
      const isValid = await act(async () => {
        return await result.current.validateStep(0);
      });
      
      expect(isValid).toBe(false);
      expect(result.current.validationErrors.size).toBeGreaterThan(0);
    });
  });
  
  describe('Draft Management', () => {
    it('should persist draft state to sessionStorage', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());
      
      act(() => {
        result.current.setFieldValue('companyName', 'Test GmbH');
        result.current.draftId = 'test-draft-123';
      });
      
      // Check sessionStorage
      const stored = JSON.parse(
        sessionStorage.getItem('customer-onboarding-draft') || '{}'
      );
      
      expect(stored.state.draftId).toBe('test-draft-123');
      expect(stored.state.customerData).toContainEqual(['companyName', 'Test GmbH']);
    });
  });
});
```

### Component Tests

```typescript
// __tests__/components/DynamicFieldRenderer.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DynamicFieldRenderer } from '../../components/fields/DynamicFieldRenderer';
import { FieldDefinition } from '../../types/field.types';

describe('DynamicFieldRenderer', () => {
  const mockFields: FieldDefinition[] = [
    {
      key: 'companyName',
      label: 'Firmenname',
      fieldType: 'text',
      required: true,
      entityType: 'customer',
      isCustom: false
    },
    {
      key: 'industry',
      label: 'Branche',
      fieldType: 'select',
      required: true,
      options: ['hotel', 'krankenhaus', 'restaurant'],
      entityType: 'customer',
      isCustom: false
    }
  ];
  
  const defaultProps = {
    fields: mockFields,
    values: new Map(),
    errors: new Map(),
    onChange: jest.fn(),
    onBlur: jest.fn()
  };
  
  it('renders all fields correctly', () => {
    render(<DynamicFieldRenderer {...defaultProps} />);
    
    expect(screen.getByLabelText('Firmenname *')).toBeInTheDocument();
    expect(screen.getByLabelText('Branche *')).toBeInTheDocument();
  });
  
  it('displays field values', () => {
    const values = new Map([
      ['companyName', 'Test GmbH'],
      ['industry', 'hotel']
    ]);
    
    render(<DynamicFieldRenderer {...defaultProps} values={values} />);
    
    expect(screen.getByDisplayValue('Test GmbH')).toBeInTheDocument();
    expect(screen.getByDisplayValue('hotel')).toBeInTheDocument();
  });
  
  it('shows validation errors', () => {
    const errors = new Map([
      ['companyName', 'Firmenname ist erforderlich']
    ]);
    
    render(<DynamicFieldRenderer {...defaultProps} errors={errors} />);
    
    expect(screen.getByText('Firmenname ist erforderlich')).toBeInTheDocument();
  });
});
```

## 🔧 Test Patterns & Best Practices

### Mocking Strategies

```java
// Backend Mocking
@QuarkusTest
class ServiceTest {
    @InjectMock
    Repository repository;
    
    @BeforeEach
    void setup() {
        when(repository.findById(any())).thenReturn(Optional.of(entity));
    }
}
```

```typescript
// Frontend Mocking
jest.mock('../../services/customerApi', () => ({
  customerApi: {
    createDraft: jest.fn().mockResolvedValue({ id: '123' })
  }
}));
```

### Test Data Builders

```java
// Backend
public class CustomerTestDataBuilder {
    public static Customer aCustomer() {
        return Customer.builder()
            .id(UUID.randomUUID())
            .customerNumber("C-2025-001")
            .status(CustomerStatus.ACTIVE)
            .build();
    }
}
```

```typescript
// Frontend
export const createMockFieldDefinition = (
  overrides: Partial<FieldDefinition> = {}
): FieldDefinition => ({
  key: 'testField',
  label: 'Test Field',
  fieldType: 'text',
  required: false,
  entityType: 'customer',
  isCustom: false,
  ...overrides
});
```

## 📚 Weiterführende Links

- [Integration Tests →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/02-integration-tests.md)
- [Test Coverage Reports](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/04-performance-tests.md#coverage-reports)
- [CI/CD Integration](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md#ci-monitoring)