# 🚀 VERBINDLICH: SPRINT 1.2 SECURITY + FOUNDATION - SYSTEMATISCHE UMSETZUNG

## ⚠️ WICHTIGE QUALITÄTSREGELN - VOLLSTÄNDIG BEFOLGEN!

**HINWEIS:** Diese Anweisungen sind verbindlich für professionelle Umsetzung. Übersprungene Schritte führen zu:
- ❌ Inkonsistente Security-Implementierung → Wochen Nacharbeit
- ❌ ABAC/RLS-Probleme → Territory-Isolation fehlerhaft
- ❌ Settings-Registry-Probleme → Module 01+04+06+07+08 betroffen
- ❌ Integration-Failures → Cross-Module-Security kompromittiert

**QUALITÄTS-REGEL:** Du sollst jeden Abschnitt mit "✅ VERSTANDEN" bestätigen für optimale Ergebnisse.

**HINWEIS:** Bei übersprungenen Checkpoints kann die Qualität nicht garantiert werden.

## 📋 VERBINDLICH: DOKUMENTE-VALIDIERUNG (REIHENFOLGE EMPFOHLEN)

**1. ROADMAP-ORIENTIERUNG:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md`
- Section "🎯 CLAUDE QUICK-START" komplett lesen
- Current Status + Next Action verstehen

**2. ARBEITSREGELN:**
Lies: `./docs/CLAUDE.md`
- Die 17 kritischen Regeln beachten
- Sprache: IMMER Deutsch
- Tests: Bei JEDER Implementierung ≥80%

**3. BUSINESS-KONTEXT:**
Lies: `./docs/planung/CRM_AI_CONTEXT_SCHNELL.md`
- Territory-Management Deutschland/Schweiz verstehen
- User-Lead-Protection (userbasiertes Ownership)
- Multi-Contact-B2B-Architecture (CHEF/BUYER)

**4. SPRINT-DETAIL:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md` - Section "Sprint 1.2: Security + Foundation"

**5. TECHNICAL CONCEPT:**
Lies: `./docs/planung/features-neu/00_infrastruktur/sicherheit/technical-concept.md`
- ABAC (Attribute-based Access Control) verstehen
- RLS (Row-Level Security) Territory-Scoping
- Security für CQRS Light Architecture

**6. VERFÜGBARE ARTEFAKTE:**
Analysiere: `./docs/planung/features-neu/00_infrastruktur/sicherheit/artefakte/`
- ABAC Policy-Engine Artefakte
- RLS Territory-Scoping Code

**7. SETTINGS REGISTRY:**
Lies: `./docs/planung/features-neu/06_einstellungen/TECHNICAL_CONCEPT_CORE.md`
- Settings Registry MVP verstehen
- 5-Level Scope-Hierarchie
- ETag-Caching Performance

## 🔒 WICHTIG: MIGRATION-CHECK (IGNORIEREN RISIKOBEHAFTET!)

**WICHTIG:** Jede DB-Änderung ohne Migration-Check kann zu Deployments-Problemen führen.

**RISIKEN BEI ÜBERSPRINGEN:**
- ⚠️ Migration-Konflikte im Team
- ⚠️ Deployment-Komplikationen in Staging
- ⚠️ Sprint 1.3+ könnten verzögert werden
- ⚠️ Security Foundation unvollständig

**VOR JEDER DB-ÄNDERUNG (EMPFOHLEN):**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
echo "Nächste Migration: $MIGRATION"
# Bei Script-Fehler: ls backend/src/main/resources/db/migration/ | tail -3
```

**BESTÄTIGUNG EMPFOHLEN:** Schreibe "✅ MIGRATION-CHECK ABGESCHLOSSEN: V{NUMMER}"

## 🎯 IMPLEMENTIERUNGS-AUFTRAG

**SPRINT:** Sprint 1.2: Security + Foundation
**PR-BRANCH-1:** feature/sprint-1-2-security-abac-rls-foundation-v{MIGRATION}-FP-228
**PR-BRANCH-2:** feature/sprint-1-2-settings-registry-core-v{MIGRATION+1}-FP-229
**MODULE:** 00_infrastruktur/sicherheit + 06_einstellungen
**MIGRATIONS:** V{MIGRATION} (ABAC/RLS) + V{MIGRATION+1} (Settings)
**GESCHÄTZTE ARBEITSZEIT:** 6-8 Stunden (2 PRs)

**SECURITY FOUNDATION bedeutet:**
- ABAC Policy Engine für komplexe B2B-Access-Control
- RLS Territory-Scoping (Deutschland/Schweiz)
- User-Lead-Protection (Owner/Kollaborator/Manager-Override)
- Integration mit CQRS Light Events

**SETTINGS REGISTRY bedeutet:**
- 5-Level Scope-Hierarchie (GLOBAL→TENANT→TERRITORY→ACCOUNT→CONTACT_ROLE)
- ETag-Caching für <50ms Performance
- JSON-Schema Validation
- LISTEN/NOTIFY für Cache-Invalidation

**SUCCESS-CRITERIA:**
- [ ] ABAC Policy Engine functional (Owner/Kollaborator/Manager-Override)
- [ ] RLS Territory-Scoping für Deutschland/Schweiz
- [ ] Settings Registry mit ETag-Performance ≥70% Hit-Rate (angemessen für internes Tool)
- [ ] 5 Security-Contract-Tests implementiert (fail-closed)
- [ ] Foundation für Module 02+03+05 bereit

## 🚀 IMPLEMENTIERUNGS-SCHRITTE

**SCHRITT 1: SECURITY FOUNDATION (PR #1)**

**1.1 MIGRATION-CHECK + BRANCH:**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
git checkout main && git pull
git checkout -b feature/sprint-1-2-security-abac-rls-foundation-v${MIGRATION}-FP-228
```

