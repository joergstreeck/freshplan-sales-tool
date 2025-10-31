# 📋 Sprint 2.1.7.2 - Bereinigungs- und Aktualisierungsplan

**📅 Erstellt:** 2025-10-31
**🎯 Ziel:** Dokumentenbereinigung, Konsistenz, keine Redundanzen
**👤 Autor:** Claude Code
**📍 Sprint:** 2.1.7.2 - Customer Management + Xentral Integration

---

## 🎯 ZIEL DER BEREINIGUNG

**Primärziel:**
> Alle Sprint 2.1.7.2 Dokumente aktualisieren, in `planung/artefakte/sprint-2.1.7.2/` konsolidieren und im Hauptdokument referenzieren. **Keine Inkonsistenzen und Redundanzen** mehr!

**Sekundärziele:**
1. TRIGGER_SPRINT_2_1_7_2.md als zentrales Einstiegsdokument etablieren
2. Alle 8 Spezifikationsdokumente in Unterordner verschieben
3. Cross-Referenzen zwischen Dokumenten vervollständigen
4. Status, Datum, Versionsnummern aktualisieren
5. Redundanzen eliminieren

---

## 📊 AUSGANGSLAGE (Ist-Zustand)

### Dokumente in `/docs/planung/` (ROOT - müssen verschoben werden)

| Datei | Größe | Status | Aktion |
|-------|-------|--------|--------|
| `TRIGGER_SPRINT_2_1_7_2.md` | ~40 KB | HAUPT-DOKUMENT | ✏️ Aktualisieren (bleibt in ROOT) |
| `LEAD_SEARCH_IMPLEMENTATION.md` | ~15 KB | Spezifikation | 📦 Verschieben + Aktualisieren |
| `SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md` | ~20 KB | Spezifikation | 📦 Verschieben + Aktualisieren |
| `SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md` | ~30 KB (897 Zeilen) | DEPRECATED | 📦 Verschieben (keine Änderungen) |
| `TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md` | ~20 KB (674 Zeilen) | Architektur-Spec | 📦 Verschieben + Aktualisieren |

### Dokumente in `/docs/planung/artefakte/` (bereits im Ziel-Ordner - Aktualisierung)

| Datei | Größe | Status | Aktion |
|-------|-------|--------|--------|
| `SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md` | 12 KB (409 Zeilen) | Final Architecture | 📦 Verschieben in sprint-2.1.7.2/ + Aktualisieren |
| `SPEC_SPRINT_2_1_7_2_TECHNICAL.md` | 146 KB (2590+ Zeilen) | Technische Spec | 📦 Verschieben in sprint-2.1.7.2/ + Aktualisieren |
| `ISSUE_XENTRAL_CDI_2_1_7_2.md` | 4.8 KB (182 Zeilen) | Known Issue | 📦 Verschieben in sprint-2.1.7.2/ (keine Änderungen) |
| `SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md` | 19 KB (579 Zeilen) | Design Decisions | 📦 Verschieben in sprint-2.1.7.2/ + Aktualisieren |

### Zusätzlich erstellt

| Datei | Größe | Status | Aktion |
|-------|-------|--------|--------|
| `sprint-2.1.7.2-COMMIT-SUMMARY.md` | ~25 KB | Commit-Zusammenfassung | 📦 Verschieben + Aktualisieren |

**TOTAL:** 10 Dokumente (1 Haupt + 8 Spezifikationen + 1 Commit-Summary)

---

## 🔍 IDENTIFIZIERTE PROBLEME

### 1. Strukturprobleme

- **Fehlende Ordnerstruktur:** Dokumente verstreut über `planung/` und `planung/artefakte/`
- **Keine klare Hierarchie:** Hauptdokument vs. Spezifikationen nicht klar getrennt
- **Fehlende Cross-Referenzen:** Dokumente referenzieren sich nicht gegenseitig

