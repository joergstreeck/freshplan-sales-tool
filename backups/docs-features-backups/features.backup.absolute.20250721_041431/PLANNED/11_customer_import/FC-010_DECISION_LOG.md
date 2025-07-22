# 📋 DECISION LOG: FC-010 Customer Import

**Status:** 🟡 Wichtige Entscheidungen offen  
**Owner:** @joergstreeck  
**Last Updated:** 17.07.2025 14:55  

---

## ✅ ENTSCHIEDENE FRAGEN

### 1. Architektur-Ansatz
**Frage:** Migration vs. Adaptation der Legacy CustomerForm?  
**Entscheidung:** Hybrid - Backend Adaptation (80%), Frontend Migration (20%)  
**Begründung:** Nutze bewährte CustomerService-Logik, aber neue UI für Cockpit-Theme  
**Datum:** 17.07.2025  

### 2. Flexibilität vs. Performance
**Frage:** Configuration-Driven vs. Hardcoded Fields?  
**Entscheidung:** Configuration-Driven mit Plugin-System  
**Begründung:** Langfristige Flexibilität wichtiger als initiale Performance  
**Datum:** 17.07.2025  

### 3. UI-Architektur
**Frage:** Wizard vs. Single-Page Import?  
**Entscheidung:** Hybrid - Wizard (Default) + Expert Mode (Toggle)  
**Begründung:** "Geführte Freiheit" - Einsteiger und Power-User bedienen  
**Datum:** 17.07.2025  

### 4. Validation Strategy
**Frage:** Plugin-System vs. Monolithische Validierung?  
**Entscheidung:** Plugin-System mit branchen-spezifischen Plugins  
**Begründung:** Erweiterbarkeit für neue Branchen ohne Code-Änderung  
**Datum:** 17.07.2025  

### 5. Database Schema
**Frage:** JSONB vs. Normalized Tables für Field Configs?  
**Entscheidung:** Hybrid - Structured Tables + JSONB für Rules  
**Begründung:** Performance für Queries + Flexibilität für Validation Rules  
**Datum:** 17.07.2025  

---

## ❓ OFFENE FRAGEN (Antwort benötigt!)

### 1. Rule Engine Komplexität
**Frage:** JavaScript Rule Engine sofort oder erst später?  
**Impact:** +2-3 Tage Aufwand, aber maximale Flexibilität  
**Optionen:**
- A) Sofort: Volle Flexibilität von Anfang an
- B) Später: MVP mit Plugin-System, Rule Engine in v2
- C) Hybrid: Einfache Rules sofort, JavaScript später

**Empfehlung:** Option B - Plugin-System reicht für MVP  
**Awaiting Decision:** @joergstreeck  

### 2. Custom Field Types
**Frage:** Wie viele Custom Field Types sollen wir unterstützen?  
**Impact:** Mehr Types = mehr UI-Komponenten + Validation  
**Aktuelle Liste:** text, number, email, date, select, boolean, iban, phone  
**Potentielle Erweiterungen:** file, image, coordinates, json, markdown  

**Empfehlung:** Bei 8 Core Types bleiben für MVP  
**Awaiting Decision:** @joergstreeck  

### 3. Import Session Management
**Frage:** Wie lange sollen Import-Sessions gespeichert werden?  
**Impact:** Database Growth + Cleanup Jobs  
**Optionen:**
- A) 30 Tage: Für Audit und Debugging
- B) 7 Tage: Nur für aktuelle Troubleshooting
- C) 1 Tag: Minimaler Speicher-Footprint

**Empfehlung:** Option A - 30 Tage für Audit-Compliance  
**Awaiting Decision:** @joergstreeck  

### 4. Duplicate Detection Algorithm
**Frage:** Wie aggressiv soll die Duplicate Detection sein?  
**Impact:** False Positives vs. False Negatives  
**Optionen:**
- A) Konservativ: Nur bei sehr hoher Ähnlichkeit (≥95%)
- B) Aggressiv: Bereits bei mittlerer Ähnlichkeit (≥80%)
- C) Konfigurierbar: User kann Threshold einstellen

**Empfehlung:** Option C - Konfigurierbar mit Default 85%  
**Awaiting Decision:** @joergstreeck  

