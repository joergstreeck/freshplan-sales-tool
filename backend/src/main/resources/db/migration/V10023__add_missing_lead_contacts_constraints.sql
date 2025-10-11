-- V10023: DEPRECATED - Redundant with V10016/V10017
--
-- Root Cause Analysis (Post-Mortem):
-- - V10016 already created the CHECK constraint (line 72-73)
-- - V10017 already created the UNIQUE index (line 104-107)
-- - V10017 already created the trigger function (line 19-68)
-- - This migration was created under false assumption that constraints were missing
--
-- Resolution:
-- - V10017 was fixed to use COALESCE(is_deleted, false) for NULL-safety
-- - All constraints now work correctly
-- - This migration is now a NO-OP (does nothing)
--
-- Author: Sprint 2.1.6 Phase 5+ Fix
-- Date: 2025-10-11
-- Status: DEPRECATED / NO-OP

-- NO-OP: All constraints already exist in V10016/V10017
SELECT 1 WHERE FALSE; -- Empty result set, no side effects
