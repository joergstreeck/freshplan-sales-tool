package com.freshfoodz.crm.help.operations;

import com.freshfoodz.crm.help.service.CARResponse;
import com.freshfoodz.crm.help.service.GuidedOperationProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

/**
 * User-Lead-Operations Guided Operations für CAR-Strategy
 * Integration des External AI Operations-Runbooks in das Help-System
 *
 * Basiert auf: /00_infrastruktur/betrieb/artefakte/USER_LEAD_STATE_MACHINE_RUNBOOK.md
 */
@ApplicationScoped
public class UserLeadOperationsGuide implements GuidedOperationProvider {

    @Override
    public String getOperationCategory() {
        return "operations";
    }

    @Override
    public double getConfidenceForQuery(String userQuery) {
        String query = userQuery.toLowerCase();

        // High confidence für User-Lead-Protection-Anfragen
        if (query.contains("lead protection") ||
            query.contains("lead-protection") ||
            query.contains("user lead") ||
            query.contains("reminder") ||
            query.contains("expiry") ||
            query.contains("expired lead") ||
            query.contains("hold") ||
            query.contains("stop clock") ||
            query.contains("6 month") ||
            query.contains("60 day") ||
            query.contains("10 day")) {
            return 0.95;
        }

        // Medium confidence für Operations-Anfragen
        if (query.contains("operations") ||
            query.contains("runbook") ||
            query.contains("state machine") ||
            query.contains("qualified activity") ||
            query.contains("grace period")) {
            return 0.75;
        }

        // Low confidence für allgemeine Begriffe
        if (query.contains("protection") ||
            query.contains("activity") ||
            query.contains("timer")) {
            return 0.40;
        }

        return 0.0;
    }

    @Override
    public CARResponse handleQuery(String userQuery) {
        String query = userQuery.toLowerCase();

        // Spezifische Use-Cases basierend auf External AI Runbook
        if (query.contains("lead protection") || query.contains("user lead")) {
            return createLeadProtectionOverview();
        }

        if (query.contains("reminder") || query.contains("expiry")) {
            return createReminderOperationsGuide();
        }

        if (query.contains("hold") || query.contains("stop clock")) {
            return createHoldManagementGuide();
        }

        if (query.contains("state machine") || query.contains("states")) {
            return createStateMachineGuide();
        }

        if (query.contains("qualified activity")) {
            return createQualifiedActivityGuide();
        }

        // Default: Comprehensive Operations Overview
        return createComprehensiveOperationsGuide();
    }

    private CARResponse createLeadProtectionOverview() {
        return CARResponse.guidedOperation()
            .title("User-Lead-Protection System Übersicht")
            .quickSummary("6M+60T+10T User-basierte Lead-Protection mit Stop-Clock Management")
            .businessContext(
                "Lead-Protection ist USER-basiert (kein Territory-Schutz). " +
                "Schutz endet nach 6 Monaten ODER bei 60 Tagen Inaktivität (+10 Tage Grace). " +
                "Holds pausieren beide Timer (Stop-Clock)."
            )
            .actionSteps(List.of(
                "1. Lead-Protection-Status prüfen:",
                "   SELECT id, user_id, state, protection_expires_at FROM v_user_lead_protection WHERE user_id = ?",
                "",
                "2. Aktive Holds überprüfen:",
                "   SELECT * FROM lead_holds WHERE lead_id = ? AND end_at IS NULL",
                "",
                "3. Reminder-Pipeline-Status:",
                "   SELECT * FROM lead_reminders WHERE lead_id = ? ORDER BY created_at DESC LIMIT 5"
            ))
            .troubleshooting(Map.of(
                "Lead zeigt falschen Status",
                "CTE-Views refreshen: REFRESH MATERIALIZED VIEW v_user_lead_protection",

                "Reminder nicht versendet",
                "Scheduler prüfen: SELECT count(*) FROM lead_reminders WHERE window_start = current_date",

                "Hold-Berechnung falsch",
                "Active Holds prüfen: SELECT * FROM lead_holds WHERE lead_id = ? AND end_at IS NULL"
            ))
            .escalation("Bei Sample-Loss-Risk: Sofort an Operations-Lead eskalieren")
            .nextSteps(List.of(
                "Tägliche Routine um 00:15 UTC automatisch",
                "KPI-Monitoring: Reminder SLA <1h",
                "Expiry-Tracking: GRACE → EXPIRED Pipeline"
            ))
            .build();
    }

