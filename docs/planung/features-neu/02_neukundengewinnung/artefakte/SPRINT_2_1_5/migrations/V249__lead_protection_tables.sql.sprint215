-- V249__lead_protection_tables.sql
-- Sprint 2.1.5: Lead Protection and Activity Tracking (Contract Requirements)
-- Author: team/leads-backend
-- Date: 2025-09-28
--
-- Implements lead protection (6 months), activity tracking (60-day rule),
-- and stop-the-clock mechanism as per contract requirements

-- =====================================================
-- 1. LEAD PROTECTION TABLE
-- =====================================================
-- Tracks lead protection period and progress requirements
CREATE TABLE IF NOT EXISTS lead_protection (
  id BIGSERIAL PRIMARY KEY,
  lead_id BIGINT NOT NULL REFERENCES leads(id) ON DELETE CASCADE,

  -- Protection period (6 months from registration)
  registered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  protection_until TIMESTAMP WITH TIME ZONE NOT NULL,

  -- 60-day activity requirement tracking
  last_progress_at TIMESTAMP WITH TIME ZONE,
  progress_warning_sent_at TIMESTAMP WITH TIME ZONE,
  progress_deadline TIMESTAMP WITH TIME ZONE,

  -- Status management
  status VARCHAR(20) NOT NULL DEFAULT 'protected'
    CHECK (status IN ('protected', 'warning', 'expired', 'released')),

  -- Stop-the-clock mechanism
  stop_the_clock_reason TEXT,
  stop_the_clock_start TIMESTAMP WITH TIME ZONE,
  stop_the_clock_end TIMESTAMP WITH TIME ZONE,

  -- Metadata
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

  UNIQUE(lead_id)
);

CREATE INDEX idx_lead_protection_status ON lead_protection(status);
CREATE INDEX idx_lead_protection_deadline ON lead_protection(progress_deadline)
  WHERE progress_deadline IS NOT NULL;
CREATE INDEX idx_lead_protection_expires ON lead_protection(protection_until);

COMMENT ON TABLE lead_protection IS
  'Lead protection tracking per contract: 6-month protection, 60-day progress requirement';
COMMENT ON COLUMN lead_protection.status IS
  'protected=active, warning=60-day warning sent, expired=protection ended, released=manually released';
COMMENT ON COLUMN lead_protection.stop_the_clock_reason IS
  'Reason for pausing progress requirement (e.g., FreshFoodz delay, customer vacation)';

