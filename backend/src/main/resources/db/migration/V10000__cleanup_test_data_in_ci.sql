-- V10000: CI Test Data Cleanup Migration
-- This migration runs in CI to clean up ONLY problematic test data from previous runs
-- It targets SPECIFIC patterns that cause issues, not all test data

-- Only run in test/ci profile (check if we have problematic test data)
DO $$
BEGIN
    -- Only clean if we find the SPECIFIC problematic patterns from failed CI runs
    IF EXISTS (
        SELECT 1 FROM customers 
        WHERE company_name IN ('Parent Company 1755379439908', 'Child Company 1755379439715', 
                               'Source Company 1755379439775', 'Target Company 1755379439896')
           OR (company_name LIKE '%Company %' AND company_name ~ '\d{13}$')  -- ends with 13-digit timestamp
    ) THEN
        
        RAISE NOTICE 'Found problematic test data from previous CI runs, cleaning up...';
        
        -- Delete in correct order due to foreign keys
        
        -- 1. Delete timeline events for problematic test customers ONLY
        DELETE FROM customer_timeline_events
        WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE (
                -- ONLY customers with 13-digit timestamps (milliseconds since epoch)
                company_name ~ '\d{13}$'
                -- ONLY these specific problematic patterns without [TEST] prefix
                OR (company_name LIKE 'Parent Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Child Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Source Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Target Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Status Test Company %' AND company_name ~ '\d{10,}$')
            )
        );
        
        -- 2. Delete opportunities for problematic test customers ONLY
        DELETE FROM opportunities
        WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE (
                company_name ~ '\d{13}$'
                OR (company_name LIKE 'Parent Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Child Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Source Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Target Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Status Test Company %' AND company_name ~ '\d{10,}$')
            )
        );
        
        -- 3. Delete customer contacts for problematic test customers ONLY
        DELETE FROM customer_contacts
        WHERE customer_id IN (
            SELECT id FROM customers 
            WHERE (
                company_name ~ '\d{13}$'
                OR (company_name LIKE 'Parent Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Child Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Source Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Target Company %' AND company_name NOT LIKE '[TEST]%')
                OR (company_name LIKE 'Status Test Company %' AND company_name ~ '\d{10,}$')
            )
        );
        
        -- 4. Finally delete ONLY the problematic test customers
        DELETE FROM customers 
        WHERE (
            -- ONLY customers with 13-digit timestamps (milliseconds)
            company_name ~ '\d{13}$'
            -- ONLY these specific patterns WITHOUT [TEST] prefix
            OR (company_name LIKE 'Parent Company %' AND company_name NOT LIKE '[TEST]%')
            OR (company_name LIKE 'Child Company %' AND company_name NOT LIKE '[TEST]%')
            OR (company_name LIKE 'Source Company %' AND company_name NOT LIKE '[TEST]%')
            OR (company_name LIKE 'Target Company %' AND company_name NOT LIKE '[TEST]%')
            OR (company_name LIKE 'Status Test Company %' AND company_name ~ '\d{10,}$')
        );
        
        RAISE NOTICE 'Problematic test data cleanup completed';
        
    ELSE
        RAISE NOTICE 'No problematic test data found, skipping cleanup';
    END IF;
END $$;