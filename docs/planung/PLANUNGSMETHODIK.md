# 📋 Planungsmethodik - Claude-Optimierte Dokumentation

**Erstellt:** 2025-09-18
**Status:** ✅ Living Document - kontinuierlich erweitert
**Zweck:** Standards für Claude-freundliche Planungsdokumentation
**Owner:** Development Team + Claude

## 🧠 **QUICK REFERENCE FÜR CLAUDE**

### **Sofort-Checkliste bei neuen Plänen:**
- [ ] **Länge:** Max 300-400 Zeilen pro Dokument
- [ ] **Executive Summary:** 3-4 Sätze What/Why/When/How
- [ ] **Atomare Struktur:** Ein Plan = Ein klarer Scope
- [ ] **Cross-Links:** Mindestens 3 Verweise zu Related Docs
- [ ] **Actionables:** Konkrete, messbare Schritte
- [ ] **Claude Handover:** Section für nächsten Claude

### **Standard-Template verwenden:**
```markdown
# [PLAN_NAME] - [TYPE] Plan

**📊 Plan Status:** 🔵 Draft / 🟡 Review / ✅ Approved / 🔄 In Progress
**🎯 Owner:** [Team/Person]
**⏱️ Timeline:** [Start] → [End]
**🔧 Effort:** [XS/S/M/L/XL]

## 🎯 Executive Summary (für Claude)
[3-4 sentences: What, Why, When, How]

## 📋 Context & Dependencies
### Current State: [max 5 bullets]
### Target State: [max 5 bullets]
### Dependencies: → [Links]

## 🛠️ Implementation Phases [2-4 phases max]
## ✅ Success Metrics [3-5 measurable outcomes]
## 🔗 Related Documentation [Links]
## 🤖 Claude Handover Section
```

## 📊 **CLAUDE-OPTIMIERTE STANDARDS**

### **1. Dokument-Architektur Prinzipien:**

#### **Atomarität > Vollständigkeit:**
```yaml
✅ RICHTIG:
  - Ein Dokument = Ein klarer Scope
  - LAYOUT_MIGRATION_PLAN.md (Focus: MainLayoutV2→V3)
  - SMARTLAYOUT_IMPLEMENTATION.md (Focus: Content Detection)

❌ FALSCH:
  - FRONTEND_COMPLETE_ROADMAP.md (zu viele Topics)
  - EVERYTHING_UI_RELATED.md (unklarer Scope)
```

#### **Optimale Dokument-Länge:**
```yaml
Ideal für Claude-Processing:
  - Executive Summary: 30-50 Zeilen
  - Main Content: 200-300 Zeilen
  - Related Links: 20-30 Zeilen
  - Gesamt: 300-400 Zeilen maximum

Kritische Grenzen:
  - >500 Zeilen: Claude verliert Context
  - <100 Zeilen: Zu wenig Detail für Implementation
  - Monolithen: Aufteilen in Phasen oder Bereiche
```

### **2. Strukturelle Patterns:**

#### **Executive Summary Pattern:**
```markdown
## 🎯 Executive Summary (für Claude)
**Mission:** [One sentence goal]
**Problem:** [What we're solving]
**Solution:** [How we solve it]
**Timeline:** [When it happens]
**Impact:** [What changes]

**Quick Context:** [2-3 sentences for immediate understanding]
```

#### **Dependency Chain Pattern:**
```markdown
## 🔗 Plan Dependencies
**Depends on:**
- ✅ [Completed Prerequisite](link)
- 🔄 [In Progress Requirement](link)
- 📋 [Planned Dependency](link)

**Enables:**
- 📋 [Follow-up Plan A](link)
- 📋 [Follow-up Plan B](link)

**Parallel Activities:**
- 🔄 [Concurrent Plan](link)
```

#### **Phase Structure Pattern:**
```markdown
## 🛠️ Implementation Phases

### Phase 1: [Name] (Week 1-2)
**Goal:** [Clear objective]
**Actions:**
- [ ] Specific task 1 with success criteria
- [ ] Specific task 2 with success criteria

**Code Changes:**
```typescript
// Example implementation
```

**Success Criteria:**
- Measurable outcome 1
- Measurable outcome 2

**Next:** → [Phase 2 Section](#phase-2) or [Related Plan](link)
```

### **3. Cross-Reference System:**

