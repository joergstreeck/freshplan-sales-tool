# 🧪 FC-005 CUSTOMER MANAGEMENT - TEST STRATEGY

**Datum:** 26.07.2025  
**Version:** 1.0  
**Coverage-Ziel:** ≥ 80% für neue Features  

## 📋 Inhaltsverzeichnis

1. [Test-Pyramide](#test-pyramide)
2. [Unit Tests](#unit-tests)
3. [Integration Tests](#integration-tests)
4. [E2E Tests](#e2e-tests)
5. [Performance Tests](#performance-tests)
6. [Test Data Management](#test-data-management)
7. [CI/CD Integration](#cicd-integration)

---

## Test-Pyramide

```
         /\
        /E2E\        10% - Critical User Journeys
       /------\
      /  Integ  \    30% - API & DB Integration
     /------------\
    /   Unit Tests  \ 60% - Business Logic & Components
   /------------------\
```

### Coverage-Ziele pro Layer

| Layer | Minimum | Ziel | Fokus |
|-------|---------|------|-------|
| Unit | 80% | 90% | Business Logic, Validation |
| Integration | 70% | 80% | API Contracts, DB Operations |
| E2E | - | 100% | Critical Paths |
| Performance | - | 100% | Load Scenarios |

---

## Unit Tests

### Backend Unit Tests

#### Field Value Service Tests

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

#### Customer Service Tests

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

### Frontend Unit Tests

#### Store Tests

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

#### Component Tests

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
    },
    {
      key: 'expectedVolume',
      label: 'Erwartetes Volumen',
      fieldType: 'number',
      required: false,
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
    expect(screen.getByLabelText('Erwartetes Volumen')).toBeInTheDocument();
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
      ['companyName', 'Firmenname ist erforderlich'],
      ['industry', 'Bitte wählen Sie eine Branche']
    ]);
    
    render(<DynamicFieldRenderer {...defaultProps} errors={errors} />);
    
    expect(screen.getByText('Firmenname ist erforderlich')).toBeInTheDocument();
    expect(screen.getByText('Bitte wählen Sie eine Branche')).toBeInTheDocument();
  });
  
  it('calls onChange when field value changes', async () => {
    const onChange = jest.fn();
    const user = userEvent.setup();
    
    render(<DynamicFieldRenderer {...defaultProps} onChange={onChange} />);
    
    const input = screen.getByLabelText('Firmenname *');
    await user.type(input, 'New Company');
    
    expect(onChange).toHaveBeenCalledWith('companyName', 'New Company');
  });
  
  it('calls onBlur when field loses focus', async () => {
    const onBlur = jest.fn();
    const user = userEvent.setup();
    
    render(<DynamicFieldRenderer {...defaultProps} onBlur={onBlur} />);
    
    const input = screen.getByLabelText('Firmenname *');
    await user.click(input);
    await user.tab(); // Move focus away
    
    expect(onBlur).toHaveBeenCalledWith('companyName');
  });
  
  it('handles number field formatting', async () => {
    const onChange = jest.fn();
    const user = userEvent.setup();
    
    render(<DynamicFieldRenderer {...defaultProps} onChange={onChange} />);
    
    const numberInput = screen.getByLabelText('Erwartetes Volumen');
    await user.type(numberInput, '1234567');
    
    // Should format with thousand separators
    expect(onChange).toHaveBeenLastCalledWith('expectedVolume', 1234567);
  });
});
```

---

## Integration Tests

### API Integration Tests

```java
@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
class CustomerResourceIT {
    
    @Test
    @TestSecurity(user = "testuser", roles = "sales")
    void testCustomerDraftWorkflow() {
        // Step 1: Create draft
        CreateCustomerDraftRequest createRequest = buildCreateRequest();
        
        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/customers/draft")
            .then()
            .statusCode(200)
            .extract()
            .response();
            
        String draftId = createResponse.jsonPath().getString("id");
        assertThat(draftId).isNotNull();
        
        // Step 2: Update draft
        UpdateCustomerDraftRequest updateRequest = UpdateCustomerDraftRequest.builder()
            .fieldValues(List.of(
                new FieldValueRequest("contactEmail", "updated@example.com")
            ))
            .build();
            
        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/customers/draft/{id}", draftId)
            .then()
            .statusCode(200);
        
        // Step 3: Add location
        AddLocationRequest locationRequest = AddLocationRequest.builder()
            .locationType("hauptstandort")
            .fieldValues(List.of(
                new FieldValueRequest("roomCount", 50)
            ))
            .build();
            
        given()
            .contentType(ContentType.JSON)
            .body(locationRequest)
            .when()
            .post("/api/customers/draft/{id}/locations", draftId)
            .then()
            .statusCode(200);
        
        // Step 4: Finalize draft
        CustomerResponse finalResponse = given()
            .when()
            .post("/api/customers/draft/{id}/finalize", draftId)
            .then()
            .statusCode(200)
            .extract()
            .as(CustomerResponse.class);
            
        assertThat(finalResponse.getStatus()).isEqualTo("ACTIVE");
        assertThat(finalResponse.getFieldValues())
            .containsEntry("contactEmail", "updated@example.com");
    }
    
    @Test
    @TestSecurity(user = "testuser", roles = "admin")
    void testFieldDefinitionRetrieval() {
        // Test customer fields
        List<FieldDefinitionResponse> customerFields = given()
            .when()
            .get("/api/field-definitions/CUSTOMER")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", FieldDefinitionResponse.class);
            
        assertThat(customerFields)
            .extracting("key")
            .contains("companyName", "industry", "chainCustomer");
        
        // Test industry-specific location fields
        List<FieldDefinitionResponse> hotelFields = given()
            .queryParam("industry", "hotel")
            .when()
            .get("/api/field-definitions/LOCATION")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", FieldDefinitionResponse.class);
            
        assertThat(hotelFields)
            .extracting("key")
            .contains("roomCount", "starRating", "hasRestaurant");
    }
}
```

### Database Integration Tests

```java
@QuarkusTest
@TestProfile(DatabaseTestProfile.class)
@TestTransaction
class CustomerRepositoryIT {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    FieldValueRepository fieldValueRepository;
    
    @Test
    void testCustomerWithFieldValues() {
        // Create customer
        Customer customer = new Customer();
        customer.setStatus(CustomerStatus.DRAFT);
        customerRepository.persist(customer);
        
        // Add field values
        FieldValue companyName = createFieldValue(
            customer.getId(), 
            "companyName", 
            "Test GmbH"
        );
        FieldValue industry = createFieldValue(
            customer.getId(), 
            "industry", 
            "hotel"
        );
        
        fieldValueRepository.persist(companyName);
        fieldValueRepository.persist(industry);
        
        // Flush and clear to force DB read
        customerRepository.flush();
        customerRepository.getEntityManager().clear();
        
        // Retrieve and verify
        List<FieldValue> values = fieldValueRepository
            .findByEntity(customer.getId(), EntityType.CUSTOMER);
            
        assertThat(values).hasSize(2);
        assertThat(values)
            .extracting("fieldDefinitionId", "value")
            .containsExactlyInAnyOrder(
                tuple("companyName", "Test GmbH"),
                tuple("industry", "hotel")
            );
    }
    
    @Test
    void testCleanupOldDrafts() {
        // Create old and new drafts
        Customer oldDraft = createDraft(30); // 30 days old
        Customer newDraft = createDraft(1);  // 1 day old
        
        customerRepository.persist(oldDraft);
        customerRepository.persist(newDraft);
        customerRepository.flush();
        
        // Run cleanup
        customerRepository.cleanupOldDrafts(7); // Delete > 7 days
        
        // Verify
        assertThat(customerRepository.findById(oldDraft.getId())).isEmpty();
        assertThat(customerRepository.findById(newDraft.getId())).isPresent();
    }
}
```

---

## E2E Tests

### Playwright E2E Tests

```typescript
// e2e/customer-onboarding.spec.ts
import { test, expect } from '@playwright/test';
import { CustomerTestData } from './fixtures/customerTestData';

test.describe('Customer Onboarding Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Login
    await page.goto('/login');
    await page.fill('[name="username"]', 'testuser');
    await page.fill('[name="password"]', 'testpass');
    await page.click('button[type="submit"]');
    
    // Navigate to customer creation
    await page.goto('/customers/new');
  });
  
  test('complete customer creation flow', async ({ page }) => {
    // Step 1: Fill customer data
    await page.fill('[name="companyName"]', 'E2E Test GmbH');
    await page.selectOption('[name="industry"]', 'hotel');
    await page.selectOption('[name="chainCustomer"]', 'ja');
    await page.fill('[name="street"]', 'Teststraße 123');
    await page.fill('[name="postalCode"]', '12345');
    await page.fill('[name="city"]', 'Teststadt');
    await page.fill('[name="contactName"]', 'Max Mustermann');
    await page.fill('[name="contactEmail"]', 'max@test.de');
    await page.fill('[name="contactPhone"]', '0123456789');
    
    // Verify auto-save
    await page.waitForSelector('[data-testid="save-indicator"]:has-text("Gespeichert")');
    
    // Navigate to next step
    await page.click('button:has-text("Weiter")');
    
    // Step 2: Add locations (should be visible because chainCustomer = ja)
    await expect(page.locator('h2:has-text("Standorte")')).toBeVisible();
    
    await page.fill('[name="smallHotels"]', '2');
    await page.fill('[name="mediumHotels"]', '1');
    await page.check('[name="detailedLocations"]');
    
    await page.click('button:has-text("Weiter")');
    
    // Step 3: Add detailed locations
    await expect(page.locator('h2:has-text("Detaillierte Standorte")')).toBeVisible();
    
    // Add first location
    await page.click('button:has-text("Standort hinzufügen")');
    await page.fill('[name="locations[0].name"]', 'Hotel Hauptstadt');
    await page.fill('[name="locations[0].street"]', 'Hauptstraße 1');
    await page.fill('[name="locations[0].postalCode"]', '10115');
    await page.fill('[name="locations[0].city"]', 'Berlin');
    
    // Finalize
    await page.click('button:has-text("Abschließen")');
    
    // Verify success
    await expect(page.locator('[role="alert"]:has-text("Kunde erfolgreich angelegt")')).toBeVisible();
    await expect(page).toHaveURL(/\/customers\/[a-f0-9-]+$/);
  });
  
  test('draft recovery after browser refresh', async ({ page }) => {
    // Fill some data
    await page.fill('[name="companyName"]', 'Draft Test GmbH');
    await page.fill('[name="industry"]', 'restaurant');
    
    // Wait for auto-save
    await page.waitForSelector('[data-testid="save-indicator"]:has-text("Gespeichert")');
    
    // Refresh page
    await page.reload();
    
    // Verify draft recovery dialog
    await expect(page.locator('text="Entwurf fortsetzen?"')).toBeVisible();
    await page.click('button:has-text("Fortsetzen")');
    
    // Verify data restored
    await expect(page.locator('[name="companyName"]')).toHaveValue('Draft Test GmbH');
    await expect(page.locator('[name="industry"]')).toHaveValue('restaurant');
  });
  
  test('validation prevents progression', async ({ page }) => {
    // Try to proceed without filling required fields
    await page.click('button:has-text("Weiter")');
    
    // Verify validation errors
    await expect(page.locator('text="Firmenname ist erforderlich"')).toBeVisible();
    await expect(page.locator('text="Branche ist erforderlich"')).toBeVisible();
    
    // Verify still on step 1
    await expect(page.locator('[data-testid="stepper-step-0"]')).toHaveAttribute('aria-current', 'step');
  });
  
  test('chain customer controls location visibility', async ({ page }) => {
    // Fill minimum required fields
    await page.fill('[name="companyName"]', 'Test GmbH');
    await page.selectOption('[name="industry"]', 'hotel');
    
    // With chainCustomer = nein (default)
    await page.selectOption('[name="chainCustomer"]', 'nein');
    
    // Should not see locations tab
    await expect(page.locator('[data-testid="step-locations"]')).not.toBeVisible();
    
    // Change to chainCustomer = ja
    await page.selectOption('[name="chainCustomer"]', 'ja');
    
    // Should now see locations tab
    await expect(page.locator('[data-testid="step-locations"]')).toBeVisible();
  });
});
```

---

## Performance Tests

### JMeter Test Plan

```xml
<!-- customer-performance-test.jmx -->
<jmeterTestPlan>
  <hashTree>
    <TestPlan>
      <stringProp name="TestPlan.name">Customer Management Performance</stringProp>
      
      <!-- Thread Group: Concurrent Users -->
      <ThreadGroup>
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">60</stringProp>
        
        <!-- HTTP Request: Create Customer Draft -->
        <HTTPSamplerProxy>
          <stringProp name="HTTPSampler.path">/api/customers/draft</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <stringProp name="HTTPSampler.contentEncoding">UTF-8</stringProp>
          
          <!-- Response Assertion -->
          <ResponseAssertion>
            <stringProp name="Assertion.response_time_max">500</stringProp>
          </ResponseAssertion>
        </HTTPSamplerProxy>
        
        <!-- HTTP Request: Get Field Definitions -->
        <HTTPSamplerProxy>
          <stringProp name="HTTPSampler.path">/api/field-definitions/CUSTOMER</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          
          <ResponseAssertion>
            <stringProp name="Assertion.response_time_max">200</stringProp>
          </ResponseAssertion>
        </HTTPSamplerProxy>
      </ThreadGroup>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
