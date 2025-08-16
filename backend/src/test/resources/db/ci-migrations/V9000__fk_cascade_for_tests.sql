-- CI ONLY: Normalize FKs referencing customers(id) for reliable test cleanup.
-- Self-ref (customers->customers) -> ON DELETE SET NULL
-- Others                          -> ON DELETE CASCADE
DO $$
DECLARE
  r record;
  child_cols text;
  parent_cols text;
  is_self boolean;
  target_action text;
  target_code char(1);
  changed_cascade int := 0;
  changed_set_null int := 0;
  total_seen int := 0;
BEGIN
  RAISE NOTICE 'CI FK Migration V9000 starting...';

  IF to_regclass('public.customers') IS NULL THEN
    RAISE NOTICE 'customers table missing -> skip CI FK migration';
    RETURN;
  END IF;

  FOR r IN
    SELECT conname,
           conrelid,  -- OID für JOINs behalten
           confrelid, -- OID für JOINs behalten
           conrelid::regclass AS child,
           confrelid::regclass AS parent,
           conkey, confkey,
           confdeltype,
           (conrelid = confrelid) AS self_ref
    FROM pg_constraint
    WHERE contype = 'f'
      AND confrelid = 'public.customers'::regclass
  LOOP
    total_seen := total_seen + 1;

    -- Zielaktion ableiten
    is_self := r.self_ref;
    IF is_self THEN
      target_action := 'ON DELETE SET NULL';
      target_code   := 'n'; -- SET NULL
    ELSE
      target_action := 'ON DELETE CASCADE';
      target_code   := 'c'; -- CASCADE
    END IF;

    -- Bereits korrekt? Dann nur loggen und weiter
    IF r.confdeltype = target_code THEN
      RAISE NOTICE 'FK % on % already % -> skip', r.conname, r.child, target_action;
      CONTINUE;
    END IF;

    -- Spaltenlisten für FK neu ermitteln (mit OIDs)
    SELECT string_agg(quote_ident(a.attname), ', ' ORDER BY u.i) INTO child_cols
    FROM unnest(r.conkey) WITH ORDINALITY AS u(attnum, i)
    JOIN pg_attribute a ON a.attrelid = r.conrelid AND a.attnum = u.attnum;

    SELECT string_agg(quote_ident(a.attname), ', ' ORDER BY u.i) INTO parent_cols
    FROM unnest(r.confkey) WITH ORDINALITY AS u(attnum, i)
    JOIN pg_attribute a ON a.attrelid = r.confrelid AND a.attnum = u.attnum;

    -- FK droppen & neu mit Zielaktion anlegen
    EXECUTE format('ALTER TABLE %s DROP CONSTRAINT IF EXISTS %I', r.child, r.conname);

    IF is_self THEN
      EXECUTE format(
        'ALTER TABLE %s ADD CONSTRAINT %I FOREIGN KEY (%s) REFERENCES %s (%s) ON DELETE SET NULL',
        r.child, r.conname, child_cols, r.parent, parent_cols
      );
      changed_set_null := changed_set_null + 1;
    ELSE
      EXECUTE format(
        'ALTER TABLE %s ADD CONSTRAINT %I FOREIGN KEY (%s) REFERENCES %s (%s) ON DELETE CASCADE',
        r.child, r.conname, child_cols, r.parent, parent_cols
      );
      changed_cascade := changed_cascade + 1;
    END IF;

    RAISE NOTICE 'FK % on % -> % applied', r.conname, r.child, target_action;
  END LOOP;

  RAISE NOTICE 'CI FK Migration complete: % FKs processed; adjusted: % CASCADE, % SET NULL',
               total_seen, changed_cascade, changed_set_null;
END $$;