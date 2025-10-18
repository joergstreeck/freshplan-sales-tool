-- =====================================================
-- Migration V90006: Cleanup Opportunity Names (DEV-SEED)
-- =====================================================
-- Beschreibung: Entfernt OpportunityType-Präfixe aus Namen
--               (Neuer Standort:, Sortimentserweiterung:, etc.)
--               da diese jetzt als Badges angezeigt werden
--
-- Sprint: 2.1.7.1 (OpportunityType Badges)
-- Autor: Claude
-- Datum: 2025-10-16
-- =====================================================

-- Bereinige alle Opportunity-Namen (nur DEV-SEED Daten)
UPDATE opportunities
SET name = REGEXP_REPLACE(
    name,
    '^(Neuer Standort|Sortimentserweiterung|Verlängerung|Verlaengerung|Neugeschäft|Neugeschaeft):\s*',
    '',
    'i'
)
WHERE name ~ '^(Neuer Standort|Sortimentserweiterung|Verlängerung|Verlaengerung|Neugeschäft|Neugeschaeft):\s*';

-- Verification: Zeige bereinigte Namen
-- SELECT id, name, opportunity_type FROM opportunities WHERE name LIKE '[DEV-SEED]%' ORDER BY name;
