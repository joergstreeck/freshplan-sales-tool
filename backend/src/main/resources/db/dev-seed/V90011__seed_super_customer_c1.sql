-- =====================================================
-- V90011: DEV-SEED – Super-Customer C1 (Complete Scenario)
-- =====================================================
-- Created: 2025-10-25
-- Sprint: 2.1.7.2 Phase 2
-- Purpose: C1 Super-Customer mit ALLEN Daten (konvertiert von Lead L3)
-- =====================================================
-- Test Strategy: SEED-Daten (persistent, is_test_data = FALSE)
-- Customer Number: KD-DEV-123 (außerhalb V-Nummernkreis)
-- =====================================================

-- ====================
-- 1. CUSTOMER C1 (Super-Customer)
-- ====================
-- Gewissenhaft befüllt wie ein fleißiger Verkäufer!

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
    -- industry, -- DEPRECATED field, omit to use NULL

    -- Status & Lifecycle
    status,
    lifecycle_stage,
    partner_status,

    -- Lead Parity Fields (Sprint 2.1.6)
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

    -- Integration (Sprint 2.1.7.2)
    xentral_customer_id,

    -- Lead Conversion Tracking
    original_lead_id,

    -- Flags (WICHTIG: is_test_data = FALSE für SEED!)
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
    'GMBH', -- Separate legal_form field!
    'UNTERNEHMEN',
    'GROSSHANDEL', -- businessType (new field)
    -- industry omitted (DEPRECATED, auto-set to NULL)

    -- Status & Lifecycle
    'AKTIV',
    'RETENTION',
    'KEIN_PARTNER',

    -- Lead Parity Fields (realistisch für Großhandel)
    'GROSS', -- kitchen_size
    50, -- employee_count
    2, -- branch_count
    TRUE, -- is_chain
    250000.00, -- estimated_volume

    -- Financial Information (erfolgreicher Kunde)
    250000.00, -- expected_annual_volume
    235000.00, -- actual_annual_volume (etwas unter Erwartung)
    'NETTO_30',
    50000.00, -- credit_limit
    'STANDARD',

    -- Location Information (2 Standorte: München HQ + Frankfurt Branch)
    2, -- total_locations_eu
    2, -- locations_germany
    0, -- locations_austria
    0, -- locations_switzerland
    0, -- locations_rest_eu
    'YES', -- expansion_planned (CHECK constraint: 'YES', 'NO', 'UNKNOWN')

    -- Pain Points (realistisch für Großhandel)
    TRUE, -- pain_staff_shortage (Fachkräftemangel)
    TRUE, -- pain_high_costs (Energiekosten gestiegen)
    FALSE, -- pain_food_waste (gut organisiert)
    FALSE, -- pain_quality_inconsistency (hohe Standards)
    TRUE, -- pain_time_pressure (schnelle Lieferzeiten erforderlich)
    TRUE, -- pain_supplier_quality (sucht bessere Lieferanten)
    FALSE, -- pain_unreliable_delivery (derzeit stabil)
    FALSE, -- pain_poor_service (zufrieden)
    'Hauptproblem: Zuverlässige Bio-Lieferanten für Großhandel schwer zu finden. Interesse an Premium-Sortiment mit kurzen Lieferzeiten.',

    -- Business Details
    'PRIVATE', -- primary_financing (PRIVATE, PUBLIC, or MIXED)
    FALSE, -- is_seasonal_business (Großhandel ganzjährig)
    NULL, -- seasonal_pattern

    -- CRM Tracking
    10, -- risk_score (sehr guter Kunde)
    NOW() - INTERVAL '5 days', -- last_contact_date
    NOW() + INTERVAL '14 days', -- next_follow_up_date
    90, -- churn_threshold_days
    NOW() - INTERVAL '7 days', -- last_order_date

    -- Integration
    'XENTRAL-C1-SUPER', -- xentral_customer_id

    -- Lead Conversion (konvertiert von Lead L3 - VERHANDLUNG)
    90003, -- original_lead_id (Lead 90003 = "Kita Sonnenschein München")

    -- Flags (SEED-Daten!)
    FALSE, -- is_test_data (WICHTIG: SEED = persistent!)
    FALSE, -- is_deleted

    -- Audit
    NOW() - INTERVAL '45 days', -- created_at (vor 45 Tagen konvertiert)
    'DEV-SEED-SYSTEM',
    NOW() - INTERVAL '5 days', -- updated_at
    'DEV-SEED-SYSTEM'
);

