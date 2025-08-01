package de.freshplan.domain.customer.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for data quality metrics dashboard
 */
public class DataQualityMetricsDTO {
    
    // Contact metrics
    private Long totalContacts;
    private Long contactsWithInteractions;
    private Double averageInteractionsPerContact;
    private Double dataCompletenessScore; // 0-100%
    
    // Intelligence metrics
    private Long contactsWithWarmthScore;
    private Double warmthScoreConfidence; // Average confidence
    private Double suggestionsAcceptanceRate;
    private Double predictionAccuracy;
    
    // Data freshness
    private Long freshContacts; // < 90 days
    private Long agingContacts; // 90-180 days
    private Long staleContacts; // 180-365 days
    private Long criticalContacts; // > 365 days
    
    // Recommendations
    private Boolean showDataCollectionHints;
    private List<String> criticalDataGaps;
    private List<String> improvementSuggestions;
    
    // Constructors
    public DataQualityMetricsDTO() {
        this.criticalDataGaps = new ArrayList<>();
        this.improvementSuggestions = new ArrayList<>();
    }
    
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

    public Double getAverageInteractionsPerContact() {
        return averageInteractionsPerContact;
    }

    public void setAverageInteractionsPerContact(Double averageInteractionsPerContact) {
        this.averageInteractionsPerContact = averageInteractionsPerContact;
    }

    public Double getDataCompletenessScore() {
        return dataCompletenessScore;
    }

    public void setDataCompletenessScore(Double dataCompletenessScore) {
        this.dataCompletenessScore = dataCompletenessScore;
    }

    public Long getContactsWithWarmthScore() {
        return contactsWithWarmthScore;
    }

    public void setContactsWithWarmthScore(Long contactsWithWarmthScore) {
        this.contactsWithWarmthScore = contactsWithWarmthScore;
    }

    public Double getWarmthScoreConfidence() {
        return warmthScoreConfidence;
    }

    public void setWarmthScoreConfidence(Double warmthScoreConfidence) {
        this.warmthScoreConfidence = warmthScoreConfidence;
    }

    public Double getSuggestionsAcceptanceRate() {
        return suggestionsAcceptanceRate;
    }

    public void setSuggestionsAcceptanceRate(Double suggestionsAcceptanceRate) {
        this.suggestionsAcceptanceRate = suggestionsAcceptanceRate;
    }

    public Double getPredictionAccuracy() {
        return predictionAccuracy;
    }

    public void setPredictionAccuracy(Double predictionAccuracy) {
        this.predictionAccuracy = predictionAccuracy;
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
    
    // Business methods
    public String getOverallDataQuality() {
        if (dataCompletenessScore == null) return "UNKNOWN";
        if (dataCompletenessScore >= 80) return "EXCELLENT";
        if (dataCompletenessScore >= 60) return "GOOD";
        if (dataCompletenessScore >= 40) return "FAIR";
        if (dataCompletenessScore >= 20) return "POOR";
        return "CRITICAL";
    }
    
    public boolean needsImmediateAttention() {
        return criticalContacts != null && criticalContacts > 0;
    }
    
    public double getInteractionCoverage() {
        if (totalContacts == null || totalContacts == 0) return 0;
        if (contactsWithInteractions == null) return 0;
        return (contactsWithInteractions * 100.0) / totalContacts;
    }
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final DataQualityMetricsDTO dto = new DataQualityMetricsDTO();
        
        public Builder totalContacts(Long totalContacts) {
            dto.setTotalContacts(totalContacts);
            return this;
        }
        
        public Builder contactsWithInteractions(Long contactsWithInteractions) {
            dto.setContactsWithInteractions(contactsWithInteractions);
            return this;
        }
        
        public Builder averageInteractionsPerContact(Double avg) {
            dto.setAverageInteractionsPerContact(avg);
            return this;
        }
        
        public Builder dataCompletenessScore(Double score) {
            dto.setDataCompletenessScore(score);
            return this;
        }
        
        public Builder contactsWithWarmthScore(Long count) {
            dto.setContactsWithWarmthScore(count);
            return this;
        }
        
        public Builder freshContacts(Long count) {
            dto.setFreshContacts(count);
            return this;
        }
        
        public Builder agingContacts(Long count) {
            dto.setAgingContacts(count);
            return this;
        }
        
        public Builder staleContacts(Long count) {
            dto.setStaleContacts(count);
            return this;
        }
        
        public Builder criticalContacts(Long count) {
            dto.setCriticalContacts(count);
            return this;
        }
        
        public DataQualityMetricsDTO build() {
            // Auto-generate recommendations
            generateRecommendations(dto);
            return dto;
        }
        
        private void generateRecommendations(DataQualityMetricsDTO dto) {
            if (dto.getInteractionCoverage() < 50) {
                dto.getCriticalDataGaps().add("Über 50% der Kontakte haben keine Interaktionen");
                dto.getImprovementSuggestions().add("Import von E-Mail-Historie durchführen");
            }
            
            if (dto.getDataCompletenessScore() != null && dto.getDataCompletenessScore() < 60) {
                dto.getCriticalDataGaps().add("Datenqualität unter 60%");
                dto.getImprovementSuggestions().add("Datenpflege-Kampagne starten");
            }
            
            if (dto.getCriticalContacts() != null && dto.getCriticalContacts() > 0) {
                dto.getCriticalDataGaps().add(dto.getCriticalContacts() + " Kontakte über 1 Jahr nicht aktualisiert");
                dto.getImprovementSuggestions().add("Sofortige Überprüfung kritischer Kontakte");
            }
            
            dto.setShowDataCollectionHints(!dto.getCriticalDataGaps().isEmpty());
        }
    }
}