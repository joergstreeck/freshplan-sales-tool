package de.freshplan.api.common;

import de.freshplan.domain.customer.constants.CustomerConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Standardized pagination request with validation and defaults. Ensures consistent pagination
 * across all endpoints.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class PaginationRequest {

  @Min(0)
  private final int page;

  @Min(1)
  @Max(CustomerConstants.MAX_PAGE_SIZE)
  private final int size;

  private PaginationRequest(int page, int size) {
    this.page = page;
    this.size = size;
  }

  /**
   * Creates a pagination request with validation and defaults.
   *
   * @param page The page number (0-based), defaults to 0 if null or negative
   * @param size The page size, defaults to 20, max 100
   * @return A validated pagination request
   */
  public static PaginationRequest of(Integer page, Integer size) {
    int validatedPage = CustomerConstants.DEFAULT_PAGE_NUMBER;
    int validatedSize = CustomerConstants.DEFAULT_PAGE_SIZE;

    if (page != null && page >= 0) {
      validatedPage = page;
    }

    if (size != null) {
      if (size > CustomerConstants.MAX_PAGE_SIZE) {
        validatedSize = CustomerConstants.MAX_PAGE_SIZE;
      } else if (size >= CustomerConstants.MIN_PAGE_SIZE) {
        validatedSize = size;
      }
    }

    return new PaginationRequest(validatedPage, validatedSize);
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  /**
   * Calculates the offset for database queries.
   *
   * @return The offset (page * size)
   */
  public int getOffset() {
    return page * size;
  }

  @Override
  public String toString() {
    return "PaginationRequest{page=" + page + ", size=" + size + "}";
  }
}
