package de.freshplan.domain.customer.entity;

/**
 * Defines the various roles a contact can have within a customer organization.
 * A contact can have multiple roles simultaneously.
 */
public enum ContactRole {
    /**
     * The person who makes final purchasing decisions
     */
    DECISION_MAKER("Entscheidungsträger", "Trifft finale Kaufentscheidungen"),
    
    /**
     * Technical expert who evaluates solutions
     */
    TECHNICAL_CONTACT("Technischer Ansprechpartner", "Bewertet technische Lösungen"),
    
    /**
     * Handles invoicing and payment matters
     */
    BILLING_CONTACT("Rechnungsempfänger", "Zuständig für Rechnungen und Zahlungen"),
    
    /**
     * Manages day-to-day operations
     */
    OPERATIONS_CONTACT("Operativer Ansprechpartner", "Verwaltet Tagesgeschäft"),
    
    /**
     * Influences decisions but doesn't make final call
     */
    INFLUENCER("Beeinflusser", "Beeinflusst Entscheidungen ohne finale Befugnis"),
    
    /**
     * Internal advocate for our solution
     */
    CHAMPION("Fürsprecher", "Interner Befürworter unserer Lösung"),
    
    /**
     * Controls access to decision makers
     */
    GATEKEEPER("Gatekeeper", "Kontrolliert Zugang zu Entscheidungsträgern");
    
    private final String germanLabel;
    private final String description;
    
    ContactRole(String germanLabel, String description) {
        this.germanLabel = germanLabel;
        this.description = description;
    }
    
    public String getGermanLabel() {
        return germanLabel;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Compatibility methods for CustomerContact.java
    public String getRoleName() {
        return this.name();
    }
    
    public boolean getIsDecisionMakerRole() {
        return this == DECISION_MAKER;
    }
}