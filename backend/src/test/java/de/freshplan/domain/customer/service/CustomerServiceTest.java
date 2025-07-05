package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.domain.customer.service.exception.*;
import de.freshplan.domain.customer.service.mapper.CustomerMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CustomerService.
 * Tests all business logic and service methods with proper mocking.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    @Inject
    CustomerService customerService;

    @InjectMock
    CustomerRepository customerRepository;

    @InjectMock
    CustomerNumberGeneratorService numberGenerator;

    @InjectMock
    CustomerMapper customerMapper;

    private Customer testCustomer;
    private CreateCustomerRequest validCreateRequest;
    private UpdateCustomerRequest validUpdateRequest;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        // Create test customer entity
        testCustomer = new Customer();
        testCustomer.setId(UUID.randomUUID());
        testCustomer.setCustomerNumber("KD-2025-00001");
        testCustomer.setCompanyName("Test Hotel GmbH");
        testCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
        testCustomer.setIndustry(Industry.HOTEL);
        testCustomer.setStatus(CustomerStatus.LEAD);
        testCustomer.setLifecycleStage(CustomerLifecycleStage.ACQUISITION);
        testCustomer.setIsDeleted(false);
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer.setCreatedBy("test-user");

        // Create valid request DTOs
        validCreateRequest = new CreateCustomerRequest(
                "Test Hotel GmbH",
                CustomerType.UNTERNEHMEN
        );

        validUpdateRequest = new UpdateCustomerRequest(
                "Updated Hotel GmbH",
                "Updated Trading Name",
                "GmbH",
                CustomerType.UNTERNEHMEN,
                Industry.HOTEL,
                Classification.A_KUNDE,
                null, // parentCustomerId
                CustomerHierarchyType.STANDALONE,
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

        // Create response DTO
        customerResponse = new CustomerResponse(
                testCustomer.getId().toString(),
                testCustomer.getCustomerNumber(),
                testCustomer.getCompanyName(),
                null, // tradingName
                null, // legalForm
                testCustomer.getCustomerType(),
                testCustomer.getIndustry(),
                null, // classification
                null, // parentCustomerId
                testCustomer.getHierarchyType(),
                List.of(), // childCustomerIds
                false, // hasChildren
                testCustomer.getStatus(),
                testCustomer.getLifecycleStage(),
                null, // partnerStatus
                null, // expectedAnnualVolume
                null, // actualAnnualVolume
                null, // paymentTerms
                null, // creditLimit
                null, // deliveryCondition
                0, // riskScore
                false, // atRisk
                null, // lastContactDate
                null, // nextFollowUpDate
                testCustomer.getCreatedAt(),
                testCustomer.getCreatedBy(),
                null, // updatedAt
                null, // updatedBy
                testCustomer.getIsDeleted(),
                null, // deletedAt
                null  // deletedBy
        );
    }

    @Nested
    @DisplayName("Customer Creation")
    class CustomerCreation {

        @Test
        @DisplayName("Should create customer with valid data")
        void createCustomer_withValidData_shouldReturnCreatedCustomer() {
            // Given
            String generatedNumber = "KD-2025-00001";
            when(numberGenerator.generateNext()).thenReturn(generatedNumber);
            when(customerMapper.toEntity(eq(validCreateRequest), eq(generatedNumber), eq("test-user")))
                    .thenReturn(testCustomer);
            when(customerMapper.toResponse(testCustomer)).thenReturn(customerResponse);

            // When
            CustomerResponse result = customerService.createCustomer(validCreateRequest, "test-user");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.companyName()).isEqualTo("Test Hotel GmbH");
            assertThat(result.customerNumber()).isEqualTo("KD-2025-00001");
            
            verify(customerRepository).persist(testCustomer);
            verify(numberGenerator).generateNext();
            verify(customerMapper).toEntity(validCreateRequest, generatedNumber, "test-user");
            verify(customerMapper).toResponse(testCustomer);
        }

        @Test
        @DisplayName("Should throw exception for null request")
        void createCustomer_withNullRequest_shouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> customerService.createCustomer(null, "test-user"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("CreateCustomerRequest cannot be null");
        }
    }

    @Nested
    @DisplayName("Customer Retrieval")
    class CustomerRetrieval {

        @Test
        @DisplayName("Should find customer by ID")
        void findById_withValidId_shouldReturnCustomer() {
            // Given
            UUID customerId = testCustomer.getId();
            when(customerRepository.findByIdActive(customerId)).thenReturn(Optional.of(testCustomer));
            when(customerMapper.toResponse(testCustomer)).thenReturn(customerResponse);

            // When
            CustomerResponse result = customerService.getCustomer(customerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(customerId.toString());
            
            verify(customerRepository).findByIdActive(customerId);
            verify(customerMapper).toResponse(testCustomer);
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void findById_withNonExistentId_shouldThrowException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(customerRepository.findByIdActive(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> customerService.getCustomer(nonExistentId))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Customer Update")
    class CustomerUpdate {

        @Test
        @DisplayName("Should update customer with valid data")
        void updateCustomer_withValidData_shouldReturnUpdatedCustomer() {
            // Given
            UUID customerId = testCustomer.getId();
            when(customerRepository.findByIdActive(customerId)).thenReturn(Optional.of(testCustomer));
            when(customerMapper.toResponse(testCustomer)).thenReturn(customerResponse);

            // When
            CustomerResponse result = customerService.updateCustomer(customerId, validUpdateRequest, "test-user");

            // Then
            assertThat(result).isNotNull();
            
            verify(customerRepository).findByIdActive(customerId);
            verify(customerMapper).updateEntity(testCustomer, validUpdateRequest, "test-user");
            verify(customerMapper).toResponse(testCustomer);
        }
    }

    @Nested
    @DisplayName("Customer Deletion")
    class CustomerDeletion {

        @Test
        @DisplayName("Should soft delete customer without children")
        void deleteCustomer_withoutChildren_shouldSoftDelete() {
            // Given
            UUID customerId = testCustomer.getId();
            when(customerRepository.findByIdActive(customerId)).thenReturn(Optional.of(testCustomer));
            when(customerRepository.hasChildren(customerId)).thenReturn(false);

            // When
            customerService.deleteCustomer(customerId, "test-user", "Test deletion");

            // Then
            verify(customerRepository).findByIdActive(customerId);
            verify(customerRepository).hasChildren(customerId);
            assertThat(testCustomer.getIsDeleted()).isTrue();
            assertThat(testCustomer.getDeletedBy()).isEqualTo("test-user");
        }

        @Test
        @DisplayName("Should throw exception when customer has children")
        void deleteCustomer_withChildren_shouldThrowException() {
            // Given
            UUID customerId = testCustomer.getId();
            when(customerRepository.findByIdActive(customerId)).thenReturn(Optional.of(testCustomer));
            when(customerRepository.hasChildren(customerId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> customerService.deleteCustomer(customerId, "test-user", "Test deletion"))
                    .isInstanceOf(CustomerHasChildrenException.class)
                    .hasMessage("Cannot delete customer with children. Delete children first.");
        }
    }

    @Nested
    @DisplayName("Dashboard")
    class Dashboard {

        @Test
        @DisplayName("Should return dashboard data")
        void getDashboard_shouldReturnCorrectData() {
            // Given
            when(customerRepository.countActive()).thenReturn(150L);
            when(customerRepository.countByStatus(CustomerStatus.AKTIV)).thenReturn(120L);
            when(customerRepository.countNewThisMonth()).thenReturn(15L);
            when(customerRepository.countAtRisk(70)).thenReturn(8L);
            when(customerRepository.countOverdueFollowUps()).thenReturn(23L);

            // Mock status counts
            when(customerRepository.countByStatus(CustomerStatus.LEAD)).thenReturn(20L);
            when(customerRepository.countByStatus(CustomerStatus.PROSPECT)).thenReturn(35L);
            when(customerRepository.countByStatus(CustomerStatus.AKTIV)).thenReturn(120L);
            when(customerRepository.countByStatus(CustomerStatus.RISIKO)).thenReturn(8L);
            when(customerRepository.countByStatus(CustomerStatus.INAKTIV)).thenReturn(2L);
            when(customerRepository.countByStatus(CustomerStatus.ARCHIVIERT)).thenReturn(0L);

            // Mock lifecycle counts
            when(customerRepository.countByLifecycleStage(CustomerLifecycleStage.ACQUISITION)).thenReturn(55L);
            when(customerRepository.countByLifecycleStage(CustomerLifecycleStage.ONBOARDING)).thenReturn(20L);
            when(customerRepository.countByLifecycleStage(CustomerLifecycleStage.GROWTH)).thenReturn(60L);
            when(customerRepository.countByLifecycleStage(CustomerLifecycleStage.RETENTION)).thenReturn(10L);
            when(customerRepository.countByLifecycleStage(CustomerLifecycleStage.RECOVERY)).thenReturn(5L);

            // When
            CustomerDashboardResponse result = customerService.getDashboardData();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.totalCustomers()).isEqualTo(150L);
            assertThat(result.activeCustomers()).isEqualTo(120L);
            assertThat(result.newThisMonth()).isEqualTo(15L);
            assertThat(result.atRiskCount()).isEqualTo(8L);
            assertThat(result.upcomingFollowUps()).isEqualTo(23L);
            
            // Verify status counts
            assertThat(result.customersByStatus().get(CustomerStatus.LEAD)).isEqualTo(20L);
            assertThat(result.customersByStatus().get(CustomerStatus.AKTIV)).isEqualTo(120L);
            
            // Verify lifecycle counts
            assertThat(result.customersByLifecycle().get(CustomerLifecycleStage.ACQUISITION)).isEqualTo(55L);
            assertThat(result.customersByLifecycle().get(CustomerLifecycleStage.GROWTH)).isEqualTo(60L);
        }
    }
}