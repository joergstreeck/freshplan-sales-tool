-- V242: RLS Fail-Closed Security Policies
-- Ensures that all RLS policies fail-closed when GUC variables are not set
-- This is a critical security enhancement to prevent data leakage

-- Update existing RLS policies to fail-closed pattern
-- Pattern: current_setting('app.user_context', true) IS NOT NULL AND ...

-- 1. Update leads RLS policies to fail-closed
-- Update SELECT policy
ALTER POLICY leads_select_policy ON leads
USING (
    current_setting('app.user_context', true) IS NOT NULL AND (
        owner_user_id = current_setting('app.user_context', true) OR
        current_setting('app.user_context', true) = ANY(collaborator_user_ids) OR
        current_setting('app.role_context', true) IN ('ADMIN', 'SYSTEM', 'MANAGER')
    )
);

-- Update UPDATE policy
ALTER POLICY leads_update_policy ON leads
USING (
    current_setting('app.user_context', true) IS NOT NULL AND (
        owner_user_id = current_setting('app.user_context', true) OR
        current_setting('app.role_context', true) IN ('ADMIN', 'SYSTEM')
    )
);

-- Update DELETE policy
ALTER POLICY leads_delete_policy ON leads
USING (
    current_setting('app.user_context', true) IS NOT NULL AND (
        owner_user_id = current_setting('app.user_context', true) OR
        current_setting('app.role_context', true) IN ('ADMIN', 'SYSTEM')
    )
);

-- 2. Update lead_activities RLS policy to fail-closed
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_policies
        WHERE tablename = 'lead_activities'
        AND policyname = 'lead_activities_access_policy'
    ) THEN
        ALTER POLICY lead_activities_access_policy ON lead_activities
        USING (
            current_setting('app.user_context', true) IS NOT NULL AND (
                EXISTS (
                    SELECT 1 FROM leads
                    WHERE leads.id = lead_activities.lead_id
                    AND (
                        leads.owner_user_id = current_setting('app.user_context', true) OR
                        current_setting('app.user_context', true) = ANY(leads.collaborator_user_ids) OR
                        current_setting('app.role_context', true) IN ('ADMIN', 'SYSTEM')
                    )
                )
            )
        );
    END IF;
END $$;

-- 3. Create helper function to check RLS context
CREATE OR REPLACE FUNCTION check_rls_context()
RETURNS TABLE (
    user_context TEXT,
    role_context TEXT,
    tenant_context TEXT,
    territory_context TEXT,
    policies_active BOOLEAN
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT
        current_setting('app.user_context', true)::TEXT,
        current_setting('app.role_context', true)::TEXT,
        current_setting('app.tenant_id', true)::TEXT,
        current_setting('app.territory_context', true)::TEXT,
        EXISTS(SELECT 1 FROM pg_policies WHERE tablename = 'leads')::BOOLEAN;
END;
$$;

-- Grant execute to application role (if it exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'freshplan') THEN
        EXECUTE 'GRANT EXECUTE ON FUNCTION check_rls_context() TO freshplan';
    END IF;
END $$;