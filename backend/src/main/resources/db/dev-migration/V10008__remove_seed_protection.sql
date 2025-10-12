-- V10008__remove_seed_protection.sql
-- Entfernt verbleibende SEED-Protection-Trigger und zugehörige Funktionen
-- Datum: 2025-08-20
-- Zweck: Cleanup nach Phase 1 - SEED-Infrastruktur-Entfernung

DO $$
DECLARE
  r RECORD;
BEGIN
  -- 1) Alle relevanten Trigger finden (Name oder Funktionscode deutet auf SEED-Schutz hin)
  FOR r IN
    SELECT 
      n.nspname   AS schema_name,
      c.relname   AS table_name,
      tg.tgname   AS trigger_name,
      p.oid       AS func_oid,
      p.proname   AS func_name,
      pg_get_function_identity_arguments(p.oid) AS func_args
    FROM pg_trigger tg
    JOIN pg_class c       ON c.oid = tg.tgrelid
    JOIN pg_proc  p       ON p.oid = tg.tgfoid
    JOIN pg_namespace n   ON n.oid = c.relnamespace
    WHERE NOT tg.tgisinternal
      AND (
           tg.tgname ILIKE '%seed%' OR
           p.proname ILIKE '%seed%' OR
           pg_get_functiondef(p.oid) ILIKE '%SEED PROTECTION%'  -- Text im Funktionskörper
      )
  LOOP
    -- Log what we're removing (for audit trail)
    RAISE NOTICE 'Removing SEED protection trigger: %.% trigger %', 
                 r.schema_name, r.table_name, r.trigger_name;
    
    -- 2) Trigger löschen (idempotent)
    EXECUTE format('DROP TRIGGER IF EXISTS %I ON %I.%I;', 
                   r.trigger_name, r.schema_name, r.table_name);

    -- 3) Falls keine weiteren Trigger mehr auf die Funktion zeigen: Funktion löschen (idempotent)
    IF NOT EXISTS (SELECT 1 FROM pg_trigger t WHERE t.tgfoid = r.func_oid) THEN
      RAISE NOTICE 'Removing orphaned SEED function: %.%(%)', 
                   r.schema_name, r.func_name, r.func_args;
      EXECUTE format('DROP FUNCTION IF EXISTS %I.%I(%s);', 
                     r.schema_name, r.func_name, r.func_args);
    END IF;
  END LOOP;
  
  -- 4) Zusätzliche Bereinigung: Explizit bekannte SEED-Funktionen entfernen
  -- Falls sie ohne Trigger existieren sollten
  DROP FUNCTION IF EXISTS public.forbid_seed_delete() CASCADE;
  DROP FUNCTION IF EXISTS public.forbid_seed_update() CASCADE;
  DROP FUNCTION IF EXISTS public.forbid_seed_truncate() CASCADE;
  DROP FUNCTION IF EXISTS public.protect_seed_data() CASCADE;
  DROP FUNCTION IF EXISTS public.prevent_seed_deletion() CASCADE;
  
  -- 5) Abschlussmeldung
  RAISE NOTICE 'SEED protection cleanup completed successfully';
END $$;

-- Verifizierung: Zeige verbleibende Trigger (sollte leer sein für SEED-bezogene)
DO $$
DECLARE
  count_triggers INTEGER;
BEGIN
  SELECT COUNT(*) INTO count_triggers
  FROM pg_trigger tg
  JOIN pg_proc p ON p.oid = tg.tgfoid
  WHERE NOT tg.tgisinternal
    AND (tg.tgname ILIKE '%seed%' OR p.proname ILIKE '%seed%');
    
  IF count_triggers > 0 THEN
    RAISE WARNING 'Found % remaining SEED-related triggers after cleanup!', count_triggers;
  ELSE
    RAISE NOTICE 'Verification passed: No SEED-related triggers remaining';
  END IF;
END $$;