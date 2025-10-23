package de.freshplan.domain.customer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.dto.PaymentBehavior;
import de.freshplan.domain.customer.dto.RevenueMetrics;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import de.freshplan.modules.xentral.service.XentralApiService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit and Integration Tests for RevenueMetricsService
 *
 * <p>Sprint 2.1.7.2: Customer Dashboard - Revenue Metrics from Xentral
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@DisplayName("RevenueMetricsService - Revenue Metrics from Xentral")
class RevenueMetricsServiceTest {

  @Inject RevenueMetricsService revenueMetricsService;

  @InjectMock XentralApiService xentralApiService;

  @Inject EntityManager entityManager;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean customers table before each test
    entityManager.createNativeQuery("DELETE FROM customers").executeUpdate();
    entityManager.flush();

    // Create test customer WITH Xentral-ID using native SQL
    entityManager
        .createNativeQuery(
            """
            INSERT INTO customers (
              id, customer_number, company_name, customer_type, xentral_customer_id,
              created_at, created_by, updated_at, updated_by
            ) VALUES (
              'c0000000-0001-0000-0000-000000000001'::uuid,
              'KD-TEST-001',
              'Test Hotel GmbH',
              'UNTERNEHMEN',
              'XC-10001',
              NOW(),
              'TEST_USER',
              NOW(),
              'TEST_USER'
            )
            """)
        .executeUpdate();

    // Create test customer WITHOUT Xentral-ID using native SQL
    entityManager
        .createNativeQuery(
            """
            INSERT INTO customers (
              id, customer_number, company_name, customer_type, xentral_customer_id,
              created_at, created_by, updated_at, updated_by
            ) VALUES (
              'c0000000-0001-0000-0000-000000000002'::uuid,
              'KD-TEST-002',
              'Test Restaurant GmbH',
              'UNTERNEHMEN',
              NULL,
              NOW(),
              'TEST_USER',
              NOW(),
              'TEST_USER'
            )
            """)
        .executeUpdate();

