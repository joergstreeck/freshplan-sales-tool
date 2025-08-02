package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.*;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing customer chain structure and business model. Implements Sprint 2
 * sales-focused features.
 *
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class CustomerChainService {

  private static final Logger log = LoggerFactory.getLogger(CustomerChainService.class);

  @Inject CustomerRepository customerRepository;

  @Inject PotentialCalculator potentialCalculator;

  /** Updates the chain structure for a customer. */
  public ChainStructureDto updateChainStructure(
      UUID customerId, ChainStructureDto chainStructure, String updatedBy) {

    log.info("Updating chain structure for customer: {}", customerId);

    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

    // Validate chain structure
    if (!chainStructure.isValid()) {
      throw new IllegalArgumentException("Sum of locations exceeds total EU locations");
    }

    // Update fields
    if (chainStructure.getTotalLocationsEU() != null) {
      customer.setTotalLocationsEU(chainStructure.getTotalLocationsEU());
    }
    if (chainStructure.getLocationsGermany() != null) {
      customer.setLocationsGermany(chainStructure.getLocationsGermany());
    }
    if (chainStructure.getLocationsAustria() != null) {
      customer.setLocationsAustria(chainStructure.getLocationsAustria());
    }
    if (chainStructure.getLocationsSwitzerland() != null) {
      customer.setLocationsSwitzerland(chainStructure.getLocationsSwitzerland());
    }
    if (chainStructure.getLocationsRestEU() != null) {
      customer.setLocationsRestEU(chainStructure.getLocationsRestEU());
    }

    customer.setUpdatedBy(updatedBy);
    customer.setUpdatedAt(LocalDateTime.now());

    customerRepository.persist(customer);

    // Return updated structure
    return new ChainStructureDto(
        customer.getTotalLocationsEU(),
        customer.getLocationsGermany(),
        customer.getLocationsAustria(),
        customer.getLocationsSwitzerland(),
        customer.getLocationsRestEU());
  }

  /** Updates the business model and pain points for a customer. */
  public BusinessModelDto updateBusinessModel(
      UUID customerId, BusinessModelDto businessModel, String updatedBy) {

    log.info("Updating business model for customer: {}", customerId);

    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

    // Update fields
    if (businessModel.getPrimaryFinancing() != null) {
      customer.setPrimaryFinancing(businessModel.getPrimaryFinancing());
    }
    if (businessModel.getPainPoints() != null) {
      customer.setPainPoints(businessModel.getPainPoints());
    }

    customer.setUpdatedBy(updatedBy);
    customer.setUpdatedAt(LocalDateTime.now());

    customerRepository.persist(customer);

    // Return updated model
    return new BusinessModelDto(customer.getPrimaryFinancing(), customer.getPainPoints());
  }

  /** Calculates the sales potential for a customer. */
  public PotentialCalculationResponse calculatePotential(
      UUID customerId, PotentialCalculationRequest request) {

    log.info(
        "Calculating potential for customer: {} in industry: {}",
        customerId,
        request.getIndustry());

    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

    // Use the potential calculator service
    return potentialCalculator.calculate(customer, request);
  }
}
