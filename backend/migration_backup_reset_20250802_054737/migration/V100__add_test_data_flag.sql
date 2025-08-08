-- Add is_test_data flag to all relevant tables for clean test data management
-- This allows us to easily identify and remove test data before production

-- Add flag to customers table
ALTER TABLE customers ADD COLUMN IF NOT EXISTS is_test_data BOOLEAN DEFAULT FALSE;

-- Add flag to customer_timeline_events table
ALTER TABLE customer_timeline_events ADD COLUMN IF NOT EXISTS is_test_data BOOLEAN DEFAULT FALSE;

-- Add flag to customer_contacts table
ALTER TABLE customer_contacts ADD COLUMN IF NOT EXISTS is_test_data BOOLEAN DEFAULT FALSE;

-- Add flag to customer_locations table
ALTER TABLE customer_locations ADD COLUMN IF NOT EXISTS is_test_data BOOLEAN DEFAULT FALSE;

-- Create indexes for efficient test data queries
CREATE INDEX IF NOT EXISTS idx_customers_test_data ON customers(is_test_data) WHERE is_test_data = TRUE;
CREATE INDEX IF NOT EXISTS idx_timeline_events_test_data ON customer_timeline_events(is_test_data) WHERE is_test_data = TRUE;
CREATE INDEX IF NOT EXISTS idx_contacts_test_data ON customer_contacts(is_test_data) WHERE is_test_data = TRUE;
CREATE INDEX IF NOT EXISTS idx_locations_test_data ON customer_locations(is_test_data) WHERE is_test_data = TRUE;

-- Add comments for documentation
COMMENT ON COLUMN customers.is_test_data IS 'Flag to identify test data that should not go to production';
COMMENT ON COLUMN customer_timeline_events.is_test_data IS 'Flag to identify test data that should not go to production';
COMMENT ON COLUMN customer_contacts.is_test_data IS 'Flag to identify test data that should not go to production';
COMMENT ON COLUMN customer_locations.is_test_data IS 'Flag to identify test data that should not go to production';