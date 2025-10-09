-- V278: Pain-Scoring System V3 - Operational + Switching Pains + Urgency
-- Author: Claude Code (Sprint 2.1.6 Phase 5+)
-- Date: 2025-10-09
--
-- Purpose: Erweitert Pain-Scoring mit 8 strukturierten Feldern + Urgency-Dimension
--
-- Features:
-- 1. OPERATIONAL PAINS (5 Felder): Staff Shortage, High Costs, Food Waste, Quality Inconsistency, Time Pressure
-- 2. SWITCHING PAINS (3 Felder): Supplier Quality, Unreliable Delivery, Poor Service
-- 3. URGENCY DIMENSION: NORMAL/MEDIUM/HIGH/EMERGENCY (separate von Pain)
-- 4. MULTI-PAIN BONUS: +10 Punkte wenn 4+ Pains aktiv
--
-- Pain-Score Berechnung:
-- - Base: 56 Punkte max (8 Felder)
-- - Cap: -4 bei Staff + Quality (Doppel-Counting vermeiden)
-- - Bonus: +10 bei 4+ Pains
-- - Max: 62 Punkte
--
-- Lead-Score Integration:
-- Dringlichkeit = (Pain/62 × 60%) + (Urgency/25 × 40%)

-- ============================================================================
-- 1. RENAME EXISTING FIELDS (Safe: V277 ist frisch, keine Daten)
-- ============================================================================

ALTER TABLE leads RENAME COLUMN pain_quality_issues TO pain_supplier_quality;

-- ============================================================================
-- 2. DROP UNUSED FIELD (Clean Cut)
-- ============================================================================

-- pain_limited_product_range wird nicht mehr benötigt
-- (war nie ein echter Pain, sondern Feature-Wunsch)
ALTER TABLE leads DROP COLUMN IF EXISTS pain_limited_product_range;

-- ============================================================================
-- 3. ADD NEW OPERATIONAL PAINS
-- ============================================================================

ALTER TABLE leads
  ADD COLUMN pain_staff_shortage BOOLEAN DEFAULT false,
  ADD COLUMN pain_food_waste BOOLEAN DEFAULT false,
  ADD COLUMN pain_quality_inconsistency BOOLEAN DEFAULT false,
  ADD COLUMN pain_time_pressure BOOLEAN DEFAULT false;

-- ============================================================================
-- 4. ADD URGENCY + MULTI-PAIN BONUS
-- ============================================================================

ALTER TABLE leads
  ADD COLUMN urgency_level VARCHAR(20) DEFAULT 'NORMAL',
  ADD COLUMN multi_pain_bonus INTEGER DEFAULT 0;

-- ============================================================================
-- 5. ADD CONSTRAINTS
-- ============================================================================

ALTER TABLE leads ADD CONSTRAINT chk_urgency_level
    CHECK (urgency_level IN ('NORMAL', 'MEDIUM', 'HIGH', 'EMERGENCY'));

-- ============================================================================
-- 6. UPDATE COMMENTS (Documentation)
-- ============================================================================

-- Operational Pains
COMMENT ON COLUMN leads.pain_staff_shortage IS
    'Operational Pain: Personalmangel/Fachkräftemangel (+10 Lead-Score) - Existenzieller Pain, Koch fehlt = Notfall';
COMMENT ON COLUMN leads.pain_high_costs IS
    'Operational Pain: Hoher Kostendruck (+7 Lead-Score) - Budget-Druck, rational aber nicht sofort wechselbereit';
COMMENT ON COLUMN leads.pain_food_waste IS
    'Operational Pain: Food Waste/Überproduktion (+7 Lead-Score) - 40-Tage-Haltbarkeit = Killer-USP, messbar, emotional';
COMMENT ON COLUMN leads.pain_quality_inconsistency IS
    'Operational Pain: Interne Qualitätsinkonsistenz (+6 Lead-Score, -4 wenn mit Staff kombiniert) - Eigene Küche hat Schwankungen';
