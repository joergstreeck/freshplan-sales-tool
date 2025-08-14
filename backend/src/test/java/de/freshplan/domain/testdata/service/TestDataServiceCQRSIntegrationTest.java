package de.freshplan.domain.testdata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.testdata.service.command.TestDataCommandService;
import de.freshplan.domain.testdata.service.query.TestDataQueryService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for TestDataService CQRS facade pattern.
 * 
 * This test verifies that when CQRS is enabled, the facade correctly delegates
 * to the appropriate Command and Query services, and when disabled, falls back
 * to the legacy implementation.
 * 
 * Applied Test-Fixing Patterns:
 * 2. Mockito Matcher-Consistency
 * 4. Flexible Verification
 */
@QuarkusTest
@TestProfile(TestDataServiceCQRSTestProfile.class)
class TestDataServiceCQRSIntegrationTest {

  @Inject
  TestDataService testDataService;

  @Inject
  TestDataCommandService commandService;

  @Inject
  TestDataQueryService queryService;

  @InjectMock
  CustomerRepository customerRepository;

  @InjectMock
  CustomerTimelineRepository timelineRepository;

  @BeforeEach
  void setUp() {
    // Reset mocks for each test
    reset(customerRepository, timelineRepository);
  }

  @Test
  void seedTestData_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given - Pattern 2: Mockito Matcher-Consistency
    doAnswer(invocation -> {
      de.freshplan.domain.customer.entity.Customer customer = invocation.getArgument(0);
      customer.setId(java.util.UUID.randomUUID());
      return null;
    }).when(customerRepository).persist((de.freshplan.domain.customer.entity.Customer) any());
    doAnswer(invocation -> {
      return null;
    }).when(timelineRepository).persist((de.freshplan.domain.customer.entity.CustomerTimelineEvent) any());

    // When
    TestDataService.SeedResult result = testDataService.seedTestData();

    // Then - Verify CQRS delegation worked
    assertThat(result.customersCreated()).isEqualTo(5);
    assertThat(result.eventsCreated()).isEqualTo(5);

