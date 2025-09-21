-- Query Optimization Snippets

-- Covering index for cockpit_leads_hot list (owner + status sorted by last activity)
CREATE INDEX IF NOT EXISTS ix_cockpit_leads_hot_owner_status_last
  ON cockpit_leads_hot(owner_user_id, status, last_activity_at DESC)
  INCLUDE (customer_name, roi_potential, version_etag);

-- Pagination pattern (seek method avoids OFFSET for large pages)
-- WHERE (owner_user_id = :user) AND (status = :status) AND (last_activity_at, lead_id) < (:cursor_ts, :cursor_id)
-- ORDER BY last_activity_at DESC, lead_id DESC LIMIT :limit;

-- Named parameters only; avoid SELECT *; return only projected columns.

-- Example read query
/* :user uuid, :status text, :cursor_ts timestamptz, :cursor_id uuid, :limit int */
SELECT lead_id, customer_name, status, last_activity_at, roi_potential, version_etag
FROM cockpit_leads_hot
WHERE owner_user_id = :user AND status = :status
  AND (:cursor_ts IS NULL OR (last_activity_at, lead_id) < (:cursor_ts, :cursor_id))
ORDER BY last_activity_at DESC, lead_id DESC
LIMIT :limit;