### 2. Inhaltliche Probleme

- **Veraltete Statusangaben:** Einige Dokumente zeigen Status "IN PROGRESS" obwohl Sprint abgeschlossen ist
- **Inkonsistente Datumsangaben:** Manche Dokumente haben kein Update-Datum
- **Fehlende Kontext-Links:** Wichtige Architektur-Entscheidungen nicht verlinkt

### 3. Redundanzen

- **Deliverable-Beschreibungen:** D11 wird in 4 verschiedenen Dokumenten beschrieben
  - TRIGGER_SPRINT_2_1_7_2.md (Übersicht)
  - SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md (Funktional)
  - TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md (Architektur)
  - SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md (Final Architecture)

- **Lead → Customer Konversion:** In 3 Dokumenten beschrieben
  - SPEC_SPRINT_2_1_7_2_TECHNICAL.md (D1)
  - SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md (Feldmapping)
  - LEAD_SEARCH_IMPLEMENTATION.md (Konversions-Flow)

- **Xentral Integration:** In 4 Dokumenten beschrieben
  - SPEC_SPRINT_2_1_7_2_TECHNICAL.md (D2-D7)
  - SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md (Sales-Rep Mapping)
  - ISSUE_XENTRAL_CDI_2_1_7_2.md (CDI Problem)
  - TRIGGER_SPRINT_2_1_7_2.md (Übersicht)

### 4. Inkonsistenzen

- **Status-Angaben:** Sprint ist abgeschlossen, aber einige Dokumente zeigen "IN PROGRESS"
- **Versionsnummern:** Einige Dokumente haben keine Versionsnummer
- **Struktur:** Einige Dokumente verwenden "##", andere "###" für Hauptsektionen

---

## 📁 ZIEL-STRUKTUR (Soll-Zustand)

### Hierarchie nach Bereinigung

```
/docs/planung/
├── TRIGGER_SPRINT_2_1_7_2.md (HAUPT-DOKUMENT - referenziert alle anderen)
│
└── artefakte/
    └── sprint-2.1.7.2/
        ├── CLEANUP_PLAN.md (dieses Dokument)
        ├── sprint-2.1.7.2-COMMIT-SUMMARY.md (Commit-Analyse)
        │
        ├── LEAD_SEARCH_IMPLEMENTATION.md (D2.1 - Lead Search)
        ├── SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md (D11 - Funktional)
        ├── SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md (DEPRECATED - historisch)
        ├── TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md (D11 - Architektur)
        ├── SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md (D11 - Final Architecture)
        │
        ├── SPEC_SPRINT_2_1_7_2_TECHNICAL.md (Technische Spec - ALLE Deliverables)
        ├── SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md (Design Decisions)
        └── ISSUE_XENTRAL_CDI_2_1_7_2.md (Known Issue)
```

### Dokumenten-Hierarchie (Lesefluss)

**Einstieg:**
1. `TRIGGER_SPRINT_2_1_7_2.md` - Haupt-Einstiegsdokument

**Übersicht:**
2. `sprint-2.1.7.2-COMMIT-SUMMARY.md` - Was wurde gemacht? (95 Commits)
3. `SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md` - Warum so gemacht? (7 Design Decisions)

**Technische Details:**
4. `SPEC_SPRINT_2_1_7_2_TECHNICAL.md` - Wie implementiert? (10 Deliverables)

**Spezielle Themen:**
5. `LEAD_SEARCH_IMPLEMENTATION.md` - Lead Search Details
6. `TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md` - D11 Architektur-Konzept
7. `SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md` - D11 Final Architecture
8. `SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md` - D11 Funktional

**Historisch/Issues:**
9. `SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md` - DEPRECATED (nicht verwenden)
10. `ISSUE_XENTRAL_CDI_2_1_7_2.md` - Bekanntes CDI-Problem (nicht kritisch)

**Prozess:**
11. `CLEANUP_PLAN.md` - Dieser Bereinigungs-Plan

