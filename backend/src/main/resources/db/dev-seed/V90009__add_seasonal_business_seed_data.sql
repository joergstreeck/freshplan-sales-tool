-- ============================================================================
-- V90009: DEV-SEED Seasonal Business Customers
-- ============================================================================
-- Sprint 2.1.7.4 - Section 7: Seasonal Business Support
-- Purpose: Realistic seasonal business customers for UI testing
--
-- IMPORTANT: Only runs in %dev profile (NOT in tests or production)
-- These are DEMO customers to showcase seasonal business features in Dashboard
--
-- Idempotent: Can be run multiple times (ON CONFLICT DO NOTHING)
--
-- Test Scenarios (for October - Month 10):
--   KD-SEASON-001: Eiscafé (May-Sept) → OUT-OF-SEASON in October
--   KD-SEASON-002: Eisdiele (June-Aug) → OUT-OF-SEASON in October
--   KD-SEASON-003: Strandbar (April-Sept) → OUT-OF-SEASON in October
--   KD-SEASON-004: Biergarten (May-Oct) → IN-SEASON in October ✅
--   KD-SEASON-005: Weihnachtsmarkt (Nov-Dec) → OUT-OF-SEASON in October
--   KD-SEASON-006: Skihütte (Dec-March) → OUT-OF-SEASON in October
--
-- Expected Dashboard Metrics (October):
--   - Saisonal Aktiv: 2 (Biergarten Augustiner + existing test customer)
--   - Saisonal Pausiert: 5
-- ============================================================================


-- ============================================================================
-- SEASONAL CUSTOMERS (6)
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
    kitchen_size,
    employee_count,
    branch_count,
    is_chain,
    estimated_volume,
    -- Seasonal Business Fields (Sprint 2.1.7.4 Section 7)
    is_seasonal_business,
    seasonal_months,
    seasonal_pattern,
    -- End of fields
    is_deleted,
    is_test_data,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
-- ============================================================================
-- 1. Eiscafé Venezia (SUMMER: May-September) → OUT-OF-SEASON in Oktober
-- ============================================================================
(
    gen_random_uuid(),
    'KD-SEASON-001',
    'Eiscafé Venezia GmbH',
    'Venezia Eis',
    'GmbH',
    'UNTERNEHMEN',
    'FOOD_SERVICE',
    'RESTAURANT',
    'AKTIV',
    'ACTIVE',
    'SILVER',
    35000,  -- Expected annual volume (seasonal!)
    28000,  -- Actual annual volume (last season)
    'NET_30',
    25000,  -- Credit limit
    'DAP',  -- Delivery condition
    65,     -- Risk score (good)
    CURRENT_DATE - INTERVAL '120 days',  -- Last contact: End of season
    CURRENT_DATE + INTERVAL '90 days',   -- Next follow-up: Before next season (March)
    1, 0, 0, 0, 1,  -- Germany: 1 location
    TRUE, FALSE, TRUE, FALSE, TRUE,  -- Pain points: Staff, Waste, Time
    FALSE, FALSE, FALSE,
    'Saisongeschäft (Mai-Sept). Spezialisiert auf italienisches Eis. Hoher Qualitätsanspruch.',
    'PRIVATE',
    'MITTEL',   -- Kitchen size
    12,         -- Employee count (seasonal workers)
    1,          -- Branch count
    FALSE,      -- Not a chain
    35000,      -- Estimated volume
    -- Seasonal Business Fields
    TRUE,
    '[5, 6, 7, 8, 9]'::jsonb,  -- May-September
    'SUMMER',
    -- End of fields
    FALSE,
    TRUE,
    NOW() - INTERVAL '180 days',
    'seed-data',
    NOW() - INTERVAL '120 days',
    'seed-data'
),

