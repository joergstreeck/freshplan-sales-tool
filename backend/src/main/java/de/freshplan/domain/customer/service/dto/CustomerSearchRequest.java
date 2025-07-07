package de.freshplan.domain.customer.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for dynamic customer search functionality. Supports global search, complex filters,
 * and sorting.
 */
public class CustomerSearchRequest {

  @Size(max = 100, message = "Global search term cannot exceed 100 characters")
  private String globalSearch;

  @Valid private List<FilterCriteria> filters = new ArrayList<>();

  @Valid private SortCriteria sort;

  // Constructors
  public CustomerSearchRequest() {}

  // Builder pattern for easier construction
  public static Builder builder() {
    return new Builder();
  }

  // Getters and Setters
  public String getGlobalSearch() {
    return globalSearch;
  }

  public void setGlobalSearch(String globalSearch) {
    this.globalSearch = globalSearch;
  }

  public List<FilterCriteria> getFilters() {
    return filters;
  }

  public void setFilters(List<FilterCriteria> filters) {
    this.filters = filters != null ? filters : new ArrayList<>();
  }

  public SortCriteria getSort() {
    return sort;
  }

  public void setSort(SortCriteria sort) {
    this.sort = sort;
  }

  // Builder class
  public static class Builder {
    private final CustomerSearchRequest request = new CustomerSearchRequest();

    public Builder withGlobalSearch(String globalSearch) {
      request.setGlobalSearch(globalSearch);
      return this;
    }

    public Builder withFilter(FilterCriteria filter) {
      if (request.filters == null) {
        request.filters = new ArrayList<>();
      }
      request.filters.add(filter);
      return this;
    }

    public Builder withFilters(List<FilterCriteria> filters) {
      request.setFilters(filters);
      return this;
    }

    public Builder withSort(SortCriteria sort) {
      request.setSort(sort);
      return this;
    }

    public CustomerSearchRequest build() {
      return request;
    }
  }
}
