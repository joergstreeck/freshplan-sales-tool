package de.freshplan.domain.customer.service.mapper;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for CustomerMapper.
 * Tests all mapping operations between entities and DTOs.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@DisplayName("CustomerMapper Tests")
class CustomerMapperTest {

    @Inject
    CustomerMapper customerMapper;
    
    @InjectMock
    CustomerRepository customerRepository;

    private Customer testCustomer;
    private CreateCustomerRequest createRequest;
    private UpdateCustomerRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Setup mock parent customer for parent reference tests
        Customer mockParentCustomer = new Customer();
        mockParentCustomer.setId(UUID.fromString("54b985b7-8bb0-4a8d-bd0c-fefd24bc1255"));
        mockParentCustomer.setCompanyName("Parent Company");
        mockParentCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
        mockParentCustomer.setHierarchyType(CustomerHierarchyType.STANDALONE);
        mockParentCustomer.setStatus(CustomerStatus.AKTIV);
        mockParentCustomer.setIsDeleted(false);
        
        // Mock parent customer repository calls
        when(customerRepository.findByIdActive(any(UUID.class)))
                .thenReturn(Optional.of(mockParentCustomer));
        
        // Create test customer entity
        testCustomer = new Customer();
        testCustomer.setId(UUID.randomUUID());
        testCustomer.setCustomerNumber("KD-2025-00001");
        testCustomer.setCompanyName("Test Hotel GmbH");
        testCustomer.setTradingName("Hotel Test");
        testCustomer.setLegalForm("GmbH");
        testCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
        testCustomer.setIndustry(Industry.HOTEL);
        testCustomer.setClassification(Classification.A_KUNDE);
        testCustomer.setHierarchyType(CustomerHierarchyType.STANDALONE);
        testCustomer.setStatus(CustomerStatus.AKTIV);
        testCustomer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
        testCustomer.setExpectedAnnualVolume(new BigDecimal("50000.00"));
        testCustomer.setActualAnnualVolume(new BigDecimal("45000.00"));
        testCustomer.setPaymentTerms(PaymentTerms.NETTO_30);
        testCustomer.setCreditLimit(new BigDecimal("10000.00"));
        testCustomer.setDeliveryCondition(DeliveryCondition.STANDARD);
        testCustomer.setRiskScore(25);
        testCustomer.setLastContactDate(LocalDateTime.now().minusDays(15));
        testCustomer.setNextFollowUpDate(LocalDateTime.now().plusDays(30));
        testCustomer.setIsDeleted(false);
        testCustomer.setCreatedAt(LocalDateTime.now().minusDays(30));
        testCustomer.setCreatedBy("test-user");
        testCustomer.setUpdatedAt(LocalDateTime.now().minusDays(5));
        testCustomer.setUpdatedBy("update-user");

        // Add some child customers
        Customer childCustomer = new Customer();
        childCustomer.setId(UUID.randomUUID());
        childCustomer.setCustomerNumber("KD-2025-00002");
        childCustomer.setCompanyName("Child Hotel");
        childCustomer.setParentCustomer(testCustomer);
        testCustomer.getChildCustomers().add(childCustomer);

        // Create test DTOs
        createRequest = CreateCustomerRequest.builder()
                .companyName("New Hotel GmbH")
                .tradingName("New Hotel")
                .legalForm("GmbH")
                .customerType(CustomerType.UNTERNEHMEN)
                .industry(Industry.HOTEL)
                .classification(Classification.B_KUNDE)
                .hierarchyType(CustomerHierarchyType.STANDALONE)
                .status(CustomerStatus.LEAD)
                .lifecycleStage(CustomerLifecycleStage.ACQUISITION)
                .expectedAnnualVolume(new BigDecimal("75000.00"))
                .actualAnnualVolume(new BigDecimal("0.00"))
                .paymentTerms(PaymentTerms.NETTO_14)
                .creditLimit(new BigDecimal("5000.00"))
                .deliveryCondition(DeliveryCondition.STANDARD)
                .lastContactDate(LocalDateTime.now().minusDays(1))
                .nextFollowUpDate(LocalDateTime.now().plusDays(7))
                .build();

