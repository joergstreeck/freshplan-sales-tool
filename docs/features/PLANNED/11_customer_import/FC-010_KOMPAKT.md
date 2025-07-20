# 📥 FC-010 CUSTOMER IMPORT & DATA MANAGEMENT (KOMPAKT)

**Erstellt:** 17.07.2025 14:50  
**Status:** 📋 PLANNED  
**Priorität:** HIGH - Kritisch für Migration  
**Aufwand:** 10-16 Tage (flexible Architektur)  

---

## 🧠 WAS WIR BAUEN

**Problem:** 5000+ Bestandskunden in Excel-Listen + starre Import-Logik  
**Lösung:** Configuration-Driven Import mit Plugin-Architektur  
**Warum:** Flexibilität für neue Felder/Branchen ohne Code-Änderungen  

> **Business Case:** Jede neue Kundenfeld-Anfrage kostet aktuell 2-3 Entwicklertage

### 🎯 Geführte Freiheit Prinzipien:
- **Menüführung:** "Daten → Import → Kunden" mit Breadcrumbs
- **Quick Action:** Import-Button direkt in leerer Kundenliste
- **Wizard-Modus:** Schritt-für-Schritt durch den Import
- **Expert-Modus:** Alle Optionen auf einer Seite für Power-User
- **Smart Defaults:** KI-basierte Spalten-Erkennung

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Flexible Schema erstellen:**
```bash
cd backend/src/main/resources/db/migration
touch V4.0__create_import_configuration_tables.sql
# → Details: [Abschnitt "Configuration Schema"](#configuration-schema)
```

### 2. **Field Configuration Entity:**
```bash
cd backend/src/main/java/de/freshplan/domain/import/entity
touch ImportFieldConfig.java
# → Template: [Abschnitt "Entity Templates"](#entity-templates)
```

### 3. **Offene Fragen klären:**
```bash
cat docs/features/PLANNED/FC-010_DECISION_LOG.md
# → 4 kritische Entscheidungen für Jörg
```

**Gesamt: 10-16 Tage Aufwand**

---

## 📋 KRITISCHE FLEXIBILITÄT

- **Neue Felder:** Ohne Code-Änderung → [Details](#field-flexibility)  
- **Branchen-Logic:** Plugin-System → [Details](#business-plugins)  
- **Validation Rules:** Konfigurierbar → [Details](#validation-config)  
- **UI-Anpassung:** Dynamisch generiert → [Details](#dynamic-ui)  

---

## 🔗 VOLLSTÄNDIGE DETAILS

**Implementation Guide:** [FC-010_IMPLEMENTATION_GUIDE.md](./FC-010_IMPLEMENTATION_GUIDE.md)
- [Configuration Schema](#configuration-schema) - 5 neue Tabellen
- [Entity Templates](#entity-templates) - Copy-paste ready
- [Plugin Architecture](#plugin-architecture) - Erweiterbar
- [Dynamic UI Generation](#dynamic-ui) - React Magic
- [Rule Engine Integration](#rule-engine) - Drools ready

**Entscheidungen:** [FC-010_DECISION_LOG.md](./FC-010_DECISION_LOG.md)
- Field-Types erweitern?
- Rule Engine sofort oder später?
- Custom Validation Language?
- Performance vs. Flexibilität?

---

## 🎨 FLEXIBILITÄT FEATURES

### **1. Configuration-Driven Fields**
```json
{
  "id": "companyName",
  "label": "Firmenname", 
  "type": "text",
  "required": true,
  "mappingPatterns": ["firma", "company", "name"],
  "validationRules": [{"type": "minLength", "value": 2}]
}
```

### **2. Plugin-Based Business Logic**
```java
@ApplicationScoped
public class GastronomieValidationPlugin implements CustomValidationPlugin {
    public void validate(ImportData data, ValidationResult result) {
        if ("Gastronomie".equals(data.getIndustry())) {
            validateSeatingCapacity(data, result);
        }
    }
}
```

### **3. Dynamic UI Generation**
```typescript
const DynamicImportWizard = () => {
  const [fieldConfigs] = useFieldConfigs(); // Aus Backend
  
  return (
    <ImportWizard>
      {fieldConfigs.map(config => (
        <DynamicField key={config.id} config={config} />
      ))}
    </ImportWizard>
  );
};
```

---

## 📞 NÄCHSTE SCHRITTE

1. **DECISION_LOG durchgehen** - Jörg's Input needed
2. **Configuration Schema** - 5 neue Tabellen
3. **Field Config Service** - CRUD für Konfiguration
4. **Plugin Architecture** - Erweiterbare Validation
5. **Dynamic UI Components** - React Magic
6. **Rule Engine** - Drools Integration
7. **Legacy Integration** - CustomerService Adapter
8. **Import Pipeline** - Streaming + Batch Processing

**WICHTIG:** Ohne Flexibilität wird jede neue Anforderung zum Entwicklungsaufwand!

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - Import-Berechtigungen
- **[👥 FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_KOMPAKT.md)** - Import-Permissions
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Customer-Datenmodell

### ⚡ Plugin Integration:
- **[🧮 M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)** - Rabattstaffeln importieren
- **[🛡️ FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_KOMPAKT.md)** - Zuordnungen importieren
- **[📥 FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_KOMPAKT.md)** - Xentral-IDs mappen

### 🚀 Ermöglicht folgende Features:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Echte Kundendaten
- **[🔍 FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_KOMPAKT.md)** - Dubletten beim Import
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Import-Events

### 🎨 UI Integration:
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Import-Konfiguration
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Import-Menüpunkt
- **[📊 FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_KOMPAKT.md)** - Import-Statistiken

### 🔧 Technische Details:
- **[FC-010_IMPLEMENTATION_GUIDE.md](./FC-010_IMPLEMENTATION_GUIDE.md)** - Plugin Architecture
- **[FC-010_DECISION_LOG.md](./FC-010_DECISION_LOG.md)** - Architektur-Entscheidungen

---

## 🎯 MIGRATION STRATEGY

### **Phase 1: Core Configuration (3-4 Tage)**
- Database Schema für Field Configs
- Basic CRUD für Konfiguration
- Simple Dynamic UI

### **Phase 2: Plugin System (2-3 Tage)**
- Validation Plugin Architecture
- Business Rule Engine
- Custom Field Types

### **Phase 3: Advanced Features (3-4 Tage)**
- Smart Column Mapping
- Batch Processing Pipeline
- Error Handling & Recovery

### **Phase 4: Integration (2-3 Tage)**
- CustomerService Adapter
- Timeline Integration
- Testing & Performance

**Nach Phase 1:** Basis-Import funktioniert  
**Nach Phase 2:** Erweiterbar für neue Branchen  
**Nach Phase 3:** Production-ready  
**Nach Phase 4:** Vollständig integriert  

---

## 🏆 LANGZEIT-VORTEILE

**Entwicklungszeit-Ersparnis:**
- Neue Felder: 5 Min statt 2-3 Tage
- Neue Branchen-Logic: 30 Min statt 1-2 Tage
- UI-Anpassungen: Automatisch generiert

**Business-Flexibilität:**
- Kunden-spezifische Felder
- Branchen-spezifische Validierung
- Compliance-Anforderungen

**Technical Debt Reduktion:**
- Keine hardcoded Field-Listen
- Erweiterbare Architektur
- Testbare Plugin-System