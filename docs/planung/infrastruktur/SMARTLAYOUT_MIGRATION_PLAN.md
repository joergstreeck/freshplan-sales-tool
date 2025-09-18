# SmartLayout Migration - Infrastructure Plan

**📊 Plan Status:** 🔄 In Progress
**🎯 Owner:** Development Team
**⏱️ Timeline:** Q4 2025 → Q1 2026
**🔧 Effort:** M

## 🎯 Executive Summary (für Claude)

**Mission:** Migration von MainLayoutV2 zu SmartLayout mit intelligenter Content-Breiten-Erkennung
**Problem:** Aktuelle Layouts sind statisch und erfordern manuelle Konfiguration für verschiedene Content-Typen
**Solution:** SmartLayout automatisiert Breiten-Entscheidungen basierend auf Content-Typ-Erkennung
**Timeline:** 3-4 Monate schrittweise Migration
**Impact:** 85% weniger manuelle Layout-Konfigurationen, bessere UX

**Quick Context:** Das SmartLayout-System erkennt automatisch Content-Typen (Tabellen, Formulare, Text) und wendet optimierte Breiten und Paddings an, ohne manuelle Entwickler-Intervention.

## 📋 Context & Dependencies

### Current State:
- MainLayoutV2 mit manueller maxWidth-Konfiguration
- Inkonsistente Breiten zwischen verschiedenen Content-Typen
- Entwickler müssen für jede neue Seite Layout-Entscheidungen treffen
- Performance-Impact durch suboptimale Breiten-Einstellungen
- 23 bestehende Komponenten benötigen Migration

### Target State:
- SmartLayout mit automatischer Content-Type-Detection
- Einheitliche UX durch intelligente Breiten-Algorithmen
- Zero-Config für Entwickler bei neuen Seiten
- Performance-optimierte Rendering-Pipeline
- 100% Komponenten verwenden SmartLayout

### Dependencies:
→ [Design System V2](../grundlagen/DESIGN_SYSTEM.md) - Content-Type-Definitionen
→ [Component Library Standards](../grundlagen/COMPONENT_LIBRARY.md) - React-Patterns
→ [Performance Standards](../grundlagen/PERFORMANCE_STANDARDS.md) - Rendering-Optimierungen

## 🛠️ Implementation Phases

### Phase 1: SmartLayout Core Implementation (Woche 1-4)
**Goal:** Basis SmartLayout Komponente mit Content-Type-Detection

**Actions:**
- [ ] Implement SmartLayout React Component mit TypeScript
- [ ] Create Content-Type-Detection-Logic für Table, Form, Text, Dashboard
- [ ] Develop Responsive-Breakpoint-System (xs/sm/md/lg/xl)
- [ ] Build Unit Tests mit 90%+ Coverage
- [ ] Create Storybook Stories für alle Content-Types

**Code Changes:**
```typescript
// SmartLayout Implementation
const SmartLayout: React.FC<SmartLayoutProps> = ({ children, className }) => {
  const contentType = useContentTypeDetection(children);
  const layoutConfig = getLayoutConfig(contentType);

  return (
    <Container maxWidth={layoutConfig.maxWidth} sx={layoutConfig.styles}>
      {children}
    </Container>
  );
};
```

**Success Criteria:**
- SmartLayout erkennt 4 Content-Types mit 95%+ Genauigkeit
- Performance-Benchmark ≤5ms für Type-Detection
- Zero Breaking Changes zu bestehenden Layouts

