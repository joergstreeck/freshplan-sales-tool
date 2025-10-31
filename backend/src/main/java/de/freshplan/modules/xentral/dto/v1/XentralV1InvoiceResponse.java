package de.freshplan.modules.xentral.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Response wrapper for Xentral v1 API /api/v1/invoices endpoint.
 *
 * <p>Xentral v1 API returns data in a simpler wrapper format: { "data": [...], "meta": {...} }
 *
 * <p>Sprint: 2.1.7.2 - D2a Real API DTOs
 *
 * @see XentralV1Invoice
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record XentralV1InvoiceResponse(List<XentralV1Invoice> data, Meta meta) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Meta(Integer total, Integer page, Integer perPage) {}

  /**
   * Validates that the response contains data.
   *
   * @return true if response has at least one invoice, false otherwise
   */
  public boolean hasData() {
    return data != null && !data.isEmpty();
  }

  /**
   * Gets total number of invoices available in Xentral (from meta).
   *
   * @return total count or 0 if not available
   */
  public int getTotalCount() {
    return meta != null && meta.total() != null ? meta.total() : 0;
  }
}
