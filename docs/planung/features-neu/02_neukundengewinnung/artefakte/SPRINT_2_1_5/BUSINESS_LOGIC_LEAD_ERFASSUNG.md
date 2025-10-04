---
sprint: "2.1.5"
domain: "business-logic"
doc_type: "overview"
status: "approved"
owner: "team/product"
updated: "2025-10-04"
---

# Business-Logik: Lead-Erfassung (Progressive Profiling)

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Business Logic

> **🎯 Zweck:**
> Zentrale Übersicht der Business-Logik für Lead-Erfassung im B2B-Kontext.
> Verweist auf detaillierte Artefakte, vermeidet Redundanz.

---

## 1. Progressive Profiling (3-Stufen-Modell)

### Grundprinzip: Jede Karte separat speicherbar

**Best Practice:** User kann nach jeder Karte speichern und Dialog schließen.
- **KEIN Zwang**, alle Karten durchzugehen
- **Niedrige Hürde** für schnelle Vormerkung (Messe/Telefon)
- **Flexible Datenentwicklung** über Zeit

### Karte 0: Vormerkung (stage=0)

**Zweck:** Lead mit minimalen Infos anlegen

**Pflichtfelder:**
- Firmenname (min. 2 Zeichen)
- Quelle (LeadSource: MESSE, EMPFEHLUNG, TELEFON, WEB_FORMULAR, PARTNER, SONSTIGE)

**Optionale Felder:**
- Stadt, PLZ, Branche
- **Erstkontakt (empfohlen):** Kanal, Zeitpunkt, Notizen (≥10 Zeichen)

**Erstkontakt-Logik:**
- **WENN dokumentiert →** Schutz startet (registered_at = now, protection_until = +6 Monate)
- **WENN NICHT →** Pre-Claim (registered_at = NULL, 10-Tage-Frist)
- **Nachträglich möglich:** Lead öffnen → Activity FIRST_CONTACT_DOCUMENTED hinzufügen

**Button:** `[Vormerkung speichern]` → POST /api/leads mit stage=0, schließt Dialog

