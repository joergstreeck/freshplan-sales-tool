-- ============================================================================
-- V10026: Opportunity-Lead-Customer Integration
-- ============================================================================
-- Purpose: Enable full Lead → Opportunity → Customer lifecycle
-- Sprint: 2.1.6.2 Phase 2 - DEV-SEED Opportunities Extension
-- Date: 2025-10-13
--
-- Enables:
--   1. Create Opportunity from Lead (lead_id → opportunity)
--   2. Convert Opportunity to Customer (opportunity → customer)
--   3. Create Opportunity for Customer (customer_id → opportunity)
--
-- Changes:
--   - Add lead_id column to opportunities table
--   - Add foreign key constraint
--   - Add index for performance
--   - Add check constraint (lead_id OR customer_id must exist)
--   - Add comments for documentation
-- ============================================================================

-- ============================================================================
-- 1. Add lead_id column
-- ============================================================================
ALTER TABLE opportunities
ADD COLUMN IF NOT EXISTS lead_id BIGINT;

-- ============================================================================
-- 2. Add foreign key constraint
-- ============================================================================
ALTER TABLE opportunities
ADD CONSTRAINT fk_opportunities_lead
FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE SET NULL;

-- ============================================================================
-- 3. Add index (partial - only where lead_id IS NOT NULL)
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_opportunities_lead_id
ON opportunities(lead_id)
WHERE lead_id IS NOT NULL;

-- ============================================================================
-- 4. Add check constraint
-- ============================================================================
-- Either lead_id OR customer_id must be set (or both during conversion)
-- Exception: NEW_LEAD stage can exist without either (unqualified lead)
ALTER TABLE opportunities
ADD CONSTRAINT chk_opportunity_has_source
CHECK (
    lead_id IS NOT NULL
    OR customer_id IS NOT NULL
    OR stage = 'NEW_LEAD'
);

-- ============================================================================
-- 5. Add column comments
-- ============================================================================
COMMENT ON COLUMN opportunities.lead_id IS
'Reference to originating lead. Set when opportunity created from qualified lead. NULL for customer-initiated opportunities or unqualified NEW_LEAD stage.';

-- ============================================================================
-- 6. Update existing opportunities (if any)
-- ============================================================================
-- Ensure existing opportunities pass new constraint
-- All existing opportunities should have customer_id set
UPDATE opportunities
SET lead_id = NULL
WHERE customer_id IS NOT NULL AND lead_id IS NULL;

-- ============================================================================
-- 7. Add composite index for lead-to-customer conversion queries
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_opportunities_lead_customer
ON opportunities(lead_id, customer_id, stage)
WHERE lead_id IS NOT NULL OR customer_id IS NOT NULL;

-- ============================================================================
-- END OF V10026
-- ============================================================================
-- Summary:
--   ✅ opportunities.lead_id column added
--   ✅ FK constraint to leads table
--   ✅ Partial indexes for performance
--   ✅ Check constraint ensures data integrity
--   ✅ Comments for documentation
--   ✅ Supports Lead → Opportunity → Customer workflow
-- ============================================================================
