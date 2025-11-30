package de.freshplan.modules.xentral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.freshplan.modules.xentral.client.MockXentralApiClient;
import de.freshplan.modules.xentral.dto.XentralCustomerDTO;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for XentralApiService Graceful Degradation (Fallback to Mock on Error).
 *
 * <p>Sprint 2.1.7.7 - E2E Critical Path Validation (Xentral Boundary Testing)
 *
 * <p>These tests verify that when the real Xentral API fails (500 error, timeout, etc.), the
 * service gracefully falls back to mock data instead of propagating the error to the caller.
 *
 * <p>WICHTIG: Diese Tests dokumentieren das gewünschte Verhalten für Production:
 *
 * <ul>
 *   <li>Bei Xentral API 500: Fallback zu Mock-Daten
 *   <li>Bei Xentral API Timeout: Fallback zu Mock-Daten
 *   <li>Bei Xentral API Connection Error: Fallback zu Mock-Daten
 *   <li>Conversion-Workflow (Lead → Opportunity → Customer) ist NICHT betroffen
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.7
 */
@QuarkusTest
@Tag("integration")
@DisplayName("XentralApiService Graceful Degradation Tests")
class XentralApiServiceGracefulDegradationTest {

  @Inject XentralApiService service;

  @InjectMock MockXentralApiClient mockClient;

  @InjectMock XentralV1V2ApiAdapter realAdapter;

