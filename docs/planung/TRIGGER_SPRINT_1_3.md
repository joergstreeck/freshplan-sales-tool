# 🚀 VERBINDLICH: SPRINT 1.3 SECURITY GATES + CI HARDENING - SYSTEMATISCHE UMSETZUNG [IN PROGRESS]

## ⚠️ WICHTIGE QUALITÄTSREGELN - VOLLSTÄNDIG BEFOLGEN!

**HINWEIS:** Diese Anweisungen sind verbindlich für professionelle Umsetzung. Übersprungene Schritte führen zu:
- ❌ Fehlende Security-Gates → Phase 2 kann nicht sicher starten
- ❌ CI-Pipeline-Probleme → PR-Qualität nicht gewährleistet
- ❌ Foundation-Performance-Probleme → <200ms P95 nicht erreichbar
- ❌ Integration-Failures → Security-Validierung unvollständig

**QUALITÄTS-REGEL:** Du sollst jeden Abschnitt mit "✅ VERSTANDEN" bestätigen für optimale Ergebnisse.

**HINWEIS:** Bei übersprungenen Checkpoints kann die Qualität nicht garantiert werden.

## 📋 VERBINDLICH: DOKUMENTE-VALIDIERUNG (REIHENFOLGE EMPFOHLEN)

**1. ROADMAP-ORIENTIERUNG:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md`
- Section "🎯 CLAUDE QUICK-START"
- 🚨 SECURITY-GATE CHECKPOINT PHASE 1 → PHASE 2

**2. ARBEITSREGELN:**
Lies: `./docs/CLAUDE.md`
- Die 17 kritischen Regeln beachten
- Two-Pass Review: Spotless + Strategic Quality

**3. BUSINESS-KONTEXT:**
Lies: `./docs/planung/CRM_AI_CONTEXT_SCHNELL.md`
- Lead-Protection State-Machine verstehen
- 6M+60T+10T Protection-Rules

**4. SPRINT-DETAIL:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md` - Section "Sprint 1.3: Security Gates + CI"

**5. TECHNICAL CONCEPT:**
Lies: `./docs/planung/features-neu/00_infrastruktur/betrieb/technical-concept.md`
- User-Lead-Protection Operations
- CI Pipeline Hardening für Security-Gates

**6. QUALITY-GATES:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md` - Section "🔧 VERBINDLICHE QUALITY GATES"
- Required PR-Checks als GitHub Actions
- PR-Template 6-Block Enforcement

## 🔒 HINWEIS: MIGRATION-CHECK (OPTIONAL FÜR CI-SETUP)

```bash
# Nur bei DB-Änderungen nötig:
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
```

## 🎯 IMPLEMENTIERUNGS-AUFTRAG

**SPRINT:** Sprint 1.3: Security Gates + CI Hardening
**PR-BRANCH-1:** feature/sprint-1-3-security-gates-enforcement-FP-231 [✅ MERGED as PR #97]
**PR-BRANCH-2:** feature/sprint-1-3-foundation-integration-testing-FP-232
**PR-BRANCH-3:** feature/sprint-1-3-frontend-settings-integration-FP-233
**MODULE:** CI/CD Pipeline + Foundation Integration
**GESCHÄTZTE ARBEITSZEIT:** 6-8 Stunden (3 PRs)

**SECURITY GATES bedeutet:**
- 5 Security-Contract-Tests als Required GitHub Checks
- Fail-closed Verification (PR blockiert bei Security-Violations)
- PR-Template Enforcement mit 6 Pflichtblöcken

**CI HARDENING bedeutet:**
- PR-Pipeline: Smoke-Tests nur (schnell)
- Nightly-Pipeline: Full-Tests (k6, ZAP, Lighthouse)
- Security-Contracts als Blocker für alle PRs

**SUCCESS-CRITERIA:**
- [ ] 5 Security-Contract-Tests als Required PR-Checks
- [ ] PR-Template mit 6-Block-Enforcement
- [ ] CI Pipeline Split: PR-Smoke vs Nightly-Full
- [ ] Foundation-Performance <200ms P95 bestätigt (Foundation-Baseline)
- [ ] SECURITY-GATE für Phase 2 operational

## 🚀 IMPLEMENTIERUNGS-SCHRITTE

**SCHRITT 1: SECURITY-GATES ENFORCEMENT (PR #1)**

**1.1 BRANCH ERSTELLEN:**
```bash
git checkout main && git pull
git checkout -b feature/sprint-1-3-security-gates-enforcement-FP-231
```

**1.2 GITHUB REQUIRED CHECKS:**
```yaml
# .github/workflows/security-gates.yml
name: Security Gates
on: [pull_request]
jobs:
  security-contracts:
    runs-on: ubuntu-latest
    steps:
      - name: Security Contract Tests
        run: ./mvnw test -Dtest=*SecurityContractTest*

      - name: Fail-closed Verification
        run: ./scripts/verify-fail-closed-security.sh
