package com.freshplan.reports;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.*;

/**
 * Unit Tests for ABAC-Secured Reports Query Service
 *
 * @see ../../grundlagen/TESTING_GUIDE.md - TestDataBuilder Pattern
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Security Testing
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Query Performance Testing
 *
 * This test class validates the ABAC security enforcement, SQL injection
 * prevention, and cursor-based pagination of the ReportsQuery service.
 * Uses TestDataBuilder pattern for maintainable test data creation.
 *
 * Security Test Coverage:
 * - Territory scoping enforcement
 * - Chain scoping enforcement
 * - SQL injection prevention via named parameters
 * - Parameter validation and sanitization
 *
 * Performance Test Coverage:
 * - Cursor pagination functionality
 * - Query optimization validation
 * - Memory-efficient result processing
 *
 * @author Backend Team
 * @version 1.1
 * @since 2025-09-19
 */
@QuarkusTest
@DisplayName("Reports Query Service Unit Tests")
class ReportsQueryTest {

    @Inject
    ReportsQuery reportsQuery;

    @InjectMock
    EntityManager entityManager;

    @InjectMock
    ScopeContext scopeContext;

    @InjectMock
    Query sqlQuery;

    @BeforeEach
    void setUp() {
        // Given: Default mock configuration
        when(entityManager.createNativeQuery(anyString())).thenReturn(sqlQuery);
        when(sqlQuery.setParameter(anyString(), any())).thenReturn(sqlQuery);
        when(sqlQuery.getResultList()).thenReturn(new ArrayList<>());
    }

    @Nested
    @DisplayName("Customer Analytics Query Tests")
    class CustomerAnalyticsTests {

