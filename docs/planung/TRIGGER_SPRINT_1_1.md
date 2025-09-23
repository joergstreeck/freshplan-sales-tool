# 🚀 VERBINDLICH: SPRINT 1.1 CQRS LIGHT FOUNDATION - SYSTEMATISCHE UMSETZUNG

## ⚠️ WICHTIGE QUALITÄTSREGELN - VOLLSTÄNDIG BEFOLGEN!

**HINWEIS:** Diese Anweisungen sind verbindlich für professionelle Umsetzung. Übersprungene Schritte führen zu:
- ❌ Inkonsistente CQRS-Implementierung → Wochen Nacharbeit
- ❌ Migration-Konflikte → Sprint 1.2+ blockiert
- ❌ Performance-Probleme → <200ms P95 nicht erreichbar (Foundation-Baseline)
- ❌ Integration-Failures → Module 02+03+05 betroffen

**QUALITÄTS-REGEL:** Du sollst jeden Abschnitt mit "✅ VERSTANDEN" bestätigen für optimale Ergebnisse.

**HINWEIS:** Bei übersprungenen Checkpoints kann die Qualität nicht garantiert werden.

## 📋 VERBINDLICH ZWINGEND: DOKUMENTE-VALIDIERUNG (REIHENFOLGE NICHT VERÄNDERBAR!)

**ENFORCEMENT-REGEL:** Du MUSST alle 7 Dokumente vollständig lesen. Kein Überspringen, kein "kenne ich schon", keine Abkürzungen.

**1. ROADMAP-ORIENTIERUNG (COMPLIANCE-CHECK):**
Lies VOLLSTÄNDIG: `./docs/planung/PRODUCTION_ROADMAP_2025.md`
- Section "🎯 CLAUDE QUICK-START" WORT-FÜR-WORT lesen
- Current Status + Next Action EXPLIZIT verstehen
- **BESTÄTIGE:** "✅ Ich habe PRODUCTION_ROADMAP komplett gelesen und verstehe den aktuellen Stand"

**2. ARBEITSREGELN (COMPLIANCE-CHECK):**
Lies VOLLSTÄNDIG: `./docs/CLAUDE.md`
- Die 17 kritischen Regeln ALLE durchgehen
- Sprache: IMMER Deutsch + Tests: Bei JEDER Implementierung ≥80%
- **BESTÄTIGE:** "✅ Ich habe alle 17 CLAUDE-Regeln gelesen und werde sie befolgen"

**3. BUSINESS-KONTEXT (COMPLIANCE-CHECK):**
Lies VOLLSTÄNDIG: `./docs/planung/CRM_AI_CONTEXT_SCHNELL.md`
- FreshFoodz B2B-Food-Geschäftsmodell KOMPLETT verstehen
- Multi-Channel-Vertrieb + Cook&Fresh® + Territory-Management
- **BESTÄTIGE:** "✅ Ich verstehe das FreshFoodz-Geschäftsmodell und keine Gebietsschutz-Regel"

**4. SPRINT-DETAIL (COMPLIANCE-CHECK):**
Lies VOLLSTÄNDIG: `./docs/planung/PRODUCTION_ROADMAP_2025.md` - Section "Sprint 1.1"
- Detaillierte Sprint-Pläne VOLLSTÄNDIG analysieren
- **BESTÄTIGE:** "✅ Ich habe Sprint 1.1 Details komplett gelesen und verstehe alle Success-Criteria"

**5. TECHNICAL CONCEPT (COMPLIANCE-CHECK):**
Lies VOLLSTÄNDIG: `./docs/planung/features-neu/00_infrastruktur/migrationen/technical-concept.md`
- CQRS Light vs Event-Sourcing Unterschiede verstehen
- PostgreSQL LISTEN/NOTIFY Strategie verstehen
- **BESTÄTIGE:** "✅ Ich verstehe CQRS Light Architektur und PostgreSQL LISTEN/NOTIFY"

