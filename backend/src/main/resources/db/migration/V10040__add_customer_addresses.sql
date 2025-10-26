-- Migration V10040: Add billing and delivery addresses to customers
-- Sprint 2.1.7.2 D11 - Phase 3: Multi-Location Details

-- Add billing_address column for primary billing address
ALTER TABLE customers
  ADD COLUMN billing_address VARCHAR(500);

-- Add delivery_addresses column for array of delivery addresses (JSONB for flexibility)
ALTER TABLE customers
  ADD COLUMN delivery_addresses JSONB DEFAULT '[]'::jsonb;

-- Add column comments for documentation
COMMENT ON COLUMN customers.billing_address IS 'Primary billing address for invoicing (single address as formatted string)';
COMMENT ON COLUMN customers.delivery_addresses IS 'JSON array of delivery addresses for multi-location customers [{street, city, zip, country}, ...]';

-- Create index on delivery_addresses for efficient querying
CREATE INDEX idx_customers_delivery_addresses ON customers USING GIN (delivery_addresses);
