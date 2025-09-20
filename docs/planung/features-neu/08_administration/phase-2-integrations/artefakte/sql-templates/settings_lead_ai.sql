-- Migration: $(./scripts/get-next-migration.sh)__settings_lead_ai.sql
-- Settings Registry kompatibel zu Modul 06 (type, scope, schema, merge_strategy)

-- Lead-Protection (global, replace)
INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('lead.protection.baseMonths','scalar','["global"]','{"months":6}','{"type":"object","required":["months"],"properties":{"months":{"type":"integer","minimum":1,"maximum":24}}}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('lead.protection.progressDays','scalar','["global"]','{"days":60}','{"type":"object","required":["days"],"properties":{"days":{"type":"integer","minimum":7,"maximum":120}}}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('lead.protection.graceDays','scalar','["global"]','{"days":10}','{"type":"object","required":["days"],"properties":{"days":{"type":"integer","minimum":1,"maximum":30}}}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('lead.protection.mode','scalar','["global"]','{"mode":"GREATEST"}','{"type":"object","properties":{"mode":{"type":"string","enum":["GREATEST","LEAST"]}}}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

-- Reminder-Kanäle (org/tenant, append)
INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('lead.protection.reminder.channels','list','["tenant","org"]','["EMAIL","INAPP"]','{"type":"array","items":{"type":"string","enum":["EMAIL","INAPP","TASK"]}}','append')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

-- Notify-Rollen (org/tenant, append)
INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('lead.protection.notify.roles','list','["tenant","org"]','["manager"]','{"type":"array","items":{"type":"string"}}','append')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

-- AI Budget & Routing (org/user, replace)
INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('ai.budget.monthly.cap','scalar','["tenant","org"]','1000','{"type":"number","minimum":0}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('ai.routing.confidence.threshold','scalar','["user","org","tenant"]','0.7','{"type":"number","minimum":0,"maximum":1}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('ai.routing.demotion.cooldown','scalar','["org","tenant"]','"PT30M"','{"type":"string","pattern":"^P(T.*)$"}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('ai.cache.ttl','scalar','["org","tenant"]','"PT8H"','{"type":"string","pattern":"^P(T.*)$"}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

-- Credit / Peak SLO
INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('credit.cache.ttl','scalar','["org","tenant"]','"PT8H"','{"type":"string","pattern":"^P(T.*)$"}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('credit.peak.slo.p95.ms','scalar','["global"]','500','{"type":"integer","minimum":100,"maximum":2000}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;

INSERT INTO settings_registry(key, type, scope, default_value, schema, merge_strategy) VALUES
 ('credit.batch.window.ms','scalar','["org","tenant"]','50','{"type":"integer","minimum":0,"maximum":2000}','replace')
ON CONFLICT (key) DO UPDATE SET schema=EXCLUDED.schema, merge_strategy=EXCLUDED.merge_strategy;