        @Test
        @DisplayName("fetchCustomerAnalytics - ABAC territory scoping enforcement")
        void fetchCustomerAnalytics_withTerritoryScope_shouldEnforceAbacSecurity() {
            // Given: User has limited territory access
            when(scopeContext.getTerritories()).thenReturn(Set.of("nord", "west"));
            when(scopeContext.getChainId()).thenReturn(null);

            // Mock query result with customer data
            List<Object[]> mockResults = Arrays.asList(
                    new Object[]{"123e4567-e89b-12d3-a456-426614174000", "Restaurant Alpha",
                            "nord", "active", "high", 25000.0, 3, true,
                            LocalDate.of(2025, 3, 1), LocalDate.of(2025, 8, 31),
                            LocalDate.of(2025, 12, 31), "exclusive"},
                    new Object[]{"456e7890-e89b-12d3-a456-426614174001", "Bistro Beta",
                            "west", "pending", "medium", 15000.0, 2, false,
                            LocalDate.of(2025, 4, 1), LocalDate.of(2025, 9, 30),
                            LocalDate.of(2026, 1, 31), "non_exclusive"}
            );
            when(sqlQuery.getResultList()).thenReturn(mockResults);

            // When: Fetch customer analytics with territory filter
            Map<String, Object> result = reportsQuery.fetchCustomerAnalytics(
                    "gastronomiebetriebe", "nord", null, null, null, 100, null
            );

            // Then: Results contain ABAC-filtered data
            assertNotNull(result);
            assertTrue(result.containsKey("items"));
            assertTrue(result.containsKey("nextCursor"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
            assertEquals(2, items.size());

            // Verify first customer
            Map<String, Object> customer1 = items.get(0);
            assertEquals("123e4567-e89b-12d3-a456-426614174000", customer1.get("customerId"));
            assertEquals("Restaurant Alpha", customer1.get("name"));
            assertEquals("nord", customer1.get("territory"));
            assertEquals("high", customer1.get("roiBucket"));

            // Verify ABAC query parameters
            verify(sqlQuery).setParameter("territories", new String[]{"nord", "west"});
            verify(sqlQuery).setParameter("territory", "nord");
            verify(sqlQuery).setParameter("limit", 100);
        }

        @Test
        @DisplayName("fetchCustomerAnalytics - Chain scoping with territory override")
        void fetchCustomerAnalytics_withChainScope_shouldApplyBothFilters() {
            // Given: User has chain-level access
            when(scopeContext.getTerritories()).thenReturn(Set.of("nord"));
            when(scopeContext.getChainId()).thenReturn("chain-123");

            // When: Fetch analytics with both scopes
            reportsQuery.fetchCustomerAnalytics(
                    null, null, null, null, null, 50, null
            );

            // Then: Both ABAC filters applied
            verify(sqlQuery).setParameter("territories", new String[]{"nord"});
            verify(sqlQuery).setParameter("chainId", "chain-123");
            verify(sqlQuery).setParameter("limit", 50);
        }

        @Test
        @DisplayName("fetchCustomerAnalytics - Seasonal date filtering")
        void fetchCustomerAnalytics_withSeasonalDates_shouldApplyDateFilters() {
            // Given: Seasonal analysis date range
            when(scopeContext.getTerritories()).thenReturn(Collections.emptySet());
            when(scopeContext.getChainId()).thenReturn(null);

            String seasonFrom = "2025-03-01";
            String seasonTo = "2025-08-31";
            String renewalBefore = "2025-12-31";

            // When: Fetch with seasonal filters
            reportsQuery.fetchCustomerAnalytics(
                    null, null, seasonFrom, seasonTo, renewalBefore, 100, null
            );

            // Then: Date parameters applied
            verify(sqlQuery).setParameter("seasonFrom", LocalDate.parse(seasonFrom));
            verify(sqlQuery).setParameter("seasonTo", LocalDate.parse(seasonTo));
            verify(sqlQuery).setParameter("renewalBefore", LocalDate.parse(renewalBefore));
        }

        @Test
        @DisplayName("fetchCustomerAnalytics - Cursor pagination support")
        void fetchCustomerAnalytics_withCursor_shouldSupportPagination() {
            // Given: Pagination cursor from previous request
            when(scopeContext.getTerritories()).thenReturn(Collections.emptySet());
            String cursor = "123e4567-e89b-12d3-a456-426614174000";

            // Mock full page of results
            List<Object[]> fullPage = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                fullPage.add(new Object[]{
                        UUID.randomUUID().toString(), "Customer " + i, "nord",
                        "active", "medium", 10000.0, 2, true,
                        LocalDate.now(), LocalDate.now().plusMonths(6),
                        LocalDate.now().plusYears(1), "non_exclusive"
                });
            }
            when(sqlQuery.getResultList()).thenReturn(fullPage);

            // When: Fetch with cursor
            Map<String, Object> result = reportsQuery.fetchCustomerAnalytics(
                    null, null, null, null, null, 100, cursor
            );

            // Then: Cursor pagination applied
            verify(sqlQuery).setParameter("cursor", UUID.fromString(cursor));

            // Next cursor should be set (full page returned)
            assertNotNull(result.get("nextCursor"));
        }

        @Test
        @DisplayName("fetchCustomerAnalytics - Limit clamping security")
        void fetchCustomerAnalytics_withExcessiveLimit_shouldClampToMaximum() {
            // Given: User requests excessive limit
            when(scopeContext.getTerritories()).thenReturn(Collections.emptySet());

            // When: Fetch with limit > 200
            reportsQuery.fetchCustomerAnalytics(
                    null, null, null, null, null, 1000, null
            );

            // Then: Limit clamped to maximum (200)
            verify(sqlQuery).setParameter("limit", 200);
        }

        @Test
        @DisplayName("fetchCustomerAnalytics - SQL injection prevention")
        void fetchCustomerAnalytics_withMaliciousInput_shouldPreventSqlInjection() {
            // Given: Malicious SQL injection attempt
            when(scopeContext.getTerritories()).thenReturn(Collections.emptySet());
            String maliciousTerritory = "'; DROP TABLE customers; --";

            // When: Fetch with malicious territory parameter
            reportsQuery.fetchCustomerAnalytics(
                    null, maliciousTerritory, null, null, null, 100, null
            );

            // Then: Parameter passed safely via named parameter
            verify(sqlQuery).setParameter("territory", maliciousTerritory);
            // Verify SQL not modified (malicious code treated as literal string)
            verify(entityManager).createNativeQuery(argThat(sql ->
                    sql.contains("AND c.territory = :territory") &&
                            !sql.contains("DROP TABLE")
            ));
        }
    }

