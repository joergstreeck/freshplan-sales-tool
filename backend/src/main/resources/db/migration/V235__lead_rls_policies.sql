-- V235: Lead RLS Policies - Row Level Security for Lead Protection
-- Sprint 2.1 PR #104: Fail-closed security implementation

-- Enable RLS on leads table
ALTER TABLE leads ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if any
DROP POLICY IF EXISTS leads_select_policy ON leads;
DROP POLICY IF EXISTS leads_insert_policy ON leads;
DROP POLICY IF EXISTS leads_update_policy ON leads;
DROP POLICY IF EXISTS leads_delete_policy ON leads;

-- SELECT: Read access for owner, collaborators, managers, admins
CREATE POLICY leads_select_policy ON leads
FOR SELECT
USING (
    -- Owner can see their own leads
    owner_user_id = current_setting('app.current_user', true)
    -- Collaborators can see leads they're assigned to
    OR current_setting('app.current_user', true) = ANY(collaborator_user_ids)
    -- Managers and admins can see all leads
    OR current_setting('app.current_role', true) IN ('MANAGER', 'ADMIN')
    -- Deleted leads are hidden from non-admins
    AND (status != 'DELETED' OR current_setting('app.current_role', true) = 'ADMIN')
);

-- INSERT: Users can create leads (become automatic owner)
CREATE POLICY leads_insert_policy ON leads
FOR INSERT
WITH CHECK (
    -- User must be authenticated
    current_setting('app.current_user', true) IS NOT NULL
    -- Owner must be the current user on insert
    AND owner_user_id = current_setting('app.current_user', true)
);

-- UPDATE: Only owner or admin can update
CREATE POLICY leads_update_policy ON leads
FOR UPDATE
USING (
    -- Owner can update their leads
    owner_user_id = current_setting('app.current_user', true)
    -- Admins can update any lead
    OR current_setting('app.current_role', true) = 'ADMIN'
)
WITH CHECK (
    -- Cannot change owner unless admin
    (owner_user_id = current_setting('app.current_user', true)
     OR current_setting('app.current_role', true) = 'ADMIN')
);

-- DELETE (soft delete via status): Only owner or admin
CREATE POLICY leads_delete_policy ON leads
FOR DELETE
USING (
    -- Owner can delete their leads
    owner_user_id = current_setting('app.current_user', true)
    -- Admins can delete any lead
    OR current_setting('app.current_role', true) = 'ADMIN'
);

-- Grant necessary permissions (only if role exists)
-- Note: This would normally be handled by infrastructure setup
-- For test environment, we skip this if role doesn't exist
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'application_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON leads TO application_user;
    END IF;
END $$;

-- Create index for RLS performance
CREATE INDEX IF NOT EXISTS idx_leads_owner_collaborators
ON leads(owner_user_id, collaborator_user_ids)
WHERE status != 'DELETED';

COMMENT ON POLICY leads_select_policy ON leads IS 'User-based lead visibility: owner, collaborators, or admin';
COMMENT ON POLICY leads_insert_policy ON leads IS 'Users become automatic owner on lead creation';
COMMENT ON POLICY leads_update_policy ON leads IS 'Owner or admin can update leads';
COMMENT ON POLICY leads_delete_policy ON leads IS 'Owner or admin can delete leads (soft delete via status)';