-- ============================================================================
-- 2. Eisdiele Dolce Vita (SUMMER: June-August) → OUT-OF-SEASON in Oktober
-- ============================================================================
(
    gen_random_uuid(),
    'KD-SEASON-002',
    'Eisdiele Dolce Vita',
    'Dolce Vita',
    'Einzelunternehmen',
    'PRIVAT',
    'FOOD_SERVICE',
    'RESTAURANT',
    'AKTIV',
    'ACTIVE',
    'BRONZE',
    18000,  -- Expected annual volume
    15000,  -- Actual annual volume
    'NET_15',
    10000,  -- Credit limit
    'DAP',
    70,     -- Risk score (very good)
    CURRENT_DATE - INTERVAL '90 days',
    CURRENT_DATE + INTERVAL '120 days',
    1, 0, 0, 0, 1,
    TRUE, TRUE, FALSE, FALSE, TRUE,  -- Pain points: Staff, Costs, Time
    FALSE, FALSE, FALSE,
    'Kleine Eisdiele im Familienbesitz. Kurze Saison Juni-August.',
    'PRIVATE',
    'KLEIN',
    6,
    1,
    FALSE,
    18000,
    TRUE,
    '[6, 7, 8]'::jsonb,  -- June-August
    'SUMMER',
    FALSE,
    TRUE,
    NOW() - INTERVAL '200 days',
    'seed-data',
    NOW() - INTERVAL '90 days',
    'seed-data'
),

-- ============================================================================
-- 3. Strandbar Sylt (SUMMER: April-September) → OUT-OF-SEASON in Oktober
-- ============================================================================
(
    gen_random_uuid(),
    'KD-SEASON-003',
    'Strandbar Sylt GmbH & Co. KG',
    'Sylt Beach Bar',
    'GmbH & Co. KG',
    'UNTERNEHMEN',
    'FOOD_SERVICE',
    'RESTAURANT',
    'AKTIV',
    'ACTIVE',
    'GOLD',
    85000,  -- Expected annual volume
    78000,  -- Actual annual volume
    'NET_30',
    50000,  -- Credit limit
    'DAP',
    55,     -- Risk score
    CURRENT_DATE - INTERVAL '30 days',
    CURRENT_DATE + INTERVAL '150 days',
    1, 0, 0, 0, 1,
    TRUE, FALSE, TRUE, TRUE, TRUE,  -- Pain points: Staff, Waste, Quality, Time
    FALSE, FALSE, FALSE,
    'Premium Strandbar auf Sylt. Lange Saison April-September. Gehobene Gastronomie.',
    'PRIVATE',
    'GROSS',
    25,
    1,
    FALSE,
    85000,
    TRUE,
    '[4, 5, 6, 7, 8, 9]'::jsonb,  -- April-September
    'SUMMER',
    FALSE,
    TRUE,
    NOW() - INTERVAL '365 days',
    'seed-data',
    NOW() - INTERVAL '30 days',
    'seed-data'
),

-- ============================================================================
-- 4. Biergarten Augustiner (SUMMER/AUTUMN: May-October) → IN-SEASON in Oktober ✅
-- ============================================================================
(
    gen_random_uuid(),
    'KD-SEASON-004',
    'Biergarten Augustiner München GmbH',
    'Augustiner Biergarten',
    'GmbH',
    'UNTERNEHMEN',
    'FOOD_SERVICE',
    'RESTAURANT',
    'AKTIV',
    'ACTIVE',
    'PLATINUM',
    120000,  -- Expected annual volume
    115000,  -- Actual annual volume
    'NET_30',
    75000,   -- Credit limit
    'DAP',
    45,      -- Risk score (excellent)
    CURRENT_DATE - INTERVAL '7 days',
    CURRENT_DATE + INTERVAL '14 days',
    1, 0, 0, 0, 1,
    TRUE, FALSE, TRUE, FALSE, TRUE,  -- Pain points: Staff, Waste, Time
    TRUE, FALSE, FALSE,              -- + Supplier Quality
    'Traditioneller Biergarten in München. Saison Mai-Oktober. Hohe Frequenz.',
    'PRIVATE',
    'SEHR_GROSS',
    40,
    1,
    FALSE,
    120000,
    TRUE,
    '[5, 6, 7, 8, 9, 10]'::jsonb,  -- May-October (IN-SEASON!)
    'SUMMER',
    FALSE,
    TRUE,
    NOW() - INTERVAL '500 days',
    'seed-data',
    NOW() - INTERVAL '7 days',
    'seed-data'
),