#### **Linking Standards:**
```markdown
## 📚 Technical References
**Foundation Knowledge:**
- **Design Standards:** → [DESIGN_SYSTEM.md](grundlagen/DESIGN_SYSTEM.md)
- **API Standards:** → [API_STANDARDS.md](grundlagen/API_STANDARDS.md)

**Implementation Details:**
- **Code Location:** `frontend/src/components/layout/`
- **Config Files:** `vite.config.ts`, `tsconfig.json`
- **Test Files:** `layout.test.tsx`, `smartlayout.test.tsx`

**Related Plans:**
- **Dependencies:** → [Prerequisite Plan](link)
- **Follow-ups:** → [Next Phase Plan](link)
- **Alternatives:** → [Alternative Approach](link)
```

#### **Master Index Integration:**
```markdown
Jeder Bereich hat einen Master Index:
- 🏗️ [Infrastructure Master](infrastruktur/00_MASTER_INDEX.md)
- 🎯 [Features Master](features/00_MASTER_INDEX.md)
- 🏛️ [Architecture Master](architektur/00_MASTER_INDEX.md)
```

### **4. Actionable Content Standards:**

#### **Konkrete vs. Vage Actions:**
```yaml
✅ KONKRET (Claude kann umsetzen):
- "Reduce API response time from 500ms to <100ms for GET /customers"
- "Migrate 15 components from MainLayoutV2 to MainLayoutV3"
- "Implement SmartLayout with 4 content type detections: Table, Form, Text, Dashboard"

❌ VAGE (Claude kann nicht umsetzen):
- "Optimize performance"
- "Improve user experience"
- "Make the system better"
```

#### **Success Criteria Format:**
```markdown
## ✅ Success Metrics
**Quantitative:**
- API Response Time: <100ms P95 (currently 500ms)
- Bundle Size: <500KB (currently 750KB)
- Test Coverage: >90% (currently 67%)

**Qualitative:**
- All new pages use MainLayoutV3
- Developers report easier layout decisions
- No manual width configurations needed

**Timeline:**
- Phase 1: Week 1-2
- Phase 2: Week 3-4
- Complete: End of Month
```

## 🤖 **CLAUDE HANDOVER OPTIMIZATION**

### **Handover Section Standard:**
```markdown
## 🤖 Claude Handover Section
**Für nächsten Claude:**

**Aktueller Stand:**
[2-3 Sätze: Was wurde erreicht, was ist der Status]

**Nächster konkreter Schritt:**
[Spezifische Aktion die als nächstes gemacht werden soll]

**Wichtige Dateien für Context:**
- `[file1.ts]` - [Kurze Beschreibung]
- `[file2.md]` - [Kurze Beschreibung]

**Offene Entscheidungen:**
- [Frage 1 die geklärt werden muss]
- [Frage 2 die geklärt werden muss]

**Kontext-Links:**
- **Grundlagen:** → [Relevant Foundation Doc](link)
- **Dependencies:** → [Required Plans](link)
```

### **Context Preservation Patterns:**
```markdown
## 📝 Context für Kontinuität
**Was bereits analysiert wurde:**
- [Previous finding 1]
- [Previous finding 2]

**Getroffene Entscheidungen:**
- [Decision 1] - Grund: [Why]
- [Decision 2] - Grund: [Why]

**Verworfene Alternativen:**
- [Alternative A] - Warum nicht: [Reason]
- [Alternative B] - Warum nicht: [Reason]
```

## 🚫 **ANTI-PATTERNS VERMEIDEN**

### **Dokument-Strukturelle Anti-Patterns:**
```yaml
❌ MONOLITHEN:
- Dokumente >500 Zeilen
- "Complete Guide to Everything"
- Verschachtelte Unterpunkte >3 Ebenen

❌ VAGE REFERENZEN:
- "Siehe Anhang A.3.2" ohne Link
- "Wie besprochen" ohne Context
- "Bekanntes Problem" ohne Details

❌ CONTEXT-LOSE PLÄNE:
- Keine Dependencies erwähnt
- Fehlendes "Warum"
- Keine Success Criteria

❌ CLAUDE-UNFRIENDLY:
- Circular References zwischen Docs
- Fehlende Executive Summaries
- Keine konkreten Next Steps
```

### **Inhaltliche Anti-Patterns:**
```yaml
❌ IMPLEMENTIERUNGS-DETAILS IN PLÄNEN:
- Vollständige Code-Listings
- Debug-Sessions-Dokumentation
- Spezifische Bugs/Fixes

❌ BUSINESS-LOGIC IN TECHNISCHEN PLÄNEN:
- Rabatt-Berechnungslogik in Layout-Plan
- Feature-Requirements in Infrastruktur-Plan
- User Stories in Technical Architecture

❌ TIMELINE-PROBLEME:
- Unklare oder fehlende Timelines
- Abhängigkeiten ohne zeitliche Planung
- Unrealistische Effort-Schätzungen
```

