package de.freshplan.modules.xentral.dto.v2;

import de.freshplan.modules.xentral.dto.XentralCustomerDTO;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;

/**
 * Maps Xentral v2 API Customer entities to unified XentralCustomerDTO.
 *
 * <p>Transformation: XentralV2Customer (API format) → XentralCustomerDTO (internal format)
 *
 * <p>Note: Financial metrics (totalRevenue, averageDaysToPay, lastOrderDate) are NOT available in
 * v2 API and must be calculated separately from invoice data (see XentralV1V2ApiAdapter).
 *
 * <p>Sprint: 2.1.7.2 - D2a Real API DTOs
 *
 * @see XentralV2Customer
 * @see XentralCustomerDTO
 */
@ApplicationScoped
public class XentralV2CustomerMapper {

  /**
   * Maps a single v2 customer to unified DTO (without financial metrics).
   *
   * <p>Financial metrics must be enriched later via invoice aggregation.
   *
   * @param customer v2 customer from API
   * @return unified customer DTO with null financial fields
   */
  public XentralCustomerDTO toDTO(XentralV2Customer customer) {
    if (customer == null) {
      return null;
    }

    // Validate required fields
    customer.validate();

    return new XentralCustomerDTO(
        customer.id(), // xentralId
        customer.getCompanyName(), // companyName
        customer.getEmail(), // email
        customer.getPhone(), // phone
        null, // totalRevenue (enriched later from invoices)
        null, // averageDaysToPay (enriched later from invoices)
        null, // lastOrderDate (enriched later from invoices)
        customer.getSalesRepId() // salesRepId
        );
  }

  /**
   * Enriches an existing DTO with financial metrics calculated from invoice data.
   *
   * <p>This method is called AFTER invoice aggregation to add financial data.
   *
   * @param dto existing customer DTO (from toDTO)
   * @param totalRevenue calculated from SUM(invoice.balance.paid)
   * @param averageDaysToPay calculated from AVG(paymentDate - invoiceDate)
   * @param lastOrderDate calculated from MAX(invoice.date)
   * @return new DTO with enriched financial data
   */
  public XentralCustomerDTO enrichWithFinancialData(
      XentralCustomerDTO dto,
      BigDecimal totalRevenue,
      Integer averageDaysToPay,
      java.time.LocalDate lastOrderDate) {

    if (dto == null) {
      return null;
    }

    return new XentralCustomerDTO(
        dto.xentralId(),
        dto.companyName(),
        dto.email(),
        dto.phone(),
        totalRevenue,
        averageDaysToPay,
        lastOrderDate,
        dto.salesRepId());
  }
}