    private CARResponse createReminderOperationsGuide() {
        return CARResponse.guidedOperation()
            .title("Reminder-Pipeline Operations")
            .quickSummary("Automatische Reminder-Versendung für 60T+10T Inaktivität")
            .businessContext(
                "Reminder werden bei REMINDER_DUE Status versendet (≥60T Inaktivität, <70T). " +
                "Idempotent pro Window. Events: lead.protection.reminder"
            )
            .actionSteps(List.of(
                "1. Tagesroutine Status (automatisch 00:15 UTC):",
                "   SELECT state, count(*) FROM v_user_lead_protection GROUP BY state",
                "",
                "2. Reminder-Queue prüfen:",
                "   SELECT count(*) FROM v_user_lead_protection WHERE state = 'REMINDER_DUE'",
                "",
                "3. Reminder-Versendung validieren:",
                "   SELECT count(*) FROM lead_reminders WHERE window_start = current_date",
                "",
                "4. Manual Reminder-Replay (bei Problemen):",
                "   -- Siehe ../artefakte/reminders.sql für idempotente Replay-Logic"
            ))
            .troubleshooting(Map.of(
                "Reminder-Burst erkannt",
                "Rate-Limit per User aktivieren + Backoff/Jitter erhöhen",

                "Outbox Lag > 1h",
                "Replay-Runbook ausführen, Priorität SEV2",

                "Duplicate Reminders",
                "Window-Key-Deduplication prüfen: reminder_due_at::date"
            ))
            .kpis(List.of(
                "Reminder SLA: <1h nach Erreichen 60T Inaktivität",
                "Reminder-Queue: <100 pending",
                "Event-Lag: <5min Outbox processing"
            ))
            .build();
    }

    private CARResponse createHoldManagementGuide() {
        return CARResponse.guidedOperation()
            .title("Hold-Management (Stop-Clock)")
            .quickSummary("Holds pausieren sowohl 6M- als auch 60T-Timer für Lead-Protection")
            .businessContext(
                "Holds sind Stop-Clock-Mechanismus. Pausieren Zeit-Berechnung für beide Timer. " +
                "Verwendung: Urlaub, Krankheit, Administrative-Delays."
            )
            .actionSteps(List.of(
                "1. Hold erstellen (Stop-Clock aktivieren):",
                "   INSERT INTO lead_holds (lead_id, user_id, reason, start_at, created_by)",
                "   VALUES (?, ?, 'urlaub_user', now(), ?)",
                "",
                "2. Hold beenden (Stop-Clock deaktivieren):",
                "   UPDATE lead_holds SET end_at = now() WHERE lead_id = ? AND user_id = ? AND end_at IS NULL",
                "",
                "3. Hold-Duration überprüfen:",
                "   SELECT lead_id, user_id, SUM(COALESCE(end_at, now()) - start_at) as total_hold",
                "   FROM lead_holds GROUP BY lead_id, user_id",
                "",
                "4. Protection-Impact validieren:",
                "   SELECT id, protection_expires_at FROM v_user_lead_protection WHERE lead_id = ?"
            ))
            .troubleshooting(Map.of(
                "Hold-Overrun detected",
                "Policy-Check: Max Hold-Duration pro Lead validieren",

                "Multiple active Holds",
                "Audit: SELECT * FROM lead_holds WHERE end_at IS NULL GROUP BY lead_id HAVING count(*) > 1",

                "Hold-Calculation wrong",
                "CTE-Logic prüfen: WITH hold_windows AS ... Berechnung validieren"
            ))
            .businessRules(List.of(
                "Max Hold-Duration: Policy-gesteuert (Standard: 6 Monate)",
                "Hold-Reason erforderlich für Audit-Trail",
                "Holds pausieren BEIDE Timer (6M + 60T)",
                "Multiple Holds pro Lead addieren sich"
            ))
            .build();
    }

    private CARResponse createStateMachineGuide() {
        return CARResponse.guidedOperation()
            .title("User-Lead State-Machine")
            .quickSummary("4 Haupt-States: PROTECTED → REMINDER_DUE → GRACE → EXPIRED")
            .businessContext(
                "Vereinfachte State-Machine für User-Lead-Protection mit Hold-Integration"
            )
            .actionSteps(List.of(
                "State-Definitionen:",
                "",
                "🟢 PROTECTED:",
                "   - Aktiver Schutz seit Assignment",
                "   - Timer läuft mit Hold-Pause",
                "   - Qualifizierte Aktivitäten verlängern Schutz",
                "",
                "🟡 REMINDER_DUE:",
                "   - Effektive Inaktivität ≥60T (aber <70T)",
                "   - Reminder wird versendet",
                "   - 10-Tage Grace-Period startet",
                "",
                "🟠 GRACE:",
                "   - 10T Frist nach Reminder",
                "   - Neue qualifizierte Aktivität reaktiviert Schutz",
                "   - Kritischer Zustand für Sample-Loss-Prevention",
                "",
                "🔴 EXPIRED:",
                "   - Schutz erloschen",
                "   - 6M erreicht ODER 60T+10T ohne Aktivität",
                "   - Lead verfügbar für andere User"
            ))
            .businessRules(List.of(
                "State berechnet durch v_user_lead_protection View",
                "Hold-Duration subtrahiert von allen Timern",
                "Qualifizierte Aktivitäten reaktivieren aus GRACE",
                "Expiry ist final (kein automatisches Comeback)"
            ))
            .monitoring(List.of(
                "State-Distribution: SELECT state, count(*) FROM v_user_lead_protection GROUP BY state",
                "GRACE-Alerts: Kritischer Zustand für Business",
                "EXPIRED-Tracking: Lead-Loss-Prevention KPI"
            ))
            .build();
    }

