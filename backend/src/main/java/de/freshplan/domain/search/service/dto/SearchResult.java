package de.freshplan.domain.search.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Individual search result with relevance scoring and metadata.
 *
 * @since FC-005 PR4
 */
public class SearchResult {

  /** Type of result (customer or contact). */
  @JsonProperty("type")
  private String type;

  /** Entity ID. */
  @JsonProperty("id")
  private String id;

  /** The actual entity data (Customer or Contact). */
  @JsonProperty("data")
  private Object data;

  /** Relevance score (0-100, higher is more relevant). */
  @JsonProperty("relevanceScore")
  private int relevanceScore;

  /** Fields that matched the search query. */
  @JsonProperty("matchedFields")
  private List<String> matchedFields = new ArrayList<>();

  /** Optional highlight snippets for matched fields. */
  @JsonProperty("highlights")
  private List<String> highlights;

  // Constructors
  public SearchResult() {
    this.matchedFields = new ArrayList<>();
  }

  public SearchResult(
      String type,
      String id,
      Object data,
      int relevanceScore,
      List<String> matchedFields,
      List<String> highlights) {
    this.type = type;
    this.id = id;
    this.data = data;
    this.relevanceScore = relevanceScore;
    this.matchedFields = matchedFields != null ? matchedFields : new ArrayList<>();
    this.highlights = highlights;
  }

  // Builder pattern
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String type;
    private String id;
    private Object data;
    private int relevanceScore;
    private List<String> matchedFields = new ArrayList<>();
    private List<String> highlights;

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder data(Object data) {
      this.data = data;
      return this;
    }

    public Builder relevanceScore(int relevanceScore) {
      this.relevanceScore = relevanceScore;
      return this;
    }

    public Builder matchedFields(List<String> matchedFields) {
      this.matchedFields = matchedFields;
      return this;
    }

    public Builder highlights(List<String> highlights) {
      this.highlights = highlights;
      return this;
    }

    public SearchResult build() {
      return new SearchResult(type, id, data, relevanceScore, matchedFields, highlights);
    }
  }

  // Getters and Setters
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  public int getRelevanceScore() {
    return relevanceScore;
  }

  public void setRelevanceScore(int relevanceScore) {
    this.relevanceScore = relevanceScore;
  }

  public List<String> getMatchedFields() {
    return matchedFields;
  }

  public void setMatchedFields(List<String> matchedFields) {
    this.matchedFields = matchedFields;
  }

  public List<String> getHighlights() {
    return highlights;
  }

  public void setHighlights(List<String> highlights) {
    this.highlights = highlights;
  }
}
