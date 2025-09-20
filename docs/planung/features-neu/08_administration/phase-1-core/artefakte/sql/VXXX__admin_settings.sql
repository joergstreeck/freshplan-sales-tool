-- VXXX__admin_settings.sql
-- NOTE: Use './scripts/get-next-migration.sh' to determine the next migration number.
-- Replace 'VXXX' with the returned number before commit/deploy.
-- Foundation Standards: named constraints, indexes, RLS where applicable, and comments.

-- Insert Settings‑Registry keys (if your registry lives in settings_registry)
INSERT INTO settings_registry(key, type, merge_strategy, default_value, json_schema, scope, version)
VALUES
  ('admin.approval.tier1.delay','scalar','scalar','"PT30M"','{"type":"string","pattern":"^P(T.*)$"}','["global","tenant"]',1)
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, merge_strategy, default_value, json_schema, scope, version)
VALUES
  ('admin.approval.tier2.delay','scalar','scalar','"PT10M"','{"type":"string","pattern":"^P(T.*)$"}','["global","tenant"]',1)
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, merge_strategy, default_value, json_schema, scope, version)
VALUES
  ('outbox.rate.max','scalar','scalar','100','{"type":"integer","minimum":0}','["tenant"]',1)
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, merge_strategy, default_value, json_schema, scope, version)
VALUES
  ('dsar.auto_approve','scalar','scalar','false','{"type":"boolean"}','["tenant"]',1)
ON CONFLICT (key) DO NOTHING;
