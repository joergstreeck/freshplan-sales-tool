-- V123: Make all audit fields nullable for initial creation
-- Date: 2025-07-05
-- Author: Claude (FreshPlan Team)
-- Description: Fix all NOT NULL constraints on update/audit fields

-- First, let's see what columns actually exist
DO $$ 
DECLARE
    col RECORD;
BEGIN
    -- Loop through all columns that might be audit fields
    FOR col IN 
        SELECT column_name, is_nullable
        FROM information_schema.columns
        WHERE table_schema = 'public' 
        AND table_name = 'customers'
        AND column_name IN (
            'updated_at', 'updated_by',
            'last_modified_at', 'last_modified_by',
            'modified_at', 'modified_by',
            'last_updated_at', 'last_updated_by'
        )
        AND is_nullable = 'NO'
    LOOP
        EXECUTE format('ALTER TABLE customers ALTER COLUMN %I DROP NOT NULL', col.column_name);
        RAISE NOTICE 'Made column % nullable', col.column_name;
    END LOOP;
END $$;

-- Also make these fields nullable in related tables (if they exist and have these columns)
DO $$ 
BEGIN
    -- customer_contacts
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer_contacts' 
        AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE customer_contacts ALTER COLUMN updated_at DROP NOT NULL;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer_contacts' 
        AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE customer_contacts ALTER COLUMN updated_by DROP NOT NULL;
    END IF;

    -- customer_locations
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer_locations' 
        AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE customer_locations ALTER COLUMN updated_at DROP NOT NULL;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer_locations' 
        AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE customer_locations ALTER COLUMN updated_by DROP NOT NULL;
    END IF;
END $$;