# Test Naming Convention für FreshPlan

## Einheitliche Standards für Test-Benennung

### 1. Klassen-Level: @DisplayName verwenden
```java
@DisplayName("Repository - Customer Basic Operations")
class CustomerRepositoryTest { }

@DisplayName("Service - User Management")
class UserServiceTest { }

@DisplayName("Resource - Contact Interactions API")
class ContactInteractionResourceIT { }
```

### 2. Methoden-Level: Beschreibende Namen
```java
@Test
@DisplayName("Should create customer with valid data")
void createCustomer_withValidData_shouldReturnCreatedCustomer() { }

@Test
@DisplayName("Should throw exception for duplicate email")
void createUser_withDuplicateEmail_shouldThrowException() { }
```

### 3. Naming Pattern für Methoden:
- **Pattern:** `methodUnderTest_condition_expectedResult`
- **Beispiele:**
  - `findByEmail_withExistingEmail_shouldReturnUser`
  - `deleteCustomer_withActiveOrders_shouldThrowException`
  - `calculateDiscount_withVipStatus_shouldApply20Percent`

### 4. @DisplayName Best Practices:
- Beginne mit "Should" für Test-Methoden
- Verwende business-verständliche Sprache
- Vermeide technische Implementation-Details
- Halte es kurz aber präzise

### 5. Test-Kategorien im DisplayName:
- **Unit Tests:** "Unit - [Component]"
- **Integration Tests:** "Integration - [Feature]"
- **Repository Tests:** "Repository - [Entity]"
- **Resource Tests:** "API - [Endpoint]"
- **Service Tests:** "Service - [Business Function]"

### 6. Parametrisierte Tests:
```java
@ParameterizedTest(name = "Stage {0} should have default probability")
@EnumSource(OpportunityStage.class)
@DisplayName("Should set correct default probability for each stage")
void validateDefaultProbabilities(OpportunityStage stage) { }
```

### 7. Nested Tests (wenn verwendet):
```java
@Nested
@DisplayName("When creating customers")
class CustomerCreation {
    @Test
    @DisplayName("Should validate required fields")
    void shouldValidateRequiredFields() { }
}
```

## Migration Strategy

1. **Phase 1:** Neue Tests folgen der Convention
2. **Phase 2:** Kritische Tests werden migriert
3. **Phase 3:** Schrittweise Migration aller Tests

## Beispiel-Migration

**Vorher:**
```java
@Test
void testCreateUser() { }
```

**Nachher:**
```java
@Test
@DisplayName("Should create user with valid data")
void createUser_withValidData_shouldReturnCreatedUser() { }
```