```

**1.3 5 SECURITY-CONTRACT-TESTS:**
1. **Owner-Access-Test:** Lead-Creator kann lesen/bearbeiten
2. **Kollaborator-Access-Test:** Team-Member kann lesen, nicht bearbeiten
3. **Manager-Override-Test:** Manager kann mit Audit-Trail
4. **Territory-Isolation-Test:** DE User sieht keine CH Leads
5. **Fail-Closed-Test:** Policy-Fehler blockiert Access

**1.4 PR-TEMPLATE ENFORCEMENT:**
```yaml
# .github/pull_request_template.md
## 🎯 Ziel
[PFLICHT: Was wird implementiert?]

## ⚠️ Risiko
[PFLICHT: Welche Risiken? Mitigation?]

## 🔄 Migrations-Schritte + Rollback
[PFLICHT: SQL-Änderungen? Rollback-Plan?]

## ⚡ Performance-Nachweis
[PFLICHT: k6-Results? Bundle-Size?]

## 🔒 Security-Checks
[PFLICHT: ABAC/RLS? ZAP-Results?]

## 📚 SoT-Referenzen
[PFLICHT: Technical-Concepts? Artefakte?]
```

**SCHRITT 2: FOUNDATION INTEGRATION TESTING (PR #2)**

**2.1 BRANCH ERSTELLEN:**
```bash
git checkout main && git pull
git checkout -b feature/sprint-1-3-foundation-integration-testing-FP-232
```

**2.2 CI PIPELINE SPLIT:**
```yaml
# PR-Pipeline (schnell, <10min):
- Spotless formatting check
- Unit tests critical modules
- k6-smoke <200ms P95
- Security-contracts
- Bundle-size regression

# Nightly-Pipeline (vollständig, ~30min):
- Full test suite
- k6 full load testing
- ZAP security scan full
- Lighthouse performance full
- Cross-module integration tests
```

**2.3 FOUNDATION-PERFORMANCE VALIDATION:**
```bash
# Performance-Benchmarks dokumentieren:
./scripts/benchmark-cqrs-foundation.sh
./scripts/benchmark-security-performance.sh
./scripts/benchmark-settings-performance.sh

# Targets bestätigen:
# CQRS Events: <200ms P95
# Security Policies: <100ms P95
# Settings ETag: <50ms (≥70% Hit-Rate)
```

**SCHRITT 3: SECURITY-GATE VALIDATION:**

**3.1 VERBINDLICHE FREIGABE-KRITERIEN TESTEN:**
- [ ] ✅ CQRS Light operational mit <200ms P95
- [ ] ✅ ABAC/RLS Security-Contracts grün
- [ ] ✅ Settings Registry mit ETag-Performance
- [ ] ✅ 5 Security-Contract-Tests als Required Checks
- [ ] ✅ Rollback-Procedures <5min getestet

**3.2 PHASE 2 FREIGABE:**
```bash
# Validation Script:
./scripts/validate-phase-1-complete.sh

