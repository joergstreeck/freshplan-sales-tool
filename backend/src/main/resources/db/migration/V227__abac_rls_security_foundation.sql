-- V227: ABAC/RLS Security Foundation für FreshFoodz CRM
-- Implementiert Territory-based RLS + User-based Lead Protection + Multi-Contact Visibility
-- Part of Sprint 1.2: Security + Foundation (FP-228)

BEGIN;

-- Lock timeout für safety
SET LOCAL lock_timeout = '250ms';
SET LOCAL statement_timeout = '15s';

-- ============================================================================
-- SECURITY ENUMS UND TYPES
-- ============================================================================

-- Note visibility levels für Multi-Contact Management
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'note_visibility') THEN
        CREATE TYPE note_visibility AS ENUM (
            'OWNER_ONLY',      -- Nur Lead-Owner
            'COLLABORATORS',   -- Owner + Kollaboratoren
            'ACCOUNT_TEAM',    -- Alle im Account-Team
            'ORG_READ'         -- Organisation-weit lesbar
        );
    END IF;
END $$;

-- Note categories für strukturierte Notizen
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'note_category') THEN
        CREATE TYPE note_category AS ENUM (
            'GENERAL',     -- Allgemeine Notizen
            'COMMERCIAL',  -- Kommerzielle Details (Preise, Konditionen)
            'PRODUCT'      -- Produkt-Feedback
        );
    END IF;
END $$;

-- Lead action types für Audit Trail
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'lead_action') THEN
        CREATE TYPE lead_action AS ENUM (
            'VIEW',
            'CREATE',
            'EDIT',
            'DELETE',
            'ASSIGN',
            'EXPORT',
            'OVERRIDE'
        );
    END IF;
END $$;

-- Access outcome für Audit Trail
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'lead_access_outcome') THEN
        CREATE TYPE lead_access_outcome AS ENUM (
            'ALLOW',
            'DENY',
            'PARTIAL'
        );
    END IF;
END $$;

-- ============================================================================
-- HELPER FUNCTIONS (STABLE für Index-Optimierung)
-- ============================================================================

-- Get current user ID from session
CREATE OR REPLACE FUNCTION app_user_id()
RETURNS uuid
LANGUAGE sql STABLE
AS $$
    SELECT NULLIF(current_setting('app.user_id', true), '')::uuid
$$;

-- Get current organization ID
CREATE OR REPLACE FUNCTION app_org_id()
RETURNS text
LANGUAGE sql STABLE
AS $$
    SELECT NULLIF(current_setting('app.org_id', true), '')
$$;

-- Get current territory (DE/CH)
CREATE OR REPLACE FUNCTION app_territory()
RETURNS text
LANGUAGE sql STABLE
AS $$
    SELECT NULLIF(current_setting('app.territory', true), '')
$$;

-- Get user scopes (permissions)
CREATE OR REPLACE FUNCTION app_scopes()
RETURNS text[]
LANGUAGE sql STABLE
AS $$
    SELECT CASE
        WHEN current_setting('app.scopes', true) IS NULL
            OR current_setting('app.scopes', true) = ''
        THEN ARRAY[]::text[]
        ELSE string_to_array(current_setting('app.scopes', true), ',')
    END
$$;

-- Get contact roles (CHEF/BUYER)
CREATE OR REPLACE FUNCTION app_contact_roles()
RETURNS text[]
LANGUAGE sql STABLE
AS $$
    SELECT CASE
        WHEN current_setting('app.contact_roles', true) IS NULL
            OR current_setting('app.contact_roles', true) = ''
        THEN ARRAY[]::text[]
        ELSE string_to_array(current_setting('app.contact_roles', true), ',')
    END
$$;

-- Check if user has specific scope
CREATE OR REPLACE FUNCTION app_has_scope(s text)
RETURNS boolean
LANGUAGE sql STABLE
AS $$
    SELECT s = ANY(app_scopes())
$$;

-- Check if user has specific contact role
CREATE OR REPLACE FUNCTION app_has_contact_role(r text)
RETURNS boolean
LANGUAGE sql STABLE
AS $$
    SELECT r = ANY(app_contact_roles())
$$;

-- ============================================================================
-- OWNERSHIP & COLLABORATION FUNCTIONS
-- ============================================================================

-- Check if user owns a lead
CREATE OR REPLACE FUNCTION is_lead_owner(p_lead_id uuid)
RETURNS boolean
LANGUAGE sql STABLE
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM user_lead_assignments ula
        JOIN leads l ON l.id = ula.lead_id
        WHERE ula.lead_id = p_lead_id
            AND ula.user_id = app_user_id()
            AND ula.territory = app_territory()
            AND l.territory = ula.territory
            AND l.org_id = app_org_id()
    );
$$;

-- Lead collaborators table
CREATE TABLE IF NOT EXISTS lead_collaborators (
    lead_id uuid NOT NULL,
    user_id uuid NOT NULL,
    added_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    added_by uuid,
    PRIMARY KEY (lead_id, user_id),
    FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lead_collaborators_user
ON lead_collaborators(user_id);

CREATE INDEX IF NOT EXISTS idx_lead_collaborators_lead
ON lead_collaborators(lead_id);

-- Check if user is collaborator
CREATE OR REPLACE FUNCTION is_lead_collaborator(p_lead_id uuid)
RETURNS boolean
LANGUAGE sql STABLE
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM lead_collaborators c
        WHERE c.lead_id = p_lead_id
            AND c.user_id = app_user_id()
    );