COMMENT ON COLUMN leads.pain_time_pressure IS
    'Operational Pain: Zeitdruck/Effizienzprobleme (+5 Lead-Score) - Prozesse vereinfachen, Zeitersparnis';

-- Switching Pains
COMMENT ON COLUMN leads.pain_supplier_quality IS
    'Switching Pain: Qualitätsprobleme beim aktuellen Lieferanten (+10 Lead-Score) - Höchste Priorität, emotional + rational, 85% Win-Rate';
COMMENT ON COLUMN leads.pain_unreliable_delivery IS
    'Switching Pain: Unzuverlässige Lieferzeiten (+8 Lead-Score) - Operativer Notstand, Restaurant ohne Zutaten = Umsatzausfall';
COMMENT ON COLUMN leads.pain_poor_service IS
    'Switching Pain: Schlechter Service/Support (+3 Lead-Score) - Emotionaler Pain, aber nicht geschäftskritisch';

-- Urgency + Bonus
COMMENT ON COLUMN leads.urgency_level IS
    'Urgency: NORMAL(0), MEDIUM(5), HIGH(10), EMERGENCY(25) - Separate Dimension für Zeitdruck, nicht Pain. Dringlichkeit = (Pain/62 × 60%) + (Urgency/25 × 40%)';
COMMENT ON COLUMN leads.multi_pain_bonus IS
    'Auto-calculated: +10 Punkte wenn 4+ Pains aktiv (verzweifelter Lead = schneller Close + höhere Retention)';

-- ============================================================================
-- 7. CREATE INDEXES (Performance)
-- ============================================================================

-- Index für Hot Lead Queries (Urgency-Filter)
CREATE INDEX idx_leads_urgency_hot ON leads(urgency_level)
    WHERE urgency_level IN ('HIGH', 'EMERGENCY');

-- Index für Pain-Kombinationen (Doppel-Counting Detection)
CREATE INDEX idx_leads_staff_quality ON leads(pain_staff_shortage, pain_quality_inconsistency)
    WHERE pain_staff_shortage = true AND pain_quality_inconsistency = true;

-- Index für Operational Pains (Segmentierung)
CREATE INDEX idx_leads_operational_pains ON leads(pain_staff_shortage, pain_food_waste, pain_high_costs)
    WHERE pain_staff_shortage = true OR pain_food_waste = true OR pain_high_costs = true;

-- ============================================================================
-- 8. UPDATE EXISTING DATA (Defaults setzen)
-- ============================================================================

-- Bestehende Leads: Urgency auf NORMAL setzen (falls NULL)
UPDATE leads
SET urgency_level = 'NORMAL'
WHERE urgency_level IS NULL;

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Added: 4 neue Operational Pain-Felder (Staff, Food Waste, Quality, Time)
-- Renamed: pain_quality_issues → pain_supplier_quality
-- Dropped: pain_limited_product_range (nie ein echter Pain)
-- Added: urgency_level (NORMAL/MEDIUM/HIGH/EMERGENCY)
-- Added: multi_pain_bonus (auto-calculated)
-- Indexes: 3 neue Indexes (Urgency, Pain-Combos, Operational Pains)
--
-- Pain-Score Impact (Final):
-- - Operational: Staff(10), Costs(7), Waste(7), Quality(6), Time(5) = 35 max
-- - Switching: Supplier(10), Delivery(8), Service(3) = 21 max
-- - Cap: -4 bei Staff + Quality (Doppel-Counting)
-- - Bonus: +10 bei 4+ Pains
-- - Max: 62 Punkte
--
-- Lead-Score Formel:
-- Dringlichkeit = (Pain/62 × 15) + (Urgency/25 × 10) = max 25 Punkte
--
-- Next Steps:
-- - Lead Entity: calculatePainScore() implementieren
-- - LeadDTO: Neue Felder mappen
-- - Frontend: BusinessPotentialDialog mit 8 Pain-Checkboxen + Urgency-Dropdown
-- - Tests: LeadPainScoringTest (11 Test-Cases)
