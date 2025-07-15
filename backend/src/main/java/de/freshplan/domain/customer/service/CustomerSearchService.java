package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.dto.CustomerSearchRequest;
import de.freshplan.domain.customer.service.mapper.CustomerMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/** Service for searching customers with dynamic filters. */
@ApplicationScoped
@Transactional
public class CustomerSearchService {

  private static final Logger LOG = Logger.getLogger(CustomerSearchService.class);

  @Inject CustomerQueryBuilder queryBuilder;

  @Inject CustomerMapper customerMapper;

  /**
   * Searches for customers based on the given search request.
   *
   * @param request the search criteria
   * @param page the page number (0-based)
   * @param size the page size
   * @return a page of customer responses
   */
  public PagedResponse<CustomerResponse> search(CustomerSearchRequest request, int page, int size) {
    LOG.debugf(
        "Searching customers with request: globalSearch=%s, filters=%d",
        request.getGlobalSearch(), request.getFilters() != null ? request.getFilters().size() : 0);

    // Build and execute query
    PanacheQuery<Customer> query = queryBuilder.buildQuery(request);

    // Get total count before pagination
    long totalElements = query.count();

    // Apply pagination
    List<Customer> customers = query.page(Page.of(page, size)).list();

    // Map to DTOs
    List<CustomerResponse> responses =
        customers.stream().map(customerMapper::toResponse).collect(Collectors.toList());

    // Calculate page info
    int totalPages = (int) Math.ceil((double) totalElements / size);

    LOG.debugf("Found %d customers (page %d of %d)", totalElements, page + 1, totalPages);

    return new PagedResponse<>(
        responses, page, size, totalElements, totalPages, page == 0, page >= totalPages - 1);
  }

  /** Response wrapper for paginated results. */
  public static class PagedResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;
    private final int numberOfElements;

    public PagedResponse(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {
      this.content = content;
      this.page = page;
      this.size = size;
      this.totalElements = totalElements;
      this.totalPages = totalPages;
      this.first = first;
      this.last = last;
      this.numberOfElements = content != null ? content.size() : 0;
    }

    // Getters
    public List<T> getContent() {
      return content;
    }

    public int getPage() {
      return page;
    }

    public int getSize() {
      return size;
    }

    public long getTotalElements() {
      return totalElements;
    }

    public int getTotalPages() {
      return totalPages;
    }

    public boolean isFirst() {
      return first;
    }

    public boolean isLast() {
      return last;
    }

    public int getNumberOfElements() {
      return numberOfElements;
    }
  }
}
