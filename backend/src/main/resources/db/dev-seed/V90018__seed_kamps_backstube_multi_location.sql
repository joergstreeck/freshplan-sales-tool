-- ============================================================================
-- Migration V90018: Seed Kamps Backstube Multi-Location (COMPLETE WITH DEPTH!)
-- Sprint 2.1.7.7 - Multi-Location Management
-- ============================================================================
-- PURPOSE:
--   Provides realistic DEEP SEED data for Kamps Backstube (Bakery Chain) with:
--   - 1 HEADQUARTER (Schwalmtal) with full company details
--   - 4 FILIALE customers (Düsseldorf, Köln, Essen, Dortmund - NRW region)
--   - 2-3 Contacts per location (Filialleiter, Einkauf)
--   - Timeline Events (Customer Creation, Notes)
--   - Pain Points (Personalmangel, frühe Lieferzeiten 4:00 Uhr!)
--   - Same xentral_customer_id (92156) for Fuzzy-Matching testing
--
-- BUSINESS CONTEXT:
--   Kamps ist eine Bäckerei-Kette mit frühen Lieferzeiten (Bäckerei startet 4:00 Uhr!)
--   Hauptproblem: Frische-Produkte müssen pünktlich um 3:30 Uhr geliefert werden
-- ============================================================================

-- ============================================================================
-- 1️⃣ KAMPS BACKSTUBE GMBH - HEADQUARTER (Schwalmtal)
-- ============================================================================

