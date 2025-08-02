-- V12: Add last_modified audit fields explicitly
-- Date: 2025-07-05
-- Author: Claude (FreshPlan Team)
-- Description: Add last_modified_by and last_modified_at fields that Hibernate expects

-- Add last_modified fields to customers table if they don't exist
DO $$ 
BEGIN
    -- Add last_modified_at if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customers' 
        AND column_name = 'last_modified_at'
    ) THEN
        ALTER TABLE customers ADD COLUMN last_modified_at TIMESTAMP;
        RAISE NOTICE 'Added column last_modified_at to customers table';
    END IF;
    
    -- Add last_modified_by if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customers' 
        AND column_name = 'last_modified_by'
    ) THEN
        ALTER TABLE customers ADD COLUMN last_modified_by VARCHAR(100);
        RAISE NOTICE 'Added column last_modified_by to customers table';
    END IF;
END $$;

-- Also add these fields to related tables if they exist
DO $$ 
BEGIN
    -- customer_contacts
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customer_contacts') THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'customer_contacts' 
            AND column_name = 'last_modified_at'
        ) THEN
            ALTER TABLE customer_contacts ADD COLUMN last_modified_at TIMESTAMP;
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'customer_contacts' 
            AND column_name = 'last_modified_by'
        ) THEN
            ALTER TABLE customer_contacts ADD COLUMN last_modified_by VARCHAR(100);
        END IF;
    END IF;
    
    -- customer_locations
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customer_locations') THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'customer_locations' 
            AND column_name = 'last_modified_at'
        ) THEN
            ALTER TABLE customer_locations ADD COLUMN last_modified_at TIMESTAMP;
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'customer_locations' 
            AND column_name = 'last_modified_by'
        ) THEN
            ALTER TABLE customer_locations ADD COLUMN last_modified_by VARCHAR(100);
        END IF;
    END IF;
END $$;