# FC-009: Contract Renewal Management

**Feature Code:** FC-009  
**Status:** 📋 Planungsphase  
**Geschätzter Aufwand:** 5-7 Tage  
**Priorität:** HOCH - Kritisch für FreshPlan-Partnerschaftsmodell  
**Erstellt:** 24.07.2025  

## 🎯 Zusammenfassung

Automatisiertes Management der jährlichen FreshPlan-Partnerschaftsvereinbarungen mit Renewal-Workflow, Preisindex-Monitoring und Xentral-Integration. Das Feature stellt sicher, dass keine Rabatte ohne gültige Vereinbarung gewährt werden.

## 📚 Verwandte Dokumente

- **Business Context:** [/Users/joergstreeck/freshplan-sales-tool/docs/business/freshplan_summary.md](/Users/joergstreeck/freshplan-sales-tool/docs/business/freshplan_summary.md)
- **Abhängiges Feature:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md)
- **Integration:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-xentral-integration.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-xentral-integration.md)
- **Offene Fragen:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/OPEN_QUESTIONS_TRACKER.md#10-contract-renewal-management-fc-009-🆕](/Users/joergstreeck/freshplan-sales-tool/docs/features/OPEN_QUESTIONS_TRACKER.md#10-contract-renewal-management-fc-009-🆕)

## 💼 Business-Anforderungen

### Geschäftliche Rahmenbedingungen:
- **Vertragslaufzeit:** 12 Monate mit automatischer Verlängerung
- **Kündigungsfrist:** 3 Monate zum Laufzeitende
- **Vertragsebene:** Zentral pro Kunde (nicht pro Filiale)
- **Preisanpassung:** Bei Verbraucherpreisindex > 5% möglich
- **Rückwirkende Renewals:** Erlaubt, aber mit aktuellen Listenpreisen

### Prozess-Anforderungen:
1. Transparente Renewal-Verwaltung im Kanban Board
2. Automatische Erinnerungen und Eskalationen
3. Manuelle Preiskommunikation durch Vertrieb
4. Xentral-Synchronisation für Rabattstatus

## 🏗️ Technische Umsetzung

Die technische Implementierung ist aufgeteilt in:

### 📄 Detail-Dokumente:

1. **Backend-Architektur:** [./FC-009/backend-architecture.md](./FC-009/backend-architecture.md)
   - Contract Monitoring Entity
   - State Machine
   - Scheduled Jobs
   - Event-System

2. **Frontend-Komponenten:** [./FC-009/frontend-components.md](./FC-009/frontend-components.md)
   - RENEWAL Kanban-Spalte
   - Contract Badges
   - Quick Actions
   - Monitoring Dashboard

3. **Xentral-Integration:** [./FC-009/xentral-integration.md](./FC-009/xentral-integration.md)
   - Event Payloads
   - API Endpoints
   - Sync-Strategie
   - Fehlerbehandlung

4. **Business-Workflows:** [./FC-009/business-workflows.md](./FC-009/business-workflows.md)
   - Eskalations-Kette
   - Lapsed Renewal Prozess
   - Preisindex-Monitoring
   - Audit-Trail

## 🔗 Auswirkungen auf andere Features

### Direkte Abhängigkeiten:
- **M4 Opportunity Pipeline:** 7. Spalte + neue API Endpoints
- **FC-005 Xentral:** Contract Status Events
- **FC-003 E-Mail:** Renewal-Templates
- **M11 Reporting:** Neue KPIs
- **FC-012 Audit Trail:** Alle Contract-Änderungen werden auditiert
- **FC-015 Rechte & Rollen:** Contract-Approval Workflows für Verlängerungen

### Detail-Analyse:
Siehe [./FC-009/impact-analysis.md](./FC-009/impact-analysis.md)

## 📊 Metriken & Erfolg

- **Renewal Rate:** > 85% Ziel
- **Rechtzeitigkeit:** 95% vor Ablauf bearbeitet
- **Revenue at Risk:** < 5% des Gesamtumsatzes
- **Sync-Erfolg:** 99.9% mit Xentral

## 🚀 Implementierungsplan

1. **Phase 1:** Backend-Grundlagen (2 Tage)
2. **Phase 2:** Frontend-Integration (2 Tage)
3. **Phase 3:** Xentral-Anbindung (2 Tage)
4. **Phase 4:** Testing & Rollout (1 Tag)

Details: [./FC-009/implementation-plan.md](./FC-009/implementation-plan.md)

## ✅ Definition of Done

- [ ] RENEWAL Stage funktioniert
- [ ] Auto-Move bei < 90 Tagen
- [ ] Xentral-Sync läuft
- [ ] Eskalationen greifen
- [ ] Audit Trail integriert (@Auditable)
- [ ] Tests > 90% Coverage
- [ ] Dokumentation komplett