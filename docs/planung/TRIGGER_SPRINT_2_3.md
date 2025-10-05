# 🚀 VERBINDLICH: SPRINT 2.3 KOMMUNIKATION - SYSTEMATISCHE UMSETZUNG (NACH SECURITY-GATE)

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

## ⚠️ WICHTIGE QUALITÄTSREGELN - VOLLSTÄNDIG BEFOLGEN!

**HINWEIS:** Diese Anweisungen sind verbindlich für professionelle Umsetzung. Übersprungene Schritte führen zu:
- ❌ Inkonsistente Thread/Message/Outbox-Implementierung → Wochen Nacharbeit
- ❌ Email-Engine-Probleme → B2B-Communication fehlerhaft
- ❌ SLA-Engine T+3/T+7 Probleme → Follow-up-Automation defekt
- ❌ Integration-Failures → Cockpit-Module (Sprint 2.4) betroffen

**QUALITÄTS-REGEL:** Du sollst jeden Abschnitt mit "✅ VERSTANDEN" bestätigen für optimale Ergebnisse.

**HINWEIS:** Bei übersprungenen Checkpoints kann die Qualität nicht garantiert werden.

## 📋 VERBINDLICH: DOKUMENTE-VALIDIERUNG (REIHENFOLGE EMPFOHLEN)

**1. ROADMAP-ORIENTIERUNG:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md`
- Section "🎯 CLAUDE QUICK-START"
- 🚨 SECURITY-GATE CHECKPOINT bestätigen!

**2. ARBEITSREGELN:**
Lies: `./docs/CLAUDE.md`
- Die 17 kritischen Regeln beachten

**3. BUSINESS-KONTEXT:**
Lies: `./docs/planung/CRM_AI_CONTEXT_SCHNELL.md`
- Thread/Message/Outbox-Pattern für B2B-Communication
- SLA-Engine T+3/T+7
- Multi-Contact-Communication (CHEF/BUYER)

**4. SPRINT-DETAIL:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md` - Section "Sprint 2.3: Kommunikation"
- 🚨 Verbindliche Freigabe-Kriterien VOR diesem Sprint!

**5. TECHNICAL CONCEPT:**
Lies: `./docs/planung/features-neu/05_kommunikation/technical-concept.md`
- Enterprise Email-Engine
- Best-of-Both-Worlds Architecture (8.6/10 Enterprise-Ready)
- Shared Email-Core + B2B-Food SLA-Engine

**6. VERFÜGBARE ARTEFAKTE:**
Analysiere: `./docs/planung/features-neu/05_kommunikation/artefakte/`
- 41 Production-Ready Files
- DevOps Excellence + Business Logic Perfektion

