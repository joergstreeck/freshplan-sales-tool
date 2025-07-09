# 🔗 Feature-Abhängigkeiten-Matrix

**Datum:** 09.07.2025  
**Status:** Erste Analyse abgeschlossen  
**Zweck:** Übersicht über Abhängigkeiten zwischen neuen Features  

## 📊 Abhängigkeits-Matrix

| Feature | Abhängig von | Beeinflusst | Kritische Integration |
|---------|--------------|-------------|----------------------|
| **FC-003: E-Mail** | M5 (Customer), M4 (Activities) | FC-004, FC-006 | Customer Timeline |
| **FC-004: Verkäuferschutz** | M5 (Customer), FC-005 | FC-007 | Xentral für Provisionen |
| **FC-005: Xentral** | OAuth, Webhooks | FC-004, FC-007 | Payment Events |
| **FC-006: Mobile** | GraphQL API, FC-003, FC-004 | - | Offline Sync |
| **FC-007: Dashboard** | M4, M5, FC-004, FC-005 | FC-006 | Real-Time Data |

## 🎯 Kritische Pfade

### Pfad 1: Provisions-System
```
FC-005 (Xentral) → FC-004 (Verkäuferschutz) → FC-007 (Dashboard)
```
**Warum kritisch:** Ohne Xentral-Daten keine korrekte Provisionsberechnung

### Pfad 2: Kommunikations-Stack
```
FC-003 (E-Mail) → M4 (Activities) → Timeline
```
**Warum kritisch:** E-Mails müssen in die Activity-Timeline integriert werden

### Pfad 3: Mobile-Readiness
```
GraphQL API → FC-006 (Mobile) → Offline Sync
```
**Warum kritisch:** Mobile App braucht optimierte API

## 🏗️ Empfohlene Implementierungsreihenfolge

### Batch 1: Foundation (Parallel möglich)
- FC-003: E-Mail-Integration
- FC-005: Xentral-Integration (API Client)

### Batch 2: Core Business Logic
- FC-004: Verkäuferschutz-System (nach FC-005 API)
- FC-007: Dashboard Basis

### Batch 3: Advanced Features
- FC-006: Mobile App
- FC-007: Dashboard Erweiterungen

## ⚠️ Risiken durch Abhängigkeiten

### Risiko 1: Xentral API Verzögerung
**Impact:** FC-004 und FC-007 blockiert
**Mitigation:** Mock-API für Entwicklung

### Risiko 2: Performance bei Real-Time Updates
**Impact:** FC-007 Dashboard langsam
**Mitigation:** Event-basierte Architektur, Caching

### Risiko 3: Mobile Offline-Sync Komplexität
**Impact:** FC-006 Verzögerung
**Mitigation:** Schrittweise Features, Online-First

## 🔄 Synergien zwischen Features

### E-Mail + Verkäuferschutz
- E-Mails dokumentieren automatisch Kundenkontakt
- Schutz-Level kann durch E-Mail-Aktivität steigen

### Xentral + Dashboard
- Echtzeit-Provisionsanzeige
- Forecast basiert auf echten Zahlen

### Mobile + Alle Features
- Alle Features müssen Mobile-Ready sein
- Push-Notifications für alle Events

## 📈 Technische Vorbedingungen

### Für alle neuen Features:
1. **Event-System** muss etabliert sein
2. **GraphQL API** für flexible Queries
3. **Webhook-Infrastruktur** für externe Events
4. **Background Jobs** für Sync-Operationen
5. **Caching-Strategie** für Performance

### Spezifische Anforderungen:
- **FC-003**: SMTP-Server oder Service
- **FC-004**: Provisions-Regelwerk (Business)
- **FC-005**: Xentral API Zugangsdaten
- **FC-006**: App Store Accounts
- **FC-007**: Analytics-Datenbank (Read Models)