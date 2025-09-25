# 🔒 TRIGGER: Sprint 1.6 - RLS Adoption in Modulen

**Sprint:** 1.6 (Foundation - RLS Module Migration)
**Dauer:** 3-4h
**Priorität:** P0 - KRITISCH (Modul 02 blockiert Sprint 2.1)
**Status:** 🚀 READY TO START

## 🎯 Sprint-Ziel

**RLS-ADOPTION:** Systematische Migration aller Module-Services zu @RlsContext Pattern nach Sprint 1.5 Security Retrofit. Fokus auf Modul 02 (Neukundengewinnung) als P0, da dies Sprint 2.1 blockiert.

## ⚠️ Problem

Nach Sprint 1.5 (PR #106) ist das @RlsContext Pattern implementiert, aber:
- 33+ Services in Modulen haben KEIN @RlsContext
- Modul 02 (aktueller Sprint 2.1) hat 5 ungeschützte Services
- Risiko von Territory-Leaks und Connection-Affinity-Problemen
- Keine CI-Prüfung für neue Services ohne @RlsContext

## ✅ Lösung: Ein sauberer Commit-Stack

### Commit 1: Docs/Hub (planungsmethodik-konform)
- ADR-0007 in ADR-Index verlinken
- RLS-Badge in alle Module 01-08 technical-concepts
- SPRINT_1_5_GAP_ANALYSIS.md finalisieren
- Keine Copy-Paste, nur Badge + Deep-Links

### Commit 2: CI Guard (FP-arm)
- `tools/rls-guard.sh` - Heuristik für @Transactional + DB ohne @RlsContext
- `.github/workflows/ci-rls-guard.yml` - GitHub Action
- `docs/checklists/RLS_COMPLIANCE_CHECKLIST.md` - 4-Punkte-Checkliste

### Commit 3: Modul 02 Fix (P0 - KRITISCH)
Services mit @RlsContext annotieren:
- `LeadService` - 4 Methoden
- `LeadProtectionService` - alle DB-Methoden
- `UserLeadSettingsService` - 3 Methoden
- `TerritoryService` - 1 Methode
- `EmailActivityDetectionService` - 1 Methode

### Commit 4: Tests & Querverweise
- Tests auf neue GUC-Keys umstellen
- TRIGGER_INDEX.md aktualisieren
- ROADMAP Update

### Commit 5: PR-Politur
- CHANGELOG.md
- SECURITY_UPDATE_SPRINT_1_5.md Tabelle

## 📋 Implementierungs-Details

### RLS-Badge für Module (einheitlich):
```markdown
> **RLS-Status (Sprint 1.6):** ✅ @RlsContext CDI-Interceptor verpflichtend
> 🔎 Details: [ADR-0007](../../../adr/ADR-0007-rls-connection-affinity.md) · [Security Update](../../../SECURITY_UPDATE_SPRINT_1_5.md)
```

### Service-Pattern:
```java
@ApplicationScoped
@Transactional  // Klassen-Level
public class LeadService {

    @RlsContext  // NEU: Bei jeder DB-Methode
    public int processReminders() {
        // Existing code
    }
}
```

### CI-Guard Heuristik:
```bash
# Findet: @Transactional + EntityManager/Query OHNE @RlsContext
# FP-arm: Nur wenn beide Bedingungen erfüllt
```

## 📊 Scope & Priorisierung

### Diese PR (Sprint 1.6):
| Modul | Services | Priorität | Status |
|-------|----------|-----------|---------|
| 02 Neukundengewinnung | 5 | P0 - KRITISCH | 🔧 Fix in PR |
| CI/Docs | - | P0 | 📝 Erstellen |

### Follow-up PRs:
| Ticket | Modul | Services | Priorität |
|--------|-------|----------|-----------|
| FP-273 | 03 Kundenmanagement | ~10 | P1 - Sprint 2.2 |
| FP-274 | Domain Services | ~15 | P2 - Backlog |
| FP-275 | Import/Batch | TBD | P1 - Async Paths |
| FP-276 | Monitoring | - | P2 - Metrics |

## 🧪 Test-Plan

### Unit/Integration:
- Alle Modul 02 Tests grün
- Neue GUC-Keys verwendet
- @TestSecurity + @Transactional

### Smoke Tests:
1. Ohne Context → 0 rows (fail-closed)
2. Mit user_context → nur eigene Daten
3. Mit ADMIN/SYSTEM → volle Sicht

### CI-Guard Test:
- Positive: Service ohne @RlsContext → Fehler
- Negative: Service mit @RlsContext → OK

## ✅ Definition of Done

- [ ] RLS-Badge in allen 8 Modulen (nur Links, keine Duplikate)
- [ ] CI-Guard aktiv und getestet
- [ ] Modul 02: Alle 5 Services mit @RlsContext
- [ ] Tests grün mit neuen GUC-Keys
- [ ] ADR-0007 im Index verlinkt
- [ ] GAP-Analysis finalisiert
- [ ] ROADMAP/TRIGGER_INDEX aktualisiert
- [ ] PR Review positiv
- [ ] CI Pipeline grün

## 🚀 Deployment

```bash
# Kein DB-Break, nur Code-Annotationen
# Automatisch via CI/CD nach Merge
```

## 📚 Dokumentation

- [ADR-0007: RLS Connection Affinity Pattern](../adr/ADR-0007-rls-connection-affinity.md)
- [SECURITY_UPDATE_SPRINT_1_5.md](./SECURITY_UPDATE_SPRINT_1_5.md)
- [SPRINT_1_5_GAP_ANALYSIS.md](./SPRINT_1_5_GAP_ANALYSIS.md)
- [RLS_COMPLIANCE_CHECKLIST.md](../checklists/RLS_COMPLIANCE_CHECKLIST.md)

## 🎯 Erwartetes Ergebnis

- **Modul 02** vollständig RLS-compliant → Sprint 2.1 kann fortfahren
- **CI-Guard** verhindert neue Regression
- **Klare Roadmap** für restliche Module-Migration
- **Zero-Redundancy Docs** mit Badge + Links Pattern

---

**Sprint-Start:** Nach Review dieses Triggers
**Geschätzte Dauer:** 3-4h
**PR-Titel:** `🔒 feat(security): Sprint 1.6 - RLS Adoption für Module mit CI-Guard`