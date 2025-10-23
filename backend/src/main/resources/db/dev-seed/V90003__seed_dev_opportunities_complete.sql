-- =====================================================
-- V90003: DEV-SEED – Opportunities (Complete Integration)
-- =====================================================
-- Created: 2025-10-13
-- Author: Claude + User
-- Sprint: 2.1.6.2 Phase 2
-- Purpose: Realistische Opportunities für UI-Testing (DEV-ONLY)
-- =====================================================
-- Migration Trigger: Sprint 2.1.6.2 Phase 2 (Opportunity Pipeline Complete)
-- =====================================================

-- ====================
-- 1. OPPORTUNITIES FROM LEADS (4x)
-- ====================

-- OPP-1: Neugeschäft (vom Lead) → NEEDS_ANALYSIS
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id, opportunity_type,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Neugeschäft: Frische Küche Berlin GmbH (vom Lead)',
    'Einkäufer Herr Müller benötigt Frischprodukte für 120-Betten-Seniorenheim. Budget: 20.000€/Monat.',
    'NEEDS_ANALYSIS',
    20000.00,
    CURRENT_DATE + INTERVAL '30 days',
    40,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    90001, -- lead_id (matches V90002)
    NULL,
    'NEUGESCHAEFT', -- Sprint 2.1.7.1: Freshfoodz Business Type
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '2 days'
);

-- OPP-2: Neugeschäft (vom Lead) → PROPOSAL
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id, opportunity_type,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Neugeschäft: BioMarkt Hamburg e.K. (vom Lead)',
    'Bio-zertifizierte Lebensmittel für 3 Filialen. Angebot erstellt.',
    'PROPOSAL',
    15000.00,
    CURRENT_DATE + INTERVAL '21 days',
    60,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    90002, -- lead_id
    NULL,
    'NEUGESCHAEFT', -- Sprint 2.1.7.1: Freshfoodz Business Type
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '1 day'
);

-- OPP-3: Neugeschäft (vom Lead) → NEGOTIATION
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id, opportunity_type,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Neugeschäft: Kita Sonnenschein München (vom Lead)',
    'Pausensnacks für 80 Kinder. Preisverhandlung läuft.',
    'NEGOTIATION',
    8000.00,
    CURRENT_DATE + INTERVAL '14 days',
    80,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    90003, -- lead_id
    NULL,
    'NEUGESCHAEFT', -- Sprint 2.1.7.1: Freshfoodz Business Type
    NOW() - INTERVAL '18 days',
    NOW() - INTERVAL '1 day'
);

-- OPP-4: Neugeschäft (vom Lead) → CLOSED_WON
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id, opportunity_type,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Neugeschäft: Fitness First Köln AG (vom Lead) - GEWONNEN',
    'Protein-Snacks für 5 Studios. Deal gewonnen! Bereit für Conversion. Preis und Liefergeschwindigkeit überzeugend.',
    'CLOSED_WON',
    12000.00,
    CURRENT_DATE - INTERVAL '2 days',
    100,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    90004, -- lead_id
    NULL,
    'NEUGESCHAEFT', -- Sprint 2.1.7.1: Freshfoodz Business Type
    NOW() - INTERVAL '25 days',
    NOW() - INTERVAL '2 days'
);

-- ====================
-- 2. OPPORTUNITIES FOR EXISTING CUSTOMERS (6x)
-- ====================

-- OPP-5: Neuer Standort → QUALIFICATION
-- Note: Only insert if customer exists (V90001 may not have run yet)
DO $$
DECLARE
  v_customer_id UUID;
