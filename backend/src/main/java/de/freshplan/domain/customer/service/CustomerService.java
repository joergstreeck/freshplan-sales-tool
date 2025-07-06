package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerLifecycleStage;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerTimelineEvent;
import de.freshplan.domain.customer.entity.EventCategory;
import de.freshplan.domain.customer.entity.ImportanceLevel;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.domain.customer.service.exception.CustomerAlreadyExistsException;
import de.freshplan.domain.customer.service.exception.CustomerHasChildrenException;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.customer.service.mapper.CustomerMapper;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Customer management operations.
 *
 * <p>This service encapsulates the business logic for customer management, providing a clean API
 * for customer operations while handling validation, error cases, and data transformation.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final CustomerNumberGeneratorService numberGenerator;
  private final CustomerMapper customerMapper;

  @Inject
  public CustomerService(
      CustomerRepository customerRepository,
      CustomerNumberGeneratorService numberGenerator,
      CustomerMapper customerMapper) {
    this.customerRepository = customerRepository;
    this.numberGenerator = numberGenerator;
    this.customerMapper = customerMapper;
  }

  // ========== CRUD OPERATIONS ==========

  /**
   * Creates a new customer with automatic customer number generation.
   *
   * @param request The customer creation request
   * @param createdBy The user creating the customer
   * @return The created customer response
   * @throws CustomerAlreadyExistsException if company name already exists
   */
  public CustomerResponse createCustomer(@Valid CreateCustomerRequest request, String createdBy) {
    // Null validation
    if (request == null) {
      throw new IllegalArgumentException("CreateCustomerRequest cannot be null");
    }

    // Check for potential duplicates by company name
    List<Customer> potentialDuplicates =
        customerRepository.findPotentialDuplicates(request.companyName());

    if (!potentialDuplicates.isEmpty()) {
      throw new CustomerAlreadyExistsException(
          "Customer with similar company name already exists: "
              + potentialDuplicates.get(0).getCompanyName(),
          "companyName",
          request.companyName());
    }

    // Generate unique customer number
    String customerNumber = numberGenerator.generateNext();

    // Create customer entity using mapper
    Customer customer = customerMapper.toEntity(request, customerNumber, createdBy);

    // Persist customer
    customerRepository.persist(customer);

    // Create timeline event for customer creation
    createTimelineEvent(
        customer,
        "CUSTOMER_CREATED",
        "Kunde erstellt mit Nummer: " + customerNumber,
        createdBy,
        ImportanceLevel.HIGH);

    return customerMapper.toResponse(customer);
  }

  /**
   * Retrieves a customer by ID.
   *
   * @param id The customer ID
   * @return The customer response
   * @throws CustomerNotFoundException if customer not found
   */
  public CustomerResponse getCustomer(UUID id) {
    Customer customer =
        customerRepository.findByIdActive(id).orElseThrow(() -> new CustomerNotFoundException(id));

    return customerMapper.toResponse(customer);
  }

  /**
   * Updates an existing customer.
   *
   * @param id The customer ID
   * @param request The update request
   * @param updatedBy The user updating the customer
   * @return The updated customer response
   * @throws CustomerNotFoundException if customer not found
   */
  public CustomerResponse updateCustomer(
      UUID id, @Valid UpdateCustomerRequest request, String updatedBy) {

    Customer customer =
        customerRepository.findByIdActive(id).orElseThrow(() -> new CustomerNotFoundException(id));

    // Use mapper for update logic
    customerMapper.updateEntity(customer, request, updatedBy);

    return customerMapper.toResponse(customer);
  }

  /**
   * Soft deletes a customer.
   *
   * @param id The customer ID
   * @param deletedBy The user deleting the customer
   * @param reason The reason for deletion
   * @throws CustomerNotFoundException if customer not found
   * @throws CustomerHasChildrenException if customer has children
   */
  public void deleteCustomer(UUID id, String deletedBy, String reason) {
    Customer customer =
        customerRepository.findByIdActive(id).orElseThrow(() -> new CustomerNotFoundException(id));

    // Business rule: Cannot delete customer with children
    if (customerRepository.hasChildren(id)) {
      throw new CustomerHasChildrenException(
          "Cannot delete customer with children. Delete children first.");
    }

    // Soft delete
    customer.setIsDeleted(true);
    customer.setDeletedAt(LocalDateTime.now());
    customer.setDeletedBy(deletedBy);

    // Create timeline event
    createTimelineEvent(
        customer,
        "CUSTOMER_DELETED",
        "Kunde archiviert. Grund: " + reason,
        deletedBy,
        ImportanceLevel.HIGH);
  }

  /**
   * Restores a soft-deleted customer.
   *
   * @param id The customer ID
   * @param restoredBy The user restoring the customer
   * @throws CustomerNotFoundException if customer not found
   */
  public CustomerResponse restoreCustomer(UUID id, String restoredBy) {
    // Find even deleted customers for restore operation
    Optional<Customer> customerOpt = customerRepository.findByIdOptional(id);
    Customer customer = customerOpt.orElseThrow(() -> new CustomerNotFoundException(id));

    if (!customer.getIsDeleted()) {
      throw new IllegalStateException("Customer is not deleted: " + id);
    }

    // Restore
    customer.setIsDeleted(false);
    customer.setDeletedAt(null);
    customer.setDeletedBy(null);
    customer.setUpdatedBy(restoredBy);

    // Create timeline event for restoration
    createTimelineEvent(
        customer, "CUSTOMER_RESTORED", "Kunde wiederhergestellt", restoredBy, ImportanceLevel.HIGH);

    return customerMapper.toResponse(customer);
  }

  // ========== LIST & SEARCH OPERATIONS ==========

  /** Gets all customers with pagination. */
  public CustomerListResponse getAllCustomers(int page, int size) {
    Page pageRequest = Page.of(page, size);
    List<Customer> customers = customerRepository.findAllActive(pageRequest);
    long totalElements = customerRepository.countActive();

    return customerMapper.toMinimalListResponse(customers, page, size, totalElements);
  }

  /** Searches customers by company name. */
  public CustomerListResponse searchCustomers(String searchTerm, int page, int size) {
    Page pageRequest = Page.of(page, size);
    List<Customer> customers = customerRepository.searchByCompanyName(searchTerm, pageRequest);

    // For search, we use the actual result count as total (not global count)
    long totalElements = customers.size();

    return customerMapper.toMinimalListResponse(customers, page, size, totalElements);
  }

  /** Filters customers by status. */
  public CustomerListResponse getCustomersByStatus(CustomerStatus status, int page, int size) {
    Page pageRequest = Page.of(page, size);
    List<Customer> customers = customerRepository.findByStatus(status, pageRequest);
    long totalElements = customerRepository.countByStatus(status);

    List<CustomerResponse> customerResponses =
        customers.stream().map(this::mapToResponse).collect(Collectors.toList());

    return CustomerListResponse.of(customerResponses, page, size, totalElements);
  }

  /** Filters customers by industry. */
  public CustomerListResponse getCustomersByIndustry(Industry industry, int page, int size) {
    Page pageRequest = Page.of(page, size);
    List<Customer> customers = customerRepository.findByIndustry(industry, pageRequest);

    // Count manually since we don't have countByIndustry method
    long totalElements = customers.size();

    List<CustomerResponse> customerResponses =
        customers.stream().map(this::mapToResponse).collect(Collectors.toList());

    return CustomerListResponse.of(customerResponses, page, size, totalElements);
  }

  // ========== HIERARCHY OPERATIONS ==========

  /** Gets the hierarchy tree for a customer. */
  public CustomerResponse getCustomerHierarchy(UUID customerId) {
    Customer customer =
        customerRepository
            .findByIdActive(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

    return mapToResponseWithHierarchy(customer);
  }

  /** Adds a child customer to a parent. */
  public CustomerResponse addChildCustomer(UUID parentId, UUID childId, String updatedBy) {
    Customer parent =
        customerRepository
            .findByIdActive(parentId)
            .orElseThrow(
                () -> new CustomerNotFoundException("Parent customer not found", parentId));

    Customer child =
        customerRepository
            .findByIdActive(childId)
            .orElseThrow(() -> new CustomerNotFoundException("Child customer not found", childId));

    // Business rules
    if (child.getParentCustomer() != null) {
      throw new IllegalStateException("Customer already has a parent: " + childId);
    }

    // Prevent circular references
    if (isDescendant(parent, child)) {
      throw new IllegalStateException("Cannot create circular hierarchy");
    }

    child.setParentCustomer(parent);
    child.setUpdatedBy(updatedBy);

    return mapToResponse(child);
  }

  // ========== RISK MANAGEMENT ==========

  /** Gets customers at risk. */
  public CustomerListResponse getCustomersAtRisk(int minRiskScore, int page, int size) {
    Page pageRequest = Page.of(page, size);
    List<Customer> customers = customerRepository.findAtRisk(minRiskScore, pageRequest);
    long totalElements = customerRepository.countAtRisk(minRiskScore);

    List<CustomerResponse> customerResponses =
        customers.stream().map(this::mapToResponse).collect(Collectors.toList());

    return CustomerListResponse.of(customerResponses, page, size, totalElements);
  }

  /** Gets customers with overdue follow-ups. */
  public CustomerListResponse getOverdueFollowUps(int page, int size) {
    Page pageRequest = Page.of(page, size);
    List<Customer> customers = customerRepository.findOverdueFollowUps(pageRequest);
    long totalElements = customerRepository.countOverdueFollowUps();

    List<CustomerResponse> customerResponses =
        customers.stream().map(this::mapToResponse).collect(Collectors.toList());

    return CustomerListResponse.of(customerResponses, page, size, totalElements);
  }

  /**
   * Updates risk scores for all customers. This is a maintenance operation that should be run
   * periodically.
   */
  public void updateAllRiskScores() {
    List<Customer> allCustomers = customerRepository.findAllActive(Page.ofSize(1000));

    for (Customer customer : allCustomers) {
      customer.updateRiskScore();
    }

    // Changes are automatically persisted due to @Transactional
  }

  // ========== DUPLICATE DETECTION ==========

  /** Checks for potential duplicates. */
  public List<CustomerResponse> checkDuplicates(String companyName) {
    List<Customer> duplicates = customerRepository.findPotentialDuplicates(companyName);

    return duplicates.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  /** Merges two customers (keeps the target, deletes the source). */
  public CustomerResponse mergeCustomers(UUID targetId, UUID sourceId, String mergedBy) {
    Customer target =
        customerRepository
            .findByIdActive(targetId)
            .orElseThrow(
                () -> new CustomerNotFoundException("Target customer not found", targetId));

    Customer source =
        customerRepository
            .findByIdActive(sourceId)
            .orElseThrow(
                () -> new CustomerNotFoundException("Source customer not found", sourceId));

    // Business rule: Cannot merge customer with children
    if (source.hasChildren()) {
      throw new CustomerHasChildrenException(sourceId, "merge");
    }

    // Merge business logic - combine financial data
    if (target.getExpectedAnnualVolume() == null && source.getExpectedAnnualVolume() != null) {
      target.setExpectedAnnualVolume(source.getExpectedAnnualVolume());
    }
    if (target.getActualAnnualVolume() == null && source.getActualAnnualVolume() != null) {
      target.setActualAnnualVolume(source.getActualAnnualVolume());
    }

    // Update contact dates if source is more recent
    if (source.getLastContactDate() != null
        && (target.getLastContactDate() == null
            || source.getLastContactDate().isAfter(target.getLastContactDate()))) {
      target.setLastContactDate(source.getLastContactDate());
    }

    target.setUpdatedBy(mergedBy);
    target.updateRiskScore();

    // Soft delete source customer
    source.setIsDeleted(true);
    source.setDeletedAt(LocalDateTime.now());
    source.setDeletedBy(mergedBy);

    return mapToResponse(target);
  }

  // ========== STATUS OPERATIONS ==========

  /** Changes customer status with business rules. */
  public CustomerResponse changeStatus(
      UUID customerId, CustomerStatus newStatus, String updatedBy) {
    Customer customer =
        customerRepository
            .findByIdActive(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

    CustomerStatus oldStatus = customer.getStatus();

    // Business rules for status transitions
    validateStatusTransition(oldStatus, newStatus);

    customer.setStatus(newStatus);
    customer.setUpdatedBy(updatedBy);
    customer.updateRiskScore();

    // Create timeline event
    createTimelineEvent(
        customer,
        "STATUS_CHANGE",
        "Status geändert von " + oldStatus + " zu " + newStatus,
        updatedBy);

    return mapToResponse(customer);
  }

  // ========== DASHBOARD DATA ==========

  /** Gets dashboard statistics. */
  public CustomerDashboardResponse getDashboardData() {
    // Basic counts
    long totalCustomers = customerRepository.countActive();
    long activeCustomers = customerRepository.countByStatus(CustomerStatus.AKTIV);
    long newThisMonth = customerRepository.countNewThisMonth();
    long atRiskCount = customerRepository.countAtRisk(70); // Risk score >= 70
    long upcomingFollowUps = customerRepository.countOverdueFollowUps();

    // Status distribution
    var customersByStatus = new java.util.HashMap<CustomerStatus, Long>();
    for (CustomerStatus status : CustomerStatus.values()) {
      customersByStatus.put(status, customerRepository.countByStatus(status));
    }

    // Lifecycle distribution
    var customersByLifecycle = new java.util.HashMap<CustomerLifecycleStage, Long>();
    for (CustomerLifecycleStage stage : CustomerLifecycleStage.values()) {
      customersByLifecycle.put(stage, customerRepository.countByLifecycleStage(stage));
    }

    return new CustomerDashboardResponse(
        totalCustomers,
        activeCustomers,
        newThisMonth,
        atRiskCount,
        upcomingFollowUps,
        customersByStatus,
        customersByLifecycle);
  }

  // ========== PRIVATE HELPER METHODS ==========

  private CustomerResponse mapToResponse(Customer customer) {
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
        customer.getIndustry(),
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
        customer.getCreatedAt(),
        customer.getCreatedBy(),
        customer.getUpdatedAt(),
        customer.getUpdatedBy(),
        customer.getIsDeleted(),
        customer.getDeletedAt(),
        customer.getDeletedBy());
  }

  private CustomerResponse mapToResponseWithHierarchy(Customer customer) {
    // For hierarchy view, load children recursively
    // This is simplified - in production you'd want to optimize this
    return mapToResponse(customer);
  }

  private boolean isDescendant(Customer potential_ancestor, Customer potential_descendant) {
    Customer current = potential_descendant.getParentCustomer();
    while (current != null) {
      if (current.getId().equals(potential_ancestor.getId())) {
        return true;
      }
      current = current.getParentCustomer();
    }
    return false;
  }

  private void validateStatusTransition(CustomerStatus from, CustomerStatus to) {
    // Business rules for status transitions
    if (from == CustomerStatus.ARCHIVIERT && to != CustomerStatus.INAKTIV) {
      throw new IllegalStateException(
          "Cannot transition from ARCHIVIERT to " + to + ". Must go through INAKTIV first.");
    }

    // Add more business rules as needed
  }

  // ========== TIMELINE EVENT HELPERS ==========

  /**
   * Creates a timeline event for the customer.
   *
   * @param customer The customer
   * @param eventType The event type
   * @param description The event description
   * @param performedBy The user who performed the action
   */
  private void createTimelineEvent(
      Customer customer, String eventType, String description, String performedBy) {
    createTimelineEvent(customer, eventType, description, performedBy, ImportanceLevel.MEDIUM);
  }

  /**
   * Creates a timeline event for the customer with specified importance.
   *
   * @param customer The customer
   * @param eventType The event type
   * @param description The event description
   * @param performedBy The user who performed the action
   * @param importance The importance level
   */
  private void createTimelineEvent(
      Customer customer,
      String eventType,
      String description,
      String performedBy,
      ImportanceLevel importance) {
    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setEventType(eventType);
    event.setEventDate(LocalDateTime.now());
    event.setTitle(generateEventTitle(eventType));
    event.setDescription(description);
    event.setPerformedBy(performedBy);
    event.setImportance(importance);

    // Set category based on event type
    event.setCategory(mapEventTypeToCategory(eventType));

    // Add to customer's timeline
    customer.getTimelineEvents().add(event);
  }

  /** Generates a title for the timeline event based on the event type. */
  private String generateEventTitle(String eventType) {
    return switch (eventType) {
      case "CUSTOMER_CREATED" -> "Kunde erstellt";
      case "CUSTOMER_DELETED" -> "Kunde archiviert";
      case "CUSTOMER_RESTORED" -> "Kunde wiederhergestellt";
      case "STATUS_CHANGE" -> "Status geändert";
      case "LIFECYCLE_CHANGE" -> "Lifecycle-Stadium geändert";
      case "CONTACT_ADDED" -> "Kontakt hinzugefügt";
      case "LOCATION_ADDED" -> "Standort hinzugefügt";
      case "NOTE_ADDED" -> "Notiz hinzugefügt";
      case "MERGE_PERFORMED" -> "Kunde zusammengeführt";
      default -> "Allgemeine Aktivität";
    };
  }

  /** Maps event type to appropriate category. */
  private EventCategory mapEventTypeToCategory(String eventType) {
    return switch (eventType) {
      case "CUSTOMER_CREATED", "CUSTOMER_DELETED", "CUSTOMER_RESTORED" -> EventCategory.SYSTEM;
      case "STATUS_CHANGE", "LIFECYCLE_CHANGE" -> EventCategory.STATUS_UPDATE;
      case "CONTACT_ADDED", "LOCATION_ADDED" -> EventCategory.DATA_CHANGE;
      case "NOTE_ADDED" -> EventCategory.NOTE;
      case "MERGE_PERFORMED" -> EventCategory.SYSTEM;
      default -> EventCategory.OTHER;
    };
  }
}
