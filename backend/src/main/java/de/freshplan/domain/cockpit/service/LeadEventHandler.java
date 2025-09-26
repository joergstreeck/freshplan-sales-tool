package de.freshplan.domain.cockpit.service;

import de.freshplan.domain.cockpit.service.SalesCockpitService;
import de.freshplan.modules.leads.events.FollowUpProcessedEvent;
import de.freshplan.modules.leads.events.LeadStatusChangeEvent;
import de.freshplan.modules.leads.service.LeadService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

/**
 * Event Handler für Lead-Events zur Dashboard-Integration.
 * Leitet Lead-bezogene Events an das Dashboard-Modul weiter.
 *
 * Sprint 2.1.1 P0 HOTFIX - Integration Gap zwischen Lead-Management und Dashboard
 */
@ApplicationScoped
@de.freshplan.infrastructure.security.RlsContext
public class LeadEventHandler {

    @Inject
    SalesCockpitService cockpitService;

    @Inject
    LeadService leadService;

    /**
     * Verarbeitet Follow-up Events und aktualisiert Dashboard-Statistiken.
     *
     * @param event FollowUpProcessedEvent mit T+3/T+7 Informationen
     */
    @Transactional
    public void onFollowUpProcessed(@Observes FollowUpProcessedEvent event) {
        Log.infof("Processing follow-up event for lead: %s", event.leadId());

        try {
            // Dashboard-Statistiken aktualisieren
            updateDashboardStatistics(event);

            // Dashboard-Event wird via DashboardEventPublisher mit AFTER_COMMIT publiziert
            // (Observer-Pattern sorgt für automatische Verarbeitung)

            // Metrics tracken
            trackFollowUpMetrics(event);

        } catch (Exception e) {
            Log.errorf(e, "Failed to process follow-up event for lead: %s", event.leadId());
            // Event wird nicht propagiert, um System-Stabilität zu gewährleisten
        }
    }

    /**
     * Verarbeitet Lead-Status-Änderungen und notifiziert Dashboard.
     *
     * @param event LeadStatusChangeEvent mit Status-Transition (SoT aus PR #110)
     */
    @Transactional
    public void onLeadStatusChanged(@Observes LeadStatusChangeEvent event) {
        Log.infof("Lead status changed: %s from %s to %s",
            event.leadId(), event.oldStatus(), event.newStatus());

        try {
            // Dashboard Cache invalidieren für Real-time Updates
            cockpitService.invalidateDashboardCache(event.userId());

            // Dashboard-Event wird via DashboardEventPublisher mit AFTER_COMMIT publiziert
            // Der DashboardEventPublisher observiert das gleiche Event und handled es korrekt

        } catch (Exception e) {
            Log.errorf(e, "Failed to process lead status change for: %s", event.leadId());
        }
    }

    /**
     * Aktualisiert Dashboard-Statistiken nach Follow-up Verarbeitung.
     */
    private void updateDashboardStatistics(FollowUpProcessedEvent event) {
        // Dashboard-spezifische Statistiken werden lazy beim nächsten Abruf berechnet
        // Cache-Invalidierung reicht für Real-time Updates
        cockpitService.invalidateDashboardCache(event.userId());

        Log.debugf("Dashboard statistics invalidated for user: %s", event.userId());
    }

    // Dashboard-Updates werden jetzt via DashboardEventPublisher mit AFTER_COMMIT gehandled

    /**
     * Trackt Follow-up Metriken für Analytics.
     */
    private void trackFollowUpMetrics(FollowUpProcessedEvent event) {
        // Metrics werden via DashboardEventPublisher mit AFTER_COMMIT publiziert
        Log.debugf("Metrics tracking delegated to DashboardEventPublisher for follow-up: %s", event.followUpType());
    }
}