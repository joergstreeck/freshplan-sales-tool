-- Migration V214: Ändere user_id von UUID zu VARCHAR für Keycloak-Kompatibilität
-- Keycloak liefert die Sub-Claim als String, nicht immer als gültige UUID

-- Schritt 1: Neue Spalte als VARCHAR hinzufügen
ALTER TABLE audit_logs 
ADD COLUMN user_id_new VARCHAR(100);

-- Schritt 2: Konvertiere existierende UUIDs zu Strings
UPDATE audit_logs 
SET user_id_new = user_id::text 
WHERE user_id IS NOT NULL;

-- Schritt 3: Alte Spalte entfernen
ALTER TABLE audit_logs 
DROP COLUMN user_id;

-- Schritt 4: Neue Spalte umbenennen
ALTER TABLE audit_logs 
RENAME COLUMN user_id_new TO user_id;

-- Schritt 5: NOT NULL Constraint hinzufügen
ALTER TABLE audit_logs 
ALTER COLUMN user_id SET NOT NULL;

-- Schritt 6: Index neu erstellen
DROP INDEX IF EXISTS idx_audit_user;
CREATE INDEX idx_audit_user ON audit_logs(user_id);

-- Kommentar hinzufügen
COMMENT ON COLUMN audit_logs.user_id IS 'User ID als String für Keycloak Sub-Claim Kompatibilität';