---

## 🔧 AKTIONSPLAN

### Phase 1: Dokumente verschieben (10 min)

**1.1 Verschieben aus `/docs/planung/` nach `artefakte/sprint-2.1.7.2/`:**

```bash
# Aus planung/ ROOT
mv docs/planung/LEAD_SEARCH_IMPLEMENTATION.md docs/planung/artefakte/sprint-2.1.7.2/
mv docs/planung/SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md docs/planung/artefakte/sprint-2.1.7.2/
mv docs/planung/SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md docs/planung/artefakte/sprint-2.1.7.2/
mv docs/planung/TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md docs/planung/artefakte/sprint-2.1.7.2/
mv docs/sprint-2.1.7.2-COMMIT-SUMMARY.md docs/planung/artefakte/sprint-2.1.7.2/
```

**1.2 Verschieben aus `/docs/planung/artefakte/` nach `artefakte/sprint-2.1.7.2/`:**

```bash
# Aus artefakte/ ROOT
mv docs/planung/artefakte/SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md docs/planung/artefakte/sprint-2.1.7.2/
mv docs/planung/artefakte/SPEC_SPRINT_2_1_7_2_TECHNICAL.md docs/planung/artefakte/sprint-2.1.7.2/
mv docs/planung/artefakte/ISSUE_XENTRAL_CDI_2_1_7_2.md docs/planung/artefakte/sprint-2.1.7.2/
mv docs/planung/artefakte/SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md docs/planung/artefakte/sprint-2.1.7.2/
```

**Ergebnis:** Alle 9 Dokumente (+ dieser Plan) liegen in `artefakte/sprint-2.1.7.2/`

---

### Phase 2: TRIGGER_SPRINT_2_1_7_2.md aktualisieren (20 min)

**Ziel:** Haupt-Dokument als zentrale Anlaufstelle etablieren

**Änderungen:**

1. **Status aktualisieren:**
   - Von "IN PROGRESS" auf "✅ COMPLETE" (Sprint ist abgeschlossen)
   - Datum auf 2025-10-31 aktualisieren

2. **Neue Sektion "📚 Dokumentation" hinzufügen:**

```markdown
## 📚 DOKUMENTATION

Alle detaillierten Spezifikationen liegen unter: `docs/planung/artefakte/sprint-2.1.7.2/`

### Übersicht & Kontext
- [📋 Commit-Zusammenfassung](./artefakte/sprint-2.1.7.2/sprint-2.1.7.2-COMMIT-SUMMARY.md) - Was wurde gemacht? (95 Commits, 472 Tests)
- [🎨 Design Decisions](./artefakte/sprint-2.1.7.2/SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md) - Warum so gemacht? (7 Entscheidungen)
- [🔧 Technische Spezifikation](./artefakte/sprint-2.1.7.2/SPEC_SPRINT_2_1_7_2_TECHNICAL.md) - Wie implementiert? (10 Deliverables, 2590+ Zeilen)

### Deliverable D11: Customer Detail Cockpit
- [🏗️ Architektur-Konzept](./artefakte/sprint-2.1.7.2/TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md) - Server-Driven Cards
- [✅ Final Architecture](./artefakte/sprint-2.1.7.2/SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md) - Verbindliche Implementierung
- [📝 Funktionale Spec](./artefakte/sprint-2.1.7.2/SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md) - UX und Features
- [❌ DEPRECATED: Progressive Disclosure](./artefakte/sprint-2.1.7.2/SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md) - Verworfene Architektur

### Weitere Spezifikationen
- [🔍 Lead Search Implementation](./artefakte/sprint-2.1.7.2/LEAD_SEARCH_IMPLEMENTATION.md) - D2.1 Suchfunktion

### Issues & Known Problems
- [⚠️ Xentral CDI Issue](./artefakte/sprint-2.1.7.2/ISSUE_XENTRAL_CDI_2_1_7_2.md) - Nicht kritisch für Sprint 2.1.7.4

### Meta
- [📋 Bereinigungs-Plan](./artefakte/sprint-2.1.7.2/CLEANUP_PLAN.md) - Dieser Dokumentations-Cleanup
```