    private CARResponse createQualifiedActivityGuide() {
        return CARResponse.guidedOperation()
            .title("Qualifizierte Aktivitäten")
            .quickSummary("Belegbare Aktivitäten die Lead-Protection verlängern")
            .businessContext(
                "Nur qualifizierte Aktivitäten zählen für Lead-Protection-Verlängerung. " +
                "Unterscheidung zwischen Marketing-Touch und echtem Sales-Engagement."
            )
            .actionSteps(List.of(
                "Qualifizierte Activity-Types:",
                "",
                "✅ QUALIFIED_CALL - Persönliches Gespräch",
                "✅ CUSTOMER_REACTION - Messbare Customer-Response",
                "✅ SCHEDULED_FOLLOWUP - Geplanter nächster Termin",
                "✅ SAMPLE_FEEDBACK - Feedback zu Sample-Delivery",
                "✅ ROI_PRESENTATION - Business-Case-Präsentation",
                "",
                "❌ Nicht qualifiziert:",
                "❌ EMAIL_SENT - Passive Communication",
                "❌ MARKETING_TOUCH - Automated Outreach",
                "❌ SYSTEM_NOTE - Administrative Entries"
            ))
            .businessRules(List.of(
                "Qualifizierte Aktivität reaktiviert aus GRACE-State",
                "Activity-Timestamp muss nach Reminder-Date liegen",
                "Multiple Aktivitäten pro Tag = 1 Protection-Extension",
                "User muss Activity-Creator sein (not assigned)"
            ))
            .validation(List.of(
                "Activity-Quality prüfen:",
                "SELECT type, count(*) FROM lead_activities WHERE created_at > now() - interval '7 days' GROUP BY type",
                "",
                "Protection-Extensions validieren:",
                "SELECT la.lead_id, la.type, la.created_at, lp.state",
                "FROM lead_activities la JOIN v_user_lead_protection lp ON la.lead_id = lp.id",
                "WHERE la.created_at > now() - interval '1 day' AND la.type IN ('QUALIFIED_CALL', 'CUSTOMER_REACTION')"
            ))
            .build();
    }

    private CARResponse createComprehensiveOperationsGuide() {
        return CARResponse.guidedOperation()
            .title("User-Lead-Operations Comprehensive Guide")
            .quickSummary("Vollständige Operations-Übersicht für User-Lead-Protection-System")
            .businessContext(
                "Enterprise-Grade Operations für 6M+60T+10T User-Lead-Protection mit " +
                "Stop-Clock-Management, Reminder-Pipeline und Business-KPI-Monitoring"
            )
            .actionSteps(List.of(
                "📊 Daily Operations (automatisch 00:15 UTC):",
                "1. State computation via v_user_lead_protection",
                "2. Reminder dispatch für REMINDER_DUE leads",
                "3. Grace→Expire processing für 10T+ grace leads",
                "4. Hold-subtraction für alle aktiven holds",
                "",
                "🔧 Manual Operations:",
                "• Lead-Status: SELECT * FROM v_user_lead_protection WHERE user_id = ?",
                "• Hold erstellen: INSERT INTO lead_holds (...)",
                "• Reminder-Replay: Siehe artefakte/reminders.sql",
                "• KPI-Check: Reminder SLA, State-Distribution, Expiry-Rate"
            ))
            .troubleshooting(Map.of(
                "Outbox lag > 1h",
                "Replay-Runbook, SEV2 Priority",

                "Reminder burst",
                "Rate-limit per User + Backoff",

                "Hold overrun",
                "Policy audit + Max-duration check",

                "State calculation wrong",
                "CTE-Views refresh + Hold-validation"
            ))
            .kpis(List.of(
                "Reminder SLA: <1h nach 60T Inaktivität",
                "GRACE-Leads: <5% of total leads",
                "Expiry-Rate: <2% monthly",
                "Hold-Usage: <10% leads mit aktiven holds"
            ))
            .escalation(
                "SEV1: Sample-Loss-Risk (GRACE→EXPIRED ohne Aktivität)\n" +
                "SEV2: Reminder-Pipeline-Failure (Outbox lag)\n" +
                "SEV3: Hold-Policy-Violation (Overrun detection)"
            )
            .relatedDocumentation(List.of(
                "• External AI Runbook: /betrieb/artefakte/USER_LEAD_STATE_MACHINE_RUNBOOK.md",
                "• SQL-Implementation: /betrieb/artefakte/v_user_lead_protection.sql",
                "• Monitoring-Alerts: /betrieb/artefakte/alerts-user-lead.yml",
                "• Operations-Dashboard: /betrieb/artefakte/seasonal-ops.json"
            ))
            .build();
    }
}