-- =====================================================
-- V90012: Cleanup & Recreate Super-Customer C1
-- =====================================================
-- Reason: V90011 hatte 'industry' Feld mit ungültigem Wert
-- Fix: industry=NULL (DEPRECATED field)
-- =====================================================

-- Cleanup fehlerhafter Customer KD-DEV-123 (falls vorhanden)
DELETE FROM activities WHERE entity_id = 'c0000000-0123-0000-0000-000000000001';
DELETE FROM opportunities WHERE customer_id = 'c0000000-0123-0000-0000-000000000001'::uuid;
DELETE FROM customer_contacts WHERE customer_id = 'c0000000-0123-0000-0000-000000000001'::uuid;
DELETE FROM customer_addresses WHERE location_id IN (
  SELECT id FROM customer_locations WHERE customer_id = 'c0000000-0123-0000-0000-000000000001'::uuid
);
DELETE FROM customer_locations WHERE customer_id = 'c0000000-0123-0000-0000-000000000001'::uuid;
DELETE FROM customers WHERE id = 'c0000000-0123-0000-0000-000000000001'::uuid;

-- Jetzt: Volle V90011 Migration nochmal, ABER mit industry=NULL!
-- (Code von V90011 kopiert, aber industry Feld removed)

-- ====================
-- 1. CUSTOMER C1 (Super-Customer)
-- ====================

INSERT INTO customers (
    -- Primary Keys & Identifiers
    id,
    customer_number,

    -- Company Information (legal_form SEPARAT!)
    company_name,
    trading_name,
    legal_form,
    customer_type,
    business_type,
    -- industry omitted (DEPRECATED, auto NULL)

    -- Status & Lifecycle
    status,
    lifecycle_stage,
    partner_status,

    -- Lead Parity Fields
    kitchen_size,
    employee_count,
    branch_count,
    is_chain,
    estimated_volume,

    -- Financial Information
    expected_annual_volume,
    actual_annual_volume,
    payment_terms,
    credit_limit,
    delivery_condition,

    -- Location Information
    total_locations_eu,
    locations_germany,
    locations_austria,
    locations_switzerland,
    locations_rest_eu,
    expansion_planned,

    -- Pain Points (8 Boolean Flags)
    pain_staff_shortage,
    pain_high_costs,
    pain_food_waste,
    pain_quality_inconsistency,
    pain_time_pressure,
    pain_supplier_quality,
    pain_unreliable_delivery,
    pain_poor_service,
    pain_notes,

    -- Business Details
    primary_financing,
    is_seasonal_business,
    seasonal_pattern,

    -- CRM Tracking
    risk_score,
    last_contact_date,
    next_follow_up_date,
    churn_threshold_days,
    last_order_date,

    -- Integration
    xentral_customer_id,

    -- Lead Conversion Tracking
    original_lead_id,

    -- Flags
    is_test_data,
    is_deleted,

    -- Audit Fields
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    -- Primary Keys
    'c0000000-0123-0000-0000-000000000001'::uuid,
    'KD-DEV-123',

    -- Company Information (OHNE Rechtsform im Namen!)
    'Großhandel Frische Küche',
    'Fresh Kitchen Wholesale',
    'GMBH',
    'UNTERNEHMEN',
    'GROSSHANDEL', -- businessType (valid)
    -- industry omitted → NULL (DEPRECATED field)

    -- Status & Lifecycle
    'AKTIV',
    'RETENTION',
    'KEIN_PARTNER',

    -- Lead Parity Fields
    'GROSS',
    50,
    2,
    TRUE,
    250000.00,

    -- Financial Information
    250000.00,
    235000.00,
    'NETTO_30',
    50000.00,
    'STANDARD',

    -- Location Information
    2,
    2,
    0,
    0,
    0,
    'JA', -- expansion_planned (ExpansionPlan Enum: JA, NEIN, GEPLANT, UNKLAR)

    -- Pain Points
    TRUE,
    TRUE,
    FALSE,
    FALSE,
    TRUE,
    TRUE,
    FALSE,
    FALSE,
    'Hauptproblem: Zuverlässige Bio-Lieferanten für Großhandel schwer zu finden. Interesse an Premium-Sortiment mit kurzen Lieferzeiten.',

    -- Business Details
    'PRIVATE',
    FALSE,
    NULL,

    -- CRM Tracking
    10,
    NOW() - INTERVAL '5 days',
    NOW() + INTERVAL '14 days',
    90,
    NOW() - INTERVAL '7 days',

    -- Integration
    'XENTRAL-C1-SUPER',

    -- Lead Conversion
    90003,

    -- Flags (SEED = persistent!)
    FALSE,
    FALSE,

    -- Audit
    NOW() - INTERVAL '45 days',
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '5 days',
    'DEV-SEED-SYSTEM'
);

