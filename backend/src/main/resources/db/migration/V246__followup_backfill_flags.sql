-- V246: Backfill Follow-up Flags für bestehende Aktivitäten
--
-- Sprint 2.1 - FP-235: Sicherstellen dass bestehende Follow-ups korrekt markiert sind
-- Verhindert Doppelversand bei bereits kontaktierten Leads

-- Setze T+3 Flag für Leads die bereits T+3 Follow-up erhalten haben
UPDATE leads l
SET t3_followup_sent = true,
    followup_count = COALESCE(followup_count, 0) + 1
WHERE EXISTS (
    SELECT 1 FROM lead_activities a
    WHERE a.lead_id = l.id
      AND a.metadata->>'followup_type' = 'T3_FOLLOWUP'
)
AND t3_followup_sent = false;

-- Setze T+7 Flag für Leads die bereits T+7 Follow-up erhalten haben
UPDATE leads l
SET t7_followup_sent = true,
    followup_count = COALESCE(followup_count, 0) + 1
WHERE EXISTS (
    SELECT 1 FROM lead_activities a
    WHERE a.lead_id = l.id
      AND a.metadata->>'followup_type' = 'T7_FOLLOWUP'
)
AND t7_followup_sent = false;

-- Update last_followup_at basierend auf neuester Follow-up Aktivität
UPDATE leads l
SET last_followup_at = (
    SELECT MAX(a.activity_date)
    FROM lead_activities a
    WHERE a.lead_id = l.id
      AND a.metadata->>'followup_type' IN ('T3_FOLLOWUP', 'T7_FOLLOWUP')
)
WHERE EXISTS (
    SELECT 1 FROM lead_activities a
    WHERE a.lead_id = l.id
      AND a.metadata->>'followup_type' IN ('T3_FOLLOWUP', 'T7_FOLLOWUP')
)
AND last_followup_at IS NULL;

-- Dokumentation
COMMENT ON TABLE leads IS 'Lead-Tabelle mit Follow-up Tracking Flags (V246: Backfilled für Bestandsdaten)';