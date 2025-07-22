# 🚀 FC-002 Master-Implementierungsplan

**Feature:** FC-002 - UI/UX-Neuausrichtung & Prozessorientierung  
**Erstellt:** 09.07.2025  
**Status:** ✅ Finalisiert  
**Geschätzter Gesamtaufwand:** 35-40 Personentage  

## 📋 Executive Summary

Dieser Plan definiert die optimale Implementierungsreihenfolge für alle 8 Module der UI/UX-Neuausrichtung. Die Strategie basiert auf:
- **Abhängigkeiten**: Basis-Module zuerst
- **Quick Wins**: Hoher User-Impact bei geringem Aufwand
- **Risikominimierung**: Kritische Module früh angehen

## 🎯 Implementierungs-Reihenfolge

### Prioritäts-Matrix

| Prio | Modul | Aufwand | Impact | Abhängigkeiten | Begründung |
|------|-------|---------|--------|----------------|------------|
| 1 | M1 - Hauptnavigation | 2 Tage | HOCH | Keine | Basis für alle anderen Module |
| 2 | M7 - Einstellungen | 3 Tage | HOCH | M1 | Quick Win - Backend ready, User-Basis |
| 3 | M3 - Cockpit | 3 Tage | SEHR HOCH | M1, FC-001 | Kern-UI für tägliche Arbeit |
| 4 | M2 - Quick-Create | 2 Tage | MITTEL | M1 | Globale Funktionalität |
| 5 | M5 - Kundenmanagement | 12 Tage | SEHR HOCH | M1, M2 | Größtes Modul, kritisch für Business |
| 6 | M8 - Rechner | 2 Tage | MITTEL | M1 | Kann parallel laufen |
| 7 | M4 - Neukundengewinnung | 5 Tage | MITTEL | M1, M5 | Baut auf Kundenmanagement auf |
| 8 | M6 - Berichte | 5 Tage | NIEDRIG | Alle | Benötigt Daten aus anderen Modulen |

## 🏁 Meilensteine

### 📍 Meilenstein 1: "Das neue Grundgerüst" (5 Tage)
**Ziel:** Navigations-Struktur und Basis-Funktionalität etablieren

#### Sprint 1.1: Navigation & Auth (2 Tage)
- **M1 - Hauptnavigation** (2 Tage)
  - [ ] Sidebar-Komponente mit 5-Punkte-Navigation
  - [ ] Responsive Design (Mobile Hamburger)
  - [ ] Navigation Store implementieren
  - [ ] Routing-Integration
  - [ ] Keyboard Shortcuts (Alt+1-5)

#### Sprint 1.2: User & Settings (3 Tage)
- **M7 - Einstellungen** (3 Tage)
  - [ ] Settings-Dashboard mit Tab-Navigation
  - [ ] User-Preferences Backend API
  - [ ] Persönliche Einstellungen UI
  - [ ] Admin-Panel für User Management
  - [ ] Keycloak Self-Service Integration

**Deliverables:**
- ✅ Voll funktionsfähige Navigation
- ✅ User können sich einloggen und Settings verwalten
- ✅ Basis für alle weiteren Module steht

---

### 📍 Meilenstein 2: "Das neue Cockpit" (17 Tage)
**Ziel:** Kern-Arbeitsbereich und kritische Backend-Modernisierung

#### Sprint 2.1: Cockpit UI (3 Tage)
- **M3 - Cockpit-Integration** (3 Tage)
  - [ ] CockpitView.tsx mit MUI erstellen
  - [ ] 3-Spalten-Layout migrieren
  - [ ] Responsive Breakpoints
  - [ ] Performance-Optimierung
  - [ ] Integration mit FC-001 (Focus List)

#### Sprint 2.2: Quick Actions (2 Tage)
- **M2 - Quick-Create System** (2 Tage)
  - [ ] Globaler "+ Neu" Button in Header
  - [ ] Quick-Create Modal/Drawer
  - [ ] Entity-Typ Auswahl
  - [ ] Context-aware Vorschläge
  - [ ] Keyboard Shortcut (Ctrl/Cmd+N)

#### Sprint 2.3: Backend-Modernisierung (12 Tage)
- **M5 - Kundenmanagement Backend** (7 Tage)
  - [ ] Phase 1: Modulare Struktur aufbauen
  - [ ] Phase 2: Event Bus implementieren
  - [ ] Phase 3: API Facade erstellen
  - [ ] Phase 4: Feature Flags einrichten
  - [ ] Phase 5: Migration durchführen
  - [ ] Phase 6: Read Models optimieren
  - [ ] Phase 7: Cleanup & Dokumentation