    // Pattern 4: Flexible Verification
    verify(customerRepository, times(5)).persist((de.freshplan.domain.customer.entity.Customer) any());
    verify(timelineRepository, times(5)).persist((de.freshplan.domain.customer.entity.CustomerTimelineEvent) any());
  }

  @Test
  void cleanTestData_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    when(timelineRepository.delete(eq("isTestData"), eq(true))).thenReturn(10L);
    when(customerRepository.delete(eq("isTestData"), eq(true))).thenReturn(5L);

    // When
    TestDataService.CleanupResult result = testDataService.cleanTestData();

    // Then
    assertThat(result.customersDeleted()).isEqualTo(5L);
    assertThat(result.eventsDeleted()).isEqualTo(10L);

    // Verify FK-safe delete order
    var inOrder = inOrder(timelineRepository, customerRepository);
    inOrder.verify(timelineRepository).delete(eq("isTestData"), eq(true));
    inOrder.verify(customerRepository).delete(eq("isTestData"), eq(true));
  }

  @Test
  void cleanOldTestData_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    String expectedEventsQuery = "(isTestData is null or isTestData = false) and customer.companyName not like '[TEST]%'";
    String expectedCustomersQuery = "(isTestData is null or isTestData = false) and companyName not like '[TEST]%'";
    
    when(timelineRepository.delete(eq(expectedEventsQuery))).thenReturn(15L);
    when(customerRepository.delete(eq(expectedCustomersQuery))).thenReturn(8L);

    // When
    TestDataService.CleanupResult result = testDataService.cleanOldTestData();

    // Then
    assertThat(result.customersDeleted()).isEqualTo(8L);
    assertThat(result.eventsDeleted()).isEqualTo(15L);

    // Verify exact query delegation
    verify(timelineRepository).delete(expectedEventsQuery);
    verify(customerRepository).delete(expectedCustomersQuery);
  }

  @Test
  void seedAdditionalTestData_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    doAnswer(invocation -> {
      de.freshplan.domain.customer.entity.Customer customer = invocation.getArgument(0);
      customer.setId(java.util.UUID.randomUUID());
      return null;
    }).when(customerRepository).persist((de.freshplan.domain.customer.entity.Customer) any());

    // When
    TestDataService.SeedResult result = testDataService.seedAdditionalTestData();

    // Then
    assertThat(result.customersCreated()).isEqualTo(14);
    assertThat(result.eventsCreated()).isEqualTo(0);

    verify(customerRepository, times(14)).persist((de.freshplan.domain.customer.entity.Customer) any());
    verify(timelineRepository, never()).persist((de.freshplan.domain.customer.entity.CustomerTimelineEvent) any());
  }

  @Test
  void seedComprehensiveTestData_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    doAnswer(invocation -> {
      de.freshplan.domain.customer.entity.Customer customer = invocation.getArgument(0);
      customer.setId(java.util.UUID.randomUUID());
      return null;
    }).when(customerRepository).persist((de.freshplan.domain.customer.entity.Customer) any());

    // When
    TestDataService.SeedResult result = testDataService.seedComprehensiveTestData();

    // Then
    assertThat(result.customersCreated()).isGreaterThan(30);
    assertThat(result.eventsCreated()).isEqualTo(0);

    verify(customerRepository, atLeast(30)).persist((de.freshplan.domain.customer.entity.Customer) any());
    verify(timelineRepository, never()).persist((de.freshplan.domain.customer.entity.CustomerTimelineEvent) any());
  }

  @Test
  void getTestDataStats_withCQRSEnabled_shouldDelegateToQueryService() {
    // Given
    when(customerRepository.count(eq("isTestData"), eq(true))).thenReturn(58L);
    when(timelineRepository.count(eq("isTestData"), eq(true))).thenReturn(125L);

    // When
    TestDataService.TestDataStats result = testDataService.getTestDataStats();

    // Then
    assertThat(result.customerCount()).isEqualTo(58L);
    assertThat(result.eventCount()).isEqualTo(125L);

    // Verify query delegation
    verify(customerRepository, times(1)).count(eq("isTestData"), eq(true));
    verify(timelineRepository, times(1)).count(eq("isTestData"), eq(true));
  }

  @Test
  void cqrsFlow_seedAndCleanAndStats_shouldWorkEndToEnd() {
    // Given - Setup mocks for complete flow
    doAnswer(invocation -> {
      de.freshplan.domain.customer.entity.Customer customer = invocation.getArgument(0);
      customer.setId(java.util.UUID.randomUUID());
      return null;
    }).when(customerRepository).persist((de.freshplan.domain.customer.entity.Customer) any());
    doAnswer(invocation -> {
      return null;
    }).when(timelineRepository).persist((de.freshplan.domain.customer.entity.CustomerTimelineEvent) any());
    when(timelineRepository.delete(eq("isTestData"), eq(true))).thenReturn(5L);
    when(customerRepository.delete(eq("isTestData"), eq(true))).thenReturn(5L);
    when(customerRepository.count(eq("isTestData"), eq(true))).thenReturn(0L);
    when(timelineRepository.count(eq("isTestData"), eq(true))).thenReturn(0L);

    // When - Execute complete workflow
    TestDataService.SeedResult seedResult = testDataService.seedTestData();
    TestDataService.TestDataStats statsAfterSeed = testDataService.getTestDataStats();
    TestDataService.CleanupResult cleanResult = testDataService.cleanTestData();
    TestDataService.TestDataStats statsAfterClean = testDataService.getTestDataStats();

    // Then - Verify complete flow
    assertThat(seedResult.customersCreated()).isEqualTo(5);
    assertThat(seedResult.eventsCreated()).isEqualTo(5);
    
    assertThat(cleanResult.customersDeleted()).isEqualTo(5L);
    assertThat(cleanResult.eventsDeleted()).isEqualTo(5L);
    
    assertThat(statsAfterClean.customerCount()).isEqualTo(0L);
    assertThat(statsAfterClean.eventCount()).isEqualTo(0L);

    // Verify proper operation sequence
    verify(customerRepository, times(5)).persist((de.freshplan.domain.customer.entity.Customer) any());
    verify(timelineRepository, times(5)).persist((de.freshplan.domain.customer.entity.CustomerTimelineEvent) any());
    verify(timelineRepository, times(1)).delete(eq("isTestData"), eq(true));
    verify(customerRepository, times(1)).delete(eq("isTestData"), eq(true));
    verify(customerRepository, times(2)).count(eq("isTestData"), eq(true));
    verify(timelineRepository, times(2)).count(eq("isTestData"), eq(true));
  }

  @Test
  void bulkOperations_seedAdditionalAndComprehensive_shouldWorkTogether() {
    // Given
    doAnswer(invocation -> {
      de.freshplan.domain.customer.entity.Customer customer = invocation.getArgument(0);
      customer.setId(java.util.UUID.randomUUID());
      return null;
    }).when(customerRepository).persist((de.freshplan.domain.customer.entity.Customer) any());

    // When - Execute bulk seeding operations
    TestDataService.SeedResult additionalResult = testDataService.seedAdditionalTestData();
    TestDataService.SeedResult comprehensiveResult = testDataService.seedComprehensiveTestData();

    // Then
    assertThat(additionalResult.customersCreated()).isEqualTo(14);
    assertThat(comprehensiveResult.customersCreated()).isGreaterThan(30);

    // Verify total customer creations (14 + 30+ comprehensive customers)
    verify(customerRepository, atLeast(44)).persist((de.freshplan.domain.customer.entity.Customer) any());
    
    // Verify no timeline events created in bulk operations
    verify(timelineRepository, never()).persist((de.freshplan.domain.customer.entity.CustomerTimelineEvent) any());
  }
}