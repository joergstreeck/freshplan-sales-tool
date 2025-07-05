package de.freshplan.domain.customer.entity;

/**
 * Customer classification for prioritization.
 */
public enum Classification {
    /**
     * High-value strategic customer.
     */
    A_KUNDE,
    
    /**
     * Important regular customer.
     */
    B_KUNDE,
    
    /**
     * Standard customer.
     */
    C_KUNDE,
    
    /**
     * Low-value or occasional customer.
     */
    D_KUNDE
}