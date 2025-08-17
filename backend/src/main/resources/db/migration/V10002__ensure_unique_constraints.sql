-- V10002: Ensure Unique Constraints for Test Data Management
-- This migration ensures that critical unique constraints exist
-- to prevent duplicate key violations during test data creation

DO $$
BEGIN
    -- 1. Check and add unique constraint on customers.customer_number
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'customers_customer_number_uk'
    ) THEN
        -- First check if there's already a unique index/constraint with different name
        IF NOT EXISTS (
            SELECT 1 FROM pg_indexes 
            WHERE tablename = 'customers' 
            AND indexdef LIKE '%UNIQUE%customer_number%'
        ) THEN
            ALTER TABLE customers 
                ADD CONSTRAINT customers_customer_number_uk 
                UNIQUE (customer_number);
            RAISE NOTICE 'Added unique constraint on customers.customer_number';
        ELSE
            RAISE NOTICE 'Unique constraint on customers.customer_number already exists';
        END IF;
    ELSE
        RAISE NOTICE 'customers_customer_number_uk already exists';
    END IF;

    -- 2. Check and add unique constraint on permissions.code
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'permissions_code_uk'
    ) THEN
        -- Check if permissions table exists first
        IF EXISTS (
            SELECT 1 FROM information_schema.tables 
            WHERE table_name = 'permissions'
        ) THEN
            -- Check for existing unique constraint with different name
            IF NOT EXISTS (
                SELECT 1 FROM pg_indexes 
                WHERE tablename = 'permissions' 
                AND indexdef LIKE '%UNIQUE%code%'
            ) THEN
                ALTER TABLE permissions 
                    ADD CONSTRAINT permissions_code_uk 
                    UNIQUE (code);
                RAISE NOTICE 'Added unique constraint on permissions.code';
            ELSE
                RAISE NOTICE 'Unique constraint on permissions.code already exists';
            END IF;
        ELSE
            RAISE NOTICE 'Table permissions does not exist yet - skipping';
        END IF;
    ELSE
        RAISE NOTICE 'permissions_code_uk already exists';
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
        
        -- Mark existing test data
        UPDATE customers 
        SET is_test_data = TRUE
        WHERE company_name LIKE '[TEST%]%' 
           OR company_name LIKE '[SEED]%'
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