-- =====================================================
-- 2. LEAD ACTIVITY TABLE
-- =====================================================
-- Tracks all activities for progress documentation
CREATE TABLE IF NOT EXISTS lead_activities (
  id BIGSERIAL PRIMARY KEY,
  lead_id BIGINT NOT NULL REFERENCES leads(id) ON DELETE CASCADE,

  -- Activity details
  activity_type VARCHAR(30) NOT NULL
    CHECK (activity_type IN (
      'call', 'email', 'meeting', 'demo', 'offer',
      'negotiation', 'contract_sent', 'follow_up', 'note', 'other'
    )),
  activity_date TIMESTAMP WITH TIME ZONE NOT NULL,

  -- Progress flag (counts towards 60-day requirement)
  counts_as_progress BOOLEAN NOT NULL DEFAULT true,

  -- Activity content
  summary VARCHAR(500),
  description TEXT,
  outcome VARCHAR(50) CHECK (outcome IN (
    'successful', 'no_answer', 'postponed', 'rejected', 'pending', null
  )),

  -- Next steps
  next_action VARCHAR(200),
  next_action_date DATE,

  -- User tracking
  performed_by VARCHAR(255) NOT NULL,

  -- Metadata
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lead_activities_lead_date ON lead_activities(lead_id, activity_date DESC);
CREATE INDEX idx_lead_activities_progress ON lead_activities(lead_id, activity_date DESC)
  WHERE counts_as_progress = true;
CREATE INDEX idx_lead_activities_type ON lead_activities(activity_type);
CREATE INDEX idx_lead_activities_user ON lead_activities(performed_by);

COMMENT ON TABLE lead_activities IS
  'Activity log for leads, used to track progress per 60-day contract requirement';
COMMENT ON COLUMN lead_activities.counts_as_progress IS
  'Whether this activity counts towards 60-day progress requirement';

-- =====================================================
-- 3. LEAD TRANSFER HISTORY
-- =====================================================
-- Tracks lead ownership changes between partners/sales reps
CREATE TABLE IF NOT EXISTS lead_transfers (
  id BIGSERIAL PRIMARY KEY,
  lead_id BIGINT NOT NULL REFERENCES leads(id) ON DELETE CASCADE,

  -- Transfer details
  from_user_id VARCHAR(255),
  to_user_id VARCHAR(255) NOT NULL,
  transfer_reason VARCHAR(100) CHECK (transfer_reason IN (
    'initial_assignment', 'reassignment', 'partner_change',
    'protection_expired', 'manual_release', 'other'
  )),
  transfer_note TEXT,

  -- Protection impact
  protection_reset BOOLEAN NOT NULL DEFAULT false,
  new_protection_until TIMESTAMP WITH TIME ZONE,

  -- Approval workflow (optional)
  requires_approval BOOLEAN NOT NULL DEFAULT false,
  approved_by VARCHAR(255),
  approved_at TIMESTAMP WITH TIME ZONE,

  -- Metadata
  transferred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  transferred_by VARCHAR(255) NOT NULL
);

CREATE INDEX idx_lead_transfers_lead ON lead_transfers(lead_id, transferred_at DESC);
CREATE INDEX idx_lead_transfers_to_user ON lead_transfers(to_user_id);
CREATE INDEX idx_lead_transfers_from_user ON lead_transfers(from_user_id);

COMMENT ON TABLE lead_transfers IS
  'History of lead ownership transfers between users/partners';

-- =====================================================
-- 4. HELPER FUNCTIONS
-- =====================================================

-- Calculate protection end date (6 months from registration)
CREATE OR REPLACE FUNCTION calculate_protection_until(registered_at TIMESTAMP WITH TIME ZONE)
RETURNS TIMESTAMP WITH TIME ZONE AS $$
BEGIN
  RETURN registered_at + INTERVAL '6 months';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Calculate next progress deadline (60 days from last activity)
CREATE OR REPLACE FUNCTION calculate_progress_deadline(last_activity TIMESTAMP WITH TIME ZONE)
RETURNS TIMESTAMP WITH TIME ZONE AS $$
BEGIN
  IF last_activity IS NULL THEN
    RETURN CURRENT_TIMESTAMP + INTERVAL '60 days';
  END IF;
  RETURN last_activity + INTERVAL '60 days';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Check if lead protection is active
CREATE OR REPLACE FUNCTION is_lead_protected(lead_id_param BIGINT)
RETURNS BOOLEAN AS $$
DECLARE
  protection_status VARCHAR(20);
  protection_end TIMESTAMP WITH TIME ZONE;
BEGIN
  SELECT status, protection_until
  INTO protection_status, protection_end
  FROM lead_protection
  WHERE lead_id = lead_id_param;

  IF protection_status IS NULL THEN
    RETURN false;
  END IF;

  IF protection_status IN ('expired', 'released') THEN
    RETURN false;
  END IF;

  IF protection_end < CURRENT_TIMESTAMP THEN
    -- Update status if expired
    UPDATE lead_protection
    SET status = 'expired',
        updated_at = CURRENT_TIMESTAMP
    WHERE lead_id = lead_id_param;
    RETURN false;
  END IF;

  RETURN true;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 5. TRIGGERS
-- =====================================================

-- Auto-create protection record when lead is created
CREATE OR REPLACE FUNCTION create_lead_protection_trigger()
RETURNS TRIGGER AS $$
BEGIN
  -- Only create protection for new leads with an owner
  IF NEW.owner_user_id IS NOT NULL THEN
    INSERT INTO lead_protection (
      lead_id,
      registered_at,
      protection_until,
      progress_deadline,
      status
    ) VALUES (
      NEW.id,
      CURRENT_TIMESTAMP,
      calculate_protection_until(CURRENT_TIMESTAMP),
      calculate_progress_deadline(NULL),
      'protected'
    ) ON CONFLICT (lead_id) DO NOTHING;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS create_lead_protection_on_insert ON leads;
CREATE TRIGGER create_lead_protection_on_insert
  AFTER INSERT ON leads
  FOR EACH ROW
  EXECUTE FUNCTION create_lead_protection_trigger();

-- Update progress deadline when activity is logged
CREATE OR REPLACE FUNCTION update_progress_deadline_trigger()
RETURNS TRIGGER AS $$
BEGIN
  -- Only update if activity counts as progress
  IF NEW.counts_as_progress = true THEN
    UPDATE lead_protection
    SET last_progress_at = NEW.activity_date,
        progress_deadline = calculate_progress_deadline(NEW.activity_date),
        status = CASE
          WHEN status = 'warning' THEN 'protected'
          ELSE status
        END,
        updated_at = CURRENT_TIMESTAMP
    WHERE lead_id = NEW.lead_id
      AND status IN ('protected', 'warning');
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_progress_on_activity ON lead_activities;
CREATE TRIGGER update_progress_on_activity
  AFTER INSERT ON lead_activities
  FOR EACH ROW
  EXECUTE FUNCTION update_progress_deadline_trigger();

-- =====================================================
-- 6. INITIAL DATA / EXAMPLES
-- =====================================================
-- Note: In production, these would be managed by application logic

-- Example: Check for leads needing 60-day warning
-- This would typically run as a scheduled job
CREATE OR REPLACE FUNCTION check_progress_warnings()
RETURNS INTEGER AS $$
DECLARE
  updated_count INTEGER := 0;
BEGIN
  UPDATE lead_protection
  SET status = 'warning',
      progress_warning_sent_at = CURRENT_TIMESTAMP,
      updated_at = CURRENT_TIMESTAMP
  WHERE status = 'protected'
    AND progress_deadline < CURRENT_TIMESTAMP + INTERVAL '7 days'
    AND progress_warning_sent_at IS NULL;

  GET DIAGNOSTICS updated_count = ROW_COUNT;
  RETURN updated_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION check_progress_warnings IS
  'Scheduled job: marks leads needing 60-day progress warning (run daily)';

-- Example: Release expired protections
CREATE OR REPLACE FUNCTION release_expired_protections()
RETURNS INTEGER AS $$
DECLARE
  updated_count INTEGER := 0;
BEGIN
  UPDATE lead_protection
  SET status = 'expired',
      updated_at = CURRENT_TIMESTAMP
  WHERE status IN ('protected', 'warning')
    AND (
      protection_until < CURRENT_TIMESTAMP
      OR (progress_deadline < CURRENT_TIMESTAMP - INTERVAL '7 days' AND stop_the_clock_start IS NULL)
    );

  GET DIAGNOSTICS updated_count = ROW_COUNT;
  RETURN updated_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION release_expired_protections IS
  'Scheduled job: releases expired lead protections (run daily)';