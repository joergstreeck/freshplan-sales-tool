-- V90009: Add Seasonal Business Seed Data
-- Sprint 2.1.7.4 - Section 7: Seasonal Business Support
-- Purpose: Add realistic seasonal business customers for UI testing

-- IMPORTANT: Only runs in %dev profile (NOT in tests or production)
-- These are DEMO customers to showcase seasonal business features in the Dashboard

-- =============================================================================
-- SEASONAL CUSTOMERS (Mixed In-Season and Out-of-Season)
-- =============================================================================

-- 1. Eiscafé Venezia (SUMMER: May-September) → OUT-OF-SEASON in Oktober
INSERT INTO customers (
    id, customer_number, company_name, trading_name,
    status, customer_type, business_type,
    is_seasonal_business, seasonal_months, seasonal_pattern,
    created_by, created_at, is_test_data
) VALUES (
    gen_random_uuid(),
    'KD-SEASON-001',
    'Eiscafé Venezia',
    'Venezia Eis',
    'AKTIV',
    'NEUKUNDE',
    'RESTAURANT',
    TRUE,
    '[5, 6, 7, 8, 9]'::jsonb, -- May-September
    'SUMMER',
    'seed-data',
    NOW() - INTERVAL '180 days',
    TRUE
) ON CONFLICT (customer_number) DO NOTHING;

-- 2. Eisdiele Dolce Vita (SUMMER: June-August) → OUT-OF-SEASON in Oktober
INSERT INTO customers (
    id, customer_number, company_name, trading_name,
    status, customer_type, business_type,
    is_seasonal_business, seasonal_months, seasonal_pattern,
    created_by, created_at, is_test_data
) VALUES (
    gen_random_uuid(),
    'KD-SEASON-002',
    'Eisdiele Dolce Vita',
    'Dolce Vita',
    'AKTIV',
    'NEUKUNDE',
    'RESTAURANT',
    TRUE,
    '[6, 7, 8]'::jsonb, -- June-August
    'SUMMER',
    'seed-data',
    NOW() - INTERVAL '200 days',
    TRUE
) ON CONFLICT (customer_number) DO NOTHING;

-- 3. Strandbar Sylt (SUMMER: April-September) → OUT-OF-SEASON in Oktober
INSERT INTO customers (
    id, customer_number, company_name, trading_name,
    status, customer_type, business_type,
    is_seasonal_business, seasonal_months, seasonal_pattern,
    created_by, created_at, is_test_data
) VALUES (
    gen_random_uuid(),
    'KD-SEASON-003',
    'Strandbar Sylt',
    'Sylt Beach',
    'AKTIV',
    'UNTERNEHMEN',
    'RESTAURANT',
    TRUE,
    '[4, 5, 6, 7, 8, 9]'::jsonb, -- April-September
    'SUMMER',
    'seed-data',
    NOW() - INTERVAL '365 days',
    TRUE
) ON CONFLICT (customer_number) DO NOTHING;

-- 4. Biergarten Augustiner (SUMMER/AUTUMN: May-October) → IN-SEASON in Oktober
INSERT INTO customers (
    id, customer_number, company_name, trading_name,
    status, customer_type, business_type,
    is_seasonal_business, seasonal_months, seasonal_pattern,
    created_by, created_at, is_test_data
) VALUES (
    gen_random_uuid(),
    'KD-SEASON-004',
    'Biergarten Augustiner',
    'Augustiner München',
    'AKTIV',
    'UNTERNEHMEN',
    'RESTAURANT',
    TRUE,
    '[5, 6, 7, 8, 9, 10]'::jsonb, -- May-October
    'SUMMER',
    'seed-data',
    NOW() - INTERVAL '500 days',
    TRUE
) ON CONFLICT (customer_number) DO NOTHING;

-- 5. Weihnachtsmarkt Stand (WINTER: November-December) → OUT-OF-SEASON in Oktober
INSERT INTO customers (
    id, customer_number, company_name, trading_name,
    status, customer_type, business_type,
    is_seasonal_business, seasonal_months, seasonal_pattern,
    created_by, created_at, is_test_data
) VALUES (
    gen_random_uuid(),
    'KD-SEASON-005',
    'Weihnachtsmarkt Nürnberg',
    'Christkindl',
    'AKTIV',
    'NEUKUNDE',
    'RESTAURANT',
    TRUE,
    '[11, 12]'::jsonb, -- November-December
    'WINTER',
    'seed-data',
    NOW() - INTERVAL '90 days',
    TRUE
) ON CONFLICT (customer_number) DO NOTHING;

-- 6. Skihütte Zugspitze (WINTER: December-March) → OUT-OF-SEASON in Oktober
INSERT INTO customers (
    id, customer_number, company_name, trading_name,
    status, customer_type, business_type,
    is_seasonal_business, seasonal_months, seasonal_pattern,
    created_by, created_at, is_test_data
) VALUES (
    gen_random_uuid(),
    'KD-SEASON-006',
    'Skihütte Zugspitze',
    'Zugspitze Lodge',
    'AKTIV',
    'UNTERNEHMEN',
    'RESTAURANT',
    TRUE,
    '[12, 1, 2, 3]'::jsonb, -- December-March
    'WINTER',
    'seed-data',
    NOW() - INTERVAL '600 days',
    TRUE
) ON CONFLICT (customer_number) DO NOTHING;

-- =============================================================================
-- SUMMARY (for October - current month = 10)
-- =============================================================================
-- IN-SEASON (seasonalActive):  1 customer  (Biergarten Augustiner)
-- OUT-OF-SEASON (seasonalPaused): 5 customers (Eiscafés, Weihnachtsmarkt, Skihütte)
--
-- Expected Dashboard Metrics (October):
-- - Saisonal Aktiv: 1
-- - Saisonal Pausiert: 5
-- =============================================================================