-- ====================
-- 2. CUSTOMER LOCATIONS (2x: HQ + BRANCH)
-- ====================

-- Location 1: Hauptsitz München (HQ)
INSERT INTO customer_locations (
    id,
    customer_id,
    location_name,
    location_code,
    category,
    description,
    phone,
    email,
    is_main_location,
    is_active,
    is_billing_location,
    is_shipping_location,
    delivery_instructions,
    notes,
    is_deleted,
    created_at,
    created_by
) VALUES (
    'c1000000-0123-0001-0000-000000000001'::uuid,
    'c0000000-0123-0000-0000-000000000001'::uuid,
    'Großhandel Frische Küche - Zentrale München',
    'HQ-MUC',
    'HEADQUARTERS',
    'Hauptsitz mit Verwaltung und Lager',
    '+49 89 123456-0',
    'zentrale@frische-kueche-wholesale.de',
    TRUE, -- is_main_location
    TRUE,
    TRUE, -- is_billing_location
    TRUE, -- is_shipping_location
    'Lieferung über Laderampe, Zugang Hof links',
    'Öffnungszeiten Warenannahme: Mo-Fr 6:00-16:00',
    FALSE,
    NOW() - INTERVAL '45 days',
    'DEV-SEED-SYSTEM'
);

-- Location 2: Filiale Frankfurt (BRANCH)
INSERT INTO customer_locations (
    id,
    customer_id,
    location_name,
    location_code,
    category,
    description,
    phone,
    email,
    is_main_location,
    is_active,
    is_billing_location,
    is_shipping_location,
    delivery_instructions,
    notes,
    is_deleted,
    created_at,
    created_by
) VALUES (
    'c1000000-0123-0002-0000-000000000001'::uuid,
    'c0000000-0123-0000-0000-000000000001'::uuid,
    'Großhandel Frische Küche - Filiale Frankfurt',
    'BRANCH-FFM',
    'BRANCH',
    'Filiale Frankfurt mit regionalem Vertrieb',
    '+49 69 987654-0',
    'frankfurt@frische-kueche-wholesale.de',
    FALSE,
    TRUE,
    FALSE,
    TRUE, -- is_shipping_location
    'Anlieferung nur nach telefonischer Voranmeldung',
    'Regionallager für Hessen und Rheinland-Pfalz',
    FALSE,
    NOW() - INTERVAL '30 days',
    'DEV-SEED-SYSTEM'
);

-- ====================
-- 3. CUSTOMER ADDRESSES (2x: BILLING @ HQ + DELIVERY @ BRANCH)
-- ====================

-- Address 1: Rechnungsadresse bei HQ München
INSERT INTO customer_addresses (
    id,
    location_id,
    address_type,
    street,
    street_number,
    additional_line,
    postal_code,
    city,
    state_province,
    country,
    is_primary_for_type,
    is_active,
    is_validated,
    is_deleted,
    created_at,
    created_by
) VALUES (
    'c2000000-0123-0001-0001-000000000001'::uuid,
    'c1000000-0123-0001-0000-000000000001'::uuid, -- HQ München
    'BILLING',
    'Großmarktstraße',
    '42',
    'Halle 7',
    '80939',
    'München',
    'Bayern',
    'DEU',
    TRUE, -- is_primary_for_type
    TRUE,
    TRUE, -- is_validated
    FALSE,
    NOW() - INTERVAL '45 days',
    'DEV-SEED-SYSTEM'
);

