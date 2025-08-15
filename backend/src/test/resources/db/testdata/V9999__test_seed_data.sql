-- ============================================================================
-- V9999: TEST SEED DATA - NUR FÜR TEST-UMGEBUNG
-- ============================================================================
-- Diese Migration läuft NUR in der Test-Datenbank (Testcontainers)
-- Die hohe Nummer (9999) stellt sicher, dass sie IMMER als letzte läuft
-- ============================================================================

-- Nur in Test-Umgebung ausführen
DO $$
BEGIN
    -- Prüfe ob wir in einer Test-Datenbank sind
    IF current_database() LIKE '%test%' THEN
        
        -- Bereinige ALLE Test-Daten (nicht nur SEED)
        DELETE FROM contact_interactions;
        DELETE FROM customer_contacts;
        DELETE FROM customer_timeline_events;
        DELETE FROM opportunities;
        -- Lösche ALLE Test-Kunden (SEED + von Tests erstellte)
        DELETE FROM customers WHERE is_test_data = true;
        -- Zusätzlich: Lösche alle Kunden mit Test-Patterns im Namen
        DELETE FROM customers WHERE company_name LIKE '%[TEST-%]%';
        DELETE FROM customers WHERE company_name LIKE '%[SEED]%';
        -- Sicherheitshalber: Lösche alle mit typischen Test-Prefixen
        DELETE FROM customers WHERE customer_number LIKE 'PF%-%';
        DELETE FROM customers WHERE customer_number LIKE 'S1%';
        DELETE FROM customers WHERE customer_number LIKE 'S2%';
        DELETE FROM customers WHERE customer_number LIKE 'E1%';
        DELETE FROM customers WHERE customer_number LIKE 'E2%';
        DELETE FROM customers WHERE customer_number LIKE 'ACT%';
        DELETE FROM customers WHERE customer_number LIKE 'INA%';
        DELETE FROM customers WHERE customer_number LIKE 'PA%';
        DELETE FROM customers WHERE customer_number LIKE 'PI%';
        DELETE FROM customers WHERE customer_number LIKE 'SEED-%';
        
        -- ============================================================================
        -- ERSTELLE 50 STABILE SEED-KUNDEN FÜR TESTS
        -- ============================================================================
        
        -- Seed-Kunden 1-10: Aktive Großkunden
        INSERT INTO customers (id, customer_number, company_name, customer_type, status, industry, 
                               expected_annual_volume, risk_score, is_test_data, created_by, created_at)
        VALUES 
        (gen_random_uuid(), 'SEED-001', '[SEED] Großhotel München GmbH', 'UNTERNEHMEN', 'AKTIV', 'HOTEL', 
         500000, 5, true, 'seed', NOW() - INTERVAL '2 years'),
        (gen_random_uuid(), 'SEED-002', '[SEED] Restaurant-Kette Berlin AG', 'UNTERNEHMEN', 'AKTIV', 'RESTAURANT', 
         750000, 10, true, 'seed', NOW() - INTERVAL '18 months'),
        (gen_random_uuid(), 'SEED-003', '[SEED] Catering Service Frankfurt', 'UNTERNEHMEN', 'AKTIV', 'CATERING', 
         300000, 15, true, 'seed', NOW() - INTERVAL '1 year'),
        (gen_random_uuid(), 'SEED-004', '[SEED] Kantine Hamburg GmbH', 'UNTERNEHMEN', 'AKTIV', 'GEMEINSCHAFTSVERPFLEGUNG', 
         400000, 8, true, 'seed', NOW() - INTERVAL '6 months'),
        (gen_random_uuid(), 'SEED-005', '[SEED] Bäckerei-Kette Köln', 'UNTERNEHMEN', 'AKTIV', 'BAECKEREI', 
         250000, 12, true, 'seed', NOW() - INTERVAL '3 months'),
        (gen_random_uuid(), 'SEED-006', '[SEED] Metzgerei Stuttgart', 'UNTERNEHMEN', 'AKTIV', 'METZGEREI', 
         180000, 20, true, 'seed', NOW() - INTERVAL '1 month'),
        (gen_random_uuid(), 'SEED-007', '[SEED] Bio-Markt Dresden', 'UNTERNEHMEN', 'AKTIV', 'EINZELHANDEL', 
         220000, 5, true, 'seed', NOW() - INTERVAL '2 weeks'),
        (gen_random_uuid(), 'SEED-008', '[SEED] Seniorenheim Leipzig', 'UNTERNEHMEN', 'AKTIV', 'PFLEGE', 
         350000, 3, true, 'seed', NOW() - INTERVAL '1 week'),
        (gen_random_uuid(), 'SEED-009', '[SEED] Krankenhaus Düsseldorf', 'UNTERNEHMEN', 'AKTIV', 'KRANKENHAUS', 
         900000, 2, true, 'seed', NOW() - INTERVAL '3 days'),
        (gen_random_uuid(), 'SEED-010', '[SEED] Schul-Catering Essen', 'UNTERNEHMEN', 'AKTIV', 'BILDUNG', 
         450000, 7, true, 'seed', NOW() - INTERVAL '1 day');
        
        -- Weitere 40 Seed-Kunden mit verschiedenen Status
        INSERT INTO customers (id, customer_number, company_name, customer_type, status, industry, 
                               expected_annual_volume, risk_score, is_test_data, created_by, created_at)
        SELECT 
            gen_random_uuid(),
            'SEED-' || LPAD(generate_series::text, 3, '0'),
            '[SEED] Test-Kunde ' || generate_series,
            CASE 
                WHEN generate_series % 3 = 0 THEN 'UNTERNEHMEN'
                WHEN generate_series % 3 = 1 THEN 'NEUKUNDE'
                ELSE 'PRIVATKUNDE'
            END,
            CASE 
                WHEN generate_series % 4 = 0 THEN 'AKTIV'
                WHEN generate_series % 4 = 1 THEN 'LEAD'
                WHEN generate_series % 4 = 2 THEN 'INAKTIV'
                ELSE 'ANGEBOT'
            END,
            CASE 
                WHEN generate_series % 5 = 0 THEN 'HOTEL'
                WHEN generate_series % 5 = 1 THEN 'RESTAURANT'
                WHEN generate_series % 5 = 2 THEN 'CATERING'
                WHEN generate_series % 5 = 3 THEN 'EINZELHANDEL'
                ELSE 'SONSTIGE'
            END,
            (generate_series * 10000)::decimal,
            (generate_series % 100)::integer,
            true,
            'seed',
            NOW() - (generate_series || ' days')::interval
        FROM generate_series(11, 50);
        
        RAISE NOTICE 'Test-Seed-Daten erfolgreich erstellt: 50 SEED-Kunden';
    ELSE
        RAISE NOTICE 'Überspringe Test-Seed-Daten in Produktions-DB';
    END IF;
END $$;