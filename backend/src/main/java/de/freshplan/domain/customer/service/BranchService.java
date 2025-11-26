package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.AddressType;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerAddress;
import de.freshplan.domain.customer.entity.CustomerHierarchyType;
import de.freshplan.domain.customer.entity.CustomerLocation;
import de.freshplan.domain.customer.entity.LocationCategory;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.CreateBranchRequest;
import de.freshplan.domain.customer.service.dto.CreateCustomerRequest;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.dto.CustomerResponseBuilder;
import de.freshplan.domain.customer.service.dto.UpdateCustomerRequest;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.customer.service.exception.InvalidHierarchyException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing branch (FILIALE) customers within a multi-location hierarchy.
 *
 * <p>Sprint 2.1.7.7 Multi-Location Management - D1
 *
 * <p>This service handles branch-specific operations: - Creating branches under a HEADQUARTER -
 * Listing branches by parent - Validating hierarchy integrity - Inheriting parent properties
 * (xentral_customer_id) - Creating primary Location with Address
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */
@ApplicationScoped
public class BranchService {

  private static final Logger log = LoggerFactory.getLogger(BranchService.class);

  private final CustomerRepository customerRepository;
  private final CustomerService customerService;
  private final EntityManager entityManager;

  @Inject
  public BranchService(
      CustomerRepository customerRepository,
      CustomerService customerService,
      EntityManager entityManager) {
    this.customerRepository = customerRepository;
    this.customerService = customerService;
    this.entityManager = entityManager;
  }

  // ========== BRANCH CRUD OPERATIONS ==========

  /**
   * Creates a new branch (FILIALE) under a headquarter customer.
   *
   * <p>Sprint 2.1.7.7: Extended to support Address and Contact from CreateBranchRequest
   *
   * <p>Behavior: - Sets hierarchyType to FILIALE - Links to parent HEADQUARTER - Copies
   * xentral_customer_id from parent - Creates primary Location with Address if provided - Validates
   * parent is HEADQUARTER
   *
   * @param headquarterId UUID of the parent HEADQUARTER customer
   * @param request Branch creation request with optional address/contact
   * @param createdBy User creating the branch
   * @return Created branch customer response
   * @throws CustomerNotFoundException if headquarter not found
   * @throws InvalidHierarchyException if parent is not HEADQUARTER
   */
  @Transactional
  public CustomerResponse createBranch(
      UUID headquarterId, @Valid CreateBranchRequest request, String createdBy) {
    log.debug("Creating branch for headquarter ID: {}", headquarterId);

    // 1. Validate parent exists and is HEADQUARTER
    Customer headquarter = validateHeadquarter(headquarterId);

    // 2. Build CreateCustomerRequest with FILIALE hierarchy and parent link
    CreateCustomerRequest customerRequest =
        CreateCustomerRequest.builder()
            .companyName(request.companyName())
            .tradingName(request.tradingName())
            .customerType(request.customerType())
            .businessType(request.businessType())
            .hierarchyType(CustomerHierarchyType.FILIALE) // Force FILIALE
            .parentCustomerId(headquarterId.toString()) // Link to parent
            .status(request.status())
            .expectedAnnualVolume(request.expectedAnnualVolume())
            .build();

    // 3. Create via CustomerService
    CustomerResponse branch = customerService.createCustomer(customerRequest, createdBy);

    // 4. Get the created branch entity
    UUID branchId = UUID.fromString(branch.id());
    Customer branchEntity =
        customerRepository
            .findByIdOptional(branchId)
            .orElseThrow(() -> new CustomerNotFoundException(branchId));

    // 5. Copy xentral_customer_id from parent
    branchEntity.setXentralCustomerId(headquarter.getXentralCustomerId());

    // 6. Create primary Location with Address if address data provided
    if (request.address() != null && hasAddressData(request.address())) {
      createPrimaryLocation(branchEntity, request, createdBy);
    }

    // 7. Persist changes
    customerRepository.persist(branchEntity);

    // 8. Refresh response with updated data
    branch = new CustomerResponseBuilder().fromEntity(branchEntity).build();

    log.info(
        "Created branch {} (ID: {}) under headquarter {} (ID: {}) with inherited xentralCustomerId:"
            + " {}",
        branch.companyName(),
        branch.id(),
        headquarter.getCompanyName(),
        headquarterId,
        branch.xentralCustomerId());

    return branch;
  }

