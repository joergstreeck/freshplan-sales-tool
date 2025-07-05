package de.freshplan.domain.customer.entity;

/**
 * Enumeration for importance levels of timeline events.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum ImportanceLevel {
    
    LOW("Niedrig", 1),
    MEDIUM("Mittel", 2),
    HIGH("Hoch", 3),
    CRITICAL("Kritisch", 4),
    URGENT("Dringend", 5);
    
    private final String displayName;
    private final int priority;
    
    ImportanceLevel(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getPriority() {
        return priority;
    }
    
    /**
     * Checks if this importance level is higher than another.
     */
    public boolean isHigherThan(ImportanceLevel other) {
        return this.priority > other.priority;
    }
    
    /**
     * Checks if this importance level is lower than another.
     */
    public boolean isLowerThan(ImportanceLevel other) {
        return this.priority < other.priority;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}