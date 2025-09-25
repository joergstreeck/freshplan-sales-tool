package de.freshplan.modules.leads.api;

import java.util.List;

/**
 * Generic DTO for paginated API responses. Provides type-safe and discoverable response structure
 * for all paginated endpoints.
 *
 * @param <T> the type of data elements in the response
 */
public class PaginatedResponse<T> {
  private List<T> data;
  private int page;
  private int size;
  private long total;
  private long totalPages;

  public PaginatedResponse() {}

  public PaginatedResponse(List<T> data, int page, int size, long total) {
    this.data = data;
    this.page = page;
    this.size = size;
    this.total = total;
    this.totalPages = (total + size - 1) / size;
  }

  // Getters and setters
  public List<T> getData() {
    return data;
  }

  public void setData(List<T> data) {
    this.data = data;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public long getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(long totalPages) {
    this.totalPages = totalPages;
  }

  // Builder pattern for fluent API
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static class Builder<T> {
    private List<T> data;
    private int page;
    private int size;
    private long total;

    public Builder<T> data(List<T> data) {
      this.data = data;
      return this;
    }

    public Builder<T> page(int page) {
      this.page = page;
      return this;
    }

    public Builder<T> size(int size) {
      this.size = size;
      return this;
    }

    public Builder<T> total(long total) {
      this.total = total;
      return this;
    }

    public PaginatedResponse<T> build() {
      return new PaginatedResponse<>(data, page, size, total);
    }
  }
}