# Erwartetes Ergebnis:
# ✅ Foundation Performance <200ms P95
# ✅ Security Gates operational
# ✅ All Required Checks active
# ✅ Phase 2 can start: Module 02 Neukundengewinnung
```

## ⚠️ KRITISCHE REGELN

**SECURITY-GATE ENFORCEMENT:**
- KEINE PR ohne Security-Contract-Tests grün
- Phase 2 startet NUR bei kompletter Foundation
- Security-Violations blockieren ALLE nachfolgenden Entwicklungen

**CI-PERFORMANCE:**
- PR-Pipeline <10min (Developer-Velocity)
- Nightly-Pipeline für vollständige Validation
- Security-Checks sind wichtig für Qualität

## ✅ ERFOLGSMESSUNG

**PHASE 1 IST COMPLETE WENN:**
- [ ] Alle 5 Security-Contract-Tests grün als Required Checks
- [ ] Foundation-Performance <200ms P95 dokumentiert
- [ ] PR-Template enforcement aktiv
- [ ] CI Pipeline split operational
- [ ] Security-Gate für Module 05 Kommunikation bereit

**ROADMAP-UPDATE:**
Nach Sprint 1.3 Complete:
- Progress: 3/35 → 5/35 (2 PRs)
- Status: ✅ PHASE 1 FOUNDATION COMPLETE (YYYY-MM-DD)
- Next Action: 🚀 PHASE 2 START - Sprint 2.1 Neukundengewinnung
- Blockers released: Module 02+03 können starten

**🚨 CRITICAL MILESTONE:**
FOUNDATION-FIRST STRATEGY erfolgreich - Business-Module können auf optimaler Performance-Foundation starten!

**🔓 FREIGABE FÜR PHASE 2:**
- Module 02 Neukundengewinnung: Ready (Security ✅)
- Module 03 Kundenmanagement: Ready (Territory ✅)
- Module 05 Kommunikation: Wartet auf Security-Gate Validation

## 🆕 SCHRITT 3: FRONTEND SETTINGS INTEGRATION (PR #3)

**3.1 BRANCH ERSTELLEN:**
```bash
git checkout main && git pull
git checkout -b feature/sprint-1-3-frontend-settings-integration-FP-233
```

**3.2 ZIEL:**
Settings Registry im Frontend nutzen (React Query + ETag/304), um Theme & Feature-Flags aus security_settings zu beziehen.
HTTP-Last senken (304), sauberes Caching etablieren, End-to-End zeigen.

**3.3 SCOPE:**
Kein Over-Engineering: nur zwei Keys als Referenz-Implementierung
- `ui.theme` (GLOBAL → { primary: "#94C456", secondary: "#004F7B" })
- `system.feature_flags` (GLOBAL → { "experimental": false })

**3.4 IMPLEMENTIERUNG:**

**API-Client: Conditional Requests**
```typescript
// frontend/src/lib/settings/api.ts
const etagStore = new Map<string, string>();

export async function fetchSetting(key: string) {
  const etag = etagStore.get(key);
  const headers: HeadersInit = {};

  if (etag) {
    headers['If-None-Match'] = etag;
  }

  const response = await fetch(
    `/api/settings?scope=GLOBAL&key=${key}`,
    { headers }
  );

  if (response.status === 304) {
    // Cache hit - return cached data
    return getCachedData(key);
  }

  const newEtag = response.headers.get('ETag');
  if (newEtag) {
    etagStore.set(key, newEtag);
  }

  return response.json();
}
```

**React Query Hooks:**
```typescript
// frontend/src/lib/settings/hooks.ts
export function useSetting(key: string) {
  return useQuery({
    queryKey: ['setting', key],
    queryFn: () => fetchSetting(key),
    staleTime: 60_000,  // 1 minute
    gcTime: 600_000,    // 10 minutes
    retry: 1,
  });
}

export function useFeatureFlag(flag: string) {
  const { data } = useSetting('system.feature_flags');
  return data?.value?.[flag] ?? false;
}
```

**Theme-Wiring:**
```typescript
// frontend/src/theme/ThemeProvider.tsx
const DEFAULT_THEME = {
  primary: '#94C456',
  secondary: '#004F7B',
};

export function ThemeProvider({ children }) {
  const { data: themeSetting } = useSetting('ui.theme');

  const theme = {
    ...DEFAULT_THEME,
    ...(themeSetting?.value || {}),
  };

  return (
    <ThemeContext.Provider value={theme}>
      {children}
    </ThemeContext.Provider>
  );
}
```

**3.5 TESTS:**
- Dev: zweimaliger GET liefert beim zweiten Mal 304 (DevTools/Network)
- Theme-Switch per API → ETag ändert sich → neuer Wert nach Reload
- Feature Flag Toggle funktioniert
- Fallback bei API-Fehler auf Defaults

**3.6 AKZEPTANZKRITERIEN:**
- [ ] ui.theme & system.feature_flags werden über API geladen (kein Mock/Hardcoding)
- [ ] ETag wird gesendet (If-None-Match) und 304 korrekt behandelt
- [ ] Fallback bei API-Fehlern/404 auf Defaults
- [ ] CI grün, ESLint „no mocks in business paths" bleibt grün
- [ ] Kurze Doku: SETTINGS_REGISTRY.md → Abschnitt „Frontend-Integration / ETag"

**PR-TITEL:**
```
feat(settings): Sprint 1.3 PR #3 – Frontend Settings Integration with ETag/304 (FP-233)
```

Arbeite systematisch PR #1 [✅ DONE] dann PR #2 dann PR #3 für Foundation-Completion!