-- Address 2: Lieferadresse bei BRANCH Frankfurt
INSERT INTO customer_addresses (
    id,
    location_id,
    address_type,
    street,
    street_number,
    additional_line,
    postal_code,
    city,
    state_province,
    country,
    is_primary_for_type,
    is_active,
    is_validated,
    is_deleted,
    created_at,
    created_by
) VALUES (
    'c2000000-0123-0002-0001-000000000001'::uuid,
    'c1000000-0123-0002-0000-000000000001'::uuid, -- BRANCH Frankfurt
    'DELIVERY',
    'Handelshofweg',
    '15',
    'Tor 3',
    '60308',
    'Frankfurt am Main',
    'Hessen',
    'DEU',
    TRUE,
    TRUE,
    TRUE,
    FALSE,
    NOW() - INTERVAL '30 days',
    'DEV-SEED-SYSTEM'
);

-- ====================
-- 4. CUSTOMER CONTACTS (2x: Primary + Secondary)
-- ====================

-- Contact 1: Einkaufsleiter (Primary, Decision Maker)
INSERT INTO customer_contacts (
    id,
    customer_id,
    salutation,
    title,
    first_name,
    last_name,
    position,
    department,
    decision_level,
    email,
    phone,
    mobile,
    is_primary,
    is_decision_maker,
    is_active,
    preferred_communication_method,
    language_preference,
    assigned_location_id,
    last_contact_date,
    interaction_count,
    warmth_score,
    personal_notes,
    is_deleted,
    created_at,
    created_by
) VALUES (
    'c3000000-0123-0001-0000-000000000001'::uuid,
    'c0000000-0123-0000-0000-000000000001'::uuid,
    'Herr',
    NULL,
    'Thomas',
    'Schneider',
    'Einkaufsleiter',
    'Einkauf & Beschaffung',
    'manager',
    'thomas.schneider@frische-kueche-wholesale.de',
    '+49 89 123456-20',
    '+49 171 9876543',
    TRUE, -- is_primary
    TRUE, -- is_decision_maker
    TRUE,
    'EMAIL',
    'DE',
    'c1000000-0123-0001-0000-000000000001'::uuid, -- HQ München
    NOW() - INTERVAL '5 days',
    12, -- interaction_count
    85, -- warmth_score (sehr warmer Kontakt)
    'Sehr professionell, schätzt Zuverlässigkeit und Bio-Qualität. Entscheidungsfreudig.',
    FALSE,
    NOW() - INTERVAL '45 days',
    'DEV-SEED-SYSTEM'
);

-- Contact 2: Vertriebsleiter Frankfurt (Secondary)
INSERT INTO customer_contacts (
    id,
    customer_id,
    salutation,
    first_name,
    last_name,
    position,
    department,
    decision_level,
    email,
    phone,
    mobile,
    is_primary,
    is_decision_maker,
    is_active,
    preferred_communication_method,
    language_preference,
    assigned_location_id,
    last_contact_date,
    interaction_count,
    warmth_score,
    personal_notes,
    is_deleted,
    created_at,
    created_by
) VALUES (
    'c3000000-0123-0002-0000-000000000001'::uuid,
    'c0000000-0123-0000-0000-000000000001'::uuid,
    'Frau',
    'Sandra',
    'Weber',
    'Vertriebsleiterin Filiale',
    'Vertrieb',
    'operational',
    'sandra.weber@frische-kueche-wholesale.de',
    '+49 69 987654-30',
    '+49 160 1234567',
    FALSE,
    FALSE,
    TRUE,
    'PHONE',
    'DE',
    'c1000000-0123-0002-0000-000000000001'::uuid, -- BRANCH Frankfurt
    NOW() - INTERVAL '8 days',
    7,
    70, -- warmth_score
    'Pragmatisch, kümmert sich um operative Abwicklung Frankfurt.',
    FALSE,
    NOW() - INTERVAL '30 days',
    'DEV-SEED-SYSTEM'
);

-- ====================
-- 5. OPPORTUNITY (1x: QUALIFICATION Stage)
-- ====================

