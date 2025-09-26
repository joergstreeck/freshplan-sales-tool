package de.freshplan.modules.leads.service;

import de.freshplan.infrastructure.security.RlsContext;
import de.freshplan.modules.leads.domain.*;
import de.freshplan.modules.leads.events.FollowUpProcessedEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * T+3/T+7 Follow-up Automation Service
 *
 * <p>Implementiert automatische Follow-ups gemäß TRIGGER_SPRINT_2_1.md: - T+3: Sample-Follow-up
 * nach 3 Tagen - T+7: Bulk-Order-Follow-up nach 7 Tagen
 *
 * <p>Business-Value: +40% Lead-Conversion durch systematische Automation
 */
@ApplicationScoped
public class FollowUpAutomationService {

  private static final Logger LOG = Logger.getLogger(FollowUpAutomationService.class);

  private static final int T3_DAYS = 3;
  private static final int T7_DAYS = 7;
  private static final String SYSTEM_USER_ID = "SYSTEM";

  // Dynamically generated from ActivityType enum to avoid maintenance issues
  // Uses the isMeaningfulContact() flag from the enum itself
  private static final List<ActivityType> MEANINGFUL_ACTIVITY_TYPES =
      Arrays.stream(ActivityType.values())
          .filter(ActivityType::isMeaningfulContact)
          .collect(Collectors.toList());

  @Inject EntityManager em;

  @Inject LeadService leadService;

  @Inject UserLeadSettingsService settingsService;

  @Inject EmailNotificationService emailService;

  @Inject Event<FollowUpProcessedEvent> followUpEvent;

  @ConfigProperty(name = "freshplan.followup.enabled", defaultValue = "false")
  boolean followUpEnabled;

  @ConfigProperty(name = "freshplan.followup.batchSize", defaultValue = "200")
  int batchSize;

  // Clock für testbare Zeit-Logik
  private Clock clock = Clock.systemDefaultZone();

