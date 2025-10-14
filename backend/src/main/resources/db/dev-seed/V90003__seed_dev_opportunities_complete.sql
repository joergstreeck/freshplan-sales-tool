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

-- OPP-1: Qualified Lead → NEEDS_ANALYSIS
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Opportunity: Frische Küche Berlin GmbH (vom Lead)',
    'Einkäufer Herr Müller benötigt Frischprodukte für 120-Betten-Seniorenheim. Budget: 20.000€/Monat.',
    'NEEDS_ANALYSIS',
    20000.00,
    CURRENT_DATE + INTERVAL '30 days',
    40,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    90001, -- lead_id (matches V90002)
    NULL,
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '2 days'
);

-- OPP-2: Qualified Lead → PROPOSAL
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Opportunity: BioMarkt Hamburg e.K. (vom Lead)',
    'Bio-zertifizierte Lebensmittel für 3 Filialen. Angebot erstellt.',
    'PROPOSAL',
    15000.00,
    CURRENT_DATE + INTERVAL '21 days',
    60,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    90002, -- lead_id
    NULL,
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '1 day'
);

-- OPP-3: Qualified Lead → NEGOTIATION
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Opportunity: Kita Sonnenschein München (vom Lead)',
    'Pausensnacks für 80 Kinder. Preisverhandlung läuft.',
    'NEGOTIATION',
    8000.00,
    CURRENT_DATE + INTERVAL '14 days',
    80,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    90003, -- lead_id
    NULL,
    NOW() - INTERVAL '18 days',
    NOW() - INTERVAL '1 day'
);

-- OPP-4: Qualified Lead → CLOSED_WON (ready for Customer Conversion)
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Opportunity: Fitness First Köln AG (vom Lead) - GEWONNEN',
    'Protein-Snacks für 5 Studios. Deal gewonnen! Bereit für Conversion. Preis und Liefergeschwindigkeit überzeugend.',
    'CLOSED_WON',
    12000.00,
    CURRENT_DATE - INTERVAL '2 days',
    100,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    90004, -- lead_id
    NULL,
    NOW() - INTERVAL '25 days',
    NOW() - INTERVAL '2 days'
);

-- ====================
-- 2. OPPORTUNITIES FOR EXISTING CUSTOMERS (6x)
-- ====================

-- OPP-5: UPSELL (Expansion) → QUALIFICATION
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Upsell: [DEV-SEED] Kantine Schulweg 45 - Expansion',
    'Kunde möchte 2 weitere Standorte beliefern lassen.',
    'QUALIFICATION',
    18000.00,
    CURRENT_DATE + INTERVAL '45 days',
    25,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    NULL,
    (SELECT id FROM customers WHERE customer_number = 'KD-DEV-001' LIMIT 1),
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '1 day'
);

-- OPP-6: CROSS-SELL (New Product Line) → NEEDS_ANALYSIS
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Cross-Sell: [DEV-SEED] Seniorenheim Blumenstraße 12 - Bio-Linie',
    'Interesse an Bio-zertifizierter Produktlinie.',
    'NEEDS_ANALYSIS',
    10000.00,
    CURRENT_DATE + INTERVAL '30 days',
    40,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    NULL,
    (SELECT id FROM customers WHERE customer_number = 'KD-DEV-002' LIMIT 1),
    NOW() - INTERVAL '12 days',
    NOW() - INTERVAL '5 days'
);

-- OPP-7: RENEWAL (Contract Extension) → PROPOSAL
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Renewal: [DEV-SEED] Kindertagesstätte Parkweg 8 - Vertragsverlängerung',
    'Jahresvertrag läuft aus. Verlängerung um 2 Jahre geplant.',
    'PROPOSAL',
    22000.00,
    CURRENT_DATE + INTERVAL '60 days',
    75,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    NULL,
    (SELECT id FROM customers WHERE customer_number = 'KD-DEV-003' LIMIT 1),
    NOW() - INTERVAL '8 days',
    NOW() - INTERVAL '2 days'
);

-- OPP-8: UPSELL (Volume Increase) → NEGOTIATION
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Upsell: [DEV-SEED] Krankenhaus Am Stadtpark 5 - Volumen +30%',
    'Neue Abteilung eröffnet. Liefervolumen soll um 30% steigen.',
    'NEGOTIATION',
    35000.00,
    CURRENT_DATE + INTERVAL '20 days',
    80,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    NULL,
    (SELECT id FROM customers WHERE customer_number = 'KD-DEV-004' LIMIT 1),
    NOW() - INTERVAL '15 days',
    NOW() - INTERVAL '3 days'
);

-- OPP-9: CROSS-SELL (Snacks) → CLOSED_WON
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Cross-Sell: [DEV-SEED] Betriebskantine Hauptstraße 100 - Snacks - GEWONNEN',
    'Zusätzlich zu Hauptgerichten jetzt auch Snack-Sortiment. Deal gewonnen! Test-Phase erfolgreich. Mitarbeiter lieben die Auswahl.',
    'CLOSED_WON',
    8000.00,
    CURRENT_DATE - INTERVAL '5 days',
    100,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    NULL,
    (SELECT id FROM customers WHERE customer_number = 'KD-DEV-005' LIMIT 1),
    NOW() - INTERVAL '20 days',
    NOW() - INTERVAL '5 days'
);

-- OPP-10: RENEWAL (Lost) → CLOSED_LOST
INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date,
    probability, assigned_to, lead_id, customer_id,
    created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Renewal: [DEV-SEED] Kantine Schulweg 45 - Verlängerung gescheitert - VERLOREN',
    'Vertragsverlängerung gescheitert. Kunde wechselt zu Wettbewerber. Preis zu hoch. Wettbewerber bot 15% günstiger an.',
    'CLOSED_LOST',
    15000.00,
    CURRENT_DATE - INTERVAL '10 days',
    0,
    NULL, -- assigned_to (User-Management not yet in DEV-SEED)
    NULL,
    (SELECT id FROM customers WHERE customer_number = 'KD-DEV-001' LIMIT 1),
    NOW() - INTERVAL '90 days',
    NOW() - INTERVAL '10 days'
);

-- ====================
-- 3. OPPORTUNITY ACTIVITIES (Skipped - Schema not yet available)
-- ====================
-- OpportunityActivity table not yet implemented (will be added in Sprint 2.1.6.2 Phase 3)

-- =====================================================
-- VERIFICATION QUERY (Run after migration)
-- =====================================================
-- SELECT stage, COUNT(*), SUM(expected_value) as total_value
-- FROM opportunities
-- GROUP BY stage
-- ORDER BY stage;
--
-- Expected Output:
-- NEEDS_ANALYSIS    | 2 | €30,000
-- PROPOSAL          | 2 | €37,000
-- QUALIFICATION     | 1 | €18,000
-- NEGOTIATION       | 2 | €43,000
-- CLOSED_WON        | 2 | €20,000
-- CLOSED_LOST       | 1 | €15,000
-- =====================================================
