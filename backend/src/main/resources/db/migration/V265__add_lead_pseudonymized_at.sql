-- Migration V265: Add pseudonymized_at column for DSGVO B2B Pseudonymization
-- Sprint 2.1.6 Phase 3 - Automated Jobs
-- Business Rule: "B2B Personal Data Pseudonymization (DSGVO Art. 4)"
-- Contract Mapping: See docs/CONTRACT_MAPPING.md §2(8)i

-- Add pseudonymized_at timestamp to track when PII was pseudonymized
ALTER TABLE leads
ADD COLUMN pseudonymized_at TIMESTAMPTZ NULL;

-- Add comment for documentation
COMMENT ON COLUMN leads.pseudonymized_at IS
  'Sprint 2.1.6 Phase 3: Timestamp when PII was pseudonymized (email → SHA-256, phone/contact/notes → NULL). See DSGVO Art. 4 for B2B data retention rules.';

-- Create index for DSGVO Pseudonymization Job (Job 3)
-- Query pattern: status=EXPIRED AND updatedAt < NOW() - 60 days AND pseudonymizedAt IS NULL
-- Code Review (ChatGPT): IF NOT EXISTS für Idempotenz
CREATE INDEX IF NOT EXISTS idx_leads_pseudonymization_check
ON leads (status, updated_at, pseudonymized_at)
WHERE status = 'EXPIRED' AND pseudonymized_at IS NULL;

-- Add comment for index documentation
COMMENT ON INDEX idx_leads_pseudonymization_check IS
  'Sprint 2.1.6 Phase 3: Partial index for DSGVO Pseudonymization Job. Accelerates query: status=EXPIRED AND updatedAt < threshold AND pseudonymizedAt IS NULL.';