**6. VERFÜGBARE ARTEFAKTE (COMPLIANCE-CHECK):**
Analysiere VOLLSTÄNDIG: `./docs/planung/features-neu/00_infrastruktur/migrationen/artefakte/`
- ALLE verfügbaren Artefakte durchgehen
- Copy-Paste-Ready Code identifizieren
- **BESTÄTIGE:** "✅ Ich habe alle verfügbaren Artefakte analysiert und weiß was ich nutzen kann"

**7. QUALITY-GATES (COMPLIANCE-CHECK):**
Lies VOLLSTÄNDIG: `./docs/planung/PRODUCTION_ROADMAP_2025.md` - Section "🔧 VERBINDLICHE QUALITY GATES"
- Required PR-Checks: security-contracts, k6-smoke, bundle-size ALLE verstehen
- 6-Block PR-Template verstehen
- **BESTÄTIGE:** "✅ Ich kenne alle Quality-Gates und PR-Template-Anforderungen"

## 🔒 NICHT-VERHANDELBAR: MIGRATION-CHECK (IGNORIEREN = SPRINT-FAILURE!)

**NICHT-VERHANDELBAR:** Jede DB-Änderung ohne Migration-Check führt zu kaputten Deployments und Sprint-Abbruch.

**CONSEQUENCES BEI ÜBERSPRINGEN:**
- 🔥 Migration-Konflikte im Team
- 🔥 Deployment-Failures in Staging
- 🔥 Sprint 1.2+ können nicht starten
- 🔥 CQRS Light Foundation unbrauchbar

