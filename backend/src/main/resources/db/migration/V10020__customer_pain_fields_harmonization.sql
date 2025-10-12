-- V279: Customer Pain Fields Harmonization - 100% Parity mit Lead
-- Author: Claude Code (Sprint 2.1.6 Phase 5+)
-- Date: 2025-10-09
--
-- Purpose: Migriert Customer von JSONB pain_points zu strukturierten Boolean-Feldern
--          → 100% Harmonisierung mit Lead Pain Scoring V3
--
-- Vorteile:
-- 1. Typsicher: WHERE pain_supplier_quality = true (statt JSONB-Querying)
-- 2. Lead-to-Customer Konversion: 1:1 Feld-Mapping
-- 3. Einheitliche Pain-Scoring-Logik (Lead + Customer)
-- 4. Einfaches Querying & Filtering
--
-- Felder (identisch zu Lead V278):
-- - OPERATIONAL PAINS (5): Staff Shortage, High Costs, Food Waste, Quality Inconsistency, Time Pressure
-- - SWITCHING PAINS (3): Supplier Quality, Unreliable Delivery, Poor Service
-- - Notes: pain_notes TEXT

-- ============================================================================
-- 1. ADD BOOLEAN PAIN FIELDS (identisch zu Lead)
-- ============================================================================

ALTER TABLE customers
  -- OPERATIONAL PAINS (5 Felder)
  ADD COLUMN IF NOT EXISTS pain_staff_shortage BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_high_costs BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_food_waste BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_quality_inconsistency BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_time_pressure BOOLEAN DEFAULT false,

  -- SWITCHING PAINS (3 Felder)
  ADD COLUMN IF NOT EXISTS pain_supplier_quality BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_unreliable_delivery BOOLEAN DEFAULT false,
  ADD COLUMN IF NOT EXISTS pain_poor_service BOOLEAN DEFAULT false,

  -- Notes (Freitext)
  ADD COLUMN IF NOT EXISTS pain_notes TEXT;

-- ============================================================================
-- 2. MIGRATE EXISTING DATA (JSONB → Boolean)
-- ============================================================================

-- Mapping JSONB pain_points Array → Boolean Fields
-- Legacy-Werte aus pain_points JSONB:
-- - "quality_issues" → pain_supplier_quality
-- - "high_costs" → pain_high_costs
-- - "unreliable_delivery" → pain_unreliable_delivery
-- - "poor_service" → pain_poor_service

UPDATE customers
SET
  pain_supplier_quality = (pain_points ? 'quality_issues'),
  pain_high_costs = (pain_points ? 'high_costs'),
  pain_unreliable_delivery = (pain_points ? 'unreliable_delivery'),
  pain_poor_service = (pain_points ? 'poor_service')
WHERE pain_points IS NOT NULL AND jsonb_typeof(pain_points) = 'array';

-- ============================================================================
-- 3. ADD COMMENTS (Documentation)
-- ============================================================================

-- OPERATIONAL PAINS
COMMENT ON COLUMN customers.pain_staff_shortage IS
    'Operational Pain: Personalmangel/Fachkräftemangel - Existenzieller Pain, Koch fehlt = Notfall';
COMMENT ON COLUMN customers.pain_high_costs IS
    'Operational Pain: Hoher Kostendruck - Budget-Druck, rational aber nicht sofort wechselbereit';
COMMENT ON COLUMN customers.pain_food_waste IS
    'Operational Pain: Food Waste/Überproduktion - 40-Tage-Haltbarkeit = Killer-USP, messbar, emotional';
COMMENT ON COLUMN customers.pain_quality_inconsistency IS
    'Operational Pain: Interne Qualitätsinkonsistenz - Eigene Küche hat Schwankungen';
COMMENT ON COLUMN customers.pain_time_pressure IS
    'Operational Pain: Zeitdruck/Effizienzprobleme - Prozesse vereinfachen, Zeitersparnis';

-- SWITCHING PAINS
COMMENT ON COLUMN customers.pain_supplier_quality IS
    'Switching Pain: Qualitätsprobleme beim aktuellen Lieferanten - Höchste Priorität, emotional + rational';
COMMENT ON COLUMN customers.pain_unreliable_delivery IS
    'Switching Pain: Unzuverlässige Lieferzeiten - Operativer Notstand, Restaurant ohne Zutaten = Umsatzausfall';
COMMENT ON COLUMN customers.pain_poor_service IS
    'Switching Pain: Schlechter Service/Support - Emotionaler Pain, aber nicht geschäftskritisch';

-- Notes
COMMENT ON COLUMN customers.pain_notes IS
    'Freitext für Details zu Pain-Faktoren (früher in pain_points als String)';

-- ============================================================================
-- 4. CREATE INDEXES (Performance - analog zu Lead)
-- ============================================================================

-- Index für Operational Pains (Segmentierung)
CREATE INDEX IF NOT EXISTS idx_customers_operational_pains ON customers(pain_staff_shortage, pain_food_waste, pain_high_costs)
    WHERE pain_staff_shortage = true OR pain_food_waste = true OR pain_high_costs = true;

-- Index für Switching Pains (Abwanderungsrisiko)
CREATE INDEX IF NOT EXISTS idx_customers_switching_pains ON customers(pain_supplier_quality, pain_unreliable_delivery)
    WHERE pain_supplier_quality = true OR pain_unreliable_delivery = true;

-- Index für Staff + Quality Kombination (analog zu Lead Cap-Logic)
CREATE INDEX IF NOT EXISTS idx_customers_staff_quality ON customers(pain_staff_shortage, pain_quality_inconsistency)
    WHERE pain_staff_shortage = true AND pain_quality_inconsistency = true;

-- ============================================================================
-- 5. BACKWARD COMPATIBILITY (JSONB bleibt erstmal)
-- ============================================================================

-- WICHTIG: pain_points JSONB bleibt erhalten für Backward Compatibility
-- → Alte Queries funktionieren weiter
-- → Kann in späteren Migrationen entfernt werden (wenn alle Services umgestellt sind)

COMMENT ON COLUMN customers.pain_points IS
    'DEPRECATED: Legacy JSONB field - Use Boolean pain_* fields instead (V279+)';

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Added: 8 neue Boolean Pain-Felder (identisch zu Lead V278)
-- Migrated: Bestehende pain_points JSONB Daten → Boolean Fields
-- Indexed: 3 neue Indexes (Operational Pains, Switching Pains, Staff+Quality)
-- Deprecated: pain_points JSONB (bleibt für Backward Compatibility)
--
-- Harmonisierung:
-- ✅ 100% Feld-Parity mit Lead (8 Boolean + pain_notes)
-- ✅ Lead-to-Customer Konversion: 1:1 Mapping
-- ✅ Einheitliche Pain-Scoring-Logik möglich
-- ✅ Typsicheres Querying
--
-- Next Steps:
-- - Customer Entity: Boolean-Felder + Getter/Setter
-- - CustomerDTO: Mapping aktualisieren
-- - LeadConversionService: 1:1 Pain-Mapping bei Lead → Customer
-- - Optional: pain_points JSONB entfernen (V280+)
