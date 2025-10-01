-- V256: Lead Activities Augmentation for Progress Tracking
-- Sprint 2.1.5 - Lead Protection & Progressive Profiling (B2B)
-- ADR-004: Inline-First Architecture
--
-- Kontext:
-- - Handelsvertretervertrag §3.3: 60-Tage-Aktivitätsstandard
-- - Aktivitäten müssen explizit als "Progress" markiert werden
-- - Zusätzliche Aktivitäten-Felder für Vertriebsdokumentation
--
-- Bestehende Struktur (V229):
--   - lead_activities(id, lead_id, user_id, activity_type, activity_date, description, metadata)
--   - is_meaningful_contact (BOOLEAN DEFAULT FALSE)
--   - resets_timer (BOOLEAN DEFAULT FALSE)
--
-- Migration: Additive only (ALTER TABLE, kein DROP)

-- ============================================================================
-- 1. Progress Tracking Flag (60-Tage-Aktivitätsstandard)
-- ============================================================================

ALTER TABLE lead_activities
  ADD COLUMN counts_as_progress BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN lead_activities.counts_as_progress IS
  'Vertrag §3.3: Aktivität gilt als "belegbarer Fortschritt" (Standard: FALSE, konservativ)
   TRUE löst Trigger aus → leads.progress_deadline = NOW() + 60 days (V257)
   Beispiele für Progress: Call mit Outcome, Meeting durchgeführt, Demo abgeschlossen';

-- ============================================================================
-- 2. Vertriebsdokumentation (erweiterte Felder)
-- ============================================================================

ALTER TABLE lead_activities
  ADD COLUMN summary VARCHAR(500) NULL;

COMMENT ON COLUMN lead_activities.summary IS
  'Kurzzusammenfassung der Aktivität (max 500 Zeichen). Für Dashboard/Timeline-Anzeige.';

ALTER TABLE lead_activities
  ADD COLUMN outcome VARCHAR(50) NULL;

COMMENT ON COLUMN lead_activities.outcome IS
  'Ergebnis der Aktivität: positive_interest | needs_more_info | not_interested | callback_scheduled | demo_scheduled | closed_won | closed_lost';

ALTER TABLE lead_activities
  ADD COLUMN next_action VARCHAR(200) NULL;

COMMENT ON COLUMN lead_activities.next_action IS
  'Geplante Folgeaktion (freitext). Beispiel: "Angebot schicken bis Fr", "Follow-up Call in 2 Wochen"';

ALTER TABLE lead_activities
  ADD COLUMN next_action_date DATE NULL;

COMMENT ON COLUMN lead_activities.next_action_date IS
  'Datum für geplante Folgeaktion. Für Reminder-System in Sprint 2.1.6+';

ALTER TABLE lead_activities
  ADD COLUMN performed_by VARCHAR(50) NULL;

COMMENT ON COLUMN lead_activities.performed_by IS
  'User ID des tatsächlichen Ausführenden (falls abweichend von user_id).
   Beispiel: Partner A erfasst Aktivität, die Partner B durchgeführt hat.';

-- ============================================================================
-- 3. Backfill performed_by aus user_id (für bestehende Daten)
-- ============================================================================

UPDATE lead_activities
SET performed_by = user_id
WHERE performed_by IS NULL;

COMMENT ON TABLE lead_activities IS
  'Lead Activity Log mit Progress-Tracking (V229 + V256)
   V229: Basis-Felder (activity_type, description, is_meaningful_contact)
   V256: Progress-Flag (counts_as_progress) + Vertriebsdoku (summary, outcome, next_action)';

-- ============================================================================
-- 4. Index für Progress-Queries
-- ============================================================================

-- Für Trigger V257: "Find latest activity WHERE counts_as_progress=true"
-- Note: CONCURRENTLY removed for Flyway compatibility (no mixed transactional/non-transactional)
CREATE INDEX IF NOT EXISTS idx_lead_activities_progress
  ON lead_activities (lead_id, activity_date DESC)
  WHERE counts_as_progress = TRUE;

COMMENT ON INDEX idx_lead_activities_progress IS
  'Performance für Trigger update_progress_on_activity (V257): Letzte Progress-Aktivität finden';

-- ============================================================================
-- Migration Audit
-- ============================================================================

-- Migration erfolgreich durchgeführt
-- Neue Felder:
--   - counts_as_progress (BOOLEAN DEFAULT FALSE) - konservativ
--   - summary (VARCHAR(500) NULL)
--   - outcome (VARCHAR(50) NULL)
--   - next_action (VARCHAR(200) NULL)
--   - next_action_date (DATE NULL)
--   - performed_by (VARCHAR(50) NULL)
-- Backfill:
--   - performed_by aus user_id kopiert für bestehende Aktivitäten
-- Neue Indizes:
--   - idx_lead_activities_progress (WHERE counts_as_progress=TRUE)
