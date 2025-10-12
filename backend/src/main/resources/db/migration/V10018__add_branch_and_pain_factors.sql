-- V277: Add Branch/Chain & Pain-Factors to leads table
--
-- Purpose: Geschäftspotenzial erweitern mit Filialen/Kettenbetrieben & Pain-Faktoren
--
-- Features:
-- 1. Filialen/Kettenbetriebe (branch_count, is_chain)
--    → Gesamtpotenzial = estimatedVolume × branchCount
--    → Lead-Score Boost bei Kettenbetrieben
--
-- 2. Pain-Faktoren (5 Boolean-Felder + Freitext)
--    → Qualitätsprobleme, Kosten, Lieferzeiten, Sortiment, Service
--    → Lead-Score Boost (bis zu +31 Punkte)
--
-- Sprint: 2.1.6 Phase 5+
-- Date: 2025-10-08

-- ============================================================================
-- 1. ADD COLUMNS: Filialen/Kettenbetriebe
-- ============================================================================

ALTER TABLE leads
  ADD COLUMN IF NOT EXISTS branch_count INTEGER DEFAULT 1,
  ADD COLUMN IF NOT EXISTS is_chain BOOLEAN DEFAULT false;

COMMENT ON COLUMN leads.branch_count IS 'Anzahl Filialen/Standorte. Default: 1 (Einzelstandort). Multiplier für estimatedVolume.';
COMMENT ON COLUMN leads.is_chain IS 'Kettenbetrieb ja/nein. Wenn true, ist branch_count > 1 zu erwarten.';

-- ============================================================================
-- 2. ADD COLUMNS: Pain-Faktoren (5 Standard-Faktoren)
-- ============================================================================

ALTER TABLE leads
  ADD COLUMN IF NOT EXISTS pain_quality_issues BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_high_costs BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_unreliable_delivery BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_limited_product_range BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_poor_service BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_notes TEXT;

COMMENT ON COLUMN leads.pain_quality_issues IS 'Pain: Qualitätsprobleme beim aktuellen Lieferanten (Höchste Prio: +10 Lead-Score)';
COMMENT ON COLUMN leads.pain_high_costs IS 'Pain: Zu hohe Kosten, Budget-Druck (+5 Lead-Score)';
COMMENT ON COLUMN leads.pain_unreliable_delivery IS 'Pain: Unzuverlässige Lieferzeiten (+8 Lead-Score)';
COMMENT ON COLUMN leads.pain_limited_product_range IS 'Pain: Eingeschränktes Sortiment (+3 Lead-Score)';
COMMENT ON COLUMN leads.pain_poor_service IS 'Pain: Schlechter Service/Support (+5 Lead-Score)';
COMMENT ON COLUMN leads.pain_notes IS 'Freitext: Weitere Details zu Pain-Faktoren';

-- ============================================================================
-- 3. CREATE INDEX: Pain-Faktoren für Filtering/Segmentierung
-- ============================================================================

-- Index für "Leads mit hohen Pain-Faktoren" (Quick Filter für Vertriebler)
CREATE INDEX IF NOT EXISTS idx_leads_pain_factors
  ON leads(pain_quality_issues, pain_high_costs, pain_unreliable_delivery)
  WHERE pain_quality_issues = true
     OR pain_high_costs = true
     OR pain_unreliable_delivery = true;

-- Index für Kettenbetriebe (hohe Priorität)
CREATE INDEX IF NOT EXISTS idx_leads_chains
  ON leads(is_chain, branch_count)
  WHERE is_chain = true AND branch_count > 1;

-- ============================================================================
-- 4. UPDATE EXISTING LEADS: Defaults setzen
-- ============================================================================

-- Bestehende Leads ohne branch_count → Default 1 (Einzelstandort)
UPDATE leads
SET branch_count = 1,
    is_chain = false
WHERE branch_count IS NULL;

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Added: 2 Filial-Felder (branch_count, is_chain)
-- Added: 6 Pain-Felder (5 Boolean + 1 TEXT)
-- Indexes: 2 neue Indexes (Pain-Faktoren, Kettenbetriebe)
-- Defaults: Bestehende Leads mit branch_count=1, is_chain=false
--
-- Lead-Score Impact:
-- - Kettenbetrieb (branch_count > 1): +10 Punkte
-- - Pain-Faktoren (gesamt): bis zu +31 Punkte
--   → Quality Issues: +10
--   → High Costs: +5
--   → Unreliable Delivery: +8
--   → Limited Range: +3
--   → Poor Service: +5
--
-- Next Steps:
-- - Lead-Scoring-Service: Pain-Boost implementieren
-- - Frontend: BusinessPotentialCard mit Pain-Chips
-- - LeadDTO: Neue Felder mappen
