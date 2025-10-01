---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "adr"
status: "accepted"
sprint: "2.1.5"
owner: "team/leads-backend"
decision_date: "2025-10-01"
---

# ADR-004: Lead Protection Inline-First Architecture

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Shared → ADR-004

## Status

**ACCEPTED** - 2025-10-01

## Context

Sprint 2.1.5 implementiert Lead-Protection gemäß Handelsvertretervertrag (6M+60T+10T Regelwerk).

**Zwei Architektur-Optionen identifiziert:**

### Option A: Separate Tabelle `lead_protection`
- Neue Tabelle als Source of Truth
- Trigger-Synchronisation mit `leads`-Inline-Feldern
- Saubere vertragliche Trennung
- Komplexe Datenhaltung (doppelte Wahrheit)

### Option B: Inline-Felder in `leads` (Status Quo erweitern)
- Bestehende Protection-Felder bleiben Source of Truth
- Additive Migrations (ALTER TABLE only)
- Einfachere Implementierung
- Weniger Trigger-Komplexität

## Decision

**Wir wählen Option B: Inline-First Architecture**

### Implementierung (Sprint 2.1.5):

**V255: Fehlende Felder ergänzen**
```sql
ALTER TABLE leads
  ADD COLUMN progress_warning_sent_at TIMESTAMPTZ NULL,
  ADD COLUMN progress_deadline TIMESTAMPTZ NULL,
  ADD COLUMN stage SMALLINT NOT NULL DEFAULT 0;
```

**V256: Lead Activities erweitern**
```sql
ALTER TABLE lead_activities
  ADD COLUMN counts_as_progress BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN summary VARCHAR(500),
  ADD COLUMN outcome VARCHAR(50),
  ADD COLUMN next_action VARCHAR(200),
  ADD COLUMN next_action_date DATE,
  ADD COLUMN performed_by VARCHAR(50);
```

**V257: Helper Functions + Triggers**
```sql
CREATE FUNCTION calculate_protection_until(...);
CREATE FUNCTION calculate_progress_deadline(...);
CREATE TRIGGER update_progress_on_activity ...;
```

### Begründung

**Technische Gründe:**
1. Lead-Entity hat **bereits** alle Protection-Felder:
   - protectionStartAt, protectionMonths
   - protectionDays60, protectionDays10
   - clockStoppedAt, stopReason, stopApprovedBy
   - registeredAt, lastActivityAt, reminderSentAt, gracePeriodStartAt

2. LeadProtectionService **nutzt** diese Felder aktiv:
   - canTransitionStatus()
   - getRemainingProtectionDays()
   - getDaysUntilNextTransition()

3. **Keine Datenredundanz:** Vermeidung von "doppelter Wahrheit"

4. **Einfachere Trigger:** Kein Synchronisations-Overhead

**Business-Gründe:**
1. Sprint 2.1.5 Scope bereits groß (Progressive Profiling + Protection)
2. Vermeidung von Migration-Komplexität (300+ Zeilen V249)
3. Schnellere Time-to-Market für vertragliche Compliance
4. Bestehender LeadProtectionService kann weiter genutzt werden

**Wartbarkeit:**
1. Klare Source of Truth (keine Frage "welche Tabelle ist aktuell?")
2. Weniger Trigger = weniger Fehlerquellen
3. Einfacheres Debugging (ein Ort für Protection-Daten)

## Consequences

### Positive

✅ **Einfachere Implementierung:** Nur additive ALTER TABLE Migrations
✅ **Bestehender Code nutzbar:** LeadProtectionService bleibt funktional
✅ **Keine Datenredundanz:** Eine Source of Truth
✅ **Weniger Trigger-Komplexität:** Kein Synchronisations-Overhead
✅ **Schnellere Entwicklung:** Fokus auf Business-Logic statt DB-Architektur

### Negative

⚠️ **Vertragliche Trennung weniger explizit:** Protection-Daten nicht isoliert
⚠️ **Lead-Tabelle wird größer:** Mehr Spalten (aber akzeptabel)
⚠️ **Schwieriger zu "extrahieren":** Falls separate Tabelle später gewünscht

### Neutral

📌 **Migration später möglich:** Separate Tabelle kann in 2.1.6+ eingeführt werden (mit ADR)
📌 **Contract-Mapping bleibt präzise:** Dokumentation zeigt Vertragsbezug
📌 **V249-Artefakt nicht verloren:** Als Materiallager für 2.1.6 verfügbar

## Risks & Mitigation

### Risiko 1: Lead-Tabelle wird zu groß
**Mitigation:**
- Monitoring: Regelmäßige Tabellengrößen-Checks
- Falls nötig: Partitionierung nach created_at
- Oder: Migration zu separater Tabelle in 2.1.6+

### Risiko 2: Contract-Mapping wird unklar
**Mitigation:**
- Explizite Dokumentation in CONTRACT_MAPPING.md
- Code-Kommentare verweisen auf Vertragsklauseln
- Spalten-Kommentare in DB mit Contract-Referenz

