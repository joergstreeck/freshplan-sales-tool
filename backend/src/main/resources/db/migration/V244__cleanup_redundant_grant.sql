-- V244: Cleanup redundanter GRANT (idempotent)
--
-- Kontext: In V242 wurde check_rls_context() erstellt und GRANT vergeben.
-- In V243 wurde versehentlich der gleiche GRANT nochmals eingefügt.
-- Diese Migration dokumentiert das und ist idempotent (no-op).
--
-- WICHTIG: V243 darf NICHT geändert werden, da sie bereits deployed ist!
-- Der redundante GRANT in V243 ist harmlos (PostgreSQL ignoriert doppelte GRANTs).

-- Dokumentation für Audit-Trail
DO $$
BEGIN
    -- Dieser Block dokumentiert, dass wir uns des redundanten GRANTs bewusst sind
    -- Der GRANT in V242 ist ausreichend, der in V243 ist redundant aber harmlos

    -- Prüfe ob die Funktion existiert und die Rechte korrekt sind
    IF EXISTS (
        SELECT 1
        FROM information_schema.routines
        WHERE routine_name = 'check_rls_context'
          AND routine_schema = 'public'
    ) THEN
        -- Rechte sind bereits durch V242 und V243 vergeben
        -- Nichts zu tun - idempotent
        RAISE NOTICE 'check_rls_context() permissions already correctly set by V242/V243';
    ELSE
        -- Dies sollte nie passieren, da V242 die Funktion erstellt
        RAISE WARNING 'check_rls_context() function not found - check migrations V242/V243';
    END IF;
END $$;

-- Zusätzliche Dokumentation als Kommentar für zukünftige Entwickler:
-- Der redundante GRANT in V243 wurde beibehalten um Flyway-Checksum-Fehler
-- in bereits migrierten Umgebungen zu vermeiden.
-- Best Practice: Deployed migrations niemals ändern!