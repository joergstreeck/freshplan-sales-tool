# Essential Files für KI-Partner - Leseliste

## 📚 Dokumente in empfohlener Reihenfolge

### 1. Projekt-Vision & Strategie (Start hier!)
- `PROJECT_BRIEFING_FOR_AI_PARTNER.md` - Deine Einführung
- `VISION_AND_ROADMAP.md` - Wohin die Reise geht
- `CLAUDE.md` - Unsere Arbeitsphilosophie

### 2. Architektur & Technische Dokumentation
- `ARCHITECTURE.md` - System-Übersicht
- `MIGRATION_PLAN_V2.md` - Aktueller Refactoring-Plan
- `DASHBOARD_ARCHITECTURE.md` - UI-Struktur

### 3. Aktueller Status
- `MIGRATION_STATUS.md` - Wo stehen wir?
- `CUSTOMER_MODULE_V2_TEST_REPORT.md` - Beispiel unserer Gründlichkeit
- `GRUENDLICHER_TEST_REPORT.md` - Aktuelle Test-Ergebnisse

### 4. Code-Struktur (wichtigste Files)
```
src/
├── FreshPlanApp.ts                    # Hauptanwendung
├── types/index.ts                     # Alle TypeScript-Typen
├── modules/
│   ├── CustomerModule.ts              # Alt (Phase 1)
│   └── CustomerModuleV2.ts            # Neu (Phase 2)
├── services/
│   └── CustomerServiceV2.ts           # Service Layer Beispiel
├── infrastructure/
│   └── repositories/
│       └── LocalStorageCustomerRepository.ts  # Repository Pattern
├── domain/
│   ├── repositories/
│   │   └── ICustomerRepository.ts     # Interface
│   └── validators/
│       └── ICustomerValidator.ts      # Validation Interface
└── legacy-script.ts                   # Das wollen wir loswerden
```

### 5. Business Context
- `docs/business/freshplan_summary.md` - Was macht FreshPlan?
- `docs/business/agb_summary.md` - Geschäftsbedingungen
- `FEATURE_CHECKLIST.md` - Alle Features

### 6. Test-Szenarien & Qualität
- `TEST_KD_SCENARIOS.md` - Realistische Kundendaten
- `CUSTOMER_MODULE_TEST_CHECKLIST.md` - Wie wir testen
- `E2E_TEST_GUIDE.md` - End-to-End Testing

### 7. HTML Templates (für UI-Verständnis)
- `GOLDEN_REFERENCE.html` - Die Original-Referenz
- `index.html` - Aktuelle Implementierung

## 🎯 Quick Start für Code-Review

Wenn du direkt mit Code-Review starten willst:

1. **Vergleiche diese zwei Files:**
   - `src/modules/CustomerModule.ts` (alt)
   - `src/modules/CustomerModuleV2.ts` (neu)
   
2. **Schaue dir die Tests an:**
   - `src/__tests__/integration/customer-module-v2-simple.test.ts`
   - `src/__tests__/integration/customer-module-v2-fixed.test.ts`

3. **Prüfe das Repository Pattern:**
   - `src/domain/repositories/ICustomerRepository.ts`
   - `src/infrastructure/repositories/LocalStorageCustomerRepository.ts`

## 💡 Fokus-Themen für dein Feedback

### Architektur-Fragen:
1. Ist das Repository Pattern hier sinnvoll eingesetzt?
2. Sollten wir Dependency Injection Framework nutzen?
3. Wie können wir Module besser entkoppeln?

### Test-Strategie:
1. Wie erreichen wir 90%+ Coverage ohne Overhead?
2. Sollten wir Contract Testing einführen?
3. Wie testen wir die Legacy-Migration?

### Performance:
1. Wie optimieren wir für 1000+ Standorte?
2. Virtual Scrolling vs. Pagination?
3. Web Workers für schwere Berechnungen?

### Zukunft:
1. GraphQL vs. REST für die API?
2. Offline-First mit Service Workers?
3. Micro-Frontend Architektur?

## 📝 Notizen-Format

Wenn du beim Lesen Notizen machst, nutze dieses Format:

```markdown
### File: [Dateiname]
**Zweck**: Was macht diese Datei?
**Gut**: Was gefällt dir?
**Verbesserung**: Was könnte besser sein?
**Idee**: Deine Vorschläge
**Priorität**: Hoch/Mittel/Niedrig
```

---

**Tipp**: Starte mit `PROJECT_BRIEFING_FOR_AI_PARTNER.md` und dann `VISION_AND_ROADMAP.md`. Danach kannst du je nach Interesse in Code oder Business-Docs eintauchen.