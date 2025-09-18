# 🏠 Mein Cockpit - FreshFoodz Sales Dashboard

**📊 Status:** ✅ Technical Concept erstellt - Ready für Implementation
**🎯 Owner:** Development Team + Product Team
**📱 Position:** Hauptmodul (Cockpit = Herzstück des CRM)
**🔗 Dependencies:** 02_neukundengewinnung, 03_kundenmanagement

---

## 🎯 **Was ist das Cockpit?**

**Das zentrale Multi-Channel-B2B-Dashboard für FreshFoodz Cook&Fresh® Genussberater.**

3-Spalten-Layout für produktive Verkaufsprozesse:
- **Spalte 1:** Genussberater-Tag (Sample-Tests, ROI-Termine, FreshFoodz-KPIs)
- **Spalte 2:** Multi-Channel-Pipeline (Direct/Partner-Filter, Account-Übersicht)
- **Spalte 3:** Account-Intelligence (ROI-Calculator, Channel-Info, Quick Actions)

---

## ✅ **Aktueller Stand (Code-Analyse 18.09.2025):**

### **🚀 Production-Ready Basis:**
- **SalesCockpitV2.tsx:** 3-Spalten MUI-Layout funktional
- **ResizablePanels:** Professional drag-and-drop mit localStorage
- **Backend CQRS:** SalesCockpitQueryService optimiert (19/19 Tests grün)
- **Industry-Enum:** HOTEL, RESTAURANT, CATERING, KANTINE = 1:1 FreshFoodz-Match

### **🔧 FreshFoodz-Gap (4-5 Wochen Aufwand):**
- **Multi-Channel:** Keine ChannelType (DIRECT/PARTNER) Unterscheidung
- **ROI-Calculator:** Komplett fehlend (Legacy unbrauchbar)
- **Sample-Management:** Keine Cook&Fresh®-spezifischen Workflows
- **FreshFoodz-KPIs:** Generische CRM-Metriken statt Gastronomy-Sales

---

## 📋 **Dokumentation:**

### **📖 Haupt-Planung:**
- **[technical-concept.md](./technical-concept.md)** ← **MAIN DOCUMENT** (4-Phasen-Roadmap)

### **💬 Entscheidungshistorie:**
- **[diskussionen/](./diskussionen/)** ← ChatGPT + Claude Strategiediskussionen
- **[freshfoodz-gap-analyse.md](./diskussionen/2025-09-18_freshfoodz-gap-analyse.md)** ← Code vs. Business-Anforderungen

### **🔮 Visionäre Features:**
- **[zukunft/](./zukunft/)** ← Features mit unklaren Business-Requirements
- **[seasonal-opportunities.md](./zukunft/seasonal-opportunities.md)** ← Saisonale Lead-Detection
- **[advanced-roi-features.md](./zukunft/advanced-roi-features.md)** ← Erweiterte ROI-Kalkulationen
- **[partner-performance-tracking.md](./zukunft/partner-performance-tracking.md)** ← Partner-Channel-Performance

---

## 🛠️ **Implementation-Roadmap:**

### **Phase 1: Multi-Channel Foundation (2-3 Wochen)**
- Customer Entity um ChannelType erweitern
- FocusListColumnMUI um Direct/Partner-Filter
- MyDayColumnMUI für FreshFoodz-KPIs anpassen

### **Phase 2: ROI-Calculator Integration (2 Wochen)**
- ROI-Calculator-Modal entwickeln
- Integration in ActionCenterColumnMUI
- Cook&Fresh®-spezifische ROI-Logik

### **Phase 3: Advanced Dashboard-Features (1-2 Wochen)**
- Channel-Intelligence erweitern
- Performance-Optimierung
- Integration zu anderen Modulen

### **Phase 4: Production-Ready (1 Woche)**
- Testing, Polish, Performance-Tuning
- SmartLayout-Pilot-Integration

**Gesamtaufwand:** 6-8 Wochen (50% Einsparung durch solide Basis!)

---

## 🚀 **Warum das Cockpit das Herzstück ist:**

### **Strategische Bedeutung:**
- **Zentrale Schaltzentrale** für alle FreshFoodz Genussberater
- **Multi-Channel-Koordination** (Direct Sales + Partner Channel)
- **ROI-Beratungstools** für Cook&Fresh® Produktverkauf
- **Performance-Dashboard** für Gastronomy-Sales-KPIs

### **Technische Basis:**
- **90% Infrastructure bereits da** → Fokus auf Geschäftslogik!
- **Backend CQRS optimiert** → Performance bereits gelöst
- **3-Spalten-Layout perfekt** für FreshFoodz Genussberater-Workflow

---

## 🔗 **Nächste Schritte:**

1. **Phase 1 starten:** ChannelType + Multi-Channel-Filter implementieren
2. **Business-Alignment:** ROI-Calculator-Requirements mit Jörg finalisieren
3. **Integration-Planning:** 02_neukundengewinnung + 03_kundenmanagement koordinieren

**Das Cockpit wird zum FreshFoodz Cook&Fresh® Sales Command Center! 🍃🚀**