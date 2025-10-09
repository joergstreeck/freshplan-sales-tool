-- V280: Beziehungsebene - Relationship Status + Decision Maker Access
-- Author: Claude Code (Sprint 2.1.6 Phase 5+)
-- Date: 2025-10-09
--
-- Purpose: Erweitert Lead-Scoring um Engagement-Dimension (25 Punkte)
--          Basierend auf Beziehungsqualität + Entscheider-Zugang
--
-- Lead Score Formula:
-- - Umsatz: 25% (Business Metrics)
-- - Engagement: 25% (Beziehungsebene) ← THIS MIGRATION
-- - Fit: 25% (Business Type, Territory, etc.)
-- - Dringlichkeit: 25% (Pain + Urgency)
--
-- Engagement Score Berechnung:
-- - Relationship Status: 40% (max 10 Punkte)
-- - Decision Maker Access: 60% (max 15 Punkte)
-- - Recency Bonus: ±5 Punkte
-- - Touchpoint Bonus: +5 Punkte
-- - Cap: 0-25 Punkte

-- ============================================================================
-- 1. ADD RELATIONSHIP FIELDS TO LEADS
-- ============================================================================

ALTER TABLE leads
    ADD COLUMN IF NOT EXISTS relationship_status VARCHAR(30) DEFAULT 'COLD',
    ADD COLUMN IF NOT EXISTS decision_maker_access VARCHAR(30) DEFAULT 'UNKNOWN',
    ADD COLUMN IF NOT EXISTS competitor_in_use VARCHAR(100),
    ADD COLUMN IF NOT EXISTS internal_champion_name VARCHAR(100);

-- ============================================================================
-- 2. ADD CONSTRAINTS
-- ============================================================================

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.constraint_column_usage
    WHERE table_name = 'leads' AND constraint_name = 'chk_relationship_status'
  ) THEN
    ALTER TABLE leads ADD CONSTRAINT chk_relationship_status
        CHECK (relationship_status IN (
            'COLD', 'CONTACTED', 'ENGAGED_SKEPTICAL',
            'ENGAGED_POSITIVE', 'TRUSTED', 'ADVOCATE'
        ));
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.constraint_column_usage
    WHERE table_name = 'leads' AND constraint_name = 'chk_decision_maker_access'
  ) THEN
    ALTER TABLE leads ADD CONSTRAINT chk_decision_maker_access
        CHECK (decision_maker_access IN (
            'UNKNOWN', 'BLOCKED', 'INDIRECT',
            'DIRECT', 'IS_DECISION_MAKER'
        ));
  END IF;
END $$;

-- ============================================================================
-- 3. ADD COMMENTS (Documentation)
-- ============================================================================

-- Relationship Status
COMMENT ON COLUMN leads.relationship_status IS
    'Qualität der Beziehung zum Kontakt:
    - COLD (0): Kein Kontakt
    - CONTACTED (5): Erstkontakt hergestellt
    - ENGAGED_SKEPTICAL (8): Mehrere Touchpoints, skeptisch
    - ENGAGED_POSITIVE (12): Mehrere Touchpoints, positiv
    - TRUSTED (17): Vertrauensbasis vorhanden
    - ADVOCATE (25): Kämpft aktiv für uns';

-- Decision Maker Access
COMMENT ON COLUMN leads.decision_maker_access IS
    'Zugang zum Entscheider:
    - UNKNOWN (0): Noch nicht identifiziert
    - BLOCKED (-3): Entscheider bekannt, Zugang blockiert
    - INDIRECT (10): Zugang über Dritte (Assistent, Mitarbeiter, Partner)
    - DIRECT (20): Direkter Kontakt zum Entscheider
    - IS_DECISION_MAKER (25): Unser Kontakt IST der Entscheider';

-- Competitor
COMMENT ON COLUMN leads.competitor_in_use IS
    'Aktueller Wettbewerber (falls bekannt) - beeinflusst Sales-Argumentation und Switching-Pain-Strategie';

-- Internal Champion
COMMENT ON COLUMN leads.internal_champion_name IS
    'Name des internen Champions (falls vorhanden) - ermöglicht gezielte Unterstützung, kann auch Nicht-Entscheider sein';

-- ============================================================================
-- 4. CREATE INDEXES (Performance)
-- ============================================================================

-- Hot Leads: TRUSTED + ADVOCATE (priorisieren!)
CREATE INDEX IF NOT EXISTS idx_leads_relationship_hot ON leads(relationship_status)
    WHERE relationship_status IN ('TRUSTED', 'ADVOCATE');

-- Decision Maker Access: DIRECT + IS_DECISION_MAKER (schneller Close!)
CREATE INDEX IF NOT EXISTS idx_leads_decision_maker_hot ON leads(decision_maker_access)
    WHERE decision_maker_access IN ('DIRECT', 'IS_DECISION_MAKER');

-- Problem-Leads: BLOCKED (brauchen Taktik-Wechsel)
CREATE INDEX IF NOT EXISTS idx_leads_blocked ON leads(decision_maker_access)
    WHERE decision_maker_access = 'BLOCKED';

-- Kombinierter Index: Relationship + Decision Maker für Engagement-Score-Queries
CREATE INDEX IF NOT EXISTS idx_leads_engagement ON leads(relationship_status, decision_maker_access);

-- ============================================================================
-- 5. UPDATE EXISTING LEADS (Default-Werte)
-- ============================================================================

-- Alle bestehenden Leads starten mit COLD + UNKNOWN
-- (wird im Frontend/Backend beim ersten Kontakt aktualisiert)
UPDATE leads
SET relationship_status = 'COLD', decision_maker_access = 'UNKNOWN'
WHERE relationship_status IS NULL OR decision_maker_access IS NULL;

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Added: 4 neue Beziehungsfelder (relationship_status, decision_maker_access, competitor_in_use, internal_champion_name)
-- Constraints: 2 CHECK constraints für Enum-Werte
-- Indexes: 4 neue Indexes (Hot Leads, Decision Maker, Blocked, Engagement)
--
-- Engagement Score Impact:
-- - Relationship Status: 0-25 Punkte (40% Gewicht)
-- - Decision Maker Access: -3 bis +25 Punkte (60% Gewicht)
-- - Recency Bonus/Malus: ±5 Punkte (aus Activities)
-- - Touchpoint Bonus: +5 Punkte (aus Activities)
-- - Finale Range: 0-25 Punkte (gecappt)
--
-- Next Steps:
-- - Lead Entity: RelationshipStatus + DecisionMakerAccess Enums
-- - Lead Entity: calculateEngagementScore() Methode
-- - LeadActivity: isCountsAsMeaningful() Heuristik (duration>=5min oder notes>100 chars)
-- - LeadDTO: Mapping für 4 neue Felder
-- - Frontend: Types + UI für Beziehungsfelder
