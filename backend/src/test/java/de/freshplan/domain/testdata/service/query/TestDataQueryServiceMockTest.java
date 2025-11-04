package de.freshplan.domain.testdata.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.testdata.service.TestDataService;
import de.freshplan.domain.testdata.service.provider.TestDataStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Mock-based tests for TestDataQueryService (migrated from @QuarkusTest).
 *
 * <p>Sprint 2.1.4: Migriert von @QuarkusTest zu Mockito (~15s Ersparnis pro Run).
 *
 * <p>Testet CQRS Query-Operationen für TestData-Statistiken ohne DB-Zugriff.
 *
 * <p>Applied Test-Fixing Patterns: 2. Mockito Matcher-Consistency (all parameters as matchers) 4.
 * Flexible Verification (atLeastOnce() when appropriate)
 *
 * <p>Note: PanacheQuery-Mocking not needed for simple count queries.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("TestDataQueryService Mock Tests")
class TestDataQueryServiceMockTest {

  @Mock private CustomerRepository customerRepository;

  @Mock private CustomerTimelineRepository timelineRepository;

  @InjectMocks private TestDataQueryService queryService;

  @Test
  void getTestDataStats_shouldReturnCorrectCounts() {
    // Given - Pattern 2: Mockito Matcher-Consistency
    when(customerRepository.count(eq("isTestData"), eq(true))).thenReturn(58L);
    when(timelineRepository.count(eq("isTestData"), eq(true))).thenReturn(125L);

    // When
    TestDataStats result = queryService.getTestDataStats();

    // Then
    assertThat(result.customerCount()).isEqualTo(58L);
    assertThat(result.eventCount()).isEqualTo(125L);

    // Verify repository calls with exact matcher consistency
    verify(customerRepository, times(1)).count(eq("isTestData"), eq(true));
    verify(timelineRepository, times(1)).count(eq("isTestData"), eq(true));
  }

  @Test
  void getTestDataStats_withZeroCounts_shouldReturnZero() {
    // Given
    when(customerRepository.count(eq("isTestData"), eq(true))).thenReturn(0L);
    when(timelineRepository.count(eq("isTestData"), eq(true))).thenReturn(0L);

    // When
    TestDataStats result = queryService.getTestDataStats();

    // Then
    assertThat(result.customerCount()).isEqualTo(0L);
    assertThat(result.eventCount()).isEqualTo(0L);
  }

  @Test
  void getTestDataStats_withLargeCounts_shouldReturnCorrectValues() {
    // Given - Test large numbers
    when(customerRepository.count(eq("isTestData"), eq(true))).thenReturn(999999L);
    when(timelineRepository.count(eq("isTestData"), eq(true))).thenReturn(5000000L);

    // When
    TestDataStats result = queryService.getTestDataStats();

    // Then
    assertThat(result.customerCount()).isEqualTo(999999L);
    assertThat(result.eventCount()).isEqualTo(5000000L);
  }

  @Test
  void getTestDataStats_shouldNotPerformAnyWriteOperations() {
    // Given
    when(customerRepository.count(eq("isTestData"), eq(true))).thenReturn(10L);
    when(timelineRepository.count(eq("isTestData"), eq(true))).thenReturn(20L);

    // When
    queryService.getTestDataStats();

    // Then - Verify no write operations (CQRS compliance)
    verifyNoWriteOperationsForQuery();
  }

  /**
   * Helper method to verify that no write operations are performed in QueryService. This is
   * critical for CQRS compliance - Query services must be read-only.
   */
  private void verifyNoWriteOperationsForQuery() {
    // Verify no persist operations
    verify(customerRepository, never())
        .persist((de.freshplan.domain.customer.entity.Customer) any());
    verify(timelineRepository, never())
        .persist((de.freshplan.domain.customer.entity.CustomerTimelineEvent) any());

    // Verify no delete operations
    verify(customerRepository, never()).delete(any());
    verify(timelineRepository, never()).delete(any());

    // Verify no update operations (if they existed in repository)
    // Note: TestDataService repositories only have count, persist, delete - no updates
  }
}