BEGIN
  SELECT id INTO v_customer_id FROM customers WHERE customer_number = 'KD-DEV-001' LIMIT 1;

  IF v_customer_id IS NOT NULL THEN
    INSERT INTO opportunities (
        id, name, description, stage, expected_value, expected_close_date,
        probability, assigned_to, lead_id, customer_id, opportunity_type,
        created_at, updated_at
    ) VALUES (
        gen_random_uuid(),
        'Neuer Standort: [DEV-SEED] Kantine Schulweg 45 - Expansion',
        'Kunde möchte 2 weitere Standorte beliefern lassen.',
        'QUALIFICATION',
        18000.00,
        CURRENT_DATE + INTERVAL '45 days',
        25,
        NULL, -- assigned_to (User-Management not yet in DEV-SEED)
        NULL,
        v_customer_id,
        'NEUER_STANDORT', -- Sprint 2.1.7.1: Zusätzliche Standorte
        NOW() - INTERVAL '3 days',
        NOW() - INTERVAL '1 day'
    );
  ELSE
    RAISE WARNING 'Skipping OPP-5: Customer KD-DEV-001 not found. V90001 may not have run yet.';
  END IF;
END $$;
-- OPP-6: Sortimentserweiterung (Neue Produktlinie) → NEEDS_ANALYSIS
DO $$
DECLARE
  v_customer_id UUID;
BEGIN
  SELECT id INTO v_customer_id FROM customers WHERE customer_number = 'KD-DEV-002' LIMIT 1;
  IF v_customer_id IS NOT NULL THEN
    INSERT INTO opportunities (
        id, name, description, stage, expected_value, expected_close_date,
        probability, assigned_to, lead_id, customer_id, opportunity_type,
        created_at, updated_at
    ) VALUES (
        gen_random_uuid(),
        'Sortimentserweiterung: [DEV-SEED] Seniorenheim Blumenstraße 12 - Bio-Linie',
        'Interesse an Bio-zertifizierter Produktlinie.',
        'NEEDS_ANALYSIS',
        10000.00,
        CURRENT_DATE + INTERVAL '30 days',
        40,
        NULL,
        NULL,
        v_customer_id,
        'SORTIMENTSERWEITERUNG',
        NOW() - INTERVAL '12 days',
        NOW() - INTERVAL '5 days'
    );
  ELSE
    RAISE WARNING 'Skipping OPP-6: Customer KD-DEV-002 not found.';
  END IF;
END $$;

-- OPP-7: Verlängerung (Vertragsverlängerung) → PROPOSAL
DO $$
DECLARE
  v_customer_id UUID;
BEGIN
  SELECT id INTO v_customer_id FROM customers WHERE customer_number = 'KD-DEV-003' LIMIT 1;
  IF v_customer_id IS NOT NULL THEN
    INSERT INTO opportunities (
        id, name, description, stage, expected_value, expected_close_date,
        probability, assigned_to, lead_id, customer_id, opportunity_type,
        created_at, updated_at
    ) VALUES (
        gen_random_uuid(),
        'Verlängerung: [DEV-SEED] Kindertagesstätte Parkweg 8 - Vertragsverlängerung',
        'Jahresvertrag läuft aus. Verlängerung um 2 Jahre geplant.',
        'PROPOSAL',
        22000.00,
        CURRENT_DATE + INTERVAL '60 days',
        75,
        NULL,
        NULL,
        v_customer_id,
        'VERLAENGERUNG',
        NOW() - INTERVAL '8 days',
        NOW() - INTERVAL '2 days'
    );
  ELSE
    RAISE WARNING 'Skipping OPP-7: Customer KD-DEV-003 not found.';
  END IF;
END $$;

-- OPP-8: Sortimentserweiterung (Volumen-Erhöhung) → NEGOTIATION
DO $$
DECLARE
  v_customer_id UUID;
BEGIN
  SELECT id INTO v_customer_id FROM customers WHERE customer_number = 'KD-DEV-004' LIMIT 1;
  IF v_customer_id IS NOT NULL THEN
    INSERT INTO opportunities (
        id, name, description, stage, expected_value, expected_close_date,
        probability, assigned_to, lead_id, customer_id, opportunity_type,
        created_at, updated_at
    ) VALUES (
        gen_random_uuid(),
        'Sortimentserweiterung: [DEV-SEED] Krankenhaus Am Stadtpark 5 - Volumen +30%',
        'Neue Abteilung eröffnet. Liefervolumen soll um 30% steigen.',
        'NEGOTIATION',
        35000.00,
        CURRENT_DATE + INTERVAL '20 days',
        80,
        NULL,
        NULL,
        v_customer_id,
        'SORTIMENTSERWEITERUNG',
        NOW() - INTERVAL '15 days',
        NOW() - INTERVAL '3 days'
    );
  ELSE
    RAISE WARNING 'Skipping OPP-8: Customer KD-DEV-004 not found.';
  END IF;
