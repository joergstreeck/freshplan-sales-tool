-- =========================================
-- M4 Opportunity Pipeline - Main Table
-- =========================================
-- 
-- Creates the opportunities table for the sales pipeline management.
-- Each opportunity represents a sales chance that goes through different stages.
--
-- @author FreshPlan Team
-- @since 2.0.0
-- @feature FC-002 M4 Opportunity Pipeline
-- =========================================

-- Main opportunities table
CREATE TABLE opportunities (
    -- Primary key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Basic information
    name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Pipeline stage (enum stored as string)
    stage VARCHAR(50) NOT NULL 
        CHECK (stage IN (
            'NEW_LEAD', 
            'QUALIFICATION', 
            'NEEDS_ANALYSIS', 
            'PROPOSAL', 
            'NEGOTIATION', 
            'CLOSED_WON', 
            'CLOSED_LOST'
        )),
    
    -- Relationships
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    assigned_to UUID REFERENCES app_user(id) ON DELETE SET NULL,
    
    -- Business fields
    expected_value DECIMAL(19,2) CHECK (expected_value >= 0),
    expected_close_date DATE,
    probability INTEGER CHECK (probability >= 0 AND probability <= 100),
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    stage_changed_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Soft delete support (for future)
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- Performance indexes
CREATE INDEX idx_opportunities_stage ON opportunities(stage);
CREATE INDEX idx_opportunities_assigned_to ON opportunities(assigned_to);
CREATE INDEX idx_opportunities_customer ON opportunities(customer_id);
CREATE INDEX idx_opportunities_stage_changed ON opportunities(stage_changed_at);
CREATE INDEX idx_opportunities_expected_close ON opportunities(expected_close_date);
CREATE INDEX idx_opportunities_created ON opportunities(created_at);
CREATE INDEX idx_opportunities_soft_delete ON opportunities(is_deleted) WHERE is_deleted = FALSE;

-- Composite indexes for common queries
CREATE INDEX idx_opportunities_active_stage ON opportunities(stage, stage_changed_at) 
    WHERE is_deleted = FALSE AND stage NOT IN ('CLOSED_WON', 'CLOSED_LOST');

CREATE INDEX idx_opportunities_forecast ON opportunities(stage, expected_value, probability) 
    WHERE is_deleted = FALSE AND stage NOT IN ('CLOSED_WON', 'CLOSED_LOST');

-- Comments for documentation
COMMENT ON TABLE opportunities IS 'Sales opportunities in the pipeline system';
COMMENT ON COLUMN opportunities.stage IS 'Current stage in the sales pipeline process';
COMMENT ON COLUMN opportunities.probability IS 'Win probability percentage (0-100)';
COMMENT ON COLUMN opportunities.expected_value IS 'Expected revenue in EUR';
COMMENT ON COLUMN opportunities.stage_changed_at IS 'Timestamp when stage was last changed';

-- Test opportunities will be created through the backend service
-- No initial data insertion to avoid constraint violations