**VOR JEDER DB-ÄNDERUNG (ZWINGEND):**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
echo "Nächste Migration: $MIGRATION"
# Bei Script-Fehler: ls backend/src/main/resources/db/migration/ | tail -3
```

**BESTÄTIGUNG ERFORDERLICH:** Schreibe "✅ MIGRATION-CHECK ABGESCHLOSSEN: V{NUMMER}"

## 🛡️ ZUSÄTZLICH: MOCK-GOVERNANCE SETUP (SPRINT 1.1 ERWEITERUNG)

**PRIORITY 1 - GOVERNANCE FOUNDATION:**

**MOCK-GOVERNANCE (PARALLEL ZU CQRS):**
- [ ] ADR-0006 "Mock-Governance" dokumentiert
- [ ] ESLint-Regel "no mocks in business logic" aktiviert
- [ ] CI-Guard `mock-guard.yml` aktiviert
- [ ] Dev-Flyway-Pfad eingerichtet (`db/dev-migration`)
- [ ] Optional: Husky pre-commit Hook

**ERFORDERLICHE BESTÄTIGUNG:** Schreibe "✅ MOCK-GOVERNANCE SETUP ABGESCHLOSSEN"

**WARUM PARALLEL:**
- CQRS Foundation braucht saubere Governance-Basis
- Mock-freie APIs ermöglichen echte Integration-Tests
- Dev-Seeds unterstützen CQRS Event-Testing

## 🎯 COMPLIANCE-CHECKPOINT-SYSTEM - OBLIGATORISCHE VALIDIERUNG

**REGEL:** NUR wenn alle 4 Checkpoints bestätigt sind, DANN darfst du mit Implementation beginnen!

### CHECKPOINT 1: BUSINESS-CONTEXT VERSTÄNDNIS
**Frage:** Nenne 3 FreshFoodz-spezifische Geschäftsregeln aus CRM_AI_CONTEXT_SCHNELL.md
**Erwartung:** Kein Gebietsschutz + Multi-Contact B2B + Territory Deutschland/Schweiz
**Bestätigung:** [ ] ✅ "CHECKPOINT 1 BESTANDEN"

### CHECKPOINT 2: CQRS-LIGHT VERSTÄNDNIS
**Frage:** Was ist der Unterschied zwischen CQRS Light und Full Event-Sourcing?
**Erwartung:** PostgreSQL LISTEN/NOTIFY statt Event-Bus, eine Datenbank
**Bestätigung:** [ ] ✅ "CHECKPOINT 2 BESTANDEN"

### CHECKPOINT 3: MIGRATION-STRATEGIE
**Frage:** Warum ist dynamische Migration-Ermittlung kritisch?
**Erwartung:** Vermeidet Konflikte, Team-Koordination, Migration-Hardcoding gefährlich
**Bestätigung:** [ ] ✅ "CHECKPOINT 3 BESTANDEN"

### CHECKPOINT 4: PERFORMANCE-ZIELE
**Frage:** Welche Performance-Ziele gelten für CQRS Light Foundation?
**Erwartung:** <200ms P95 für Events (Foundation-Baseline), Foundation für alle nachfolgenden Module
**Bestätigung:** [ ] ✅ "CHECKPOINT 4 BESTANDEN"

**GATE-FREIGABE:** NUR wenn alle 4 ✅ vorhanden: Schreibe "🚀 IMPLEMENTATION-FREIGABE ERTEILT"

## 🛡️ IMPLEMENTIERUNGS-GATES - BEDINGTE FREIGABEN

**STOPP-REGEL:** Jeder Schritt hat Validation-Requirements. Bei Fehlschlag → STOP!

### GATE 1: MIGRATION PREPARATION ⛔
**BEDINGUNG:** Alle 7 Dokumente gelesen + Migration-Check + 4 Checkpoints bestätigt
**VALIDATION:** Alle "✅ BESTÄTIGE" Statements sind vorhanden
**BEI FEHLSCHLAG:** Return to PFLICHT-LESEFOLGE, keine Implementation erlaubt
**FREIGABE-CODE:** "🟢 GATE 1 FREIGEGEBEN"

### GATE 2: CQRS IMPLEMENTATION ⛔
**BEDINGUNG:** Gate 1 freigegeben + Event-Schema designed + Tests ≥80% Coverage
**VALIDATION:** PostgreSQL LISTEN/NOTIFY Setup funktional
**BEI FEHLSCHLAG:** Rollback Migration, Debug vor Fortsetzung
**FREIGABE-CODE:** "🟢 GATE 2 FREIGEGEBEN"

### GATE 3: INTEGRATION TESTING ⛔
**BEDINGUNG:** Gate 2 freigegeben + Cross-Module Events funktional
**VALIDATION:** Performance <200ms P95 bestätigt
**BEI FEHLSCHLAG:** Performance-Optimierung erforderlich
**FREIGABE-CODE:** "🟢 GATE 3 FREIGEGEBEN"

### GATE 4: PR CREATION ⛔
**BEDINGUNG:** Gate 3 freigegeben + Alle Quality-Gates erfüllt + 6-Block PR-Template
**VALIDATION:** Security-Contracts + k6-smoke + bundle-size alle grün
**BEI FEHLSCHLAG:** Quality-Gates nicht erfüllt, PR blockiert
**FREIGABE-CODE:** "🟢 PR-ERSTELLUNG AUTORISIERT"

**ENFORCEMENT:** Ohne korrekten Freigabe-Code wird der nächste Schritt VERWEIGERT!

---

## 🎯 IMPLEMENTIERUNGS-AUFTRAG

**SPRINT:** Sprint 1.1: CQRS Light Foundation (Foundation-First Strategy)
**PR-BRANCH:** feature/sprint-1-1-cqrs-light-foundation-v{MIGRATION}-FP-225
**MODULE:** 00_infrastruktur/migrationen
**MIGRATION:** V{MIGRATION} (dynamisch ermittelt)
**GESCHÄTZTE ARBEITSZEIT:** 4-6 Stunden (1 PR)

**CQRS LIGHT FOUNDATION bedeutet:**
- PostgreSQL LISTEN/NOTIFY Event-System (NICHT Event-Bus)
- Event-Schema für Cross-Module Communication
- Foundation für alle nachfolgenden Module (02+03+05+01+04+06+07+08)
- Cost-Efficient für internes Tool (5-50 Benutzer)

**SUCCESS-CRITERIA:**
- [ ] CQRS Light Event-System operational mit PostgreSQL LISTEN/NOTIFY
- [ ] Event-Schema für Cross-Module Communication definiert
- [ ] Performance <200ms P95 für Event-Publishing
- [ ] Foundation bereit für Module 02 Neukundengewinnung
- [ ] Security-Contracts + Quality-Gates erfüllt

## 🚀 IMPLEMENTIERUNGS-SCHRITTE

**NUR NACH GATE 1 FREIGABE:**

**SCHRITT 1: MIGRATION-CHECK + BRANCH ERSTELLEN**
```bash
# NUR wenn alle Compliance-Checkpoints ✅:
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
git checkout main && git pull
git checkout -b feature/sprint-1-1-cqrs-light-foundation-v${MIGRATION}-FP-225
```

**SCHRITT 2: VERFÜGBARE ARTEFAKTE ANALYSIEREN (GATE 2 ERFORDERLICH)**
- Analysiere verfügbare Dokumentation in `/00_infrastruktur/migrationen/`
- Nutze existing PostgreSQL Setup als Grundlage
- Entwickle CQRS Light Event-Schema basierend auf FreshFoodz Business-Requirements
- **HINWEIS:** Fokus auf pragmatische Lösung für 5-50 User internes Tool

**SCHRITT 3: IMPLEMENTIERUNG (GATE 2 ERFORDERLICH)**
- Event-Schema Definitionen (EventType, EventPayload, EventMetadata)
- PostgreSQL LISTEN/NOTIFY Channel Setup
- Event-Publishing Service Implementation
- Event-Subscription Service Implementation

**SCHRITT 4: QUALITY-GATES (GATE 3 ERFORDERLICH)**
- Unit-Tests ≥80% Coverage
- Integration-Tests für LISTEN/NOTIFY
- Performance-Tests <200ms P95 (Foundation-Baseline)
- Security-Contract-Tests

**SCHRITT 5: PR ERSTELLEN (GATE 4 ERFORDERLICH)**
- 6-Block PR-Template verwenden
- Alle Quality-Gates bestätigen
- Roadmap-Update mit Progress-Tracking

## ⚠️ KRITISCHE REGELN

**FOUNDATION-FIRST:**
- Diese Foundation blockiert ALLE nachfolgenden Module
- Performance <200ms P95 ist wichtig für alle 8 Module (Foundation-Baseline)
- PostgreSQL LISTEN/NOTIFY ist bevorzugt (kosteneffizient, kein Event-Bus nötig)

**QUALITY-GATES:**
- Security-Contracts sollten grün sein vor PR
- Performance-Tests sollten <200ms P95 zeigen
- Bundle-Size sollte im Budget bleiben

## ✅ ERFOLGSMESSUNG

**SPRINT 1.1 IST FERTIG WENN:**
- [ ] 1 PR erfolgreich merged (CQRS Light Foundation)
- [ ] Event-System operational mit PostgreSQL LISTEN/NOTIFY
- [ ] Performance <200ms P95 bestätigt (Foundation-Baseline)
- [ ] Foundation für Module 02+03+05 bereit
- [ ] Alle Gates 1-4 durchlaufen und freigegeben

**ROADMAP-UPDATE:**
Nach Sprint 1.1 Complete:
- Progress: 0/35 → 1/35 (1 PR)
- Status: ✅ Sprint 1.1 (YYYY-MM-DD)
- Next Action: Sprint 1.2 Security + Foundation
- Blocker released: CQRS Foundation für alle Module verfügbar

**🚀 FOUNDATION COMPLETE:**
Module 02 Neukundengewinnung kann auf CQRS Light Foundation starten!

**Arbeite systematisch durch alle Gates 1 → 2 → 3 → 4!**