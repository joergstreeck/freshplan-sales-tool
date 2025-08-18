package de.freshplan.domain.customer.service.command;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerTimelineEvent;
import de.freshplan.domain.customer.entity.EventCategory;
import de.freshplan.domain.customer.entity.ImportanceLevel;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.CustomerNumberGeneratorService;
import de.freshplan.domain.customer.service.dto.CreateCustomerRequest;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.dto.UpdateCustomerRequest;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command Service for Customer domain following CQRS pattern. Handles all write operations (create,
 * update, delete).
 *
 * <p>This service works in parallel with the existing CustomerService and can be activated via
 * feature flag: features.cqrs.enabled
 *
 * <p>IMPORTANT: This service maintains EXACT compatibility with CustomerService to ensure seamless
 * switching via feature flag.
 */
@ApplicationScoped
public class CustomerCommandService {

  private static final Logger log = LoggerFactory.getLogger(CustomerCommandService.class);

  private final CustomerRepository customerRepository;
  private final CustomerNumberGeneratorService numberGenerator;
  private final CustomerMapper customerMapper;

  @Inject
  public CustomerCommandService(
      CustomerRepository customerRepository,
      CustomerNumberGeneratorService numberGenerator,
      CustomerMapper customerMapper) {
    this.customerRepository = customerRepository;
    this.numberGenerator = numberGenerator;
    this.customerMapper = customerMapper;
  }

