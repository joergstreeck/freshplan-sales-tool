# 🚦 CODE QUALITY INITIATIVE - START HIER!

**⚠️ KRITISCH:** Dies sind sensible Code-Änderungen. Lies ALLES bevor du handelst!

## 🎯 Dein Einstiegspunkt

**Du bist hier:** `/docs/CODE_QUALITY_START_HERE.md`  
**Nächstes Dokument:** [`/docs/NEXT_STEP.md`](/docs/NEXT_STEP.md)

---

## 📋 SOFORT-CHECKLISTE für neuen Claude

### 1️⃣ System-Status prüfen (2 Min)
```bash
# Kopiere diese Befehle GENAU SO:
cd /Users/joergstreeck/freshplan-sales-tool
git branch --show-current
git status
git log --oneline -5
```

**Erwarteter Branch:** `feature/code-review-improvements` oder `main`

### 2️⃣ Services prüfen (1 Min)
```bash
# Sind alle Services up?
curl -s http://localhost:8080/api/ping || echo "❌ Backend nicht erreichbar"
curl -s http://localhost:5173 || echo "❌ Frontend nicht erreichbar"
```

### 3️⃣ PR Status prüfen
```bash
gh pr list --state open
# Suche nach: PR #83 - Code Quality Baseline
```

---

## 🗺️ VOLLSTÄNDIGE DOKUMENTEN-MAP

### Hierarchie der Dokumente:
```
📍 DU BIST HIER: CODE_QUALITY_START_HERE.md
│
├─→ 1. NEXT_STEP.md (Was JETZT zu tun ist)
│   └─→ Links zu: CODE_QUALITY_PR_ROADMAP.md
│
├─→ 2. CODE_QUALITY_PR_ROADMAP.md (Detaillierter Sprint-Plan)
│   ├─→ Links zu: ENTERPRISE_CODE_REVIEW_2025.md
│   ├─→ Links zu: TEST_STRATEGY_PER_PR.md
│   └─→ Links zu: CODE_QUALITY_UPDATE_ANALYSIS.md
│
├─→ 3. ENTERPRISE_CODE_REVIEW_2025.md (Alle Findings & Metriken)
│   └─→ Links zu: CODE_QUALITY_PR_ROADMAP.md
│
├─→ 4. TEST_STRATEGY_PER_PR.md (Test-Pläne für jede PR)
│   └─→ Links zu: CODE_QUALITY_PR_ROADMAP.md
│
└─→ 5. CODE_QUALITY_UPDATE_ANALYSIS.md (Status-Check)
    └─→ Links zu: CODE_QUALITY_PR_ROADMAP.md
```

---

## 🚨 KRITISCHE WARNUNGEN

### ⛔ NIEMALS:
- Code ändern ohne die Roadmap zu lesen
- Console.log komplett löschen (manche werden in Tests gebraucht!)
- TypeScript 'any' blind ersetzen (kann Tests brechen)
- Große Refactorings ohne Tests

### ✅ IMMER:
- Erst testen, dann committen
- Branch vor Änderungen prüfen
- Bei Unsicherheit: STOPPEN und fragen
- Backup vor großen Änderungen

---

## 📚 DOKUMENTEN-SCHNELLZUGRIFF

| Dokument | Zweck | Wann lesen |
|----------|-------|------------|
| [`NEXT_STEP.md`](/docs/NEXT_STEP.md) | Was JETZT tun | ZUERST |
| [`CODE_QUALITY_PR_ROADMAP.md`](/docs/features/CODE_QUALITY_PR_ROADMAP.md) | Sprint-Plan & PRs | VOR jeder PR |
| [`ENTERPRISE_CODE_REVIEW_2025.md`](/docs/features/ENTERPRISE_CODE_REVIEW_2025.md) | Findings & Metriken | Für Kontext |
| [`TEST_STRATEGY_PER_PR.md`](/docs/features/TEST_STRATEGY_PER_PR.md) | Test-Pläne | Bei PR-Erstellung |
| [`CODE_QUALITY_UPDATE_ANALYSIS.md`](/docs/features/CODE_QUALITY_UPDATE_ANALYSIS.md) | Was ist erledigt | Status-Check |

---

## 🎯 DEIN NÄCHSTER SCHRITT

**JETZT:** Öffne [`/docs/NEXT_STEP.md`](/docs/NEXT_STEP.md) und folge den Anweisungen dort!

---

**Navigation:**  
⬅️ Zurück zu: [`CLAUDE.md`](/docs/CLAUDE.md)  
➡️ Weiter zu: [`NEXT_STEP.md`](/docs/NEXT_STEP.md)