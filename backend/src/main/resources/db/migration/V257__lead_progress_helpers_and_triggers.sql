-- V257: Lead Progress Helper Functions & Triggers
-- Sprint 2.1.5 - Lead Protection & Progressive Profiling (B2B)
-- ADR-004: Inline-First Architecture
--
-- Kontext:
-- - Handelsvertretervertrag §3.2: 6-Monats-Schutz Berechnung
-- - Handelsvertretervertrag §3.3: 60-Tage-Progress-Deadline automatisch aktualisieren
-- - Trigger-basierte Automatisierung (kein Scheduled Job in 2.1.5)
--
-- Migration: Functions + Triggers (minimal, gezielt)

-- ============================================================================
-- 1. Function: calculate_protection_until()
-- ============================================================================

CREATE OR REPLACE FUNCTION calculate_protection_until(
  p_registered_at TIMESTAMPTZ,
  p_protection_months INTEGER DEFAULT 6
)
RETURNS TIMESTAMPTZ
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
  -- Vertrag §3.2: 6 Monate ab Registrierung
  -- Verwendung: SELECT calculate_protection_until(registered_at, protection_months)
  -- Using MAKE_INTERVAL for type-safety (Copilot Review)
  RETURN p_registered_at + MAKE_INTERVAL(months => p_protection_months);
END;
$$;

COMMENT ON FUNCTION calculate_protection_until(TIMESTAMPTZ, INTEGER) IS
  'Vertrag §3.2: Berechnet protection_until = registered_at + N Monate
   Beispiel: calculate_protection_until(2025-01-01, 6) = 2025-07-01';

-- ============================================================================
-- 2. Function: calculate_progress_deadline()
-- ============================================================================

CREATE OR REPLACE FUNCTION calculate_progress_deadline(
  p_last_activity_at TIMESTAMPTZ
)
RETURNS TIMESTAMPTZ
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
  -- Vertrag §3.3: 60 Tage ab letzter Aktivität
  -- Verwendung: SELECT calculate_progress_deadline(last_activity_at)
  RETURN p_last_activity_at + INTERVAL '60 days';
END;
$$;

COMMENT ON FUNCTION calculate_progress_deadline(TIMESTAMPTZ) IS
  'Vertrag §3.3: Berechnet progress_deadline = last_activity_at + 60 Tage
   Beispiel: calculate_progress_deadline(2025-01-01) = 2025-03-02';

-- ============================================================================
-- 3. Trigger Function: update_progress_on_activity()
-- ============================================================================

CREATE OR REPLACE FUNCTION update_progress_on_activity()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  -- Nur bei INSERT/UPDATE mit counts_as_progress=TRUE
  IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') AND NEW.counts_as_progress = TRUE THEN

    -- Update leads: progress_deadline + last_activity_at
    UPDATE leads
    SET
      progress_deadline = calculate_progress_deadline(NEW.activity_date),
      last_activity_at = NEW.activity_date,
      updated_at = NOW()
    WHERE id = NEW.lead_id;

    -- Log für Debugging (nur in Dev/Test)
    RAISE DEBUG 'Progress updated for lead_id=%: progress_deadline=%, activity_date=%',
      NEW.lead_id,
      calculate_progress_deadline(NEW.activity_date),
      NEW.activity_date;

  END IF;

  RETURN NEW;
END;
$$;

COMMENT ON FUNCTION update_progress_on_activity() IS
  'Vertrag §3.3: Automatisches Progress-Deadline Update bei counts_as_progress=TRUE
   Trigger auf lead_activities (INSERT/UPDATE)
   Setzt: leads.progress_deadline = activity_date + 60 days';

-- ============================================================================
-- 4. Trigger auf lead_activities
-- ============================================================================

DROP TRIGGER IF EXISTS trg_update_progress_on_activity ON lead_activities;

CREATE TRIGGER trg_update_progress_on_activity
  AFTER INSERT OR UPDATE OF counts_as_progress, activity_date
  ON lead_activities
  FOR EACH ROW
  EXECUTE FUNCTION update_progress_on_activity();

COMMENT ON TRIGGER trg_update_progress_on_activity ON lead_activities IS
  'Vertrag §3.3: Automatisches Progress-Deadline Update
   Wann: INSERT/UPDATE lead_activities mit counts_as_progress=TRUE
   Aktion: leads.progress_deadline = activity_date + 60 days';

-- ============================================================================
-- Migration Audit
-- ============================================================================

-- Migration erfolgreich durchgeführt
-- Neue Functions:
--   1. calculate_protection_until(registered_at, protection_months) → TIMESTAMPTZ
--   2. calculate_progress_deadline(last_activity_at) → TIMESTAMPTZ
-- Neue Trigger:
--   3. trg_update_progress_on_activity
--      - Tabelle: lead_activities
--      - Event: AFTER INSERT OR UPDATE OF counts_as_progress, activity_date
--      - Aktion: UPDATE leads SET progress_deadline, last_activity_at
-- Keine Daten-Migration erforderlich
