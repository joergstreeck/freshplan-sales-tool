-- V236: Consent & Assignment Tables - Foundation for Sprint 2.3
-- Sprint 2.1 PR #104: Table skeleton for future implementation

-- Lead Consent tracking (GDPR compliant)
CREATE TABLE IF NOT EXISTS lead_consent (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('EMAIL', 'PHONE', 'SMS', 'POST')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPT_IN', 'OPT_OUT', 'UNKNOWN', 'PENDING')),
    method VARCHAR(50), -- DOI, form, import, manual, api
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    evidence JSONB, -- Additional proof/context
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),

    CONSTRAINT uk_lead_consent_channel UNIQUE(lead_id, channel)
);

-- User Lead Assignments (audit trail for lead ownership changes)
CREATE TABLE IF NOT EXISTS user_lead_assignments (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id VARCHAR(64) NOT NULL,
    assigned_by VARCHAR(64) NOT NULL,
    assignment_type VARCHAR(30) NOT NULL DEFAULT 'OWNER',
    reason VARCHAR(255),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unassigned_at TIMESTAMPTZ,
    unassigned_by VARCHAR(64),
    unassign_reason VARCHAR(255)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_lead_consent_lead ON lead_consent(lead_id);
CREATE INDEX IF NOT EXISTS idx_lead_consent_status ON lead_consent(status);
CREATE INDEX IF NOT EXISTS idx_lead_consent_occurred ON lead_consent(occurred_at DESC);

CREATE INDEX IF NOT EXISTS idx_assignments_lead ON user_lead_assignments(lead_id);
CREATE INDEX IF NOT EXISTS idx_assignments_user ON user_lead_assignments(user_id);
CREATE INDEX IF NOT EXISTS idx_assignments_active ON user_lead_assignments(lead_id, user_id)
WHERE unassigned_at IS NULL;

-- Comments for documentation
COMMENT ON TABLE lead_consent IS 'GDPR-compliant consent tracking per lead and channel';
COMMENT ON COLUMN lead_consent.channel IS 'Communication channel for consent';
COMMENT ON COLUMN lead_consent.status IS 'Current consent status';
COMMENT ON COLUMN lead_consent.method IS 'How consent was obtained';
COMMENT ON COLUMN lead_consent.evidence IS 'Additional proof/context in JSON format';

COMMENT ON TABLE user_lead_assignments IS 'Audit trail for lead ownership and assignment history';
COMMENT ON COLUMN user_lead_assignments.assignment_type IS 'Type of assignment (OWNER, COLLABORATOR, WATCHER)';
COMMENT ON COLUMN user_lead_assignments.unassigned_at IS 'NULL means assignment is still active';