3. **Deliverable-Sektionen aktualisieren:**
   - Bei D11 auf die spezifischen Architektur-Docs verweisen
   - Redundanzen entfernen (Details in Sub-Docs)

4. **Done-Criteria aktualisieren:**
   - [x] Alle Deliverables implementiert
   - [x] Tests ≥80% Coverage (472 Tests, 100% Pass)
   - [x] PR merged to main (Branch: feature/sprint-2-1-7-2-customer-xentral-integration)
   - [x] Browser-Test erfolgreich
   - [x] Dokumentation vollständig (10 Dokumente)

---

### Phase 3: Einzelne Dokumente aktualisieren (40 min)

**3.1 sprint-2.1.7.2-COMMIT-SUMMARY.md**
- ✅ Status: "COMPLETE" (bereits korrekt)
- ✏️ Pfad-Referenzen anpassen (neue Locations)
- ✏️ Link zu TRIGGER im Header hinzufügen

**3.2 SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md**
- ✅ Status: "APPROVED" (bereits korrekt)
- ✏️ Link zu TRIGGER und TECHNICAL im Header hinzufügen
- ✏️ Cross-Referenz zu COMMIT-SUMMARY (zeigt Implementierung)

**3.3 SPEC_SPRINT_2_1_7_2_TECHNICAL.md**
- ✅ Status: "COMPLETE" (vermutlich bereits korrekt)
- ✏️ Link zu TRIGGER und DESIGN_DECISIONS im Header hinzufügen
- ✏️ Redundante Architektur-Details entfernen (verweisen auf D11-Dokumente)

**3.4 LEAD_SEARCH_IMPLEMENTATION.md**
- ⚠️ Status: Prüfen (vermutlich "COMPLETE")
- ✏️ Link zu TRIGGER im Header hinzufügen
- ✏️ Cross-Referenz zu SPEC_SPRINT_2_1_7_2_TECHNICAL.md (D2.1)

**3.5 SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md**
- ⚠️ Status: Prüfen
- ✏️ Cross-Referenz zu:
  - TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md (Architektur)
  - SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md (Final Architecture)
- ✏️ Hinweis: "Siehe SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md für verbindliche Implementierung"

**3.6 TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md**
- ⚠️ Status: Prüfen
- ✏️ Cross-Referenz zu SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md
- ✏️ Hinweis: "Diese Architektur wurde in SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md finalisiert"

**3.7 SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md**
- ✅ Status: "APPROVED (Final Architecture)" (bereits korrekt)
- ✏️ Link zu TRIGGER im Header
- ✏️ Cross-Referenz zu SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md (verworfene Alternative)

**3.8 SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md**
- ✅ Status: "DEPRECATED" (bereits korrekt)
- ✏️ KEINE ÄNDERUNGEN (außer Header-Link zu TRIGGER)
- ✏️ Hinweis verstärken: "NICHT IMPLEMENTIEREN - siehe SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md"

**3.9 ISSUE_XENTRAL_CDI_2_1_7_2.md**
- ✅ Status: "OPEN" (korrekt)
- ✏️ KEINE ÄNDERUNGEN (außer Header-Link zu TRIGGER)

---

### Phase 4: Redundanzen eliminieren (20 min)

**Strategie:** "Single Source of Truth" pro Thema

**4.1 Deliverable D11 (Customer Detail Cockpit)**

**Problem:** 4 Dokumente beschreiben D11
- TRIGGER_SPRINT_2_1_7_2.md (Übersicht - 2-3 Absätze)
- SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md (Funktional - UX/Features)
- TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md (Architektur-Konzept)
- SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md (Final Architecture - VERBINDLICH)

