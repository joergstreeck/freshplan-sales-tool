-- Fix primary_financing check constraint to match FinancingType enum values
-- The enum has: PRIVATE, PUBLIC, MIXED
-- But the constraint was checking for: CASH, CREDIT, LEASING, MIXED

-- Drop the old constraint
ALTER TABLE customers 
DROP CONSTRAINT IF EXISTS chk_customers_primary_financing;

-- Add the correct constraint matching the FinancingType enum
ALTER TABLE customers 
ADD CONSTRAINT chk_customers_primary_financing CHECK (
    primary_financing IS NULL OR 
    primary_financing IN ('PRIVATE', 'PUBLIC', 'MIXED')
);

-- Update comment to reflect the correct values
COMMENT ON COLUMN customers.primary_financing IS 'Primary financing type: PRIVATE, PUBLIC, or MIXED (Sprint 2)';