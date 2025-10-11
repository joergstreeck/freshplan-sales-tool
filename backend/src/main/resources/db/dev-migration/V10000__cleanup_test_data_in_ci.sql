-- ============================================================================
-- V10000: CI-only Conditional Test Data Cleanup
-- ============================================================================
-- Purpose: Clean up excessive test data in CI environments
-- Only runs when test data exceeds threshold (safety mechanism)
-- Uses is_test_data flag for safe identification
-- ============================================================================

DO $$
DECLARE
    test_count INTEGER;
    deleted_count INTEGER;
    threshold INTEGER := 100;  -- Hard cleanup threshold
    soft_threshold INTEGER := 50;  -- Soft cleanup threshold
    lock_acquired BOOLEAN;
    cleanup_mode TEXT := 'NONE';  -- NONE, SOFT, or HARD
    time_window INTERVAL;
BEGIN
    -- Try to acquire advisory lock to prevent parallel executions
    SELECT pg_try_advisory_lock(hashtext('V10000.cleanup')::bigint) INTO lock_acquired;
    
    IF NOT lock_acquired THEN
        RAISE NOTICE 'V10000: Skipped (another cleanup in progress)';
        RETURN;
    END IF;
    
    -- Guard: Only run in CI/Test environments
    IF current_setting('ci.build', true) <> 'true' THEN
        RAISE NOTICE 'V10000 cleanup skipped (not CI, ci.build=%)', current_setting('ci.build', true);
        PERFORM pg_advisory_unlock(hashtext('V10000.cleanup')::bigint);
        RETURN;
    END IF;
    
    -- Count current test data (excluding SEED data)
    SELECT COUNT(*) INTO test_count 
    FROM customers 
    WHERE is_test_data = true
      AND customer_number NOT LIKE 'SEED-%';
    
    RAISE NOTICE 'V10000: Found % test data records (threshold: %)', test_count, threshold;
    
    -- Two-stage cleanup strategy
    IF test_count > threshold THEN
        -- HARD CLEANUP: Threshold exceeded - remove ALL non-seed test data
        RAISE NOTICE 'V10000: THRESHOLD EXCEEDED (%/%) - Hard cleanup triggered', test_count, threshold;
        RAISE WARNING 'Removing ALL non-seed test customers to restore stability';
        cleanup_mode := 'HARD';
        time_window := NULL;  -- No time restriction for hard cleanup
        
    ELSIF test_count > soft_threshold THEN
        -- SOFT CLEANUP: Moderate amount - clean older data (90 minutes)
        RAISE NOTICE 'V10000: Moderate test data (%) - Soft cleanup of data > 90 minutes', test_count;
        cleanup_mode := 'SOFT';
        time_window := INTERVAL '90 minutes';
        
    ELSE
        -- NO CLEANUP: Within acceptable limits
        RAISE NOTICE 'V10000: Test data within limits (%) - no cleanup needed', test_count;
        PERFORM pg_advisory_unlock(hashtext('V10000.cleanup')::bigint);
        RETURN;
    END IF;
    
    -- CLEANUP IN CORRECT ORDER (respecting foreign keys)
    
    -- 1. Contact Interactions (if table exists)
    IF to_regclass('public.contact_interactions') IS NOT NULL THEN
        DELETE FROM contact_interactions ci
        USING customer_contacts cc, customers c
        WHERE ci.contact_id = cc.id
          AND cc.customer_id = c.id
          AND c.is_test_data = true
          AND c.customer_number NOT LIKE 'SEED-%'
          AND (cleanup_mode = 'HARD' OR 
               (c.created_at IS NULL OR c.created_at < NOW() - time_window));
        GET DIAGNOSTICS deleted_count = ROW_COUNT;
        IF deleted_count > 0 THEN
            RAISE NOTICE 'Deleted % contact_interactions (%)', deleted_count, cleanup_mode;
        END IF;
    END IF;
    
    -- 2. Customer Contacts
    IF to_regclass('public.customer_contacts') IS NOT NULL THEN
        DELETE FROM customer_contacts cc
        USING customers c
        WHERE cc.customer_id = c.id
          AND c.is_test_data = true
          AND c.customer_number NOT LIKE 'SEED-%'
          AND (cleanup_mode = 'HARD' OR 
               (c.created_at IS NULL OR c.created_at < NOW() - time_window));
        GET DIAGNOSTICS deleted_count = ROW_COUNT;
        IF deleted_count > 0 THEN
            RAISE NOTICE 'Deleted % old customer_contacts', deleted_count;
        END IF;
    END IF;
    
    -- 3. Timeline Events
    IF to_regclass('public.customer_timeline_events') IS NOT NULL THEN
        DELETE FROM customer_timeline_events cte
        USING customers c
        WHERE cte.customer_id = c.id
          AND c.is_test_data = true
          AND c.customer_number NOT LIKE 'SEED-%'
          AND (cleanup_mode = 'HARD' OR 
               (c.created_at IS NULL OR c.created_at < NOW() - time_window));
        GET DIAGNOSTICS deleted_count = ROW_COUNT;
        IF deleted_count > 0 THEN
            RAISE NOTICE 'Deleted % old timeline_events', deleted_count;
        END IF;
    END IF;
    
    -- 4. Opportunities
    IF to_regclass('public.opportunities') IS NOT NULL THEN
        DELETE FROM opportunities o
        USING customers c
        WHERE o.customer_id = c.id
          AND c.is_test_data = true
          AND c.customer_number NOT LIKE 'SEED-%'
          AND (cleanup_mode = 'HARD' OR 
               (c.created_at IS NULL OR c.created_at < NOW() - time_window));
        GET DIAGNOSTICS deleted_count = ROW_COUNT;
        IF deleted_count > 0 THEN
            RAISE NOTICE 'Deleted % old opportunities', deleted_count;
        END IF;
    END IF;
    
    -- 5. Audit Trail (only for test customers)
    IF to_regclass('public.audit_trail') IS NOT NULL THEN
        -- Check if entity_type column exists
        PERFORM 1 FROM information_schema.columns
        WHERE table_schema='public' 
          AND table_name='audit_trail' 
          AND column_name='entity_type';
          
        IF FOUND THEN
            DELETE FROM audit_trail a
            USING customers c
            WHERE a.entity_type = 'CUSTOMER'
              AND a.entity_id = c.id
              AND c.is_test_data = true
              AND c.customer_number NOT LIKE 'SEED-%'
              AND (cleanup_mode = 'HARD' OR 
                   (c.created_at IS NULL OR c.created_at < NOW() - time_window));
            GET DIAGNOSTICS deleted_count = ROW_COUNT;
            IF deleted_count > 0 THEN
                RAISE NOTICE 'Deleted % old audit_trail entries', deleted_count;
            END IF;
        END IF;
    END IF;
    
    -- 6. Finally: Delete test customers based on cleanup mode
    DELETE FROM customers 
    WHERE is_test_data = true
      AND customer_number NOT LIKE 'SEED-%'  -- Never delete SEED data
      AND (cleanup_mode = 'HARD' OR 
           (created_at IS NULL OR created_at < NOW() - time_window));
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    IF deleted_count > 0 THEN
        IF cleanup_mode = 'HARD' THEN
            RAISE NOTICE 'V10000 HARD cleanup completed - removed % test customers (threshold exceeded)', deleted_count;
        ELSE
            RAISE NOTICE 'V10000 SOFT cleanup completed - removed % test customers older than 90 minutes', deleted_count;
        END IF;
    ELSE
        RAISE NOTICE 'V10000 cleanup completed - no test data to remove';
    END IF;
    
    -- Final count
    SELECT COUNT(*) INTO test_count 
    FROM customers 
    WHERE is_test_data = true;
    
    RAISE NOTICE 'V10000: Test data after cleanup: % records (SEEDs: 20, Dynamic: %)', 
                 test_count, test_count - 20;
    
    -- Release advisory lock
    PERFORM pg_advisory_unlock(hashtext('V10000.cleanup')::bigint);
    
END $$;