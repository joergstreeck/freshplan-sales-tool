-- ============================================================================
-- V90001: DEV-SEED Complete Customer Data
-- ============================================================================
-- Purpose: Realistic seed data for manual UI testing in development
-- Environment: ONLY loaded in %dev (NOT in %test, NOT in production)
--
-- ⚠️  IMPORTANT: Tests must NEVER use this data!
-- ⚠️  Tests create their own data via TestDataFactory
--
-- Idempotent: Can be run multiple times (ON CONFLICT DO NOTHING)
--
-- Structure (respecting FK dependencies):
--   1. customers (5 realistic German B2B food customers)
--   2. customer_locations (6 locations, multi-location for KD-DEV-003)
--   3. customer_addresses (6 addresses, realistic German addresses)
--   4. customer_contacts (8 contacts, decision makers + secondary)
--
-- Test Scenarios:
--   KD-DEV-001: Premium Restaurant (AKTIV, Happy Path)
--   KD-DEV-002: Hotel Prospect (PROSPECT, Qualification Phase)
--   KD-DEV-003: Catering Multi-Location (AKTIV, DE+CH)
--   KD-DEV-004: Incomplete Kantine (PROSPECT, NULL Volume - Edge Case)
--   KD-DEV-005: Archived Großhandel (ARCHIVIERT, Old Contact)
-- ============================================================================


-- ============================================================================
-- 1. CUSTOMERS (5)
-- ============================================================================

