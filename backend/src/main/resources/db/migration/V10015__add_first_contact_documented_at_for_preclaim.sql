-- =====================================================
-- V274: Add first_contact_documented_at for Pre-Claim Logic (Variante B)
-- =====================================================
-- Sprint 2.1.6 Phase 5 - Pre-Claim Business Logic (Variante B)
--
-- BUSINESS RULE (Handelsvertretervertrag §2(8)(a)):
-- - registered_at = IMMER gesetzt (Audit Trail: "Wann wurde Lead erfasst?")
-- - first_contact_documented_at = NULL → Pre-Claim aktiv (10 Tage Frist)
-- - first_contact_documented_at != NULL → Vollständiger Schutz
--
-- CHANGE: Neues Feld first_contact_documented_at (nullable)
-- Referenz: docs/planung/features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/VARIANTE_B_MIGRATION_GUIDE.md
-- =====================================================

-- 1. ADD COLUMN: first_contact_documented_at
ALTER TABLE leads
  ADD COLUMN first_contact_documented_at TIMESTAMPTZ NULL;

COMMENT ON COLUMN leads.first_contact_documented_at IS
  'Zeitpunkt der Erstkontakt-Dokumentation (MESSE/TELEFON: Pflicht bei Erstellung,
   EMPFEHLUNG/WEB/PARTNER/SONSTIGES: Optional, 10 Tage Frist).
   NULL = Pre-Claim aktiv (Lead geschützt, aber 10 Tage für Erstkontakt).
   NOT NULL = Vollständiger Schutz (Erstkontakt dokumentiert).
   Variante B: registered_at bleibt NOT NULL (Audit Trail).';

-- 2. INDEX für Pre-Claim Queries (Nightly Job: "Finde Pre-Claims mit abgelaufener Frist")
-- Partial Index: Nur Leads OHNE first_contact_documented_at (Pre-Claims)
CREATE INDEX IF NOT EXISTS idx_leads_first_contact_documented_at
  ON leads(registered_at)
  WHERE first_contact_documented_at IS NULL;

COMMENT ON INDEX idx_leads_first_contact_documented_at IS
  'Partial Index für Pre-Claim Queries: Findet Leads mit registered_at < NOW() - 10 days
   UND first_contact_documented_at IS NULL (Pre-Claim-Frist abgelaufen).
   Verwendet in Nightly Job für Pre-Claim-Expiry-Checks.';

-- 3. BACKFILL: Bestandsleads (bereits geschützte Leads)
-- Alle existierenden Leads bekommen first_contact_documented_at = registered_at
-- (Bestehende Geschäftsbeziehung impliziert dokumentierten Erstkontakt)
UPDATE leads
SET first_contact_documented_at = registered_at
WHERE first_contact_documented_at IS NULL
  AND registered_at IS NOT NULL;

-- 4. STATISTICS (für Query Planner)
ANALYZE leads;

-- ROLLBACK (falls nötig):
-- DROP INDEX IF EXISTS idx_leads_first_contact_documented_at;
-- ALTER TABLE leads DROP COLUMN IF EXISTS first_contact_documented_at;
