-- V240: Lead email deduplication cleanup
-- Sprint 2.1: Entfernt redundanten Index (bereits in V234 vorhanden)
-- Dieser Migration ist ein No-Op, da der Index idx_leads_email_normalized
-- bereits in V234__lead_dod_requirements.sql erstellt wurde

-- Hinweis: Der Index idx_leads_email_normalized mit WHERE-Klausel für
-- email_normalized IS NOT NULL AND status != 'DELETED' existiert bereits
-- aus Migration V234 und muss nicht dupliziert werden.

-- Diese Migration wird beibehalten für Migrations-Kontinuität,
-- führt aber keine Änderungen durch.