📖 **Details:** [FRONTEND_DELTA.md Section 0](./FRONTEND_DELTA.md#0-progressive-profiling-ux-regeln-best-practice)

---

### Karte 1: Registrierung (stage=1)

**Zweck:** Kontaktdaten erfassen (Lead erreichbar machen)

**Pflichtfelder:**
- **Mind. ein Kontaktkanal:** E-Mail ODER Telefon

**Optionale Felder:**
- Vorname, Nachname

**DSGVO-Logik:**
- **Vertriebs-Wizard:** KEINE Consent-Checkbox (Kunde sitzt nicht daneben)
  - Stattdessen: Hinweis "Berechtigtes Interesse (Art. 6 Abs. 1 lit. f DSGVO)"
  - Link zu Gesetzestext (Popup/Neues Fenster)
- **Web-Formular (Sprint 2.1.6):** Consent-Checkbox PFLICHT (Kunde gibt selbst Daten ein)

**Schutz:** Startet **NICHT** automatisch bei Kontaktdaten (nur bei Erstkontakt-Dokumentation)

**Button:** `[Registrierung speichern]` → POST /api/leads mit stage=1, schließt Dialog

📖 **Details:** [DSGVO_CONSENT_SPECIFICATION.md](./DSGVO_CONSENT_SPECIFICATION.md)

---

### Karte 2: Qualifizierung (stage=2)

**Zweck:** Geschäftliche Details nachtragen

**Alle Felder optional:**
- Geschätztes Volumen, Küchengröße, Mitarbeiterzahl, Website, Branche (Details)

**Button:** `[Qualifizierung speichern]` → POST /api/leads mit stage=2, schließt Dialog

📖 **Details:** [FRONTEND_DELTA.md Section 0](./FRONTEND_DELTA.md#karte-2-qualifizierung)

---

## 2. Pre-Claim Mechanik

**Definition:** Vormerkung ohne aktiven Schutz

**Bedingungen:**
- `registered_at IS NULL` (kein Erstkontakt dokumentiert)
- 10-Tage-Frist ab `created_at`
- **KEINE Zuordnung** zum Partner (kann von anderen übernommen werden)

**UI:**
- Badge: "⏳ Vormerkung läuft ab in X Tagen"
- Filter: "Pre-Claim Leads" (eigener Filter in LeadList)
- Warn-Farbe bei < 3 Tagen

**Übergang zu Schutz:**
- Erstkontakt nachträglich dokumentieren → Pre-Claim endet, Schutz startet
- **Backdating:** Erst Sprint 2.1.6 (außer Altdaten-Migration)

📖 **Details:** [PRE_CLAIM_LOGIC.md](./PRE_CLAIM_LOGIC.md)

---

## 3. Lead-Schutz (6 Monate + 60-Tage-Regel)

**Schutz-Start:**
- **NUR** bei Erstkontakt-Dokumentation (FIRST_CONTACT_DOCUMENTED Activity)
- **NICHT** bei Kontaktdaten-Erfassung
- **NICHT** bei Vormerkung ohne Erstkontakt

**Schutz-Dauer:**
- **6 Monate** ab registered_at (Basis-Schutz)
- **60-Tage-Progress-Regel:** Timer resettet bei Progress-Aktivität

**Progress-Aktivitäten (countsAsProgress = TRUE):**
- QUALIFIED_CALL
- MEETING
- DEMO
- ROI_PRESENTATION
- SAMPLE_SENT

**Non-Progress-Aktivitäten (countsAsProgress = FALSE):**
- NOTE, FOLLOW_UP, EMAIL, CALL, SAMPLE_FEEDBACK
- FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED (System)

📖 **Details:** [ACTIVITY_TYPES_PROGRESS_MAPPING.md](./ACTIVITY_TYPES_PROGRESS_MAPPING.md)

---

## 4. Dedupe-Logik (Hard vs. Soft Collisions)

**HTTP 409 Conflict** mit RFC 7807 Problem+JSON

### Hard Collision (Exact Match)
- **Trigger:** companyName + city + postalCode identisch (normalisiert)
- **Response:** Problem+JSON **OHNE** `extensions.severity`
- **Override:** Nur Manager/Admin mit `overrideReason` (min. 10 Zeichen)
- **UI:** DuplicateLeadDialog (Manager-only)

### Soft Collision (Similar Match)
- **Trigger:** companyName ähnlich (Levenshtein ≤ 2) ODER email identisch
- **Response:** Problem+JSON **MIT** `extensions.severity: "WARNING"`
- **Override:** Alle Rollen mit `reason` (min. 10 Zeichen)
- **UI:** SimilarLeadDialog (alle Rollen)

**Problem+JSON Structure:**
```json
{
  "type": "https://api.freshfoodz.com/problems/duplicate-lead",
  "title": "Duplicate Lead",
  "status": 409,
  "detail": "Lead mit ähnlichen Daten existiert bereits.",
  "extensions": {
    "severity": "WARNING",  // NUR bei Soft Collision
    "duplicates": [
      {
        "leadId": "uuid",
        "companyName": "Pizza Roma GmbH",
        "city": "München",
        "postalCode": "80331",
        "ownerUserId": "uuid"
      }
    ]
  }
}
```

📖 **Details:** [DEDUPE_POLICY.md](./DEDUPE_POLICY.md)

---

## 5. DSGVO Compliance (Berechtigtes Interesse vs. Einwilligung)

### Vertriebs-Wizard (Sprint 2.1.5)
- **KEINE Consent-Checkbox** (Kunde sitzt nicht daneben)
- **Rechtsgrundlage:** Art. 6 Abs. 1 lit. f DSGVO (Berechtigtes Interesse)
- **UI:** Hinweisbox mit Link zu Gesetzestext
- **Verarbeitung:** B2B-Geschäftsanbahnung

### Web-Formular (Sprint 2.1.6)
- **Consent-Checkbox PFLICHT** (Kunde gibt selbst Daten ein)
- **Rechtsgrundlage:** Art. 6 Abs. 1 lit. a DSGVO (Einwilligung)
- **Backend-Feld:** `consent_given_at` (V259 Migration)
- **Widerruf:** Jederzeit möglich

**Quellenabhängige Logik:**
```typescript
if (source === 'WEB_FORMULAR') {
  // Einwilligung erforderlich
  consentRequired = true;
} else {
  // Berechtigtes Interesse (Art. 6 Abs. 1 lit. f)
  consentRequired = false;
}
```

📖 **Details:** [DSGVO_CONSENT_SPECIFICATION.md](./DSGVO_CONSENT_SPECIFICATION.md)

---

## 6. Erstkontakt-Dokumentation

**Zweck:** Initialen Kontakt festhalten (startet Schutz)

**Activity-Type:** FIRST_CONTACT_DOCUMENTED
- `countsAsProgress: false` (System Activity)
- Startet Schutz, aber **KEIN** 60-Tage-Progress-Reset

**Felder:**
- **Kanal:** MESSE, PHONE, EMAIL, REFERRAL, OTHER
- **Zeitpunkt:** datetime-local (ISO 8601)
- **Notizen:** min. 10 Zeichen

**Transformation (Frontend → Backend):**
```typescript
activities: [{
  activityType: 'FIRST_CONTACT_DOCUMENTED',
  performedAt: formData.firstContact.performedAt,
  summary: `${channel}: ${notes}`,
  countsAsProgress: false,
  metadata: {
    channel: formData.firstContact.channel,
    notes: formData.firstContact.notes
  }
}]
```

**Nachträglich:**
- Lead öffnen → Activity hinzufügen (Type: FIRST_CONTACT_DOCUMENTED)
- **Zeitpunkt:** Aktuell (Backdating erst Sprint 2.1.6)

📖 **Details:** [FRONTEND_DELTA.md Section 3](./FRONTEND_DELTA.md#3-erstkontakt-block-ui-transformation)

---

## 7. Anti-Patterns (was NICHT tun)

1. ❌ **Consent-Checkbox im Vertriebs-Wizard** (nur bei WEB_FORMULAR)
2. ❌ **Erstkontakt mehrfach abfragen** (nur auf Karte 0)
3. ❌ **Speichern ans Ende koppeln** (jede Karte separat speicherbar)
4. ❌ **Schutz bei Kontaktdaten starten** (nur bei Erstkontakt)
5. ❌ **Alle Karten erzwingen** (User kann jederzeit speichern & schließen)
6. ❌ **Hardcoded `source: 'manual'`** (User wählt Source explizit)
7. ❌ **consentGivenAt in Sprint 2.1.5 senden** (Backend-Feld erst V259)

📖 **Details:** [FRONTEND_DELTA.md Section 0](./FRONTEND_DELTA.md#-nicht-tun-anti-patterns)

---

## 8. Verwandte Artefakte (Sprint 2.1.5)

- 📋 **[FRONTEND_DELTA.md](./FRONTEND_DELTA.md)** - Zentrale Frontend-Spezifikation (877 Zeilen)
- 🔒 **[PRE_CLAIM_LOGIC.md](./PRE_CLAIM_LOGIC.md)** - Pre-Claim Mechanik (10-Tage-Frist)
- 🔒 **[DSGVO_CONSENT_SPECIFICATION.md](./DSGVO_CONSENT_SPECIFICATION.md)** - DSGVO Compliance
- 📊 **[ACTIVITY_TYPES_PROGRESS_MAPPING.md](./ACTIVITY_TYPES_PROGRESS_MAPPING.md)** - 13 Activity-Types
- 🔄 **[DEDUPE_POLICY.md](./DEDUPE_POLICY.md)** - Hard/Soft Collisions (409 Handling)
- 📝 **[CONTRACT_MAPPING.md](./CONTRACT_MAPPING.md)** - Vertragliche Regeln (6M + 60T + 10T)

---

## 9. Sprint 2.1.6 Erweiterungen (geplant)

- ✅ **Backdating:** Activity-Zeitpunkte in Vergangenheit setzen
- ✅ **consent_given_at:** Backend-Feld für Web-Formular (V259)
- ✅ **Bestandsleads-Migration:** Altdaten-Import mit historischen Timestamps
- ✅ **Nightly Jobs:** Automatische Warn-/Expiry-Emails
- ✅ **Fuzzy-Matching:** Erweiterte Dedupe mit Levenshtein + pg_trgm

📖 **Details:** [SPRINT_2_1_6/SUMMARY.md](../SPRINT_2_1_6/SUMMARY.md)

---

**📅 Erstellt:** 2025-10-04
**🔄 Letzte Aktualisierung:** 2025-10-04
**✅ Status:** Approved (Production-Ready)
