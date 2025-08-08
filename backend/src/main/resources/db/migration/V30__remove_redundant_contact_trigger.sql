-- Remove redundant trigger for updated_at column
-- The Contact entity already uses @UpdateTimestamp annotation in Hibernate
-- Having both causes potential inconsistencies

-- Drop the trigger
DROP TRIGGER IF EXISTS update_customer_contacts_updated_at ON customer_contacts;

-- Drop the function if it's not used by other tables
-- Check if function is used elsewhere before dropping
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_trigger t
        JOIN pg_proc p ON t.tgfoid = p.oid
        WHERE p.proname = 'update_updated_at_column'
        AND t.tgname != 'update_customer_contacts_updated_at'
    ) THEN
        DROP FUNCTION IF EXISTS update_updated_at_column();
    END IF;
END$$;

-- Add comment to document that updated_at is managed by Hibernate
COMMENT ON COLUMN customer_contacts.updated_at IS 'Automatically updated by Hibernate @UpdateTimestamp';