# 📊 CODE INVENTORY - FreshPlan Sales Tool

**Erstellt am:** 09.07.2025  
**Zweck:** Vollständige Bestandsaufnahme des bestehenden Codes als Basis für FC-002 Planung

## 🎯 Executive Summary

### Projektumfang
- **Gesamtgröße:** ~200 Produktiv-Dateien + 43 Tests + 15 DB-Migrationen
- **Frontend:** 106 TypeScript/React-Dateien in 5 Feature-Modulen
- **Backend:** 94 Java-Dateien in 6 Domain-Modulen
- **Technischer Stack:** React 18 + Quarkus + Keycloak + PostgreSQL

### Identifizierte Haupt-Features
1. **Calculator** - Kalkulations-Tool für Catering-Aufträge (22 Frontend-Dateien)
2. **Customer Management** - Umfassende Kundenverwaltung (54 Backend-Dateien!)
3. **Cockpit** - Dashboard mit KPIs und Visualisierungen (14+6 Dateien)
4. **User & Profile Management** - Benutzer- und Rollenverwaltung
5. **Dynamic Focus List** (FC-001) - Neu implementiert, noch nicht integriert

## 📁 Frontend-Struktur im Detail

### Feature-Module Übersicht

```
frontend/src/
├── features/                    # Feature-basierte Organisation
│   ├── calculator/ (22 Dateien) # Größtes Frontend-Modul
│   │   ├── api/                 # API-Client für Calculator
│   │   ├── components/          # UI-Komponenten
│   │   ├── hooks/               # Custom Hooks
│   │   ├── store/               # Zustand-Management
│   │   └── types/               # TypeScript-Definitionen
│   ├── cockpit/ (14 Dateien)    # Dashboard/Übersicht
│   │   ├── components/          # SalesCockpit, MyDayColumn etc.
│   │   ├── data/                # Mock-Daten
│   │   ├── hooks/               # useSalesCockpit Hook
│   │   └── types/               # Dashboard-Typen
│   ├── customer/ (5 Dateien)    # Einzelkunden-Ansicht
│   ├── customers/ (4 Dateien)   # Kundenliste (⚠️ KEINE TESTS!)
│   └── users/ (11 Dateien)      # Benutzerverwaltung
├── components/ (24 Dateien)      # Gemeinsame UI-Komponenten
├── i18n/ (18 Dateien)           # Übersetzungen DE/EN
├── contexts/                     # React Contexts
├── store/                        # Global State
├── hooks/                        # Globale Hooks
├── services/                     # API Services
├── types/                        # Globale Typen
└── temp/ (❗ Ungenutzt)         # Zu bereinigen
```

### 🚨 Claude's Beobachtungen - Frontend

**1. Cockpit-Modul ist bereits 3-Spalten-ready!**
- `SalesCockpit.tsx` implementiert bereits die Vision aus dem Master Plan
- `MyDayColumn.tsx`, `FocusListColumn.tsx`, `ActionCenterColumn.tsx` existieren
- ABER: Noch mit CSS statt MUI, keine TypeScript strict mode

**2. Calculator dominiert das Frontend**
- Mit 22 Dateien das größte Feature-Modul
- Gut strukturiert mit eigener API, Store, Hooks
- Könnte als Referenz für andere Module dienen

**3. Customers vs Customer - Verwirrende Trennung**
- `customer/` = Einzelansicht (5 Dateien)
- `customers/` = Liste (4 Dateien, KEINE Tests!)
- Diese Trennung sollte in FC-002 überdacht werden

**4. Technische Schuld im temp/ Verzeichnis**
- Enthält ungenutzte Komponenten
- Sollte vor FC-002 bereinigt werden

## 🔧 Backend-Struktur im Detail

### Domain-Module Übersicht

```
backend/src/main/java/de/freshplan/
├── domain/                      # Domain-Driven Design
│   ├── customer/ (54 Dateien!)  # ❗ MEGA-Modul
│   │   ├── entity/              # Customer, Contact, Note, Activity
│   │   ├── repository/          # Data Access Layer
│   │   ├── service/             # Business Logic
│   │   │   ├── dto/            # 21 DTOs!
│   │   │   ├── mapper/         # Entity-DTO Mapping
│   │   │   └── timeline/       # Timeline-Feature
│   │   └── validation/          # Custom Validators
│   ├── user/ (14 Dateien)       # Benutzerverwaltung
│   ├── profile/ (10 Dateien)    # Persönliche Profile
│   ├── cockpit/ (6 Dateien)     # Dashboard-Aggregationen
│   ├── calculator/ (3 Dateien)  # Kalkulations-Service
│   └── testdata/                # Test-Daten-Generator
├── api/                         # REST Layer
│   ├── resources/               # REST Endpoints
│   └── exception/               # Exception Handling
└── infrastructure/              # Rate Limiting, Config
```