## 📈 **LESSONS LEARNED (Evolution Log)**

### **2025-09-18: Gründung der Methodologie**
**Erkenntnisse aus der Grundlagen-Konsolidierung:**
- **Problem:** Planungsabschnitte waren in Foundation-Docs vermischt
- **Lösung:** Separate Infrastruktur-Planung mit atomaren Dokumenten
- **Learning:** Foundation = Standards, Planung = Roadmaps trennen

**Was funktioniert gut:**
- Executive Summaries mit 3-4 Sätzen
- Konkrete Success Criteria mit Zahlen
- Cross-References zwischen related Docs

**Was verbessert werden muss:**
- Noch zu wenig atomare Struktur
- Master Indices fehlen noch
- Claude Handover Sections standardisieren

### **Nächste Iteration (geplant):**
- [ ] Master Index für jeden Planungsbereich erstellen
- [ ] Template-Compliance aller bestehenden Pläne prüfen
- [ ] Claude Handover Sections zu allen aktiven Plänen hinzufügen

## 🔧 **PRAKTISCHE UMSETZUNG**

### **Neue Pläne erstellen:**
1. **Template kopieren** aus [TEMPLATE_STANDARDS.md](vorlagen/)
2. **Scope definieren:** Einer klaren Zielsetzung
3. **Dependencies mappen:** Was wird vorausgesetzt?
4. **Phasen strukturieren:** 2-4 konkrete Phasen
5. **Cross-Links setzen:** Minimum 3 Verweise
6. **Claude Handover:** Vorbereiten für Übergaben

### **Bestehende Pläne verbessern:**
1. **Länge prüfen:** >400 Zeilen? → Aufteilen
2. **Executive Summary:** Fehlt? → Hinzufügen
3. **Actionables:** Vage? → Konkretisieren
4. **Links:** Broken? → Reparieren
5. **Handover:** Fehlt? → Ergänzen

### **Master Indices pflegen:**
1. **Status tracking:** Alle Pläne aktuell?
2. **Dependency chain:** Logische Reihenfolge?
3. **Timeline:** Realistische Schätzungen?
4. **Ownership:** Wer ist verantwortlich?

## 🎯 **QUALITÄTS-CHECKLISTE**

### **Vor Publish eines Plans:**
- [ ] **Länge:** 300-400 Zeilen maximum
- [ ] **Executive Summary:** 3-4 Sätze What/Why/When/How
- [ ] **Dependencies:** Klar dokumentiert mit Links
- [ ] **Phases:** 2-4 konkrete Phasen mit Actions
- [ ] **Success Criteria:** Messbare Outcomes
- [ ] **Cross-Links:** Minimum 3 Related Docs
- [ ] **Claude Handover:** Nächster Schritt klar
- [ ] **Technical References:** Code/Config Locations
- [ ] **Timeline:** Realistische Schätzungen
- [ ] **Owner:** Wer ist verantwortlich

### **Regelmäßige Reviews:**
- **Monatlich:** Status aller aktiven Pläne aktualisieren
- **Quarterly:** Lessons Learned ergänzen
- **Bei Problemen:** Anti-Patterns identifizieren und dokumentieren
- **Bei Übergaben:** Claude Handover Sections aktualisieren

## 📂 **DOKUMENTATIONS-STRATEGIE: Hybrid-Ansatz für Planung vs. Zukunft**

### **Problem:** Manche Features sind sofort planbar, andere visionär
### **Lösung:** 4-Ebenen-Strategie mit klarer Kategorisierung

#### **🆕 NEUE REGEL: Planung vs. Zukunft trennen (seit 18.09.2025)**

### **Kategorisierung-Matrix:**
```yaml
Sofort planbar (technical-concept.md):
  - ✅ Technisch machbar (4-8 Wochen Aufwand)
  - ✅ Business-Requirements klar
  - ✅ Dependencies verfügbar
  - ✅ Ressourcen allokierbar

Zukunftsvision (/zukunft/):
  - 🔮 Business-Logik unklar
  - 🔮 Dependencies unsicher (andere Module erst)
  - 🔮 Aufwand >8 Wochen oder unschätzbar
  - 🔮 "Nice-to-have" ohne Business-Pressure
```

