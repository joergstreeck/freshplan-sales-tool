---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "konzept"
status: "draft"
sprint: "2.1.5"
owner: "team/qa"
updated: "2025-09-28"
---

# Sprint 2.1.5 – QA Checklist

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → QA Checklist

## Pre-Deployment Checklist

### Database Migrations
- [ ] V249 Migration läuft erfolgreich in Test-Umgebung
- [ ] V250 Migration läuft erfolgreich in Test-Umgebung
- [ ] Rollback-Scripts getestet und dokumentiert
- [ ] Performance-Impact gemessen (< 5 Sekunden für Prod-Datenvolumen)
- [ ] Keine Locks auf leads-Tabelle während Migration

### Lead Protection System
- [ ] 6-Monats-Schutzfrist wird korrekt berechnet
- [ ] 60-Tage-Deadline wird korrekt gesetzt
- [ ] Status-Übergang protected → warning nach 53 Tagen
- [ ] Status-Übergang warning → expired nach 60 Tagen
- [ ] Stop-the-Clock pausiert Deadline korrekt
- [ ] Stop-the-Clock erfordert Pflichtgrund
- [ ] Audit-Events werden für alle Statuswechsel erzeugt

### Progressive Profiling
- [ ] Stage 0: Nur Firma + Stadt erlaubt (keine personenbezogenen Daten)
- [ ] Stage 1: Optionale Kontaktdaten werden akzeptiert
- [ ] Stage 2: VAT-ID Validierung funktioniert
- [ ] Stage-Übersprung wird verhindert
- [ ] Validierungsfehler werden nutzerfreundlich angezeigt
- [ ] Form-State bleibt bei Browser-Refresh erhalten

### API Testing
- [ ] POST /api/leads mit stage=0 → 201 Created
- [ ] POST /api/leads mit Duplikat → 409 Conflict
- [ ] POST /api/leads mit Soft-Duplikat → 202 Accepted + Kandidaten
- [ ] Idempotency-Key verhindert doppelte Einträge
- [ ] Response-Format entspricht OpenAPI-Spezifikation
- [ ] Rate-Limiting greift bei > 100 Requests/Minute

### Fuzzy Matching
- [ ] Ähnliche Firmennamen werden erkannt (Score > 0.7)
- [ ] Gleiche Stadt erhöht Match-Score
- [ ] Kandidatenliste ist nach Score sortiert
- [ ] Maximal 5 Kandidaten werden zurückgegeben
- [ ] Performance < 200ms für typische Anfrage

### Frontend Components
- [ ] LeadWizard zeigt alle 3 Stufen korrekt
- [ ] LeadProtectionBadge zeigt verbleibende Tage
- [ ] ActivityTimeline lädt Events paginiert
- [ ] DuplicateReviewModal zeigt Kandidaten mit Score
- [ ] Merge/Reject/Create-New Buttons funktionieren
- [ ] Accessibility: Keyboard-Navigation funktioniert
- [ ] Accessibility: Screen-Reader Support getestet

### Security
- [ ] SQL-Injection Tests auf alle neuen Endpoints
- [ ] XSS-Tests auf alle Input-Felder
- [ ] CSRF-Token wird validiert
- [ ] Rate-Limiting funktioniert
- [ ] Audit-Log enthält User-ID und Timestamp
- [ ] Keine PII in Logs oder Error-Messages

### Performance
- [ ] Frontend Bundle < 200KB (gzipped)
- [ ] API Response P95 < 200ms
- [ ] Keine N+1 Queries in Lead-Listen
- [ ] Protection-Check Batch-Job < 500ms für 1000 Leads
- [ ] Memory-Profiling: Keine Leaks nach 1h Nutzung

### Data Migration
- [ ] Bestehende Leads erhalten Protection-Records
- [ ] Source-Feld wird mit 'manual' befüllt
- [ ] Stage wird basierend auf vorhandenen Daten gesetzt
- [ ] Keine Datenverluste nach Migration
- [ ] Backup vor Migration erstellt

### DSGVO Compliance
- [ ] Stage 0 speichert keine personenbezogenen Daten
- [ ] Löschfunktion entfernt alle verknüpften Daten
- [ ] Export-Funktion enthält alle gespeicherten Daten
- [ ] Consent-Tracking für Marketing-Source
- [ ] Datenschutzerklärung verlinkt und aktuell

### Integration Tests
- [ ] E2E: Kompletter Lead-Wizard Durchlauf
- [ ] E2E: Duplikat-Review und Merge
- [ ] E2E: Stop-the-Clock mit Manager-Approval
- [ ] E2E: Activity-Logging und Timeline-Update
- [ ] API-Integration mit Legacy-System getestet

### Browser Compatibility
- [ ] Chrome/Edge (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Mobile: iOS Safari
- [ ] Mobile: Chrome Android

### Monitoring & Alerts
- [ ] Metriken für Protection-Stati eingerichtet
- [ ] Alert bei > 10% expired Leads
- [ ] Alert bei Failed Protection-Jobs
- [ ] Dashboard für Lead-Statistiken verfügbar
- [ ] Error-Tracking in Sentry konfiguriert

### Documentation
- [ ] User-Dokumentation aktualisiert
- [ ] API-Dokumentation vollständig
- [ ] Deployment-Guide vorhanden
- [ ] Rollback-Prozedur dokumentiert
- [ ] Known Issues dokumentiert

### Regression Tests
- [ ] Existing Lead CRUD funktioniert weiterhin
- [ ] Lead-Search Performance unverändert
- [ ] Lead-Export Format kompatibel
- [ ] Bestehende Integrationen funktionieren
- [ ] Keine Breaking Changes ohne Migration

## Post-Deployment Verification

### Smoke Tests (Production)
- [ ] Health-Check Endpoints antworten
- [ ] Lead-Creation funktioniert
- [ ] Protection-Badge wird angezeigt
- [ ] Keine Error-Logs in ersten 10 Minuten
- [ ] Monitoring zeigt normale Werte

### Feature Flags
- [ ] lead_protection aktiviert und funktional
- [ ] progressive_profiling aktiviert
- [ ] fuzzy_matching aktiviert
- [ ] Fallback-Verhalten bei Deaktivierung getestet

### User Acceptance
- [ ] Key-User haben Feature getestet
- [ ] Feedback-Kanal eingerichtet
- [ ] Training-Material bereitgestellt
- [ ] Support-Team instruiert

## Sign-Off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| QA Lead | | | |
| Product Owner | | | |
| Tech Lead | | | |
| Security | | | |
| Data Protection | | | |

## Notes & Issues

_Platz für Anmerkungen während des QA-Prozesses:_

---

**QA Status:** ⏳ In Progress
**Target Release:** 2025-10-11
**Last Updated:** 2025-09-28