INSERT INTO customers (
    id, customer_number,
    company_name, trading_name, legal_form, customer_type, business_type,
    hierarchy_type, parent_customer_id,
    status, lifecycle_stage, partner_status,
    kitchen_size, employee_count, branch_count, is_chain, estimated_volume,
    expected_annual_volume, actual_annual_volume,
    payment_terms, credit_limit, delivery_condition,
    street, postal_code, city, country_code,
    locations_de, locations_ch, locations_at,
    pain_staff_shortage, pain_high_costs, pain_food_waste, pain_quality_inconsistency,
    pain_time_pressure, pain_supplier_quality, pain_unreliable_delivery, pain_poor_service,
    pain_notes,
    primary_financing, is_seasonal_business, seasonal_pattern,
    risk_score, last_contact_date, next_follow_up_date, churn_threshold_days, last_order_date,
    xentral_customer_id,
    is_test_data, is_deleted,
    created_at, created_by, updated_at, updated_by
) VALUES (
    '33333333-3333-3333-3333-000000000001'::uuid, 'C-ML-KAMPS-HQ',
    'Kamps Backstube GmbH', 'Kamps', 'GMBH', 'UNTERNEHMEN', 'BAECKEREI',
    'HEADQUARTER', NULL,
    'AKTIV', 'GROWTH', 'KEIN_PARTNER',
    'GROSS', 180, 5, TRUE, 2800000.00,
    2800000.00, 2650000.00,
    'NETTO_30', 80000.00, 'EXPRESS',
    'Viersener Straße 25', '41366', 'Schwalmtal', 'DE',
    5, 0, 0,
    TRUE, TRUE, TRUE, FALSE, TRUE, TRUE, TRUE, FALSE,
    'Hauptproblem: Frühe Lieferzeiten (3:30 Uhr!) für Frischprodukte. Bäckerei-Produktion startet 4:00 Uhr. Personalmangel bei Bäckern. Sucht verlässliche Nacht-Lieferanten mit Frische-Garantie. Interesse an Bio-Backzutaten.',
    'PRIVATE', FALSE, NULL,
    22, CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE + INTERVAL '7 days', 90, CURRENT_DATE - INTERVAL '1 day',
    '92156',
    FALSE, FALSE,
    NOW() - INTERVAL '220 days', 'SYSTEM_SEED', NOW() - INTERVAL '3 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 2️⃣ KAMPS DÜSSELDORF HAUPTBAHNHOF - FILIALE
-- ============================================================================

INSERT INTO customers (
    id, customer_number, company_name, trading_name, legal_form, customer_type, business_type,
    hierarchy_type, parent_customer_id,
    status, lifecycle_stage, partner_status,
    kitchen_size, employee_count, branch_count, is_chain, estimated_volume,
    expected_annual_volume, actual_annual_volume,
    payment_terms, credit_limit, delivery_condition,
    street, postal_code, city, country_code,
    locations_de, locations_ch, locations_at,
    pain_staff_shortage, pain_high_costs, pain_food_waste, pain_quality_inconsistency,
    pain_time_pressure, pain_supplier_quality, pain_unreliable_delivery, pain_poor_service,
    pain_notes,
    primary_financing, is_seasonal_business, seasonal_pattern,
    risk_score, last_contact_date, next_follow_up_date, churn_threshold_days, last_order_date,
    xentral_customer_id,
    is_test_data, is_deleted,
    created_at, created_by, updated_at, updated_by
) VALUES (
    '33333333-3333-3333-3333-000000000002'::uuid, 'C-ML-KAMPS-DUS',
    'Kamps Düsseldorf Hauptbahnhof', 'Kamps DUS HBF', 'GMBH', 'UNTERNEHMEN', 'BAECKEREI',
    'FILIALE', '33333333-3333-3333-3333-000000000001'::uuid,
    'AKTIV', 'GROWTH', 'KEIN_PARTNER',
    'GROSS', 38, 1, FALSE, 720000.00,
    720000.00, 680000.00,
    'NETTO_30', 80000.00, 'EXPRESS',
    'Konrad-Adenauer-Platz 14', '40210', 'Düsseldorf', 'DE',
    1, 0, 0,
    TRUE, TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE,
    'HBF-Standort: Hohe Laufkundschaft Mo-Fr 6:00-20:00. Frühschicht startet 4:00 Uhr → Lieferung bis 3:30 Uhr nötig! Problem: Verkehrs-Stau in Düsseldorf morgens. Interesse an vorgebackenen Bio-Brötchen.',
    'PRIVATE', FALSE, NULL,
    18, CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE + INTERVAL '7 days', 90, CURRENT_DATE - INTERVAL '1 day',
    '92156',
    FALSE, FALSE,
    NOW() - INTERVAL '190 days', 'SYSTEM_SEED', NOW() - INTERVAL '2 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 3️⃣ KAMPS KÖLN NEUMARKT - FILIALE
-- ============================================================================

INSERT INTO customers (
    id, customer_number, company_name, trading_name, legal_form, customer_type, business_type,
    hierarchy_type, parent_customer_id,
    status, lifecycle_stage, partner_status,
    kitchen_size, employee_count, branch_count, is_chain, estimated_volume,
    expected_annual_volume, actual_annual_volume,
    payment_terms, credit_limit, delivery_condition,
    street, postal_code, city, country_code,
    locations_de, locations_ch, locations_at,
    pain_staff_shortage, pain_high_costs, pain_food_waste, pain_quality_inconsistency,
    pain_time_pressure, pain_supplier_quality, pain_unreliable_delivery, pain_poor_service,
    pain_notes,
    primary_financing, is_seasonal_business, seasonal_pattern,
    risk_score, last_contact_date, next_follow_up_date, churn_threshold_days, last_order_date,
    xentral_customer_id,
    is_test_data, is_deleted,
    created_at, created_by, updated_at, updated_by
) VALUES (
    '33333333-3333-3333-3333-000000000003'::uuid, 'C-ML-KAMPS-KOELN',
    'Kamps Köln Neumarkt', 'Kamps Köln', 'GMBH', 'UNTERNEHMEN', 'BAECKEREI',
    'FILIALE', '33333333-3333-3333-3333-000000000001'::uuid,
    'AKTIV', 'GROWTH', 'KEIN_PARTNER',
    'GROSS', 35, 1, FALSE, 650000.00,
    650000.00, 620000.00,
    'NETTO_30', 80000.00, 'EXPRESS',
    'Neumarkt 5', '50667', 'Köln', 'DE',
    1, 0, 0,
    FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE,
    'Köln-Standort: Innenstadt mit Touristenverkehr. Karneval = Peak-Season! Lieferung bis 3:30 Uhr. Personal gut besetzt. Problem: Saisonale Schwankungen (Karneval vs. Sommer).',
    'PRIVATE', TRUE, 'EVENT_BASED',
    20, CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE + INTERVAL '10 days', 90, CURRENT_DATE - INTERVAL '2 days',
    '92156',
    FALSE, FALSE,
    NOW() - INTERVAL '180 days', 'SYSTEM_SEED', NOW() - INTERVAL '3 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 4️⃣ KAMPS ESSEN LIMBECKER PLATZ - FILIALE
-- ============================================================================

INSERT INTO customers (
    id, customer_number, company_name, trading_name, legal_form, customer_type, business_type,
    hierarchy_type, parent_customer_id,
    status, lifecycle_stage, partner_status,
    kitchen_size, employee_count, branch_count, is_chain, estimated_volume,
    expected_annual_volume, actual_annual_volume,
    payment_terms, credit_limit, delivery_condition,
    street, postal_code, city, country_code,
    locations_de, locations_ch, locations_at,
    pain_staff_shortage, pain_high_costs, pain_food_waste, pain_quality_inconsistency,
    pain_time_pressure, pain_supplier_quality, pain_unreliable_delivery, pain_poor_service,
    pain_notes,
    primary_financing, is_seasonal_business, seasonal_pattern,
    risk_score, last_contact_date, next_follow_up_date, churn_threshold_days, last_order_date,
    xentral_customer_id,
    is_test_data, is_deleted,
    created_at, created_by, updated_at, updated_by
) VALUES (
    '33333333-3333-3333-3333-000000000004'::uuid, 'C-ML-KAMPS-ESSEN',
    'Kamps Essen Limbecker Platz', 'Kamps Essen', 'GMBH', 'UNTERNEHMEN', 'BAECKEREI',
    'FILIALE', '33333333-3333-3333-3333-000000000001'::uuid,
    'AKTIV', 'GROWTH', 'KEIN_PARTNER',
    'GROSS', 32, 1, FALSE, 600000.00,
    600000.00, 570000.00,
    'NETTO_30', 80000.00, 'EXPRESS',
    'Limbecker Platz 1', '45127', 'Essen', 'DE',
    1, 0, 0,
    TRUE, FALSE, FALSE, FALSE, TRUE, FALSE, TRUE, FALSE,
    'Essen-Standort: Shopping-Center-Lage. Lieferung bis 3:30 Uhr. Problem: Personalmangel + unzuverlässige bisherige Lieferanten (2x Ausfall letzte Woche!). Sucht dringend verlässlichen Backup-Lieferanten.',
    'PRIVATE', FALSE, NULL,
    28, CURRENT_DATE - INTERVAL '4 days', CURRENT_DATE + INTERVAL '7 days', 90, CURRENT_DATE - INTERVAL '3 days',
    '92156',
    FALSE, FALSE,
    NOW() - INTERVAL '170 days', 'SYSTEM_SEED', NOW() - INTERVAL '4 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 5️⃣ KAMPS DORTMUND WESTENHELLWEG - FILIALE
-- ============================================================================

INSERT INTO customers (
    id, customer_number, company_name, trading_name, legal_form, customer_type, business_type,
    hierarchy_type, parent_customer_id,
    status, lifecycle_stage, partner_status,
    kitchen_size, employee_count, branch_count, is_chain, estimated_volume,
    expected_annual_volume, actual_annual_volume,
    payment_terms, credit_limit, delivery_condition,
    street, postal_code, city, country_code,
    locations_de, locations_ch, locations_at,
    pain_staff_shortage, pain_high_costs, pain_food_waste, pain_quality_inconsistency,
    pain_time_pressure, pain_supplier_quality, pain_unreliable_delivery, pain_poor_service,
    pain_notes,
    primary_financing, is_seasonal_business, seasonal_pattern,
    risk_score, last_contact_date, next_follow_up_date, churn_threshold_days, last_order_date,
    xentral_customer_id,
    is_test_data, is_deleted,
    created_at, created_by, updated_at, updated_by
) VALUES (
    '33333333-3333-3333-3333-000000000005'::uuid, 'C-ML-KAMPS-DORTMUND',
    'Kamps Dortmund Westenhellweg', 'Kamps Dortmund', 'GMBH', 'UNTERNEHMEN', 'BAECKEREI',
    'FILIALE', '33333333-3333-3333-3333-000000000001'::uuid,
    'AKTIV', 'GROWTH', 'KEIN_PARTNER',
    'GROSS', 30, 1, FALSE, 580000.00,
    580000.00, 550000.00,
    'NETTO_30', 80000.00, 'EXPRESS',
    'Westenhellweg 102', '44137', 'Dortmund', 'DE',
    1, 0, 0,
    FALSE, TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE,
    'Dortmund-Standort: Fußgängerzone mit hoher Frequenz Sa+So. Lieferung bis 3:30 Uhr. Personal OK. Problem: Hohe Energiekosten (Backöfen 4-20 Uhr). Interesse an Energie-effizienten Produkten.',
    'PRIVATE', FALSE, NULL,
    24, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '14 days', 90, CURRENT_DATE - INTERVAL '2 days',
    '92156',
    FALSE, FALSE,
    NOW() - INTERVAL '160 days', 'SYSTEM_SEED', NOW() - INTERVAL '5 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 🧑‍💼 CONTACTS: Kamps Backstube GmbH (HEADQUARTER)
-- ============================================================================

-- Contact 1: Geschäftsführer (EXECUTIVE)
INSERT INTO customer_contacts (
    id, customer_id, salutation, first_name, last_name,
    position, decision_level, warmth_level,
    email, phone, mobile,
    is_primary, preferred_contact_method, notes,
    is_deleted, created_at, created_by
) VALUES (
    '33333333-3333-C001-0000-000000000001'::uuid,
    '33333333-3333-3333-3333-000000000001'::uuid,
    'HERR', 'Peter', 'Schneider',
    'Geschäftsführer Kamps Backstube GmbH',
    'EXECUTIVE', 72,
    'peter.schneider@kamps.de', '+49 2163 5678-10', '+49 172 3456789',
    TRUE, 'EMAIL',
    'Hauptansprechpartner für strategische Entscheidungen. Fokus auf Kostenoptimierung und Qualität. Sehr interessiert an Nacht-Lieferkonzepten. Tipp: Immer pünktlich (Bäcker-Mentalität!).',
    FALSE, NOW() - INTERVAL '220 days', 'SYSTEM_SEED'
);

-- Contact 2: Einkaufsleiter (MANAGER)
INSERT INTO customer_contacts (
    id, customer_id, salutation, first_name, last_name,
    position, decision_level, warmth_level,
    email, phone, mobile,
    is_primary, preferred_contact_method, notes,
    is_deleted, created_at, created_by
) VALUES (
    '33333333-3333-C001-0000-000000000002'::uuid,
    '33333333-3333-3333-3333-000000000001'::uuid,
    'FRAU', 'Sandra', 'Koch',
    'Leiterin Zentraleinkauf',
    'MANAGER', 88,
    'sandra.koch@kamps.de', '+49 2163 5678-25', '+49 151 7788990',
    FALSE, 'PHONE',
    'Operative Hauptansprechpartnerin. Verhandelt Preise für alle Filialen. Sehr freundlich und offen. WICHTIG: Lieferzeiten 3:30 Uhr sind NICHT verhandelbar! Am besten erreichbar: Mo/Mi 9-11 Uhr.',
    FALSE, NOW() - INTERVAL '215 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 🧑‍💼 CONTACTS: Kamps Düsseldorf (Branch-spezifisch)
-- ============================================================================

INSERT INTO customer_contacts (
    id, customer_id, salutation, first_name, last_name,
    position, decision_level, warmth_level,
    email, phone, mobile,
    is_primary, preferred_contact_method, notes,
    is_deleted, created_at, created_by
) VALUES (
    '33333333-3333-C002-0000-000000000001'::uuid,
    '33333333-3333-3333-3333-000000000002'::uuid,
    'HERR', 'Michael', 'Berg',
    'Filialleiter Düsseldorf HBF',
    'OPERATIONAL', 65,
    'michael.berg@kamps.de', '+49 211 8899-10', '+49 170 1122334',
    TRUE, 'PHONE',
    'Filialleiter seit 3 Jahren. Pragmatisch und lösungsorientiert. Problem: Morgendlicher Verkehrs-Stau verzögert oft Lieferungen. Bevorzugt SMS bei Lieferproblemen (schneller als Anruf).',
    FALSE, NOW() - INTERVAL '190 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 📋 TIMELINE EVENTS
-- ============================================================================

INSERT INTO customer_timeline_events (
    id, customer_id, event_type, event_date,
    title, description, category, importance, performed_by, created_at
) VALUES (
    '33333333-3333-E001-0000-000000000001'::uuid,
    '33333333-3333-3333-3333-000000000001'::uuid,
    'CUSTOMER_CREATED', NOW() - INTERVAL '220 days',
    'Kunde angelegt',
    'Kamps Backstube GmbH als Headquarter-Kunde angelegt. 5 Filialen in NRW. Xentral-Kunde 92156 verknüpft. Lieferzeit-Requirement: 3:30 Uhr!',
    'SYSTEM', 'HIGH', 'SYSTEM_SEED',
    NOW() - INTERVAL '220 days'
);

INSERT INTO customer_timeline_events (
    id, customer_id, event_type, event_date,
    title, description, category, importance, performed_by, created_at
) VALUES (
    '33333333-3333-E001-0000-000000000002'::uuid,
    '33333333-3333-3333-3333-000000000001'::uuid,
    'NOTE_ADDED', NOW() - INTERVAL '218 days',
    'Erstgespräch mit GF Peter Schneider',
    'Erstgespräch geführt. Kamps sucht verlässliche Nacht-Lieferanten mit Frische-Garantie. Hauptproblem: Frühe Lieferzeiten (3:30 Uhr) + Personalmangel bei Bäckern. Interesse an Bio-Backzutaten. Follow-up: Produktmuster + Liefertest um 3:30 Uhr.',
    'NOTE', 'HIGH', 'Sales Rep - Julia Weber',
    NOW() - INTERVAL '218 days'
);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- 1️⃣ Show Kamps hierarchy (should show 1 parent + 4 children):
-- SELECT
--   CASE WHEN hierarchy_type = 'HEADQUARTER' THEN '📂 ' || company_name
--        ELSE '  ├─ 🍞 ' || company_name
--   END AS hierarchy,
--   city, employee_count, actual_annual_volume
-- FROM customers
-- WHERE xentral_customer_id = '92156'
-- ORDER BY CASE hierarchy_type WHEN 'HEADQUARTER' THEN 0 ELSE 1 END, city;
