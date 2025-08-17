-- V10002: Ensure Unique Constraints for Test Data Management
-- This migration ensures that critical unique constraints exist
-- to prevent duplicate key violations during test data creation

DO $$
DECLARE
    has_unique_constraint BOOLEAN;
BEGIN
    -- 1. Check and add unique constraint on customers.customer_number (robust check)
    -- Check if ANY unique constraint exists on customer_number column
    SELECT EXISTS (
        SELECT 1
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ANY(con.conkey)
        WHERE rel.relname = 'customers'
          AND con.contype = 'u'  -- unique constraint
        GROUP BY con.oid
        HAVING array_agg(att.attname ORDER BY att.attnum) = ARRAY['customer_number']::text[]
    ) INTO has_unique_constraint;
    
    IF NOT has_unique_constraint THEN
        -- Also check for unique indexes
        IF NOT EXISTS (
            SELECT 1 FROM pg_indexes 
            WHERE tablename = 'customers' 
            AND indexdef LIKE '%UNIQUE%customer_number%'
        ) THEN
            -- Clean up duplicates (only in CI/Test environments)
            IF current_setting('ci.build', true) = 'true' THEN
                -- In CI: Clean up test data duplicates
                WITH duplicates AS (
                    SELECT customer_number, COUNT(*) as cnt
                    FROM customers
                    WHERE is_test_data = true  -- Only test data!
                    GROUP BY customer_number
                    HAVING COUNT(*) > 1
                )
                DELETE FROM customers c
                USING duplicates d
                WHERE c.customer_number = d.customer_number
                  AND c.is_test_data = true  -- Only delete test data!
                  AND c.id NOT IN (
                      SELECT MIN(id) FROM customers 
                      WHERE customer_number = d.customer_number
                        AND is_test_data = true
                  );
                RAISE NOTICE 'Cleaned up duplicate test customers in CI environment';
            ELSE
                -- Outside CI: Check for duplicates and abort if found
                IF EXISTS (
                    SELECT 1 FROM customers
                    GROUP BY customer_number
                    HAVING COUNT(*) > 1
                ) THEN
                    RAISE EXCEPTION 'Duplicate customer_number detected outside CI - manual cleanup required';
                END IF;
            END IF;
            
            ALTER TABLE customers 
                ADD CONSTRAINT customers_customer_number_uk 
                UNIQUE (customer_number);
            RAISE NOTICE 'Added unique constraint on customers.customer_number';
        ELSE
            RAISE NOTICE 'Unique index on customers.customer_number already exists';
        END IF;
    ELSE
        RAISE NOTICE 'Unique constraint on customers.customer_number already exists';
    END IF;

    -- 2. Check and add unique constraint on permissions (adaptive for code vs permission_code)
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'permissions') THEN
        DECLARE
            col_name TEXT;
            has_constraint BOOLEAN;
        BEGIN
            -- Determine which column exists: 'code' or 'permission_code'
            SELECT CASE
                WHEN EXISTS (SELECT 1 FROM information_schema.columns 
                            WHERE table_name = 'permissions' AND column_name = 'code') THEN 'code'
                WHEN EXISTS (SELECT 1 FROM information_schema.columns 
                            WHERE table_name = 'permissions' AND column_name = 'permission_code') THEN 'permission_code'
                ELSE NULL
            END INTO col_name;
            
            IF col_name IS NOT NULL THEN
                -- Check if any unique constraint exists on this column
                SELECT EXISTS (
                    SELECT 1 FROM pg_constraint con
                    JOIN pg_class rel ON rel.oid = con.conrelid
                    JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ANY(con.conkey)
                    WHERE rel.relname = 'permissions'
                      AND con.contype = 'u'
                      AND att.attname = col_name
                ) INTO has_constraint;
                
                IF NOT has_constraint THEN
                    -- Create unique constraint on the existing column
                    EXECUTE format('ALTER TABLE permissions ADD CONSTRAINT permissions_%s_uk UNIQUE (%I)', 
                                   col_name, col_name);
                    RAISE NOTICE 'Added unique constraint on permissions.%', col_name;
                ELSE
                    RAISE NOTICE 'Unique constraint on permissions.% already exists', col_name;
                END IF;
            ELSE
                RAISE NOTICE 'No code or permission_code column found in permissions table';
            END IF;
        END;
    ELSE
        RAISE NOTICE 'Table permissions does not exist yet - skipping';
    END IF;

    -- 3. Add is_test_data column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customers' 
        AND column_name = 'is_test_data'
    ) THEN
        ALTER TABLE customers 
            ADD COLUMN is_test_data BOOLEAN DEFAULT FALSE;
        RAISE NOTICE 'Added is_test_data column to customers table';
        
        -- Mark existing test data (corrected LIKE patterns)
        UPDATE customers 
        SET is_test_data = TRUE
        WHERE company_name LIKE '[TEST-%' -- Matches [TEST-XXX]
           OR company_name LIKE '[SEED]%'  -- Matches [SEED]
           OR customer_number LIKE 'TEST-%'
           OR customer_number LIKE 'SEED-%';
    ELSE
        RAISE NOTICE 'Column is_test_data already exists in customers';
    END IF;

    -- 4. Add is_test_data to other relevant tables
    -- Opportunities
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'opportunities') THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'opportunities' 
            AND column_name = 'is_test_data'
        ) THEN
            ALTER TABLE opportunities 
                ADD COLUMN is_test_data BOOLEAN DEFAULT FALSE;
            RAISE NOTICE 'Added is_test_data column to opportunities table';
        END IF;
    END IF;

    -- Customer Contacts
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customer_contacts') THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'customer_contacts' 
            AND column_name = 'is_test_data'
        ) THEN
            ALTER TABLE customer_contacts 
                ADD COLUMN is_test_data BOOLEAN DEFAULT FALSE;
            RAISE NOTICE 'Added is_test_data column to customer_contacts table';
        END IF;
    END IF;

    -- Audit Trail
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_trail') THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'audit_trail' 
            AND column_name = 'is_test_data'
        ) THEN
            ALTER TABLE audit_trail 
                ADD COLUMN is_test_data BOOLEAN DEFAULT FALSE;
            RAISE NOTICE 'Added is_test_data column to audit_trail table';
        END IF;
    END IF;

    -- 5. Set is_test_data columns to NOT NULL (after all updates)
    -- This ensures no NULL values can sneak in
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customers' 
        AND column_name = 'is_test_data'
        AND is_nullable = 'YES'
    ) THEN
        -- First update any NULL values to false
        UPDATE customers SET is_test_data = FALSE WHERE is_test_data IS NULL;
        ALTER TABLE customers ALTER COLUMN is_test_data SET NOT NULL;
        RAISE NOTICE 'Set is_test_data to NOT NULL in customers';
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'opportunities' 
        AND column_name = 'is_test_data'
        AND is_nullable = 'YES'
    ) THEN
        UPDATE opportunities SET is_test_data = FALSE WHERE is_test_data IS NULL;
        ALTER TABLE opportunities ALTER COLUMN is_test_data SET NOT NULL;
        RAISE NOTICE 'Set is_test_data to NOT NULL in opportunities';
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer_contacts' 
        AND column_name = 'is_test_data'
        AND is_nullable = 'YES'
    ) THEN
        UPDATE customer_contacts SET is_test_data = FALSE WHERE is_test_data IS NULL;
        ALTER TABLE customer_contacts ALTER COLUMN is_test_data SET NOT NULL;
        RAISE NOTICE 'Set is_test_data to NOT NULL in customer_contacts';
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'audit_trail' 
        AND column_name = 'is_test_data'
        AND is_nullable = 'YES'
    ) THEN
        UPDATE audit_trail SET is_test_data = FALSE WHERE is_test_data IS NULL;
        ALTER TABLE audit_trail ALTER COLUMN is_test_data SET NOT NULL;
        RAISE NOTICE 'Set is_test_data to NOT NULL in audit_trail';
    END IF;

END $$;

-- Create index for better performance when filtering test data
CREATE INDEX IF NOT EXISTS idx_customers_is_test_data 
    ON customers(is_test_data) 
    WHERE is_test_data = true;

CREATE INDEX IF NOT EXISTS idx_opportunities_is_test_data 
    ON opportunities(is_test_data) 
    WHERE is_test_data = true;

CREATE INDEX IF NOT EXISTS idx_customer_contacts_is_test_data 
    ON customer_contacts(is_test_data) 
    WHERE is_test_data = true;