  /**
   * Creates a new customer. This method is an EXACT copy of CustomerService.createCustomer() to
   * ensure compatibility when switching via feature flag.
   *
   * @param request The customer creation request
   * @param createdBy The user creating the customer
   * @return The created customer response
   * @throws CustomerAlreadyExistsException if company name already exists
   */
  @Transactional
  public CustomerResponse createCustomer(@Valid CreateCustomerRequest request, String createdBy) {
    // Null validation - exact copy from CustomerService
    if (request == null) {
      throw new IllegalArgumentException("CreateCustomerRequest cannot be null");
    }
    if (createdBy == null || createdBy.trim().isEmpty()) {
      throw new IllegalArgumentException("createdBy cannot be null or empty");
    }

    log.debug("Creating new customer with company name: {}", request.companyName());

    // Check for potential duplicates by company name
    List<Customer> potentialDuplicates =
        customerRepository.findPotentialDuplicates(request.companyName());

    if (!potentialDuplicates.isEmpty()) {
      log.warn(
          "Duplicate customer name detected: {} similar to existing: {}",
          request.companyName(),
          potentialDuplicates.get(0).getCompanyName());
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

    log.info(
        "Customer created successfully - ID: {}, Number: {}, Company: {}",
        customer.getId(),
        customer.getCustomerNumber(),
        customer.getCompanyName());

    return customerMapper.toResponse(customer);
  }

  /**
   * Updates an existing customer. This method is an EXACT copy of CustomerService.updateCustomer()
   * to ensure compatibility when switching via feature flag.
   *
   * @param id The customer ID
   * @param request The update request
   * @param updatedBy The user updating the customer
   * @return The updated customer response
   * @throws CustomerNotFoundException if customer not found
   */
  @Transactional
  public CustomerResponse updateCustomer(
      UUID id, @Valid UpdateCustomerRequest request, String updatedBy) {
    log.debug("Updating customer with ID: {}", id);

    // Null validation - exact copy from CustomerService
    if (id == null) {
      throw new IllegalArgumentException("Customer ID cannot be null");
    }
    if (request == null) {
      throw new IllegalArgumentException("UpdateCustomerRequest cannot be null");
    }
    if (updatedBy == null || updatedBy.trim().isEmpty()) {
      throw new IllegalArgumentException("updatedBy cannot be null or empty");
    }

    Customer customer =
        customerRepository
            .findByIdActive(id)
            .orElseThrow(
                () -> {
                  log.error("Customer not found for update - ID: {}", id);
                  return new CustomerNotFoundException(id);
                });

    // Use mapper for update logic
    customerMapper.updateEntity(customer, request, updatedBy);

    log.info(
        "Customer updated successfully - ID: {}, Company: {}",
        customer.getId(),
        customer.getCompanyName());

    return customerMapper.toResponse(customer);
  }

  /**
   * Deletes a customer (soft delete). EXACT COPY from CustomerService - for parallel implementation
   * via feature flag.
   *
   * @param id The customer ID
   * @param deletedBy The user performing the deletion
   * @param reason The reason for deletion
   * @throws CustomerNotFoundException if customer not found
   * @throws CustomerHasChildrenException if customer has children
   */
  @Transactional
  public void deleteCustomer(UUID id, String deletedBy, String reason) {
    log.debug("Deleting customer with ID: {} - Reason: {}", id, reason);

    // Null validation
    if (id == null) {
      throw new IllegalArgumentException("Customer ID cannot be null");
    }
    if (deletedBy == null || deletedBy.trim().isEmpty()) {
      throw new IllegalArgumentException("deletedBy cannot be null or empty");
    }
    if (reason == null || reason.trim().isEmpty()) {
      throw new IllegalArgumentException("reason cannot be null or empty");
    }

    Customer customer =
        customerRepository
            .findByIdActive(id)
            .orElseThrow(
                () -> {
                  log.error("Customer not found for deletion - ID: {}", id);
                  return new CustomerNotFoundException(id);
                });

    // Business rule: Cannot delete customer with children
    if (customerRepository.hasChildren(id)) {
      log.warn("Cannot delete customer {} - has child customers", id);
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

    log.info(
        "Customer deleted (soft delete) - ID: {}, Company: {}, Reason: {}",
        customer.getId(),
        customer.getCompanyName(),
        reason);
  }

  /**
   * Restores a soft-deleted customer. EXACT COPY from CustomerService - for parallel implementation
   * via feature flag.
   *
   * @param id The customer ID
   * @param restoredBy The user restoring the customer
   * @throws CustomerNotFoundException if customer not found
   */
  @Transactional
  public CustomerResponse restoreCustomer(UUID id, String restoredBy) {
    log.debug("Restoring customer with ID: {}", id);

    // Null validation
    if (id == null) {
      throw new IllegalArgumentException("Customer ID cannot be null");
    }
    if (restoredBy == null || restoredBy.trim().isEmpty()) {
      throw new IllegalArgumentException("restoredBy cannot be null or empty");
    }

    // Find even deleted customers for restore operation
    Optional<Customer> customerOpt = customerRepository.findByIdOptional(id);
    Customer customer =
        customerOpt.orElseThrow(
            () -> {
              log.error("Customer not found for restoration - ID: {}", id);
              return new CustomerNotFoundException(id);
            });

    if (!customer.getIsDeleted()) {
      log.warn("Attempted to restore non-deleted customer - ID: {}", id);
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

    log.info(
        "Customer restored successfully - ID: {}, Company: {}",
        customer.getId(),
        customer.getCompanyName());

    return customerMapper.toResponse(customer);
  }

  /**
   * Adds a child customer to a parent. EXACT COPY from CustomerService - for parallel
   * implementation via feature flag.
   *
   * <p>NOTE: This method does NOT create a Timeline Event (inconsistent with other commands). TODO:
   * Consider adding Timeline Event in future improvement (Technical Debt).
   *
   * @param parentId The parent customer ID
   * @param childId The child customer ID
   * @param updatedBy The user performing the operation
   * @return The updated child customer
   * @throws CustomerNotFoundException if parent or child not found
   * @throws IllegalStateException if child already has parent or circular reference detected
   */
  @Transactional
  public CustomerResponse addChildCustomer(UUID parentId, UUID childId, String updatedBy) {
    log.debug("Adding child customer - parent: {}, child: {}", parentId, childId);

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
      log.warn(
          "Cannot add child - customer {} already has parent {}",
          childId,
          child.getParentCustomer().getId());
      throw new IllegalStateException("Customer already has a parent: " + childId);
    }

    // Prevent circular references
    // Fixed: Correct hierarchy check to prevent cycles
    if (isDescendant(child, parent)) {
      log.error("Circular hierarchy detected - parent: {}, child: {}", parentId, childId);
      throw new IllegalStateException("Cannot create circular hierarchy");
    }

    child.setParentCustomer(parent);
    child.setUpdatedBy(updatedBy);

    // NOTE: Original CustomerService does NOT create a Timeline Event here!
    // This is inconsistent with other command methods but we keep it for compatibility.
    // Technical Debt: Should add createTimelineEvent(child, "PARENT_CHANGED", ...)

    log.info(
        "Child customer added successfully - parent: {} ({}), child: {} ({})",
        parent.getId(),
        parent.getCompanyName(),
        child.getId(),
        child.getCompanyName());

    return customerMapper.toResponse(child);
  }

  /**
   * Updates risk scores for all customers. EXACT COPY from CustomerService - for parallel
   * implementation via feature flag. This is a maintenance operation that should be run
   * periodically.
   *
   * <p>NOTE: This method has several issues we keep for compatibility: - Limited to 1000 customers
   * (Page.ofSize(1000)) - No Timeline Event created (inconsistent with other commands) - No error
   * handling for individual customers - No way to update only specific customers TODO: Consider
   * improvements in future version (Technical Debt)
   */
  @Transactional
  public void updateAllRiskScores() {
    log.info("Starting risk score update for all customers");

    List<Customer> allCustomers = customerRepository.findAllActive(Page.ofSize(1000));
    int updatedCount = 0;

    for (Customer customer : allCustomers) {
      int oldScore = customer.getRiskScore();
      customer.updateRiskScore();

      if (oldScore != customer.getRiskScore()) {
        log.debug(
            "Risk score updated for customer {} - old: {}, new: {}",
            customer.getId(),
            oldScore,
            customer.getRiskScore());
        updatedCount++;
      }
    }

    log.info(
        "Risk score update completed - {} customers updated out of {} total",
        updatedCount,
        allCustomers.size());

    // Changes are automatically persisted due to @Transactional
  }

  /**
   * Merges two customers (keeps the target, deletes the source). EXACT COPY from CustomerService
   * for compatibility.
   *
   * @param targetId The ID of the target customer (will be kept)
   * @param sourceId The ID of the source customer (will be deleted)
   * @param mergedBy The user performing the merge
   * @return The merged customer response
   * @throws CustomerNotFoundException if either customer is not found
   * @throws CustomerHasChildrenException if source customer has children
   */
  @Transactional
  public CustomerResponse mergeCustomers(UUID targetId, UUID sourceId, String mergedBy) {
    log.info("Merging customers - target: {}, source: {}", targetId, sourceId);

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
      log.warn("Cannot merge customer {} - has child customers", sourceId);
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

    // Technical Debt: Should create Timeline Event for merge operation
    // createTimelineEvent(target, "CUSTOMERS_MERGED",
    //     "Merged with customer: " + source.getCompanyName(),
    //     mergedBy, ImportanceLevel.HIGH);

    log.info(
        "Customers merged successfully - target: {} ({}), source: {} ({}) deleted",
        target.getId(),
        target.getCompanyName(),
        source.getId(),
        source.getCompanyName());

    // Note: CustomerService uses private mapToResponse(), we use customerMapper
    return customerMapper.toResponse(target);
  }

  /**
   * Changes customer status with business rules. This method is an EXACT copy of
   * CustomerService.changeStatus() to ensure compatibility when switching via feature flag.
   *
   * @param customerId The customer ID
   * @param newStatus The new status to set
   * @param updatedBy The user performing the status change
   * @return The updated customer response
   * @throws CustomerNotFoundException if customer not found
   * @throws IllegalStateException if status transition is not allowed
   */
  @Transactional
  public CustomerResponse changeStatus(
      UUID customerId, CustomerStatus newStatus, String updatedBy) {
    log.debug("Changing customer status - ID: {}, new status: {}", customerId, newStatus);

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

    log.info(
        "Customer status changed - ID: {}, Company: {}, Status: {} -> {}",
        customer.getId(),
        customer.getCompanyName(),
        oldStatus,
        newStatus);

    // Note: CustomerService uses private mapToResponse(), we use customerMapper
    return customerMapper.toResponse(customer);
  }

  /**
   * Creates a timeline event for the customer with default MEDIUM importance. EXACT COPY from
   * CustomerService - overloaded version.
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
   * Creates a timeline event for the customer with specified importance. Private helper method -
   * exact copy from CustomerService.
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

    log.debug(
        "Timeline event created - Type: {}, Customer: {}, By: {}",
        eventType,
        customer.getCustomerNumber(),
        performedBy);
  }

  /**
   * Generates a title for the timeline event based on event type. Private helper method - exact
   * copy from CustomerService.
   */
  private String generateEventTitle(String eventType) {
    return switch (eventType) {
      case "CUSTOMER_CREATED" -> "Kunde angelegt";
      case "CUSTOMER_UPDATED" -> "Kunde aktualisiert";
      case "CUSTOMER_DELETED" -> "Kunde gelöscht";
      case "CUSTOMER_RESTORED" -> "Kunde wiederhergestellt";
      case "STATUS_CHANGED" -> "Status geändert";
      case "CONTACT_ADDED" -> "Kontakt hinzugefügt";
      case "CONTACT_REMOVED" -> "Kontakt entfernt";
      case "OPPORTUNITY_CREATED" -> "Opportunity erstellt";
      case "RISK_SCORE_UPDATED" -> "Risiko-Score aktualisiert";
      case "PARENT_CHANGED" -> "Hierarchie geändert";
      default -> eventType;
    };
  }

  /** Maps event type to category. Private helper method - exact copy from CustomerService. */
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

  /**
   * Checks if potential_ancestor is an ancestor of potential_descendant. Private helper method -
   * exact copy from CustomerService. Used to prevent circular hierarchies.
   *
   * @param potential_ancestor The potential ancestor customer
   * @param potential_descendant The potential descendant customer
   * @return true if potential_ancestor is in the parent chain of potential_descendant
   */
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

  /**
   * Validates status transitions according to business rules. EXACT COPY from
   * CustomerService.validateStatusTransition()
   *
   * @param from The current status
   * @param to The target status
   * @throws IllegalStateException if transition is not allowed
   */
  private void validateStatusTransition(CustomerStatus from, CustomerStatus to) {
    // Business rules for status transitions
    if (from == CustomerStatus.ARCHIVIERT && to != CustomerStatus.INAKTIV) {
      throw new IllegalStateException(
          "Cannot transition from ARCHIVIERT to " + to + ". Must go through INAKTIV first.");
    }

    // Add more business rules as needed
  }
}