**Next:** → [Phase 2](#phase-2)

### Phase 2: Pilot Migration (Woche 5-8)
**Goal:** Migration von 5 kritischen Seiten als Proof-of-Concept

**Actions:**
- [ ] Migrate UsersPage von MainLayoutV2 → SmartLayout
- [ ] Migrate CustomerList mit Table-Content-Type
- [ ] Migrate SettingsPage mit Form-Content-Type
- [ ] Migrate Dashboard mit Mixed-Content-Type
- [ ] Migrate Calculator Modal mit Dialog-Content-Type

**Success Criteria:**
- Keine visuellen Regressionen (Pixel-Perfect-Tests)
- Performance-Verbesserung ≥10% bei Layout-Rendering
- Entwickler-Feedback: "Einfacher zu verwenden"

**Next:** → [Phase 3](#phase-3)

### Phase 3: Full Rollout (Woche 9-12)
**Goal:** Migration aller verbleibenden 18 Komponenten

**Actions:**
- [ ] Automated Migration mit Codemod-Script für einfache Cases
- [ ] Manual Migration für komplexe Layout-Komponenten
- [ ] Update Documentation und Guidelines
- [ ] Remove MainLayoutV2 Code und Dependencies
- [ ] Performance-Monitoring und Optimierungen

**Success Criteria:**
- 100% Komponenten verwenden SmartLayout
- MainLayoutV2 vollständig entfernt
- Bundle-Size-Reduktion ≥15KB (gzipped)
- Zero Production-Issues nach 2 Wochen

**Next:** → [Monitoring & Optimization Phase](#phase-4)

### Phase 4: Monitoring & Optimization (Woche 13-16)
**Goal:** Performance-Monitoring und kontinuierliche Optimierung

**Actions:**
- [ ] Setup Real User Monitoring (RUM) für Layout-Performance
- [ ] A/B Test Advanced Content-Type-Detection Features
- [ ] Optimize für Edge-Cases und komplexe Content-Mischungen
- [ ] Document Best Practices und Common Patterns
- [ ] Train Team auf neue SmartLayout-Patterns

**Success Criteria:**
- Layout-Performance-Scores ≥95 (Lighthouse)
- Entwickler-Onboarding-Zeit ≤30 Minuten
- Support-Tickets für Layout-Probleme ≤2 pro Monat

## ✅ Success Metrics

**Quantitative:**
- Content-Type-Detection-Accuracy: >95% (currently manual)
- Layout-Rendering-Time: <5ms P95 (currently ~12ms)
- Developer-Configuration-Time: <2min per page (currently ~15min)
- Bundle-Size-Reduction: >15KB gzipped
- Performance-Score: >95 Lighthouse (currently 82)

**Qualitative:**
- Alle neuen Seiten nutzen SmartLayout ohne manuelle Konfiguration
- Entwickler berichten von "intuitiverer" Layout-Entwicklung
- Design-Team bestätigt konsistentere UX
- Keine Layout-bedingte Produktions-Issues

**Timeline:**
- Phase 1: Woche 1-4 (SmartLayout Core)
- Phase 2: Woche 5-8 (Pilot Migration)
- Phase 3: Woche 9-12 (Full Rollout)
- Phase 4: Woche 13-16 (Optimization)

## 🔗 Related Documentation

**Foundation Knowledge:**
- **Design Standards:** → [DESIGN_SYSTEM.md](../grundlagen/DESIGN_SYSTEM.md)
- **Component Patterns:** → [COMPONENT_LIBRARY.md](../grundlagen/COMPONENT_LIBRARY.md)
- **Performance Guidelines:** → [PERFORMANCE_STANDARDS.md](../grundlagen/PERFORMANCE_STANDARDS.md)

**Implementation Details:**
- **Code Location:** `frontend/src/components/layout/SmartLayout/`
- **Test Files:** `SmartLayout.test.tsx`, `ContentTypeDetection.test.tsx`
- **Storybook:** `SmartLayout.stories.tsx`

**Related Plans:**
- **Dependencies:** Benötigt Design System V2 Completion
- **Follow-ups:** → [Component Library V3 Migration](./COMPONENT_LIBRARY_V3_PLAN.md) (geplant)
- **Alternatives:** Evaluiert: CSS Container Queries (zu früh im Browser-Support)

## 🤖 Claude Handover Section

**Für nächsten Claude:**

**Aktueller Stand:**
Migration-Plan erstellt nach PLANUNGSMETHODIK-Standards. SmartLayout-Architektur ist im Design System definiert, aber noch nicht implementiert.

**Nächster konkreter Schritt:**
Phase 1 starten - SmartLayout React Component implementieren mit Content-Type-Detection-Logic für die 4 Haupt-Content-Types.

**Wichtige Dateien für Context:**
- `docs/planung/grundlagen/DESIGN_SYSTEM.md` - SmartLayout Spezifikation und Content-Type-Matrix
- `frontend/src/components/layout/MainLayoutV2.tsx` - Aktueller Layout-Code für Migration-Reference
- `docs/planung/grundlagen/COMPONENT_LIBRARY.md` - React-Pattern-Standards

**Offene Entscheidungen:**
- Welche Content-Type-Detection-Strategie: Children-Analysis vs. Props-basiert?
- Performance vs. Accuracy Trade-off bei Content-Type-Detection?
- Backwards-Compatibility-Strategie während Migration-Phase?

**Kontext-Links:**
- **Grundlagen:** → [Design System V2](../grundlagen/DESIGN_SYSTEM.md)
- **Dependencies:** → [Component Library Standards](../grundlagen/COMPONENT_LIBRARY.md)