```

### k6 Performance Tests

```javascript
// performance/customer-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '2m', target: 100 }, // Ramp up
    { duration: '5m', target: 100 }, // Stay at 100 users
    { duration: '2m', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests under 500ms
    errors: ['rate<0.1'],             // Error rate under 10%
  },
};

export default function() {
  // Get field definitions (cached)
  const fieldDefResponse = http.get(
    `${__ENV.BASE_URL}/api/field-definitions/CUSTOMER`
  );
  
  check(fieldDefResponse, {
    'field definitions status is 200': (r) => r.status === 200,
    'field definitions response time < 200ms': (r) => r.timings.duration < 200,
  });
  
  // Create customer draft
  const customerData = {
    fieldValues: [
      { fieldKey: 'companyName', value: `Perf Test ${Date.now()}` },
      { fieldKey: 'industry', value: 'hotel' },
      { fieldKey: 'chainCustomer', value: 'nein' },
    ],
  };
  
  const createResponse = http.post(
    `${__ENV.BASE_URL}/api/customers/draft`,
    JSON.stringify(customerData),
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  const success = check(createResponse, {
    'create draft status is 200': (r) => r.status === 200,
    'create draft response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  errorRate.add(!success);
  
  sleep(1); // Think time
}
```

---

## Test Data Management

### Test Data Factory

```java
@ApplicationScoped
public class CustomerTestDataFactory {
    
    @Inject
    CustomerService customerService;
    
    @Inject
    FieldValueService fieldValueService;
    
    public Customer createTestCustomer(String industry, boolean isChainCustomer) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("companyName", generateCompanyName());
        fieldValues.put("industry", industry);
        fieldValues.put("chainCustomer", isChainCustomer ? "ja" : "nein");
        fieldValues.put("street", faker.address().streetAddress());
        fieldValues.put("postalCode", generateGermanPostalCode());
        fieldValues.put("city", faker.address().city());
        fieldValues.put("contactName", faker.name().fullName());
        fieldValues.put("contactEmail", faker.internet().emailAddress());
        fieldValues.put("contactPhone", generateGermanPhone());
        
        CreateCustomerDraftRequest request = CreateCustomerDraftRequest.builder()
            .fieldValues(convertToFieldRequests(fieldValues))
            .build();
            
        CustomerDraftResponse draft = customerService.createDraft(request);
        
        if (isChainCustomer) {
            addTestLocations(draft.getId(), industry);
        }
        
        return customerService.finalizeDraft(draft.getId());
    }
    
    private void addTestLocations(UUID customerId, String industry) {
        int locationCount = faker.number().numberBetween(2, 5);
        
        for (int i = 0; i < locationCount; i++) {
            Map<String, Object> locationFields = generateLocationFields(industry);
            // Add location...
        }
    }
}
```

### Test Fixtures

```typescript
// fixtures/customerTestData.ts
export const CustomerTestData = {
  validHotelCustomer: {
    companyName: 'Test Hotel GmbH',
    legalForm: 'gmbh',
    industry: 'hotel',
    chainCustomer: 'ja',
    street: 'Hotelstraße 1',
    postalCode: '10115',
    city: 'Berlin',
    contactName: 'Max Mustermann',
    contactEmail: 'max@testhotel.de',
    contactPhone: '030123456789',
    expectedVolume: '500000',
    paymentMethod: 'rechnung'
  },
  
  validRestaurantCustomer: {
    companyName: 'Gasthaus zum Test',
    legalForm: 'einzelunternehmen',
    industry: 'restaurant',
    chainCustomer: 'nein',
    street: 'Restaurantweg 42',
    postalCode: '80331',
    city: 'München',
    contactName: 'Anna Schmidt',
    contactEmail: 'anna@gasthaus-test.de',
    contactPhone: '089987654321',
    expectedVolume: '150000',
    paymentMethod: 'barzahlung'
  },
  
  // Industry-specific location data
  hotelLocations: {
    small: { count: 3, avgRooms: 40 },
    medium: { count: 2, avgRooms: 100 },
    large: { count: 1, avgRooms: 250 }
  }
};
```

---

## CI/CD Integration

### GitHub Actions Workflow

```yaml
# .github/workflows/customer-tests.yml
name: Customer Management Tests

on:
  pull_request:
    paths:
      - 'backend/src/**/customer/**'
      - 'frontend/src/features/customers/**'
      - 'docs/features/FC-005-CUSTOMER-MANAGEMENT/**'

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Run Backend Unit Tests
        run: |
          cd backend
          ./mvnw test -Dtest="**/customer/**/*Test"
          
      - name: Run Backend Integration Tests
        run: |
          cd backend
          ./mvnw test -Dtest="**/customer/**/*IT"
          
      - name: Generate Coverage Report
        run: |
          cd backend
          ./mvnw jacoco:report
          
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml
          flags: backend-customer
          
  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          
      - name: Install Dependencies
        run: |
          cd frontend
          npm ci
          
      - name: Run Unit Tests
        run: |
          cd frontend
          npm run test:unit -- --coverage features/customers
          
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./frontend/coverage/lcov.info
          flags: frontend-customer
          
  e2e-tests:
    runs-on: ubuntu-latest
    needs: [backend-tests, frontend-tests]
    steps:
      - uses: actions/checkout@v3
      
      - name: Start Services
        run: |
          docker-compose -f docker-compose.test.yml up -d
          ./scripts/wait-for-services.sh
          
      - name: Run E2E Tests
        run: |
          cd frontend
          npm run test:e2e -- --spec customer-onboarding
          
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-results
          path: frontend/test-results/
```

### Test Quality Gates

```yaml
# sonar-project.properties
sonar.qualitygate.conditions=\
  coverage:overall >= 80,\
  bugs:new == 0,\
  vulnerabilities:new == 0,\
  code_smells:new <= 5,\
  duplicated_lines_density:new <= 3
```