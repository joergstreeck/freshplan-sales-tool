# ADR-0006: Mock-Governance (Business-Logic mock-frei)

**Status:** Accepted
**Datum:** 2025-09-23
**Entscheider:** Development Team
**Kontext:** Sprint 1.1 - Governance Foundation

## Kontext

Frontend enthielt Mock-Dateien (z.B. `mockData.ts`) in produktionsnahen Pfaden. Backend nutzt TestDataBuilder + Dev-Seeds. Das führte zu:

- **Mock-Reality-Gap:** Dashboard zeigt Mock-KPIs statt echter Backend-Daten
- **Fragmentierte Development-Experience:** Parallele Mock-Systeme (Frontend vs. Backend)
- **Integration-Issues versteckt:** Unterschiede zwischen Mock-Responses und echten APIs
- **Keine Enforcement:** Mock-Pollution rutscht durch PRs ohne Kontrolle

## Entscheidung

**Mock-Verbot in Business-Logic-Pfaden mit klaren Ausnahmen:**

1. **Verbotene Pfade:** `src/app`, `src/features`, `src/lib`, `src/hooks`, `src/store`
2. **Erlaubte Pfade:** `__tests__`, `*.test.*`, `*.spec.*`, `*.stories.*`, `.storybook/`, `fixtures/`, `__mocks__`
3. **Enforcement:** ESLint-Regel + CI-Guard brechen PRs bei Verstößen
4. **Dev-Daten:** Quarkus `dev-migration` (Flyway) + optionaler Seed-Endpoint (nur dev-Profil)
5. **API-Layer:** React Query + optionale Zod-Validierung + konsistentes Error-Handling

## Begründung

**Warum nicht globales Mock-Verbot:**
- Tests brauchen Mocks für Isolation
- Storybook braucht Mock-Daten für Demo-Komponenten
- Fixtures sind legitim für E2E-Tests

**Warum Enforcement notwendig:**
- Conventions allein sind zu fragil
- Entwickler weichen auf Mocks aus wenn lokale Daten fehlen
- Review-Prozess übersieht Mock-Imports

**Warum Dev-Seeds:**
- Produktionsnahe Development-Experience
- Keine Mock-API-Mismatch-Issues
- TestDataBuilder-System bereits vorhanden

## Konsequenzen

**Positive:**
- ✅ Keine Mock-Reality-Gap mehr
- ✅ Frontend-Backend-Integration-Issues früh erkannt
- ✅ Einheitliche Development-Experience
- ✅ Automated Enforcement verhindert Regression

**Negative:**
- ⚠️ Initial Setup-Aufwand für ESLint/CI-Konfiguration
- ⚠️ Backend muss Dev-APIs früher bereitstellen
- ⚠️ False Positives möglich bei Edge Cases

**Mitigationen:**
- Pre-commit-Hook warnt früh bei Verstößen
- Dev-Seeds überbrücken fehlende Backend-APIs
- Ausnahme-Globs für legitime Mock-Nutzung

## Alternativen

**A) Komplettes Mock-Verbot global**
- Verworfen: Blockiert Tests, Storybook, Demos

**B) Nur Conventions, keine Gates**
- Verworfen: Zu fragil, Regression-anfällig

**C) Mock-Warnung statt Error**
- Verworfen: Warnings werden ignoriert

## Implementation

- **Sprint 1.1:** ESLint/CI/Dev-Seeds setup
- **Sprint 1.2:** Erste Frontend Mock-Elimination (Cockpit)
- **Weitere Sprints:** Schrittweise Mock-Replacement nach CQRS-Foundation

## Monitoring

- CI-Guard Dashboard zeigt Mock-Violations
- ESLint-Report in PR-Checks
- Quarterly Mock-Debt Review

---

**Related:**
- [Mock Infrastructure Analysis](../features-neu/00_infrastruktur/betrieb/analyse/02_MOCK_INFRASTRUCTURE_ANALYSIS.md)
- [Mock Governance Implementation](../features-neu/00_infrastruktur/standards/03_MOCK_GOVERNANCE.md)