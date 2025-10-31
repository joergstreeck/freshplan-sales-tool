package de.freshplan.modules.xentral.dto.v1;

import de.freshplan.modules.xentral.dto.XentralInvoiceDTO;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Maps Xentral v1 API Invoice entities to unified XentralInvoiceDTO.
 *
 * <p>Transformation: XentralV1Invoice + XentralV1InvoiceBalance → XentralInvoiceDTO
 *
 * <p>Sprint: 2.1.7.2 - D2a Real API DTOs
 *
 * @see XentralV1Invoice
 * @see XentralV1InvoiceBalance
 * @see XentralInvoiceDTO
 */
@ApplicationScoped
public class XentralV1InvoiceMapper {

  /**
   * Maps a v1 invoice with balance data to unified DTO.
   *
   * @param invoice v1 invoice from API
   * @param balance balance data from /api/v1/invoices/{id}/balance endpoint
   * @return unified invoice DTO
   */
  public XentralInvoiceDTO toDTO(XentralV1Invoice invoice, XentralV1InvoiceBalance balance) {
    if (invoice == null) {
      return null;
    }

    // Validate required fields
    invoice.validate();

    return new XentralInvoiceDTO(
        invoice.id(), // invoiceId
        invoice.invoiceNumber(), // invoiceNumber
        invoice.customerId(), // customerId
        invoice.total(), // amount
        invoice.invoiceDate(), // invoiceDate
        invoice.dueDate(), // dueDate
        balance != null ? balance.paymentDate() : null, // paymentDate
        mapStatus(invoice.status(), balance) // status
        );
  }

  /**
   * Maps v1 invoice without balance data (when balance endpoint is not available).
   *
   * <p>Used for fallback scenarios or when balance data is not required.
   *
   * @param invoice v1 invoice from API
   * @return unified invoice DTO without payment details
   */
  public XentralInvoiceDTO toDTO(XentralV1Invoice invoice) {
    return toDTO(invoice, null);
  }

  /**
   * Determines invoice status based on invoice.status and balance data.
   *
   * <p>Priority: 1. Balance data (isFullyPaid → "paid", isPartiallyPaid → "partial") 2. Invoice
   * status (direct mapping)
   *
   * @param invoiceStatus status from invoice
   * @param balance balance data (may be null)
   * @return normalized status string
   */
  private String mapStatus(String invoiceStatus, XentralV1InvoiceBalance balance) {
    // If balance data is available, use it for accurate status
    if (balance != null) {
      if (balance.isFullyPaid()) {
        return "paid";
      }
      if (balance.isPartiallyPaid()) {
        return "partial";
      }
    }

    // Fallback to invoice status
    if (invoiceStatus == null) {
      return "open";
    }

    return switch (invoiceStatus.toLowerCase()) {
      case "paid", "completed" -> "paid";
      case "partial" -> "partial";
      case "overdue" -> "overdue";
      case "cancelled", "canceled" -> "cancelled";
      default -> "open";
    };
  }
}