    entityManager.flush();
  }

  @Test
  @Transactional
  @DisplayName("getRevenueMetrics() - customer not found → throws NotFoundException")
  void getRevenueMetrics_CustomerNotFound_ThrowsNotFoundException() {
    // GIVEN: Non-existent customer ID
    UUID nonExistentId = UUID.randomUUID();

    // WHEN: Getting revenue metrics
    // THEN: Should throw NotFoundException
    assertThrows(
        NotFoundException.class,
        () -> revenueMetricsService.getRevenueMetrics(nonExistentId),
        "Should throw NotFoundException for non-existent customer");
  }

  @Test
  @Transactional
  @DisplayName("getRevenueMetrics() - customer without Xentral-ID → empty metrics")
  void getRevenueMetrics_CustomerWithoutXentralId_ReturnsEmptyMetrics() {
    // GIVEN: Customer without Xentral-ID (will use seed data from V90001)
    // Customer with customer_number = 'KD-DEV-002' has xentral_customer_id = NULL
    UUID customerId = UUID.fromString("c0000000-0001-0000-0000-000000000002");

    // WHEN: Getting revenue metrics
    RevenueMetrics metrics = revenueMetricsService.getRevenueMetrics(customerId);

    // THEN: Should return empty metrics
    assertNotNull(metrics, "Metrics should not be null");
    assertEquals(
        BigDecimal.ZERO, metrics.revenue30Days(), "Revenue 30 days should be zero");
    assertEquals(
        BigDecimal.ZERO, metrics.revenue90Days(), "Revenue 90 days should be zero");
    assertEquals(
        BigDecimal.ZERO, metrics.revenue365Days(), "Revenue 365 days should be zero");
    assertEquals(
        PaymentBehavior.N_A, metrics.paymentBehavior(), "Payment behavior should be N_A");
    assertNull(metrics.averageDaysToPay(), "Average days to pay should be null");
    assertNull(metrics.lastOrderDate(), "Last order date should be null");
  }

  @Test
  @Transactional
  @DisplayName("getRevenueMetrics() - customer with Xentral-ID but no invoices → empty metrics")
  void getRevenueMetrics_CustomerWithXentralIdButNoInvoices_ReturnsEmptyMetrics() {
    // GIVEN: Customer with Xentral-ID (from seed data)
    UUID customerId = UUID.fromString("c0000000-0001-0000-0000-000000000001");
    String xentralCustomerId = "XC-10001";

    // Mock: No invoices returned from Xentral
    when(xentralApiService.getInvoicesByCustomer(xentralCustomerId))
        .thenReturn(new ArrayList<>());

    // WHEN: Getting revenue metrics
    RevenueMetrics metrics = revenueMetricsService.getRevenueMetrics(customerId);

    // THEN: Should return empty metrics
    assertNotNull(metrics, "Metrics should not be null");
    assertEquals(
        BigDecimal.ZERO, metrics.revenue30Days(), "Revenue 30 days should be zero");
    assertEquals(
        BigDecimal.ZERO, metrics.revenue90Days(), "Revenue 90 days should be zero");
    assertEquals(
        BigDecimal.ZERO, metrics.revenue365Days(), "Revenue 365 days should be zero");
    assertEquals(
        PaymentBehavior.N_A, metrics.paymentBehavior(), "Payment behavior should be N_A");
    assertNull(metrics.averageDaysToPay(), "Average days to pay should be null");
    assertNull(metrics.lastOrderDate(), "Last order date should be null");

    verify(xentralApiService, times(1)).getInvoicesByCustomer(xentralCustomerId);
  }

  @Test
  @Transactional
  @DisplayName("calculateRevenue() - calculates revenue for 30/90/365 days correctly")
  void getRevenueMetrics_CalculatesRevenuePeriods_Correctly() {
    // GIVEN: Customer with Xentral-ID and invoices
    UUID customerId = UUID.fromString("c0000000-0001-0000-0000-000000000001");
    String xentralCustomerId = "XC-10001";

    LocalDate now = LocalDate.now();

    // Create test invoices
    List<XentralInvoiceDTO> invoices = List.of(
        // Invoice from 15 days ago (in 30-day period)
        new XentralInvoiceDTO(
            "INV-001",                      // invoiceId
            "R-2025-001",                   // invoiceNumber
            xentralCustomerId,              // customerId
            new BigDecimal("1000.00"),      // amount
            now.minusDays(15),              // invoiceDate
            now.minusDays(15).plusDays(14), // dueDate
            now.minusDays(1),               // paymentDate
            "PAID"),                        // status
        // Invoice from 45 days ago (in 90-day period, but NOT in 30-day)
        new XentralInvoiceDTO(
            "INV-002",
            "R-2025-002",
            xentralCustomerId,
            new BigDecimal("2000.00"),
            now.minusDays(45),
            now.minusDays(45).plusDays(14),
            now.minusDays(31),
            "PAID"),
        // Invoice from 200 days ago (in 365-day period, but NOT in 30/90-day)
        new XentralInvoiceDTO(
            "INV-003",
            "R-2024-123",
            xentralCustomerId,
            new BigDecimal("3000.00"),
            now.minusDays(200),
            now.minusDays(200).plusDays(14),
            now.minusDays(186),
            "PAID"),
        // Invoice from 400 days ago (OUTSIDE all periods)
        new XentralInvoiceDTO(
            "INV-004",
            "R-2024-001",
            xentralCustomerId,
            new BigDecimal("9999.00"),
            now.minusDays(400),
            now.minusDays(400).plusDays(14),
            now.minusDays(386),
            "PAID"));

    when(xentralApiService.getInvoicesByCustomer(xentralCustomerId)).thenReturn(invoices);

    // WHEN: Getting revenue metrics
    RevenueMetrics metrics = revenueMetricsService.getRevenueMetrics(customerId);

    // THEN: Revenue should be calculated correctly for each period
    assertNotNull(metrics, "Metrics should not be null");

    // 30 days: Only INV-001 (1000)
    assertEquals(
        new BigDecimal("1000.00"),
        metrics.revenue30Days(),
        "Revenue 30 days should be 1000.00");

    // 90 days: INV-001 + INV-002 (1000 + 2000 = 3000)
    assertEquals(
        new BigDecimal("3000.00"),
        metrics.revenue90Days(),
        "Revenue 90 days should be 3000.00");

    // 365 days: INV-001 + INV-002 + INV-003 (1000 + 2000 + 3000 = 6000)
    assertEquals(
        new BigDecimal("6000.00"),
        metrics.revenue365Days(),
        "Revenue 365 days should be 6000.00");

    // Payment behavior should be GOOD (14 days hardcoded in Mock-Mode)
    assertEquals(
        PaymentBehavior.GOOD,
        metrics.paymentBehavior(),
        "Payment behavior should be GOOD");
    assertEquals(14, metrics.averageDaysToPay(), "Average days to pay should be 14 (Mock-Mode)");

    // Last order date should be from INV-001 (15 days ago)
    assertEquals(
        now.minusDays(15), metrics.lastOrderDate(), "Last order date should be from INV-001");

    verify(xentralApiService, times(1)).getInvoicesByCustomer(xentralCustomerId);
  }

  @Test
  @Transactional
  @DisplayName("getRevenueMetrics() - last order date is most recent invoice")
  void getRevenueMetrics_LastOrderDate_IsMostRecentInvoice() {
    // GIVEN: Customer with multiple invoices
    UUID customerId = UUID.fromString("c0000000-0001-0000-0000-000000000001");
    String xentralCustomerId = "XC-10001";

    LocalDate now = LocalDate.now();
    LocalDate mostRecentDate = now.minusDays(5);

    List<XentralInvoiceDTO> invoices = List.of(
        new XentralInvoiceDTO(
            "INV-001",
            "R-2025-010",
            xentralCustomerId,
            new BigDecimal("500.00"),
            now.minusDays(30),
            now.minusDays(30).plusDays(14),
            now.minusDays(16),
            "PAID"),
        new XentralInvoiceDTO(
            "INV-002",
            "R-2025-020",
            xentralCustomerId,
            new BigDecimal("800.00"),
            mostRecentDate,
            mostRecentDate.plusDays(14),
            null,
            "OPEN"),
        new XentralInvoiceDTO(
            "INV-003",
            "R-2025-015",
            xentralCustomerId,
            new BigDecimal("600.00"),
            now.minusDays(15),
            now.minusDays(15).plusDays(14),
            now.minusDays(1),
            "PAID"));

    when(xentralApiService.getInvoicesByCustomer(xentralCustomerId)).thenReturn(invoices);

    // WHEN: Getting revenue metrics
    RevenueMetrics metrics = revenueMetricsService.getRevenueMetrics(customerId);

    // THEN: Last order date should be the most recent invoice date
    assertNotNull(metrics, "Metrics should not be null");
    assertEquals(
        mostRecentDate,
        metrics.lastOrderDate(),
        "Last order date should be the most recent invoice date");
  }

  @Test
  @Transactional
  @DisplayName("getRevenueMetrics() - payment behavior classification (Mock-Mode: 14 days → GOOD)")
  void getRevenueMetrics_PaymentBehavior_Classification() {
    // GIVEN: Customer with invoices
    UUID customerId = UUID.fromString("c0000000-0001-0000-0000-000000000001");
    String xentralCustomerId = "XC-10001";

    List<XentralInvoiceDTO> invoices = List.of(
        new XentralInvoiceDTO(
            "INV-001",
            "R-2025-050",
            xentralCustomerId,
            new BigDecimal("1000.00"),
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(10).plusDays(14),
            LocalDate.now().plusDays(4),
            "PAID"));

    when(xentralApiService.getInvoicesByCustomer(xentralCustomerId)).thenReturn(invoices);

    // WHEN: Getting revenue metrics
    RevenueMetrics metrics = revenueMetricsService.getRevenueMetrics(customerId);

    // THEN: Payment behavior should be GOOD (14 days = Mock-Mode hardcoded value)
    assertNotNull(metrics, "Metrics should not be null");
    assertEquals(
        PaymentBehavior.GOOD,
        metrics.paymentBehavior(),
        "Payment behavior should be GOOD (14 days)");
    assertEquals(14, metrics.averageDaysToPay(), "Average days to pay should be 14 (Mock-Mode)");
  }
}
