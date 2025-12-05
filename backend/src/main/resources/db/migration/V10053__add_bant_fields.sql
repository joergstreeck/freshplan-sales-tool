-- ============================================================================
-- V10053: BANT-Felder für Lead-Qualifizierung
-- Sprint 2.1.8 Phase 4: Advanced Search + BANT
-- ============================================================================
--
-- BANT = Budget, Authority, Need, Timeline
-- Standard-Framework für B2B Lead-Qualifizierung
--
-- Diese Felder ergänzen das bestehende Lead Scoring System:
-- - Führen zu besserer Priorisierung
-- - Ermöglichen systematische Qualifizierung
-- - Integrieren sich in Lead Score Berechnung
-- ============================================================================

-- 1. BANT Core Fields
-- Budget: Wurde Budget bestätigt? Welche Größenordnung?
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_budget_status VARCHAR(20);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_budget_amount DECIMAL(12,2);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_budget_timeline VARCHAR(30);

-- Authority: Ist der Ansprechpartner der Entscheider?
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_authority_level VARCHAR(30);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_authority_decision_process TEXT;

-- Need: Besteht ein konkreter Bedarf?
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_need_level VARCHAR(20);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_need_pain_points TEXT;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_need_current_solution VARCHAR(255);

-- Timeline: Wann soll die Entscheidung fallen?
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_timeline_status VARCHAR(30);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_timeline_target_date DATE;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_timeline_urgency VARCHAR(20);

-- 2. BANT Composite Score (0-100)
-- Wird automatisch berechnet aus den 4 Dimensionen
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_score INTEGER;

-- 3. BANT Qualifizierungs-Timestamp
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_qualified_at TIMESTAMP;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS bant_qualified_by VARCHAR(50);

-- 4. Default-Werte für bestehende Leads
UPDATE leads SET
  bant_budget_status = 'UNKNOWN',
  bant_authority_level = 'UNKNOWN',
  bant_need_level = 'UNKNOWN',
  bant_timeline_status = 'UNKNOWN'
WHERE bant_budget_status IS NULL;

-- 5. Constraints für ENUM-ähnliche Werte
-- Budget Status: UNKNOWN, CONFIRMED, PENDING, REJECTED
ALTER TABLE leads ADD CONSTRAINT chk_bant_budget_status
  CHECK (bant_budget_status IS NULL OR bant_budget_status IN ('UNKNOWN', 'CONFIRMED', 'PENDING', 'REJECTED', 'NO_BUDGET'));

-- Authority Level: UNKNOWN, DECISION_MAKER, INFLUENCER, CHAMPION, BLOCKER, USER
ALTER TABLE leads ADD CONSTRAINT chk_bant_authority_level
  CHECK (bant_authority_level IS NULL OR bant_authority_level IN ('UNKNOWN', 'DECISION_MAKER', 'INFLUENCER', 'CHAMPION', 'BLOCKER', 'USER'));

-- Need Level: UNKNOWN, CRITICAL, HIGH, MEDIUM, LOW, NONE
ALTER TABLE leads ADD CONSTRAINT chk_bant_need_level
  CHECK (bant_need_level IS NULL OR bant_need_level IN ('UNKNOWN', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'NONE'));

-- Timeline Status: UNKNOWN, IMMEDIATE, QUARTER, HALF_YEAR, YEAR, FUTURE, NO_TIMELINE
ALTER TABLE leads ADD CONSTRAINT chk_bant_timeline_status
  CHECK (bant_timeline_status IS NULL OR bant_timeline_status IN ('UNKNOWN', 'IMMEDIATE', 'QUARTER', 'HALF_YEAR', 'YEAR', 'FUTURE', 'NO_TIMELINE'));

-- 6. Kommentare für Dokumentation
COMMENT ON COLUMN leads.bant_budget_status IS 'Sprint 2.1.8: BANT Budget Status (UNKNOWN/CONFIRMED/PENDING/REJECTED/NO_BUDGET)';
COMMENT ON COLUMN leads.bant_budget_amount IS 'Sprint 2.1.8: BANT Budget Betrag in EUR';
COMMENT ON COLUMN leads.bant_authority_level IS 'Sprint 2.1.8: BANT Authority Level (DECISION_MAKER/INFLUENCER/CHAMPION/BLOCKER/USER)';
COMMENT ON COLUMN leads.bant_need_level IS 'Sprint 2.1.8: BANT Need Level (CRITICAL/HIGH/MEDIUM/LOW/NONE)';
COMMENT ON COLUMN leads.bant_timeline_status IS 'Sprint 2.1.8: BANT Timeline Status (IMMEDIATE/QUARTER/HALF_YEAR/YEAR/FUTURE)';
COMMENT ON COLUMN leads.bant_score IS 'Sprint 2.1.8: BANT Composite Score (0-100)';