INSERT INTO opportunities (
    id,
    name,
    description,
    stage,
    expected_value,
    expected_close_date,
    probability,
    assigned_to,
    lead_id,
    customer_id,
    opportunity_type,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'Sortimentserweiterung: Großhandel Frische Küche - Premium Bio-Linie',
    'Interesse an Premium-Bio-Sortiment für Weitervertrieb an Gastronomie. Qualifizierung läuft.',
    'QUALIFICATION',
    35000.00,
    CURRENT_DATE + INTERVAL '60 days',
    30, -- probability (frühe Qualifizierungsphase)
    NULL, -- assigned_to (User-Management not in DEV-SEED)
    NULL, -- lead_id (Customer-Opportunity, kein Lead)
    'c0000000-0123-0000-0000-000000000001'::uuid,
    'SORTIMENTSERWEITERUNG',
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '2 days'
);

-- ====================
-- 6. ACTIVITIES (5x: Lead-Phase + Customer-Phase)
-- ====================
-- Sprint 2.1.7.2 D8: Unified Activity System
-- entity_type = 'CUSTOMER', entity_id = Customer UUID (as TEXT)

-- Activity 1: Lead-Phase - FIRST_CONTACT_DOCUMENTED (als Lead L3)
INSERT INTO activities (
    id,
    entity_type,
    entity_id,
    activity_type,
    activity_date,
    description,
    summary,
    outcome,
    is_meaningful_contact,
    resets_timer,
    counts_as_progress,
    user_id,
    performed_by,
    created_at
) VALUES (
    gen_random_uuid(),
    'CUSTOMER',
    'c0000000-0123-0000-0000-000000000001', -- Customer UUID as TEXT
    'FIRST_CONTACT_DOCUMENTED',
    NOW() - INTERVAL '50 days',
    'Erstkontakt auf Großhandelsmesse München. Interesse an Bio-Sortiment für Gastronomie-Kunden.',
    'Messekontakt - Interesse Bio-Großhandel',
    'QUALIFIED',
    TRUE, -- is_meaningful_contact
    TRUE, -- resets_timer
    TRUE, -- counts_as_progress
    'DEV-SEED-USER',
    'DEV-SEED-USER',
    NOW() - INTERVAL '50 days'
);

-- Activity 2: Lead-Phase - QUALIFIED_CALL
INSERT INTO activities (
    id,
    entity_type,
    entity_id,
    activity_type,
    activity_date,
    description,
    summary,
    outcome,
    next_action,
    next_action_date,
    is_meaningful_contact,
    resets_timer,
    counts_as_progress,
    user_id,
    created_at
) VALUES (
    gen_random_uuid(),
    'CUSTOMER',
    'c0000000-0123-0000-0000-000000000001',
    'QUALIFIED_CALL',
    NOW() - INTERVAL '48 days',
    'Telefonat mit Einkaufsleiter Schneider. Budget 200k€/Jahr für Bio-Frischware bestätigt. 2 Standorte München + Frankfurt.',
    'Qualifizierung: Budget 200k€, 2 Standorte',
    'SUCCESSFUL',
    'Angebot vorbereiten für Bio-Gemüse und Obst',
    (NOW() - INTERVAL '48 days')::date + INTERVAL '7 days',
    TRUE,
    TRUE,
    TRUE,
    'DEV-SEED-USER',
    NOW() - INTERVAL '48 days'
);

-- Activity 3: Lead-Phase - MEETING (Vor-Ort-Termin)
INSERT INTO activities (
    id,
    entity_type,
    entity_id,
    activity_type,
    activity_date,
    description,
    summary,
    outcome,
    next_action,
    is_meaningful_contact,
    resets_timer,
    counts_as_progress,
    user_id,
    created_at
) VALUES (
    gen_random_uuid(),
    'CUSTOMER',
    'c0000000-0123-0000-0000-000000000001',
    'MEETING',
    NOW() - INTERVAL '46 days',
    'Vor-Ort-Termin Großmarkt München. Besichtigung Lager, Kühlkette geprüft. Qualitätsstandards hoch. Entscheidung für Testphase getroffen.',
    'Meeting München - Testphase vereinbart',
    'SUCCESSFUL',
    'Testlieferung organisieren',
    TRUE,
    TRUE,
    TRUE,
    'DEV-SEED-USER',
    NOW() - INTERVAL '46 days'
);

