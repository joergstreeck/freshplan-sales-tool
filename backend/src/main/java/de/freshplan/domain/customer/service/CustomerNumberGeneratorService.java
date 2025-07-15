package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.constants.CustomerConstants;
import de.freshplan.domain.customer.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Year;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service for generating unique customer numbers in the format KD-YYYY-XXXXX. Thread-safe
 * implementation to ensure no duplicate numbers are generated.
 *
 * <p>Examples: - KD-2025-00001 - KD-2025-00234 - KD-2026-00001 (resets each year)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CustomerNumberGeneratorService {

  private final CustomerRepository customerRepository;
  private final ReentrantLock lock = new ReentrantLock();

  @Inject
  public CustomerNumberGeneratorService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  /**
   * Generates the next available customer number for the current year. Format: KD-YYYY-XXXXX
   *
   * <p>This method is thread-safe using ReentrantLock to prevent duplicate numbers in concurrent
   * scenarios.
   *
   * @return Next available customer number
   */
  @Transactional
  public String generateNext() {
    lock.lock();
    try {
      int currentYear = Year.now().getValue();
      String yearPrefix =
          CustomerConstants.CUSTOMER_NUMBER_PREFIX
              + CustomerConstants.CUSTOMER_NUMBER_SEPARATOR
              + currentYear
              + CustomerConstants.CUSTOMER_NUMBER_SEPARATOR;

      // Get the highest number for current year
      Integer maxNumber = customerRepository.getMaxCustomerNumberForYear(currentYear);

      // Next number (starting from 1 if no customers exist for this year)
      int nextNumber = (maxNumber != null) ? maxNumber + 1 : 1;

      // Format with leading zeros (5 digits)
      return yearPrefix + String.format("%05d", nextNumber);

    } finally {
      lock.unlock();
    }
  }

  /**
   * Generates a customer number for a specific year. Mainly used for testing or data migration
   * purposes.
   *
   * @param year The year for which to generate the customer number
   * @return Next available customer number for the specified year
   */
  @Transactional
  public String generateForYear(int year) {
    lock.lock();
    try {
      String yearPrefix =
          CustomerConstants.CUSTOMER_NUMBER_PREFIX
              + CustomerConstants.CUSTOMER_NUMBER_SEPARATOR
              + year
              + CustomerConstants.CUSTOMER_NUMBER_SEPARATOR;

      // Get the highest number for specified year
      Integer maxNumber = customerRepository.getMaxCustomerNumberForYear(year);

      // Next number
      int nextNumber = (maxNumber != null) ? maxNumber + 1 : 1;

      // Format with leading zeros (5 digits)
      return yearPrefix + String.format("%05d", nextNumber);

    } finally {
      lock.unlock();
    }
  }

  /**
   * Validates if a customer number follows the correct format.
   *
   * @param customerNumber The customer number to validate
   * @return true if the format is valid, false otherwise
   */
  public boolean isValidFormat(String customerNumber) {
    if (customerNumber == null || customerNumber.isBlank()) {
      return false;
    }

    // Pattern: KD-YYYY-XXXXX (where YYYY is year and XXXXX is 5-digit number)
    return customerNumber.matches("^" + CustomerConstants.CUSTOMER_NUMBER_PATTERN + "$");
  }

  /**
   * Extracts the year from a customer number.
   *
   * @param customerNumber The customer number
   * @return The year, or null if the format is invalid
   */
  public Integer extractYear(String customerNumber) {
    if (!isValidFormat(customerNumber)) {
      return null;
    }

    try {
      return Integer.parseInt(customerNumber.substring(3, 7));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Extracts the sequence number from a customer number.
   *
   * @param customerNumber The customer number
   * @return The sequence number, or null if the format is invalid
   */
  public Integer extractSequenceNumber(String customerNumber) {
    if (!isValidFormat(customerNumber)) {
      return null;
    }

    try {
      return Integer.parseInt(customerNumber.substring(8));
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