  /**
   * Check if address data is present (not all null)
   *
   * @param address Address DTO
   * @return true if at least one address field is set
   */
  private boolean hasAddressData(CreateBranchRequest.AddressDto address) {
    return address.street() != null
        || address.postalCode() != null
        || address.city() != null
        || address.country() != null;
  }

  /**
   * Creates primary location with address for the branch.
   *
   * @param branch Branch customer entity
   * @param request Branch creation request with address/contact
   * @param createdBy User creating the location
   */
  private void createPrimaryLocation(
      Customer branch, CreateBranchRequest request, String createdBy) {
    log.debug("Creating primary location for branch: {}", branch.getCompanyName());

    // Create Location
    CustomerLocation location = new CustomerLocation();
    location.setCustomer(branch);
    location.setLocationName(branch.getCompanyName() + " - Hauptstandort");
    location.setCategory(LocationCategory.BRANCH_OFFICE);
    location.setIsMainLocation(true);
    location.setCreatedAt(LocalDateTime.now());
    location.setCreatedBy(createdBy);

    // Set contact info from request
    if (request.contact() != null) {
      location.setPhone(request.contact().phone());
      location.setEmail(request.contact().email());
    }

    entityManager.persist(location);

    // Create Address
    CreateBranchRequest.AddressDto addressDto = request.address();
    CustomerAddress address = new CustomerAddress();
    address.setLocation(location);
    address.setAddressType(AddressType.BILLING); // Primary billing address
    address.setStreet(addressDto.street());
    address.setPostalCode(addressDto.postalCode());
    address.setCity(addressDto.city() != null ? addressDto.city() : "Unbekannt");
    // Convert 2-letter country code to 3-letter if needed
    address.setCountry(convertCountryCode(addressDto.country()));
    address.setCreatedAt(LocalDateTime.now());
    address.setCreatedBy(createdBy);

    entityManager.persist(address);

    log.info(
        "Created primary location '{}' with address for branch {}",
        location.getLocationName(),
        branch.getCompanyName());
  }

  /**
   * Converts 2-letter ISO country code to 3-letter code.
   *
   * @param code 2-letter country code (e.g., "DE")
   * @return 3-letter country code (e.g., "DEU")
   */
  private String convertCountryCode(String code) {
    if (code == null) {
      return "DEU"; // Default to Germany
    }
    // Simple mapping for common codes - extend as needed
    return switch (code.toUpperCase()) {
      case "DE" -> "DEU";
      case "AT" -> "AUT";
      case "CH" -> "CHE";
      case "FR" -> "FRA";
      case "IT" -> "ITA";
      case "NL" -> "NLD";
      case "BE" -> "BEL";
      case "LU" -> "LUX";
      case "PL" -> "POL";
      case "CZ" -> "CZE";
      default -> code.length() == 3 ? code : "DEU";
    };
  }

  /**
   * Lists all branches (FILIALE) belonging to a headquarter.
   *
   * @param headquarterId UUID of the parent HEADQUARTER customer
   * @return List of branch customer responses
   * @throws CustomerNotFoundException if headquarter not found
   */
  @Transactional
  public List<CustomerResponse> getBranchesByHeadquarter(UUID headquarterId) {
    log.debug("Fetching branches for headquarter ID: {}", headquarterId);

    // Validate headquarter exists
    Customer headquarter = validateHeadquarter(headquarterId);

    // Fetch branches via repository (use existing findChildren method)
    List<Customer> branches = customerRepository.findChildren(headquarter.getId());

    log.info("Found {} branches for headquarter {}", branches.size(), headquarter.getCompanyName());

    return branches.stream()
        .map(branch -> new CustomerResponseBuilder().fromEntity(branch).build())
        .collect(Collectors.toList());
  }

