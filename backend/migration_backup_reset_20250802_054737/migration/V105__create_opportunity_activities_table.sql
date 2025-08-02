-- =========================================
-- M4 Opportunity Pipeline - Activities Table
-- =========================================
-- 
-- Creates the opportunity_activities table for tracking all activities
-- related to opportunities (calls, emails, meetings, notes, etc.).
-- Provides full audit trail and activity history.
--
-- @author FreshPlan Team
-- @since 2.0.0
-- @feature FC-002 M4 Opportunity Pipeline
-- =========================================

-- Opportunity activities table
CREATE TABLE opportunity_activities (
    -- Primary key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Relationships
    opportunity_id UUID NOT NULL REFERENCES opportunities(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES app_user(id) ON DELETE SET NULL,
    
    -- Activity details
    activity_type VARCHAR(50) NOT NULL 
        CHECK (activity_type IN (
            'CALL',
            'EMAIL', 
            'MEETING',
            'NOTE',
            'TASK',
            'PROPOSAL_SENT',
            'STAGE_CHANGED',
            'CALCULATOR_USED',
            'DOCUMENT_SENT',
            'FOLLOW_UP'
        )),
    
    title VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Scheduling
    scheduled_date TIMESTAMP,
    
    -- Completion tracking
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Performance indexes
CREATE INDEX idx_opportunity_activities_opportunity ON opportunity_activities(opportunity_id);
CREATE INDEX idx_opportunity_activities_created_by ON opportunity_activities(created_by);
CREATE INDEX idx_opportunity_activities_type ON opportunity_activities(activity_type);
CREATE INDEX idx_opportunity_activities_created ON opportunity_activities(created_at);
CREATE INDEX idx_opportunity_activities_scheduled ON opportunity_activities(scheduled_date);
CREATE INDEX idx_opportunity_activities_completed ON opportunity_activities(completed, completed_at);

-- Composite indexes for common queries
CREATE INDEX idx_opportunity_activities_timeline ON opportunity_activities(opportunity_id, created_at DESC);
CREATE INDEX idx_opportunity_activities_todo ON opportunity_activities(created_by, completed, scheduled_date) 
    WHERE completed = FALSE AND scheduled_date IS NOT NULL;

-- Comments for documentation
COMMENT ON TABLE opportunity_activities IS 'Activity log and task management for opportunities';
COMMENT ON COLUMN opportunity_activities.activity_type IS 'Type of activity performed';
COMMENT ON COLUMN opportunity_activities.scheduled_date IS 'When the activity is scheduled (for tasks)';
COMMENT ON COLUMN opportunity_activities.completed IS 'Whether the activity/task is completed';

-- Trigger to automatically set completed_at when completed changes to true
CREATE OR REPLACE FUNCTION set_completed_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.completed = TRUE AND OLD.completed = FALSE THEN
        NEW.completed_at = CURRENT_TIMESTAMP;
    ELSIF NEW.completed = FALSE THEN
        NEW.completed_at = NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_completed_at
    BEFORE UPDATE ON opportunity_activities
    FOR EACH ROW
    EXECUTE FUNCTION set_completed_at();

-- Test activities will be created through the backend service
-- No initial data insertion to avoid constraint violations