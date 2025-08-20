-- ============================================================================
-- V10001: Test Data Contract Guard (Warning-based)
-- ============================================================================
-- Purpose: Monitor and warn about test data consistency issues
-- Does NOT fail the migration, only provides warnings
-- Helps identify problems early without breaking CI
-- ============================================================================

DO $$
DECLARE
    test_count INTEGER;
    unmarked_count INTEGER;
    no_prefix_count INTEGER;
    old_test_count INTEGER;
    duplicate_count INTEGER;
    warning_level TEXT := 'OK';
    warning_messages TEXT := '';
BEGIN
    -- This migration runs always (no guard) to provide monitoring
    RAISE NOTICE '=== V10001: Test Data Contract Check Starting ===';
    
    -- Check 1: Total test data count
    SELECT COUNT(*) INTO test_count 
    FROM customers 
    WHERE is_test_data = true;
    
    IF test_count > 50 THEN
        warning_level := 'WARNING';
        warning_messages := warning_messages || 
            format('- High test data count: %s (recommended max: 50)%s', test_count, E'\n');
        RAISE WARNING 'Test data count is high: % (recommended max: 50)', test_count;
    ELSIF test_count > 30 THEN
        RAISE NOTICE 'Test data count approaching limit: % (recommended max: 50)', test_count;
    ELSE
        RAISE NOTICE 'Test data count OK: %', test_count;
    END IF;
    
    -- Check 2: Unmarked test data (has prefix but is_test_data = false)
    SELECT COUNT(*) INTO unmarked_count
    FROM customers 
    WHERE (company_name LIKE '[TEST-%]%' OR company_name LIKE '[SEED]%')
      AND is_test_data = false;
    
    IF unmarked_count > 0 THEN
        warning_level := 'WARNING';
        warning_messages := warning_messages || 
            format('- Found %s unmarked test customers (prefix but is_test_data=false)%s', unmarked_count, E'\n');
        RAISE WARNING 'Found % customers with test prefix but is_test_data=false', unmarked_count;
    ELSE
        RAISE NOTICE 'All test-prefixed data properly marked: OK';
    END IF;
    
    -- Check 3: Test data without proper prefix
    SELECT COUNT(*) INTO no_prefix_count
    FROM customers 
    WHERE is_test_data = true
      AND company_name NOT LIKE '[TEST-%]%'
      AND company_name NOT LIKE '[SEED]%'
      AND company_name NOT LIKE '[DEV]%';
    
    IF no_prefix_count > 0 THEN
        RAISE NOTICE 'Found % test records without standard prefix (may be from builder)', no_prefix_count;
    END IF;
    
    -- Check 4: Old test data (potential cleanup candidates)
    SELECT COUNT(*) INTO old_test_count
    FROM customers 
    WHERE is_test_data = true
      AND created_at < NOW() - INTERVAL '7 days'
      AND customer_number NOT LIKE 'SEED-%';  -- SEEDs can be old
    
    IF old_test_count > 20 THEN
        warning_messages := warning_messages || 
            format('- %s test records older than 7 days (consider cleanup)%s', old_test_count, E'\n');
        RAISE NOTICE 'Old test data accumulating: % records older than 7 days', old_test_count;
    END IF;
    
    -- Check 5: Duplicate customer numbers (should never happen)
    SELECT COUNT(*) INTO duplicate_count
    FROM (
        SELECT customer_number, COUNT(*) as cnt
        FROM customers
        GROUP BY customer_number
        HAVING COUNT(*) > 1
    ) dups;
    
    IF duplicate_count > 0 THEN
        warning_level := 'CRITICAL';
        warning_messages := warning_messages || 
            format('- CRITICAL: Found %s duplicate customer numbers!%s', duplicate_count, E'\n');
        RAISE WARNING 'CRITICAL: Found % duplicate customer numbers!', duplicate_count;
    END IF;
    
    -- Summary Report
    RAISE NOTICE '=== V10001: Test Data Contract Check Complete ===';
    RAISE NOTICE 'Status: %', warning_level;
    RAISE NOTICE 'Test Data Statistics:';
    RAISE NOTICE '  - Total test customers: %', test_count;
    RAISE NOTICE '  - Unmarked with prefix: %', unmarked_count;
    RAISE NOTICE '  - Missing standard prefix: %', no_prefix_count;
    RAISE NOTICE '  - Older than 7 days: %', old_test_count;
    RAISE NOTICE '  - Duplicate customer numbers: %', duplicate_count;
    
    -- If there are warnings, output them all together
    IF warning_messages != '' THEN
        RAISE NOTICE E'\n=== WARNINGS SUMMARY ===\n%', warning_messages;
    END IF;
    
    -- Create or update a tracking record (if we have a monitoring table)
    IF to_regclass('public.migration_health_checks') IS NOT NULL THEN
        INSERT INTO migration_health_checks (
            migration_name,
            check_timestamp,
            status,
            test_count,
            warnings
        ) VALUES (
            'V10001',
            NOW(),
            warning_level,
            test_count,
            warning_messages
        );
    END IF;
    
    -- NOTE: We do NOT raise an exception - this is a monitoring migration
    -- It provides visibility but doesn't block the process
    
END $$;

-- Optional: Create a summary view for easy monitoring
CREATE OR REPLACE VIEW test_data_contract_status AS
SELECT 
    COUNT(*) FILTER (WHERE is_test_data = true) as total_test_data,
    COUNT(*) FILTER (WHERE is_test_data = true AND customer_number LIKE 'SEED-%') as seed_data,
    COUNT(*) FILTER (WHERE is_test_data = true AND customer_number NOT LIKE 'SEED-%') as dynamic_test_data,
    COUNT(*) FILTER (WHERE (company_name LIKE '[TEST-%]%' OR company_name LIKE '[SEED]%') AND is_test_data = false) as unmarked_test_data,
    COUNT(*) FILTER (WHERE is_test_data = true AND created_at < NOW() - INTERVAL '7 days') as old_test_data,
    CASE 
        WHEN COUNT(*) FILTER (WHERE is_test_data = true) > 50 THEN 'WARNING: Too many test records'
        WHEN COUNT(*) FILTER (WHERE (company_name LIKE '[TEST-%]%' OR company_name LIKE '[SEED]%') AND is_test_data = false) > 0 THEN 'WARNING: Unmarked test data found'
        ELSE 'OK: Test data within limits'
    END as contract_status
FROM customers;

COMMENT ON VIEW test_data_contract_status IS 'Quick status check for test data contract compliance. Use: SELECT * FROM test_data_contract_status;';