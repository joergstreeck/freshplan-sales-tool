---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "deltalog"
status: "approved"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# Sprint 2.1.5 – Delta Log (Scope-Änderung)

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Delta Log

## Scope-Änderung

### Original Scope (vor 2025-09-28)
**Titel:** Fuzzy Matching & Review UI
**Fokus:** Duplikat-Erkennung und Merge-Funktionalität

**Geplante Features:**
- Fuzzy-Matching Algorithmus
- Kandidaten-Review UI
- Merge/Unmerge Operationen
- Match-Score Visualisierung
- Konflikt-Resolution bei Merge

### Neuer Scope (ab 2025-09-28)
**Titel:** Lead Protection & Progressive Profiling (B2B)
**Fokus:** Vertragliche Schutz-Mechanismen und DSGVO-konforme Erfassung

**Neue Features:**
- 6-Monats Lead-Schutz (Vertrag §3.2)
- 60-Tage Aktivitätsstandard (Vertrag §3.3)
- Stop-the-Clock Mechanismus (Vertrag §3.3.2)
- Progressive Profiling (3 Stufen)
- Fuzzy-Matching nur für Soft-Duplicates (reduziert)

## Begründung

### Vertragliche Dringlichkeit
Der Handelsvertretervertrag definiert **verbindliche Lead-Schutz-Regeln**, die technisch umgesetzt werden müssen:
- 6 Monate Schutz ab Registrierung
- 60 Tage Aktivitätspflicht
- Stop-the-Clock bei FreshFoodz-Verzögerungen

Diese Regeln sind **vertraglich verpflichtend** und haben Priorität vor Nice-to-Have Features.

### DSGVO-Anforderungen
Progressive Profiling ermöglicht **Datenminimierung**:
- Stage 0: Keine personenbezogenen Daten (nur Firma/Ort)
- Stage 1: Optional Kontaktdaten
- Stage 2: Vollständige Qualifizierung

Dies entspricht den DSGVO-Prinzipien und reduziert rechtliche Risiken.

### Business Impact
Lead-Schutz ist **geschäftskritisch**:
- Verhindert Partner-Konflikte
- Sichert Provisionsansprüche
- Schafft faire Wettbewerbsbedingungen
- Erhöht Partner-Zufriedenheit

## Verschobene Features

### Nach Sprint 2.1.6
**Matching & Review (erweitert)**
- Vollständiger Fuzzy-Match Algorithmus
- Merge/Unmerge mit Identitätsgraph
- Konflikt-Resolution UI
- Match-Historie und Audit

**Begründung:** Diese Features sind wichtig, aber nicht vertraglich verpflichtend. Sie werden in Sprint 2.1.6 zusammen mit Lead-Transfer implementiert, da beide Features ähnliche UI-Komponenten nutzen.

## Impact-Analyse

### Positive Impacts
- ✅ Vertragliche Compliance erreicht
- ✅ DSGVO-konforme Datenerfassung
- ✅ Reduzierte Partner-Konflikte
- ✅ Klarere Business-Logik

### Negative Impacts
- ⚠️ Merge-Funktionalität verzögert (1 Sprint)
- ⚠️ Vollständiges Fuzzy-Matching verzögert
- ⚠️ Zusätzliche Migration erforderlich (V249/V250)

### Mitigation
- Basis Fuzzy-Matching in 2.1.5 für Soft-Duplicates
- Merge UI-Komponenten können wiederverwendet werden
- Documentation bereits vorbereitet für 2.1.6

## Stakeholder-Kommunikation

### Informierte Stakeholder
- Product Owner: ✅ Approved (2025-09-28)
- Tech Lead: ✅ Approved
- Partner Management: ✅ Informed
- Legal/Compliance: ✅ Confirmed contract alignment

### Kommunikationskanäle
- Slack #team-leads-backend: Announcement gepostet
- JIRA MOD02-215: Scope Update dokumentiert
- Sprint Planning Meeting: Präsentiert am 2025-09-28

## Retention Policy

### Stubs für verschobene Features
Gemäß 2-Sprint-Regel werden Stubs angelegt für:
- `artefakte/SPRINT_2_1_6/matching-review-stub.md`
- `artefakte/SPRINT_2_1_6/merge-unmerge-stub.md`

Diese verweisen auf die neue Implementierung in Sprint 2.1.6.

## Lessons Learned

### Was gut lief
- Frühzeitige Erkennung der vertraglichen Priorität
- Klare Scope-Definition für beide Sprints
- Stakeholder-Alignment erreicht

### Verbesserungspotential
- Vertragliche Anforderungen früher in Sprint-Planung einbeziehen
- Legal Review vor Sprint-Start durchführen
- Buffer für regulatorische Anforderungen einplanen

## Approval

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Product Owner | | 2025-09-28 | ✅ |
| Tech Lead | | 2025-09-28 | ✅ |
| Scrum Master | | 2025-09-28 | ✅ |

## References

- Handelsvertretervertrag (§3.2, §3.3, §3.3.2)
- DSGVO Art. 5 (Datenminimierung)
- Original Sprint Planning: TRIGGER_SPRINT_2_1_5.md (old version)
- Updated Sprint Planning: TRIGGER_SPRINT_2_1_5.md (current)
- Sprint 2.1.6 Planning: TRIGGER_SPRINT_2_1_6.md (upcoming)