-- ============================================================================
-- Migration V90019: Seed Knuspr Multi-Location (COMPLETE WITH DEPTH!)
-- Sprint 2.1.7.7 - Multi-Location Management
-- ============================================================================
-- PURPOSE:
--   Provides realistic DEEP SEED data for Knuspr (Online-Supermarket) with:
--   - 1 HEADQUARTER (München HQ) with full company details
--   - 3 FILIALE customers (Micro-Fulfillment-Centers in München)
--   - 2-3 Contacts per location (Operations Manager, Warehouse Manager)
--   - Timeline Events (Customer Creation, Notes)
--   - Pain Points (10-Minuten-Delivery!, Frische-Garantie, Lageroptimierung)
--   - Same xentral_customer_id (78934) for Fuzzy-Matching testing
--
-- BUSINESS CONTEXT:
--   Knuspr ist ein Online-Supermarkt (10-Minuten-Lieferung wie Gorillas/Flink)
--   Hauptproblem: Ultra-schnelle Lieferung + Frische-Garantie + Lageroptimierung
-- ============================================================================

-- ============================================================================
-- 1️⃣ KNUSPR GMBH - HEADQUARTER (München HQ)
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
    '44444444-4444-4444-4444-000000000001'::uuid, 'C-ML-KNUSPR-HQ',
    'Knuspr GmbH', 'Knuspr', 'GMBH', 'UNTERNEHMEN', 'E_COMMERCE',
    'HEADQUARTER', NULL,
    'AKTIV', 'GROWTH', 'KEIN_PARTNER',
    NULL, 220, 4, TRUE, 4200000.00,  -- kitchen_size NULL (E-Commerce hat keine Küche)
    4200000.00, 3950000.00,
    'NETTO_7', 120000.00, 'SAME_DAY',  -- Express-Lieferung!
    'Landsberger Straße 110', '80339', 'München', 'DE',
    4, 0, 0,
    TRUE, TRUE, TRUE, FALSE, TRUE, TRUE, FALSE, FALSE,
    'Hauptproblem: 10-Minuten-Lieferung erfordert ultra-schnelle Lagerdrehung. Foodwaste-Risiko bei Frischprodukten. Personalmangel bei Fahrern. Sucht verlässliche Just-in-Time-Lieferanten mit mehrfacher Tages-Lieferung (3x täglich!). Interesse an Predictive-Ordering.',
    'VENTURE', FALSE, NULL,  -- Venture-finanziert (Startup!)
    30, CURRENT_DATE - INTERVAL '4 days', CURRENT_DATE + INTERVAL '7 days', 60, CURRENT_DATE - INTERVAL '1 day',
    '78934',
    FALSE, FALSE,
    NOW() - INTERVAL '240 days', 'SYSTEM_SEED', NOW() - INTERVAL '4 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 2️⃣ KNUSPR MÜNCHEN SCHWABING - FILIALE (Micro-Fulfillment-Center)
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
    '44444444-4444-4444-4444-000000000002'::uuid, 'C-ML-KNUSPR-SCHWABING',
    'Knuspr Micro-Fulfillment-Center Schwabing', 'Knuspr Schwabing', 'GMBH', 'UNTERNEHMEN', 'E_COMMERCE',
    'FILIALE', '44444444-4444-4444-4444-000000000001'::uuid,
    'AKTIV', 'GROWTH', 'KEIN_PARTNER',
    NULL, 75, 1, FALSE, 1500000.00,
    1500000.00, 1420000.00,
    'NETTO_7', 120000.00, 'SAME_DAY',
    'Leopoldstraße 82', '80802', 'München', 'DE',
    1, 0, 0,
    TRUE, FALSE, TRUE, FALSE, TRUE, FALSE, FALSE, FALSE,
    'MFC Schwabing: Einzugsgebiet Uni-Viertel + Münchner Freiheit. Hohe Nachfrage 18-22 Uhr (Feierabend-Bestellungen!). Problem: Foodwaste bei Obst/Gemüse (kurze Haltbarkeit). Lieferung 3x täglich nötig: 6:00, 12:00, 18:00 Uhr. Interesse an kleineren Gebinden.',
    'VENTURE', FALSE, NULL,
    25, CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE + INTERVAL '7 days', 60, CURRENT_DATE - INTERVAL '1 day',
    '78934',
    FALSE, FALSE,
    NOW() - INTERVAL '210 days', 'SYSTEM_SEED', NOW() - INTERVAL '3 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 3️⃣ KNUSPR MÜNCHEN GIESING - FILIALE (Micro-Fulfillment-Center)
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
    '44444444-4444-4444-4444-000000000003'::uuid, 'C-ML-KNUSPR-GIESING',
    'Knuspr Micro-Fulfillment-Center Giesing', 'Knuspr Giesing', 'GMBH', 'UNTERNEHMEN', 'E_COMMERCE',
    'FILIALE', '44444444-4444-4444-4444-000000000001'::uuid,
    'AKTIV', 'GROWTH', 'KEIN_PARTNER',
    NULL, 68, 1, FALSE, 1350000.00,
    1350000.00, 1280000.00,
    'NETTO_7', 120000.00, 'SAME_DAY',
    'Tegernseer Landstraße 135', '81539', 'München', 'DE',
    1, 0, 0,
    FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE,
    'MFC Giesing: Einzugsgebiet Familien-Viertel. Hohe Nachfrage Freitag-Abend + Sonntag. Problem: Keine - läuft sehr gut! Lieferung 3x täglich: 6:00, 12:00, 18:00 Uhr. Personal gut besetzt. Hinweis: Beste Performance aller MFCs!',
    'VENTURE', FALSE, NULL,
    15, CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE + INTERVAL '10 days', 60, CURRENT_DATE - INTERVAL '1 day',
    '78934',
    FALSE, FALSE,
    NOW() - INTERVAL '200 days', 'SYSTEM_SEED', NOW() - INTERVAL '2 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 4️⃣ KNUSPR MÜNCHEN PASING - FILIALE (Micro-Fulfillment-Center)
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
    '44444444-4444-4444-4444-000000000004'::uuid, 'C-ML-KNUSPR-PASING',
    'Knuspr Micro-Fulfillment-Center Pasing', 'Knuspr Pasing', 'GMBH', 'UNTERNEHMEN', 'E_COMMERCE',
    'FILIALE', '44444444-4444-4444-4444-000000000001'::uuid,
    'AKTIV', 'GROWTH', 'KEIN_PARTNER',
    NULL, 70, 1, FALSE, 1350000.00,
    1350000.00, 1250000.00,
    'NETTO_7', 120000.00, 'SAME_DAY',
    'Planegger Straße 9', '81241', 'München', 'DE',
    1, 0, 0,
    TRUE, FALSE, TRUE, FALSE, TRUE, TRUE, FALSE, FALSE,
    'MFC Pasing: Einzugsgebiet Laim + Pasing. Problem: Personalmangel bei Fahrern (schwer zu rekrutieren). Foodwaste bei Milchprodukten (kurze MHD). Lieferung 3x täglich: 6:00, 12:00, 18:00 Uhr. Interesse an Predictive Ordering + längeren MHD-Produkten.',
    'VENTURE', FALSE, NULL,
    35, CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE + INTERVAL '7 days', 60, CURRENT_DATE - INTERVAL '2 days',
    '78934',
    FALSE, FALSE,
    NOW() - INTERVAL '190 days', 'SYSTEM_SEED', NOW() - INTERVAL '6 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 🧑‍💼 CONTACTS: Knuspr GmbH (HEADQUARTER)
