package de.freshplan.modules.leads.service;

import de.freshplan.infrastructure.security.RlsContext;
import de.freshplan.modules.leads.domain.*;
import de.freshplan.modules.leads.events.FollowUpProcessedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * T+3/T+7 Follow-up Automation Service
 *
 * Implementiert automatische Follow-ups gemäß TRIGGER_SPRINT_2_1.md:
 * - T+3: Sample-Follow-up nach 3 Tagen
 * - T+7: Bulk-Order-Follow-up nach 7 Tagen
 *
 * Business-Value: +40% Lead-Conversion durch systematische Automation
 */
@ApplicationScoped
public class FollowUpAutomationService {

    private static final Logger LOG = Logger.getLogger(FollowUpAutomationService.class);

    private static final int T3_DAYS = 3;
    private static final int T7_DAYS = 7;
    private static final String SYSTEM_USER_ID = "SYSTEM";

    @Inject
    EntityManager em;

    @Inject
    LeadService leadService;

    @Inject
    UserLeadSettingsService settingsService;

    @Inject
    EmailNotificationService emailService;

    @Inject
    Event<FollowUpProcessedEvent> followUpEvent;

    // Clock für testbare Zeit-Logik
    private Clock clock = Clock.systemDefaultZone();

    /**
     * Setzt Clock für Tests (package-private für Test-Zugriff)
     */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Check für fällige Follow-ups
     * TODO: Scheduled über Quarkus Scheduler Extension aktivieren
     * Alternativ: Manuell über REST-Endpoint oder externen Scheduler aufrufen
     */
    @Transactional
    @RlsContext
    public void processScheduledFollowUps() {
        LocalDateTime now = LocalDateTime.now(clock);
        LOG.infof("Starting T+3/T+7 follow-up automation at %s", now);

        try {
            // T+3 Follow-ups: Sample-Follow-up
            int t3Count = processT3FollowUps(now);

            // T+7 Follow-ups: Bulk-Order-Follow-up
            int t7Count = processT7FollowUps(now);

            LOG.infof("Follow-up automation completed: %d T+3 and %d T+7 follow-ups processed",
                    t3Count, t7Count);

            // Trigger Event für Cockpit-Integration
            if (t3Count > 0 || t7Count > 0) {
                followUpEvent.fire(new FollowUpProcessedEvent(t3Count, t7Count, now));
            }

        } catch (Exception e) {
            LOG.error("Error during follow-up automation", e);
        }
    }