-- Activity 4: Customer-Phase - ORDER (Conversion zu Customer!)
INSERT INTO activities (
    id,
    entity_type,
    entity_id,
    activity_type,
    activity_date,
    description,
    summary,
    outcome,
    is_meaningful_contact,
    resets_timer,
    counts_as_progress,
    user_id,
    created_at
) VALUES (
    gen_random_uuid(),
    'CUSTOMER',
    'c0000000-0123-0000-0000-000000000001',
    'ORDER',
    NOW() - INTERVAL '45 days',
    'Erste Bestellung eingegangen! Testphase erfolgreich. Rahmenvertrag unterzeichnet. Regelmäßige Lieferungen München + Frankfurt geplant.',
    'Lead → Kunde! Rahmenvertrag 250k€/Jahr',
    'SUCCESSFUL',
    FALSE,
    TRUE,
    TRUE,
    'DEV-SEED-USER',
    NOW() - INTERVAL '45 days'
);

-- Activity 5: Customer-Phase - FOLLOW_UP (aktuelles Follow-Up)
INSERT INTO activities (
    id,
    entity_type,
    entity_id,
    activity_type,
    activity_date,
    description,
    summary,
    outcome,
    next_action,
    next_action_date,
    is_meaningful_contact,
    resets_timer,
    counts_as_progress,
    user_id,
    created_at
) VALUES (
    gen_random_uuid(),
    'CUSTOMER',
    'c0000000-0123-0000-0000-000000000001',
    'FOLLOW_UP',
    NOW() - INTERVAL '5 days',
    'Check-In Telefonat. Zufriedenheit sehr hoch. Interesse an Premium-Bio-Linie für Spitzengastronomie. Neue Opportunity identifiziert.',
    'Follow-Up - Interesse Premium-Sortiment',
    'SUCCESSFUL',
    'Angebot Premium-Bio-Linie erstellen',
    CURRENT_DATE + INTERVAL '14 days',
    TRUE,
    TRUE,
    FALSE, -- Follow-up zählt nicht als progress
    'DEV-SEED-USER',
    NOW() - INTERVAL '5 days'
);

-- ====================
-- 7. SUCCESS MESSAGE
-- ====================
DO $$
BEGIN
  RAISE NOTICE '✅ V90011 DEV-SEED Super-Customer C1 Complete!';
  RAISE NOTICE '   Customer: KD-DEV-123 (Großhandel Frische Küche GmbH)';
  RAISE NOTICE '   - 2 Locations (HQ München + BRANCH Frankfurt)';
  RAISE NOTICE '   - 2 Addresses (BILLING @ HQ + DELIVERY @ BRANCH)';
  RAISE NOTICE '   - 2 Contacts (Einkaufsleiter + Vertriebsleiterin)';
  RAISE NOTICE '   - 1 Opportunity (QUALIFICATION: Premium-Bio-Linie 35k€)';
  RAISE NOTICE '   - 5 Activities (Lead-Phase: 3 + Customer-Phase: 2)';
  RAISE NOTICE '   - Xentral Integration: XENTRAL-C1-SUPER';
  RAISE NOTICE '   - Konvertiert von Lead L3 (ID 90003)';
  RAISE NOTICE '';
  RAISE NOTICE '📋 ALLE Felder befüllt wie gewissenhafter Verkäufer!';
  RAISE NOTICE '   - legal_form SEPARAT (nicht im company_name)';
  RAISE NOTICE '   - is_test_data = FALSE (SEED, persistent!)';
  RAISE NOTICE '   - customer_number = KD-DEV-123 (außerhalb V-Range)';
END $$;
