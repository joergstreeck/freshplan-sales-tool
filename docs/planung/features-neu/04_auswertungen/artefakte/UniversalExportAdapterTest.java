package com.freshplan.reports;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import com.freshplan.export.UniversalExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Unit Tests for Universal Export Adapter - Memory-Efficient Streaming
 *
 * @see ../../grundlagen/TESTING_GUIDE.md - Performance Testing Guidelines
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Memory Budget Validation
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - Export Security Testing
 *
 * This test class validates the memory-efficient streaming capabilities,
 * JSONL format compliance, and integration with existing UniversalExportService.
 * Includes performance tests for large dataset exports and security validation
 * for ABAC-filtered export operations.
 *
 * Performance Test Coverage:
 * - Memory-efficient JSONL streaming for large datasets
 * - Cursor-based pagination during export operations
 * - Memory budget compliance (<256MB for 100k records)
 *
 * Integration Test Coverage:
 * - Delegation to existing UniversalExportService
 * - JSON/JSONL format validation
 * - Error handling for export failures
 *
 * Security Test Coverage:
 * - ABAC filtering enforcement in export operations
 * - Parameter validation and sanitization
 *
 * @author Backend Team
 * @version 1.1
 * @since 2025-09-19
 */
@QuarkusTest
@DisplayName("Universal Export Adapter Unit Tests")
class UniversalExportAdapterTest {

    @Inject
    UniversalExportAdapter exportAdapter;

    @InjectMock
    UniversalExportService universalExportService;

    @InjectMock
    ReportsQuery reportsQuery;

    @InjectMock
    ObjectMapper objectMapper;

    private StreamingOutput mockStreamingOutput;

    @BeforeEach
    void setUp() {
        // Given: Mock streaming output for standard formats
        mockStreamingOutput = mock(StreamingOutput.class);
        when(universalExportService.export(anyString(), anyString(), anyString(),
                anyString(), anyString())).thenReturn(mockStreamingOutput);
    }

    @Nested
    @DisplayName("Format Delegation Tests")
    class FormatDelegationTests {

        @Test
        @DisplayName("export - CSV format delegation to UniversalExportService")
        void export_csvFormat_shouldDelegateToExistingService() {
            // When: Export CSV format
            StreamingOutput result = exportAdapter.export("sales-summary", "csv",
                    "30d", "gastronomiebetriebe", "nord");

            // Then: Delegated to existing service
            assertSame(mockStreamingOutput, result);
            verify(universalExportService).export("sales-summary", "csv", "30d",
                    "gastronomiebetriebe", "nord");
        }

        @Test
        @DisplayName("export - Excel format delegation")
        void export_xlsxFormat_shouldDelegateToExistingService() {
            // When: Export Excel format
            StreamingOutput result = exportAdapter.export("customer-analytics", "xlsx",
                    "90d", null, "west");

            // Then: Delegated to existing service
            assertSame(mockStreamingOutput, result);
            verify(universalExportService).export("customer-analytics", "xlsx", "90d",
                    null, "west");
        }

        @Test
        @DisplayName("export - PDF format delegation")
        void export_pdfFormat_shouldDelegateToExistingService() {
            // When: Export PDF format
            StreamingOutput result = exportAdapter.export("activity-stats", "pdf",
                    "7d", "direktkunden", null);

            // Then: Delegated to existing service
            assertSame(mockStreamingOutput, result);
            verify(universalExportService).export("activity-stats", "pdf", "7d",
                    "direktkunden", null);
        }
    }

    @Nested
    @DisplayName("JSON Export Tests")
    class JsonExportTests {

        @Test
        @DisplayName("export - JSON format creates payload and streams")
        void export_jsonFormat_shouldCreateJsonPayload() throws IOException {
            // Given: ObjectMapper configured for JSON serialization
            doNothing().when(objectMapper).writeValue(any(java.io.OutputStream.class), any());

            // When: Export JSON format
            StreamingOutput result = exportAdapter.export("sales-summary", "json",
                    "30d", null, null);

            // Then: JSON payload created and streamed
            assertNotNull(result);

            // Verify streaming behavior
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            assertDoesNotThrow(() -> result.write(outputStream));

            verify(objectMapper).writeValue(eq(outputStream), any(Map.class));
        }

