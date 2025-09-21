# 📋 Pipeline Skalierung & Contract Renewal - Planungs-Session

**Datum:** 24.07.2025  
**Features:** FC-009 (Contract Renewal) & FC-010 (Pipeline Scalability)  
**Status:** Technische Konzepte erstellt

## 🎯 Zusammenfassung der Diskussion

Wir haben heute zwei wichtige Features für das FreshPlan CRM geplant:

### 1. FC-009: Contract Renewal Management
- **7. Kanban-Spalte "RENEWAL"** für auslaufende Verträge
- **Automatische Eskalation** bei 90/60/30 Tagen
- **Event-Driven Xentral Integration** für lose Kopplung
- **Lapsed Renewals** mit aktuellen Listenpreisen möglich

### 2. FC-010: Pipeline Scalability & UX
- **WIP-Limits** als Soft-Warnings implementieren
- **Smart Filter System** mit Quick Filters und Presets
- **3 Display Modi** (Kompakt/Standard/Detailliert)
- **Performance Optimierungen** für 500+ Opportunities

## 📊 Wichtige Business-Entscheidungen

### Contract Renewal (FC-009):
1. **Verträge zentral** - nicht pro Filiale
2. **Keine Teil-Renewals** - nur Komplettverlängerungen
3. **Rückwirkende Renewals erlaubt** mit Listenpreisen
4. **Preiskommunikation manuell** durch Vertrieb

### Pipeline UX (FC-010):
1. **Phase 1 Quick Wins**: Filter-Bar, Kompakt-Modus, WIP-Warnings
2. **Keine harten WIP-Limits** - Flexibilität wichtig
3. **Progressive Disclosure** für Closed Stages
4. **Bulk Actions** erst bei größerem Volumen

## 🏗️ Technische Highlights

### Hybride Dokumentenstruktur:
- **Haupt-Dokumente** < 150 Zeilen mit klaren Verweisen
- **Detail-Dokumente** für spezifische Aspekte
- **Absolute Pfade** für alle Verlinkungen
- **Parent-Child Beziehungen** klar dokumentiert

### Key Implementierungen:
```typescript
// WIP-Limits als Soft Warning
const WIP_LIMITS = {
  LEAD: 20,
  QUALIFIED: 15,
  PROPOSAL: 10,
  NEGOTIATION: 7,
  RENEWAL: 10
};

// Smart Filter Presets
const PRESET_VIEWS = {
  "Mein Fokus": { myOpportunities: true, needsAction: true },
  "Diese Woche": { closingSoon: true },
  "Große Deals": { highValue: true },
  "Renewals": { stage: ['RENEWAL'] }
};
```

## 🔗 Erstellte Dokumente

### FC-009 Contract Renewal:
1. **Hauptdokument**: `/docs/features/2025-07-24_TECH_CONCEPT_FC-009-contract-renewal-management.md`
2. **Backend**: `/docs/features/FC-009/backend-architecture.md`
3. **Frontend**: `/docs/features/FC-009/frontend-components.md`
4. **Xentral**: `/docs/features/FC-009/xentral-integration.md`

### FC-010 Pipeline Scalability:
1. **Hauptdokument**: `/docs/features/FC-010-pipeline-scalability-ux.md`
2. **UI/UX Specs**: `/docs/features/FC-010/ui-ux-specifications.md`
3. **Implementation**: `/docs/features/FC-010/implementation-strategy.md`

## 📈 Auswirkungen auf andere Features

### M4 Opportunity Pipeline:
- RENEWAL als 7. Stage hinzugefügt
- Neue API Endpoints für Filtering & Bulk Operations
- Contract Monitoring Integration

### FC-005 Xentral Integration:
- Contract Status Events definiert
- Discount Management API erweitert
- Price Index Monitoring Events

### Neue offene Fragen dokumentiert:
- Eskalations-Empfänger E-Mails
- Provisionslogik bei Lapsed Renewals
- Performance-Schwellwerte für Virtual Scrolling

## 🚀 Nächste Schritte

1. **M4 Backend-Integration abschließen** (TODO-60)
2. **Filter-Bar implementieren** (Phase 1 von FC-010)
3. **RENEWAL Stage hinzufügen** (TODO-64)
4. **Contract Monitoring Entity** erstellen

## 💡 Key Learnings

1. **Soft Limits > Hard Limits** für B2B-Flexibilität
2. **Progressive Disclosure** essentiell bei wachsenden Daten
3. **Event-Driven Architecture** perfekt für CRM↔ERP
4. **Kompakte Ansichten** als Game Changer identifiziert

Die Planung zeigt: FreshPlan wird ein hochmodernes, skalierbares CRM mit exzellenter UX!