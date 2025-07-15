package de.freshplan.domain.customer.service.dto;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Request DTO for smart customer search with predefined sorting strategies. */
@Schema(description = "Smart search request with predefined sorting strategies")
public class SmartSearchRequest {

  @Schema(description = "Global search term", example = "restaurant")
  private String globalSearch;

  @Schema(description = "Additional filters to apply")
  private List<FilterCriteria> filters;

  @Schema(
      description =
          "Smart sorting strategy (SALES_PRIORITY, RISK_MITIGATION, ENGAGEMENT_FOCUS, REVENUE_POTENTIAL, CONTACT_FREQUENCY)",
      example = "SALES_PRIORITY")
  private String strategy = "SALES_PRIORITY";

  // Default constructor
  public SmartSearchRequest() {}

  // Getters and setters
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
    this.filters = filters;
  }

  public String getStrategy() {
    return strategy == null || strategy.isBlank() ? "SALES_PRIORITY" : strategy;
  }

  public void setStrategy(String strategy) {
    this.strategy = strategy == null || strategy.isBlank() ? "SALES_PRIORITY" : strategy;
  }
}