  /**
   * Updates a branch customer.
   *
   * <p>Note: Parent customer ID cannot be changed via this method. Validation ensures entity
   * remains FILIALE.
   *
   * @param branchId UUID of the branch to update
   * @param request Update request
   * @param updatedBy User performing the update
   * @return Updated branch customer response
   * @throws CustomerNotFoundException if branch not found
   * @throws InvalidHierarchyException if entity is not FILIALE
   */
  @Transactional
  public CustomerResponse updateBranch(
      UUID branchId, @Valid UpdateCustomerRequest request, String updatedBy) {
    log.debug("Updating branch ID: {}", branchId);

    // Validate entity is FILIALE
    Customer branch =
        customerRepository
            .findByIdOptional(branchId)
            .orElseThrow(() -> new CustomerNotFoundException(branchId));

    if (branch.getHierarchyType() != CustomerHierarchyType.FILIALE) {
      throw new InvalidHierarchyException(
          "Customer " + branchId + " is not a FILIALE. Current type: " + branch.getHierarchyType());
    }

    // Update via CustomerService (maintains parent relationship)
    CustomerResponse updated = customerService.updateCustomer(branchId, request, updatedBy);

    log.info("Updated branch {} (ID: {})", updated.companyName(), branchId);

    return updated;
  }

  /**
   * Soft-deletes a branch customer.
   *
   * @param branchId UUID of the branch to delete
   * @param deletedBy User performing the delete
   * @throws CustomerNotFoundException if branch not found
   * @throws InvalidHierarchyException if entity is not FILIALE
   */
  @Transactional
  public void deleteBranch(UUID branchId, String deletedBy) {
    log.debug("Deleting branch ID: {}", branchId);

    // Validate entity is FILIALE
    Customer branch =
        customerRepository
            .findByIdOptional(branchId)
            .orElseThrow(() -> new CustomerNotFoundException(branchId));

    if (branch.getHierarchyType() != CustomerHierarchyType.FILIALE) {
      throw new InvalidHierarchyException(
          "Customer " + branchId + " is not a FILIALE. Current type: " + branch.getHierarchyType());
    }

    // Soft-delete via CustomerService
    customerService.deleteCustomer(branchId, deletedBy, "Branch deleted via BranchService");

    log.info("Deleted branch {} (ID: {})", branch.getCompanyName(), branchId);
  }

  // ========== HIERARCHY VALIDATION ==========

  /**
   * Validates a customer is a HEADQUARTER.
   *
   * @param customerId UUID of the customer to validate
   * @return The validated HEADQUARTER customer entity
   * @throws CustomerNotFoundException if customer not found
   * @throws InvalidHierarchyException if customer is not HEADQUARTER
   */
  private Customer validateHeadquarter(UUID customerId) {
    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

    if (customer.getHierarchyType() != CustomerHierarchyType.HEADQUARTER) {
      throw new InvalidHierarchyException(
          "Customer "
              + customerId
              + " is not a HEADQUARTER. Current type: "
              + customer.getHierarchyType());
    }

    return customer;
  }

  /**
   * Validates hierarchy integrity for a customer.
   *
   * <p>Checks: - FILIALE has parent - HEADQUARTER has no parent - Parent exists and is not
   * soft-deleted
   *
   * @param customerId UUID of the customer to validate
   * @return true if hierarchy is valid
   * @throws CustomerNotFoundException if customer not found
   * @throws InvalidHierarchyException if hierarchy validation fails
   */
  @Transactional
  public boolean validateHierarchy(UUID customerId) {
    log.debug("Validating hierarchy for customer ID: {}", customerId);

    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

    if (customer.getHierarchyType() == CustomerHierarchyType.FILIALE) {
      // FILIALE must have parent
      if (customer.getParentCustomer() == null) {
        throw new InvalidHierarchyException(
            "FILIALE customer " + customerId + " has no parent HEADQUARTER");
      }

      // Parent must not be soft-deleted
      if (customer.getParentCustomer().getIsDeleted()) {
        throw new InvalidHierarchyException(
            "FILIALE customer "
                + customerId
                + " has soft-deleted parent "
                + customer.getParentCustomer().getId());
      }

      // Parent must be HEADQUARTER
      if (customer.getParentCustomer().getHierarchyType() != CustomerHierarchyType.HEADQUARTER) {
        throw new InvalidHierarchyException(
            "FILIALE customer "
                + customerId
                + " has invalid parent type: "
                + customer.getParentCustomer().getHierarchyType());
      }
    } else if (customer.getHierarchyType() == CustomerHierarchyType.HEADQUARTER) {
      // HEADQUARTER must not have parent
      if (customer.getParentCustomer() != null) {
        throw new InvalidHierarchyException("HEADQUARTER customer " + customerId + " has a parent");
      }
    }

    log.info("Hierarchy validation passed for customer ID: {}", customerId);
    return true;
  }
}
