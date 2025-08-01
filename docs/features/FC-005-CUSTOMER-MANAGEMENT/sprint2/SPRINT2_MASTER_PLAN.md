# 🎯 Sprint 2 Master Plan - Contact Management CRUD

**Feature:** FC-005 Customer Management  
**Sprint:** Sprint 2 - Contact Management CRUD Implementation  
**Datum:** 31.07.2025  
**Status:** 📋 GEPLANT  
**Dauer:** 2 Wochen  

## 📌 Executive Summary

Sprint 2 implementiert **Multi-Contact Support** mit einer **pragmatischen CRUD-Architektur**. Fokus auf Business Value statt Architektur-Komplexität - einfach, wartbar und sofort nutzbar.

**Kernziele:**
- ✅ Multi-Contact Support mit Beziehungsebene
- ✅ JPA/Hibernate Standard-Architektur
- ✅ REST API für Contact Management
- ✅ Responsive Multi-Contact UI
- ✅ Location Assignment für Filialkunden

## 🏗️ Architektur-Foundation

### CRUD als Kern
```
Frontend (React) → REST API → JPA Service → PostgreSQL
                       ↓
              Hibernate Envers (Audit)
                       ↓
              Standard Logging
```

**Details:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CONTACT_MANAGEMENT_VISION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CONTACT_MANAGEMENT_VISION.md)

## 📅 Wochenplan

### Woche 1: Event Foundation (5.-9. August)
**Ziel:** Event Store Setup + Basic Multi-Contact UI

| Tag | Backend | Frontend |
|-----|---------|----------|
| Mo | Event Store Schema + Base Events | Store für Contact Array |
| Di | Contact Aggregate + Soft Delete | ContactCard Component |
| Mi | Basic Projections (List, Detail) | ContactForm Component |
| Do | Event Versioning + Migration | Multi-Contact Layout mit Theme |
| Fr | Integration Tests | UI Tests + Review |

**WICHTIG:** Step 3 nutzt die gleiche adaptive Theme-Architektur wie Step 1 & 2:
- `CustomerFieldThemeProvider` für konsistentes Design
- `AdaptiveFormContainer` für responsive Layouts
- `DynamicFieldRenderer` für einheitliche Felddarstellung

**Deliverables:**
- ✅ Funktionierender Event Store
- ✅ Contact CRUD mit Events
- ✅ Multi-Contact UI Grundgerüst

**Details:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK1_EVENT_SOURCING.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK1_EVENT_SOURCING.md)

### Woche 2: Features + Compliance (12.-16. August)
**Ziel:** Consent Management + Mobile Actions + Location Assignment

| Tag | Backend | Frontend |
|-----|---------|----------|
| Mo | Consent Events + Storage | Consent UI Components |
| Di | Location Assignment Logic | Location Dropdown |
| Mi | Mobile Action Hub API | Quick Actions UI |
| Do | Offline Queue Backend | Service Worker Setup |
| Fr | DSGVO Export API | Export UI + Tests |

**Deliverables:**
- ✅ DSGVO-konformes Consent Management
- ✅ Location-basierte Kontaktzuordnung
- ✅ Mobile-Ready Actions
- ✅ Offline Queue Basis

**Details:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK2_FEATURES_COMPLIANCE.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK2_FEATURES_COMPLIANCE.md)

### Woche 3: Relationship + Analytics (19.-23. August)
**Ziel:** Beziehungsebene + Warmth Indicator + Analytics Events

| Tag | Backend | Frontend |
|-----|---------|----------|
| Mo | Relationship Fields Migration | Relationship Form Fields |
| Di | Warmth Calculator Service | Warmth Indicator UI |
| Mi | Analytics Event Types | Timeline Component |
| Do | Location Transition Events | Transition Visualisierung |
| Fr | Smart Projections Setup | Performance Optimierung |

**Deliverables:**
- ✅ Vollständige Beziehungsebene
- ✅ Relationship Warmth Indicator
- ✅ Analytics-Ready Events
- ✅ Performance-optimierte Projections

**Details:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK3_RELATIONSHIP_ANALYTICS.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK3_RELATIONSHIP_ANALYTICS.md)

### Woche 4: Polish + Integration (26.-30. August)
**Ziel:** Integration Tests + Performance + Dokumentation

| Tag | Fokus |
|-----|-------|
| Mo | FC-012 Audit Trail Integration |
| Di | FC-018 DSGVO Compliance Verifikation |
| Mi | Performance Tests + Optimierung |
| Do | E2E Tests + Bug Fixes |
| Fr | Dokumentation + Deployment |

**Deliverables:**
- ✅ Vollständige Integration mit FC-012 & FC-018
- ✅ Performance innerhalb SLAs
- ✅ Komplette Test Coverage
- ✅ Production-Ready

**Details:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK4_POLISH_INTEGRATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK4_POLISH_INTEGRATION.md)

## 🔗 Integration Points

### FC-012 Audit Trail
- Alle Contact Events → Audit Log
- Event Metadata für Compliance
- User Attribution automatisch

**Integration Guide:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/INTEGRATION_FC012_AUDIT_TRAIL.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/INTEGRATION_FC012_AUDIT_TRAIL.md)

### FC-018 DSGVO Compliance
- Consent Management integriert
- Crypto-Shredding vorbereitet
- Export API implementiert

**Integration Guide:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/INTEGRATION_FC018_DSGVO.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/INTEGRATION_FC018_DSGVO.md)

## 📊 Metriken & KPIs

### Sprint-Metriken
- Story Points: 40
- Test Coverage: ≥ 80%
- Performance: < 200ms API Response
- Mobile Score: ≥ 90 Lighthouse

### Business-Metriken
- Kontakte pro Kunde: Avg 3-5
- Beziehungsdaten-Vollständigkeit: > 60%
- Mobile Usage: > 40%

## 🚨 Risiken & Mitigationen

| Risiko | Impact | Mitigation |
|--------|--------|------------|
| Event Store Performance | Hoch | Projection Strategy + Archivierung |
| DSGVO Komplexität | Mittel | Frühe Juristische Prüfung |
| Mobile Offline Sync | Mittel | Conflict Resolution Patterns |

## 📚 Referenz-Dokumente

### Architektur
- [Contact Management Vision](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CONTACT_MANAGEMENT_VISION.md)
- [Event Sourcing Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/architecture/EVENT_SOURCING_FOUNDATION.md)
- [GDPR Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/architecture/GDPR_COMPLIANCE_ARCHITECTURE.md)

### Guides
- [Event Migration Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/guides/EVENT_MIGRATION_GUIDE.md)
- [Conflict Resolution Patterns](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/guides/CONFLICT_RESOLUTION_PATTERNS.md)
- [GDPR Compliance Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/guides/GDPR_COMPLIANCE_GUIDE.md)

## ✅ Definition of Done

- [ ] Alle User Stories implementiert
- [ ] Test Coverage ≥ 80%
- [ ] Performance SLAs erfüllt
- [ ] Mobile-First verifiziert
- [ ] DSGVO-Compliance bestätigt
- [ ] Audit Trail Integration getestet
- [ ] Dokumentation vollständig
- [ ] Code Review abgeschlossen

---

**Nächste Schritte:** 
1. Review mit Team
2. Story Points Schätzung
3. Sprint Planning Session

**Navigation:**
- [← Zurück zu FC-005 README](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
- [→ Weiter zu Woche 1 Details](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK1_EVENT_SOURCING.md)