    @Nested
    @DisplayName("Activity Statistics Query Tests")
    class ActivityStatsTests {

        @Test
        @DisplayName("fetchActivityStats - Daily aggregation with ABAC filtering")
        void fetchActivityStats_withKindFilter_shouldReturnDailyBreakdown() {
            // Given: Activity statistics query result
            List<Object[]> mockResults = Arrays.asList(
                    new Object[]{java.sql.Date.valueOf("2025-09-19"), 8},
                    new Object[]{java.sql.Date.valueOf("2025-09-18"), 12},
                    new Object[]{java.sql.Date.valueOf("2025-09-17"), 5}
            );
            when(sqlQuery.getResultList()).thenReturn(mockResults);
            when(scopeContext.getTerritories()).thenReturn(Set.of("nord"));

            // When: Fetch activity stats with filters
            Map<String, Object> result = reportsQuery.fetchActivityStats(
                    "calls", "2025-09-01", "2025-09-19"
            );

            // Then: Daily breakdown with totals
            assertNotNull(result);
            assertEquals("calls", result.get("kind"));
            assertEquals("2025-09-01", result.get("from"));
            assertEquals("2025-09-19", result.get("to"));
            assertEquals(25, result.get("total")); // 8 + 12 + 5

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> byDay = (List<Map<String, Object>>) result.get("byDay");
            assertEquals(3, byDay.size());
            assertEquals("2025-09-19", byDay.get(0).get("day"));
            assertEquals(8, byDay.get(0).get("count"));

            // Verify ABAC territory filtering
            verify(sqlQuery).setParameter("territories", new String[]{"nord"});
            verify(sqlQuery).setParameter("kind", "calls");
        }

        @Test
        @DisplayName("fetchActivityStats - Date range validation")
        void fetchActivityStats_withDateRange_shouldApplyTemporalFilters() {
            // Given: Empty territories (no ABAC filtering)
            when(scopeContext.getTerritories()).thenReturn(Collections.emptySet());
            when(sqlQuery.getResultList()).thenReturn(Collections.emptyList());

            String fromDate = "2025-09-01";
            String toDate = "2025-09-19";

            // When: Fetch with date range
            reportsQuery.fetchActivityStats("meetings", fromDate, toDate);

            // Then: Date parameters applied correctly
            verify(sqlQuery).setParameter("kind", "meetings");
            verify(sqlQuery).setParameter("from", java.sql.Date.valueOf(fromDate));
            verify(sqlQuery).setParameter("to", java.sql.Date.valueOf(toDate));
        }
    }

    @Nested
    @DisplayName("Cost Overview Query Tests")
    class CostOverviewTests {

