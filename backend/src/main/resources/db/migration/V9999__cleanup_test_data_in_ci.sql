-- V9999: CI Test Data Cleanup Migration
-- This migration runs in CI to clean up test data from previous runs
-- It should be the LAST migration to ensure all test data is removed

-- Only run in test/ci profile (check if we have test data)
DO $$
BEGIN
    -- Only clean if we find obvious test data patterns
    IF EXISTS (
        SELECT 1 FROM customers 
        WHERE company_name LIKE '%Company %' 
        AND company_name ~ '\d{10,}$'  -- ends with timestamp
    ) THEN
        
        RAISE NOTICE 'Found test data from previous CI runs, cleaning up...';
        
        -- Delete in correct order due to foreign keys
        
        -- 1. Delete timeline events for test customers
        DELETE FROM customer_timeline_events
        WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE (
                -- Timestamp pattern at end (e.g., "Company 1755379439715")
                company_name ~ '\d{10,}$'
                -- Or explicit test patterns
                OR company_name LIKE 'Parent Company%'
                OR company_name LIKE 'Child Company%'
                OR company_name LIKE 'Source Company%'
                OR company_name LIKE 'Target Company%'
                OR company_name LIKE 'Test Company%'
                OR company_name LIKE 'Status Test Company%'
                OR customer_number LIKE 'CUST-%'
                OR customer_number LIKE 'TEST-%'
                OR customer_number LIKE 'KD-TEST%'
                -- Or marked as test data
                OR is_test_data = true
            )
        );
        
        -- 2. Delete opportunities for test customers
        DELETE FROM opportunities
        WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE (
                company_name ~ '\d{10,}$'
                OR company_name LIKE 'Parent Company%'
                OR company_name LIKE 'Child Company%'
                OR company_name LIKE 'Source Company%'
                OR company_name LIKE 'Target Company%'
                OR company_name LIKE 'Test Company%'
                OR company_name LIKE 'Status Test Company%'
                OR customer_number LIKE 'CUST-%'
                OR customer_number LIKE 'TEST-%'
                OR customer_number LIKE 'KD-TEST%'
                OR is_test_data = true
            )
        );
        
        -- 3. Delete customer contacts
        DELETE FROM customer_contacts
        WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE (
                company_name ~ '\d{10,}$'
                OR company_name LIKE 'Parent Company%'
                OR company_name LIKE 'Child Company%'
                OR company_name LIKE 'Source Company%'
                OR company_name LIKE 'Target Company%'
                OR company_name LIKE 'Test Company%'
                OR company_name LIKE 'Status Test Company%'
                OR customer_number LIKE 'CUST-%'
                OR customer_number LIKE 'TEST-%'
                OR customer_number LIKE 'KD-TEST%'
                OR is_test_data = true
            )
        );
        
        -- 4. Finally delete the test customers themselves
        DELETE FROM customers 
        WHERE (
            -- Timestamp pattern at end
            company_name ~ '\d{10,}$'
            -- Explicit test patterns
            OR company_name LIKE 'Parent Company%'
            OR company_name LIKE 'Child Company%'
            OR company_name LIKE 'Source Company%'
            OR company_name LIKE 'Target Company%'
            OR company_name LIKE 'Test Company%'
            OR company_name LIKE 'Status Test Company%'
            OR customer_number LIKE 'CUST-%'
            OR customer_number LIKE 'TEST-%'
            OR customer_number LIKE 'KD-TEST%'
            -- Or marked as test data
            OR is_test_data = true
        );
        
        RAISE NOTICE 'Test data cleanup completed';
        
    ELSE
        RAISE NOTICE 'No obvious test data found, skipping cleanup';
    END IF;
END $$;