-- V10003: Create Test Data Dashboard View
-- This view provides monitoring and alerting for test data growth
-- Used by CI to ensure tests are properly cleaning up

CREATE OR REPLACE VIEW test_data_dashboard AS
WITH stats AS (
    SELECT 
        COUNT(*) as total_test_data,
        COUNT(*) FILTER (WHERE company_name LIKE '[SEED]%') as seed_data,
        COUNT(*) FILTER (WHERE company_name LIKE '[TEST-%') as dynamic_test_data,  -- Fixed: removed extra %
        COUNT(*) FILTER (WHERE company_name LIKE '[DEV]%') as dev_data,
        COUNT(*) FILTER (WHERE created_at > NOW() - INTERVAL '1 hour') as created_last_hour,
        COUNT(*) FILTER (WHERE created_at < NOW() - INTERVAL '7 days') as older_than_week
    FROM customers
    WHERE is_test_data = true  -- IMPORTANT: Only count marked test data!
)
SELECT 
    total_test_data,
    seed_data,
    dynamic_test_data,
    dev_data,
    created_last_hour,
    older_than_week,
    CASE 
        WHEN total_test_data > 50 THEN 'CRITICAL: Too many test records!'
        WHEN total_test_data > 30 THEN 'WARNING: Approaching limit'
        WHEN older_than_week > 10 THEN 'WARNING: Old test data needs cleanup'
        ELSE 'OK'
    END as status,
    CASE
        WHEN total_test_data > 50 THEN 3  -- Critical
        WHEN total_test_data > 30 THEN 2  -- Warning
        WHEN older_than_week > 10 THEN 2  -- Warning
        ELSE 1  -- OK
    END as severity_level
FROM stats;

-- Create a function to check test data health
CREATE OR REPLACE FUNCTION check_test_data_health()
RETURNS TABLE(
    check_name TEXT,
    result TEXT,
    details TEXT
) AS $$
BEGIN
    -- Check 1: Total test data count
    RETURN QUERY
    SELECT 
        'Total Test Data Count'::TEXT,
        CASE 
            WHEN COUNT(*) > 50 THEN 'FAIL'
            WHEN COUNT(*) > 30 THEN 'WARN'
            ELSE 'PASS'
        END::TEXT,
        FORMAT('%s test records (limit: 50)', COUNT(*))::TEXT
    FROM customers
    WHERE is_test_data = true;

    -- Check 2: Unmarked test data
    RETURN QUERY
    SELECT 
        'Unmarked Test Data'::TEXT,
        CASE 
            WHEN COUNT(*) > 0 THEN 'FAIL'
            ELSE 'PASS'
        END::TEXT,
        FORMAT('%s unmarked test records found', COUNT(*))::TEXT
    FROM customers
    WHERE (company_name LIKE '[TEST-%' OR company_name LIKE '[SEED]%')  -- Fixed LIKE pattern
      AND is_test_data = false;

    -- Check 3: Old test data
    RETURN QUERY
    SELECT 
        'Old Test Data'::TEXT,
        CASE 
            WHEN COUNT(*) > 10 THEN 'WARN'
            ELSE 'PASS'
        END::TEXT,
        FORMAT('%s test records older than 7 days', COUNT(*))::TEXT
    FROM customers
    WHERE is_test_data = true
      AND created_at < NOW() - INTERVAL '7 days';

    -- Check 4: Test data without prefix
    RETURN QUERY
    SELECT 
        'Test Data Naming Convention'::TEXT,
        CASE 
            WHEN COUNT(*) > 0 THEN 'WARN'
            ELSE 'PASS'
        END::TEXT,
        FORMAT('%s test records without proper [TEST]/[SEED] prefix', COUNT(*))::TEXT
    FROM customers
    WHERE is_test_data = true
      AND company_name NOT LIKE '[TEST-%'  -- Fixed: removed extra %
      AND company_name NOT LIKE '[SEED]%'
      AND company_name NOT LIKE '[DEV]%';
END;
$$ LANGUAGE plpgsql;

-- Usage examples:
COMMENT ON VIEW test_data_dashboard IS 'Monitor test data health and growth. Use: SELECT * FROM test_data_dashboard;';
COMMENT ON FUNCTION check_test_data_health() IS 'Detailed health checks for test data. Use: SELECT * FROM check_test_data_health();';