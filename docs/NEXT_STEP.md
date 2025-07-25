# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**M4 FRONTEND ENTERPRISE UPGRADE - ABGESCHLOSSEN ✅**

**Stand 25.07.2025 13:45:**
- ✅ **PR #63:** Erfolgreich gemerged
- ✅ **KanbanBoard.tsx:** Vollständig auf Enterprise-Standard refactored + Tests
- ✅ **KanbanBoardDndKit.tsx:** Vollständig refactored inkl. Scroll-Handler Optimierung
- ✅ **OpportunityCard.tsx:** Enterprise-Standard refactoring abgeschlossen + Tests
- ✅ **Frontend Fixes:** process.env → import.meta.env Migration
- ✅ **Tests:** 296 von 298 Frontend Tests bestehen

**🚀 NÄCHSTER SCHRITT:**

**PipelineStage.tsx Enterprise Refactoring (TODO-107):**
1. React.memo() hinzufügen
2. useCallback() für Event Handler
3. Logger-Integration
4. JSDoc-Dokumentation
5. Tests schreiben

**ABGESCHLOSSEN IN DIESER SESSION:**
- ✅ TODO-102: M4 Frontend Enterprise Upgrade
- ✅ TODO-104: KanbanBoardDndKit refactoring
- ✅ TODO-106: OpportunityCard refactoring

**ALTERNATIVE NÄCHSTE SCHRITTE:**
- Backup Smoke Tests Workflow entfernen (TODO-97)
- OpportunityDataInitializer implementieren (TODO-84)
- M4 Backend-Integration: OpportunityApi.ts verbinden (TODO-60)

**ABGESCHLOSSEN HEUTE:**
- ✅ CI-Probleme analysiert (4 rote PRs)
- ✅ PR #62 bereinigt (Force-Push ohne Log-Dateien)
- ✅ Neuer sauberer PR #63 erstellt
- ✅ Alte PRs #59-62 geschlossen
- ✅ Repository-Hygiene wiederhergestellt

```bash
# PR Status prüfen:
gh pr view 63 --json state,statusCheckRollup

# Nach Merge:
git checkout main && git pull
git branch -d fix/combined-m4-frontend-fixes

# Kanban Board testen:
http://localhost:5173/kundenmanagement/opportunities

# API testen (zeigt noch []):
curl http://localhost:8080/api/opportunities
```

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
./scripts/reality-check.sh FC-002  # M4 Opportunity Pipeline Check
```

---

## 📊 OFFENE TODOS:
```
🔴 HIGH Priority: 16 TODOs
🟡 MEDIUM Priority: 4 TODOs  
🟢 LOW Priority: 2 TODOs
```

**Status:**
- M4 Backend: ✅ PRODUCTION-READY (100% fertig)
- M4 Frontend: ✅ Code sauber, Tests vorhanden
- M4 Tests: ✅ NavigationSubMenu Test hinzugefügt
- M4 Integration: 🔴 BLOCKIERT - Backend liefert keine Testdaten