-- ============================================================================
-- V220: EINMALIGE BEREINIGUNG DER TEST-VERSCHMUTZUNG
-- ============================================================================
-- Diese Migration läuft NUR in der Entwicklungs-Datenbank
-- und entfernt ALLE Test-Daten die sich angesammelt haben
-- ============================================================================

-- Nur ausführen wenn wir NICHT in der Test-Umgebung sind
DO $$
DECLARE
    db_name text;
BEGIN
    -- Hole den Datenbanknamen
    SELECT current_database() INTO db_name;
    
    -- Nur in der Hauptdatenbank ausführen, nicht in Test-DBs
    IF db_name = 'freshplan' THEN
        RAISE NOTICE 'Bereinige Test-Verschmutzung in Datenbank: %', db_name;
        
        -- 1. Lösche abhängige Daten
        DELETE FROM contact_interactions WHERE contact_id IN (
            SELECT id FROM customer_contacts WHERE customer_id IN (
                SELECT id FROM customers 
                WHERE (company_name LIKE '[TEST%' OR 
                       customer_number LIKE 'PF%' OR 
                       customer_number LIKE 'PA%' OR 
                       customer_number LIKE 'PI%' OR 
                       customer_number LIKE 'QK%' OR 
                       customer_number LIKE 'PG%' OR 
                       customer_number LIKE 'E1%' OR 
                       customer_number LIKE 'E2%' OR 
                       customer_number LIKE 'S1%' OR 
                       customer_number LIKE 'S2%' OR 
                       customer_number LIKE 'EX%' OR 
                       customer_number LIKE 'PT%' OR 
                       customer_number LIKE 'ACT%' OR 
                       customer_number LIKE 'INA%' OR
                       is_test_data = true)
                AND customer_number != 'KD-2025-00089' -- Behalte den einen echten Kunden
            )
        );
        
        DELETE FROM customer_contacts WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE (company_name LIKE '[TEST%' OR 
                   customer_number LIKE 'PF%' OR 
                   customer_number LIKE 'PA%' OR 
                   customer_number LIKE 'PI%' OR 
                   customer_number LIKE 'QK%' OR 
                   customer_number LIKE 'PG%' OR 
                   customer_number LIKE 'E1%' OR 
                   customer_number LIKE 'E2%' OR 
                   customer_number LIKE 'S1%' OR 
                   customer_number LIKE 'S2%' OR 
                   customer_number LIKE 'EX%' OR 
                   customer_number LIKE 'PT%' OR 
                   customer_number LIKE 'ACT%' OR 
                   customer_number LIKE 'INA%' OR
                   is_test_data = true)
            AND customer_number != 'KD-2025-00089'
        );
        
        DELETE FROM customer_timeline_events WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE (company_name LIKE '[TEST%' OR 
                   customer_number LIKE 'PF%' OR 
                   customer_number LIKE 'PA%' OR 
                   customer_number LIKE 'PI%' OR 
                   customer_number LIKE 'QK%' OR 
                   customer_number LIKE 'PG%' OR 
                   customer_number LIKE 'E1%' OR 
                   customer_number LIKE 'E2%' OR 
                   customer_number LIKE 'S1%' OR 
                   customer_number LIKE 'S2%' OR 
                   customer_number LIKE 'EX%' OR 
                   customer_number LIKE 'PT%' OR 
                   customer_number LIKE 'ACT%' OR 
                   customer_number LIKE 'INA%' OR
                   is_test_data = true)
            AND customer_number != 'KD-2025-00089'
        );
        
        DELETE FROM opportunities WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE (company_name LIKE '[TEST%' OR 
                   customer_number LIKE 'PF%' OR 
                   customer_number LIKE 'PA%' OR 
                   customer_number LIKE 'PI%' OR 
                   customer_number LIKE 'QK%' OR 
                   customer_number LIKE 'PG%' OR 
                   customer_number LIKE 'E1%' OR 
                   customer_number LIKE 'E2%' OR 
                   customer_number LIKE 'S1%' OR 
                   customer_number LIKE 'S2%' OR 
                   customer_number LIKE 'EX%' OR 
                   customer_number LIKE 'PT%' OR 
                   customer_number LIKE 'ACT%' OR 
                   customer_number LIKE 'INA%' OR
                   is_test_data = true)
            AND customer_number != 'KD-2025-00089'
        );
        
        -- 2. Lösche die Test-Kunden selbst
        DELETE FROM customers 
        WHERE (company_name LIKE '[TEST%' OR 
               customer_number LIKE 'PF%' OR 
               customer_number LIKE 'PA%' OR 
               customer_number LIKE 'PI%' OR 
               customer_number LIKE 'QK%' OR 
               customer_number LIKE 'PG%' OR 
               customer_number LIKE 'E1%' OR 
               customer_number LIKE 'E2%' OR 
               customer_number LIKE 'S1%' OR 
               customer_number LIKE 'S2%' OR 
               customer_number LIKE 'EX%' OR 
               customer_number LIKE 'PT%' OR 
               customer_number LIKE 'ACT%' OR 
               customer_number LIKE 'INA%' OR
               is_test_data = true)
        AND customer_number != 'KD-2025-00089';
        
        -- 3. Erstelle die 58 stabilen Demo-Kunden für die Entwicklung
        -- Diese bleiben IMMER in der Entwicklungs-DB
        -- CALL create_stable_demo_customers_if_needed(); -- DEAKTIVIERT: Procedure existiert nicht
        
        RAISE NOTICE 'Test-Verschmutzung erfolgreich bereinigt';
    ELSE
        RAISE NOTICE 'Überspringe Bereinigung in Test-Datenbank: %', db_name;
    END IF;
END $$;

-- ============================================================================
-- STORED PROCEDURE für stabile Demo-Kunden - ENTFERNT
-- ============================================================================
-- Die Procedure wurde entfernt, da sie Probleme verursacht.
-- Demo-Kunden werden bei Bedarf manuell über V219 erstellt.