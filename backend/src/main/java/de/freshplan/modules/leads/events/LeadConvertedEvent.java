package de.freshplan.modules.leads.events;

import java.util.UUID;

/**
 * Event: Lead wurde zu Customer konvertiert.
 *
 * <p>Sprint 2.1.7.4: Auto-Conversion bei Opportunity WON
 *
 * <p>Wird gefeuert wenn: - Opportunity → CLOSED_WON und hat Lead - Lead → Customer Auto-Conversion
 * erfolgt
 *
 * @param leadId Original Lead ID
 * @param customerId Neu erstellter Customer ID
 * @param opportunityId Opportunity die den Conversion triggerte
 */
public record LeadConvertedEvent(Long leadId, UUID customerId, UUID opportunityId) {

  /**
   * Factory method for better readability.
   *
   * @param leadId Lead ID
   * @param customerId Customer ID
   * @param opportunityId Opportunity ID
   * @return LeadConvertedEvent instance
   */
  public static LeadConvertedEvent of(Long leadId, UUID customerId, UUID opportunityId) {
    return new LeadConvertedEvent(leadId, customerId, opportunityId);
  }
}
