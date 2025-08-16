package de.freshplan.domain.export.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.export.service.dto.ExportRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for HtmlExportQueryService following established CQRS test patterns.
 *
 * <p>This is a read-only service, so we focus on: - HTML generation correctness - XSS protection
 * (HTML escaping) - Statistics calculation - Filter application - NO write operations (verified)
 *
 * @author FreshPlan Team
 * @since Phase 13 CQRS Migration
 */
@QuarkusTest
class HtmlExportQueryServiceTest {

  @Inject HtmlExportQueryService queryService;

  @InjectMock CustomerRepository customerRepository;

  private Customer testCustomer1;
  private Customer testCustomer2;
  private CustomerContact primaryContact;

  @BeforeEach
  void setUp() {
    // Reset mocks
    Mockito.reset(customerRepository);

    // Create test data
    testCustomer1 = new Customer();
    testCustomer1.setId(UUID.randomUUID());
    testCustomer1.setCustomerNumber("KD-2025-00001");
    testCustomer1.setCompanyName("Test GmbH");
    testCustomer1.setStatus(CustomerStatus.AKTIV);
    testCustomer1.setIndustry(Industry.SONSTIGE); // Using SONSTIGE as IT doesn't exist
    testCustomer1.setLastContactDate(LocalDateTime.now());

    primaryContact = new CustomerContact();
    primaryContact.setFirstName("Max");
    primaryContact.setLastName("Mustermann");
    primaryContact.setIsPrimary(true);
    testCustomer1.setContacts(Collections.singletonList(primaryContact));

    testCustomer2 = new Customer();
    testCustomer2.setId(UUID.randomUUID());
    testCustomer2.setCustomerNumber("KD-2025-00002");
    testCustomer2.setCompanyName("Lead Company");
    testCustomer2.setStatus(CustomerStatus.LEAD);
    testCustomer2.setIndustry(Industry.EINZELHANDEL); // Using EINZELHANDEL instead of HANDEL
  }

  @Test
  void generateCustomersHtml_withFilters_shouldGenerateValidHtml() {
    // Given
    ExportRequest request = new ExportRequest();
    request.setStatus(Arrays.asList("AKTIV"));
    request.setIndustry("IT");

    when(customerRepository.findByFilters(anyList(), anyString()))
        .thenReturn(Arrays.asList(testCustomer1));

    // When
    String html = queryService.generateCustomersHtml(request);

    // Then
    assertThat(html).isNotNull();
    assertThat(html).contains("<!DOCTYPE html>");
    assertThat(html).contains("FreshPlan Kunden-Export");
    assertThat(html).contains("Test GmbH");
    assertThat(html).contains("KD-2025-00001");
    assertThat(html).contains("Max Mustermann");
    assertThat(html).contains("#004F7B"); // FreshPlan CI color
    assertThat(html).contains("#94C456"); // FreshPlan CI accent color

    // Verify repository interaction
    verify(customerRepository).findByFilters(eq(Arrays.asList("AKTIV")), eq("IT"));
    verifyNoMoreInteractions(customerRepository);
  }

  @Test
  void generateCustomersHtml_withMultipleCustomers_shouldCalculateStatisticsCorrectly() {
    // Given
    ExportRequest request = new ExportRequest();
    List<Customer> customers = Arrays.asList(testCustomer1, testCustomer2);

    when(customerRepository.findByFilters(any(), any())).thenReturn(customers);

    // When
    String html = queryService.generateCustomersHtml(request);

    // Then
    assertThat(html).contains("stat-value\">2</div>"); // Total count
    assertThat(html).contains("stat-value\">1</div>"); // Active count
    assertThat(html).contains("stat-label\">Aktiv</div>");
    assertThat(html).contains("stat-label\">Leads</div>");
  }

  @Test
  void generateCustomersHtml_withXssAttempt_shouldEscapeHtml() {
    // Given
    testCustomer1.setCompanyName("<script>alert('XSS')</script>");
    testCustomer1.setIndustry(null); // Will be tested as null value

    ExportRequest request = new ExportRequest();
    when(customerRepository.findByFilters(any(), any())).thenReturn(Arrays.asList(testCustomer1));

    // When
    String html = queryService.generateCustomersHtml(request);

    // Then
    assertThat(html).doesNotContain("<script>alert('XSS')</script>");
    assertThat(html).contains("&lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;");
  }

  @Test
  void generateCustomersHtml_withEmptyResult_shouldGenerateValidHtmlWithZeroStats() {
    // Given
    ExportRequest request = new ExportRequest();
    when(customerRepository.findByFilters(any(), any())).thenReturn(Collections.emptyList());

    // When
    String html = queryService.generateCustomersHtml(request);

    // Then
    assertThat(html).contains("stat-value\">0</div>"); // Total count = 0
    assertThat(html).contains("<tbody>\n</tbody>"); // Empty table body
  }

  @Test
  void generateCustomersHtml_withNullValues_shouldHandleGracefully() {
    // Given
    testCustomer1.setStatus(null);
    testCustomer1.setIndustry(null);
    testCustomer1.setLastContactDate(null);
    testCustomer1.setContacts(null);

    ExportRequest request = new ExportRequest();
    when(customerRepository.findByFilters(any(), any())).thenReturn(Arrays.asList(testCustomer1));

    // When
    String html = queryService.generateCustomersHtml(request);

    // Then
    assertThat(html).isNotNull();
    assertThat(html).contains("Test GmbH");
    assertThat(html).contains("-"); // Default for missing data
    assertThat(html).doesNotContain("null");
  }

  @Test
  void generateCustomersHtml_shouldIncludePrintOptimization() {
    // Given
    ExportRequest request = new ExportRequest();
    when(customerRepository.findByFilters(any(), any())).thenReturn(Collections.emptyList());

    // When
    String html = queryService.generateCustomersHtml(request);

    // Then
    assertThat(html).contains("@page { size: A4; margin: 2cm; }");
    assertThat(html).contains("@media print");
    assertThat(html).contains(".no-print { display: none; }");
    assertThat(html).contains("window.print()");
  }

  @Test
  void generateCustomersHtml_shouldIncludeFilterInformation() {
    // Given
    ExportRequest request = new ExportRequest();
    request.setStatus(Arrays.asList("AKTIV", "LEAD"));
    request.setIndustry("IT");

    when(customerRepository.findByFilters(any(), any())).thenReturn(Collections.emptyList());

    // When
    String html = queryService.generateCustomersHtml(request);

    // Then
    assertThat(html).contains("<strong>Branche:</strong> IT");
    assertThat(html).contains("<strong>Status-Filter:</strong> AKTIV, LEAD");
  }

  @Test
  void verifyNoWriteOperations() {
    // Given
    ExportRequest request = new ExportRequest();
    when(customerRepository.findByFilters(any(), any())).thenReturn(Collections.emptyList());

    // When
    queryService.generateCustomersHtml(request);

    // Then - verify NO write operations
    verify(customerRepository, never()).persist((Customer) any());
    verify(customerRepository, never()).persistAndFlush(any());
    verify(customerRepository, never()).delete(any());
    verify(customerRepository, never()).deleteById(any());

    // Only read operations allowed
    verify(customerRepository, times(1)).findByFilters(any(), any());
  }
}
