-- V243: Update GUC Keys to avoid PostgreSQL reserved words
-- Changes app.current_user -> app.user_context
-- Changes app.current_role -> app.role_context
-- Changes app.current_territory -> app.territory_context

-- Update existing RLS policies to use new GUC keys
-- This is safe because V242 already uses the new keys

-- 1. Update leads policies
DO $$
BEGIN
    -- Only update if policies exist
    IF EXISTS (SELECT 1 FROM pg_policies WHERE tablename = 'leads') THEN
        -- These policies were created with the new keys in V242, so nothing to do
        RAISE NOTICE 'Leads policies already use new GUC keys';
    END IF;
END $$;

-- 2. Update audit_log triggers if they exist
DO $$
BEGIN
    -- Check if audit_log table exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_log') THEN
        -- Update the audit trigger function if it exists
        IF EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'audit_trigger_function') THEN
            -- Replace the function to use new GUC keys
            CREATE OR REPLACE FUNCTION audit_trigger_function()
            RETURNS TRIGGER AS $function$
            BEGIN
                IF TG_OP = 'INSERT' THEN
                    INSERT INTO audit_log(
                        table_name, operation, user_id,
                        new_data, changed_at
                    )
                    VALUES(
                        TG_TABLE_NAME, TG_OP,
                        current_setting('app.user_context', true),
                        row_to_json(NEW), NOW()
                    );
                    RETURN NEW;
                ELSIF TG_OP = 'UPDATE' THEN
                    INSERT INTO audit_log(
                        table_name, operation, user_id,
                        old_data, new_data, changed_at
                    )
                    VALUES(
                        TG_TABLE_NAME, TG_OP,
                        current_setting('app.user_context', true),
                        row_to_json(OLD), row_to_json(NEW), NOW()
                    );
                    RETURN NEW;
                ELSIF TG_OP = 'DELETE' THEN
                    INSERT INTO audit_log(
                        table_name, operation, user_id,
                        old_data, changed_at
                    )
                    VALUES(
                        TG_TABLE_NAME, TG_OP,
                        current_setting('app.user_context', true),
                        row_to_json(OLD), NOW()
                    );
                    RETURN OLD;
                END IF;
            END;
            $function$ LANGUAGE plpgsql;
        END IF;
    END IF;
END $$;

-- 3. Update check_rls_context function to use new keys
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