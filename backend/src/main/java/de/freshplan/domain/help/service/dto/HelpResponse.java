package de.freshplan.domain.help.service.dto;

import de.freshplan.domain.help.entity.HelpType;

import java.util.UUID;

/**
 * Response DTO für Help Content Antworten
 */
public record HelpResponse(
    UUID id,
    String feature,
    String title,
    HelpType type,
    String content,
    String videoUrl,
    String interactionData,
    int priority,
    boolean struggleDetected,
    String struggleType,
    int suggestionLevel,
    long viewCount,
    double helpfulnessRate
) {
    
    public static HelpResponse empty(String feature) {
        return new HelpResponse(
            null, feature, "Keine Hilfe verfügbar", null, 
            "Für dieses Feature ist noch keine Hilfe verfügbar.", 
            null, null, 100, false, null, 0, 0L, 0.0
        );
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID id;
        private String feature;
        private String title;
        private HelpType type;
        private String content;
        private String videoUrl;
        private String interactionData;
        private int priority = 10;
        private boolean struggleDetected = false;
        private String struggleType;
        private int suggestionLevel = 0;
        private long viewCount = 0L;
        private double helpfulnessRate = 0.0;
        
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public Builder feature(String feature) {
            this.feature = feature;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder type(HelpType type) {
            this.type = type;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder videoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }
        
        public Builder interactionData(String interactionData) {
            this.interactionData = interactionData;
            return this;
        }
        
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder struggleDetected(boolean struggleDetected) {
            this.struggleDetected = struggleDetected;
            return this;
        }
        
        public Builder struggleType(String struggleType) {
            this.struggleType = struggleType;
            return this;
        }
        
        public Builder suggestionLevel(int suggestionLevel) {
            this.suggestionLevel = suggestionLevel;
            return this;
        }
        
        public Builder viewCount(long viewCount) {
            this.viewCount = viewCount;
            return this;
        }
        
        public Builder helpfulnessRate(double helpfulnessRate) {
            this.helpfulnessRate = helpfulnessRate;
            return this;
        }
        
        public HelpResponse build() {
            return new HelpResponse(
                id, feature, title, type, content, videoUrl, 
                interactionData, priority, struggleDetected, 
                struggleType, suggestionLevel, viewCount, helpfulnessRate
            );
        }
    }
}