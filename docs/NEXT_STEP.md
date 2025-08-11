# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**CODE QUALITY INITIATIVE - Sprint 1 beginnt! 🚀**

**Stand 11.08.2025 23:50:**
- ✅ **ENTERPRISE_CODE_REVIEW_2025.md** erstellt mit exakten Metriken
- ✅ **CODE_QUALITY_PR_ROADMAP.md** erstellt mit detaillierter Sprint-Planung
- ✅ **54 Commits bereit** für Baseline-PR
- 📋 **Option B gewählt:** Granulare PRs (3-5 PRs pro Sprint)

### 🚨 NÄCHSTER SCHRITT FÜR NEUEN CLAUDE:

1. **ZUERST: Baseline-PR erstellen!**
```bash
# Branch sollte sein: feature/code-review-improvements
git branch --show-current

# Push und PR erstellen
git push origin feature/code-review-improvements
gh pr create --title "feat: code quality baseline - 100% ESLint compliance" \
  --body "## 🎯 Zusammenfassung
- 100% ESLint Compliance erreicht (von 421 auf 0 Errors)
- Test-Erfolgsrate: 89.1% (590 von 662 Tests)
- 54 Commits mit gezielten Verbesserungen
- Dokumentation in ENTERPRISE_CODE_REVIEW_2025.md

Details siehe: /docs/features/CODE_QUALITY_PR_ROADMAP.md"
```

2. **DANN: Sprint 1 starten (siehe CODE_QUALITY_PR_ROADMAP.md)**
   - PR #1: Console Cleanup Frontend (~87 Dateien)
   - PR #2: TypeScript array types (~40 Dateien)  
   - PR #3: Event handler types (~30 Dateien)

### 📊 Aktuelle Metriken:
- **Console Statements:** 2.562 (Ziel: 0)
- **TypeScript 'any':** 1.621 (Ziel: <50)
- **Große Dateien:** 188 (Ziel: <20)
- **Test Coverage:** 89.1% (Ziel: 95%)

### 📍 Wichtige Dateien für neuen Claude:
- **Start:** [`/docs/CODE_QUALITY_START_HERE.md`](/docs/CODE_QUALITY_START_HERE.md) ← BEI CODE QUALITY HIER STARTEN!
- **Roadmap:** [`/docs/features/CODE_QUALITY_PR_ROADMAP.md`](/docs/features/CODE_QUALITY_PR_ROADMAP.md) ← Detaillierter Plan
- **Review:** [`/docs/features/ENTERPRISE_CODE_REVIEW_2025.md`](/docs/features/ENTERPRISE_CODE_REVIEW_2025.md) ← Alle Findings
- **Tests:** [`/docs/features/TEST_STRATEGY_PER_PR.md`](/docs/features/TEST_STRATEGY_PER_PR.md) ← Test-Pläne
- **Letzte Übergabe:** `/docs/claude-work/daily-work/2025-08-11/`

---

**Navigation:**  
⬅️ Zurück zu: [`CODE_QUALITY_START_HERE.md`](/docs/CODE_QUALITY_START_HERE.md)  
➡️ Weiter zu: [`CODE_QUALITY_PR_ROADMAP.md`](/docs/features/CODE_QUALITY_PR_ROADMAP.md)

---

## 🔥 ALTE THEMEN (nach Code Quality Sprint 1):

