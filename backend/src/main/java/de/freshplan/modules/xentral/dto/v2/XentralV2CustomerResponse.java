package de.freshplan.modules.xentral.dto.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Response wrapper for Xentral v2 API /api/v2/customers endpoint.
 *
 * <p>Xentral v2 API returns data in a paginated wrapper format: { "data": [...], "extra": {...},
 * "cursor": {...} }
 *
 * <p>Sprint: 2.1.7.2 - D2a Real API DTOs
 *
 * @see XentralV2Customer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record XentralV2CustomerResponse(List<XentralV2Customer> data, Extra extra, Cursor cursor) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Extra(Integer totalCount, Integer pageSize, Integer currentPage) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Cursor(String next, String previous) {}

  /**
   * Validates that the response contains data.
   *
   * @return true if response has at least one customer, false otherwise
   */
  public boolean hasData() {
    return data != null && !data.isEmpty();
  }

  /**
   * Gets total number of customers available in Xentral (from extra metadata).
   *
   * @return total count or 0 if not available
   */
  public int getTotalCount() {
    return extra != null && extra.totalCount() != null ? extra.totalCount() : 0;
  }

  /**
   * Checks if there are more pages available.
   *
   * @return true if cursor.next exists
   */
  public boolean hasNextPage() {
    return cursor != null && cursor.next() != null && !cursor.next().isBlank();
  }
}
