package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.*;
import de.freshplan.test.support.TestTx;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Debug test to understand why follow-up service doesn't find leads */
@QuarkusTest
class FollowUpDebugTest {

  @Inject EntityManager em;
  @Inject FollowUpAutomationService followUpService;

  @Test
  void debugFollowUpCandidates() {
    // Create test data in committed transaction
    Long leadId =
        TestTx.committed(
            () -> {
              // Clean up first
              em.createQuery("DELETE FROM LeadActivity").executeUpdate();
              em.createQuery("DELETE FROM Lead").executeUpdate();
              em.createQuery("DELETE FROM CampaignTemplate").executeUpdate();
              em.createQuery("DELETE FROM Territory").executeUpdate();

              // Create territory
              Territory territory = new Territory();
              territory.id = "DE";
              territory.name = "Deutschland";
              territory.countryCode = "DE";
              territory.languageCode = "de";
              territory.currencyCode = "EUR";
              territory.taxRate = new java.math.BigDecimal("19.00");
              territory.active = true;
              em.persist(territory);

              // Create lead
              Lead lead = new Lead();
              lead.companyName = "Debug Restaurant GmbH";
              lead.contactPerson = "Max Mustermann";
              lead.email = "debug@restaurant.de";
              lead.phone = "+49 89 123456";
              lead.status = LeadStatus.ACTIVE;
              lead.ownerUserId = UUID.randomUUID().toString();
              lead.createdBy = "test-user";
              lead.territory = territory;
              lead.registeredAt = LocalDateTime.now().minusDays(4); // 4 days old
              lead.metadata = new io.vertx.core.json.JsonObject();
              lead.metadata.put("businessType", "RESTAURANT");
              lead.t3FollowupSent = false;
              lead.t7FollowupSent = false;
              lead.followupCount = 0;
              lead.clockStoppedAt = null;
              em.persist(lead);

              // Create templates
              CampaignTemplate template = new CampaignTemplate();
              template.name = "T+3 Sample Follow-up";
              template.subject = "Test Subject";
              template.htmlContent = "<p>Test</p>";
              template.templateType = CampaignTemplate.TemplateType.SAMPLE_REQUEST;
              template.active = true;
              template.createdAt = LocalDateTime.now();
              template.updatedAt = LocalDateTime.now();
              em.persist(template);

              em.flush();
              return lead.id;
            });

    System.out.println("Created lead with ID: " + leadId);

    // Check what the query finds
    var candidates =
        em.createQuery(
                "SELECT l FROM Lead l "
                    + "WHERE l.status IN (:statuses) "
                    + "AND l.registeredAt <= :threshold "
                    + "AND l.t3FollowupSent = false "
                    + "AND l.clockStoppedAt IS NULL",
                Lead.class)
            .setParameter("statuses", java.util.List.of(LeadStatus.REGISTERED, LeadStatus.ACTIVE))
            .setParameter("threshold", LocalDateTime.now().minusDays(3))
            .getResultList();

    System.out.println("Found " + candidates.size() + " candidates for T+3 follow-up");
    for (Lead l : candidates) {
      System.out.println(
          " - Lead ID: "
              + l.id
              + ", Company: "
              + l.companyName
              + ", Status: "
              + l.status
              + ", Registered: "
              + l.registeredAt);
    }

    // Check templates
    var templates =
        em.createQuery(
                "SELECT t FROM CampaignTemplate t WHERE t.templateType = :type AND t.active = true",
                CampaignTemplate.class)
            .setParameter("type", CampaignTemplate.TemplateType.SAMPLE_REQUEST)
            .getResultList();

    System.out.println("Found " + templates.size() + " active SAMPLE_REQUEST templates");

    // Try to process
    followUpService.processScheduledFollowUps();

    // Check if lead was updated
    Lead updatedLead = em.find(Lead.class, leadId);
    System.out.println("Lead after processing:");
    System.out.println(" - t3FollowupSent: " + updatedLead.t3FollowupSent);
    System.out.println(" - followupCount: " + updatedLead.followupCount);
    System.out.println(" - lastFollowupAt: " + updatedLead.lastFollowupAt);
  }
}
