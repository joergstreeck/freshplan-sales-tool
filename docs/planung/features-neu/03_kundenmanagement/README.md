# 👥 Kundenmanagement - CRM Core Module

**📊 Modul Status:** 🔄 In Entwicklung
**🎯 Owner:** Development Team + Product Team
**📱 Sidebar Position:** Kundenmanagement (Hauptbereich)
**🔗 Related Modules:** 01_mein-cockpit, 04_auswertungen, 02_neukundengewinnung

## 🎯 Modul-Übersicht

Das Kundenmanagement ist das Herzstück des CRM-Systems. Hier werden alle kundenbezogenen Funktionen zusammengefasst: von der Kundenliste über die Neuanlage bis hin zu Verkaufschancen und Aktivitäten-Timeline.

## 🗂️ Submodule

- **alle-kunden/**: Kundenliste mit Field-Based Architecture (FC-005 Migration)
- **neuer-kunde/**: Kunde-Erfassungsformular mit Validierung
- **verkaufschancen/**: Sales Pipeline (M4 Implementation - bereits funktional)
- **aktivitaeten/**: Activity Timeline und Notes System (FC-013 Migration)

## 🔗 Dependencies

### Frontend Components
- CustomerList (React Query + MUI DataGrid)
- CustomerForm (React Hook Form + Validation)
- PipelineBoard (Drag & Drop + Status Management)
- ActivityTimeline (Virtualized List + Real-time Updates)

### Backend Services
- CustomerService (CRUD + Search + Export)
- OpportunityService (Pipeline Management + Workflow)
- ActivityService (Notes + Timeline + Audit Trail)
- CustomerRepository (JPA + Custom Queries)

### External APIs
- Xentral Integration (Customer Sync)
- E-Mail Service (Activity Tracking)
- Calendar Integration (Meeting Scheduling)

## 🚀 Quick Start für Entwickler

1. **Frontend-Entwicklung:** Starte mit `npm run dev` und navigiere zu `/customers`
2. **Backend-APIs:** Alle Endpoints unter `/api/customers`, `/api/opportunities`, `/api/activities`
3. **Database:** Customer-, Opportunity- und Activity-Tables in PostgreSQL
4. **Testing:** Unit Tests in `/backend/src/test/java/de/freshplan/customer/`
5. **Dokumentation:** Tech-Konzepte in den jeweiligen Submodul-Ordnern

## 🤖 Claude Notes

- **Aktiver Bereich:** M4 Pipeline ist bereits implementiert und funktional
- **Migration:** FC-005 Customer Management wird gerade in alle-kunden/ migriert
- **Nächste Steps:** FC-013 Activity Notes System in aktivitaeten/ integrieren
- **Integration:** Eng verzahnt mit Neukundengewinnung (Lead → Customer Conversion)
- **Performance:** Field-Based Architecture für dynamische Kundenfelder implementiert