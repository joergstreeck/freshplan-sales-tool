# FC-002: UI/UX-Neuausrichtung & Prozessorientierung (Hub)

**Feature Code:** FC-002  
**Datum:** 08.07.2025  
**Status:** 📋 In Planung  
**Typ:** Hub-Dokument (Übersicht)  
**Autor:** Claude (AI Assistant)  
**Reviewer:** Jörg Streeck  

## 📋 Module im Überblick

| Modul | Status | Dokument | Beschreibung |
|-------|--------|----------|--------------|
| M1 | 🔄 0% | [Hauptnavigation](./FC-002-M1-hauptnavigation.md) | Sidebar mit 5-Punkte-Navigation |
| M2 | 📋 0% | [Quick-Create System](./FC-002-M2-quick-create.md) | Globaler "+ Neu" Button |
| M3 | 🔍 40% | [Cockpit-Integration](./FC-002-M3-cockpit.md) | 3-Spalten-Ansicht Migration (Analyse abgeschlossen) |
| M4 | 📋 0% | [Neukundengewinnung](./FC-002-M4-neukundengewinnung.md) | Akquise-Modul |
| M5 | ✅ 50% | [Kundenmanagement](./FC-002-M5-kundenmanagement.md) | CRM-Kernfunktionen (Planung abgeschlossen) |
| M6 | 📋 0% | [Berichte & Auswertungen](./FC-002-M6-berichte.md) | Analytics & Reports |
| M7 | 🔍 Analysiert | [Einstellungen](./FC-002-M7-einstellungen.md) | User & System Settings (Analyse abgeschlossen) |
| M8 | 🔍 Analysiert | [Rechner](./FC-002-M8-rechner.md) | Kalkulator-Modul (Analyse abgeschlossen) |

## 🚀 Master-Implementierungsplan

**[📋 Zum detaillierten Implementierungsplan →](./FC-002-IMPLEMENTATION_PLAN.md)**

Der Master-Implementierungsplan definiert 3 Meilensteine mit klarer Priorisierung und Abhängigkeiten.

## 🎯 Vision

Die UI/UX-Neuausrichtung transformiert das FreshPlan Sales Tool von einer funktionsgetriebenen zu einer **prozessorientierten Anwendung**. Vertriebsmitarbeiter werden durch ihre täglichen Arbeitsabläufe geführt, statt Funktionen suchen zu müssen.

### Kernprinzipien

1. **Prozessorientierung**: Werkzeuge erscheinen dort, wo sie im Arbeitsablauf benötigt werden
2. **Klarheit**: Deutsche Begriffe statt Anglizismen ([Way of Working](../WAY_OF_WORKING.md))
3. **Konsistenz**: Einheitliche Interaktionsmuster über alle Module
4. **Performance**: Alle Aktionen < 100ms Response Time

## 🏗️ Technische Architektur

### Gesamtstruktur
```
┌─────────────────────────────────────────────────────────────┐
│ Header (Logo | Globale Suche | [+ Neu] | Profil)           │
├─────────────┬───────────────────────────────────────────────┤
│             │                                               │
│  Sidebar    │           Hauptinhaltsbereich                 │
│ Navigation  │           (Kontext-abhängig)                  │
│             │                                               │
└─────────────┴───────────────────────────────────────────────┘
```

### Routing-Struktur
```typescript
/cockpit              → Mein Cockpit (M3)
/neukundengewinnung   → Akquise-Modul (M4)
/kundenmanagement     → CRM-Funktionen (M5)
/berichte             → Analytics (M6)
/einstellungen        → Settings (M7)
```

## 🎨 Globale Standards

### UI-Zustände (Pflicht für alle Module)
- **Loading**: Skeleton/Spinner während Datenladen
- **Empty**: Leerer Zustand mit Handlungsaufforderung
- **Error**: Fehlerbehandlung mit Retry-Option
- **Success**: Erfolgreiche Aktionen mit Feedback

### Notification Service
```typescript
// Zentral in allen Modulen zu verwenden
notificationService.show({
  type: 'success' | 'error' | 'warning' | 'info',
  message: string,
  duration?: number
});
```

### Keyboard Shortcuts
- `Ctrl/Cmd + N`: Neuer Eintrag (Quick Create)
- `Ctrl/Cmd + K`: Globale Suche
- `Alt + 1-5`: Hauptnavigation
- Weitere siehe Modul-Dokumentationen

### Accessibility Standards
- WCAG 2.1 AA Compliance
- Keyboard Navigation
- Screen Reader Support
- Focus Management

## 📦 State Management

| Store | Zweck | Module |
|-------|-------|--------|
| `navigationStore` | Sidebar-Zustand | M1 |
| `quickCreateStore` | Quick-Create Modal | M2 |
| `cockpitStore` | Cockpit-Filter | M3 |
| `authStore` | User & Permissions | Alle |
| `notificationStore` | Global Notifications | Alle |

## 🚀 Implementierungsstrategie

### Phase 1: Grundstruktur (5-7 Tage)
1. M1: Hauptnavigation
2. M2: Quick-Create System
3. Globale Services (Notification, Auth)

### Phase 2: Cockpit (3-5 Tage)
4. M3: Cockpit-Integration mit FC-001

### Phase 3: Module (10-14 Tage)
5. M4-M7: Schrittweise Migration

### Phase 4: Polish (3-5 Tage)
- Performance-Optimierung
- Accessibility Audit
- E2E Tests

## 📎 Anhänge

- [Anhang A: Technische Strategien & Offene Fragen](./FC-002-anhang-A-strategien.md)
- [Anhang B: Backend-Anforderungen](./FC-002-anhang-B-backend.md)

## 🔗 Abhängigkeiten

- FC-001: Dynamic Focus List (für M3 Cockpit)
- Freshfoodz CI Standards
- Keycloak Integration

---

**Status-Updates**:
- 08.07.2025: Hub-Struktur erstellt, Module aufgeteilt
- 09.07.2025: M3 Cockpit-Modul Planung abgeschlossen (30%), Analyse durchgeführt
- [Datum]: Review durch Jörg
- [Datum]: Approved / Änderungen
## 📅 Status-Updates

### 09.07.2025 - Planungsphase abgeschlossen
- **M3 Cockpit**: Code-Analyse abgeschlossen (40% Wiederverwendbarkeit)
- **M5 Kundenmanagement**: Implementierungsplan finalisiert und freigegeben
- **M7 Einstellungen**: Kompakte Analyse durchgeführt
  - User Management Backend: 100% wiederverwendbar (production-ready)
  - Frontend: 85% wiederverwendbar (minimale UI-Anpassungen)
  - Geschätzter Aufwand: 3 Personentage
- **M8 Rechner**: Kompakte Analyse durchgeführt
  - Backend: 95% wiederverwendbar
  - Frontend: 70% wiederverwendbar (UI-Migration zu MUI nötig)
  - Geschätzter Aufwand: 2 Personentage
- **Master-Implementierungsplan erstellt**: 3 Meilensteine, 35-40 Personentage Gesamtaufwand
