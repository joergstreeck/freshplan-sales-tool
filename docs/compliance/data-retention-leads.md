---
module: "02_neukundengewinnung"
domain: "compliance"
doc_type: "policy"
status: "draft"
owner: "team/compliance"
updated: "2025-09-28"
---

# Data Retention & Pseudonymisierung - Lead Management

**📍 Navigation:** Home → Compliance → Data Retention Leads

## 1. Rechtliche Grundlage

### DSGVO Art. 6 Abs. 1 lit. f
**Berechtigtes Interesse:** Lead-Management im B2B-Vertrieb zur Kundenakquise und Geschäftsanbahnung.

### Grundsätze
- **Datenminimierung**: Nur notwendige Daten je Stufe erfassen
- **Zweckbindung**: Ausschließlich für Lead-Management und Vertrieb
- **Transparenz**: Klare Information über Datenverarbeitung
- **Speicherbegrenzung**: Definierte Aufbewahrungsfristen

## 2. Lead-Stufen & Datenumfang

### Stage 0 - Vormerkung
**Datenumfang:**
- Firmenname (PFLICHT)
- Ort/Stadt (PFLICHT)
- Branche (OPTIONAL)
- **KEINE personenbezogenen Daten**

**Retention:** 60 Tage ohne Aktivität → Pseudonymisierung

### Stage 1 - Registrierung
**Zusätzliche Daten:**
- Kontaktperson (Name, Email, Telefon)
- Lead-Quelle
- Zuständiger Partner

**Retention:** 6 Monate Lead-Schutz, danach gemäß Aktivität

### Stage 2 - Qualifiziert
**Weitere Daten:**
- Vollständige Kontaktdaten
- Notizen und Interaktionen
- Dokumente und Angebote

**Retention:** Gemäß Geschäftsbeziehung oder gesetzlichen Fristen

## 3. Aufbewahrungsfristen

| Status | Frist | Aktion | Trigger |
|--------|-------|--------|---------|
| Vormerkung ohne Aktivität | 60 Tage | Pseudonymisierung | Nightly Job |
| Lead mit abgelaufenem Schutz | 6 Monate nach Ablauf | Prüfung & ggf. Löschung | Monthly Job |
| Lead ohne Progress (Warning) | 10 Tage nach Warnung | Status = expired | Timer |
| Expired Lead ohne Übernahme | 30 Tage | Pseudonymisierung | Weekly Job |
| Kunde geworden | 10 Jahre | Archivierung | - |
| Lost/Rejected | 1 Jahr | Löschung | Quarterly Job |

## 4. Pseudonymisierungs-Prozess

### Betroffene Felder
```sql
-- Personenbezogene Daten
contact_first_name → 'DELETED'
contact_last_name → 'DELETED'
contact_email → NULL
contact_phone → NULL
notes → 'Pseudonymisiert gem. DSGVO'
attachments → gelöscht

-- Metadata erhalten
company_name → erhalten
city → erhalten
industry → erhalten
created_at → erhalten
pseudonymized_at → NOW()
```

### Ausnahmen von Löschung/Pseudonymisierung
1. **Gesetzliche Aufbewahrungsfristen**
   - Handelsrechtliche Pflichten (HGB §257)
   - Steuerrechtliche Pflichten (AO §147)
   - Vertragserfüllung

2. **Berechtigte Interessen**
   - Laufende Geschäftsbeziehung
   - Rechtsstreitigkeiten
   - Compliance-Anforderungen

## 5. Technische Implementierung

### Jobs & Scheduler

#### Daily Job: Check Inactive Leads
```sql
-- Täglich um 02:00 Uhr
SELECT id FROM leads
WHERE stage = 0
  AND last_activity_at < NOW() - INTERVAL '60 days'
  AND pseudonymized_at IS NULL;
```

#### Weekly Job: Process Expired Protections
```sql
-- Wöchentlich Sonntags 03:00 Uhr
SELECT l.id FROM leads l
JOIN lead_protection lp ON l.id = lp.lead_id
WHERE lp.status = 'expired'
  AND lp.expired_at < NOW() - INTERVAL '30 days'
  AND l.pseudonymized_at IS NULL;
```