- **M5 - Kundenmanagement Frontend** (5 Tage)
  - [ ] CustomerListView mit erweiterten Filtern
  - [ ] CustomerDetailView 360°
  - [ ] Quick Actions Integration
  - [ ] Timeline-Komponente
  - [ ] Bulk Operations

**Deliverables:**
- ✅ Vollständiges Sales Cockpit live
- ✅ Modernisierte Backend-Architektur
- ✅ Kern-CRM-Funktionalität migriert

---

### 📍 Meilenstein 3: "Feature-Vervollständigung" (12 Tage)
**Ziel:** Alle verbleibenden Module implementieren

#### Sprint 3.1: Business Features (7 Tage)
- **M8 - Rechner** (2 Tage) - *Kann parallel laufen*
  - [ ] Calculator UI zu MUI migrieren
  - [ ] Batch-Calculation Backend
  - [ ] Szenario-Vergleich Feature
  - [ ] Integration ins Aktions-Center

- **M4 - Neukundengewinnung** (5 Tage)
  - [ ] Lead-Erfassung Workflow
  - [ ] Pipeline-Visualisierung
  - [ ] Automatisierte Follow-ups
  - [ ] Integration mit M5
  - [ ] Conversion-Tracking

#### Sprint 3.2: Analytics & Polish (5 Tage)
- **M6 - Berichte & Auswertungen** (5 Tage)
  - [ ] Dashboard mit KPIs
  - [ ] Report Builder
  - [ ] Export-Funktionalität
  - [ ] Scheduled Reports
  - [ ] Visualisierungen

**Deliverables:**
- ✅ Alle Module implementiert
- ✅ Vollständige Feature-Parität erreicht
- ✅ System production-ready

---

## 📊 Ressourcen-Planung

### Team-Aufteilung (Empfehlung)

| Phase | Frontend-Dev | Backend-Dev | Parallel möglich |
|-------|--------------|-------------|------------------|
| Meilenstein 1 | M1, M7 (UI) | M7 (API) | Ja |
| Meilenstein 2 | M3, M2, M5 (UI) | M5 (Backend) | Ja - M5 BE/FE parallel |
| Meilenstein 3 | M4, M6, M8 (UI) | M8 (API) | Ja - M8 kann früher starten |

### Zeitschiene

```
Woche 1-2: Meilenstein 1 (5 Tage)
Woche 3-6: Meilenstein 2 (17 Tage)
Woche 7-8: Meilenstein 3 (12 Tage)
Woche 9: Buffer & Testing (4 Tage)
---------------------------------
Gesamt: 38 Tage (~8 Wochen mit 1 Person)
Mit 2 Entwicklern: ~4-5 Wochen
```

## 🚨 Kritische Pfade & Risiken

### Kritische Abhängigkeiten
1. **M1 muss zuerst fertig sein** - Alle anderen Module hängen davon ab
2. **M5 Backend vor M4** - Neukundengewinnung baut auf Customer-Entitäten auf
3. **FC-001 muss fertig sein** - M3 Cockpit integriert die Focus List

### Risiko-Mitigation
1. **M5 Backend-Refactoring**: Feature Flags ermöglichen schrittweise Migration
2. **Performance**: Frühzeitige Tests mit realistischen Datenmengen
3. **UI-Konsistenz**: Design System von Anfang an etablieren

## ✅ Definition of Done (pro Meilenstein)

### Meilenstein 1
- [ ] Navigation in allen Browsern getestet
- [ ] User Management funktioniert mit Keycloak
- [ ] E2E Tests für Login & Navigation
- [ ] Dokumentation aktualisiert

### Meilenstein 2
- [ ] Cockpit Performance < 100ms Response Time
- [ ] Backend-Migration ohne Breaking Changes
- [ ] Integration Tests grün
- [ ] Code Review abgeschlossen

### Meilenstein 3
- [ ] Alle Features implementiert
- [ ] Cross-Browser Testing abgeschlossen
- [ ] Performance Budgets eingehalten
- [ ] User Acceptance Testing durchgeführt

## 🎯 Erfolgskriterien

1. **User Experience**
   - Navigation intuitiv und schnell
   - Alle Aktionen < 100ms Response
   - Mobile-First Design umgesetzt

2. **Code-Qualität**
   - Test Coverage > 80%
   - Keine kritischen SonarQube Issues
   - Dokumentation vollständig

3. **Business Value**
   - Verkaufsprozess 30% schneller
   - Weniger Klicks für häufige Aktionen
   - Positive User-Feedback Score > 4.5/5

---

**Nächster Schritt:** Mit Meilenstein 1 - Sprint 1.1 (M1 Hauptnavigation) beginnen!