### 🚨 Claude's Beobachtungen - Backend

**1. Customer-Domain ist ein Monolith!**
- 54 Dateien in einem Modul sind zu viel
- Enthält: Customers, Contacts, Notes, Activities, Timeline
- Sollte in FC-002 aufgeteilt werden in kleinere, fokussierte Module

**2. Exzellente Test-Coverage im Backend**
- 33 Test-Dateien (vs. nur 10 im Frontend)
- Gute Integration-Tests mit Testcontainers
- Mock-Endpoints für Entwicklung vorhanden

**3. Timeline-Feature versteckt in Customer**
- Eigenes Sub-Package `timeline/` 
- Könnte ein eigenständiges Feature sein
- Wichtig für FC-002 Activity Timeline Phase

**4. Calculator-Backend minimal**
- Nur 3 Dateien im Backend vs. 22 im Frontend
- Meiste Logik scheint im Frontend zu sein
- Potentieller Refactoring-Kandidat?

## 🗄️ Datenbank-Schema

### Migrations-Historie (15 Dateien)
1. **V1-V5**: Basis-Schema (User, Roles, Profile, Customer)
2. **V10-V17**: Iterative Verbesserungen
3. **V100**: Test-Daten-Flag
4. **V102**: Performance-Indizes für Customer-Suche ✅

### 🚨 Claude's DB-Beobachtungen
- Gut strukturierte Migration-Historie
- Performance bereits berücksichtigt (V102)
- Test-Daten sauber getrennt (V100)

## 🧪 Test-Coverage Analyse

### Kritische Lücken
| Modul | Frontend Tests | Backend Tests | Risiko |
|-------|----------------|---------------|---------|
| Calculator | 3/22 (14%) | Minimal | Mittel |
| Cockpit | 4/14 (29%) | Gut | Niedrig |
| Customers | 0/4 (0%) ❌ | Exzellent | HOCH |
| Customer | 2/5 (40%) | Exzellent | Niedrig |
| Users | 1/11 (9%) | Gut | Mittel |

### 🚨 Claude's Test-Bedenken
- **Frontend generell untergetestet** (~10% Coverage)
- **Customers-Modul komplett ohne Tests** - Kritisch für FC-002!
- **Backend gut getestet** - Kann als Sicherheitsnetz dienen

## 💡 Strategische Empfehlungen für FC-002

### 1. Modularisierung des Customer-Monolithen
```
Vorschlag: Aufteilen in:
- customers/ (Stammdaten)
- contacts/ (Ansprechpartner)
- activities/ (Timeline, Notes)
- communications/ (E-Mails, Calls)
```

### 2. Frontend-Test-Strategie
- ERST Tests für bestehende Features schreiben
- DANN mit FC-002 Migration beginnen
- Besonders: Customers-Modul testen!

### 3. Code-Konsolidierung
- `customer/` und `customers/` zusammenführen
- `temp/` Verzeichnis bereinigen
- Calculator Frontend/Backend Balance prüfen

### 4. Wiederverwendbare Komponenten identifiziert
- **FilterBar** aus customers/ - perfekt für FC-002
- **CustomerCard** - bereits MUI-ready
- **focusListStore** - ausgereiftes State Management
- **Activity Timeline** Backend - bereit für Frontend

### 5. Migration-Reihenfolge (Empfehlung)
1. **Test-Suite aufbauen** (1-2 Tage)
2. **Cockpit migrieren** (bereits 3-Spalten-Layout!)
3. **Customer(s) konsolidieren** 
4. **Calculator integrieren**
5. **Activity Timeline** implementieren

## 🎯 Zusammenfassung für FC-002 Planung

**Positiv:**
- Solide Architektur-Basis vorhanden
- Backend gut strukturiert und getestet
- Viele wiederverwendbare Komponenten
- Performance bereits berücksichtigt

**Herausforderungen:**
- Frontend-Test-Coverage kritisch niedrig
- Customer-Domain zu groß
- Inkonsistente Modul-Struktur
- Technische Schuld (temp/, fehlende Tests)

**Wichtigste Erkenntnis:**
Das Cockpit-Modul hat bereits die 3-Spalten-Struktur! Wir müssen es "nur" auf MUI migrieren und mit den neuen Stores verbinden. Das reduziert den Aufwand erheblich.

---

*Diese Inventur dient als Arbeitsgrundlage für die schrittweise Analyse und Planung von FC-002.*