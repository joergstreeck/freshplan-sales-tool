package de.freshplan.domain.customer.entity;

/**
 * Type of customer hierarchy relationship.
 */
public enum CustomerHierarchyType {
    /**
     * Main parent company.
     */
    HEADQUARTER,
    
    /**
     * Branch or subsidiary.
     */
    FILIALE,
    
    /**
     * Department within organization.
     */
    ABTEILUNG,
    
    /**
     * Independent franchise.
     */
    FRANCHISE,
    
    /**
     * Standalone entity.
     */
    STANDALONE
}