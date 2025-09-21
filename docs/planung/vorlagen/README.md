# 📝 Template-Bibliothek - FreshPlan Documentation

**Erstellt:** 2025-09-17
**Status:** ✅ Konsolidiert & Aufgeräumt
**Zweck:** Einheitliche Dokumentationsstandards für alle Features

## 🎯 Template Overview

### **Foundation Templates** (Für alle Features)
| Template | Zweck | Wann verwenden | Status |
|----------|-------|----------------|--------|
| **FEATURE_TEMPLATE.md** | Vollständiges technisches Konzept | Neue Features (FC-XXX) | ✅ Konsolidiert |
| **API_SPEC_TEMPLATE.md** | REST API Dokumentation | Backend API Design | ✅ Neu erstellt |
| **COMPONENT_SPEC_TEMPLATE.md** | UI Component Dokumentation | React Components | ✅ Neu erstellt |
| **TEST_PLAN_TEMPLATE.md** | Umfassende Test-Strategie | Feature Testing | ✅ Neu erstellt |

### **Workflow Templates** (Für Claude-Sessions)
| Template | Zweck | Wann verwenden | Status |
|----------|-------|----------------|--------|
| **HANDOVER_TEMPLATE_IMPROVED.md** | Session-Übergaben | Ende jeder Claude-Session | ✅ Bewährt |
| **CHANGE_LOG_TEMPLATE.md** | Änderungs-Dokumentation | Größere Code-Änderungen | ✅ Bewährt |
| **PROBLEM_ANALYSIS_TEMPLATE.md** | Problem-Analyse | Debugging & Troubleshooting | ✅ Bewährt |

### **Zusätzliche Templates**
| Template | Zweck | Wann verwenden | Status |
|----------|-------|----------------|--------|
| **HANDOVER_TEMPLATE.md** | Basis-Übergabe | Einfache Sessions | ✅ Legacy |
| **HANDOVER_ADAPTIVE_TEMPLATE.md** | Adaptive Übergabe | Spezielle Situationen | ✅ Legacy |

## 🚀 **Haupt-Template: FEATURE_TEMPLATE.md**

**Das FEATURE_TEMPLATE.md ist unser GOLDSTANDARD für alle neuen Features!**

### Was macht es besonders:
- ✅ **Konsolidiert:** Beste Aspekte aus altem TECH_CONCEPT + modernen Standards
- ✅ **Vollständig:** Business Context + Technical Implementation + Testing
- ✅ **Enterprise-Ready:** Performance, Security, Accessibility berücksichtigt
- ✅ **Developer-Focused:** Code-Beispiele, konkrete Implementation-Guides
- ✅ **Theme-Compliant:** Strikte Einhaltung von FreshPlan Design System

### Struktur:
```markdown
1. Business Context (Problem, User Stories, Success Criteria)
2. Technische Architektur (System-Design, Components)
3. Backend-Implementierung (APIs, Database, Services)
4. Frontend-Implementierung (React, State Management, Theme)
5. Testing Strategy (Unit, Integration, E2E)
6. Implementation Roadmap (Phasen, Zeitschätzung)
7. Performance & Security (Optimization, Risk Management)
8. Decision Log (Architecture Decisions Records)
```

## 📋 **Template-Verwendung Guidelines**

### **Für neue Features (FC-XXX):**
1. **Start:** Kopiere `FEATURE_TEMPLATE.md` → `FC-XXX-[name].md`
2. **Anpassen:** Alle [Platzhalter] mit konkreten Werten füllen
3. **Review:** Team-Review vor Implementation-Start
4. **Update:** Status kontinuierlich aktualisieren

### **Für API-Design:**
1. **Start:** Kopiere `API_SPEC_TEMPLATE.md` → `API-[resource].md`
2. **Design:** Endpoints, DTOs, Error Handling definieren
3. **Contract:** Frontend-Backend Agreement
4. **Testing:** Test Cases aus Template ableiten

### **Für UI-Components:**
1. **Start:** Kopiere `COMPONENT_SPEC_TEMPLATE.md` → `[ComponentName].md`
2. **Design:** Props, Variants, Theme-Integration
3. **Storybook:** Stories aus Template entwickeln
4. **Accessibility:** A11y Requirements checken

### **Für Testing:**
1. **Start:** Kopiere `TEST_PLAN_TEMPLATE.md` → `TEST-FC-XXX.md`
2. **Strategy:** Test Cases definieren
3. **Automation:** CI/CD Integration planen
4. **Coverage:** Ziele festlegen und monitoren

## 🎨 **Design System Integration**

### **KRITISCH: Theme-Compliance**
Alle Templates betonen die strikte Verwendung von Theme-Variablen:

```typescript
// ✅ RICHTIG - Theme verwenden
const styles = {
  primary: theme.palette.primary.main,      // #004F7B
  success: theme.palette.success.main,      // #94C456
  text: theme.palette.text.primary,
};

// ❌ FALSCH - Hardcoded Farben
const styles = {
  primary: '#004F7B',
  success: '#94C456',
};
```

### **MUI Integration:**
- Alle Components nutzen MUI v7.3.1
- Theme Variables für Konsistenz
- Responsive Design mit Breakpoints
- Accessibility Standards (WCAG 2.1 AA)

## 🔄 **Template Evolution**

### **Version History:**
- **v1.0 (July 2025):** Original Templates erstellt
- **v2.0 (September 2025):** Konsolidierung + Foundation-First Approach
- **Aktuell:** Alle Templates auf Enterprise-Standards

### **Continuous Improvement:**
- Templates werden basierend auf Team-Feedback verbessert
- Neue Best Practices fließen ein
- Legacy-Templates werden archiviert

## 📁 **Template-Archiv**

### **Archivierte Templates:**
```
docs/planung/archiv/alte-templates/
├── TECH_CONCEPT_TEMPLATE_ORIGINAL.md  # Alte Version
└── FEATURE_TEMPLATE_NEW.md            # Erste neue Version
```

**Warum archiviert:**
- Ersetzt durch bessere konsolidierte Versionen
- Historische Referenz für Migration
- Prevent Template-Proliferation

## 🎯 **Best Practices**

### **Template-Nutzung:**
1. **Immer aktuellste Version** aus `/docs/vorlagen/` verwenden
2. **Alle Platzhalter** mit konkreten Werten ersetzen
3. **Status kontinuierlich** aktualisieren
4. **Code-Beispiele** an Projekt anpassen

### **Template-Pflege:**
1. **Feedback sammeln** nach jeder Feature-Implementation
2. **Templates erweitern** wenn neue Patterns entstehen
3. **Veraltete Templates** archivieren
4. **Version-History** dokumentieren

### **Quality Gates:**
- [ ] Alle Platzhalter ersetzt
- [ ] Status-Updates gepflegt
- [ ] Code-Beispiele getestet
- [ ] Team-Review durchgeführt

## 🔗 **Related Documentation**

- [Foundation Documentation](../grundlagen/)
- [Feature Documentation](../features-neu/)
- [Architecture Decision Records](../grundlagen/architektur/)
- [Development Guidelines](../../CLAUDE.md)

---

**📋 Template-Bibliothek erstellt:** 2025-09-17
**👨‍💻 Maintainer:** Claude + Jörg Streeck
**🎯 Ziel:** Konsistente, hochwertige Dokumentation für alle FreshPlan Features

**💡 Diese Templates sind das Rückgrat unserer Development Excellence!**