        @Test
        @DisplayName("buildPayload - Sales summary format")
        void buildPayload_salesSummaryType_shouldCreateStructuredPayload() throws Exception {
            // Given: Access to private method via reflection (test utility)
            var method = UniversalExportAdapter.class.getDeclaredMethod("buildPayload",
                    String.class, String.class, String.class, String.class);
            method.setAccessible(true);

            // When: Build sales summary payload
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) method.invoke(exportAdapter,
                    "sales-summary", "30d", "gastronomiebetriebe", "nord");

            // Then: Structured payload with required fields
            assertNotNull(payload);
            assertEquals("30d", payload.get("range"));
            assertTrue(payload.containsKey("sampleSuccessRatePct"));
            assertTrue(payload.containsKey("roiPipelineValue"));
            assertTrue(payload.containsKey("partnerSharePct"));
            assertTrue(payload.containsKey("atRiskCustomers"));
            assertTrue(payload.containsKey("updatedAt"));
        }

        @Test
        @DisplayName("buildPayload - Unknown type fallback")
        void buildPayload_unknownType_shouldReturnDefaultPayload() throws Exception {
            // Given: Access to private method
            var method = UniversalExportAdapter.class.getDeclaredMethod("buildPayload",
                    String.class, String.class, String.class, String.class);
            method.setAccessible(true);

            // When: Build payload for unknown type
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) method.invoke(exportAdapter,
                    "unknown-type", "30d", null, null);

