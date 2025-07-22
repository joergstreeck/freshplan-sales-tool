# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**⚠️ KRITISCHE STRUKTUR-PROBLEME ENTDECKT - PROGRAMMIERUNG GESTOPPT**

**Stand 22.07.2025 00:50:**
- ❌ 262 Dateien mit kaputten relativen Pfaden
- ❌ Master Plan V4 wird überall verlinkt statt V5
- ❌ Tests sagen "PASSED" aber finden echte Probleme nicht
- ❌ Feature Registry nicht synchron mit Realität
- ✅ Platzhalter bereinigt (28 gelöscht)
- ✅ Ordner-Konflikte gelöst (56-63)

**🚨 NÄCHSTER SCHRITT:**

**TODO-66: Master Plan V4 archivieren, V5 als Standard**
```bash
# KRITISCH - Das MUSS zuerst gemacht werden!
git mv docs/CRM_COMPLETE_MASTER_PLAN.md docs/LEGACY/CRM_COMPLETE_MASTER_PLAN_V4.md
git mv docs/CRM_COMPLETE_MASTER_PLAN_V5.md docs/CRM_COMPLETE_MASTER_PLAN.md

# Dann alle Links updaten
find . -name "*.md" -exec sed -i 's/CRM_COMPLETE_MASTER_PLAN_V5/CRM_COMPLETE_MASTER_PLAN/g' {} \;
```

**DANACH: TODO-67 - 262 relative Pfade fixen**
```bash
# Script schreiben das alle ../features/ → /docs/features/ konvertiert
vim scripts/fix-relative-paths.sh
```

**UNTERBROCHEN BEI:**
- Struktur-Validierung ergab erschreckende Wahrheit
- Tests lügen über echten Zustand
- User frustriert: "Das ist eine Katastrophe"
- Nächster Schritt: Strukturelle Probleme ZUERST beheben

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
./scripts/reality-check.sh FC-XXX  # Plan vs. Code abgleichen
```
Der 3-Stufen Prozess: 1) Plan lesen 2) Code lesen 3) Abgleich bestätigen

**TEST-SUITE MUSS ÜBERARBEITET WERDEN**
- Link-Test prüft nur Syntax, nicht Existenz
- Coverage-Test wurde repariert (81% real)
- Structure-Test findet Duplikate, aber nicht alle Probleme

---

## 📊 Fortschritt CLAUDE TECH Migration:
```
✅ 100% CLAUDE TECH Migration abgeschlossen (46/46)
⚠️ ABER: Strukturelle Probleme verhindern saubere Implementation
```

---

## 🚨 STATUS UPDATE:

**Die harte Wahrheit:**
- Oberflächliche Bereinigung durchgeführt ✅
- Tieferliegende Probleme entdeckt ❌
- Tests die nicht helfen ❌
- Struktur die bei jeder Änderung bricht ❌

**Geschätzte Zeit für echte Bereinigung:** 3-4 Stunden

**ERST DANN:** FC-008 Security Implementation