-- ====================
-- 2. CUSTOMER LOCATIONS (2x)
-- ====================

INSERT INTO customer_locations (
    id, customer_id, location_name, location_code, category, description,
    phone, email, is_main_location, is_active, is_billing_location, is_shipping_location,
    delivery_instructions, notes, is_deleted, created_at, created_by
) VALUES
    ('c1000000-0123-0001-0000-000000000001'::uuid, 'c0000000-0123-0000-0000-000000000001'::uuid,
     'Großhandel Frische Küche - Zentrale München', 'HQ-MUC', 'HEADQUARTERS',
     'Hauptsitz mit Verwaltung und Lager', '+49 89 123456-0', 'zentrale@frische-kueche-wholesale.de',
     TRUE, TRUE, TRUE, TRUE, 'Lieferung über Laderampe, Zugang Hof links',
     'Öffnungszeiten Warenannahme: Mo-Fr 6:00-16:00', FALSE, NOW() - INTERVAL '45 days', 'DEV-SEED-SYSTEM'),
    ('c1000000-0123-0002-0000-000000000001'::uuid, 'c0000000-0123-0000-0000-000000000001'::uuid,
     'Großhandel Frische Küche - Filiale Frankfurt', 'BRANCH-FFM', 'BRANCH_OFFICE',
     'Filiale Frankfurt mit regionalem Vertrieb', '+49 69 987654-0', 'frankfurt@frische-kueche-wholesale.de',
     FALSE, TRUE, FALSE, TRUE, 'Anlieferung nur nach telefonischer Voranmeldung',
     'Regionallager für Hessen und Rheinland-Pfalz', FALSE, NOW() - INTERVAL '30 days', 'DEV-SEED-SYSTEM');

-- ====================
-- 3. CUSTOMER ADDRESSES (2x)
-- ====================

INSERT INTO customer_addresses (
    id, location_id, address_type, street, street_number, additional_line,
    postal_code, city, state_province, country, is_primary_for_type, is_active,
    is_validated, is_deleted, created_at, created_by
) VALUES
    ('c2000000-0123-0001-0001-000000000001'::uuid, 'c1000000-0123-0001-0000-000000000001'::uuid,
     'BILLING', 'Großmarktstraße', '42', 'Halle 7', '80939', 'München', 'Bayern', 'DEU',
     TRUE, TRUE, TRUE, FALSE, NOW() - INTERVAL '45 days', 'DEV-SEED-SYSTEM'),
    ('c2000000-0123-0002-0001-000000000001'::uuid, 'c1000000-0123-0002-0000-000000000001'::uuid,
     'DELIVERY', 'Handelshofweg', '15', 'Tor 3', '60308', 'Frankfurt am Main', 'Hessen', 'DEU',
     TRUE, TRUE, TRUE, FALSE, NOW() - INTERVAL '30 days', 'DEV-SEED-SYSTEM');

-- ====================
-- 4. CUSTOMER CONTACTS (2x)
-- ====================

INSERT INTO customer_contacts (
    id, customer_id, salutation, first_name, last_name, position, department, decision_level,
    email, phone, mobile, is_primary, is_decision_maker, is_active,
    preferred_communication_method, language_preference, assigned_location_id,
    last_contact_date, interaction_count, warmth_score, personal_notes,
    is_deleted, created_at, created_by
) VALUES
    ('c3000000-0123-0001-0000-000000000001'::uuid, 'c0000000-0123-0000-0000-000000000001'::uuid,
     'HERR', 'Thomas', 'Schneider', 'Einkaufsleiter', 'Einkauf & Beschaffung', 'manager',
     'thomas.schneider@frische-kueche-wholesale.de', '+49 89 123456-20', '+49 171 9876543',
     TRUE, TRUE, TRUE, 'EMAIL', 'DE', 'c1000000-0123-0001-0000-000000000001'::uuid,
     NOW() - INTERVAL '5 days', 12, 85, 'Sehr professionell, schätzt Zuverlässigkeit und Bio-Qualität. Entscheidungsfreudig.',
     FALSE, NOW() - INTERVAL '45 days', 'DEV-SEED-SYSTEM'),
    ('c3000000-0123-0002-0000-000000000001'::uuid, 'c0000000-0123-0000-0000-000000000001'::uuid,
     'FRAU', 'Sandra', 'Weber', 'Vertriebsleiterin Filiale', 'Vertrieb', 'operational',
     'sandra.weber@frische-kueche-wholesale.de', '+49 69 987654-30', '+49 160 1234567',
     FALSE, FALSE, TRUE, 'PHONE', 'DE', 'c1000000-0123-0002-0000-000000000001'::uuid,
     NOW() - INTERVAL '8 days', 7, 70, 'Pragmatisch, kümmert sich um operative Abwicklung Frankfurt.',
     FALSE, NOW() - INTERVAL '30 days', 'DEV-SEED-SYSTEM');

