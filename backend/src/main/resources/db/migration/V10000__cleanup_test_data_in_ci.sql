-- V10000: CI Test Data Cleanup Migration
-- This migration runs in CI to clean up ALL test data without proper [TEST]/[SEED] prefix
-- It identifies test data by patterns and creators

-- Only run in test/ci profile
DO $$
DECLARE
    customer_count INTEGER;
    ci_mode BOOLEAN;
BEGIN
    -- Check if we are in CI mode
    ci_mode := current_setting('ci.build', true) = 'true';
    
    IF NOT ci_mode THEN
        RAISE NOTICE 'V10000: Skipping cleanup - not in CI mode (ci.build != true)';
        RETURN;
    END IF;
    
    RAISE NOTICE 'V10000: Running in CI mode - starting test data cleanup';
    
    -- Check if we have any unmarked test data
    SELECT COUNT(*) INTO customer_count 
    FROM customers 
    WHERE company_name NOT LIKE '[TEST]%' 
      AND company_name NOT LIKE '[SEED]%'
      AND (
          -- Common test patterns without proper prefix
          company_name LIKE 'Test Company%'
          OR company_name LIKE '%Company %' 
          OR company_name LIKE 'Parent Company%'
          OR company_name LIKE 'Child Company%'
          OR company_name LIKE 'Source Company%'
          OR company_name LIKE 'Target Company%'
          OR company_name LIKE 'Status Test%'
          OR customer_number LIKE 'TEST-%'
          OR customer_number LIKE 'CUST-%'
          -- Test users that create test data
          OR created_by IN ('test', 'test-user', 'testuser', 'test-system')
          -- Specific test customer numbers from test-system
          OR customer_number LIKE 'TEST_CUST_%'
      );
    
    IF customer_count > 0 THEN
        RAISE NOTICE 'Found % unmarked test customers from previous CI runs, cleaning up...', customer_count;
        
        -- Delete in correct order due to foreign keys
        
        -- 1. Delete timeline events
        DELETE FROM customer_timeline_events
        WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE company_name NOT LIKE '[TEST]%' 
              AND company_name NOT LIKE '[SEED]%'
              AND (
                  company_name LIKE 'Test Company%'
                  OR company_name LIKE '%Company %'
                  OR company_name LIKE 'Parent Company%'
                  OR company_name LIKE 'Child Company%'
                  OR company_name LIKE 'Source Company%'
                  OR company_name LIKE 'Target Company%'
                  OR company_name LIKE 'Status Test%'
                  OR customer_number LIKE 'TEST-%'
                  OR customer_number LIKE 'CUST-%'
                  OR created_by IN ('test', 'test-user', 'testuser', 'test-system')
              )
        );
        
        -- 2. Delete opportunities
        DELETE FROM opportunities
        WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE company_name NOT LIKE '[TEST]%' 
              AND company_name NOT LIKE '[SEED]%'
              AND (
                  company_name LIKE 'Test Company%'
                  OR company_name LIKE '%Company %'
                  OR company_name LIKE 'Parent Company%'
                  OR company_name LIKE 'Child Company%'
                  OR company_name LIKE 'Source Company%'
                  OR company_name LIKE 'Target Company%'
                  OR company_name LIKE 'Status Test%'
                  OR customer_number LIKE 'TEST-%'
                  OR customer_number LIKE 'CUST-%'
                  OR created_by IN ('test', 'test-user', 'testuser', 'test-system')
              )
        );
        
        -- 3. Delete customer contacts
        DELETE FROM customer_contacts
        WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE company_name NOT LIKE '[TEST]%' 
              AND company_name NOT LIKE '[SEED]%'
              AND (
                  company_name LIKE 'Test Company%'
                  OR company_name LIKE '%Company %'
                  OR company_name LIKE 'Parent Company%'
                  OR company_name LIKE 'Child Company%'
                  OR company_name LIKE 'Source Company%'
                  OR company_name LIKE 'Target Company%'
                  OR company_name LIKE 'Status Test%'
                  OR customer_number LIKE 'TEST-%'
                  OR customer_number LIKE 'CUST-%'
                  OR created_by IN ('test', 'test-user', 'testuser', 'test-system')
              )
        );
        
        -- 4. Finally delete the customers
        DELETE FROM customers 
        WHERE company_name NOT LIKE '[TEST]%' 
          AND company_name NOT LIKE '[SEED]%'
          AND (
              company_name LIKE 'Test Company%'
              OR company_name LIKE '%Company %'
              OR company_name LIKE 'Parent Company%'
              OR company_name LIKE 'Child Company%'
              OR company_name LIKE 'Source Company%'
              OR company_name LIKE 'Target Company%'
              OR company_name LIKE 'Status Test%'
              OR customer_number LIKE 'TEST-%'
              OR customer_number LIKE 'CUST-%'
              OR created_by IN ('test', 'test-user', 'testuser', 'test-system')
          );
        
        RAISE NOTICE 'Cleanup completed - removed % unmarked test customers', customer_count;
    ELSE
        RAISE NOTICE 'No unmarked test data found - database is clean';
    END IF;
END $$;