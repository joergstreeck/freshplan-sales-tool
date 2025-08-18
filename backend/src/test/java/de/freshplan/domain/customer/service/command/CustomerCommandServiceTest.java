package de.freshplan.domain.customer.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.CustomerNumberGeneratorService;
import de.freshplan.domain.customer.service.dto.CreateCustomerRequest;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.exception.CustomerAlreadyExistsException;
import de.freshplan.domain.customer.service.mapper.CustomerMapper;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CustomerCommandService. Verifies that the service behaves EXACTLY like
 * CustomerService.
 */
@QuarkusTest
class CustomerCommandServiceTest {

  @Inject CustomerCommandService commandService;

  @InjectMock CustomerRepository customerRepository;

  @InjectMock CustomerNumberGeneratorService numberGenerator;

  @InjectMock CustomerMapper customerMapper;

  private CreateCustomerRequest validRequest;
  private Customer mockCustomer;
  private CustomerResponse mockResponse;

  @BeforeEach
  void setUp() {
    // Setup mock customer using TestDataFactory
    mockCustomer =
        CustomerTestDataFactory.builder()
            .withCustomerNumber("KD-2025-00001")
            .withCompanyName("[TEST] Test Company GmbH")
            .build();
    mockCustomer.setId(UUID.randomUUID());

    // Mock response will be created by the mapper
  }

  private CreateCustomerRequest createValidRequest() {
    return CreateCustomerRequest.builder()
        .companyName("[TEST] Test Company GmbH")
        .customerType(CustomerType.NEUKUNDE)
        .industry(Industry.SONSTIGE)
        .expectedAnnualVolume(BigDecimal.ZERO)
        .build();
  }

  @Test
  void createCustomer_withValidRequest_shouldSucceed() {
    // Given
    CreateCustomerRequest request = createValidRequest();

    // Create real CustomerResponse using the short constructor
    CustomerResponse response =
        new CustomerResponse(
            mockCustomer.getId().toString(),
            "KD-2025-00001",
            "[TEST] Test Company GmbH",
            CustomerStatus.LEAD,
            CustomerType.NEUKUNDE,
            Industry.SONSTIGE,
            0, // riskScore
            null // lastContactDate
            );

    when(customerRepository.findPotentialDuplicates("[TEST] Test Company GmbH"))
        .thenReturn(Collections.emptyList());
    when(numberGenerator.generateNext()).thenReturn("KD-2025-00001");
    when(customerMapper.toEntity(any(), anyString(), anyString())).thenReturn(mockCustomer);
    when(customerMapper.toResponse(any())).thenReturn(response);

    // When
    CustomerResponse result = commandService.createCustomer(request, "testuser");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.customerNumber()).isEqualTo("KD-2025-00001");
    assertThat(result.companyName()).isEqualTo("[TEST] Test Company GmbH");

    // Verify interactions
    verify(customerRepository).findPotentialDuplicates("[TEST] Test Company GmbH");
    verify(numberGenerator).generateNext();
    verify(customerMapper).toEntity(request, "KD-2025-00001", "testuser");
    verify(customerRepository).persist(mockCustomer);
    verify(customerMapper).toResponse(mockCustomer);
  }

  @Test
  void createCustomer_withDuplicateName_shouldThrowException() {
    // Given
    CreateCustomerRequest request = createValidRequest();

    Customer existingCustomer =
        CustomerTestDataFactory.builder().withCompanyName("[TEST] Test Company GmbH").build();

    when(customerRepository.findPotentialDuplicates("[TEST] Test Company GmbH"))
        .thenReturn(List.of(existingCustomer));

    // When & Then
    assertThatThrownBy(() -> commandService.createCustomer(request, "testuser"))
        .isInstanceOf(CustomerAlreadyExistsException.class)
        .hasMessageContaining("Customer with similar company name already exists");

    // Verify no customer was created
    verify(customerRepository, never()).persist(any(Customer.class));
    verify(numberGenerator, never()).generateNext();
  }

  @Test
  void createCustomer_withNullRequest_shouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> commandService.createCustomer(null, "testuser"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("CreateCustomerRequest cannot be null");
  }

  @Test
  void createCustomer_withNullCreatedBy_shouldThrowException() {
    // Given
    CreateCustomerRequest request = createValidRequest();

    // When & Then
    assertThatThrownBy(() -> commandService.createCustomer(request, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("createdBy cannot be null or empty");
  }
}