END $$;

-- OPP-9: Sortimentserweiterung (Snacks) → CLOSED_WON
DO $$
DECLARE
  v_customer_id UUID;
BEGIN
  SELECT id INTO v_customer_id FROM customers WHERE customer_number = 'KD-DEV-005' LIMIT 1;
  IF v_customer_id IS NOT NULL THEN
    INSERT INTO opportunities (
        id, name, description, stage, expected_value, expected_close_date,
        probability, assigned_to, lead_id, customer_id, opportunity_type,
        created_at, updated_at
    ) VALUES (
        gen_random_uuid(),
        'Sortimentserweiterung: [DEV-SEED] Betriebskantine Hauptstraße 100 - Snacks - GEWONNEN',
        'Zusätzlich zu Hauptgerichten jetzt auch Snack-Sortiment. Deal gewonnen! Test-Phase erfolgreich. Mitarbeiter lieben die Auswahl.',
        'CLOSED_WON',
        8000.00,
        CURRENT_DATE - INTERVAL '5 days',
        100,
        NULL,
        NULL,
        v_customer_id,
        'SORTIMENTSERWEITERUNG',
        NOW() - INTERVAL '20 days',
        NOW() - INTERVAL '5 days'
    );
  ELSE
    RAISE WARNING 'Skipping OPP-9: Customer KD-DEV-005 not found.';
  END IF;
END $$;

-- OPP-10: Verlängerung (Gescheitert) → CLOSED_LOST
DO $$
DECLARE
  v_customer_id UUID;
BEGIN
  SELECT id INTO v_customer_id FROM customers WHERE customer_number = 'KD-DEV-001' LIMIT 1;
  IF v_customer_id IS NOT NULL THEN
    INSERT INTO opportunities (
        id, name, description, stage, expected_value, expected_close_date,
        probability, assigned_to, lead_id, customer_id, opportunity_type,
        created_at, updated_at
    ) VALUES (
        gen_random_uuid(),
        'Verlängerung: [DEV-SEED] Kantine Schulweg 45 - Vertragsverlängerung gescheitert - VERLOREN',
        'Vertragsverlängerung gescheitert. Kunde wechselt zu Wettbewerber. Preis zu hoch. Wettbewerber bot 15% günstiger an.',
        'CLOSED_LOST',
        15000.00,
        CURRENT_DATE - INTERVAL '10 days',
        0,
        NULL,
        NULL,
        v_customer_id,
        'VERLAENGERUNG',
        NOW() - INTERVAL '22 days',
        NOW() - INTERVAL '10 days'
    );
  ELSE
    RAISE WARNING 'Skipping OPP-10: Customer KD-DEV-001 not found.';
  END IF;
END $$;

-- ====================
-- 3. OPPORTUNITY ACTIVITIES (Skipped - Schema not yet available)
-- ====================
-- Note: opportunity_activities table will be created in future migration
-- These SEED activities will be added once the schema is ready

-- ====================
-- 4. SUCCESS MESSAGE
-- ====================
DO $$
BEGIN
  RAISE NOTICE '✅ V90003 DEV-SEED Opportunities Complete!';
  RAISE NOTICE '   - 4 Opportunities from Leads (OPP-1 to OPP-4)';
  RAISE NOTICE '   - 6 Opportunities for Customers (OPP-5 to OPP-10, if customers exist)';
  RAISE NOTICE '   - Covers all stages: QUALIFICATION → NEEDS_ANALYSIS → PROPOSAL → NEGOTIATION → CLOSED_WON/LOST';
  RAISE NOTICE '';
  RAISE NOTICE '📋 NEXT STEPS:';
  RAISE NOTICE '   1. UI: Opportunity Pipeline (Kanban Board)';
  RAISE NOTICE '   2. UI: Lead → Opportunity Conversion Dialog';
  RAISE NOTICE '   3. UI: Customer → Opportunity Creation Dialog';
END $$;