### FC-005 CONTACT MANAGEMENT UI
**Status:** PR #81 wartet auf Merge
- ✅ **PR 1 MERGED:** Core Audit System (#78)
- ✅ **PR 2 MERGED:** Audit Admin Dashboard (#80)
- ✅ **PR 3 ERSTELLT:** Contact Management UI - PR #81

**Was heute gemacht wurde (09.08.2025 23:30):**
1. **ESLint-Cleanup durchgeführt:**
   - Von 310 auf 308 Fehler reduziert
   - 6 erfolgreiche Commits mit verschiedenen Fixes
   - Alle require() imports entfernt
2. **PR #81 erfolgreich erstellt:**
   - Contact Management UI mit Audit Timeline
   - Ausführliche deutsche PR-Beschreibung
   - Build ist GRÜN, Tests teilweise grün
3. **Repository aufgeräumt:**
   - Ungetrackte Verzeichnisse entfernt
   - DataQualityMetricsDTO.java zu Git hinzugefügt

**🚀 NÄCHSTER SCHRITT:**

### 1. CI-Status von PR #81 überwachen
```bash
gh pr checks 81
# Bei Fehler: Logs analysieren und fixen
```

### 2. Nach erfolgreicher CI
```bash
# PR mergen lassen
# FC-005 ist dann komplett!

- Add CustomerDetailPage with 4 tabs (Overview, Contacts, Activities, Audit)
- Create EntityAuditTimeline component for generic entity auditing
- Implement role-based visibility (Manager, Admin, Auditor)
- Add comprehensive tests for CustomerDetailPage
- Configure routing for /customers/:customerId"

# 2. PR erstellen
gh pr create --title "feat(audit): Contact Management UI with Timeline (PR 3/3)" \
  --body "## 🎯 Zusammenfassung
  CustomerDetailPage mit integrierter Audit Timeline
  
  ## ✅ Was wurde umgesetzt
  - CustomerDetailPage mit 4 Tabs
  - EntityAuditTimeline (generisch für alle Entities)
  - Role-based Visibility
  - Routing konfiguriert
  - Tests geschrieben"
```

**Optionen für nächste Session:**
1. **SOFORT PR erstellen** (UI ist fertig):
   ```bash
   cd /Users/joergstreeck/freshplan-sales-tool
   gh pr create --title "feat(audit): Complete Audit Admin Dashboard UI with Navigation (PR 2/3)" \
     --body "## 🎯 Zusammenfassung
   Vollständig funktionsfähiges Audit Admin Dashboard mit Sidebar-Integration.
   
   ## ✅ Was wurde umgesetzt
   - 12 Admin-Komponenten implementiert
   - Sidebar Navigation mit Admin-Bereich
   - Role-based Access Control funktioniert
   - 127 Tests geschrieben (TDD-ready)
   - Material-UI v5 + FreshFoodz CI
   
   ## 🧪 Testing
   - Frontend läuft: http://localhost:5173
   - Admin-Bereich sichtbar in Sidebar
   - Route /admin/audit funktioniert
   
   ## 📋 Nächste PRs
   - PR 3: Backend Audit APIs
   - PR 4: WebSocket Integration"
   ```

2. **Backend APIs implementieren** (wenn PR später gewünscht)
3. **WebSocket Integration** (Real-time Features)

```bash
cd /Users/joergstreeck/freshplan-sales-tool/backend

# 1. Unit Tests erstellen
touch src/test/java/de/freshplan/audit/service/AuditServiceTest.java
touch src/test/java/de/freshplan/audit/repository/AuditRepositoryTest.java

# 2. Code committen
git commit -m "feat(audit): Implement core audit system with DSGVO compliance

- Add AuditLog entity with hash-chain for tamper detection
- Add AuditService for comprehensive logging
- Add AuditRepository with compliance queries
- Add Migration V212 for audit_logs table
- DSGVO-compliant with retention policies"

# 3. PR erstellen
git push origin feature/fc-005-audit-core
gh pr create --title "feat(audit): Core Audit System (PR 1/3)"
```

**UNTERBROCHEN BEI:**
- PR 1 implementiert aber noch nicht committed
- Unit Tests noch zu schreiben
- Branch: feature/fc-005-audit-core

**AKTUELLE POSITION:**
- ✅ FC-012: IN FC-005 INTEGRIERT
- ✅ Audit Backend: IMPLEMENTIERT
- 🎯 Nächstes: Unit Tests + PR erstellen

**WICHTIGE DOKUMENTE:**
- **Audit Trail System:** `/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_TRAIL_SYSTEM.md`
- **Audit Admin Dashboard:** `/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_ADMIN_DASHBOARD.md`
- **AKTUELLE Übergabe:** `/docs/claude-work/daily-work/2025-08-08/2025-08-08_HANDOVER_21-45.md`

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
# Backend Service Status:
curl http://localhost:8080/api/ping
# Sollte: JSON Response

# Migration Status:
ls -la backend/src/main/resources/db/migration/ | tail -1
# Nächste: V213

# Branch Status:
git branch --show-current
# Sollte: feature/fc-005-audit-core
```

---

## 📊 AKTUELLER STATUS:
```
🟢 Contact Management: ✅ ENTERPRISE-STANDARD (PR #77 merged)
🟢 Audit Core: ✅ IMPLEMENTIERT (1400 Zeilen)
🟡 Unit Tests: 🔄 TODO
🟡 PR 1: 🔄 Ready to create
🟢 CI/CD: ✅ VOLLSTÄNDIG GRÜN
```

**3 PRs Roadmap:**
- PR 1: Core Audit System (~1400 Zeilen) ✅ IMPLEMENTIERT
- PR 2: Audit Admin Dashboard (~2500 Zeilen) 📋 GEPLANT
- PR 3: Contact Management UI (~2900 Zeilen) 📋 GEPLANT