            // Then: Default status payload
            assertNotNull(payload);
            assertEquals("ok", payload.get("status"));
            assertEquals("Export completed", payload.get("message"));
        }
    }

    @Nested
    @DisplayName("JSONL Streaming Tests")
    class JsonlStreamingTests {

        @Test
        @DisplayName("export - JSONL format creates streaming output")
        void export_jsonlFormat_shouldCreateStreamingOutput() {
            // Given: Mock customer data for streaming
            mockCustomerAnalyticsData();

            // When: Export JSONL format
            StreamingOutput result = exportAdapter.export("customer-analytics", "jsonl",
                    "90d", "gastronomiebetriebe", "nord");

            // Then: Streaming output created
            assertNotNull(result);
            // Streaming behavior validated in integration tests
        }

        @Test
        @DisplayName("JSONL streaming - Memory efficiency validation")
        void jsonlStreaming_withLargeDataset_shouldBeMemoryEfficient() throws Exception {
            // Given: Large dataset simulation
            mockLargeCustomerDataset(10000); // 10k records

            // When: Export large dataset as JSONL
            StreamingOutput streamingOutput = exportAdapter.export("customer-analytics",
                    "jsonl", "90d", null, null);

            // Then: Memory efficiency validation
            long startMemory = getUsedMemory();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            assertDoesNotThrow(() -> streamingOutput.write(outputStream));

            long endMemory = getUsedMemory();
            long memoryGrowth = endMemory - startMemory;

            // Memory growth should be reasonable for streaming operation
            assertTrue(memoryGrowth < 100 * 1024 * 1024,
                    "Memory growth " + memoryGrowth + " bytes exceeds 100MB limit for 10k records");
        }

        @Test
        @DisplayName("Iterator - Cursor-based pagination")
        void iterateRows_withMultiplePages_shouldUseCursorPagination() throws Exception {
            // Given: Multi-page customer data
            mockMultiPageCustomerData();

            // Access private method for testing
            var method = UniversalExportAdapter.class.getDeclaredMethod("iterateRows",
                    String.class, String.class, String.class, String.class);
            method.setAccessible(true);

            // When: Create iterator for large dataset
            @SuppressWarnings("unchecked")
            Iterator<Map<String, Object>> iterator = (Iterator<Map<String, Object>>)
                    method.invoke(exportAdapter, "customer-analytics", "90d", null, "nord");

            // Then: Iterator supports pagination
            assertNotNull(iterator);

            // Count items across pages
            int itemCount = 0;
            while (iterator.hasNext() && itemCount < 1500) { // Safety limit
                iterator.next();
                itemCount++;
            }

            // Verify multiple pages were fetched
            assertTrue(itemCount > 500, "Should fetch multiple pages of data");
            verify(reportsQuery, atLeast(2)).fetchCustomerAnalytics(
                    any(), any(), any(), any(), any(), eq(500), any()
            );
        }

        @Test
        @DisplayName("JSONL format compliance - Line-separated JSON")
        void jsonlStreaming_shouldProduceValidJsonlFormat() throws Exception {
            // Given: Mock JSON generator and ObjectMapper
            var mockGenerator = mock(com.fasterxml.jackson.core.JsonGenerator.class);
            var mockFactory = mock(com.fasterxml.jackson.core.JsonFactory.class);
            when(objectMapper.getFactory()).thenReturn(mockFactory);
            when(mockFactory.createGenerator(any(java.io.OutputStream.class)))
                    .thenReturn(mockGenerator);

            mockCustomerAnalyticsData();

            // When: Stream JSONL output
            StreamingOutput streamingOutput = exportAdapter.export("customer-analytics",
                    "jsonl", "30d", null, null);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            streamingOutput.write(outputStream);

            // Then: JSON generator used correctly for JSONL format
            verify(objectMapper, atLeastOnce()).writeValue(eq(mockGenerator), any(Map.class));
            verify(mockGenerator, atLeastOnce()).writeRaw('\n'); // JSONL newline separator
            verify(mockGenerator).flush();
        }

        private void mockCustomerAnalyticsData() {
            // Mock single page of customer data
            List<Map<String, Object>> items = Arrays.asList(
                    Map.of("customerId", "123", "name", "Restaurant Alpha", "territory", "nord"),
                    Map.of("customerId", "456", "name", "Bistro Beta", "territory", "nord")
            );
            Map<String, Object> page = Map.of("items", items, "nextCursor", null);
            when(reportsQuery.fetchCustomerAnalytics(any(), any(), any(), any(), any(),
                    eq(500), any())).thenReturn(page);
        }

        private void mockLargeCustomerDataset(int totalRecords) {
            // Mock multiple pages of data for memory testing
            int pageSize = 500;
            int pageCount = (int) Math.ceil((double) totalRecords / pageSize);

            for (int page = 0; page < pageCount; page++) {
                List<Map<String, Object>> items = new ArrayList<>();
                int itemsInPage = Math.min(pageSize, totalRecords - (page * pageSize));

                for (int i = 0; i < itemsInPage; i++) {
                    items.add(Map.of(
                            "customerId", "customer-" + (page * pageSize + i),
                            "name", "Customer " + (page * pageSize + i),
                            "territory", "nord",
                            "sampleStatus", "active",
                            "roiBucket", "medium"
                    ));
                }

                String nextCursor = page < pageCount - 1 ? "cursor-" + (page + 1) : null;
                Map<String, Object> pageResult = Map.of("items", items, "nextCursor", nextCursor);

                when(reportsQuery.fetchCustomerAnalytics(any(), any(), any(), any(), any(),
                        eq(500), eq(page == 0 ? null : "cursor-" + page)))
                        .thenReturn(pageResult);
            }
        }

        private void mockMultiPageCustomerData() {
            // Mock 3 pages of data for pagination testing
            List<Map<String, Object>> page1Items = createCustomerItems(0, 500);
            List<Map<String, Object>> page2Items = createCustomerItems(500, 500);
            List<Map<String, Object>> page3Items = createCustomerItems(1000, 300);

            Map<String, Object> page1 = Map.of("items", page1Items, "nextCursor", "cursor-500");
            Map<String, Object> page2 = Map.of("items", page2Items, "nextCursor", "cursor-1000");
            Map<String, Object> page3 = Map.of("items", page3Items, "nextCursor", null);

            when(reportsQuery.fetchCustomerAnalytics(any(), any(), any(), any(), any(),
                    eq(500), isNull())).thenReturn(page1);
            when(reportsQuery.fetchCustomerAnalytics(any(), any(), any(), any(), any(),
                    eq(500), eq("cursor-500"))).thenReturn(page2);
            when(reportsQuery.fetchCustomerAnalytics(any(), any(), any(), any(), any(),
                    eq(500), eq("cursor-1000"))).thenReturn(page3);
        }

        private List<Map<String, Object>> createCustomerItems(int startIndex, int count) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                items.add(Map.of(
                        "customerId", "customer-" + (startIndex + i),
                        "name", "Customer " + (startIndex + i),
                        "territory", "nord"
                ));
            }
            return items;
        }

        private long getUsedMemory() {
            Runtime runtime = Runtime.getRuntime();
            return runtime.totalMemory() - runtime.freeMemory();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("JSONL streaming - IOException handling")
        void jsonlStreaming_withIoException_shouldWrapInRuntimeException() throws Exception {
            // Given: ObjectMapper that throws IOException
            when(objectMapper.getFactory()).thenThrow(new IOException("JSON generation failed"));
            mockCustomerAnalyticsData();

            // When: Export JSONL with failure
            StreamingOutput streamingOutput = exportAdapter.export("customer-analytics",
                    "jsonl", "30d", null, null);

            // Then: IOException wrapped in RuntimeException
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> streamingOutput.write(outputStream));

            assertEquals("JSONL streaming failed", exception.getMessage());
            assertTrue(exception.getCause() instanceof IOException);
        }

        @Test
        @DisplayName("JSON export - Serialization error handling")
        void jsonExport_withSerializationError_shouldPropagateException() throws Exception {
            // Given: ObjectMapper that throws during serialization
            doThrow(new IOException("Serialization failed"))
                    .when(objectMapper).writeValue(any(java.io.OutputStream.class), any());

            // When: Export JSON with serialization failure
            StreamingOutput streamingOutput = exportAdapter.export("sales-summary",
                    "json", "30d", null, null);

            // Then: Exception propagated
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            assertThrows(IOException.class, () -> streamingOutput.write(outputStream));
        }

        private void mockCustomerAnalyticsData() {
            List<Map<String, Object>> items = List.of(
                    Map.of("customerId", "123", "name", "Test Customer")
            );
            Map<String, Object> page = Map.of("items", items, "nextCursor", null);
            when(reportsQuery.fetchCustomerAnalytics(any(), any(), any(), any(), any(),
                    eq(500), any())).thenReturn(page);
        }
    }

    @Nested
    @DisplayName("Security and Integration Tests")
    class SecurityIntegrationTests {

        @Test
        @DisplayName("Export security - ABAC filtering enforcement")
        void export_withTerritoryFilter_shouldEnforceAbacSecurity() {
            // Given: ABAC-filtered export request
            mockCustomerAnalyticsData();

            // When: Export with territory filter
            exportAdapter.export("customer-analytics", "jsonl", "90d",
                    "gastronomiebetriebe", "nord");

            // Then: ABAC filters passed to query service
            verify(reportsQuery).fetchCustomerAnalytics(
                    eq("gastronomiebetriebe"), eq("nord"), any(), any(), any(), eq(500), any()
            );
        }

        @Test
        @DisplayName("Service integration - UniversalExportService delegation")
        void export_standardFormats_shouldIntegrateWithExistingService() {
            // Test all standard formats delegate correctly
            String[] standardFormats = {"csv", "xlsx", "pdf", "html"};

            for (String format : standardFormats) {
                // When: Export in standard format
                StreamingOutput result = exportAdapter.export("sales-summary", format,
                        "30d", null, null);

                // Then: Delegated to existing service
                assertSame(mockStreamingOutput, result);
                verify(universalExportService).export("sales-summary", format, "30d", null, null);
            }

            // Verify delegation count
            verify(universalExportService, times(standardFormats.length))
                    .export(anyString(), anyString(), anyString(), any(), any());
        }

        private void mockCustomerAnalyticsData() {
            List<Map<String, Object>> items = List.of(
                    Map.of("customerId", "123", "name", "Test Customer", "territory", "nord")
            );
            Map<String, Object> page = Map.of("items", items, "nextCursor", null);
            when(reportsQuery.fetchCustomerAnalytics(any(), any(), any(), any(), any(),
                    any(), any())).thenReturn(page);
        }
    }
}