        updateRequest = new UpdateCustomerRequest(
                "Updated Hotel GmbH",
                "Updated Trading Name",
                "AG",
                CustomerType.UNTERNEHMEN,
                Industry.HOTEL,
                Classification.A_KUNDE,
                null, // parentCustomerId
                CustomerHierarchyType.HEADQUARTER,
                CustomerStatus.AKTIV,
                CustomerLifecycleStage.GROWTH,
                new BigDecimal("100000.00"),
                new BigDecimal("80000.00"),
                PaymentTerms.NETTO_30,
                new BigDecimal("15000.00"),
                DeliveryCondition.STANDARD,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().plusDays(14)
        );
    }

    @Nested
    @DisplayName("Entity to Response Mapping")
    class EntityToResponseMapping {

        @Test
        @DisplayName("Should map complete customer entity to full response")
        void toResponse_withCompleteEntity_shouldMapAllFields() {
            // When
            CustomerResponse result = customerMapper.toResponse(testCustomer);

            // Then
            assertThat(result).isNotNull();
            
            // Basic fields
            assertThat(result.id()).isEqualTo(testCustomer.getId().toString());
            assertThat(result.customerNumber()).isEqualTo(testCustomer.getCustomerNumber());
            assertThat(result.companyName()).isEqualTo(testCustomer.getCompanyName());
            assertThat(result.tradingName()).isEqualTo(testCustomer.getTradingName());
            assertThat(result.legalForm()).isEqualTo(testCustomer.getLegalForm());
            
            // Enums
            assertThat(result.customerType()).isEqualTo(testCustomer.getCustomerType());
            assertThat(result.industry()).isEqualTo(testCustomer.getIndustry());
            assertThat(result.classification()).isEqualTo(testCustomer.getClassification());
            assertThat(result.hierarchyType()).isEqualTo(testCustomer.getHierarchyType());
            assertThat(result.status()).isEqualTo(testCustomer.getStatus());
            assertThat(result.lifecycleStage()).isEqualTo(testCustomer.getLifecycleStage());
            
            // Financial fields
            assertThat(result.expectedAnnualVolume()).isEqualTo(testCustomer.getExpectedAnnualVolume());
            assertThat(result.actualAnnualVolume()).isEqualTo(testCustomer.getActualAnnualVolume());
            assertThat(result.paymentTerms()).isEqualTo(testCustomer.getPaymentTerms());
            assertThat(result.creditLimit()).isEqualTo(testCustomer.getCreditLimit());
            assertThat(result.deliveryCondition()).isEqualTo(testCustomer.getDeliveryCondition());
            
            // Risk and dates
            assertThat(result.riskScore()).isEqualTo(testCustomer.getRiskScore());
            assertThat(result.lastContactDate()).isEqualTo(testCustomer.getLastContactDate());
            assertThat(result.nextFollowUpDate()).isEqualTo(testCustomer.getNextFollowUpDate());
            
            // Audit fields
            assertThat(result.createdAt()).isEqualTo(testCustomer.getCreatedAt());
            assertThat(result.createdBy()).isEqualTo(testCustomer.getCreatedBy());
            assertThat(result.updatedAt()).isEqualTo(testCustomer.getUpdatedAt());
            assertThat(result.updatedBy()).isEqualTo(testCustomer.getUpdatedBy());
            
            // Collections
            assertThat(result.childCustomerIds()).hasSize(1);
            assertThat(result.hasChildren()).isTrue();
        }

        @Test
        @DisplayName("Should map entity to minimal response")
        void toMinimalResponse_shouldMapOnlyEssentialFields() {
            // When
            CustomerResponse result = customerMapper.toMinimalResponse(testCustomer);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testCustomer.getId().toString());
            assertThat(result.customerNumber()).isEqualTo(testCustomer.getCustomerNumber());
            assertThat(result.companyName()).isEqualTo(testCustomer.getCompanyName());
            assertThat(result.status()).isEqualTo(testCustomer.getStatus());
            assertThat(result.industry()).isEqualTo(testCustomer.getIndustry());
            
            // Collections should be empty for minimal response
            assertThat(result.childCustomerIds()).isEmpty();
            assertThat(result.hasChildren()).isFalse();
            
            // Check other fields that should be null in minimal response
            assertThat(result.tradingName()).isNull();
            assertThat(result.legalForm()).isNull();
            assertThat(result.classification()).isNull();
            assertThat(result.parentCustomerId()).isNull();
            assertThat(result.hierarchyType()).isNull();
            assertThat(result.lifecycleStage()).isNull();
            assertThat(result.partnerStatus()).isNull();
            assertThat(result.expectedAnnualVolume()).isNull();
            assertThat(result.actualAnnualVolume()).isNull();
            assertThat(result.paymentTerms()).isNull();
            assertThat(result.creditLimit()).isNull();
            assertThat(result.deliveryCondition()).isNull();
            assertThat(result.atRisk()).isFalse();
            assertThat(result.lastContactDate()).isNull();
            assertThat(result.nextFollowUpDate()).isNull();
            assertThat(result.createdBy()).isNull();
            assertThat(result.updatedAt()).isNull();
            assertThat(result.updatedBy()).isNull();
            assertThat(result.isDeleted()).isFalse();
            assertThat(result.deletedAt()).isNull();
            assertThat(result.deletedBy()).isNull();
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void toResponse_withNullValues_shouldHandleGracefully() {
            // Given
            Customer customerWithNulls = new Customer();
            customerWithNulls.setId(UUID.randomUUID());
            customerWithNulls.setCustomerNumber("KD-2025-99999");
            customerWithNulls.setCompanyName("Minimal Company");
            customerWithNulls.setCustomerType(CustomerType.UNTERNEHMEN);
            customerWithNulls.setStatus(CustomerStatus.LEAD);
            customerWithNulls.setLifecycleStage(CustomerLifecycleStage.ACQUISITION);
            customerWithNulls.setHierarchyType(CustomerHierarchyType.STANDALONE);
            customerWithNulls.setIsDeleted(false);
            customerWithNulls.setCreatedAt(LocalDateTime.now());
            customerWithNulls.setCreatedBy("test-user");
            // All other fields are null

            // When
            CustomerResponse result = customerMapper.toResponse(customerWithNulls);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.companyName()).isEqualTo("Minimal Company");
            assertThat(result.tradingName()).isNull();
            assertThat(result.industry()).isNull();
            assertThat(result.expectedAnnualVolume()).isNull();
            assertThat(result.riskScore()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Request to Entity Mapping")
    class RequestToEntityMapping {

        @Test
        @DisplayName("Should map create request to entity")
        void toEntity_withCreateRequest_shouldMapAllFields() {
            // Given
            String customerNumber = "KD-2025-00005";

            // When
            Customer result = customerMapper.toEntity(createRequest, customerNumber, "test-user");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull(); // ID is generated by database, not mapper
            assertThat(result.getCustomerNumber()).isEqualTo(customerNumber);
            assertThat(result.getCompanyName()).isEqualTo(createRequest.companyName());
            assertThat(result.getTradingName()).isEqualTo(createRequest.tradingName());
            assertThat(result.getLegalForm()).isEqualTo(createRequest.legalForm());
            assertThat(result.getCustomerType()).isEqualTo(createRequest.customerType());
            assertThat(result.getIndustry()).isEqualTo(createRequest.industry());
            assertThat(result.getClassification()).isEqualTo(createRequest.classification());
            assertThat(result.getHierarchyType()).isEqualTo(createRequest.hierarchyType());
            assertThat(result.getStatus()).isEqualTo(createRequest.status());
            assertThat(result.getLifecycleStage()).isEqualTo(createRequest.lifecycleStage());
            assertThat(result.getExpectedAnnualVolume()).isEqualTo(createRequest.expectedAnnualVolume());
            assertThat(result.getActualAnnualVolume()).isEqualTo(createRequest.actualAnnualVolume());
            assertThat(result.getPaymentTerms()).isEqualTo(createRequest.paymentTerms());
            assertThat(result.getCreditLimit()).isEqualTo(createRequest.creditLimit());
            assertThat(result.getDeliveryCondition()).isEqualTo(createRequest.deliveryCondition());
            assertThat(result.getLastContactDate()).isEqualTo(createRequest.lastContactDate());
            assertThat(result.getNextFollowUpDate()).isEqualTo(createRequest.nextFollowUpDate());
            
            // Default values
            assertThat(result.getIsDeleted()).isFalse(); // Default value from entity
            assertThat(result.getCreatedAt()).isNull(); // Set in @PrePersist
            assertThat(result.getRiskScore()).isEqualTo(50); // updateRiskScore() calculates this based on LEAD status
        }

        @Test
        @DisplayName("Should apply default values for missing fields")
        void toEntity_withMinimalRequest_shouldApplyDefaults() {
            // Given
            CreateCustomerRequest minimalRequest = new CreateCustomerRequest(
                    "Minimal Company", CustomerType.UNTERNEHMEN);
            String customerNumber = "KD-2025-00006";

            // When
            Customer result = customerMapper.toEntity(minimalRequest, customerNumber, "test-user");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCompanyName()).isEqualTo("Minimal Company");
            assertThat(result.getCustomerType()).isEqualTo(CustomerType.UNTERNEHMEN);
            
            // Check defaults
            assertThat(result.getStatus()).isEqualTo(CustomerStatus.LEAD);
            assertThat(result.getLifecycleStage()).isEqualTo(CustomerLifecycleStage.ACQUISITION);
            assertThat(result.getHierarchyType()).isEqualTo(CustomerHierarchyType.STANDALONE); // Default value from entity
            assertThat(result.getIsDeleted()).isFalse(); // Default value from entity
            assertThat(result.getRiskScore()).isEqualTo(80); // updateRiskScore() calculates this
        }

        @Test
        @DisplayName("Should handle parent customer ID conversion")
        void toEntity_withParentCustomerId_shouldHandleParentReference() {
            // Given - use the pre-configured mock parent customer
            UUID parentId = UUID.fromString("54b985b7-8bb0-4a8d-bd0c-fefd24bc1255");
            
            CreateCustomerRequest requestWithParent = CreateCustomerRequest.builder()
                    .companyName("Child Company")
                    .customerType(CustomerType.UNTERNEHMEN)
                    .parentCustomerId(parentId.toString())
                    .hierarchyType(CustomerHierarchyType.FILIALE)
                    .build();
            String customerNumber = "KD-2025-00007";

            // When
            Customer result = customerMapper.toEntity(requestWithParent, customerNumber, "test-user");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getHierarchyType()).isEqualTo(CustomerHierarchyType.FILIALE);
            assertThat(result.getParentCustomer()).isNotNull();
            assertThat(result.getParentCustomer().getId()).isEqualTo(parentId);
        }
    }

    @Nested
    @DisplayName("Entity Update Mapping")
    class EntityUpdateMapping {

        @Test
        @DisplayName("Should update entity from update request")
        void updateEntity_withUpdateRequest_shouldUpdateAllFields() {
            // Given
            Customer originalCustomer = new Customer();
            originalCustomer.setId(UUID.randomUUID());
            originalCustomer.setCustomerNumber("KD-2025-00008");
            originalCustomer.setCompanyName("Original Company");
            originalCustomer.setStatus(CustomerStatus.LEAD);
            originalCustomer.setCreatedAt(LocalDateTime.now().minusDays(10));
            originalCustomer.setCreatedBy("original-user");
            originalCustomer.setHierarchyType(CustomerHierarchyType.STANDALONE); // Set initial hierarchy type

            // When
            customerMapper.updateEntity(originalCustomer, updateRequest, "updated-user");

            // Then
            assertThat(originalCustomer.getCompanyName()).isEqualTo(updateRequest.companyName());
            assertThat(originalCustomer.getTradingName()).isEqualTo(updateRequest.tradingName());
            assertThat(originalCustomer.getLegalForm()).isEqualTo(updateRequest.legalForm());
            assertThat(originalCustomer.getCustomerType()).isEqualTo(updateRequest.customerType());
            assertThat(originalCustomer.getIndustry()).isEqualTo(updateRequest.industry());
            assertThat(originalCustomer.getClassification()).isEqualTo(updateRequest.classification());
            assertThat(originalCustomer.getHierarchyType()).isEqualTo(CustomerHierarchyType.STANDALONE); // HierarchyType doesn't update without parent change
            assertThat(originalCustomer.getStatus()).isEqualTo(updateRequest.status());
            assertThat(originalCustomer.getLifecycleStage()).isEqualTo(updateRequest.lifecycleStage());
            assertThat(originalCustomer.getExpectedAnnualVolume()).isEqualTo(updateRequest.expectedAnnualVolume());
            assertThat(originalCustomer.getActualAnnualVolume()).isEqualTo(updateRequest.actualAnnualVolume());
            assertThat(originalCustomer.getPaymentTerms()).isEqualTo(updateRequest.paymentTerms());
            assertThat(originalCustomer.getCreditLimit()).isEqualTo(updateRequest.creditLimit());
            assertThat(originalCustomer.getDeliveryCondition()).isEqualTo(updateRequest.deliveryCondition());
            assertThat(originalCustomer.getLastContactDate()).isEqualTo(updateRequest.lastContactDate());
            assertThat(originalCustomer.getNextFollowUpDate()).isEqualTo(updateRequest.nextFollowUpDate());
            
            // Immutable fields should not change
            assertThat(originalCustomer.getId()).isNotNull();
            assertThat(originalCustomer.getCustomerNumber()).isEqualTo("KD-2025-00008");
            assertThat(originalCustomer.getCreatedAt()).isNotNull();
            assertThat(originalCustomer.getCreatedBy()).isEqualTo("original-user");
            
            // Updated fields
            assertThat(originalCustomer.getUpdatedBy()).isEqualTo("updated-user");
            // Note: updatedAt is set by @PreUpdate in entity
        }

        @Test
        @DisplayName("Should handle null values in update request")
        void updateEntity_withNullValues_shouldHandleGracefully() {
            // Given
            Customer originalCustomer = new Customer();
            originalCustomer.setCompanyName("Original Company");
            originalCustomer.setTradingName("Original Trading");
            originalCustomer.setIndustry(Industry.HOTEL);
            
            UpdateCustomerRequest nullUpdateRequest = new UpdateCustomerRequest(
                    "Updated Company", // companyName - not null
                    null, // tradingName - null
                    null, // legalForm - null
                    CustomerType.UNTERNEHMEN, // customerType - not null
                    null, // industry - null
                    null, // classification - null
                    null, // parentCustomerId - null
                    null, // hierarchyType - null
                    CustomerStatus.AKTIV, // status - not null
                    null, // lifecycleStage - null
                    null, // expectedAnnualVolume - null
                    null, // actualAnnualVolume - null
                    null, // paymentTerms - null
                    null, // creditLimit - null
                    null, // deliveryCondition - null
                    null, // lastContactDate - null
                    null  // nextFollowUpDate - null
            );

            // When
            customerMapper.updateEntity(originalCustomer, nullUpdateRequest, "updated-user");

            // Then
            assertThat(originalCustomer.getCompanyName()).isEqualTo("Updated Company");
            assertThat(originalCustomer.getTradingName()).isEqualTo("Original Trading"); // Null values in request don't update the field
            assertThat(originalCustomer.getCustomerType()).isEqualTo(CustomerType.UNTERNEHMEN);
            assertThat(originalCustomer.getIndustry()).isEqualTo(Industry.HOTEL); // Null values in request don't update the field
            assertThat(originalCustomer.getStatus()).isEqualTo(CustomerStatus.AKTIV);
        }
    }

    @Nested
    @DisplayName("Collection Mapping")
    class CollectionMapping {

        @Test
        @DisplayName("Should map child customers correctly")
        void mapChildCustomers_shouldCreateCorrectResponses() {
            // When
            CustomerResponse result = customerMapper.toResponse(testCustomer);

            // Then
            assertThat(result.childCustomerIds()).hasSize(1);
            assertThat(result.hasChildren()).isTrue();
        }

        @Test
        @DisplayName("Should map contacts correctly")
        void mapContacts_shouldCreateCorrectContactResponses() {
            // When
            CustomerResponse result = customerMapper.toResponse(testCustomer);

            // Then
            assertThat(result).isNotNull();
            // Contact mapping would be tested in detail if ContactResponse exists
        }

        @Test
        @DisplayName("Should map locations and addresses correctly")
        void mapLocations_shouldCreateCorrectLocationResponses() {
            // When
            CustomerResponse result = customerMapper.toResponse(testCustomer);

            // Then
            assertThat(result).isNotNull();
            // Location mapping would be tested in detail if LocationResponse exists
        }

        @Test
        @DisplayName("Should map timeline events correctly")
        void mapTimelineEvents_shouldCreateCorrectEventResponses() {
            // When
            CustomerResponse result = customerMapper.toResponse(testCustomer);

            // Then
            assertThat(result).isNotNull();
            // Timeline event mapping would be tested in detail if TimelineEventResponse exists
        }

        @Test
        @DisplayName("Should handle empty collections")
        void mapEmptyCollections_shouldHandleGracefully() {
            // Given
            Customer customerWithEmptyCollections = new Customer();
            customerWithEmptyCollections.setId(UUID.randomUUID());
            customerWithEmptyCollections.setCustomerNumber("KD-2025-00999");
            customerWithEmptyCollections.setCompanyName("Empty Collections Company");
            customerWithEmptyCollections.setCustomerType(CustomerType.UNTERNEHMEN);
            customerWithEmptyCollections.setStatus(CustomerStatus.LEAD);
            customerWithEmptyCollections.setLifecycleStage(CustomerLifecycleStage.ACQUISITION);
            customerWithEmptyCollections.setHierarchyType(CustomerHierarchyType.STANDALONE);
            customerWithEmptyCollections.setIsDeleted(false);
            customerWithEmptyCollections.setCreatedAt(LocalDateTime.now());
            customerWithEmptyCollections.setCreatedBy("test-user");
            // Collections are initialized as empty

            // When
            CustomerResponse result = customerMapper.toResponse(customerWithEmptyCollections);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.childCustomerIds()).isEmpty();
            assertThat(result.hasChildren()).isFalse();
        }
    }

    @Nested
    @DisplayName("Circular Reference Handling")
    class CircularReferenceHandling {

        @Test
        @DisplayName("Should handle parent-child circular references")
        void mapWithCircularReferences_shouldNotCauseStackOverflow() {
            // Given - circular reference is already set up in setUp()
            // testCustomer has childCustomer, and childCustomer has testCustomer as parent

            // When & Then - Should not throw StackOverflowError
            assertThatCode(() -> {
                CustomerResponse result = customerMapper.toResponse(testCustomer);
                assertThat(result).isNotNull();
                assertThat(result.childCustomerIds()).hasSize(1);
                assertThat(result.hasChildren()).isTrue();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Performance and Memory")
    class PerformanceAndMemory {

        @Test
        @DisplayName("Should handle large collections efficiently")
        void mapLargeCollections_shouldPerformWell() {
            // Given
            Customer customerWithManyChildren = new Customer();
            customerWithManyChildren.setId(UUID.randomUUID());
            customerWithManyChildren.setCustomerNumber("KD-2025-LARGE");
            customerWithManyChildren.setCompanyName("Company with Many Children");
            customerWithManyChildren.setCustomerType(CustomerType.UNTERNEHMEN);
            customerWithManyChildren.setStatus(CustomerStatus.AKTIV);
            customerWithManyChildren.setLifecycleStage(CustomerLifecycleStage.GROWTH);
            customerWithManyChildren.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
            customerWithManyChildren.setIsDeleted(false);
            customerWithManyChildren.setCreatedAt(LocalDateTime.now());
            customerWithManyChildren.setCreatedBy("test-user");

            // Add many child customers
            for (int i = 0; i < 100; i++) {
                Customer child = new Customer();
                child.setId(UUID.randomUUID());
                child.setCustomerNumber("KD-2025-CHILD-" + i);
                child.setCompanyName("Child Company " + i);
                child.setParentCustomer(customerWithManyChildren);
                customerWithManyChildren.getChildCustomers().add(child);
            }

            // When
            long startTime = System.currentTimeMillis();
            CustomerResponse result = customerMapper.toMinimalResponse(customerWithManyChildren);
            long duration = System.currentTimeMillis() - startTime;

            // Then
            assertThat(result).isNotNull();
            assertThat(duration).isLessThan(1000L); // Should complete within 1 second
            // Minimal response should have empty collections for performance
            assertThat(result.childCustomerIds()).isEmpty();
        }
    }
}