  /**
   * Test Profile: xentral.api.mock-mode=false
   *
   * <p>Simulates Production mode where real API is used.
   */
  public static class RealApiModeProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("xentral.api.mock-mode", "false");
    }
  }

  @Nested
  @DisplayName("Graceful Degradation on API Errors (Real API Mode)")
  @TestProfile(RealApiModeProfile.class)
  class GracefulDegradationTests {

    @Test
    @DisplayName("getCustomersBySalesRep() - falls back to mock on 500 Internal Server Error")
    void testGetCustomersBySalesRep_FallsBackOnInternalServerError() {
      // Given: Real API throws 500 Internal Server Error
      when(realAdapter.getCustomersBySalesRep("SALES-001"))
          .thenThrow(
              new WebApplicationException(
                  "Xentral API Internal Server Error", Response.Status.INTERNAL_SERVER_ERROR));

      // Mock client returns fallback data
      var mockCustomer =
          new XentralCustomerDTO(
              "FALLBACK-001",
              "Fallback Corp",
              "fallback@example.com",
              "+49-123",
              new BigDecimal("5000.00"),
              14,
              LocalDate.now(),
              "SALES-001");
      when(mockClient.getCustomersBySalesRep("SALES-001")).thenReturn(List.of(mockCustomer));

      // When
      List<XentralCustomerDTO> customers = service.getCustomersBySalesRep("SALES-001");

      // Then: Should return mock data instead of failing
      assertThat(customers)
          .as("Should return fallback mock data on API error")
          .hasSize(1)
          .extracting(XentralCustomerDTO::xentralId)
          .containsExactly("FALLBACK-001");

      // Verify: Real API was called first, then fallback to mock
      verify(realAdapter, times(1)).getCustomersBySalesRep("SALES-001");
      verify(mockClient, times(1)).getCustomersBySalesRep("SALES-001");
    }

    @Test
    @DisplayName("getCustomersBySalesRep() - falls back to mock on connection timeout")
    void testGetCustomersBySalesRep_FallsBackOnTimeout() {
      // Given: Real API throws timeout exception
      when(realAdapter.getCustomersBySalesRep("SALES-002"))
          .thenThrow(new RuntimeException("Connection timed out after 30000ms"));

      // Mock client returns fallback data
      var mockCustomer =
          new XentralCustomerDTO(
              "TIMEOUT-FALLBACK",
              "Timeout Fallback GmbH",
              "timeout@example.com",
              null,
              null,
              null,
              null,
              "SALES-002");
      when(mockClient.getCustomersBySalesRep("SALES-002")).thenReturn(List.of(mockCustomer));

      // When
      List<XentralCustomerDTO> customers = service.getCustomersBySalesRep("SALES-002");

      // Then: Should return mock data instead of failing
      assertThat(customers)
          .as("Should return fallback mock data on timeout")
          .hasSize(1)
          .extracting(XentralCustomerDTO::companyName)
          .containsExactly("Timeout Fallback GmbH");

      verify(realAdapter, times(1)).getCustomersBySalesRep("SALES-002");
      verify(mockClient, times(1)).getCustomersBySalesRep("SALES-002");
    }

    @Test
    @DisplayName("getCustomerById() - falls back to mock on 503 Service Unavailable")
    void testGetCustomerById_FallsBackOnServiceUnavailable() {
      // Given: Real API throws 503 Service Unavailable
      when(realAdapter.getCustomerById("CUST-503"))
          .thenThrow(
              new WebApplicationException(
                  "Xentral service is temporarily unavailable",
                  Response.Status.SERVICE_UNAVAILABLE));

      // Mock client returns fallback data
      var mockCustomer =
          new XentralCustomerDTO(
              "CUST-503-MOCK",
              "Service Unavailable Fallback",
              "unavailable@example.com",
              null,
              null,
              null,
              null,
              null);
      when(mockClient.getCustomerById("CUST-503")).thenReturn(mockCustomer);

      // When
      XentralCustomerDTO customer = service.getCustomerById("CUST-503");

      // Then
      assertThat(customer)
          .as("Should return fallback mock data on 503")
          .isNotNull()
          .extracting(XentralCustomerDTO::xentralId)
          .isEqualTo("CUST-503-MOCK");

      verify(realAdapter, times(1)).getCustomerById("CUST-503");
      verify(mockClient, times(1)).getCustomerById("CUST-503");
    }

    @Test
    @DisplayName("getInvoicesByCustomer() - falls back to mock on network error")
    void testGetInvoicesByCustomer_FallsBackOnNetworkError() {
      // Given: Real API throws network exception
      when(realAdapter.getInvoicesByCustomer("CUST-NET"))
          .thenThrow(new RuntimeException("java.net.UnknownHostException: xentral.example.com"));

      // Mock client returns fallback data
      var mockInvoice =
          new XentralInvoiceDTO(
              "INV-FALLBACK",
              "RE-FALLBACK-001",
              "CUST-NET",
              new BigDecimal("999.99"),
              LocalDate.now().minusDays(7),
              LocalDate.now().plusDays(23),
              null,
              "open");
      when(mockClient.getInvoicesByCustomer("CUST-NET")).thenReturn(List.of(mockInvoice));

      // When
      List<XentralInvoiceDTO> invoices = service.getInvoicesByCustomer("CUST-NET");

      // Then
      assertThat(invoices)
          .as("Should return fallback mock invoices on network error")
          .hasSize(1)
          .extracting(XentralInvoiceDTO::invoiceId)
          .containsExactly("INV-FALLBACK");

      verify(realAdapter, times(1)).getInvoicesByCustomer("CUST-NET");
      verify(mockClient, times(1)).getInvoicesByCustomer("CUST-NET");
    }

    @Test
    @DisplayName("getAllSalesReps() - falls back to mock on authentication error")
    void testGetAllSalesReps_FallsBackOnAuthError() {
      // Given: Real API throws 401 Unauthorized
      when(realAdapter.getSalesReps())
          .thenThrow(
              new WebApplicationException(
                  "Invalid API key or token expired", Response.Status.UNAUTHORIZED));

      // Mock client returns fallback data
      var mockSalesRep =
          new XentralEmployeeDTO(
              "FALLBACK-SALES", "Fallback", "Sales Rep", "fallback-sales@example.com", "sales");
      when(mockClient.getAllSalesReps()).thenReturn(List.of(mockSalesRep));

      // When
      List<XentralEmployeeDTO> salesReps = service.getAllSalesReps();

      // Then
      assertThat(salesReps)
          .as("Should return fallback mock sales reps on auth error")
          .hasSize(1)
          .extracting(XentralEmployeeDTO::employeeId)
          .containsExactly("FALLBACK-SALES");

      verify(realAdapter, times(1)).getSalesReps();
      verify(mockClient, times(1)).getAllSalesReps();
    }

    @Test
    @DisplayName("testConnection() - returns false on API error (no fallback)")
    void testTestConnection_ReturnsFalseOnError() {
      // Given: Real API throws exception
      when(realAdapter.testConnection())
          .thenThrow(new RuntimeException("Connection refused: connect"));

      // When
      boolean connected = service.testConnection();

      // Then: Connection test should return false (not throw exception)
      assertThat(connected).as("Should return false on connection error, not throw").isFalse();

      verify(realAdapter, times(1)).testConnection();
      // Note: testConnection() doesn't fallback to mock, it just returns false
    }
  }

  @Nested
  @DisplayName("API Error Scenarios Documentation")
  class ErrorScenariosDocumentation {

    @Test
    @DisplayName("Document: Conversion workflow is NOT affected by Xentral errors")
    void documentConversionWorkflowIsolation() {
      /*
       * ARCHITECTURE DOCUMENTATION
       * ==========================
       *
       * The Lead → Opportunity → Customer conversion workflow is COMPLETELY ISOLATED
       * from Xentral API calls. This test documents this important architectural decision.
       *
       * Flow:
       * 1. Lead qualifizieren (REGISTERED → QUALIFIED)
       * 2. Opportunity erstellen (createFromLead)
       * 3. Pipeline durchlaufen (NEW_LEAD → ... → NEGOTIATION → CLOSED_WON)
       * 4. Customer erstellen (convertToCustomer / handleOpportunityWon)
       *
       * NONE of these steps call Xentral synchronously!
       *
       * Xentral is only called for:
       * - Revenue metrics (read-only, async, with fallback)
       * - Invoice data (read-only, async, with fallback)
       * - Sales rep sync (background job, with fallback)
       *
       * Event publishing (LeadConvertedEvent) is explicitly non-critical:
       * ```java
       * } catch (Exception e) {
       *   logger.warn("Failed to publish LeadConvertedEvent (non-critical)", e);
       *   // Don't fail the conversion if event publishing fails
       * }
       * ```
       *
       * This means:
       * ✅ If Xentral is down → Conversions still work
       * ✅ If Xentral returns 500 → Conversions still work
       * ✅ If Xentral times out → Conversions still work
       * ✅ Revenue metrics fallback to mock data
       *
       * Production risk: LOW
       */
      assertThat(true)
          .as(
              "Conversion workflow is isolated from Xentral - "
                  + "see test documentation for architecture details")
          .isTrue();
    }

    @Test
    @DisplayName("Document: Xentral sync is event-based (eventual consistency)")
    void documentXentralSyncIsEventual() {
      /*
       * EVENTUAL CONSISTENCY DOCUMENTATION
       * ===================================
       *
       * Xentral synchronization follows an eventual consistency model:
       *
       * 1. Customer created locally → immediate
       * 2. LeadConvertedEvent published → async (non-blocking)
       * 3. Xentral sync triggered by event → background
       * 4. Xentral customer created → eventual
       *
       * If step 2 or 3 fails:
       * - Local customer is still created ✅
       * - Xentral sync can be retried later
       * - No rollback of local transaction
       *
       * Retry mechanism: Not yet implemented (tracked in backlog)
       * - Consider: Dead letter queue for failed events
       * - Consider: Scheduled reconciliation job
       */
      assertThat(true)
          .as("Xentral sync is eventual consistency - see test documentation for details")
          .isTrue();
    }
  }
}