**🎯 COPY-PASTE READY PATTERNS (aus PR #110):**
- `./docs/planung/features-neu/02_neukundengewinnung/artefakte/EVENT_SYSTEM_PATTERN.md`
  → Nutze für EMAIL_SENT/RECEIVED Events (LISTEN/NOTIFY mit AFTER_COMMIT)
- `./docs/planung/features-neu/02_neukundengewinnung/artefakte/SECURITY_TEST_PATTERN.md`
  → Nutze für Thread-Access-Control Tests (@TestSecurity)
- `./docs/planung/features-neu/02_neukundengewinnung/artefakte/PERFORMANCE_TEST_PATTERN.md`
  → Nutze für Message-Query P95 Validation

## 🔒 WICHTIG: SECURITY-GATE CHECKPOINT VALIDIERUNG

**EMPFOHLENE FREIGABE-KRITERIEN (SOLLTEN ✅ SEIN):**
```bash
# Diese Checks sollten grün sein:
./scripts/validate-security-gate-phase-2.sh

Erwartetes Ergebnis:
✅ Lead-Management (02) operational mit ABAC/RLS
✅ Customer-Management (03) operational mit Territory-Scoping
✅ 5 Security-Contract-Tests weiterhin grün
✅ Owner/Kollaborator/Manager-Override mit Audit funktional
✅ Cross-Module Events (LISTEN/NOTIFY) zwischen 02+03 funktional
```

**HINWEIS:** Bei nicht-grünem Security-Gate kann die Qualität nicht garantiert werden.

## 🔒 WICHTIG: MIGRATION-CHECK (IGNORIEREN RISIKOBEHAFTET!)

**WICHTIG:** Jede DB-Änderung ohne Migration-Check kann zu Deployments-Problemen führen.

**RISIKEN BEI ÜBERSPRINGEN:**
- ⚠️ Migration-Konflikte im Team
- ⚠️ Deployment-Komplikationen in Staging
- ⚠️ Sprint 2.4+ könnten verzögert werden
- ⚠️ Communication-Module unvollständig

**VOR JEDER DB-ÄNDERUNG (EMPFOHLEN):**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
echo "Nächste Migration: $MIGRATION"
# Bei Script-Fehler: ls backend/src/main/resources/db/migration/ | tail -3
```

**BESTÄTIGUNG EMPFOHLEN:** Schreibe "✅ MIGRATION-CHECK ABGESCHLOSSEN: V{NUMMER}"

## 🎯 IMPLEMENTIERUNGS-AUFTRAG

**SPRINT:** Sprint 2.3: Kommunikation (empfohlen nach Security-Gate ✅)
**MODULE:** 05_kommunikation
**GESCHÄTZTE ARBEITSZEIT:** 6-8 Stunden (4 PRs)

**PRs IN DIESEM SPRINT:**
1. **Thread-Message-Core:** feature/sprint-2-3-comm-thread-message-core-v{MIGRATION}-FP-242
2. **Outbox-Pattern:** feature/sprint-2-3-comm-outbox-pattern-v{MIGRATION+1}-FP-243
3. **Email-Engine:** feature/sprint-2-3-comm-email-engine-v{MIGRATION+2}-FP-244
4. **Security-Integration:** feature/sprint-2-3-comm-security-integration-v{MIGRATION+3}-FP-245

**KOMMUNIKATION bedeutet:**
- Thread/Message-Pattern mit Lead/Account-Context
- Userbasierte Ownership (Owner/Kollaborator)
- Enterprise Email-Engine mit Reliability
- SLA-Engine T+3/T+7 Integration

**SUCCESS-CRITERIA:**
- [ ] Thread/Message-Pattern mit Lead/Account-Integration
- [ ] Userbasierte Ownership für Communication
- [ ] Email-Engine operational mit Enterprise-Reliability
- [ ] ABAC/RLS für Kommunikation validiert
- [ ] Cross-Module Activity-Timeline funktional

## 🚀 IMPLEMENTIERUNGS-SCHRITTE

**PR #1: THREAD-MESSAGE-CORE (Day 32-33)**

**1.1 MIGRATION-CHECK + BRANCH:**
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
git checkout main && git pull
git checkout -b feature/sprint-2-3-comm-thread-message-core-v${MIGRATION}-FP-242
```

**1.2 THREAD/MESSAGE-PATTERN:**
- Threads gruppieren Messages zu Lead/Account-Context
- Message-Threading mit Parent-Child-Relationships
- User-Ownership Integration (wer darf lesen/schreiben)

**1.3 LEAD/ACCOUNT-CONTEXT:**
- Threads referenzieren Leads (aus Sprint 2.1)
- Threads referenzieren Accounts (aus Sprint 2.2)
- Cross-Module-Events für Activity-Timeline

**1.4 USERBASIERTE OWNERSHIP:**
- Thread-Creator wird Owner
- Kollaboratoren können hinzugefügt werden
- ABAC-Integration für Access-Control

**PR #2: OUTBOX-PATTERN (Day 34-35)**

**2.1 OUTBOX-PATTERN IMPLEMENTATION:**
- Outbox-Table für Email-Reliability
- At-least-once Delivery garantiert
- Idempotency für Duplicate-Prevention

**2.2 BOUNCE-HANDLING:**
- Email-Bounce-Detection + Status-Tracking
- Automatic Retry-Logic mit Backoff
- Dead-Letter-Queue für failed Messages

**2.3 SLA-ENGINE INTEGRATION:**
- T+3 Sample-Follow-up aus Lead-Management
- T+7 Bulk-Order-Follow-up
- CQRS Light Events für SLA-Triggers

**PR #3: EMAIL-ENGINE (Day 36-37)**

**3.1 ENTERPRISE EMAIL-ENGINE:**
- SMTP-Integration mit Connection-Pooling
- Template-System für personalisierte Emails
- Multi-Contact Email-Workflows (CHEF + BUYER)

**3.2 B2B-FOOD EMAIL-TEMPLATES:**
- Sample-Request Templates
- Follow-up Templates (T+3/T+7)
- ROI-Demonstration Templates
- Product-Catalog Templates

**3.3 PERSONALIZATION:**
- CHEF-spezifische Templates (Menu-Planung Focus)
- BUYER-spezifische Templates (Cost-Optimization Focus)
- Territory-spezifische Templates (DE vs CH)

**PR #4: SECURITY-INTEGRATION (Day 38)**

**4.1 COMMUNICATION ABAC/RLS:**
- Communication-Policies für Thread-Access
- Territory-Scoping für Email-Templates
- User-Ownership-Validation End-to-End

**4.2 ACTIVITY-TIMELINE CROSS-MODULE:**
- LISTEN/NOTIFY Events für Communication-Activities
- Integration mit Lead-Timeline (Sprint 2.1)
- Integration mit Customer-Timeline (Sprint 2.2)

**4.3 PERFORMANCE-VALIDATION:**
- Email-Engine-Load-Tests (Burst-Handling)
- Thread-Queries <400ms P95 (Heavy-Tier), alle Standard-Endpoints <200ms P95
- Outbox-Processing-Performance

## ⚠️ KRITISCHE REGELN

**SECURITY-GATE ENFORCEMENT:**
- Dieser Sprint startet NUR bei grünem Security-Gate
- Lead/Account-Referenzen setzen funktionierende Module 02+03 voraus
- Communication MUSS userbasierte Ownership respektieren

**ENTERPRISE-RELIABILITY:**
- Email-Delivery muss guaranteed sein (Outbox-Pattern)
- Bounce-Handling ist essentiell für B2B-Communication
- SLA-Engine T+3/T+7 ist Business-Critical

## ✅ ERFOLGSMESSUNG

**SPRINT 2.3 IST FERTIG WENN:**
- [ ] 4 PRs erfolgreich merged (Thread + Outbox + Email + Security)
- [ ] Thread/Message mit Lead/Account-Integration
- [ ] Enterprise Email-Engine mit Reliability
- [ ] ABAC/RLS für Communication bestätigt
- [ ] Cross-Module Activity-Timeline funktional
- [ ] SLA-Engine T+3/T+7 operational

**ROADMAP-UPDATE:**
Nach Sprint 2.3 Complete:
- Progress: 13/35 → 17/35 (4 PRs)
- Status: ✅ Sprint 2.3 (YYYY-MM-DD)
- Next Action: Sprint 2.4 Cockpit
- Integration: Communication ready für Dashboard-Integration

**BUSINESS-VALUE ERREICHT:**
- Enterprise-Grade Communication für B2B-Food-CRM
- Automated Follow-up-Workflows T+3/T+7
- Multi-Contact-Communication für CHEF/BUYER
- Activity-Timeline für Complete Customer-Journey

**🔓 NEXT PHASE READY:**
Sprint 2.4 Cockpit kann jetzt alle Communication-Events für Real-time Dashboard nutzen!

Arbeite systematisch PR #1 → #2 → #3 → #4 (NUR nach Security-Gate ✅)!