#### Monthly Job: Compliance Review
```sql
-- Monatlich am 1. um 04:00 Uhr
SELECT
  COUNT(*) FILTER (WHERE pseudonymized_at IS NOT NULL) as pseudonymized,
  COUNT(*) FILTER (WHERE deleted_at IS NOT NULL) as deleted,
  COUNT(*) FILTER (WHERE protection_status = 'expired') as expired
FROM leads
WHERE created_at > NOW() - INTERVAL '1 month';
```

### API Endpoints

#### Manual Pseudonymization
```http
DELETE /lead-protection/{leadId}/personal-data
Authorization: Bearer {token}
X-Reason: "Manual request - GDPR Article 17"
```

#### Retention Report
```http
GET /compliance/retention-report
Authorization: Bearer {token}

Response:
{
  "leads_total": 1234,
  "pseudonymized_last_30d": 45,
  "deleted_last_30d": 12,
  "upcoming_expirations": 23,
  "compliance_score": 98.5
}
```

## 6. Monitoring & Audit

### KPIs
- **Pseudonymisierungsrate**: Ziel >95% innerhalb Frist
- **Datenlecks**: 0 Toleranz
- **Compliance-Score**: Ziel >98%

### Audit-Events
```javascript
// Event-Typen
lead_personal_data_pseudonymized
lead_personal_data_deleted
lead_retention_warning
lead_retention_expired
compliance_report_generated
```

### Alerts
- Lead >55 Tage ohne Aktivität → Warning an Partner
- Pseudonymisierung fehlgeschlagen → Alert an Admin
- Compliance-Score <95% → Eskalation an Management

## 7. Verantwortlichkeiten

| Rolle | Verantwortung |
|-------|---------------|
| **Data Protection Officer** | Policy-Definition, Compliance-Überwachung |
| **System Admin** | Job-Konfiguration, technische Umsetzung |
| **Partner/Sales** | Rechtzeitige Lead-Bearbeitung |
| **Compliance Team** | Monitoring, Reporting, Audits |

## 8. Dokumentation & Training

### Pflichtschulungen
- DSGVO-Grundlagen (jährlich)
- Lead-Retention-Policy (bei Onboarding)
- System-Updates (bei Änderungen)

### Dokumentation
- Dieser Retention-Plan
- [CONTRACT_MAPPING.md](../planung/features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/CONTRACT_MAPPING.md)
- API-Dokumentation
- Audit-Trail

## 9. Einfluss von Backdating auf Fristen

### Backdating-Regelung
- **Berechtigung:** Nur Admin/Manager dürfen `registered_at` backdaten
- **Zweck:** Import von Altbeständen, dokumentierte Erstkontakte
- **Schutzdauer:** 6 Monate werden **ab `registered_at`** berechnet
- **Wichtig:** Backdating verschiebt Start **rückwirkend** – konform zum Vertrag (Registrierung/Erstkontakt)

### Auswirkungen auf Retention
- **Schutzfrist:** Beginnt mit `registered_at`, nicht `created_at`
- **60-Tage-Aktivität:** Bleibt **unverändert** an `last_activity_at` gebunden
- **Pseudonymisierung:** Erfolgt 60 Tage nach letzter Aktivität, unabhängig vom Backdating
- **Warnung:** Backdating ist **kein** Mittel zur Verlängerung der Datenhaltung jenseits der vorgesehenen Schutz-/Aufbewahrungslogik

### Audit-Trail
```sql
-- Jede Backdating-Operation wird protokolliert
INSERT INTO audit_events (
  event_type,
  lead_id,
  old_value,
  new_value,
  reason,
  performed_by,
  performed_at
) VALUES (
  'lead_registered_at_backdated',
  :leadId,
  :oldRegisteredAt,
  :newRegisteredAt,
  :reason,
  :userId,
  NOW()
);
```

### Compliance-Anforderungen
- **Dokumentationspflicht:** Grund für Backdating muss angegeben werden
- **Audit-Vollständigkeit:** Wer/wann/warum wurde backdated
- **Nachweisbarkeit:** Optional: Beleg-URL für Erstkontakt
- **Keine Manipulation:** Backdating nur für berechtigte Zwecke (Import, dokumentierte Kontakte)

## 10. Review & Updates

- **Quarterly Review**: KPIs und Compliance-Score
- **Annual Review**: Policy und Fristen
- **Ad-hoc**: Bei Gesetzesänderungen

---

**Letzte Prüfung:** 2025-09-28
**Nächste Review:** 2025-12-31
**Version:** 1.1.0