**Lösung:**
- ✅ TRIGGER: NUR Übersicht (2-3 Absätze) + Links zu Details
- ✅ COCKPIT: NUR funktionale Beschreibung (UX, Features, User Stories)
- ✅ SERVER_DRIVEN_CARDS: NUR Architektur-Konzept (Problem → Solution)
- ✅ ARCHITECTURE: Final verbindliche Implementierung (Routes, Components, Backend)

**Maßnahme:**
- ✂️ In TRIGGER: Details entfernen, nur Links zu Sub-Docs
- ✂️ In COCKPIT: Architektur-Details entfernen, verweisen auf ARCHITECTURE
- ✂️ In SERVER_DRIVEN_CARDS: Finalisierungs-Hinweis zu ARCHITECTURE
- ✅ In ARCHITECTURE: Bleibt vollständig (ist SSOT für Implementierung)

**4.2 Lead → Customer Konversion**

**Problem:** 3 Dokumente beschreiben Konversion
- SPEC_SPRINT_2_1_7_2_TECHNICAL.md (D1 - Vollständig)
- SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md (Feldmapping - 12 Felder)
- LEAD_SEARCH_IMPLEMENTATION.md (Konversions-Flow)

**Lösung:**
- ✅ TECHNICAL: SSOT für Code-Implementierung (API, Backend, Frontend)
- ✅ ARCHITECTURE: SSOT für Feldmapping (welche Daten werden kopiert)
- ✅ LEAD_SEARCH: NUR Lead-spezifische Search-Logik

**Maßnahme:**
- ✅ Keine Änderung nötig (Dokumente beschreiben verschiedene Aspekte)
- ✏️ Cross-Referenzen hinzufügen

**4.3 Xentral Integration**

**Problem:** 4 Dokumente beschreiben Xentral
- SPEC_SPRINT_2_1_7_2_TECHNICAL.md (D2-D7 - Vollständig)
- SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md (Sales-Rep Mapping Decision)
- ISSUE_XENTRAL_CDI_2_1_7_2.md (CDI Problem)
- TRIGGER_SPRINT_2_1_7_2.md (Übersicht)

**Lösung:**
- ✅ TECHNICAL: SSOT für Implementierung
- ✅ DESIGN_DECISIONS: SSOT für Entscheidungs-Rationale
- ✅ ISSUE: SSOT für bekanntes Problem
- ✅ TRIGGER: NUR Übersicht

**Maßnahme:**
- ✅ Keine Änderung nötig (verschiedene Aspekte)
- ✏️ Cross-Referenzen hinzufügen

---

### Phase 5: Cross-Referenzen vervollständigen (20 min)

**Ziel:** Jedes Dokument weiß, wo es im Kontext steht

**Standard-Header für alle Sprint-2.1.7.2 Dokumente:**

```markdown
**📍 Navigation:**
- [🏠 Sprint 2.1.7.2 Haupt-Dokument](../../TRIGGER_SPRINT_2_1_7_2.md)
- [📋 Commit-Zusammenfassung](./sprint-2.1.7.2-COMMIT-SUMMARY.md)
- [🎨 Design Decisions](./SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md)
- [🔧 Technische Spec](./SPEC_SPRINT_2_1_7_2_TECHNICAL.md)
```

**Spezifische Cross-Referenzen:**

**D11-Dokumente untereinander:**
```markdown
**📍 D11 Customer Detail Cockpit:**
- [🏗️ Architektur-Konzept](./TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md)
- [✅ Final Architecture](./SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md)
- [📝 Funktionale Spec](./SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md)
- [❌ DEPRECATED](./SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md)
```

---

### Phase 6: Validierung & Finalisierung (15 min)

**6.1 Checkliste durchgehen:**