  /** Setzt Clock für Tests (package-private für Test-Zugriff) */
  void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * Scheduled Check für fällige Follow-ups Läuft täglich um 9 Uhr morgens (konfigurierbar über
   * freshplan.followup.cron) concurrentExecution.SKIP verhindert parallele Läufe
   */
  @Scheduled(
      cron = "{freshplan.followup.cron:0 0 9 * * ?}",
      concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  @Transactional
  @RlsContext
  public void processScheduledFollowUps() {
    if (!followUpEnabled) {
      LOG.debug("Follow-up automation is disabled");
      return;
    }
    LocalDateTime now = LocalDateTime.now(clock);
    LOG.infof("Starting T+3/T+7 follow-up automation at %s", now);

    try {
      // T+3 Follow-ups: Sample-Follow-up
      int t3Count = processT3FollowUps(now);

      // T+7 Follow-ups: Bulk-Order-Follow-up
      int t7Count = processT7FollowUps(now);

      LOG.infof(
          "Follow-up automation completed: %d T+3 and %d T+7 follow-ups processed",
          t3Count, t7Count);

      // Trigger Event für Cockpit-Integration (Sprint 2.1.1 P0 HOTFIX)
      if (t3Count > 0 || t7Count > 0) {
        // Erweiterte Event-Informationen für Dashboard Integration
        FollowUpProcessedEvent event =
            FollowUpProcessedEvent.forBatch(SYSTEM_USER_ID, t3Count, t7Count);
        followUpEvent.fire(event);
        LOG.debugf("Fired follow-up event to dashboard: T3=%d, T7=%d", t3Count, t7Count);
      }

    } catch (Exception e) {
      LOG.error("Error during follow-up automation", e);
    }
  }

  /** T+3 Sample-Follow-up Sendet automatisch Sample-Angebot nach 3 Tagen ohne Aktivität */
  private int processT3FollowUps(LocalDateTime now) {
    LocalDateTime t3Threshold = now.minus(T3_DAYS, ChronoUnit.DAYS);

    // Finde Leads die für T+3 Follow-up qualifiziert sind
    List<Lead> eligibleLeads = findLeadsForFollowUp(t3Threshold, T3_DAYS);

    int processed = 0;
    for (Lead lead : eligibleLeads) {
      try {
        // Atomares Flag-Setzen für Idempotenz (verhindert Doppelversand)
        int updated =
            em.createQuery(
                    """
            UPDATE Lead l
            SET l.t3FollowupSent = true,
                l.lastFollowupAt = :now,
                l.followupCount = l.followupCount + 1
            WHERE l.id = :id AND l.t3FollowupSent = false
            """)
                .setParameter("id", lead.id)
                .setParameter("now", now)
                .executeUpdate();

        if (updated == 0) {
          // Bereits verarbeitet (Race Condition) - idempotent überspringen
          LOG.debugf("Lead %s already has T+3 follow-up, skipping", lead.id);
          continue;
        }

        // Erstelle personalisierte Sample-Campaign
        CampaignTemplate template =
            CampaignTemplate.findActiveByType(CampaignTemplate.TemplateType.SAMPLE_REQUEST);

        if (template == null) {
          LOG.warn("No active SAMPLE_REQUEST template found, reverting T+3 flag");
          // Revert Flag bei fehlendem Template
          em.createQuery(
                  """
              UPDATE Lead l
              SET l.t3FollowupSent = false,
                  l.followupCount = l.followupCount - 1
              WHERE l.id = :id
              """)
              .setParameter("id", lead.id)
              .executeUpdate();
          continue;
        }

        // Personalisiere Template mit Lead-Daten
        Map<String, String> templateData = buildTemplateData(lead);
        templateData.put("followup.type", "T+3 Sample");
        templateData.put("sample.products", getSampleRecommendations(lead));

        // Sende Follow-up Email
        boolean sent = emailService.sendCampaignEmail(lead, template, templateData);

        if (sent) {
          // Erstelle Activity für Tracking
          createFollowUpActivity(
              lead, "T3_FOLLOWUP", "T+3 Sample follow-up sent - Gratis Produktkatalog + Box");

          // Update Template-Statistiken
          template.timesUsed++;
          template.lastUsedAt = now;
          template.persist();

          processed++;
          LOG.debugf("T+3 sample follow-up sent for lead %s", lead.id);
        }

      } catch (Exception e) {
        LOG.errorf(e, "Failed to process T+3 follow-up for lead %s", lead.id);
      }
    }

    // Clear entity cache nach Bulk-Updates für frische Entities im nächsten Durchlauf
    if (processed > 0) {
      em.flush();
      em.clear();
    }

    return processed;
  }

  /** T+7 Bulk-Order-Follow-up Sendet Bulk-Order-Angebot nach 7 Tagen ohne Conversion */
  private int processT7FollowUps(LocalDateTime now) {
    LocalDateTime t7Threshold = now.minus(T7_DAYS, ChronoUnit.DAYS);

    // Finde Leads die für T+7 Follow-up qualifiziert sind
    List<Lead> eligibleLeads = findLeadsForFollowUp(t7Threshold, T7_DAYS);

    int processed = 0;
    for (Lead lead : eligibleLeads) {
      try {
        // Atomares Flag-Setzen für Idempotenz (verhindert Doppelversand)
        // Zwei separate Queries statt String-Concatenation (Gemini Review Fix)
        int updated =
            em.createQuery(
                    """
            UPDATE Lead l
            SET l.t7FollowupSent = true,
                l.lastFollowupAt = :now,
                l.followupCount = l.followupCount + 1
            WHERE l.id = :id AND l.t7FollowupSent = false
            """)
                .setParameter("id", lead.id)
                .setParameter("now", now)
                .executeUpdate();

        // Separates Status-Update wenn keine kürzliche Aktivität
        if (updated > 0 && !hasRecentMeaningfulActivity(lead, T7_DAYS)) {
          em.createQuery(
                  """
              UPDATE Lead l
              SET l.status = 'REMINDER',
                  l.reminderSentAt = :now
              WHERE l.id = :id
              """)
              .setParameter("id", lead.id)
              .setParameter("now", now)
              .executeUpdate();
        }

        if (updated == 0) {
          // Bereits verarbeitet (Race Condition) - idempotent überspringen
          LOG.debugf("Lead %s already has T+7 follow-up, skipping", lead.id);
          continue;
        }

        // Erstelle personalisierte Bulk-Order-Campaign
        CampaignTemplate template =
            CampaignTemplate.findActiveByType(CampaignTemplate.TemplateType.FOLLOW_UP);

        if (template == null) {
          LOG.warn("No active FOLLOW_UP template found, reverting T+7 flag");
          // Revert Flag bei fehlendem Template
          em.createQuery(
                  """
              UPDATE Lead l
              SET l.t7FollowupSent = false,
                  l.followupCount = l.followupCount - 1
              WHERE l.id = :id
              """)
              .setParameter("id", lead.id)
              .executeUpdate();
          continue;
        }

        // Personalisiere Template mit Lead-Daten und Bulk-Angebot
        Map<String, String> templateData = buildTemplateData(lead);
        templateData.put("followup.type", "T+7 Bulk Order");
        templateData.put("bulk.discount", getBulkDiscount(lead));
        templateData.put("bulk.minimum", getBulkMinimumOrder(lead));

        // Sende Follow-up Email
        boolean sent = emailService.sendCampaignEmail(lead, template, templateData);

        if (sent) {
          // Erstelle Activity für Tracking
          createFollowUpActivity(
              lead,
              "T7_FOLLOWUP",
              String.format(
                  "T+7 Bulk order follow-up sent - %s%% Rabatt ab %s€",
                  getBulkDiscount(lead), getBulkMinimumOrder(lead)));
          LOG.infof("Lead %s moved to REMINDER status after T+7", lead.id);

          // Update Template-Statistiken
          template.timesUsed++;
          template.lastUsedAt = now;
          template.persist();

          processed++;
          LOG.debugf("T+7 bulk order follow-up sent for lead %s", lead.id);
        }

      } catch (Exception e) {
        LOG.errorf(e, "Failed to process T+7 follow-up for lead %s", lead.id);
      }
    }

    // Clear entity cache nach Bulk-Updates für frische Entities im nächsten Durchlauf
    if (processed > 0) {
      em.flush();
      em.clear();
    }

    return processed;
  }

  /** Findet Leads die für Follow-up fällig sind Berücksichtigt Stop-the-Clock Perioden */
  private List<Lead> findLeadsForFollowUp(LocalDateTime threshold, int daysAfterCreation) {
    String jpql =
        """
            SELECT l FROM Lead l
            WHERE l.status IN (:activeStatuses)
            AND l.registeredAt <= :threshold
            AND l.clockStoppedAt IS NULL
            AND NOT EXISTS (
                SELECT 1 FROM LeadActivity a
                WHERE a.lead = l
                AND a.type IN (:meaningfulTypes)
                AND a.occurredAt > :recentActivity
            )
            ORDER BY l.registeredAt ASC
            """;

    LocalDateTime recentActivity =
        LocalDateTime.now(clock).minus(daysAfterCreation, ChronoUnit.DAYS);

    return em.createQuery(jpql, Lead.class)
        .setParameter("activeStatuses", List.of(LeadStatus.REGISTERED, LeadStatus.ACTIVE))
        .setParameter("threshold", threshold)
        .setParameter("meaningfulTypes", MEANINGFUL_ACTIVITY_TYPES)
        .setParameter("recentActivity", recentActivity)
        .setMaxResults(batchSize) // Konfigurierbare Batch-Size
        .getResultList();
  }

  /** Prüft ob Lead kürzliche meaningful Aktivität hatte */
  private boolean hasRecentMeaningfulActivity(Lead lead, int daysBack) {
    LocalDateTime since = LocalDateTime.now(clock).minus(daysBack, ChronoUnit.DAYS);

    String jpql =
        """
            SELECT COUNT(a) FROM LeadActivity a
            WHERE a.lead = :lead
            AND a.type IN (:meaningfulTypes)
            AND a.occurredAt > :since
            """;

    Long count =
        em.createQuery(jpql, Long.class)
            .setParameter("lead", lead)
            .setParameter("meaningfulTypes", MEANINGFUL_ACTIVITY_TYPES)
            .setParameter("since", since)
            .getSingleResult();

    return count > 0;
  }

  /** Erstellt Follow-up Activity für Tracking */
  private void createFollowUpActivity(Lead lead, String activityCode, String description) {
    LeadActivity activity = new LeadActivity();
    activity.lead = lead;
    activity.setType(ActivityType.NOTE); // Follow-ups als NOTE tracken
    activity.userId = SYSTEM_USER_ID;
    activity.description = description;
    activity.setOccurredAt(LocalDateTime.now(clock));
    activity.metadata.put("followup_type", activityCode);
    activity.metadata.put("automated", "true");
    activity.persist();
  }

  /** Baut Template-Daten für Personalisierung */
  private Map<String, String> buildTemplateData(Lead lead) {
    Map<String, String> data = new HashMap<>();

    // Lead-Daten
    data.put("lead.company", java.util.Objects.toString(lead.companyName, ""));
    data.put("lead.contactPerson", java.util.Objects.toString(lead.contactPerson, ""));
    data.put("lead.email", java.util.Objects.toString(lead.email, ""));
    data.put("lead.phone", java.util.Objects.toString(lead.phone, ""));

    // Territory-spezifische Daten
    Territory territory = lead.territory;
    if (territory != null) {
      data.put("territory.name", territory.name);
      data.put("territory.currency", territory.currencyCode);
      data.put("territory.taxRate", String.valueOf(territory.taxRate));
    }

    // FreshFoodz CI Farben
    data.put("color.primary", "#94C456");
    data.put("color.secondary", "#004F7B");

    return data;
  }

  /**
   * Ermittelt Sample-Empfehlungen basierend auf Lead-Profil B2B-Food-spezifisch für
   * Restaurants/Hotels/Kantinen
   */
  private String getSampleRecommendations(Lead lead) {
    // Basis-Sample-Box
    StringBuilder samples = new StringBuilder("Cook&Fresh® Starter-Box");

    // Zusätzliche Empfehlungen basierend auf Metadaten
    if (lead.metadata != null) {
      String businessType = lead.metadata.getString("businessType", "");

      switch (businessType.toUpperCase()) {
        case "RESTAURANT":
          samples.append(", Premium-Gemüse-Selection, Chef's Special");
          break;
        case "HOTEL":
          samples.append(", Frühstücks-Sortiment, Bankett-Paket");
          break;
        case "KANTINE":
        case "CANTEEN":
          samples.append(", Großküchen-Sortiment, Menü-Komponenten");
          break;
        case "CATERING":
          samples.append(", Event-Pakete, Fingerfood-Selection");
          break;
        default:
          samples.append(", Basis-Sortiment");
      }
    }

    // Saisonale Specials
    LocalDateTime now = LocalDateTime.now(clock);
    if (now.getMonthValue() >= 3 && now.getMonthValue() <= 5) {
      samples.append(", Spargel-Saison-Special");
    } else if (now.getMonthValue() >= 9 && now.getMonthValue() <= 10) {
      samples.append(", Oktoberfest-Sortiment");
    } else if (now.getMonthValue() >= 11 && now.getMonthValue() <= 12) {
      samples.append(", Weihnachts-Catering-Paket");
    }

    return samples.toString();
  }

  /** Ermittelt Bulk-Rabatt basierend auf Lead-Profil */
  private String getBulkDiscount(Lead lead) {
    if (lead.metadata == null) {
      return "10"; // Standard-Rabatt
    }

    String businessType = lead.metadata.getString("businessType", "").toUpperCase();

    return switch (businessType) {
      case "RESTAURANT" -> "15";
      case "HOTEL" -> "20";
      case "KANTINE", "CANTEEN" -> "25";
      case "CATERING" -> "10";
      default -> "10";
    };
  }

  /** Ermittelt Mindestbestellwert basierend auf Lead-Profil */
  private String getBulkMinimumOrder(Lead lead) {
    if (lead.metadata == null) {
      return "250"; // Standard-Minimum
    }

    String businessType = lead.metadata.getString("businessType", "").toUpperCase();

    return switch (businessType) {
      case "RESTAURANT" -> "500";
      case "HOTEL" -> "1000";
      case "KANTINE", "CANTEEN" -> "2000";
      case "CATERING" -> "300";
      default -> "250";
    };
  }
}
