-- Migration V267: Make Lead.ownerUserId nullable (Sprint 2.1.6 Phase 3)
--
-- WARUM DIESE MIGRATION?
-- Der Automated Job "Protection Expiry Check" setzt ownerUserId = NULL,
-- wenn der Lead-Schutz erlischt (60-Day Activity Rule + 10-Day Grace Period).
--
-- Business-Logik: Expired Leads werden "frei" (kein Owner mehr) und können
-- neu zugewiesen werden.
--
-- BREAKING CHANGE: ownerUserId war bisher @NotNull, jetzt nullable.

-- Remove NOT NULL constraint from leads.owner_user_id
ALTER TABLE leads
ALTER COLUMN owner_user_id DROP NOT NULL;

-- Add comment explaining the business logic
COMMENT ON COLUMN leads.owner_user_id IS
  'Sprint 2.1.6 Phase 3: Owner of the Lead (nullable since Protection Expiry can free Leads)';
