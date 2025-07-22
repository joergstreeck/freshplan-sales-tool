# 📋 PLANNED FEATURES - Roadmap Overview

**Status:** Geplant für zukünftige Sprints  
**Priorisierung:** Nach Geschäftswert sortiert  

## 🗂️ Feature-Übersicht

### Phase 1: Data Foundation (Nach ACTIVE Features)

#### FC-010: Customer Import & Data Management 📥
- **Aufwand:** 10-16 Tage (flexible Architektur)
- **Priorität:** ⭐ KRITISCH für Migration
- **Nutzen:** 5000+ Bestandskunden importieren, erweiterbare Import-Logik
- **Status:** BEREIT FÜR IMPLEMENTATION
- **Abhängigkeiten:** Benötigt FC-008 (Security) + FC-009 (Permissions)
- **ROI:** Break-even nach 2. Custom Field, massive Zeitersparnis

### Phase 2: Core Sales Process

#### FC-004: Verkäuferschutz-System 🛡️
- **Aufwand:** 2 Tage
- **Priorität:** ⭐ KRITISCH
- **Nutzen:** Faire Provisionsverteilung, keine Konflikte
- **Status:** Design-Phase

#### FC-003: E-Mail Integration 📧
- **Aufwand:** 5 Tage
- **Priorität:** HOCH
- **Nutzen:** Zentrale Kommunikation, keine verlorenen Mails
- **Status:** Technisches Konzept steht

#### M5: Customer Management Refactoring 👥
- **Aufwand:** 3 Tage
- **Priorität:** MITTEL
- **Nutzen:** Performance, moderne UI
- **Status:** Legacy-Code analysiert

### Phase 2: Analytics & Intelligence

#### FC-007: Chef-Dashboard 📊
- **Aufwand:** 4 Tage
- **Priorität:** HOCH für Führung
- **Nutzen:** Echtzeit-Überblick, KPIs
- **Status:** Requirements gesammelt

#### M6: Embedded Analytics 📈
- **Aufwand:** 5 Tage
- **Priorität:** MITTEL
- **Nutzen:** Datengetriebene Entscheidungen
- **Status:** Tool-Evaluation läuft

### Phase 3: Externe Integrationen

#### FC-005: Xentral Integration 🔗
- **Aufwand:** 5 Tage
- **Priorität:** MITTEL
- **Nutzen:** Automatisierter Datenfluss
- **Status:** API-Analyse pending

### Phase 4: Mobile & Future

#### FC-006: Mobile Companion App 📱
- **Aufwand:** 7 Tage
- **Priorität:** NIEDRIG (vorerst)
- **Nutzen:** Verkäufer unterwegs
- **Status:** Konzept-Phase

## 🔗 Quick Links

- [Complete Feature Roadmap](../2025-07-12_COMPLETE_FEATURE_ROADMAP.md)
- [V5 Master Plan](../../CRM_COMPLETE_MASTER_PLAN_V5.md)
- [Business Priorities](../BUSINESS_PRIORITIES.md)

## 📊 Entscheidungsmatrix

| Feature | Business Impact | Effort | Risk | Empfehlung |
|---------|----------------|--------|------|------------|
| FC-010 Customer Import | 🔴 Kritisch | 10-16d | Medium | NEXT! (nach FC-008) |
| FC-004 Verkäuferschutz | 🔴 Kritisch | 2d | Low | Sprint 2 |
| FC-003 E-Mail | 🟡 Hoch | 5d | Medium | Sprint 2 |
| FC-007 Dashboard | 🟡 Hoch | 4d | Low | Sprint 3 |
| M5 Customer | 🟢 Mittel | 3d | Low | Sprint 3 |
| FC-005 Xentral | 🟢 Mittel | 5d | High | Sprint 4 |
| M6 Analytics | 🟢 Mittel | 5d | Medium | Sprint 4 |
| FC-006 Mobile | 🔵 Nice | 7d | Medium | Q2 2025 |

## 🚀 Nächste Schritte

1. **ACTIVE Features** abschließen (FC-008 Security + FC-009 Permissions)
2. **FC-010** als erstes PLANNED Feature implementieren (Daten-Foundation)
3. **FC-004** Verkäuferschutz in Sprint 2 priorisieren
4. **FC-003 & FC-007** parallel in Sprint 2/3
5. **Quarterly Review** für weitere Priorisierung

### 🔗 FC-010 Quick Links
- **Konzept:** [FC-010_TECH_CONCEPT.md](./FC-010_TECH_CONCEPT.md) ⭐
- **Implementation:** [FC-010_IMPLEMENTATION_GUIDE.md](./FC-010_IMPLEMENTATION_GUIDE.md)
- **Entscheidungen:** [FC-010_DECISION_LOG.md](./FC-010_DECISION_LOG.md)
- **Aufwand:** 10-16 Tage für flexible Architektur
- **ROI:** Break-even nach 2. Custom Field

---

**Hinweis:** Diese Planung wird regelmäßig mit dem Product Owner abgestimmt und kann sich basierend auf Markt-Feedback ändern.