$$;

-- ============================================================================
-- AUDIT & ACCESS CONTROL
-- ============================================================================

-- Lead access audit table
CREATE TABLE IF NOT EXISTS lead_access_audit (
    id bigserial PRIMARY KEY,
    lead_id uuid NOT NULL,
    user_id uuid NOT NULL,
    territory text NOT NULL,
    action lead_action NOT NULL,
    outcome lead_access_outcome NOT NULL,
    reason text,
    metadata jsonb DEFAULT '{}',
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE
);

-- Partitioned by month for performance
CREATE INDEX IF NOT EXISTS idx_lead_audit_lead_user
ON lead_access_audit(lead_id, user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_lead_audit_user_action
ON lead_access_audit(user_id, action, created_at DESC);

-- Audit and assert edit permissions
CREATE OR REPLACE FUNCTION assert_edit_and_audit(
    p_lead_id uuid,
    p_action text DEFAULT 'EDIT',
    p_reason text DEFAULT NULL
)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
    v_can_edit boolean;
    v_territory text;
BEGIN
    -- Get lead territory
    SELECT territory INTO v_territory
    FROM leads
    WHERE id = p_lead_id;

    -- Check permissions: owner or has override scope
    v_can_edit := is_lead_owner(p_lead_id)
                  OR app_has_scope('lead:override');

    -- Log audit entry
    INSERT INTO lead_access_audit(
        lead_id,
        user_id,
        territory,
        action,
        outcome,
        reason
    )
    VALUES (
        p_lead_id,
        app_user_id(),
        COALESCE(v_territory, app_territory()),
        COALESCE(p_action, 'EDIT')::lead_action,
        CASE WHEN v_can_edit THEN 'ALLOW' ELSE 'DENY' END::lead_access_outcome,
        p_reason
    );

    -- Throw exception if denied
    IF NOT v_can_edit THEN
        RAISE EXCEPTION 'Lead edit denied for user %', app_user_id()
        USING ERRCODE = '28000';
    END IF;
END;
$$;

-- ============================================================================
-- MULTI-CONTACT NOTES VISIBILITY
-- ============================================================================

-- Contact notes with visibility control
CREATE TABLE IF NOT EXISTS contact_notes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id uuid NOT NULL,
    author_id uuid NOT NULL,
    category note_category NOT NULL,
    visibility note_visibility NOT NULL DEFAULT 'COLLABORATORS',
    content text NOT NULL,
    metadata jsonb DEFAULT '{}',
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_contact_notes_contact
ON contact_notes(contact_id, visibility);

CREATE INDEX IF NOT EXISTS idx_contact_notes_author
ON contact_notes(author_id, created_at DESC);

-- Note visibility check function
CREATE OR REPLACE FUNCTION can_view_note(p_note_id uuid)
RETURNS boolean
LANGUAGE plpgsql STABLE
AS $$
DECLARE
    v_note RECORD;
    v_lead_id uuid;
BEGIN
    -- Get note details
    SELECT cn.*, c.lead_id
    INTO v_note
    FROM contact_notes cn
    JOIN contacts c ON c.id = cn.contact_id
    WHERE cn.id = p_note_id;

    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    -- Check visibility rules
    CASE v_note.visibility
        WHEN 'OWNER_ONLY' THEN
            RETURN v_note.author_id = app_user_id();
        WHEN 'COLLABORATORS' THEN
            RETURN v_note.author_id = app_user_id()
                   OR is_lead_owner(v_note.lead_id)
                   OR is_lead_collaborator(v_note.lead_id);
        WHEN 'ACCOUNT_TEAM' THEN
            -- All users in same account can see
            RETURN EXISTS (
                SELECT 1 FROM leads
                WHERE id = v_note.lead_id
                AND org_id = app_org_id()
            );
        WHEN 'ORG_READ' THEN
            -- Organization-wide readable
            RETURN app_org_id() IS NOT NULL;
        ELSE
            RETURN FALSE;
    END CASE;
END;
$$;

-- ============================================================================
-- ROW LEVEL SECURITY POLICIES
-- ============================================================================

-- Enable RLS on all security-relevant tables
ALTER TABLE leads ENABLE ROW LEVEL SECURITY;
ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;
ALTER TABLE contact_notes ENABLE ROW LEVEL SECURITY;
ALTER TABLE lead_collaborators ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_lead_assignments ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if they exist (idempotent)
DROP POLICY IF EXISTS leads_territory_isolation ON leads;
DROP POLICY IF EXISTS leads_insert_policy ON leads;
DROP POLICY IF EXISTS leads_update_policy ON leads;
DROP POLICY IF EXISTS leads_delete_policy ON leads;

-- LEADS: Territory isolation + ownership
CREATE POLICY leads_territory_isolation ON leads
    FOR SELECT
    TO authenticated
    USING (
        -- Territory isolation
        territory = app_territory()
        AND org_id = app_org_id()
        AND (
            -- Owner or collaborator or has read scope
            is_lead_owner(id)
            OR is_lead_collaborator(id)
            OR app_has_scope('lead:read')
        )
    );

CREATE POLICY leads_insert_policy ON leads
    FOR INSERT
    TO authenticated
    WITH CHECK (
        territory = app_territory()
        AND org_id = app_org_id()
        AND app_has_scope('lead:create')
    );

CREATE POLICY leads_update_policy ON leads
    FOR UPDATE
    TO authenticated
    USING (
        territory = app_territory()
        AND org_id = app_org_id()
    )
    WITH CHECK (
        is_lead_owner(id)
        OR app_has_scope('lead:override')
    );

CREATE POLICY leads_delete_policy ON leads
    FOR DELETE
    TO authenticated
    USING (
        territory = app_territory()
        AND org_id = app_org_id()
        AND (
            is_lead_owner(id)
            OR app_has_scope('lead:delete')
        )
    );

-- CONTACTS: Inherit from lead permissions
DROP POLICY IF EXISTS contacts_access_policy ON contacts;

CREATE POLICY contacts_access_policy ON contacts
    FOR ALL
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM leads l
            WHERE l.id = contacts.lead_id
            AND l.territory = app_territory()
            AND l.org_id = app_org_id()
            AND (
                is_lead_owner(l.id)
                OR is_lead_collaborator(l.id)
                OR app_has_scope('lead:read')
            )
        )
    );

