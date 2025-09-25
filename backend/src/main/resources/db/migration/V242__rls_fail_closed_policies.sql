-- V242: RLS Fail-Closed Security Policies
-- Ensures that all RLS policies fail-closed when GUC variables are not set
-- This is a critical security enhancement to prevent data leakage

-- Update existing RLS policies to fail-closed pattern
-- Pattern: current_setting('app.current_user', true) IS NOT NULL AND ...

-- 1. Update leads RLS policy to fail-closed
ALTER POLICY leads_owner_policy ON leads
USING (
    current_setting('app.current_user', true) IS NOT NULL AND (
        owner_user_id = current_setting('app.current_user', true) OR
        current_setting('app.current_user', true) = ANY(collaborator_user_ids) OR
        current_setting('app.current_role', true) = 'admin'
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
            current_setting('app.current_user', true) IS NOT NULL AND (
                EXISTS (
                    SELECT 1 FROM leads
                    WHERE leads.id = lead_activities.lead_id
                    AND (
                        leads.owner_user_id = current_setting('app.current_user', true) OR
                        current_setting('app.current_user', true) = ANY(leads.collaborator_user_ids) OR
                        current_setting('app.current_role', true) = 'admin'
                    )
                )
            )
        );
    END IF;
END $$;

-- 3. Create fail-closed policy for settings table
DO $$
BEGIN
    -- Drop existing policy if it exists
    DROP POLICY IF EXISTS settings_territory_policy ON settings;

    -- Create new fail-closed policy
    CREATE POLICY settings_territory_policy ON settings
    FOR ALL
    USING (
        current_setting('app.current_territory', true) IS NOT NULL AND (
            territory IS NULL OR
            territory = current_setting('app.current_territory', true) OR
            current_setting('app.current_role', true) = 'admin'
        )
    )
    WITH CHECK (
        current_setting('app.current_territory', true) IS NOT NULL AND (
            territory IS NULL OR
            territory = current_setting('app.current_territory', true) OR
            current_setting('app.current_role', true) = 'admin'
        )
    );
END $$;

-- 4. Create fail-closed policy for territories table
DO $$
BEGIN
    -- Enable RLS on territories if not already enabled
    ALTER TABLE territories ENABLE ROW LEVEL SECURITY;

    -- Drop existing policy if it exists
    DROP POLICY IF EXISTS territories_access_policy ON territories;

    -- Create new fail-closed policy
    CREATE POLICY territories_access_policy ON territories
    FOR SELECT
    USING (
        current_setting('app.current_territory', true) IS NOT NULL AND (
            code = current_setting('app.current_territory', true) OR
            current_setting('app.current_role', true) = 'admin' OR
            active = true  -- All users can see active territories
        )
    );
END $$;

-- 5. Create multi-tenant dedupe index (idempotent)
CREATE INDEX IF NOT EXISTS idx_leads_tenant_email
ON leads(tenant_id, email)
WHERE tenant_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_settings_tenant_key
ON settings(tenant_id, key)
WHERE tenant_id IS NOT NULL;

-- 6. Add healthcheck function for RLS validation
CREATE OR REPLACE FUNCTION check_rls_context()
RETURNS TABLE (
    user_context text,
    role_context text,
    tenant_context text,
    territory_context text,
    rls_active boolean
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        current_setting('app.current_user', true) as user_context,
        current_setting('app.current_role', true) as role_context,
        current_setting('app.tenant_id', true) as tenant_context,
        current_setting('app.current_territory', true) as territory_context,
        EXISTS (
            SELECT 1 FROM pg_policies
            WHERE schemaname = 'public'
            AND tablename IN ('leads', 'settings', 'territories')
        ) as rls_active;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execute permission to application user
GRANT EXECUTE ON FUNCTION check_rls_context() TO freshplan;

-- Add comment for documentation
COMMENT ON FUNCTION check_rls_context() IS
'Healthcheck function to validate RLS context and policy status.
Used by monitoring to ensure RLS is properly configured.';