-- VXXX__hot_projections.sql
-- IMPORTANT: Rename with your script: V$(./scripts/get-next-migration.sh)__hot_projections.sql
-- Cockpit/Lead hot projections to avoid heavy joins on hot paths.
-- Fail-closed RLS remains; projection stores denormalized fields + version hash for ETag.

CREATE TABLE IF NOT EXISTS cockpit_leads_hot (
  lead_id uuid PRIMARY KEY,
  customer_name text NOT NULL,
  owner_user_id uuid NOT NULL,
  status text NOT NULL,
  last_activity_at timestamptz,
  sample_state text,
  roi_potential numeric(6,2),
  updated_at timestamptz NOT NULL DEFAULT now(),
  version_etag text NOT NULL
);

-- Recompute function (idempotent upsert); call from triggers or batch job
CREATE OR REPLACE FUNCTION recompute_cockpit_lead_hot(p_lead uuid) RETURNS void AS $$
DECLARE v_etag text;
BEGIN
  -- Example: compute aggregated fields
  INSERT INTO cockpit_leads_hot(lead_id, customer_name, owner_user_id, status, last_activity_at, sample_state, roi_potential, updated_at, version_etag)
  SELECT l.id, c.name, o.user_id, l.status,
         (SELECT max(a.created_at) FROM activities a WHERE a.lead_id=l.id),
         s.status, l.roi_potential, now(),
         encode(digest(l.id::text || coalesce(l.updated_at::text,'') || coalesce((SELECT max(a.updated_at)::text FROM activities a WHERE a.lead_id=l.id), ''), 'sha256'),'hex')
  FROM leads l
  JOIN customers c ON c.id=l.customer_id
  JOIN lead_ownership o ON o.lead_id=l.id
  LEFT JOIN sample_request s ON s.lead_id=l.id AND s.latest = true
  WHERE l.id=p_lead
  ON CONFLICT (lead_id) DO UPDATE SET
    customer_name=EXCLUDED.customer_name,
    owner_user_id=EXCLUDED.owner_user_id,
    status=EXCLUDED.status,
    last_activity_at=EXCLUDED.last_activity_at,
    sample_state=EXCLUDED.sample_state,
    roi_potential=EXCLUDED.roi_potential,
    updated_at=now(),
    version_etag=EXCLUDED.version_etag;
END;
$$ LANGUAGE plpgsql;

-- Example trigger on leads
DROP TRIGGER IF EXISTS trg_lead_hot ON leads;
CREATE TRIGGER trg_lead_hot
AFTER INSERT OR UPDATE OF customer_id, status, updated_at ON leads
FOR EACH ROW EXECUTE FUNCTION recompute_cockpit_lead_hot(NEW.id);

-- Optional: triggers on activities/sample_request likewise
