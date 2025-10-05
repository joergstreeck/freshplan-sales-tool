---
sprint_id: "2.1.8"
title: "DSGVO Compliance & Lead-Import"
doc_type: "konzept"
status: "planned"
owner: "team/leads-backend"
date_start: "2025-10-26"
date_end: "2025-11-01"
modules: ["02_neukundengewinnung"]
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
pr_refs: []
updated: "2025-10-05"
---

# Sprint 2.1.8 – DSGVO Compliance & Lead-Import

**📍 Navigation:** Home → Planung → Sprint 2.1.8

> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen** → `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **MIGRATION-CHECK (🚨 PFLICHT bei DB-Arbeit!)**
>    ```bash
>    # Primär:
>    /Users/joergstreeck/freshplan-sales-tool/scripts/get-next-migration.sh
>    # Ausgabe MUSS V[0-9]+ sein (z.B. V260)
>    # Diese Nummer für heutige DB-Arbeit verwenden!
>
>    # Fallback (Templates ausblenden):
>    ls -la backend/src/main/resources/db/migration/ | grep -v 'VXXX__' | tail -3
>    ```
> 3. **DSGVO-Rechtsgrundlagen prüfen** → EU-DSGVO Art. 15, 17, 7 Abs. 3
> 4. **Backend:** DSGVO-Endpoints + Lead-Import-API
> 5. **Frontend:** Import-Wizard + DSGVO-Aktionen

## 🔧 GIT WORKFLOW (KRITISCH!)

**PFLICHT-REGELN für alle Sprint-Arbeiten:**

### ✅ ERLAUBT (ohne User-Freigabe):
- `git commit` - Commits erstellen wenn User darum bittet
- `git add` - Dateien stagen
- `git status` / `git diff` - Status prüfen
- Feature-Branches anlegen (`git checkout -b feature/...`)

### 🚫 VERBOTEN (ohne explizite User-Freigabe):
- **`git push`** - NIEMALS ohne User-Erlaubnis pushen!
- **PR-Erstellung** - Nur auf explizite Anforderung
- **PR-Merge** - Nur wenn User explizit zustimmt
- **Branch-Deletion** - Remote-Branches nur mit User-OK löschen

### 📋 Standard-Workflow:
1. **Feature-Branch anlegen:** `git checkout -b feature/modXX-sprint-Y.Z-description`
2. **Arbeiten & Committen:** Code schreiben, Tests validieren, `git commit`
3. **User fragen:** "Branch ist bereit. Soll ich pushen und PR erstellen?"
4. **Erst nach Freigabe:** `git push` + PR-Erstellung

**Referenz:** `/CLAUDE.md` → Sektion "🚫 GIT PUSH POLICY (KRITISCH!)"

---


## Sprint-Ziel

**Gesetzliche Pflicht-Features + B2B-Standard Lead-Import**

Nach Sprint 2.1.7 (Team Management & Test Infrastructure) fehlen noch **gesetzlich verpflichtende** DSGVO-Features und der **B2B-Standard Lead-Import**.

**Kern-Deliverables:**
1. **Lead-Import (CSV/Excel)** - Self-Service Bulk-Import für normale User
2. **DSGVO-Auskunfts-Recht (Art. 15)** - Daten-Export auf Anfrage
3. **DSGVO-Lösch-Recht (Art. 17)** - Sofort-Löschung auf Antrag
4. **DSGVO-Einwilligungs-Widerruf (Art. 7 Abs. 3)** - Consent-Widerruf
5. **Advanced Search** - Globale Lead-Suche über alle Felder
6. **BANT-Qualifizierungs-Wizard** - Strukturierte Lead-Bewertung

**Scope-Klarstellung:**
- Sprint 2.1.6: Bestandsleads-Migrations-API (Admin-only, einmaliger Import)
- Sprint 2.1.8: Lead-Import (User-facing, wiederkehrender Bulk-Import)

---

## User Stories

### 1. Lead-Import via CSV/Excel (Self-Service) - 🔴 KRITISCH

**Begründung:** B2B-Standard - Leads kommen aus Messen (50-200 Excel-Zeilen), Partner-Listen, Event-Teilnehmer

**Problem ohne Feature:**
- Messe mit 200 Leads → 200x manuell eingeben = unrealistisch!
- Partner-Listen (Excel) → keine effiziente Übernahme
- Gekaufte Datenbanken → kein Bulk-Import möglich

**Akzeptanzkriterien:**
- [ ] **CSV/Excel Upload** (Multi-Lead-Import)
  - Backend: POST /api/leads/import (multipart/form-data)
  - Unterstützte Formate: .csv, .xlsx
  - Max. Dateigröße: 5 MB (~5000 Leads)
- [ ] **Field-Mapping UI** (Excel-Spalten → Lead-Felder)
  - Auto-Detection: "Firma" → companyName, "E-Mail" → email
  - Custom-Mapping: User kann Spalten manuell zuordnen
  - Pflichtfelder-Validation: companyName, source
- [ ] **Validation & Preview** (vor Import)
  - Zeige erste 5 Zeilen als Preview
  - Validiere ALLE Zeilen (E-Mail-Format, Duplikate)
  - Fehler-Report: "Zeile 23: E-Mail ungültig"
- [ ] **Dedupe während Import**
  - Hard Collisions: Überspringen (mit Warnung)
  - Soft Collisions: User entscheidet (pro Zeile oder Regel)
- [ ] **Import-Protokoll**
  - Erfolgreich importiert: X Leads
  - Übersprungen (Duplikate): Y Leads
  - Fehler: Z Zeilen (mit Details)
  - Download: Fehler-Report als CSV

**Betroffene Komponenten:**
- **Backend:**
  - LeadImportResource.java (POST /api/leads/import)
  - LeadImportService.java (Validation, Dedupe, Batch-Insert)
  - ImportProtocol Entity (Audit-Trail)
- **Frontend:**
  - LeadImportWizard.tsx (3 Schritte: Upload → Mapping → Preview → Import)
  - FileUploadDropzone.tsx (Drag & Drop)
  - ImportResultDialog.tsx (Protokoll anzeigen)

**Aufwand:** 8-12h (Medium Complexity - CSV-Parsing + Dedupe + UI)

**Referenzen:**
- Bestandsleads-Migrations-API (Sprint 2.1.6): Admin-only, einmaliger Import
- Dedupe-Policy: [DEDUPE_POLICY.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md)

---

### 2. DSGVO-Auskunfts-Recht (Art. 15) - 🔴 GESETZLICH PFLICHT

**Begründung:** EU-DSGVO Art. 15 "Auskunftsrecht der betroffenen Person" - **Gesetzliche Pflicht**, Bußgelder bis 20 Mio EUR!

**Rechtsgrundlage:**
```
Art. 15 DSGVO - Auskunftsrecht der betroffenen Person:
Die betroffene Person hat das Recht, von dem Verantwortlichen eine Bestätigung
darüber zu verlangen, ob sie betreffende personenbezogene Daten verarbeitet werden;
ist dies der Fall, so hat sie ein Recht auf Auskunft über diese personenbezogenen Daten.
```

**Antwortfrist:** 1 Monat ab Anfrage (Art. 12 Abs. 3 DSGVO)

**Akzeptanzkriterien:**
- [ ] **Button "Datenabruf anfordern"** (Lead-Detail, Customer-Detail)
  - RBAC: Manager/Admin only (Datenschutzbeauftragter)
  - Confirmation Dialog: "Datenabruf für [Firmenname] erstellen?"
- [ ] **PDF-Export mit allen Daten**
  - Lead/Customer-Stammdaten (Name, Adresse, Kontakt, DSGVO-Felder)
  - Alle Aktivitäten (lead_activities mit Notizen)
  - Alle Notizen (lead_notes - falls implementiert)
  - Consent-Status (consent_given_at, consent_revoked_at)
  - Schutz-Status (registered_at, protection_until, clock_stopped_at)
- [ ] **Audit-Log**
  - Feld: data_request_logs Tabelle
  - Inhalt: requested_by, requested_at, lead_id, pdf_generated
- [ ] **DSGVO-konformes PDF-Layout**
  - Header: "Datenabruf gemäß Art. 15 DSGVO"
  - Abschnitte: Stammdaten, Aktivitäten, Consent, Schutz
  - Footer: Datum, Verantwortlicher (FreshFoodz GmbH)

**Betroffene Komponenten:**
- **Backend:**
  - DataRequestResource.java (POST /api/leads/{id}/data-request)
  - DataRequestService.java (PDF-Generation via iText/Apache PDFBox)
  - data_request_logs Tabelle (Migration-Nummer: siehe `get-next-migration.sh`)
- **Frontend:**
  - DataRequestButton.tsx (Lead-Detail, Customer-Detail)
  - DataRequestDialog.tsx (Confirmation + Download)

**Aufwand:** 6-8h (Medium Complexity - PDF-Generation + DSGVO-Compliance)

**Referenzen:**
- EU-DSGVO Art. 15: https://dsgvo-gesetz.de/art-15-dsgvo/
- PDF-Libraries: iText 7 (GPL) oder Apache PDFBox (Apache License)

---

### 3. DSGVO-Lösch-Recht (Art. 17 "Recht auf Vergessenwerden") - 🔴 GESETZLICH PFLICHT

**Begründung:** EU-DSGVO Art. 17 - **Gesetzliche Pflicht**, Bußgelder bis 20 Mio EUR!

**Rechtsgrundlage:**
```
Art. 17 DSGVO - Recht auf Löschung ("Recht auf Vergessenwerden"):
Die betroffene Person hat das Recht, von dem Verantwortlichen zu verlangen,
dass sie betreffende personenbezogene Daten unverzüglich gelöscht werden.
```

**Unterschied zu Sprint 2.1.6 Pseudonymisierung:**
- Sprint 2.1.6: **Automatische** Pseudonymisierung nach 60 Tagen (Schutz abgelaufen)
- Sprint 2.1.8: **Sofort-Löschung** auf Antrag (jederzeit)

**Akzeptanzkriterien:**
- [ ] **Button "Daten löschen (DSGVO)"** (Lead-Detail, Customer-Detail)
  - RBAC: Manager/Admin only (Datenschutzbeauftragter)
  - Confirmation Dialog: "Lead unwiderruflich löschen? (DSGVO Art. 17)"
- [ ] **Lösch-Grund-Erfassung**
  - Dropdown: "Kunde hat Löschung beantragt (Art. 17)", "Daten nicht mehr erforderlich (Art. 17 Abs. 1a)", "Andere"
  - Freitext: Begründung (min. 10 Zeichen)
- [ ] **Abhängigkeiten prüfen**
  - Lead mit Opportunities → Warnung: "Lead hat 2 Opportunities! Bitte zuerst löschen oder abschließen."
  - Lead mit Audit-Trail → Soft-Delete (deleted_at, deleted_by, deletion_reason)
- [ ] **Soft-Delete (Audit-Trail behalten)**
  - Personenbezogene Daten löschen: companyName → "DSGVO-gelöscht", email → NULL, phone → NULL
  - Audit-Daten behalten: id, created_at, deleted_at, deletion_reason (für Compliance-Audits)
  - Flag: gdpr_deleted = TRUE
- [ ] **Audit-Log**
  - gdpr_deletion_logs Tabelle (deleted_by, deleted_at, reason, lead_id)

**Betroffene Komponenten:**
- **Backend:**
  - GdprDeletionResource.java (DELETE /api/leads/{id}/gdpr-delete)
  - GdprDeletionService.java (Soft-Delete + Audit)
  - Migration-Nummer: siehe `get-next-migration.sh` (gdpr_deleted BOOLEAN, gdpr_deletion_logs Tabelle)
- **Frontend:**
  - GdprDeleteButton.tsx (Lead-Detail, Customer-Detail)
  - GdprDeleteDialog.tsx (Confirmation + Grund-Erfassung)

**Aufwand:** 6-8h (Medium Complexity - Abhängigkeiten-Prüfung + Soft-Delete)

**Referenzen:**
- EU-DSGVO Art. 17: https://dsgvo-gesetz.de/art-17-dsgvo/
- Pseudonymisierung (Sprint 2.1.6): Automated Job, 60 Tage nach Schutz-Ablauf

---

### 4. DSGVO-Einwilligungs-Widerruf (Art. 7 Abs. 3) - 🟡 SOLLTE

**Begründung:** EU-DSGVO Art. 7 Abs. 3 - Widerruf muss so einfach sein wie Einwilligung

**Rechtsgrundlage:**
```
Art. 7 Abs. 3 DSGVO:
Die betroffene Person hat das Recht, ihre Einwilligung jederzeit zu widerrufen.
Durch den Widerruf der Einwilligung wird die Rechtmäßigkeit der aufgrund der
Einwilligung bis zum Widerruf erfolgten Verarbeitung nicht berührt.
```

**Kontext:**
- Sprint 2.1.6: `consent_given_at` Feld für Web-Formular ✅
- Sprint 2.1.8: **Widerruf-Mechanik** hinzufügen

**Akzeptanzkriterien:**
- [ ] **Button "Einwilligung widerrufen"** (Lead-Detail - nur wenn consent_given_at vorhanden)
  - RBAC: Alle Rollen (auch Sales-Partner - Kunde hat Recht!)
  - Confirmation Dialog: "Einwilligung widerrufen? Kunde darf dann NICHT mehr kontaktiert werden!"
- [ ] **Feld: consent_revoked_at** (Migration V265)
  - Timestamp: Wann wurde Einwilligung widerrufen?
  - consent_revoked_by: User-ID (Audit-Trail)
- [ ] **Automatische Sperr-Liste**
  - Flag: contact_blocked = TRUE (nach Widerruf)
  - Keine E-Mails/Anrufe mehr erlaubt
  - Backend-Validation: POST /api/activities → 403 Forbidden wenn contact_blocked = TRUE
- [ ] **UI-Hinweis "Kontakt gesperrt (DSGVO-Widerruf)"**
  - Badge in Lead-Detail: 🔒 "Kontakt gesperrt"
  - Tooltip: "Einwilligung widerrufen am [Datum]. Keine Kontaktaufnahme erlaubt (DSGVO Art. 7 Abs. 3)"
  - Alle Kontakt-Aktionen deaktiviert (E-Mail, Anruf, Meeting)

**Betroffene Komponenten:**
- **Backend:**
  - ConsentResource.java (POST /api/leads/{id}/revoke-consent)
  - Migration-Nummer: siehe `get-next-migration.sh` (consent_revoked_at, consent_revoked_by, contact_blocked)
- **Frontend:**
  - RevokeConsentButton.tsx (Lead-Detail)
  - ContactBlockedBadge.tsx (Lead-Detail, Lead-List)

**Aufwand:** 4-6h (Low Complexity - nur Backend-Feld + UI-Flag)

**Referenzen:**
- EU-DSGVO Art. 7 Abs. 3: https://dsgvo-gesetz.de/art-7-dsgvo/
- Consent-Specification: [DSGVO_CONSENT_SPECIFICATION.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DSGVO_CONSENT_SPECIFICATION.md)

---

### 5. Advanced Search (Globale Lead-Suche) - 🟡 WICHTIG

**Begründung:** Bei 1000+ Leads - "Wo ist der Lead von Messe XYZ?" oder "Welche Leads haben Branche 'Gastronomie' und Stadt 'München'?"

**Aktueller Stand:**
- CustomersPageV2: Universal Search mit Caching ✅
- ABER: Nur Firmenname, nicht alle Felder!

**Akzeptanzkriterien:**
- [ ] **Globale Suche über ALLE Lead-Felder**
  - Firmenname, Stadt, PLZ, Branche
  - Kontaktname, E-Mail, Telefon
  - Notizen, Quelle (source), Erstkontakt-Notizen
- [ ] **Fuzzy-Search (Tippfehler-tolerant)**
  - "Pizza Roma" findet auch "Piza Roma" (Levenshtein Distance ≤ 2)
  - Backend: PostgreSQL pg_trgm Extension
- [ ] **Recent Searches (letzte 5 Suchen speichern)**
  - LocalStorage: recentSearches = ["Gastronomie München", "Messe Frankfurt", ...]
  - Dropdown: Recent Searches anzeigen (Click → Suche ausführen)
- [ ] **Search-Highlighting**
  - Gefundene Terme in Ergebnis-Liste highlighten (z.B. "Pizza **Roma**")
- [ ] **Performance**
  - P95 < 200ms (auch bei 10.000 Leads)
  - Debouncing: 300ms (nicht bei jedem Tastendruck suchen)
  - Caching: 5 Minuten (gleiche Suche → Cache)

**Betroffene Komponenten:**
- **Backend:**
  - LeadSearchResource.java (GET /api/leads/search?q={query})
  - PostgreSQL Full-Text-Search Index (GIN Index auf tsvector)
  - Migration-Nummer: siehe `get-next-migration.sh` (CREATE INDEX idx_leads_fulltext ON leads USING GIN(to_tsvector('german', ...)))
- **Frontend:**
  - AdvancedSearchBar.tsx (Autocomplete mit Recent Searches)
  - SearchResultHighlighter.tsx (Term-Highlighting)

**Aufwand:** 8-12h (High Complexity - Full-Text-Search + Fuzzy + Performance)

**Referenzen:**
- PostgreSQL Full-Text-Search: https://www.postgresql.org/docs/current/textsearch.html
- pg_trgm Extension: https://www.postgresql.org/docs/current/pgtrgm.html

---

### 6. BANT-Qualifizierungs-Wizard - 🟡 WICHTIG

**Begründung:** Progressive Profiling (Sprint 2.1.5) hat Karte 2 "Qualifizierung", ABER nur "Volumen, Küchengröße, Mitarbeiterzahl" - **BANT-Framework fehlt!**

**Was ist BANT?**
- **B**udget: Hat der Lead Budget? (Ja/Nein/Unklar)
- **A**uthority: Ist der Kontakt Entscheider? (Ja/Nein/Unklar)
- **N**eed: Hat der Lead einen konkreten Bedarf? (Ja/Nein/Unklar)
- **T**imeline: Wann will der Lead kaufen? (Sofort/Q1/Q2/Dieses Jahr/Unklar)

**BANT-Score:** 0-4 Punkte (Je mehr "Ja", desto höher der Score)

**Akzeptanzkriterien:**
- [ ] **BANT-Felder im Lead-Entity** (Migration-Nummer: siehe `get-next-migration.sh`)
  - budget_confirmed (ENUM: YES, NO, UNCLEAR)
  - authority_confirmed (ENUM: YES, NO, UNCLEAR)  // Alternativ: decision_maker aus Contact nutzen
  - need_confirmed (ENUM: YES, NO, UNCLEAR)
  - timeline (ENUM: IMMEDIATE, THIS_QUARTER, NEXT_QUARTER, THIS_YEAR, UNCLEAR)
  - bant_score (INTEGER, berechnet: 0-4)
- [ ] **BANT-Wizard (UI - neue Karte in LeadWizard)**
  - Karte 3 (nach Qualifizierung): "BANT-Bewertung"
  - 4 Fragen mit Radio-Buttons (Ja/Nein/Unklar)
  - Automatische Score-Berechnung (0-4)
  - Badge: "🎯 BANT-Score: 3/4" (in Lead-Detail)
- [ ] **Filter: "Qualifizierte Leads" (BANT Score ≥ 3)**
  - IntelligentFilterBar: Quick-Filter "Qualifiziert" (bant_score >= 3)
- [ ] **Dashboard-Widget: BANT-Verteilung**
  - Pie-Chart: Score 0 (X Leads), Score 1 (Y Leads), ..., Score 4 (Z Leads)

**Betroffene Komponenten:**
- **Backend:**
  - Lead Entity: BANT-Felder + Score-Berechnung
  - Migration-Nummer: siehe `get-next-migration.sh` (BANT-Felder + bant_score)
- **Frontend:**
  - LeadWizard.tsx: Karte 3 "BANT-Bewertung" (nach Karte 2 Qualifizierung)
  - BantScoreBadge.tsx (Lead-Detail, Lead-List)
  - BantDashboardWidget.tsx (Dashboard)

**Aufwand:** 8-10h (Medium Complexity - Backend-Felder + UI-Wizard + Dashboard)

**Referenzen:**
- BANT-Methode: https://www.salesforce.com/eu/learning-centre/sales/bant/
- Progressive Profiling: [BUSINESS_LOGIC_LEAD_ERFASSUNG.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/BUSINESS_LOGIC_LEAD_ERFASSUNG.md)

---

## Definition of Done (Sprint 2.1.8)

**Backend (Kern-Deliverables):**
- [ ] **Lead-Import-API funktionsfähig** (CSV/Excel Upload + Field-Mapping + Validation)
- [ ] **DSGVO-Auskunfts-Recht** (PDF-Export mit allen Daten)
- [ ] **DSGVO-Lösch-Recht** (Soft-Delete + Audit-Trail)
- [ ] **DSGVO-Einwilligungs-Widerruf** (Consent-Revoke + Sperr-Liste)
- [ ] **Advanced Search** (Full-Text-Search + Fuzzy + Recent Searches)
- [ ] **BANT-Felder + Score-Berechnung**
- [ ] **Backend Tests ≥80% Coverage**
- [ ] **Migrations deployed** (Nummern: siehe `get-next-migration.sh` – 5 Migrations für DSGVO, Search, BANT)

**Frontend (Kern-Deliverables):**
- [ ] **LeadImportWizard funktional** (Upload → Mapping → Preview → Import)
- [ ] **DSGVO-Aktionen verfügbar** (Datenabruf, Löschung, Widerruf)
- [ ] **Advanced Search UI** (mit Recent Searches + Highlighting)
- [ ] **BANT-Wizard** (Karte 3 in LeadWizard)
- [ ] **Frontend Tests ≥75% Coverage**

**Compliance:**
- [ ] **DSGVO-Compliance validiert** (Art. 15, 17, 7 Abs. 3)
- [ ] **Audit-Logs für alle DSGVO-Aktionen**
- [ ] **Datenschutz-Dokumentation aktualisiert**

**Dokumentation:**
- [ ] **Lead-Import Runbook** (CSV-Format, Field-Mapping, Fehler-Handling)
- [ ] **DSGVO-Prozess dokumentiert** (Auskunft, Löschung, Widerruf)
- [ ] **BANT-Methode erklärt** (für Sales-Team)

---

## Risiken & Mitigation

**Sprint 2.1.8 spezifisch:**
- **Lead-Import Duplikate:** Dedupe während Import → Collision-Report anzeigen
  - **Mitigation:** User entscheidet pro Zeile (Skip/Override)
- **DSGVO-Löschung mit Abhängigkeiten:** Lead hat Opportunities → Warnung
  - **Mitigation:** Soft-Delete + Audit-Trail (Compliance-konform)
- **Advanced Search Performance:** Bei 10.000 Leads langsam
  - **Mitigation:** PostgreSQL GIN Index + Caching + Debouncing

---

## Abhängigkeiten

- Sprint 2.1.6 (Admin-Features) muss abgeschlossen sein
- Sprint 2.1.7 (Team Management) sollte abgeschlossen sein (für RLS-Tests)
- PostgreSQL 14+ für Full-Text-Search Features

---

## Test Strategy

**DSGVO-Tests:**
- Unit-Tests: Auskunfts-Recht, Lösch-Recht, Widerruf-Logik
- Integration-Tests: PDF-Generation, Soft-Delete, Audit-Logs
- Manual-Tests: DSGVO-Compliance mit Datenschutzbeauftragtem validieren

**Import-Tests:**
- CSV-Parsing (verschiedene Formate: UTF-8, Windows-1252)
- Dedupe während Import (Hard/Soft Collisions)
- Performance-Test: 5000 Leads in <10 Sekunden importieren

**Search-Tests:**
- Full-Text-Search mit verschiedenen Queries
- Fuzzy-Search (Tippfehler-Toleranz)
- Performance-Test: P95 < 200ms bei 10.000 Leads

---

## Monitoring & KPIs

- **Import-Erfolgsrate:** Ziel >95% (ohne Duplikate/Fehler)
- **DSGVO-Response-Zeit:** Ziel <1 Monat (gesetzliche Frist)
- **Search-Performance:** P95 <200ms
- **BANT-Score-Verteilung:** Dashboard-Widget für Sales-Team

---

## Next Sprint Preview

**Sprint 2.1.9: "Lead-Kollaboration & Tracking"**
- Lead-Notizen & Kommentare (Team-Kollaboration)
- Lead-Status-Änderungs-Historie (Audit-Trail)
- Lead-Temperatur (Hot/Warm/Cold)

**Geschätzter Aufwand:** 12-17h (~2-3 Tage)

---

**Erstellt:** 2025-10-05
**Status:** 📅 PLANNED
**Gesamt-Aufwand:** 40-56h (~1 Woche)
