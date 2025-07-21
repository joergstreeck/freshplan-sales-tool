# 📚 Dokumentations-Organisation für Hybrid-Modell

**Erstellt:** 12.07.2025  
**Zweck:** Klare Struktur für alle Planungsdokumente im Kontext des neuen Hybrid-Modells

## 🎯 Vorgeschlagene Dokumentations-Hierarchie

### 1️⃣ Master-Dokumente (Immer aktuell halten!)
```
docs/
├── CRM_COMPLETE_MASTER_PLAN.md          # Die Vision (nur Links zu Details)
└── features/
    ├── FC-002-IMPLEMENTATION_PLAN.md    # Der aktuelle Fahrplan ⭐
    └── FC-002-hub.md                    # Technische Übersicht aller Module
```

### 2️⃣ Feature-Konzepte (Pro Feature ein Dokument)
```
docs/features/
├── AKTIVE FEATURES (In Arbeit)
│   ├── 2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md  # AKTUELL
│   ├── FC-002-M8-rechner.md                                # Als Modal
│   └── FC-002-M5-kundenmanagement.md                       # Nächste Phase
│
├── GEPLANTE FEATURES (Nach Core-Migration)
│   ├── FC-003-email-integration.md      # Kommunikations-Hub
│   ├── FC-004-verkaeuferschutz.md       # Business Rules
│   ├── FC-005-xentral-integration.md    # Externe Systeme
│   ├── FC-006-mobile-app.md             # Mobile Strategy
│   └── FC-007-chef-dashboard.md         # KPIs & Analytics
│
└── ARCHIV (Veraltete Konzepte)
    ├── FC-002-M1-hauptnavigation.md     # ✅ Erledigt
    ├── FC-002-M3-cockpit.md             # ✅ Erledigt
    └── FC-002-M7-einstellungen.md       # ✅ Erledigt
```

### 3️⃣ Arbeitsdokumentation
```
docs/claude-work/daily-work/YYYY-MM-DD/
├── HANDOVER_HH-MM.md                    # Session-Übergaben
├── CHANGE_LOG_feature.md                # Was wurde geändert
└── PROBLEM_ANALYSIS_feature.md          # Gelöste Probleme
```

## 🔄 Prozess für Dokumentations-Updates

### Bei Feature-Start:
1. **Tech-Konzept erstellen** aus Template
2. **Master-Plan verlinken** (nur Verweis, keine Details!)
3. **Dependencies prüfen** und dokumentieren

### Während Implementierung:
1. **Change Logs** für jede signifikante Änderung
2. **Tech-Konzept aktualisieren** bei Abweichungen
3. **IMPLEMENTATION_PLAN** Status updaten

### Bei Feature-Abschluss:
1. **Modul-Dokument** in ARCHIV verschieben
2. **Lessons Learned** dokumentieren
3. **Master-Plan** Status auf ✅ setzen

## 🚨 Kritische Regeln

1. **NIE den Master-Plan mit Details überladen**
   - Nur Status und Links
   - Details in Feature-Konzepten

2. **IMMER Tech-Konzept vor Implementierung**
   - Verhindert Missverständnisse
   - Basis für Code-Reviews

3. **Change Logs sind PFLICHT**
   - Nachvollziehbarkeit
   - Onboarding neuer Entwickler

## 📋 Sofort zu aktualisierende Dokumente

### Begriffliche Anpassungen:
1. **FC-002-M4-neukundengewinnung.md**
   → Umbenennen in "FC-002-M4-opportunity-pipeline.md"

2. **FC-002-M2-quick-create.md**
   → Konzept anpassen: "Kontextuelle Aktionen" statt "Quick-Create System"

3. **FC-002-M6-berichte.md**
   → Konzept anpassen: "Embedded Analytics" statt "Berichte-Modul"

### Navigation Update:
```typescript
// ALT (5 Module)
const navigation = [
  'cockpit',
  'neukundengewinnung',
  'kundenmanagement', 
  'quick-create',
  'berichte'
];

// NEU (3 Haupt-Bereiche + Einstellungen)
const navigation = [
  'mein-tag',           // Dashboard mit Alerts
  'opportunities',      // Pipeline & Sales
  'kunden-kontakte',    // 360° Kundenansicht
  'einstellungen'       // Admin & Config
];
```

## 🎮 Simulations-Szenario: M4 Implementation

### Tag 1: Planung
```bash
# 1. Tech-Konzept lesen
cat docs/features/2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md

# 2. Dependencies prüfen
grep -r "M4" docs/features/*.md | grep -i "depend"

# 3. API-Contract checken
cat docs/technical/API_CONTRACT.md | grep -A20 "opportunity"
```

### Tag 2-3: Backend
```bash
# 1. Entity erstellen
vim backend/src/main/java/de/freshplan/domain/opportunity/entity/Opportunity.java

# 2. Repository
vim backend/src/main/java/de/freshplan/domain/opportunity/repository/OpportunityRepository.java

# 3. Service & DTOs
vim backend/src/main/java/de/freshplan/domain/opportunity/service/OpportunityService.java
```

### Tag 4-5: Frontend
```bash
# 1. Komponenten-Struktur
mkdir -p frontend/src/features/opportunity/{components,hooks,services,types}

# 2. Pipeline UI
vim frontend/src/features/opportunity/components/OpportunityPipeline.tsx

# 3. Integration testen
npm run dev
```

## 🔍 Erkannte Schwachstellen

### 1. **Fehlende API-Spezifikation**
- Kein OpenAPI/Swagger für Opportunities
- Lösung: API-First Development

### 2. **State Management unklar**
- Zustand vs. React Query vs. Context?
- Lösung: Klare Architektur-Entscheidung

### 3. **Testing-Strategie fehlt**
- Wie testen wir Drag & Drop?
- Lösung: Testing-Konzept pro Feature

### 4. **Performance bei vielen Opportunities**
- Virtual Scrolling nötig?
- Lösung: Performance-Budget definieren

### 5. **Migrations-Pfad unklar**
- Wie migrieren wir bestehende Daten?
- Lösung: Migrations-Strategie dokumentieren

## 🚀 Nächste Schritte

1. **Begriffe in Navigation anpassen** (5 Min)
2. **M2 und M6 Konzepte überarbeiten** (30 Min)
3. **API-Spezifikation für M4 erstellen** (1 Std)
4. **Mit M4 Backend beginnen** (1.5 Tage)

Diese Organisation macht die Dokumentation wartbar und verhindert Inkonsistenzen!