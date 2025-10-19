package de.freshplan.domain.customer.service.mapper;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Customer entities and DTOs. Handles all transformations with proper
 * validation and error handling.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CustomerMapper {

  private final CustomerRepository customerRepository;

  @Inject
  public CustomerMapper(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  // ========== ENTITY TO DTO MAPPING ==========

  /**
   * Maps a Customer entity to a CustomerResponse DTO. Includes all customer data and hierarchy
   * information.
   *
   * @param customer The customer entity
   * @return The customer response DTO
   */
  public CustomerResponse toResponse(Customer customer) {
    if (customer == null) {
      return null;
    }

    // Map child customer IDs
    List<String> childIds =
        customer.getChildCustomers().stream()
            .map(child -> child.getId().toString())
            .collect(Collectors.toList());

    return new CustomerResponse(
        customer.getId().toString(),
        customer.getCustomerNumber(),
        customer.getCompanyName(),
        customer.getTradingName(),
        customer.getLegalForm(),
        customer.getCustomerType(),
        customer.getBusinessType(),
        customer.getClassification(),
        customer.getParentCustomer() != null
            ? customer.getParentCustomer().getId().toString()
            : null,
        customer.getHierarchyType(),
        childIds,
        customer.hasChildren(),
        customer.getStatus(),
        customer.getLifecycleStage(),
        customer.getPartnerStatus(),
        customer.getExpectedAnnualVolume(),
        customer.getActualAnnualVolume(),
        customer.getPaymentTerms(),
        customer.getCreditLimit(),
        customer.getDeliveryCondition(),
        customer.getRiskScore(),
        customer.isAtRisk(),
        customer.getLastContactDate(),
        customer.getNextFollowUpDate(),

        // Chain Structure - NEW for Sprint 2
        customer.getTotalLocationsEU(),
        customer.getLocationsGermany(),
        customer.getLocationsAustria(),
        customer.getLocationsSwitzerland(),
        customer.getLocationsRestEU(),
        customer.getExpansionPlanned(),

        // Business Model - NEW for Sprint 2
        customer.getPrimaryFinancing(),
        customer.getPainPoints(),

        // Contact Information
        customer.getActiveContactsCount(),
        customer.getCreatedAt(),
        customer.getCreatedBy(),
        customer.getUpdatedAt(),
        customer.getUpdatedBy(),
        customer.getIsDeleted(),
        customer.getDeletedAt(),
        customer.getDeletedBy());
  }

  /**
   * Maps a Customer entity to a minimal CustomerResponse for list views. Only includes essential
   * fields to reduce payload size.
   *
   * @param customer The customer entity
   * @return The minimal customer response DTO
   */
  public CustomerResponse toMinimalResponse(Customer customer) {
    if (customer == null) {
      return null;
    }

    // Extended minimal response to include fields needed for filtering
    return new CustomerResponse(
        customer.getId().toString(),
        customer.getCustomerNumber(),
        customer.getCompanyName(),
        null, // tradingName
        null, // legalForm
        customer.getCustomerType(),
        customer.getBusinessType(),
        null, // classification
        null, // parentCustomerId
        null, // hierarchyType
        List.of(), // childCustomerIds
        false, // hasChildren
        customer.getStatus(),
        null, // lifecycleStage
        null, // partnerStatus
        customer.getExpectedAnnualVolume(), // NEEDED FOR FILTERS
        null, // actualAnnualVolume
        null, // paymentTerms
        null, // creditLimit
        null, // deliveryCondition
        customer.getRiskScore(),
        customer.isAtRisk(),
        customer.getLastContactDate(), // NEEDED FOR FILTERS
        null, // nextFollowUpDate
        null, // totalLocationsEU
        null, // locationsGermany
        null, // locationsAustria
        null, // locationsSwitzerland
        null, // locationsRestEU
        null, // expansionPlanned
        null, // primaryFinancing
        List.of(), // painPoints

        // Contact Information - NEEDED FOR FILTERS
        customer.getActiveContactsCount(),
        customer.getCreatedAt(),
        null, // createdBy
        null, // updatedAt
        null, // updatedBy
        false, // isDeleted
        null, // deletedAt
        null); // deletedBy
  }

  /**
   * Maps a list of Customer entities to CustomerResponse DTOs.
   *
   * @param customers The customer entities
   * @return The customer response DTOs
   */
  public List<CustomerResponse> toResponseList(List<Customer> customers) {
    if (customers == null) {
      return List.of();
    }

    return customers.stream().map(this::toResponse).collect(Collectors.toList());
  }

  /**
   * Maps a list of Customer entities to minimal CustomerResponse DTOs. Optimized for list views
   * with large datasets.
   *
   * @param customers The customer entities
   * @return The minimal customer response DTOs
   */
  public List<CustomerResponse> toMinimalResponseList(List<Customer> customers) {
    if (customers == null) {
      return List.of();
    }

    return customers.stream().map(this::toMinimalResponse).collect(Collectors.toList());
  }

  // ========== DTO TO ENTITY MAPPING ==========

  /**
   * Maps a CreateCustomerRequest to a new Customer entity. Sets all provided fields and applies
   * defaults where needed.
   *
   * @param request The create request DTO
   * @param customerNumber The generated customer number
   * @param createdBy The user creating the customer
   * @return The new customer entity (not persisted)
   * @throws CustomerMapperException if mapping fails
   */
  public Customer toEntity(CreateCustomerRequest request, String customerNumber, String createdBy) {
    if (request == null) {
      throw new CustomerMapperException("CreateCustomerRequest cannot be null");
    }

    if (customerNumber == null || customerNumber.isBlank()) {
      throw new CustomerMapperException("Customer number cannot be null or blank");
    }

    if (createdBy == null || createdBy.isBlank()) {
      throw new CustomerMapperException("CreatedBy cannot be null or blank");
    }

    Customer customer = new Customer();

    // Basic information
    customer.setCustomerNumber(customerNumber);
    customer.setCompanyName(request.companyName());
    customer.setTradingName(request.tradingName());
    customer.setLegalForm(request.legalForm());

    // Classification with defaults
    customer.setCustomerType(request.customerType());
    customer.setBusinessType(request.businessType());
    customer.setClassification(request.classification());

    // Hierarchy
    if (request.parentCustomerId() != null && !request.parentCustomerId().isBlank()) {
      try {
        UUID parentId = UUID.fromString(request.parentCustomerId());
        Optional<Customer> parent = customerRepository.findByIdActive(parentId);
        if (parent.isPresent()) {
          customer.setParentCustomer(parent.get());
          customer.setHierarchyType(request.hierarchyType());
        } else {
          throw new CustomerMapperException("Parent customer not found: " + parentId);
        }
      } catch (IllegalArgumentException e) {
        throw new CustomerMapperException(
            "Invalid parent customer ID format: " + request.parentCustomerId());
      }
    }

    // Status & Lifecycle with defaults
    customer.setStatus(
        request.status() != null
            ? request.status()
            : de.freshplan.domain.customer.entity.CustomerStatus.LEAD);
    customer.setLifecycleStage(
        request.lifecycleStage() != null
            ? request.lifecycleStage()
            : de.freshplan.domain.customer.entity.CustomerLifecycleStage.ACQUISITION);

    // Financial information
    customer.setExpectedAnnualVolume(request.expectedAnnualVolume());
    customer.setActualAnnualVolume(request.actualAnnualVolume());
    customer.setPaymentTerms(request.paymentTerms());
    customer.setCreditLimit(request.creditLimit());
    customer.setDeliveryCondition(request.deliveryCondition());

    // Contact & Follow-up
    customer.setLastContactDate(request.lastContactDate());
    customer.setNextFollowUpDate(request.nextFollowUpDate());

    // Audit fields
    customer.setCreatedBy(createdBy);

    // Sprint 2 Fields - Set defaults
    customer.setLocationsGermany(1); // Default: at least one location in Germany
    customer.setLocationsAustria(0);
    customer.setLocationsSwitzerland(0);
    customer.setLocationsRestEU(0);
    customer.setTotalLocationsEU(1); // Total should match sum
    customer.setPainPoints(new java.util.ArrayList<>()); // Empty list as default
    customer.setPrimaryFinancing(
        de.freshplan.domain.customer.entity.FinancingType.PRIVATE); // Default financing
    customer.setExpansionPlanned(null); // Default: no expansion planned (nullable field)

    // Calculate initial risk score
    customer.updateRiskScore();

    return customer;
  }

  /**
   * Updates an existing Customer entity with data from UpdateCustomerRequest. Only updates fields
   * that are not null in the request (partial updates).
   *
   * @param customer The existing customer entity
   * @param request The update request DTO
   * @param updatedBy The user updating the customer
   * @throws CustomerMapperException if mapping fails
   */
  public void updateEntity(Customer customer, UpdateCustomerRequest request, String updatedBy) {
    if (customer == null) {
      throw new CustomerMapperException("Customer entity cannot be null");
    }

    if (request == null) {
      throw new CustomerMapperException("UpdateCustomerRequest cannot be null");
    }

    if (updatedBy == null || updatedBy.isBlank()) {
      throw new CustomerMapperException("UpdatedBy cannot be null or blank");
    }

    // Update only non-null fields (partial update)
    if (request.companyName() != null) {
      customer.setCompanyName(request.companyName());
    }
    if (request.tradingName() != null) {
      customer.setTradingName(request.tradingName());
    }
    if (request.legalForm() != null) {
      customer.setLegalForm(request.legalForm());
    }
    if (request.customerType() != null) {
      customer.setCustomerType(request.customerType());
    }
    if (request.businessType() != null) {
      customer.setBusinessType(request.businessType());
    }
    if (request.classification() != null) {
      customer.setClassification(request.classification());
    }

    // Handle parent customer update
    if (request.parentCustomerId() != null) {
      if (request.parentCustomerId().isBlank()) {
        // Remove parent (make standalone)
        customer.setParentCustomer(null);
        customer.setHierarchyType(
            de.freshplan.domain.customer.entity.CustomerHierarchyType.STANDALONE);
      } else {
        try {
          UUID parentId = UUID.fromString(request.parentCustomerId());
          Optional<Customer> parent = customerRepository.findByIdActive(parentId);
          if (parent.isPresent()) {
            // Validate no circular reference
            if (wouldCreateCircularReference(customer, parent.get())) {
              throw new CustomerMapperException("Update would create circular hierarchy reference");
            }
            customer.setParentCustomer(parent.get());
            customer.setHierarchyType(request.hierarchyType());
          } else {
            throw new CustomerMapperException("Parent customer not found: " + parentId);
          }
        } catch (IllegalArgumentException e) {
          throw new CustomerMapperException(
              "Invalid parent customer ID format: " + request.parentCustomerId());
        }
      }
    }

    if (request.status() != null) {
      customer.setStatus(request.status());
    }
    if (request.lifecycleStage() != null) {
      customer.setLifecycleStage(request.lifecycleStage());
    }
    if (request.expectedAnnualVolume() != null) {
      customer.setExpectedAnnualVolume(request.expectedAnnualVolume());
    }
    if (request.actualAnnualVolume() != null) {
      customer.setActualAnnualVolume(request.actualAnnualVolume());
    }
    if (request.paymentTerms() != null) {
      customer.setPaymentTerms(request.paymentTerms());
    }
    if (request.creditLimit() != null) {
      customer.setCreditLimit(request.creditLimit());
    }
    if (request.deliveryCondition() != null) {
      customer.setDeliveryCondition(request.deliveryCondition());
    }
    if (request.lastContactDate() != null) {
      customer.setLastContactDate(request.lastContactDate());
    }
    if (request.nextFollowUpDate() != null) {
      customer.setNextFollowUpDate(request.nextFollowUpDate());
    }

    // Update audit fields
    customer.setUpdatedBy(updatedBy);

    // Recalculate risk score after updates
    customer.updateRiskScore();
  }

  // ========== SPECIALIZED MAPPING METHODS ==========

  /**
   * Maps Customer entities to CustomerListResponse with pagination info.
   *
   * @param customers The customer entities
   * @param page Current page number
   * @param size Page size
   * @param totalElements Total number of elements
   * @param useMinimal Whether to use minimal response format
   * @return The paginated customer list response
   */
  public CustomerListResponse toListResponse(
      List<Customer> customers, int page, int size, long totalElements, boolean useMinimal) {

    List<CustomerResponse> customerResponses =
        useMinimal ? toMinimalResponseList(customers) : toResponseList(customers);

    return CustomerListResponse.of(customerResponses, page, size, totalElements);
  }

  /**
   * Maps Customer entities to CustomerListResponse with minimal data. Optimized for large lists and
   * better performance.
   *
   * @param customers The customer entities
   * @param page Current page number
   * @param size Page size
   * @param totalElements Total number of elements
   * @return The paginated customer list response with minimal data
   */
  public CustomerListResponse toMinimalListResponse(
      List<Customer> customers, int page, int size, long totalElements) {

    return toListResponse(customers, page, size, totalElements, true);
  }

  /**
   * Maps Customer entities to CustomerListResponse with full data.
   *
   * @param customers The customer entities
   * @param page Current page number
   * @param size Page size
   * @param totalElements Total number of elements
   * @return The paginated customer list response with full data
   */
  public CustomerListResponse toFullListResponse(
      List<Customer> customers, int page, int size, long totalElements) {

    return toListResponse(customers, page, size, totalElements, false);
  }

  // ========== VALIDATION HELPERS ==========

  /**
   * Checks if setting a new parent would create a circular reference.
   *
   * @param customer The customer to update
   * @param newParent The proposed new parent
   * @return true if it would create a circular reference
   */
  private boolean wouldCreateCircularReference(Customer customer, Customer newParent) {
    if (customer.getId().equals(newParent.getId())) {
      return true; // Customer cannot be its own parent
    }

    Customer current = newParent.getParentCustomer();
    while (current != null) {
      if (current.getId().equals(customer.getId())) {
        return true; // Circular reference detected
      }
      current = current.getParentCustomer();
    }

    return false;
  }

  /**
   * Validates that a UUID string is valid.
   *
   * @param uuidString The UUID string to validate
   * @return true if valid, false otherwise
   */
  public boolean isValidUUID(String uuidString) {
    if (uuidString == null || uuidString.isBlank()) {
      return false;
    }

    try {
      UUID.fromString(uuidString);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  // ========== EXCEPTION CLASS ==========

  /** Exception thrown when mapping operations fail. */
  public static class CustomerMapperException extends RuntimeException {
    public CustomerMapperException(String message) {
      super(message);
    }

    public CustomerMapperException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
