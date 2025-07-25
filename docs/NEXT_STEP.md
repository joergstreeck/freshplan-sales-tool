# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**M4 BACKEND-INTEGRATION PR #66 - READY TO MERGE ✅**

**Stand 25.07.2025 16:35:**
- ✅ **PR #66:** https://github.com/joergstreeck/freshplan-sales-tool/pull/66
- ✅ **CI GRÜN:** Alle Checks erfolgreich
- ✅ **Code Review Issues behoben:**
  - Alle Log-Dateien entfernt
  - Alle Backup-Dateien entfernt  
  - Claude-Work Dokumente entfernt
  - .gitignore aktualisiert
  - ChangeStageRequest Type korrigiert
- ✅ **TODO-60 FERTIG:** M4 Backend-Integration 100% - API vollständig verbunden
- ✅ **Tests:** KanbanBoard 14/14 grün, Gesamt 93% Test Coverage

**➡️ BEREIT ZUM MERGE!**

**🚀 NÄCHSTER SCHRITT:**

**TODO-64: RENEWAL Spalte (7. Stage) hinzufügen:**
1. Backend: RENEWAL zu OpportunityStage Enum hinzufügen
2. Frontend: ACTIVE_STAGES um RENEWAL erweitern
3. Tests für neue Stage schreiben
4. Kanban Board visuell testen

**ALTERNATIVE NÄCHSTE SCHRITTE:**
- Branch-Protection für main aktivieren (TODO-94)
- Backup Smoke Tests Workflow entfernen (TODO-97)
- M4: 7. Spalte RENEWAL hinzufügen (TODO-64)

**VOLLSTÄNDIG ABGESCHLOSSEN IN DIESER SESSION:**
- ✅ TODO-102: M4 Frontend Enterprise Upgrade
- ✅ TODO-104: KanbanBoardDndKit refactoring
- ✅ TODO-106: OpportunityCard refactoring  
- ✅ TODO-107: PipelineStage refactoring
- ✅ TODO-108: Two-Pass Review mit Test-Fixes

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