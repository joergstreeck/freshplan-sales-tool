package de.freshplan.domain.intelligence.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for comprehensive data quality metrics
 */
public class DataQualityMetricsDTO {
    
    // Basic counts
    private Long totalContacts;
    private Long contactsWithInteractions;
    private Long contactsWithoutInteractions;
    private Long totalInteractions;
    
    // Freshness distribution
    private Long freshContacts;      // < 90 days
    private Long agingContacts;      // 90-180 days
    private Long staleContacts;      // 180-365 days
    private Long criticalContacts;   // > 365 days
    
    // Quality scores (0-100)
    private Double dataCompletenessScore;
    private Double interactionCoverage;
    private Double averageInteractionsPerContact;
    private Long contactsWithWarmthScore;
    
    // Overall assessment
    private String overallDataQuality; // EXCELLENT, GOOD, FAIR, POOR, CRITICAL
    private Boolean showDataCollectionHints;
    
    // Insights
    private List<String> criticalDataGaps;
    private List<String> improvementSuggestions;
    
    // Metadata
    private LocalDateTime lastUpdated;
    
    // Getters and Setters
    public Long getTotalContacts() {
        return totalContacts;
    }
    
    public void setTotalContacts(Long totalContacts) {
        this.totalContacts = totalContacts;
    }
    
    public Long getContactsWithInteractions() {
        return contactsWithInteractions;
    }
    
    public void setContactsWithInteractions(Long contactsWithInteractions) {
        this.contactsWithInteractions = contactsWithInteractions;
    }
    
    public Long getContactsWithoutInteractions() {
        return contactsWithoutInteractions;
    }
    
    public void setContactsWithoutInteractions(Long contactsWithoutInteractions) {
        this.contactsWithoutInteractions = contactsWithoutInteractions;
    }
    
    public Long getTotalInteractions() {
        return totalInteractions;
    }
    
    public void setTotalInteractions(Long totalInteractions) {
        this.totalInteractions = totalInteractions;
    }
    
    public Long getFreshContacts() {
        return freshContacts;
    }
    
    public void setFreshContacts(Long freshContacts) {
        this.freshContacts = freshContacts;
    }
    
    public Long getAgingContacts() {
        return agingContacts;
    }
    
    public void setAgingContacts(Long agingContacts) {
        this.agingContacts = agingContacts;
    }
    
    public Long getStaleContacts() {
        return staleContacts;
    }
    
    public void setStaleContacts(Long staleContacts) {
        this.staleContacts = staleContacts;
    }
    
    public Long getCriticalContacts() {
        return criticalContacts;
    }
    
    public void setCriticalContacts(Long criticalContacts) {
        this.criticalContacts = criticalContacts;
    }
    
    public Double getDataCompletenessScore() {
        return dataCompletenessScore;
    }
    
    public void setDataCompletenessScore(Double dataCompletenessScore) {
        this.dataCompletenessScore = dataCompletenessScore;
    }
    
    public Double getInteractionCoverage() {
        return interactionCoverage;
    }
    
    public void setInteractionCoverage(Double interactionCoverage) {
        this.interactionCoverage = interactionCoverage;
    }
    
    public Double getAverageInteractionsPerContact() {
        return averageInteractionsPerContact;
    }
    
    public void setAverageInteractionsPerContact(Double averageInteractionsPerContact) {
        this.averageInteractionsPerContact = averageInteractionsPerContact;
    }
    
    public Long getContactsWithWarmthScore() {
        return contactsWithWarmthScore;
    }
    
    public void setContactsWithWarmthScore(Long contactsWithWarmthScore) {
        this.contactsWithWarmthScore = contactsWithWarmthScore;
    }
    
    public String getOverallDataQuality() {
        return overallDataQuality;
    }
    
    public void setOverallDataQuality(String overallDataQuality) {
        this.overallDataQuality = overallDataQuality;
    }
    
    public Boolean getShowDataCollectionHints() {
        return showDataCollectionHints;
    }
    
    public void setShowDataCollectionHints(Boolean showDataCollectionHints) {
        this.showDataCollectionHints = showDataCollectionHints;
    }
    
    public List<String> getCriticalDataGaps() {
        return criticalDataGaps;
    }
    
    public void setCriticalDataGaps(List<String> criticalDataGaps) {
        this.criticalDataGaps = criticalDataGaps;
    }
    
    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }
    
    public void setImprovementSuggestions(List<String> improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}