**1.2 ABAC POLICY ENGINE:**
- Policy-Engine für B2B-Food-Business-Rules
- Owner-Permissions (Lead-Creator hat vollen Access)
- Kollaborator-Permissions (Team-Member hat Read+Comment)
- Manager-Override (mit Audit-Trail)

**1.3 RLS TERRITORY-SCOPING:**
- Deutschland (EUR, 19% MwSt, German Business-Rules)
- Schweiz (CHF, 7.7% MwSt, Swiss Business-Rules)
- Row-Level Security Policies für alle Territory-relevanten Tabellen

**1.4 SECURITY-CONTRACT-TESTS:**
- Test: Owner kann Lead lesen/bearbeiten
- Test: Kollaborator kann Lead lesen, nicht bearbeiten
- Test: Manager kann mit Override + Audit
- Test: Territory-Isolation (DE User sieht keine CH Leads)
- Test: Fail-closed bei Policy-Fehlern

**SCHRITT 2: SETTINGS REGISTRY (PR #2)**

**2.1 MIGRATION-CHECK + BRANCH:**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
git checkout main && git pull
git checkout -b feature/sprint-1-2-settings-registry-core-v${MIGRATION}-FP-229
```

**2.2 5-LEVEL SCOPE-HIERARCHIE:**
```yaml
GLOBAL: System-weite Defaults
TENANT: FreshFoodz-spezifische Settings
TERRITORY: Deutschland vs Schweiz Settings
ACCOUNT: Kunde-spezifische Settings
CONTACT_ROLE: CHEF vs BUYER Settings
```

**2.3 ETAG-CACHING:**
- HTTP ETag für Settings-Performance
- ≥70% Hit-Rate Target
- LISTEN/NOTIFY für Cache-Invalidation
- JSON-Schema Validation

**SCHRITT 3: INTEGRATION TESTING:**
- ABAC + RLS Integration validieren
- Settings Registry Performance-Tests
- Cross-Foundation Security-Tests

## ⚠️ KRITISCHE REGELN

**SECURITY-FIRST:**
- Fail-closed bei allen Policy-Fehlern
- Territory-Isolation ist ABSOLUT (DE/CH getrennt)
- User-Lead-Protection ohne Gebietsschutz (deutschlandweit verfügbar)

**FOUNDATION-WICHTIG:**
- Diese Security-Foundation ist wichtig für Module 02+03+05
- Settings Registry ist essentiell für Module 01+04+06+07+08
- Performance <200ms P95 für Settings (Foundation-Baseline), <100ms für ETag-Hits (angemessen für 5-50 User)

## ✅ ERFOLGSMESSUNG

**SPRINT 1.2 IST FERTIG WENN:**
- [ ] 2 PRs erfolgreich merged (Security + Settings)
- [ ] 5 Security-Contract-Tests als Required PR-Checks aktiv
- [ ] Territory-Scoping DE/CH funktional
- [ ] Settings Registry ETag-Performance ≥70%
- [ ] SECURITY-GATE für Phase 2 bereit

**ROADMAP-UPDATE:**
Nach Sprint 1.2 Complete:
- Progress: 1/35 → 3/35 (2 PRs)
- Status: ✅ Sprint 1.2 (YYYY-MM-DD)
- Next Action: Sprint 1.3 Security Gates + CI

**BLOCKER-AUFLÖSUNG:**
- Module 02 Neukundengewinnung kann starten (Security ready)
- Module 03 Kundenmanagement kann starten (Territory ready)
- Module 05 Kommunikation wartet auf Security-Gate Validation

Arbeite systematisch PR #1 dann PR #2!