- [ ] Alle 9 Dokumente in `artefakte/sprint-2.1.7.2/` verschoben
- [ ] TRIGGER_SPRINT_2_1_7_2.md aktualisiert (Status, Dokumentations-Sektion)
- [ ] Alle Dokumente haben Standard-Header mit Cross-Referenzen
- [ ] Redundanzen eliminiert (SSOT-Prinzip)
- [ ] Status-Angaben aktualisiert (COMPLETE/APPROVED)
- [ ] Datumsangaben aktualisiert (2025-10-31)
- [ ] Keine toten Links

**6.2 Validierungsscript ausführen:**

```bash
# Prüfen: Alle Links funktionieren
cd docs/planung
find . -name "*.md" -exec grep -H "\[.*\](.*\.md)" {} \; | grep -v "http" | cut -d: -f2 | sed 's/.*(\(.*\)).*/\1/' | sort -u

# Prüfen: Keine Redundanzen (doppelte Überschriften)
cd docs/planung/artefakte/sprint-2.1.7.2
grep -h "^## " *.md | sort | uniq -c | sort -rn | head -20
```

**6.3 Bereinigungs-Dokumentation finalisieren:**
- [ ] Dieser Plan als `CLEANUP_PLAN.md` speichern
- [ ] In TRIGGER referenzieren

---

## ✅ ACCEPTANCE CRITERIA

**Dokumentations-Cleanup ist COMPLETE wenn:**

1. ✅ Alle 9 Dokumente in `artefakte/sprint-2.1.7.2/` liegen
2. ✅ TRIGGER_SPRINT_2_1_7_2.md hat Sektion "📚 DOKUMENTATION" mit allen Links
3. ✅ Jedes Dokument hat Standard-Header mit Cross-Referenzen
4. ✅ Keine Redundanzen mehr (SSOT-Prinzip umgesetzt)
5. ✅ Alle Status-Angaben korrekt (COMPLETE/APPROVED/DEPRECATED/OPEN)
6. ✅ Alle Datumsangaben aktualisiert
7. ✅ Keine toten Links (Validierung erfolgreich)
8. ✅ User findet keine Inkonsistenzen mehr

---

## 📊 ZEITPLAN

| Phase | Aufwand | Status |
|-------|---------|--------|
| 1. Dokumente verschieben | 10 min | ⏳ PENDING |
| 2. TRIGGER aktualisieren | 20 min | ⏳ PENDING |
| 3. Einzeldokumente aktualisieren | 40 min | ⏳ PENDING |
| 4. Redundanzen eliminieren | 20 min | ⏳ PENDING |
| 5. Cross-Referenzen vervollständigen | 20 min | ⏳ PENDING |
| 6. Validierung & Finalisierung | 15 min | ⏳ PENDING |
| **TOTAL** | **~2h** | ⏳ PENDING |

---

## 📝 NOTIZEN & LESSONS LEARNED

### Warum diese Bereinigung notwendig war:

1. **Dokumenten-Explosion:** 10 Dokumente für 1 Sprint (zu viele Details)
2. **Keine klare Struktur:** Dokumente verstreut
3. **Redundanzen:** Gleiche Informationen in mehreren Dokumenten
4. **Fehlende Cross-Referenzen:** Schwer navigierbar

### Best Practices für zukünftige Sprints:

1. **SSOT-Prinzip:** 1 Thema = 1 Dokument (mit klarer Abgrenzung)
2. **Hierarchie:** TRIGGER → TECHNICAL → SPEC (3 Level maximal)
3. **Cross-Referenzen:** Von Anfang an einbauen
4. **Ordnerstruktur:** Sofort unter `artefakte/sprint-X.Y.Z/` anlegen
5. **Commit-Summary:** Am Ende des Sprints erstellen
6. **Cleanup-Plan:** Bei >5 Dokumenten einen Plan schreiben

---

**🤖 Erstellt mit Claude Code - Dokumentations-Bereinigung 2025-10-31**
