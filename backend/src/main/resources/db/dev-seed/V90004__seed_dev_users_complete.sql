-- ============================================================================
-- V90004: DEV-SEED Complete User Data
-- ============================================================================
-- Purpose: Realistic seed data for user management testing in development
-- Environment: ONLY loaded in %dev (NOT in %test, NOT in production)
--
-- ⚠️  IMPORTANT: Tests must NEVER use this data!
-- ⚠️  Tests create their own data via TestDataFactory
--
-- Idempotent: Can be run multiple times (ON CONFLICT DO NOTHING)
--
-- Structure (respecting FK dependencies):
--   1. app_user (5 realistic sales team members)
--   2. user_roles (roles assigned to each user)
--
-- Test Scenarios:
--   USR-DEV-001: Admin User (Full Access, Team Lead)
--   USR-DEV-002: Manager User (Team Management, Reports)
--   USR-DEV-003: Senior Sales User (Experienced, Large Territory)
--   USR-DEV-004: Junior Sales User (New, Learning)
--   USR-DEV-005: Auditor User (Read-Only, Compliance)
-- ============================================================================


-- ============================================================================
-- 1. APP_USER (5)
-- ============================================================================

INSERT INTO app_user (
    id,
    username,
    first_name,
    last_name,
    email,
    enabled,
    is_test_data,
    created_at,
    updated_at
) VALUES
-- ============================================================================
-- USR-DEV-001: Admin User - Stefan Weber (Team Lead)
-- Use Case: Full System Access, Configuration, User Management
-- Characteristics: Long-time employee, technical knowledge, training role
-- ============================================================================
(
    '90000000-0001-0000-0000-000000000001'::uuid,
    'stefan.weber',
    'Stefan',
    'Weber',
    'stefan.weber@freshplan.example',
    TRUE,
    TRUE,  -- is_test_data
    NOW() - INTERVAL '3 years',
    NOW() - INTERVAL '2 days'
),

-- ============================================================================
-- USR-DEV-002: Manager User - Anna Schmidt (Sales Manager)
-- Use Case: Team Management, Reports, Territory Planning
-- Characteristics: People manager, data-driven, strategic focus
-- ============================================================================
(
    '90000000-0002-0000-0000-000000000001'::uuid,
    'anna.schmidt',
    'Anna',
    'Schmidt',
    'anna.schmidt@freshplan.example',
    TRUE,
    TRUE,  -- is_test_data
    NOW() - INTERVAL '2 years',
    NOW() - INTERVAL '1 day'
),

-- ============================================================================
-- USR-DEV-003: Senior Sales User - Michael Becker (Territory: Süd)
-- Use Case: Large Customer Portfolio, Complex Deals, Mentoring
-- Characteristics: 8 years experience, top performer, large territory
-- ============================================================================
(
    '90000000-0003-0000-0000-000000000001'::uuid,
    'michael.becker',
    'Michael',
    'Becker',
    'michael.becker@freshplan.example',
    TRUE,
    TRUE,  -- is_test_data
    NOW() - INTERVAL '18 months',
    NOW() - INTERVAL '3 hours'
),

-- ============================================================================
-- USR-DEV-004: Junior Sales User - Julia Hoffmann (Territory: Nord)
-- Use Case: New Employee Onboarding, Training Workflows, Small Accounts
-- Characteristics: 6 months experience, learning, smaller territory
-- ============================================================================
(
    '90000000-0004-0000-0000-000000000001'::uuid,
    'julia.hoffmann',
    'Julia',
    'Hoffmann',
    'julia.hoffmann@freshplan.example',
    TRUE,
    TRUE,  -- is_test_data
    NOW() - INTERVAL '6 months',
    NOW() - INTERVAL '5 hours'
),

-- ============================================================================
-- USR-DEV-005: Auditor User - Thomas Meier (Compliance & Quality)
-- Use Case: Audit Trails, Compliance Checks, Read-Only Access
-- Characteristics: Read-only role, compliance focus, no sales activity
-- ============================================================================
(
    '90000000-0005-0000-0000-000000000001'::uuid,
    'thomas.meier',
    'Thomas',
    'Meier',
    'thomas.meier@freshplan.example',
    TRUE,
    TRUE,  -- is_test_data
    NOW() - INTERVAL '1 year',
    NOW() - INTERVAL '1 week'
)
ON CONFLICT (username) DO NOTHING;


-- ============================================================================
-- 2. USER_ROLES (Multiple roles per user)
-- ============================================================================

INSERT INTO user_roles (
    user_id,
    role
) VALUES
-- USR-DEV-001: Admin (All Roles for Testing)
('90000000-0001-0000-0000-000000000001'::uuid, 'admin'),
('90000000-0001-0000-0000-000000000001'::uuid, 'manager'),
('90000000-0001-0000-0000-000000000001'::uuid, 'sales'),

-- USR-DEV-002: Manager + Sales (Team Lead)
('90000000-0002-0000-0000-000000000001'::uuid, 'manager'),
('90000000-0002-0000-0000-000000000001'::uuid, 'sales'),

-- USR-DEV-003: Senior Sales (Sales Only)
('90000000-0003-0000-0000-000000000001'::uuid, 'sales'),

-- USR-DEV-004: Junior Sales (Sales Only)
('90000000-0004-0000-0000-000000000001'::uuid, 'sales'),

-- USR-DEV-005: Auditor (Read-Only)
('90000000-0005-0000-0000-000000000001'::uuid, 'auditor')
ON CONFLICT (user_id, role) DO NOTHING;


-- ============================================================================
-- END OF V90004
-- ============================================================================
-- Summary:
--   ✅ 5 Users (Admin, Manager, Senior Sales, Junior Sales, Auditor)
--   ✅ 8 Role Assignments (realistic multi-role scenarios)
--   ✅ is_test_data = TRUE everywhere
--   ✅ Fixed UUIDs for consistency (90000000-000X pattern)
--   ✅ ON CONFLICT DO NOTHING for idempotency
--   ✅ Realistic German names + @freshplan.example emails
--   ✅ Created_at timestamps show realistic hiring timeline
--   ✅ Updated_at timestamps show realistic activity
-- ============================================================================