INSERT INTO customers (
    id,
    customer_number,
    company_name,
    trading_name,
    legal_form,
    customer_type,
    industry,
    business_type,
    status,
    lifecycle_stage,
    partner_status,
    expected_annual_volume,
    actual_annual_volume,
    payment_terms,
    credit_limit,
    delivery_condition,
    risk_score,
    last_contact_date,
    next_follow_up_date,
    locations_germany,
    locations_austria,
    locations_switzerland,
    locations_rest_eu,
    total_locations_eu,
    pain_staff_shortage,
    pain_high_costs,
    pain_food_waste,
    pain_quality_inconsistency,
    pain_time_pressure,
    pain_supplier_quality,
    pain_unreliable_delivery,
    pain_poor_service,
    pain_notes,
    primary_financing,
    -- NEW: Lead Parity Fields (Sprint 2.1.7.4, V10032)
    kitchen_size,
    employee_count,
    branch_count,
    is_chain,
    estimated_volume,
    -- End of new fields
    is_deleted,
    is_test_data,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
-- ============================================================================
-- KD-DEV-001: Restaurant Silbertanne München (Premium, AKTIV)
-- Use Case: Happy Path, Standard Workflows, Active Customer
-- Characteristics: 5 locations, high volume, staff shortage + cost pressure
-- ============================================================================
(
    'c0000000-0001-0000-0000-000000000001'::uuid,
    'KD-DEV-001',
    'Restaurant Silbertanne München GmbH',
    'Silbertanne',
    'GMBH',
    'UNTERNEHMEN',
    NULL,
    'RESTAURANT',
    'AKTIV',
    'RETENTION',
    'KEIN_PARTNER',
    180000.00,
    165000.00,
    'NETTO_30',
    25000.00,
    'STANDARD',
    15,
    NOW() - INTERVAL '7 days',
    NOW() + INTERVAL '14 days',
    5,
    0,
    0,
    0,
    5,
    TRUE,  -- pain_staff_shortage
    TRUE,  -- pain_high_costs
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    'Personalmangel besonders in Stoßzeiten, Suche nach zuverlässigen Lieferanten für Frischware',
    'PRIVATE',
    -- NEW: Lead Parity Fields (Sprint 2.1.7.4, V10032)
    'MITTEL',      -- kitchen_size (5 Standorte → mittelgroße Küchen)
    22,            -- employee_count (durchschnittlich pro Standort)
    5,             -- branch_count (5 Standorte)
    TRUE,          -- is_chain (Multi-Location Restaurant)
    180000.00,     -- estimated_volume (= expected_annual_volume)
    -- End of new fields
    FALSE,
    FALSE,  -- is_test_data
    NOW() - INTERVAL '18 months',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '7 days',
    'DEV-SEED-SYSTEM'
),

-- ============================================================================
-- KD-DEV-002: Hotel Nordwind Hamburg (PROSPECT, Qualification)
-- Use Case: Lead Management, Qualification Phase, Follow-up Testing
-- Characteristics: Single location, moderate volume, quality issues
-- ============================================================================
(
    'c0000000-0002-0000-0000-000000000001'::uuid,
    'KD-DEV-002',
    'Hotel Nordwind Hamburg GmbH & Co. KG',
    'Hotel Nordwind',
    'GMBH_CO_KG',
    'UNTERNEHMEN',
    NULL,
    'HOTEL',
    'PROSPECT',
    'ACQUISITION',
    'KEIN_PARTNER',
    95000.00,
    NULL,
    'NETTO_30',
    NULL,
    'STANDARD',
    25,
    NOW() - INTERVAL '14 days',
    NOW() + INTERVAL '3 days',
    1,
    0,
    0,
    0,
    1,
    FALSE,
    FALSE,
    TRUE,  -- pain_food_waste
    TRUE,  -- pain_quality_inconsistency
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    'Frühstücksbuffet: hoher Food Waste, inkonsistente Qualität bei Backwaren',
    'MIXED',
    -- NEW: Lead Parity Fields (Sprint 2.1.7.4, V10032)
    'GROSS',       -- kitchen_size (4-Sterne Hotel, 120 Zimmer)
    85,            -- employee_count (Hotel-Personal inkl. F&B)
    1,             -- branch_count (Single Location)
    FALSE,         -- is_chain (Einzelhotel, kein Ketten-Hotel)
    95000.00,      -- estimated_volume (= expected_annual_volume)
    -- End of new fields
    FALSE,
    FALSE,  -- is_test_data
    NOW() - INTERVAL '3 weeks',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '14 days',
    'DEV-SEED-SYSTEM'
),

-- ============================================================================
-- KD-DEV-003: FreshEvents Catering AG (AKTIV, Multi-Location)
-- Use Case: Multi-Location Testing, Cross-Border (DE+CH)
-- Characteristics: 2 locations (Berlin + Zürich), high volume, time pressure
-- ============================================================================
(
    'c0000000-0003-0000-0000-000000000001'::uuid,
    'KD-DEV-003',
    'FreshEvents Catering AG',
    NULL,
    'AG',
    'UNTERNEHMEN',
    NULL,
    'CATERING',
    'AKTIV',
    'GROWTH',
    'KEIN_PARTNER',
    420000.00,
    390000.00,
    'NETTO_14',
    50000.00,
    'EXPRESS',
    10,
    NOW() - INTERVAL '5 days',
    NOW() + INTERVAL '7 days',
    1,
    0,
    1,
    0,
    2,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    TRUE,  -- pain_time_pressure
    FALSE,
    TRUE,  -- pain_unreliable_delivery
    FALSE,
    'Event-Catering mit engen Zeitfenstern, Cross-Border Logistik CH kritisch',
    'PRIVATE',
    -- NEW: Lead Parity Fields (Sprint 2.1.7.4, V10032)
    'GROSS',       -- kitchen_size (Event-Catering, große Produktionsküche)
    65,            -- employee_count (Event-Catering-Personal)
    2,             -- branch_count (Berlin + Zürich)
    TRUE,          -- is_chain (Multi-Location Catering)
    420000.00,     -- estimated_volume (= expected_annual_volume)
    -- End of new fields
    FALSE,
    FALSE,  -- is_test_data
    NOW() - INTERVAL '2 years',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '5 days',
    'DEV-SEED-SYSTEM'
),

-- ============================================================================
-- KD-DEV-004: Betriebsgastronomie TechPark Frankfurt (PROSPECT, Edge Case)
-- Use Case: Edge Case Testing, Incomplete Data (NULL Volume)
-- Characteristics: New lead, minimal data, no pain points yet
-- ============================================================================
(
    'c0000000-0004-0000-0000-000000000001'::uuid,
    'KD-DEV-004',
    'Betriebsgastronomie TechPark Frankfurt GmbH',
    'Mensa TechPark',
    'GMBH',
    'UNTERNEHMEN',
    NULL,
    'KANTINE',
    'PROSPECT',
    'ACQUISITION',
    'KEIN_PARTNER',
    NULL,  -- Edge Case: NULL Volume
    NULL,
    'NETTO_30',
    NULL,
    'STANDARD',
    50,
    NOW() - INTERVAL '3 days',
    NOW() + INTERVAL '5 days',
    1,
    0,
    0,
    0,
    1,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    NULL,
    'PUBLIC',
    -- NEW: Lead Parity Fields (Sprint 2.1.7.4, V10032)
    'MITTEL',      -- kitchen_size (Betriebskantine, ca. 800 Mitarbeiter)
    12,            -- employee_count (Kantinen-Personal)
    1,             -- branch_count (Single Location)
    FALSE,         -- is_chain (Einzelkantine)
    NULL,          -- estimated_volume (Edge Case: NULL Volume)
    -- End of new fields
    FALSE,
    FALSE,  -- is_test_data
    NOW() - INTERVAL '1 week',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '3 days',
    'DEV-SEED-SYSTEM'
),

-- ============================================================================
-- KD-DEV-005: Rheinland Gastro Depot GmbH (ARCHIVIERT)
-- Use Case: Archived Customer, Old Data, Re-activation Testing
-- Characteristics: Inactive for 6 months, supplier quality issues
-- ============================================================================
(
    'c0000000-0005-0000-0000-000000000001'::uuid,
    'KD-DEV-005',
    'Rheinland Gastro Depot GmbH',
    'Rheinland Depot',
    'GMBH',
    'UNTERNEHMEN',
    NULL,
    'GROSSHANDEL',
    'ARCHIVIERT',
    'RECOVERY',
    'KEIN_PARTNER',
    120000.00,
    95000.00,
    'NETTO_30',
    15000.00,
    'STANDARD',
    75,
    NOW() - INTERVAL '6 months',
    NULL,
    2,
    0,
    0,
    0,
    2,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    TRUE,  -- pain_supplier_quality
    FALSE,
    FALSE,
    'Wechsel zu Wettbewerber aufgrund Lieferqualität, Re-Aktivierung möglich',
    'PRIVATE',
    -- NEW: Lead Parity Fields (Sprint 2.1.7.4, V10032)
    'KLEIN',       -- kitchen_size (Großhandel, kleinere Produktionsküche)
    18,            -- employee_count (Depot-Personal)
    2,             -- branch_count (2 Standorte)
    FALSE,         -- is_chain (Kleiner Großhandel, keine Kette)
    120000.00,     -- estimated_volume (= expected_annual_volume)
    -- End of new fields
    FALSE,
    FALSE,  -- is_test_data
    NOW() - INTERVAL '3 years',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '6 months',
    'DEV-SEED-SYSTEM'
)
ON CONFLICT (customer_number) DO NOTHING;


-- ============================================================================
-- 2. CUSTOMER_LOCATIONS (6)
-- ============================================================================

INSERT INTO customer_locations (
    id,
    customer_id,
    location_name,
    location_code,
    category,
    is_main_location,
    is_billing_location,
    is_shipping_location,
    operating_hours,
    notes,
    phone,
    email,
    is_active,
    is_deleted,
    is_test_data,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
-- KD-DEV-001: Restaurant Silbertanne (Main Location München)
(
    '10c00000-0001-0000-0000-000000000001'::uuid,
    'c0000000-0001-0000-0000-000000000001'::uuid,
    'Silbertanne Hauptrestaurant München',
    'MUC-001',
    'HEADQUARTERS',
    TRUE,
    TRUE,
    TRUE,
    'Mo-So: 11:00-23:00',
    'Hauptstandort mit eigener Küche und Verwaltung',
    '+49 89 12345678',
    'info@silbertanne.example',
    TRUE,
    FALSE,
    TRUE,
    NOW() - INTERVAL '18 months',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '7 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-002: Hotel Nordwind (Hamburg)
(
    '10c00000-0002-0000-0000-000000000001'::uuid,
    'c0000000-0002-0000-0000-000000000001'::uuid,
    'Hotel Nordwind Hamburg Alster',
    'HAM-001',
    'HEADQUARTERS',
    TRUE,
    TRUE,
    TRUE,
    'Rezeption 24/7, Frühstück: 06:30-10:30',
    '4-Sterne Superior Hotel, 120 Zimmer',
    '+49 40 9876543',
    'info@hotel-nordwind.example',
    TRUE,
    FALSE,
    TRUE,
    NOW() - INTERVAL '3 weeks',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '14 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-003: FreshEvents (Berlin Hauptsitz)
(
    '10c00000-0003-0000-0000-000000000001'::uuid,
    'c0000000-0003-0000-0000-000000000001'::uuid,
    'FreshEvents Berlin Zentrale',
    'BER-HQ',
    'HEADQUARTERS',
    TRUE,
    TRUE,
    TRUE,
    'Mo-Fr: 08:00-18:00',
    'Zentrale mit Produktionsküche und Verwaltung',
    '+49 30 55555555',
    'berlin@freshevents.example',
    TRUE,
    FALSE,
    TRUE,
    NOW() - INTERVAL '2 years',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '5 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-003: FreshEvents (Zürich Filiale)
(
    '10c00000-0004-0000-0000-000000000001'::uuid,
    'c0000000-0003-0000-0000-000000000001'::uuid,
    'FreshEvents Zürich',
    'ZRH-001',
    'BRANCH_OFFICE',
    FALSE,
    FALSE,
    TRUE,
    'Mo-Fr: 08:00-17:00',
    'Schweiz Filiale für Cross-Border Events',
    '+41 44 1234567',
    'zuerich@freshevents.example',
    TRUE,
    FALSE,
    TRUE,
    NOW() - INTERVAL '8 months',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '2 months',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-004: Mensa TechPark (Frankfurt)
(
    '10c00000-0005-0000-0000-000000000001'::uuid,
    'c0000000-0004-0000-0000-000000000001'::uuid,
    'Mensa TechPark Frankfurt',
    'FRA-TP',
    'HEADQUARTERS',
    TRUE,
    TRUE,
    TRUE,
    'Mo-Fr: 11:30-14:30',
    'Betriebskantine für ca. 800 Mitarbeiter',
    '+49 69 7654321',
    'kantine@techpark.example',
    TRUE,
    FALSE,
    TRUE,
    NOW() - INTERVAL '1 week',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '3 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-005: Rheinland Gastro (Köln)
(
    '10c00000-0006-0000-0000-000000000001'::uuid,
    'c0000000-0005-0000-0000-000000000001'::uuid,
    'Rheinland Gastro Depot Köln',
    'CGN-001',
    'HEADQUARTERS',
    TRUE,
    TRUE,
    TRUE,
    'Mo-Fr: 07:00-16:00',
    'Großhandel mit Lager und Cash&Carry',
    '+49 221 8765432',
    'info@rheinland-depot.example',
    TRUE,
    FALSE,
    TRUE,
    NOW() - INTERVAL '3 years',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '6 months',
    'DEV-SEED-SYSTEM'
)
ON CONFLICT (id) DO NOTHING;


-- ============================================================================
-- 3. CUSTOMER_ADDRESSES (6)
-- ============================================================================

INSERT INTO customer_addresses (
    id,
    location_id,
    address_type,
    street,
    street_number,
    postal_code,
    city,
    state_province,
    country,
    latitude,
    longitude,
    is_validated,
    is_primary_for_type,
    is_active,
    is_deleted,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
-- KD-DEV-001: Silbertanne München
(
    'ad000000-0001-0000-0000-000000000001'::uuid,
    '10c00000-0001-0000-0000-000000000001'::uuid,
    'MAIN',
    'Maximilianstraße',
    '999',
    '80539',
    'München',
    'Bayern',
    'DEU',
    NULL,
    NULL,
    FALSE,
    TRUE,
    TRUE,
    FALSE,
    NOW() - INTERVAL '18 months',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '7 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-002: Hotel Nordwind Hamburg
(
    'ad000000-0002-0000-0000-000000000001'::uuid,
    '10c00000-0002-0000-0000-000000000001'::uuid,
    'MAIN',
    'Elbchaussee',
    '888',
    '22605',
    'Hamburg',
    'Hamburg',
    'DEU',
    NULL,
    NULL,
    FALSE,
    TRUE,
    TRUE,
    FALSE,
    NOW() - INTERVAL '3 weeks',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '14 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-003: FreshEvents Berlin
(
    'ad000000-0003-0000-0000-000000000001'::uuid,
    '10c00000-0003-0000-0000-000000000001'::uuid,
    'MAIN',
    'Friedrichstraße',
    '777',
    '10117',
    'Berlin',
    'Berlin',
    'DEU',
    NULL,
    NULL,
    FALSE,
    TRUE,
    TRUE,
    FALSE,
    NOW() - INTERVAL '2 years',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '5 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-003: FreshEvents Zürich
(
    'ad000000-0004-0000-0000-000000000001'::uuid,
    '10c00000-0004-0000-0000-000000000001'::uuid,
    'SHIPPING',
    'Bahnhofstrasse',
    '666',
    '8001',
    'Zürich',
    'Zürich',
    'CHE',
    NULL,
    NULL,
    FALSE,
    TRUE,
    TRUE,
    FALSE,
    NOW() - INTERVAL '8 months',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '2 months',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-004: Mensa TechPark Frankfurt
(
    'ad000000-0005-0000-0000-000000000001'::uuid,
    '10c00000-0005-0000-0000-000000000001'::uuid,
    'MAIN',
    'TechPark-Allee',
    '555',
    '60486',
    'Frankfurt am Main',
    'Hessen',
    'DEU',
    NULL,
    NULL,
    FALSE,
    TRUE,
    TRUE,
    FALSE,
    NOW() - INTERVAL '1 week',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '3 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-005: Rheinland Gastro Köln
(
    'ad000000-0006-0000-0000-000000000001'::uuid,
    '10c00000-0006-0000-0000-000000000001'::uuid,
    'MAIN',
    'Deutz-Mülheimer-Straße',
    '444',
    '51063',
    'Köln',
    'Nordrhein-Westfalen',
    'DEU',
    NULL,
    NULL,
    FALSE,
    TRUE,
    TRUE,
    FALSE,
    NOW() - INTERVAL '3 years',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '6 months',
    'DEV-SEED-SYSTEM'
)
ON CONFLICT (id) DO NOTHING;


-- ============================================================================
-- 4. CUSTOMER_CONTACTS (8)
-- ============================================================================

INSERT INTO customer_contacts (
    id,
    customer_id,
    salutation,
    first_name,
    last_name,
    position,
    department,
    email,
    phone,
    mobile,
    preferred_communication_method,
    language_preference,
    is_primary,
    is_decision_maker,
    is_active,
    is_marketing_allowed,
    notes,
    assigned_location_id,
    responsibility_scope,
    is_deleted,
    is_test_data,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
-- KD-DEV-001: Thomas Müller (Geschäftsführer, Primary)
(
    'c0000000-0001-1000-0000-000000000001'::uuid,
    'c0000000-0001-0000-0000-000000000001'::uuid,
    'HERR',
    'Thomas',
    'Müller',
    'Geschäftsführer',
    'Geschäftsleitung',
    'mueller@silbertanne.example',
    '+49 89 12345678',
    '+49 170 1234567',
    'EMAIL',
    'de',
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    'Gründer und Hauptansprechpartner, fokussiert auf Expansion',
    '10c00000-0001-0000-0000-000000000001'::uuid,
    'all',
    FALSE,
    TRUE,
    NOW() - INTERVAL '18 months',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '7 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-002: Dr. Sandra Schmidt (Hoteldirektorin, Primary)
(
    'c0000000-0002-1000-0000-000000000001'::uuid,
    'c0000000-0002-0000-0000-000000000001'::uuid,
    'FRAU',
    'Sandra',
    'Schmidt',
    'Hoteldirektorin',
    'Geschäftsleitung',
    'schmidt@hotel-nordwind.example',
    '+49 40 9876543',
    '+49 171 7654321',
    'PHONE',
    'de',
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    'PhD Hospitality Management, entscheidet über Lieferanten',
    '10c00000-0002-0000-0000-000000000001'::uuid,
    'all',
    FALSE,
    TRUE,
    NOW() - INTERVAL '3 weeks',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '14 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-002: Michael Weber (Einkaufsleiter, Secondary)
(
    'c0000000-0003-1000-0000-000000000001'::uuid,
    'c0000000-0002-0000-0000-000000000001'::uuid,
    'HERR',
    'Michael',
    'Weber',
    'Einkaufsleiter',
    'Einkauf',
    'weber@hotel-nordwind.example',
    '+49 40 9876544',
    '+49 172 2345678',
    'EMAIL',
    'de',
    FALSE,
    FALSE,
    TRUE,
    TRUE,
    'Operativer Einkauf, berichtet an Frau Schmidt',
    '10c00000-0002-0000-0000-000000000001'::uuid,
    'all',
    FALSE,
    TRUE,
    NOW() - INTERVAL '3 weeks',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '14 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-003: Andreas Becker (Geschäftsführer Berlin, Primary)
(
    'c0000000-0004-1000-0000-000000000001'::uuid,
    'c0000000-0003-0000-0000-000000000001'::uuid,
    'HERR',
    'Andreas',
    'Becker',
    'Geschäftsführer',
    'Geschäftsleitung',
    'becker@freshevents.example',
    '+49 30 55555555',
    '+49 172 3456789',
    'EMAIL',
    'de',
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    'Hauptansprechpartner für strategische Themen, beide Standorte',
    '10c00000-0003-0000-0000-000000000001'::uuid,
    'all',
    FALSE,
    TRUE,
    NOW() - INTERVAL '2 years',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '5 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-003: Sabine Meier (Standortleiterin Zürich, Secondary)
(
    'c0000000-0005-1000-0000-000000000001'::uuid,
    'c0000000-0003-0000-0000-000000000001'::uuid,
    'FRAU',
    'Sabine',
    'Meier',
    'Standortleiterin',
    'Operations',
    'meier@freshevents.example',
    '+41 44 1234567',
    '+41 79 3456789',
    'EMAIL',
    'de',
    FALSE,
    FALSE,
    TRUE,
    TRUE,
    'Verantwortlich für Zürich Standort, Schweizer Lieferungen',
    '10c00000-0004-0000-0000-000000000001'::uuid,
    'location',
    FALSE,
    TRUE,
    NOW() - INTERVAL '8 months',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '2 months',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-004: Julia Hoffmann (Betriebsleiterin, Primary)
(
    'c0000000-0006-1000-0000-000000000001'::uuid,
    'c0000000-0004-0000-0000-000000000001'::uuid,
    'FRAU',
    'Julia',
    'Hoffmann',
    'Betriebsleiterin',
    'Verwaltung',
    'hoffmann@mensa-techpark.example',
    '+49 69 7654321',
    '+49 173 4567890',
    'EMAIL',
    'de',
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    'Verantwortlich für Kantinenbetrieb, neue Ansprechpartnerin',
    '10c00000-0005-0000-0000-000000000001'::uuid,
    'all',
    FALSE,
    TRUE,
    NOW() - INTERVAL '1 week',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '3 days',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-005: Klaus Petersen (Einkaufsleiter, Primary)
(
    'c0000000-0007-1000-0000-000000000001'::uuid,
    'c0000000-0005-0000-0000-000000000001'::uuid,
    'HERR',
    'Klaus',
    'Petersen',
    'Einkaufsleiter',
    'Einkauf',
    'petersen@rheinland-depot.example',
    '+49 221 8765432',
    '+49 173 2468135',
    'PHONE',
    'de',
    TRUE,
    TRUE,
    TRUE,
    FALSE,
    'Letzter Kontakt vor 6 Monaten, keine Reaktion auf Angebote',
    '10c00000-0006-0000-0000-000000000001'::uuid,
    'all',
    FALSE,
    TRUE,
    NOW() - INTERVAL '3 years',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '6 months',
    'DEV-SEED-SYSTEM'
),

-- KD-DEV-001: Anna Wagner (Einkauf, Secondary)
(
    'c0000000-0008-1000-0000-000000000001'::uuid,
    'c0000000-0001-0000-0000-000000000001'::uuid,
    'FRAU',
    'Anna',
    'Wagner',
    'Einkauf',
    'Einkauf',
    'wagner@silbertanne.example',
    '+49 89 12345679',
    '+49 174 5678901',
    'EMAIL',
    'de',
    FALSE,
    FALSE,
    TRUE,
    TRUE,
    'Operative Bestellungen, berichtet an Herrn Müller',
    '10c00000-0001-0000-0000-000000000001'::uuid,
    'all',
    FALSE,
    TRUE,
    NOW() - INTERVAL '14 months',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '7 days',
    'DEV-SEED-SYSTEM'
)
ON CONFLICT (id) DO NOTHING;


-- ============================================================================
-- UPDATE: Lead Parity Fields (Sprint 2.1.7.4, V10032)
-- ============================================================================
-- Purpose: Update existing SEED customers with new fields from V10032
-- Reason: ON CONFLICT DO NOTHING prevents INSERT from updating existing rows
-- ============================================================================

UPDATE customers SET
    kitchen_size = 'MITTEL',
    employee_count = 22,
    branch_count = 5,
    is_chain = TRUE,
    estimated_volume = 180000.00
WHERE customer_number = 'KD-DEV-001';

UPDATE customers SET
    kitchen_size = 'GROSS',
    employee_count = 85,
    branch_count = 1,
    is_chain = FALSE,
    estimated_volume = 95000.00
WHERE customer_number = 'KD-DEV-002';

UPDATE customers SET
    kitchen_size = 'GROSS',
    employee_count = 65,
    branch_count = 2,
    is_chain = TRUE,
    estimated_volume = 420000.00
WHERE customer_number = 'KD-DEV-003';

UPDATE customers SET
    kitchen_size = 'MITTEL',
    employee_count = 12,
    branch_count = 1,
    is_chain = FALSE,
    estimated_volume = NULL
WHERE customer_number = 'KD-DEV-004';

UPDATE customers SET
    kitchen_size = 'KLEIN',
    employee_count = 18,
    branch_count = 2,
    is_chain = FALSE,
    estimated_volume = 120000.00
WHERE customer_number = 'KD-DEV-005';

-- ============================================================================
-- END OF V90001
-- ============================================================================
-- Summary:
--   ✅ 5 Customers (AKTIV, PROSPECT, ARCHIVIERT scenarios)
--   ✅ 6 Locations (including multi-location KD-DEV-003)
--   ✅ 6 Addresses (realistic German addresses)
--   ✅ 8 Contacts (Primary + Secondary decision makers)
--   ✅ is_test_data = TRUE everywhere
--   ✅ Fixed UUIDs for consistency
--   ✅ ON CONFLICT DO NOTHING for idempotency
--   ✅ Realistic German B2B food industry scenarios
--   ✅ Lead Parity Fields updated (kitchen_size, employee_count, branch_count, is_chain, estimated_volume)
-- ============================================================================
