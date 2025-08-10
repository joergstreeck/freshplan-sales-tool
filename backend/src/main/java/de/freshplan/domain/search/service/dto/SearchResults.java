package de.freshplan.domain.search.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for universal search results.
 * Includes both customer and contact results with metadata.
 * 
 * @since FC-005 PR4
 */
public class SearchResults {
    
    /**
     * List of customer search results.
     */
    @JsonProperty("customers")
    private List<SearchResult> customers = new ArrayList<>();
    
    /**
     * List of contact search results.
     */
    @JsonProperty("contacts")
    private List<SearchResult> contacts = new ArrayList<>();
    
    /**
     * Total count of all results.
     */
    @JsonProperty("totalCount")
    private int totalCount;
    
    /**
     * Execution time in milliseconds.
     */
    @JsonProperty("executionTime")
    private long executionTime;
    
    /**
     * Optional search metadata.
     */
    @JsonProperty("metadata")
    private SearchMetadata metadata;
    
    // Constructors
    public SearchResults() {
        this.customers = new ArrayList<>();
        this.contacts = new ArrayList<>();
    }
    
    public SearchResults(List<SearchResult> customers, List<SearchResult> contacts, 
                        int totalCount, long executionTime, SearchMetadata metadata) {
        this.customers = customers != null ? customers : new ArrayList<>();
        this.contacts = contacts != null ? contacts : new ArrayList<>();
        this.totalCount = totalCount;
        this.executionTime = executionTime;
        this.metadata = metadata;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<SearchResult> customers = new ArrayList<>();
        private List<SearchResult> contacts = new ArrayList<>();
        private int totalCount;
        private long executionTime;
        private SearchMetadata metadata;
        
        public Builder customers(List<SearchResult> customers) {
            this.customers = customers;
            return this;
        }
        
        public Builder contacts(List<SearchResult> contacts) {
            this.contacts = contacts;
            return this;
        }
        
        public Builder totalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }
        
        public Builder executionTime(long executionTime) {
            this.executionTime = executionTime;
            return this;
        }
        
        public Builder metadata(SearchMetadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public SearchResults build() {
            return new SearchResults(customers, contacts, totalCount, executionTime, metadata);
        }
    }
    
    // Getters and Setters
    public List<SearchResult> getCustomers() {
        return customers;
    }
    
    public void setCustomers(List<SearchResult> customers) {
        this.customers = customers;
    }
    
    public List<SearchResult> getContacts() {
        return contacts;
    }
    
    public void setContacts(List<SearchResult> contacts) {
        this.contacts = contacts;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public SearchMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(SearchMetadata metadata) {
        this.metadata = metadata;
    }
}