### **Praktisches Beispiel - Cockpit:**
```yaml
Technical Concept:
  - ChannelType + Multi-Channel-Filter (✅ 2-3 Wochen)
  - ROI-Calculator-Modal (✅ 2 Wochen)
  - FreshFoodz-Header-KPIs (✅ 1 Woche)

Zukunft-Verzeichnis:
  - Seasonal Opportunities (🔮 Business-Logik unklar)
  - Advanced ROI Features (🔮 nach MVP evaluieren)
  - Partner Performance Tracking (🔮 Dependencies fehlen)
```

## 📂 **DOKUMENTATIONS-STRATEGIE: 4-Ebenen-System**

### **Problem:** Ohne klare Regeln entstehen wieder zu viele überlappende Dokumente
### **Lösung:** 4-Ebenen-Strategie mit minimaler Redundanz

### **Ebene 1: CRM Master Plan V5 (Strategisch & Kompakt)**
**Zweck:** Überblick, Timeline, Business-Ziele
**Pro Feature max. 50-100 Zeilen:**
```markdown
## 01 Mein Cockpit
**Status:** 🔄 In Diskussion
**Ziel:** Zentrale Verkäufer-Schaltzentrale
**Timeline:** Q4 2025
**Dependencies:** FC-005, M8, FC-013
**Business Impact:** 3x schnellere Lead-Bearbeitung
**Nächster Schritt:** Technical Concept nach AI-Diskussion
```

### **Ebene 2: Duale Technical Concepts**
**2a) Claude-Optimiert:** `technical-concept.md`
- 300-400 Zeilen max, strukturiert nach Template
- Executive Summary, Implementation Phases
- Cross-References, Claude Handover

**2b) Menschen-Lesbar:** `implementation-guide.md`
- Ausführliche Erklärungen, Beispiele
- Begründungen für Architektur-Entscheidungen
- User-Stories, Workflow-Beschreibungen

### **Ebene 3: Diskussions-Archiv + Zukunft**
**Zweck:** Entscheidungshistorie + visionäre Features dokumentieren
```
01_mein-cockpit/
├── technical-concept.md          ← Claude-optimiert (✅ Sofort planbar)
├── implementation-guide.md       ← Menschenlesbar (optional)
├── diskussionen/                 ← Entscheidungshistorie
│   ├── 2025-09-18_ai-consultation.md
│   ├── 2025-09-18_freshfoodz-gap-analyse.md
│   └── decisions-summary.md
├── zukunft/                      ← 🔮 Visionäre Features
│   ├── seasonal-opportunities.md
│   ├── advanced-roi-features.md
│   └── partner-performance-tracking.md
└── README.md                     ← Überblick + Navigation
```

#### **🆕 Zukunft-Verzeichnis Regeln:**
- **Zweck:** Features mit unklaren Business-Requirements
- **Kriterien:** >8 Wochen Aufwand ODER Dependencies fehlen
- **Review:** Quarterly überprüfen ob → technical-concept.md
- **Format:** Gleich wie technical-concept.md aber mit 🔮 Status

### **Update-Regeln:**
- **Master Plan:** Status + Timeline bei Sprint-Ende
- **Technical Concepts:** Bei Architektur-Änderungen
- **Diskussions-Archiv:** Nach wichtigen Entscheidungen
- **Timeline-Tracking:** Im Master Plan (Milestones-Sektion)

### **Archivierungs-Strategie:**
**Ein einziges Archiv:** `/docs/planung/archiv/`
- **Veraltete Master Pläne** (z.B. CRM_COMPLETE_MASTER_PLAN_V4_archived.md)
- **Backup-Dateien-Pollution** (automatische Backups)
- **Historische Features** (features-historisch/)
- **Alte Templates** (nicht mehr verwendete Vorlagen)
- **NIEMALS:** Aktive Technical Concepts oder Infrastructure-Pläne

**Anti-Pattern vermeiden:**
- ❌ Mehrere Archive (archive, archiv, backup, alt)
- ❌ Aktive Dokumente im Archiv
- ❌ Archiv als "Zwischenlager" nutzen

### **Dokumentations-KPIs:**
- **Redundanz-Vermeidung:** Max 3 Hauptdokumente pro Feature
- **Update-Geschwindigkeit:** Master Plan immer <7 Tage aktuell
- **Verständlichkeit:** Implementation Guides auch für neue Entwickler

---

**📋 Dieses Dokument ist ein Living Standard und wird kontinuierlich verbessert basierend auf praktischen Erfahrungen mit Claude-optimierter Planungsdokumentation.**

**🔄 Letzte Aktualisierung:** 2025-09-18 - 3-Ebenen-Dokumentations-Strategie etabliert
**🎯 Nächste Review:** Bei der ersten größeren Planung
**📝 Feedback:** Ergänzungen in diesem Dokument direkt vornehmen