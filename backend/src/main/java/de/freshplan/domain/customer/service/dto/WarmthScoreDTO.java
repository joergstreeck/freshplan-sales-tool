package de.freshplan.domain.customer.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for warmth score calculation results
 */
public class WarmthScoreDTO {
    
    private UUID contactId;
    private Integer warmthScore; // 0-100
    private Integer confidence; // 0-100
    private Integer dataPoints;
    private LocalDateTime lastCalculated;
    private String trend; // INCREASING, STABLE, DECREASING
    private String recommendation;
    
    // Constructors
    public WarmthScoreDTO() {}
    
    // Getters and Setters
    public UUID getContactId() {
        return contactId;
    }

    public void setContactId(UUID contactId) {
        this.contactId = contactId;
    }

    public Integer getWarmthScore() {
        return warmthScore;
    }

    public void setWarmthScore(Integer warmthScore) {
        this.warmthScore = warmthScore;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public Integer getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(Integer dataPoints) {
        this.dataPoints = dataPoints;
    }

    public LocalDateTime getLastCalculated() {
        return lastCalculated;
    }

    public void setLastCalculated(LocalDateTime lastCalculated) {
        this.lastCalculated = lastCalculated;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    // Business methods
    public String getWarmthLevel() {
        if (warmthScore == null) return "UNKNOWN";
        if (warmthScore >= 80) return "HOT";
        if (warmthScore >= 60) return "WARM";
        if (warmthScore >= 40) return "NEUTRAL";
        if (warmthScore >= 20) return "COOL";
        return "COLD";
    }
    
    public boolean isHighConfidence() {
        return confidence != null && confidence >= 70;
    }
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final WarmthScoreDTO dto = new WarmthScoreDTO();
        
        public Builder contactId(UUID contactId) {
            dto.setContactId(contactId);
            return this;
        }
        
        public Builder warmthScore(Integer warmthScore) {
            dto.setWarmthScore(warmthScore);
            return this;
        }
        
        public Builder confidence(Integer confidence) {
            dto.setConfidence(confidence);
            return this;
        }
        
        public Builder dataPoints(Integer dataPoints) {
            dto.setDataPoints(dataPoints);
            return this;
        }
        
        public Builder lastCalculated(LocalDateTime lastCalculated) {
            dto.setLastCalculated(lastCalculated);
            return this;
        }
        
        public Builder trend(String trend) {
            dto.setTrend(trend);
            return this;
        }
        
        public Builder recommendation(String recommendation) {
            dto.setRecommendation(recommendation);
            return this;
        }
        
        public WarmthScoreDTO build() {
            // Auto-generate recommendation if not set
            if (dto.getRecommendation() == null) {
                dto.setRecommendation(generateRecommendation(dto));
            }
            return dto;
        }
        
        private String generateRecommendation(WarmthScoreDTO dto) {
            if (dto.getConfidence() < 30) {
                return "Mehr Interaktionen erfassen für präzisere Bewertung";
            }
            
            String level = dto.getWarmthLevel();
            switch (level) {
                case "HOT":
                    return "Exzellente Beziehung - Cross-Selling Potenzial prüfen";
                case "WARM":
                    return "Gute Beziehung - regelmäßigen Kontakt halten";
                case "NEUTRAL":
                    return "Beziehung intensivieren - persönlichen Termin vereinbaren";
                case "COOL":
                    return "Aufmerksamkeit erforderlich - Reaktivierungskampagne starten";
                case "COLD":
                    return "Dringender Handlungsbedarf - sofortigen Kontakt aufnehmen";
                default:
                    return "Keine Daten vorhanden - erste Interaktion erfassen";
            }
        }
    }
}