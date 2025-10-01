-- V255: Lead Protection Basics & Progressive Profiling Stage
-- Sprint 2.1.5 - Lead Protection & Progressive Profiling (B2B)
-- ADR-004: Inline-First Architecture - keine separate lead_protection Tabelle
--
-- Kontext:
-- - Handelsvertretervertrag §3.2: 6 Monate Lead-Schutz
-- - Handelsvertretervertrag §3.3: 60-Tage-Aktivitätsstandard
-- - DSGVO Art.5: Datenminimierung via Progressive Profiling
--
-- Migration: Additive only (ALTER TABLE, kein DROP/CREATE)

-- ============================================================================
-- 1. Progress Tracking Fields (60-Tage-Aktivitätsstandard)
-- ============================================================================

ALTER TABLE leads
  ADD COLUMN progress_warning_sent_at TIMESTAMPTZ NULL;

COMMENT ON COLUMN leads.progress_warning_sent_at IS
  'Vertrag §3.3: Zeitpunkt der 60-Tage-Warnung (Tag 53: Warning, Tag 60: Email, Tag 70: Expiry)';

ALTER TABLE leads
  ADD COLUMN progress_deadline TIMESTAMPTZ NULL;

COMMENT ON COLUMN leads.progress_deadline IS
  'Vertrag §3.3: Deadline für Fortschritt (last_activity_at + 60 days). Trigger-managed via V257.';

-- ============================================================================
-- 2. Progressive Profiling Stage (DSGVO Art.5 Datenminimierung)
-- ============================================================================

ALTER TABLE leads
  ADD COLUMN stage SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE leads
  ADD CONSTRAINT leads_stage_chk CHECK (stage BETWEEN 0 AND 2);

COMMENT ON COLUMN leads.stage IS
  'DSGVO Art.5: Progressive Profiling Stage
   0 = Vormerkung (nur Firma + Stadt, keine personenbezogenen Daten)
   1 = Registrierung (optional Contact Details)
   2 = Qualifiziert (VAT ID, Expected Volume, vollständige Daten)';

-- ============================================================================
-- 3. Index für Progress-Deadline-Queries (Nightly Jobs in Sprint 2.1.6)
-- ============================================================================

-- Für Nightly Job: "Find leads mit progress_deadline < NOW() + 7 days"
-- Note: CONCURRENTLY removed for Flyway compatibility (no mixed transactional/non-transactional)
CREATE INDEX IF NOT EXISTS idx_leads_progress_deadline
  ON leads (progress_deadline)
  WHERE progress_deadline IS NOT NULL;

COMMENT ON INDEX idx_leads_progress_deadline IS
  'Performance für Nightly Job: Progress Warning Check (Tag 53-60)';

-- ============================================================================
-- 4. Index für Stage-basierte Queries
-- ============================================================================

-- Note: CONCURRENTLY removed for Flyway compatibility (no mixed transactional/non-transactional)
CREATE INDEX IF NOT EXISTS idx_leads_stage
  ON leads (stage);

COMMENT ON INDEX idx_leads_stage IS
  'Performance für Stage-Filter: Dashboard-Widgets "Vormerkungen", "Qualifizierte Leads"';

-- ============================================================================
-- Migration Audit
-- ============================================================================

-- Migration erfolgreich durchgeführt
-- Neue Felder:
--   - progress_warning_sent_at (TIMESTAMPTZ NULL)
--   - progress_deadline (TIMESTAMPTZ NULL)
--   - stage (SMALLINT DEFAULT 0, CHECK 0-2)
-- Neue Indizes:
--   - idx_leads_progress_deadline (WHERE progress_deadline IS NOT NULL)
--   - idx_leads_stage
-- Keine Daten-Migration erforderlich (alle Leads: stage=0, progress_* NULL)