-- ============================================================================
-- 5. Weihnachtsmarkt Nürnberg (WINTER: November-December) → OUT-OF-SEASON
-- ============================================================================
(
    gen_random_uuid(),
    'KD-SEASON-005',
    'Weihnachtsmarkt Nürnberg GmbH',
    'Christkindlesmarkt Stand',
    'GmbH',
    'UNTERNEHMEN',
    'FOOD_SERVICE',
    'CATERING',
    'AKTIV',
    'ACTIVE',
    'SILVER',
    45000,  -- Expected annual volume
    42000,  -- Actual annual volume
    'NET_15',
    30000,  -- Credit limit
    'DAP',
    60,     -- Risk score
    CURRENT_DATE - INTERVAL '270 days',  -- Last contact: After last season
    CURRENT_DATE + INTERVAL '20 days',   -- Next follow-up: Before next season
    1, 0, 0, 0, 1,
    TRUE, TRUE, FALSE, FALSE, TRUE,  -- Pain points: Staff, Costs, Time
    FALSE, TRUE, FALSE,              -- + Unreliable Delivery
    'Weihnachtsmarkt-Stand in Nürnberg. Kurze Saison November-Dezember. Glühwein & Lebkuchen.',
    'PRIVATE',
    'KLEIN',
    8,
    1,
    FALSE,
    45000,
    TRUE,
    '[11, 12]'::jsonb,  -- November-December
    'WINTER',
    FALSE,
    TRUE,
    NOW() - INTERVAL '90 days',
    'seed-data',
    NOW() - INTERVAL '270 days',
    'seed-data'
),

-- ============================================================================
-- 6. Skihütte Zugspitze (WINTER: December-March) → OUT-OF-SEASON in Oktober
-- ============================================================================
(
    gen_random_uuid(),
    'KD-SEASON-006',
    'Skihütte Zugspitze GmbH & Co. KG',
    'Zugspitze Lodge',
    'GmbH & Co. KG',
    'UNTERNEHMEN',
    'FOOD_SERVICE',
    'HOTEL',
    'AKTIV',
    'ACTIVE',
    'GOLD',
    95000,  -- Expected annual volume
    88000,  -- Actual annual volume
    'NET_30',
    60000,  -- Credit limit
    'DAP',
    50,     -- Risk score
    CURRENT_DATE - INTERVAL '180 days',
    CURRENT_DATE + INTERVAL '45 days',
    1, 0, 0, 0, 1,
    TRUE, TRUE, TRUE, TRUE, TRUE,  -- Pain points: All major ones
    TRUE, FALSE, FALSE,
    'Skihütte auf der Zugspitze. Wintersaison Dezember-März. Hotel & Gastronomie kombiniert.',
    'PRIVATE',
    'GROSS',
    35,
    1,
    FALSE,
    95000,
    TRUE,
    '[12, 1, 2, 3]'::jsonb,  -- December-March
    'WINTER',
    FALSE,
    TRUE,
    NOW() - INTERVAL '600 days',
    'seed-data',
    NOW() - INTERVAL '180 days',
    'seed-data'
)
ON CONFLICT (customer_number) DO NOTHING;

-- =================================================================================
-- SUMMARY (for October - current month = 10)
-- =================================================================================
-- IN-SEASON (seasonalActive):  1 customer  (Biergarten Augustiner)
-- OUT-OF-SEASON (seasonalPaused): 5 customers (Eiscafés, Weihnachtsmarkt, Skihütte)
--
-- Expected Dashboard Metrics (October):
-- - Saisonal Aktiv: 2 (1 new + 1 existing test customer)
-- - Saisonal Pausiert: 5
-- =================================================================================