-- CONTACT NOTES: Visibility-based access
DROP POLICY IF EXISTS contact_notes_access_policy ON contact_notes;

CREATE POLICY contact_notes_access_policy ON contact_notes
    FOR ALL
    TO authenticated
    USING (can_view_note(id))
    WITH CHECK (
        -- Can only create/update if has access to contact
        EXISTS (
            SELECT 1 FROM contacts c
            JOIN leads l ON l.id = c.lead_id
            WHERE c.id = contact_notes.contact_id
            AND l.territory = app_territory()
            AND l.org_id = app_org_id()
        )
    );

-- COLLABORATORS: Only owners can manage
DROP POLICY IF EXISTS collaborators_manage_policy ON lead_collaborators;

CREATE POLICY collaborators_manage_policy ON lead_collaborators
    FOR ALL
    TO authenticated
    USING (
        is_lead_owner(lead_id)
        OR app_has_scope('lead:override')
    );

-- USER LEAD ASSIGNMENTS: Territory-scoped
DROP POLICY IF EXISTS assignments_territory_policy ON user_lead_assignments;

CREATE POLICY assignments_territory_policy ON user_lead_assignments
    FOR ALL
    TO authenticated
    USING (
        territory = app_territory()
        AND (
            user_id = app_user_id()
            OR app_has_scope('lead:assign')
        )
    );

-- ============================================================================
-- PERFORMANCE INDEXES
-- ============================================================================

-- Optimize RLS performance
CREATE INDEX IF NOT EXISTS idx_leads_rls_lookup
ON leads(org_id, territory, id)
WHERE status != 'CONVERTED';

CREATE INDEX IF NOT EXISTS idx_user_assignments_rls
ON user_lead_assignments(user_id, territory, lead_id);

CREATE INDEX IF NOT EXISTS idx_contacts_lead_lookup
ON contacts(lead_id)
INCLUDE (id, name);

-- ============================================================================
-- GRANT PERMISSIONS
-- ============================================================================

-- Grant necessary permissions to application role
GRANT USAGE ON SCHEMA public TO authenticated;
GRANT ALL ON ALL TABLES IN SCHEMA public TO authenticated;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO authenticated;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO authenticated;

-- ============================================================================
-- VALIDATION
-- ============================================================================

-- Ensure critical tables exist
DO $$
BEGIN
    -- Check if leads table has required columns
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'leads'
        AND column_name = 'territory'
    ) THEN
        RAISE EXCEPTION 'Missing required column: leads.territory';
    END IF;

    -- Check if user_lead_assignments exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'user_lead_assignments'
    ) THEN
        RAISE WARNING 'Table user_lead_assignments does not exist - creating minimal version';

        CREATE TABLE user_lead_assignments (
            lead_id uuid NOT NULL,
            user_id uuid NOT NULL,
            territory text NOT NULL,
            assigned_at timestamptz DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY (lead_id, user_id, territory),
            FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE
        );
    END IF;
END $$;

COMMIT;

-- ============================================================================
-- ROLLBACK PLAN
-- ============================================================================
-- To rollback this migration:
-- 1. Disable RLS: ALTER TABLE leads DISABLE ROW LEVEL SECURITY;
-- 2. Drop policies: DROP POLICY IF EXISTS <policy_name> ON <table>;
-- 3. Drop functions: DROP FUNCTION IF EXISTS <function_name>;
-- 4. Drop tables: DROP TABLE IF EXISTS lead_collaborators CASCADE;
-- 5. Drop types: DROP TYPE IF EXISTS note_visibility CASCADE;