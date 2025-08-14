package de.freshplan.domain.testdata.service.query;

import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.testdata.service.TestDataService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Query service for test data statistics and read operations. Handles all read-only operations
 * related to test data without any transactional overhead.
 * 
 * CQRS Pattern: This service contains only read operations (Queries) that do not modify
 * database state. No @Transactional annotations are used for optimal read performance.
 */
@ApplicationScoped
public class TestDataQueryService {

  private static final Logger LOG = Logger.getLogger(TestDataQueryService.class);

  @Inject CustomerRepository customerRepository;
  @Inject CustomerTimelineRepository timelineRepository;

  /**
   * Counts existing test data in the database. Returns statistics about test customers
   * and timeline events marked with isTestData = true.
   * 
   * This is a read-only operation without @Transactional for optimal performance.
   * 
   * @return TestDataStats containing counts of test customers and events
   */
  public TestDataService.TestDataStats getTestDataStats() {
    LOG.debugf("Retrieving test data statistics...");

    long customerCount = customerRepository.count("isTestData", true);
    long eventCount = timelineRepository.count("isTestData", true);

    LOG.debugf("Test data stats: %d customers, %d events", customerCount, eventCount);

    return new TestDataService.TestDataStats(customerCount, eventCount);
  }
}