-- ====================
-- 5. OPPORTUNITY (1x)
-- ====================

INSERT INTO opportunities (
    id, name, description, stage, expected_value, expected_close_date, probability,
    assigned_to, lead_id, customer_id, opportunity_type, created_at, updated_at
) VALUES (
    gen_random_uuid(),
    'Sortimentserweiterung: Großhandel Frische Küche - Premium Bio-Linie',
    'Interesse an Premium-Bio-Sortiment für Weitervertrieb an Gastronomie. Qualifizierung läuft.',
    'QUALIFICATION', 35000.00, CURRENT_DATE + INTERVAL '60 days', 30,
    NULL, NULL, 'c0000000-0123-0000-0000-000000000001'::uuid,
    'SORTIMENTSERWEITERUNG', NOW() - INTERVAL '10 days', NOW() - INTERVAL '2 days'
);

-- ====================
-- 6. ACTIVITIES (5x)
-- ====================

INSERT INTO activities (
    id, entity_type, entity_id, activity_type, activity_date, description, summary, outcome,
    is_meaningful_contact, resets_timer, counts_as_progress, user_id, performed_by, created_at
) VALUES
    (gen_random_uuid(), 'CUSTOMER', 'c0000000-0123-0000-0000-000000000001', 'FIRST_CONTACT_DOCUMENTED',
     NOW() - INTERVAL '50 days', 'Erstkontakt auf Großhandelsmesse München. Interesse an Bio-Sortiment für Gastronomie-Kunden.',
     'Messekontakt - Interesse Bio-Großhandel', 'QUALIFIED', TRUE, TRUE, TRUE,
     'DEV-SEED-USER', 'DEV-SEED-USER', NOW() - INTERVAL '50 days'),
    (gen_random_uuid(), 'CUSTOMER', 'c0000000-0123-0000-0000-000000000001', 'QUALIFIED_CALL',
     NOW() - INTERVAL '48 days', 'Telefonat mit Einkaufsleiter Schneider. Budget 200k€/Jahr für Bio-Frischware bestätigt. 2 Standorte München + Frankfurt.',
     'Qualifizierung: Budget 200k€, 2 Standorte', 'SUCCESSFUL', TRUE, TRUE, TRUE,
     'DEV-SEED-USER', 'DEV-SEED-USER', NOW() - INTERVAL '48 days'),
    (gen_random_uuid(), 'CUSTOMER', 'c0000000-0123-0000-0000-000000000001', 'MEETING',
     NOW() - INTERVAL '46 days', 'Vor-Ort-Termin Großmarkt München. Besichtigung Lager, Kühlkette geprüft. Qualitätsstandards hoch. Entscheidung für Testphase getroffen.',
     'Meeting München - Testphase vereinbart', 'SUCCESSFUL', TRUE, TRUE, TRUE,
     'DEV-SEED-USER', 'DEV-SEED-USER', NOW() - INTERVAL '46 days'),
    (gen_random_uuid(), 'CUSTOMER', 'c0000000-0123-0000-0000-000000000001', 'ORDER',
     NOW() - INTERVAL '45 days', 'Erste Bestellung eingegangen! Testphase erfolgreich. Rahmenvertrag unterzeichnet. Regelmäßige Lieferungen München + Frankfurt geplant.',
     'Lead → Kunde! Rahmenvertrag 250k€/Jahr', 'SUCCESSFUL', FALSE, TRUE, TRUE,
     'DEV-SEED-USER', 'DEV-SEED-USER', NOW() - INTERVAL '45 days'),
    (gen_random_uuid(), 'CUSTOMER', 'c0000000-0123-0000-0000-000000000001', 'FOLLOW_UP',
     NOW() - INTERVAL '5 days', 'Check-In Telefonat. Zufriedenheit sehr hoch. Interesse an Premium-Bio-Linie für Spitzengastronomie. Neue Opportunity identifiziert.',
     'Follow-Up - Interesse Premium-Sortiment', 'SUCCESSFUL', TRUE, TRUE, FALSE,
     'DEV-SEED-USER', 'DEV-SEED-USER', NOW() - INTERVAL '5 days');

-- ====================
-- SUCCESS MESSAGE
-- ====================
DO $$
BEGIN
  RAISE NOTICE '✅ V90012 DEV-SEED Super-Customer C1 Complete (FIXED: industry=NULL)!';
  RAISE NOTICE '   Customer: KD-DEV-123 (Großhandel Frische Küche GmbH)';
  RAISE NOTICE '   FIX: industry field set to NULL (DEPRECATED)';
  RAISE NOTICE '   - 2 Locations, 2 Addresses, 2 Contacts, 1 Opportunity, 5 Activities';
END $$;
