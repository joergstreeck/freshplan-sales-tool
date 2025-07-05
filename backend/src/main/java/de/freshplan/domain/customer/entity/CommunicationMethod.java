package de.freshplan.domain.customer.entity;

/**
 * Enumeration for preferred communication methods.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum CommunicationMethod {
    
    EMAIL("Email"),
    PHONE("Telefon"),
    MOBILE("Mobil"),
    FAX("Fax"),
    POST("Post"),
    TEAMS("Microsoft Teams"),
    SLACK("Slack"),
    WHATSAPP("WhatsApp");
    
    private final String displayName;
    
    CommunicationMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}