### 5. Performance Optimization
**Frage:** Streaming vs. Batch Processing für große Dateien?  
**Impact:** Memory Usage vs. Processing Speed  
**Aktueller Plan:** Streaming mit 100er Batches  
**Alternative:** Full Batch Processing mit Memory-Monitoring  

**Empfehlung:** Bei Streaming-Ansatz bleiben  
**Awaiting Decision:** @joergstreeck  

---

## 🚨 RISIKEN & MITIGATION

### Risiko 1: Komplexität der Plugin-Architektur
**Beschreibung:** Plugin-System könnte zu komplex für Team werden  
**Wahrscheinlichkeit:** Mittel  
**Impact:** Hoch  
**Mitigation:** 
- Umfassende Dokumentation
- Einfache Plugin-Templates
- Schrittweise Einführung

### Risiko 2: Performance bei großen Dateien
**Beschreibung:** 50.000+ Zeilen könnten System überlasten  
**Wahrscheinlichkeit:** Hoch  
**Impact:** Hoch  
**Mitigation:**
- Streaming Processing
- Configurable Batch Sizes
- Progress Tracking + Cancellation

### Risiko 3: UI-Komplexität durch Dynamic Generation
**Beschreibung:** Generated UI könnte schwer zu debuggen sein  
**Wahrscheinlichkeit:** Mittel  
**Impact:** Mittel  
**Mitigation:**
- Fallback auf Static Components
- Comprehensive Error Handling
- Debug-Modus für Development

### Risiko 4: Migration Aufwand unterschätzt
**Beschreibung:** Legacy-Integration komplexer als gedacht  
**Wahrscheinlichkeit:** Hoch  
**Impact:** Hoch  
**Mitigation:**
- Phasierte Implementation
- Extensive Testing
- Rollback-Plan

---

## 🎯 TECHNICAL DEBT ENTSCHEIDUNGEN

### 1. Initial Implementation Shortcuts
**Entscheidung:** Inline Validation Rules statt externe Rule Engine  
**Begründung:** Schnellere MVP-Delivery  
**Refactoring Plan:** Rule Engine in Phase 2  
**Estimated Effort:** 3-4 Tage später  

### 2. Field Type Limitations
**Entscheidung:** 8 Core Field Types für MVP  
**Begründung:** Reduzierte UI-Komplexität  
**Refactoring Plan:** Custom Field Types in v2  
**Estimated Effort:** 2-3 Tage pro neuem Type  

### 3. Error Handling Simplification
**Entscheidung:** Basic Error Messages ohne i18n  
**Begründung:** Fokus auf Funktionalität  
**Refactoring Plan:** Internationalization später  
**Estimated Effort:** 1-2 Tage  

---

## 📊 IMPACT ASSESSMENT

### Positive Impacts:
- **Flexibilität:** Neue Felder ohne Code-Änderung
- **Wartbarkeit:** Zentrale Konfiguration
- **Skalierbarkeit:** Plugin-System für Branchen
- **User Experience:** Wizard + Expert Mode

### Negative Impacts:
- **Komplexität:** Mehr bewegliche Teile
- **Performance:** Overhead durch Konfiguration
- **Learning Curve:** Team muss neue Architektur verstehen
- **Initial Aufwand:** 10-16 Tage statt 6-9 Tage

### Break-Even Analysis:
- **2. Custom Field:** Configuration-Driven break-even
- **3. Branche:** Plugin-System break-even
- **5. Import-Workflow:** UI-Generation break-even

---

## 🔄 CHANGE LOG

### 2025-07-17
- **14:55:** Decision Log erstellt
- **14:55:** 5 kritische Entscheidungen als offen markiert
- **14:55:** Risiko-Assessment hinzugefügt
- **14:55:** Technical Debt Plan dokumentiert

---

## 📝 NEXT REVIEW: 2025-07-20

**Agenda:**
1. Entscheidungen zu offenen Fragen
2. Phase 1 Implementation Start
3. Plugin-System Design Review
4. Performance Testing Strategy

**Participants:** @joergstreeck, Development Team  
**Expected Duration:** 90 Minuten