-- ============================================================================

-- Contact 1: CEO (EXECUTIVE)
INSERT INTO customer_contacts (
    id, customer_id, salutation, first_name, last_name,
    position, decision_level, warmth_level,
    email, phone, mobile,
    is_primary, preferred_contact_method, notes,
    is_deleted, created_at, created_by
) VALUES (
    '44444444-4444-C001-0000-000000000001'::uuid,
    '44444444-4444-4444-4444-000000000001'::uuid,
    'HERR', 'Maximilian', 'Gruber',
    'CEO & Founder Knuspr',
    'EXECUTIVE', 80,
    'max.gruber@knuspr.de', '+49 89 7788-100', '+49 175 9988776',
    TRUE, 'EMAIL',
    'CEO und Gründer. Sehr tech-affin und datengetrieben. Fokus auf Skalierung und Effizienz. Interesse an Predictive Ordering und KI-basierten Lösungen. Tipp: Mag konkrete Zahlen und ROI-Berechnungen!',
    FALSE, NOW() - INTERVAL '240 days', 'SYSTEM_SEED'
);

-- Contact 2: Head of Operations (MANAGER)
INSERT INTO customer_contacts (
    id, customer_id, salutation, first_name, last_name,
    position, decision_level, warmth_level,
    email, phone, mobile,
    is_primary, preferred_contact_method, notes,
    is_deleted, created_at, created_by
) VALUES (
    '44444444-4444-C001-0000-000000000002'::uuid,
    '44444444-4444-4444-4444-000000000001'::uuid,
    'FRAU', 'Lisa', 'Hoffmann',
    'Head of Operations',
    'MANAGER', 92,
    'lisa.hoffmann@knuspr.de', '+49 89 7788-120', '+49 160 5544332',
    FALSE, 'PHONE',
    'Operative Hauptansprechpartnerin für alle MFCs. Verhandelt Lieferkonditionen zentral. Sehr lösungsorientiert und pragmatisch. WICHTIG: 3x tägliche Lieferung (6:00, 12:00, 18:00 Uhr) ist KRITISCH für 10-Minuten-Promise! Beste Erreichbarkeit: Di/Do 14-16 Uhr.',
    FALSE, NOW() - INTERVAL '235 days', 'SYSTEM_SEED'
);

