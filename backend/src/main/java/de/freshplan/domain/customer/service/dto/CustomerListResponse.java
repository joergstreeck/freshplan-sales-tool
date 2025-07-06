package de.freshplan.domain.customer.service.dto;

import java.util.List;

/**
 * Response DTO for paginated customer lists. Follows the standard pagination format used throughout
 * the application.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record CustomerListResponse(
    List<CustomerResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last) {

  /** Creates a paginated response from a list of customers. */
  public static CustomerListResponse of(
      List<CustomerResponse> customers, int page, int size, long totalElements) {

    int totalPages = (int) Math.ceil((double) totalElements / size);
    boolean first = page == 0;
    boolean last = page >= totalPages - 1;

    return new CustomerListResponse(customers, page, size, totalElements, totalPages, first, last);
  }

  /** Creates an empty response. */
  public static CustomerListResponse empty(int page, int size) {
    return new CustomerListResponse(List.of(), page, size, 0L, 0, true, true);
  }
}
