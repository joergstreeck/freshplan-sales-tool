-- V219: Basic test customers for integration tests
-- Only creates 5 essential test customers
-- Additional test data is created by TestDataService in Java

INSERT INTO customers (
    id, customer_number, company_name, customer_type, status, industry,
    expected_annual_volume, risk_score, is_test_data, is_deleted,
    created_at, updated_at, created_by, updated_by,
    hierarchy_type, lifecycle_stage, partner_status, payment_terms, 
    delivery_condition, total_locations_eu, locations_germany,
    locations_austria, locations_switzerland, locations_rest_eu
) VALUES 
-- Basic test customer
(gen_random_uuid(), 'TEST-001', '[TEST] Basic Customer', 'UNTERNEHMEN', 'AKTIV', 'RESTAURANT',
 100000.00, 30, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration', 'migration',
 'STANDALONE', 'RETENTION', 'KEIN_PARTNER', 'NETTO_30', 'STANDARD', 1, 1, 0, 0, 0),
 
-- Risk customer
(gen_random_uuid(), 'TEST-002', '[TEST] Risk Customer', 'UNTERNEHMEN', 'RISIKO', 'HOTEL',
 50000.00, 80, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration', 'migration',
 'STANDALONE', 'RECOVERY', 'KEIN_PARTNER', 'NETTO_30', 'STANDARD', 1, 1, 0, 0, 0),

-- Chain customer
(gen_random_uuid(), 'TEST-003', '[TEST] Chain Customer', 'UNTERNEHMEN', 'AKTIV', 'RESTAURANT',
 500000.00, 20, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration', 'migration',
 'HEADQUARTER', 'GROWTH', 'KEIN_PARTNER', 'NETTO_30', 'STANDARD', 10, 8, 1, 1, 0),

-- Lead customer
(gen_random_uuid(), 'TEST-004', '[TEST] Lead Customer', 'UNTERNEHMEN', 'LEAD', 'CATERING',
 75000.00, 50, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration', 'migration',
 'STANDALONE', 'ACQUISITION', 'KEIN_PARTNER', 'NETTO_30', 'STANDARD', 1, 1, 0, 0, 0),

-- Prospect customer
(gen_random_uuid(), 'TEST-005', '[TEST] Prospect Customer', 'UNTERNEHMEN', 'PROSPECT', 'VERANSTALTUNG',
 90000.00, 40, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration', 'migration',
 'STANDALONE', 'ONBOARDING', 'KEIN_PARTNER', 'NETTO_30', 'STANDARD', 1, 1, 0, 0, 0)

ON CONFLICT (customer_number) DO NOTHING;

-- Note: Additional test data is created by TestDataService.java
-- which provides type-safe, easily maintainable test data creation