    /**
     * T+3 Sample-Follow-up
     * Sendet automatisch Sample-Angebot nach 3 Tagen ohne Aktivität
     */
    private int processT3FollowUps(LocalDateTime now) {
        LocalDateTime t3Threshold = now.minus(T3_DAYS, ChronoUnit.DAYS);

        // Finde Leads die für T+3 Follow-up qualifiziert sind
        List<Lead> eligibleLeads = findLeadsForFollowUp(t3Threshold, T3_DAYS);

        int processed = 0;
        for (Lead lead : eligibleLeads) {
            try {
                // Prüfe ob bereits T+3 Follow-up gesendet wurde über DB-Flag
                if (Boolean.TRUE.equals(lead.t3FollowupSent)) {
                    continue;
                }

                // Erstelle personalisierte Sample-Campaign
                CampaignTemplate template = CampaignTemplate.findActiveByType(
                    CampaignTemplate.TemplateType.SAMPLE_REQUEST
                );

                if (template == null) {
                    LOG.warn("No active SAMPLE_REQUEST template found, skipping T+3 follow-up");
                    continue;
                }

                // Personalisiere Template mit Lead-Daten
                Map<String, String> templateData = buildTemplateData(lead);
                templateData.put("followup.type", "T+3 Sample");
                templateData.put("sample.products", getSampleRecommendations(lead));

                // Sende Follow-up Email
                boolean sent = emailService.sendCampaignEmail(
                    lead,
                    template,
                    templateData
                );

                if (sent) {
                    // Erstelle Activity für Tracking
                    createFollowUpActivity(lead, "T3_FOLLOWUP",
                        "T+3 Sample follow-up sent - Gratis Produktkatalog + Box");

                    // Setze Follow-up Flags
                    lead.t3FollowupSent = true;
                    lead.lastFollowupAt = now;
                    lead.followupCount = lead.followupCount + 1;
                    lead.persist();

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

        return processed;
    }

    /**
     * T+7 Bulk-Order-Follow-up
     * Sendet Bulk-Order-Angebot nach 7 Tagen ohne Conversion
     */
    private int processT7FollowUps(LocalDateTime now) {
        LocalDateTime t7Threshold = now.minus(T7_DAYS, ChronoUnit.DAYS);

        // Finde Leads die für T+7 Follow-up qualifiziert sind
        List<Lead> eligibleLeads = findLeadsForFollowUp(t7Threshold, T7_DAYS);

        int processed = 0;
        for (Lead lead : eligibleLeads) {
            try {
                // Prüfe ob bereits T+7 Follow-up gesendet wurde über DB-Flag
                if (Boolean.TRUE.equals(lead.t7FollowupSent)) {
                    continue;
                }

                // Erstelle personalisierte Bulk-Order-Campaign
                CampaignTemplate template = CampaignTemplate.findActiveByType(
                    CampaignTemplate.TemplateType.FOLLOW_UP
                );

                if (template == null) {
                    LOG.warn("No active FOLLOW_UP template found, skipping T+7 follow-up");
                    continue;
                }

                // Personalisiere Template mit Lead-Daten und Bulk-Angebot
                Map<String, String> templateData = buildTemplateData(lead);
                templateData.put("followup.type", "T+7 Bulk Order");
                templateData.put("bulk.discount", getBulkDiscount(lead));
                templateData.put("bulk.minimum", getBulkMinimumOrder(lead));

                // Sende Follow-up Email
                boolean sent = emailService.sendCampaignEmail(
                    lead,
                    template,
                    templateData
                );

                if (sent) {
                    // Erstelle Activity für Tracking
                    createFollowUpActivity(lead, "T7_FOLLOWUP",
                        String.format("T+7 Bulk order follow-up sent - %s%% Rabatt ab %s€",
                            getBulkDiscount(lead), getBulkMinimumOrder(lead)));

                    // Setze Follow-up Flags
                    lead.t7FollowupSent = true;
                    lead.lastFollowupAt = now;
                    lead.followupCount = lead.followupCount + 1;

                    // Update Lead-Status wenn keine Response
                    if (!hasRecentMeaningfulActivity(lead, T7_DAYS)) {
                        lead.status = LeadStatus.REMINDER;
                        lead.reminderSentAt = now;
                    }

                    lead.persist();
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

        return processed;
    }

    /**
     * Findet Leads die für Follow-up fällig sind
     * Berücksichtigt Stop-the-Clock Perioden
     */
    @SuppressWarnings("unchecked")
    private List<Lead> findLeadsForFollowUp(LocalDateTime threshold, int daysAfterCreation) {
        String jpql = """
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

        LocalDateTime recentActivity = LocalDateTime.now().minus(daysAfterCreation, ChronoUnit.DAYS);

        return em.createQuery(jpql, Lead.class)
                .setParameter("activeStatuses", List.of(
                    LeadStatus.REGISTERED,
                    LeadStatus.ACTIVE
                ))
                .setParameter("threshold", threshold)
                .setParameter("meaningfulTypes", List.of(
                    ActivityType.EMAIL,
                    ActivityType.CALL,
                    ActivityType.MEETING,
                    ActivityType.SAMPLE_SENT,
                    ActivityType.ORDER
                ))
                .setParameter("recentActivity", recentActivity)
                .setMaxResults(100) // Batch-Verarbeitung für Performance
                .getResultList();
    }


    /**
     * Prüft ob Lead kürzliche meaningful Aktivität hatte
     */
    private boolean hasRecentMeaningfulActivity(Lead lead, int daysBack) {
        LocalDateTime since = LocalDateTime.now().minus(daysBack, ChronoUnit.DAYS);

        String jpql = """
            SELECT COUNT(a) FROM LeadActivity a
            WHERE a.lead = :lead
            AND a.type IN (:meaningfulTypes)
            AND a.occurredAt > :since
            """;

        Long count = em.createQuery(jpql, Long.class)
                .setParameter("lead", lead)
                .setParameter("meaningfulTypes", List.of(
                    ActivityType.EMAIL,
                    ActivityType.CALL,
                    ActivityType.MEETING,
                    ActivityType.SAMPLE_SENT,
                    ActivityType.ORDER
                ))
                .setParameter("since", since)
                .getSingleResult();

        return count > 0;
    }

    /**
     * Erstellt Follow-up Activity für Tracking
     */
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

    /**
     * Baut Template-Daten für Personalisierung
     */
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
     * Ermittelt Sample-Empfehlungen basierend auf Lead-Profil
     * B2B-Food-spezifisch für Restaurants/Hotels/Kantinen
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

    /**
     * Ermittelt Bulk-Rabatt basierend auf Lead-Profil
     */
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

    /**
     * Ermittelt Mindestbestellwert basierend auf Lead-Profil
     */
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