### Risiko 3: Spätere Extraktion schwierig
**Mitigation:**
- LeadProtectionService als Abstraction Layer beibehalten
- Falls separate Tabelle gewünscht: ADR + schrittweise Migration
- V249-Artefakt als Blaupause verfügbar

## Alternatives Considered

### Alternative 1: Separate Tabelle (V249-Artefakt)
**Pro:**
- Saubere vertragliche Trennung
- Explizites Protection-Modell
- Einfacher zu "auditen"

**Con:**
- Datenredundanz (Synchronisation mit leads)
- Komplexe Trigger (create_lead_protection_on_insert, etc.)
- Mehr Code-Änderungen (LeadProtectionService Rewrite)
- Größerer Scope für Sprint 2.1.5

**Entscheidung:** Abgelehnt für Sprint 2.1.5 (kann später evaluiert werden)

### Alternative 2: Hybrid (Neue Felder in Tabelle, Alte inline)
**Pro:**
- Schrittweise Migration
- Kompatibilität mit bestehendem Code

**Con:**
- "Worst of both worlds" (Komplexität + Redundanz)
- Unklar: Welche Felder wo?
- Schwierig zu dokumentieren

**Entscheidung:** Abgelehnt (zu komplex)

## Implementation Notes

### Database Schema Changes

**V255: leads table augmentation**
```sql
-- Progress tracking fields
ALTER TABLE leads ADD COLUMN progress_warning_sent_at TIMESTAMPTZ NULL;
ALTER TABLE leads ADD COLUMN progress_deadline TIMESTAMPTZ NULL;

-- Progressive profiling stage
ALTER TABLE leads ADD COLUMN stage SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE leads ADD CONSTRAINT leads_stage_chk CHECK (stage BETWEEN 0 AND 2);

COMMENT ON COLUMN leads.progress_warning_sent_at IS
  'Vertrag §3.3: Zeitpunkt der 60-Tage-Warnung';
COMMENT ON COLUMN leads.progress_deadline IS
  'Vertrag §3.3: Deadline für Fortschritt (last_activity_at + 60 days)';
COMMENT ON COLUMN leads.stage IS
  'DSGVO Art.5: Progressive Profiling Stage (0=Vormerkung, 1=Registrierung, 2=Qualifiziert)';
```

### Service Layer

**LeadProtectionService erweitern:**
- Neue Methoden für progress_deadline Berechnung
- Stage-Validierung und Transitions
- Integration mit bestehenden Protection-Methoden

**Keine Breaking Changes:**
- Bestehende Methoden bleiben funktional
- Additive Erweiterung nur

### Testing Strategy

**Unit Tests:**
- Stage-Validierung (0→1→2, keine Sprünge)
- Progress-Deadline-Berechnung
- Protection-Transitions mit neuen Feldern

**Integration Tests:**
- Trigger update_progress_on_activity
- Migration V255-V257 erfolgreich
- Lead Create mit stage=0 default

## Future Considerations

### Wenn separate Tabelle später gewünscht (2.1.6+):

1. **ADR erstellen:** ADR-004-lead-protection-table-extraction
2. **Migration planen:** Schrittweise Daten-Migration
3. **Service refactoren:** LeadProtectionService → neue Tabelle
4. **Backward Compatibility:** Inline-Felder als View/Trigger beibehalten
5. **Dokumentation:** Contract-Mapping aktualisieren

### Trigger für automatische Werte:

**Bereits in V257:**
- update_progress_on_activity: Bei lead_activities.counts_as_progress=true

**Zukünftig möglich:**
- Auto-set progress_warning_sent_at bei deadline < 7 Tage
- Auto-expire bei protection_until überschritten
- (Aber: Besser als Scheduled Jobs statt Trigger)

## References

- Handelsvertretervertrag (§3.2 Lead-Schutz, §3.3 Aktivitätsstandard, §3.3.2 Stop-the-Clock)
- DSGVO Art. 5 (Datenminimierung)
- Sprint 2.1.5 DELTA_LOG: [DELTA_LOG_2_1_5.md](../../artefakte/SPRINT_2_1_5/DELTA_LOG_2_1_5.md)
- V249-Artefakt (Materiallager): [V249__lead_protection_tables.sql.sprint215](../../artefakte/SPRINT_2_1_5/migrations/V249__lead_protection_tables.sql.sprint215)
- Lead Entity: [backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java](../../../../../backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java)
- LeadProtectionService: [backend/src/main/java/de/freshplan/modules/leads/service/LeadProtectionService.java](../../../../../backend/src/main/java/de/freshplan/modules/leads/service/LeadProtectionService.java)

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2025-10-01 | ACCEPTED | Inline-First für Sprint 2.1.5, separate Tabelle auf 2.1.6+ verschoben |
| 2025-10-01 | V255-V257 definiert | Additive Migrations, kein DROP/CREATE |
| 2025-10-01 | counts_as_progress DEFAULT FALSE | Konservativ, explizite Progress-Markierung |

## Approval

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Tech Lead | Claude Code | 2025-10-01 | ✅ |
| Product Owner | Jörg Streeck | 2025-10-01 | ⏸️ Pending |

---

**Next ADR:** ADR-005 (Lead Protection Table Extraction - wenn benötigt in 2.1.6+)