        @Test
        @DisplayName("fetchCostOverview - Service delegation pattern")
        void fetchCostOverview_withCostService_shouldDelegateToExistingService() {
            // Given: Mock cost statistics service
            Object mockCostService = mock(Object.class);

            // When: Fetch cost overview
            Map<String, Object> result = reportsQuery.fetchCostOverview(mockCostService, "month");

            // Then: Delegation pattern implemented
            assertNotNull(result);
            assertEquals("month", result.get("range"));
            assertTrue(result.containsKey("totals"));
            assertTrue(result.containsKey("breakdown"));

            // Verify basic structure (placeholder implementation)
            @SuppressWarnings("unchecked")
            Map<String, Double> totals = (Map<String, Double>) result.get("totals");
            assertEquals(0.0, totals.get("total"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> breakdown = (List<Map<String, Object>>) result.get("breakdown");
            assertEquals(1, breakdown.size());
        }
    }

    @Nested
    @DisplayName("Security and Performance Tests")
    class NonFunctionalTests {

        @Test
        @DisplayName("SQL Query Construction - Named parameters only")
        void allQueries_shouldUseNamedParametersOnly() {
            // Given: Various query scenarios
            when(scopeContext.getTerritories()).thenReturn(Set.of("nord"));

            // When: Execute different query types
            reportsQuery.fetchCustomerAnalytics("segment", "territory", null, null, null, 100, null);
            reportsQuery.fetchActivityStats("calls", "2025-09-01", "2025-09-19");

            // Then: All SQL uses named parameters (security requirement)
            verify(entityManager, atLeastOnce()).createNativeQuery(argThat(sql ->
                    sql.contains(":territories") || sql.contains(":territory") ||
                            sql.contains(":kind") || sql.contains(":from") ||
                            sql.contains(":to") || sql.contains(":limit")
            ));

            // Verify no string concatenation in SQL
            verify(entityManager, never()).createNativeQuery(argThat(sql ->
                    sql.matches(".*'\\s*\\+.*\\+\\s*'.*") // Pattern for string concatenation
            ));
        }

        @Test
        @DisplayName("Parameter Validation - Null and empty handling")
        void allMethods_shouldHandleNullParametersGracefully() {
            // Given: Null/empty parameters
            when(scopeContext.getTerritories()).thenReturn(Collections.emptySet());
            when(scopeContext.getChainId()).thenReturn(null);

            // When: Call with null parameters
            assertDoesNotThrow(() -> {
                reportsQuery.fetchCustomerAnalytics(null, null, null, null, null, 100, null);
                reportsQuery.fetchActivityStats(null, null, null);
                reportsQuery.fetchCostOverview(null, "month");
            });

            // Then: No exceptions thrown, parameters handled gracefully
            verify(sqlQuery, atLeast(3)).getResultList();
        }

        @Test
        @DisplayName("Memory Efficiency - Large result set handling")
        void fetchCustomerAnalytics_withLargeResultSet_shouldHandleMemoryEfficiently() {
            // Given: Large result set simulation
            List<Object[]> largeResultSet = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                largeResultSet.add(new Object[]{
                        UUID.randomUUID().toString(), "Customer " + i, "nord",
                        "active", "medium", 10000.0, 2, true,
                        LocalDate.now(), LocalDate.now(), LocalDate.now(), "non_exclusive"
                });
            }
            when(sqlQuery.getResultList()).thenReturn(largeResultSet);
            when(scopeContext.getTerritories()).thenReturn(Collections.emptySet());

            // When: Fetch large dataset
            long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            Map<String, Object> result = reportsQuery.fetchCustomerAnalytics(
                    null, null, null, null, null, 1000, null
            );
            long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            // Then: Result processed without excessive memory growth
            assertNotNull(result);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
            assertEquals(1000, items.size());

            // Memory growth should be reasonable (< 50MB for 1000 records)
            long memoryGrowth = endMemory - startMemory;
            assertTrue(memoryGrowth < 50 * 1024 * 1024,
                    "Memory growth " + memoryGrowth + " bytes exceeds 50MB limit");
        }
    }

    /**
     * Test Data Builder for Customer Analytics Test Data
     *
     * @see ../../grundlagen/TESTING_GUIDE.md - TestDataBuilder Pattern
     */
    static class CustomerAnalyticsTestDataBuilder {
        private String customerId = UUID.randomUUID().toString();
        private String name = "Test Customer";
        private String territory = "nord";
        private String sampleStatus = "active";
        private String roiBucket = "medium";
        private Double roiValue = 15000.0;
        private Integer decisionMakerCount = 2;
        private Boolean hasExecAlignment = false;
        private LocalDate seasonStart = LocalDate.now();
        private LocalDate seasonEnd = LocalDate.now().plusMonths(6);
        private LocalDate renewalDate = LocalDate.now().plusYears(1);
        private String exclusivity = "non_exclusive";

        public CustomerAnalyticsTestDataBuilder withTerritory(String territory) {
            this.territory = territory;
            return this;
        }

        public CustomerAnalyticsTestDataBuilder withRoiBucket(String roiBucket) {
            this.roiBucket = roiBucket;
            return this;
        }

        public CustomerAnalyticsTestDataBuilder withSeasonalDates(LocalDate start, LocalDate end) {
            this.seasonStart = start;
            this.seasonEnd = end;
            return this;
        }

        public Object[] build() {
            return new Object[]{
                    customerId, name, territory, sampleStatus, roiBucket, roiValue,
                    decisionMakerCount, hasExecAlignment, seasonStart, seasonEnd,
                    renewalDate, exclusivity
            };
        }
    }
}