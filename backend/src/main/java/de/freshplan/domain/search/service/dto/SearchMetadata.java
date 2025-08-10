package de.freshplan.domain.search.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata for search results. Contains additional information about the search execution.
 *
 * @since FC-005 PR4
 */
public class SearchMetadata {

  /** The original search query. */
  @JsonProperty("query")
  private String query;

  /** Detected query type. */
  @JsonProperty("queryType")
  private QueryType queryType;

  /** Whether the search was truncated due to limit. */
  @JsonProperty("truncated")
  private boolean truncated;

  /** Suggested alternative queries. */
  @JsonProperty("suggestions")
  private String[] suggestions;

  // Constructors
  public SearchMetadata() {}

  public SearchMetadata(
      String query, QueryType queryType, boolean truncated, String[] suggestions) {
    this.query = query;
    this.queryType = queryType;
    this.truncated = truncated;
    this.suggestions = suggestions;
  }

  // Builder pattern
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String query;
    private QueryType queryType;
    private boolean truncated;
    private String[] suggestions;

    public Builder query(String query) {
      this.query = query;
      return this;
    }

    public Builder queryType(QueryType queryType) {
      this.queryType = queryType;
      return this;
    }

    public Builder truncated(boolean truncated) {
      this.truncated = truncated;
      return this;
    }

    public Builder suggestions(String[] suggestions) {
      this.suggestions = suggestions;
      return this;
    }

    public SearchMetadata build() {
      return new SearchMetadata(query, queryType, truncated, suggestions);
    }
  }

  // Getters and Setters
  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public QueryType getQueryType() {
    return queryType;
  }

  public void setQueryType(QueryType queryType) {
    this.queryType = queryType;
  }

  public boolean isTruncated() {
    return truncated;
  }

  public void setTruncated(boolean truncated) {
    this.truncated = truncated;
  }

  public String[] getSuggestions() {
    return suggestions;
  }

  public void setSuggestions(String[] suggestions) {
    this.suggestions = suggestions;
  }
}
