# 🚀 CLAUDE QUICK START GUIDE - Nach der Übergabe

**Zweck:** Schnelle Orientierung für Claude nach jeder Übergabe  
**Zeit:** 2-3 Minuten bis zur vollen Arbeitsfähigkeit  

## 📋 1. SOFORT-CHECK (30 Sekunden)

```bash
# Wo bin ich gerade?
cat docs/features/MASTER/FEATURE_OVERVIEW.md | grep "🟡\|🔄\|In Arbeit"

# Was ist der aktuelle Fokus?
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | sed -n '15,35p'

# Welche TODOs sind offen?
cat .current-todos.md | grep -c "pending"
```

## 🗺️ 2. ORIENTIERUNG IM FEATURE-SYSTEM

### Die 3 Haupt-Ordner:

#### 📂 ACTIVE/ - Hier arbeiten wir!
```
ACTIVE/
├── 01_security_foundation/    # FC-008 (85% - Tests fehlen)
│   ├── README.md             # Feature-Übersicht
│   └── WORK_STATUS.md ⭐     # HIER steht was zu tun ist!
├── 02_opportunity_pipeline/   # M4 (0% - Ready to Start)
├── 03_calculator_modal/       # M8 (0% - Ready to Start)
└── 04_permissions_system/     # FC-009 (0% - Tech Solution fertig)
    └── TECHNICAL_SOLUTION.md # Zur Diskussion bereit
```

#### 📂 PLANNED/ - Was als nächstes kommt
- Verkäuferschutz (FC-004) - KRITISCH, wartet auf FC-009
- E-Mail Integration (FC-003)
- Chef-Dashboard (FC-007)

#### 📂 COMPLETED/ - Referenz & Historie
- Legacy Migration ✅
- FC-008 kommt hier hin WENN Tests grün sind

## 🎯 3. WO FINDE ICH WAS?

### Bei der Frage "Was soll ich tun?"
1. **Master Overview** → Zeigt alle Features + Status
   ```bash
   cat docs/features/MASTER/FEATURE_OVERVIEW.md
   ```

2. **WORK_STATUS.md** des aktiven Moduls → Detaillierte TODOs
   ```bash
   cat docs/features/ACTIVE/*/WORK_STATUS.md
   ```

3. **V5 Master Plan** → Strategische Richtung
   ```bash
   cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | head -40
   ```

### Bei der Frage "Wie war das nochmal?"
- **COMPLETED/** → Beispiele von fertigen Features
- **TECHNICAL_SOLUTION.md** → Technische Konzepte
- **README.md** in jedem Modul → Quick Context

### Bei der Frage "Was kommt als nächstes?"
- **PLANNED/README.md** → Priorisierte Feature-Liste
- **Feature Dependencies Graph** → Was blockiert was
- **NEXT_STEP.md** → Konkrete nächste Aktion

## 🔄 4. TYPISCHE ARBEITSABLÄUFE

### A) Feature weitermachen (z.B. FC-008)
```bash
# 1. Status checken
cat docs/features/ACTIVE/01_security_foundation/WORK_STATUS.md

# 2. Tests ausführen
cd backend && ./mvnw test -Dtest=SecurityContextProviderIntegrationTest

# 3. Bei Erfolg: Status updaten
# Edit WORK_STATUS.md → Progress erhöhen
```

### B) Neues Feature starten (z.B. M4)
```bash
# 1. README lesen für Context
cat docs/features/ACTIVE/02_opportunity_pipeline/README.md

# 2. WORK_STATUS.md erstellen
# Template von FC-008 kopieren und anpassen

# 3. Mit ersten TODOs beginnen
```

### C) Technical Review (z.B. FC-009)
```bash
# 1. Technical Solution öffnen
cat docs/features/ACTIVE/04_permissions_system/TECHNICAL_SOLUTION.md

# 2. Offene Fragen identifizieren
# 3. Mit Jörg diskutieren
```

## 💡 5. GOLDENE REGELN

1. **WORK_STATUS.md ist deine Wahrheit** - Dort steht IMMER der aktuelle Stand
2. **Master Overview für Überblick** - Zeigt alle Features auf einen Blick
3. **Quick Context lesen** - Jedes Modul hat einen für dich!
4. **TODOs sofort aktualisieren** - TodoWrite nach jeder Änderung
5. **Bei Unklarheit: FRAGEN** - Besser einmal zu viel als falsche Richtung

## 🚨 6. NOTFALL-KOMMANDOS

```bash
# Services nicht erreichbar?
./scripts/diagnose-problems.sh

# Git durcheinander?
git status && git log --oneline -5

# Tests schlagen fehl?
cd backend && ./mvnw clean test

# Frontend build error?
cd frontend && npm run build
```

## 📞 7. WEN FRAGE ICH BEI WAS?

- **Technische Blocker:** Direkt analysieren und lösen
- **Business Logic:** In WORK_STATUS.md → Offene Fragen
- **Architektur-Entscheidungen:** Mit Jörg besprechen
- **Performance-Probleme:** Erst messen, dann optimieren

---

**⚡ Pro-Tipp:** Starte IMMER mit dem Master Overview - das spart 80% der Orientierungszeit!