-- Contact 3: Head of Purchasing (MANAGER)
INSERT INTO customer_contacts (
    id, customer_id, salutation, first_name, last_name,
    position, decision_level, warmth_level,
    email, phone, mobile,
    is_primary, preferred_contact_method, notes,
    is_deleted, created_at, created_by
) VALUES (
    '44444444-4444-C001-0000-000000000003'::uuid,
    '44444444-4444-4444-4444-000000000001'::uuid,
    'HERR', 'Thomas', 'Fischer',
    'Head of Purchasing',
    'MANAGER', 75,
    'thomas.fischer@knuspr.de', '+49 89 7788-130', '+49 171 6677889',
    FALSE, 'EMAIL',
    'Verantwortlich für zentrale Einkaufsverhandlungen. Fokus auf Kosten-Optimierung und Qualität. Interesse an Rahmenverträgen mit flexiblen Mengen. Tipp: Bevorzugt strukturierte Angebote mit Preisstaffeln!',
    FALSE, NOW() - INTERVAL '230 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 🧑‍💼 CONTACTS: Knuspr Schwabing (Branch-spezifisch)
-- ============================================================================

INSERT INTO customer_contacts (
    id, customer_id, salutation, first_name, last_name,
    position, decision_level, warmth_level,
    email, phone, mobile,
    is_primary, preferred_contact_method, notes,
    is_deleted, created_at, created_by
) VALUES (
    '44444444-4444-C002-0000-000000000001'::uuid,
    '44444444-4444-4444-4444-000000000002'::uuid,
    'FRAU', 'Anna', 'Bauer',
    'Warehouse Manager Schwabing',
    'OPERATIONAL', 68,
    'anna.bauer@knuspr.de', '+49 89 7788-201', '+49 162 8899001',
    TRUE, 'SMS',  -- Bevorzugt SMS (schneller im Warehouse!)
    'Warehouse Managerin seit 6 Monaten. Verantwortlich für Wareneingang und Lageroptimierung. Problem: Foodwaste bei Obst/Gemüse. Tipp: Bevorzugt SMS bei Lieferankündigung (kein Email-Check im Warehouse). Ansprechbar: 7-19 Uhr.',
    FALSE, NOW() - INTERVAL '210 days', 'SYSTEM_SEED'
);

-- ============================================================================
-- 📋 TIMELINE EVENTS
-- ============================================================================

INSERT INTO customer_timeline_events (
    id, customer_id, event_type, event_date,
    title, description, category, importance, performed_by, created_at
) VALUES (
    '44444444-4444-E001-0000-000000000001'::uuid,
    '44444444-4444-4444-4444-000000000001'::uuid,
    'CUSTOMER_CREATED', NOW() - INTERVAL '240 days',
    'Kunde angelegt',
    'Knuspr GmbH als Headquarter-Kunde angelegt. 4 Micro-Fulfillment-Centers in München. Xentral-Kunde 78934 verknüpft. Lieferzeit-Requirement: 3x täglich (6:00, 12:00, 18:00 Uhr) für 10-Minuten-Delivery!',
    'SYSTEM', 'HIGH', 'SYSTEM_SEED',
    NOW() - INTERVAL '240 days'
);

INSERT INTO customer_timeline_events (
    id, customer_id, event_type, event_date,
    title, description, category, importance, performed_by, created_at
) VALUES (
    '44444444-4444-E001-0000-000000000002'::uuid,
    '44444444-4444-4444-4444-000000000001'::uuid,
    'NOTE_ADDED', NOW() - INTERVAL '238 days',
    'Erstgespräch mit CEO Max Gruber',
    'Erstgespräch mit Gründer Max Gruber. Knuspr sucht verlässliche Just-in-Time-Lieferanten mit 3x täglicher Lieferung. Hauptprobleme: Foodwaste-Risiko bei Frischprodukten + Personalmangel bei Fahrern. Interesse an Predictive Ordering und kleineren Gebinden. Follow-up: Pilot-Projekt mit MFC Schwabing starten.',
    'NOTE', 'HIGH', 'Sales Rep - Alexander Schmidt',
    NOW() - INTERVAL '238 days'
);

INSERT INTO customer_timeline_events (
    id, customer_id, event_type, event_date,
    title, description, category, importance, performed_by, created_at
) VALUES (
    '44444444-4444-E001-0000-000000000003'::uuid,
    '44444444-4444-4444-4444-000000000001'::uuid,
    'NOTE_ADDED', NOW() - INTERVAL '220 days',
    'Vertragsabschluss nach erfolgreicher Pilot-Phase',
    'Pilot mit MFC Schwabing erfolgreich! Lieferqualität und Pünktlichkeit überzeugen. Vertrag für alle 4 MFCs: 350.000€/Monat. 3x tägliche Lieferung. Zahlungsziel: 7 Tage (schnelle Zahlung!). Kreditlimit: 120.000€. Roll-Out auf alle MFCs ab nächster Woche.',
    'NOTE', 'HIGH', 'Sales Rep - Alexander Schmidt',
    NOW() - INTERVAL '220 days'
);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- 1️⃣ Show Knuspr hierarchy (should show 1 parent + 3 children):
-- SELECT
--   CASE WHEN hierarchy_type = 'HEADQUARTER' THEN '📂 ' || company_name
--        ELSE '  ├─ 📦 ' || company_name
--   END AS hierarchy,
--   city, employee_count, actual_annual_volume, risk_score
-- FROM customers
-- WHERE xentral_customer_id = '78934'
-- ORDER BY CASE hierarchy_type WHEN 'HEADQUARTER' THEN 0 ELSE 1 END, actual_annual_volume DESC;
