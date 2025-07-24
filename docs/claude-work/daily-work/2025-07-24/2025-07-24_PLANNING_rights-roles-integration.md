# FC-015 Rechte- und Rollenkonzept - Integration & Auswirkungen

**Datum:** 24.07.2025 23:00  
**Status:** ✅ Vollständig geplant mit allen Detail-Dokumenten

## 📋 Zusammenfassung

FC-015 erweitert die bestehende Security Foundation (FC-008) um feingranulare Berechtigungen, konfigurierbare Rollen, Vertretungsregelungen und Freigabeprozesse. Das Konzept hat weitreichende Auswirkungen auf alle bestehenden Features.

## 🔄 Erstellte Dokumente

### Hauptkonzept
- `/docs/features/2025-07-24_TECH_CONCEPT_FC-015-rights-roles-concept.md`

### Detail-Dokumente (alle erstellt ✅)
1. `/docs/features/FC-015/permission-system.md` - Feingranulare Permission Implementation
2. `/docs/features/FC-015/delegation-vertretung.md` - Vertretungsregeln & Zeitsteuerung
3. `/docs/features/FC-015/approval-workflows.md` - Freigabe-Prozesse & Eskalation
4. `/docs/features/FC-015/permission-management-ui.md` - Admin UI für Rollenverwaltung

## 🎯 Kern-Features

### 1. Feingranulare Permissions
- Spezifische Berechtigungen wie `opportunity.change_stage`, `contract.approve`
- Permission-basierte Annotations: `@RequiresPermission`
- Kontext-basierte Permissions mit Conditions

### 2. Vertretungsregelungen
- Zeitbasierte Delegationen für Urlaub/Krankheit
- FULL, PARTIAL oder SPECIFIC Delegation Types
- Automatische Aktivierung/Deaktivierung
- Outlook-Integration für OOO-Sync

### 3. Approval Workflows
- Flexible Freigabeprozesse für kritische Aktionen
- Multi-Level Approvals mit Eskalation
- Vordefinierte Workflows (Discount, Contract Renewal, **Bonitätsprüfung**)
- Zeitbasierte Eskalation

### 4. Bonitätsprüfungs-Workflow (NEU) 🏦
- Integration mit Xentral für Zahlungshistorie
- Externe APIs (Schufa/Creditreform)
- 4-stufiger Approval-Prozess:
  - Level 1: Automatische Prüfung (Credit Score ≥ 700)
  - Level 2: Sales Manager (Score 500-699)
  - Level 3: Finance Manager (Score < 500 oder hohe Limits)
  - Level 4: C-Level (sehr hohes Risiko)
- Auto-Reject bei Score < 300

### 5. Management UI
- Role Editor mit Permission Tree
- User Permission Overview
- Delegation Management
- Permission Analytics Dashboard

## 📊 Technische Auswirkungen auf andere Features

### M4 Opportunity Pipeline ✅
**Datei aktualisiert:** `/docs/features/2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md`
- Permission Check für Stage-Wechsel (`opportunity.change_stage`)
- Spezielle Permission für Deal-Abschluss (`opportunity.close_deal`)
- Integration in OpportunityStageValidator

### FC-009 Contract Renewal ✅
**Datei aktualisiert:** `/docs/features/2025-07-24_TECH_CONCEPT_FC-009-contract-renewal-management.md`
- Contract-Approval Workflows für Verlängerungen
- Neue Abhängigkeit dokumentiert

### FC-004 Verkäuferschutz ✅
**Datei aktualisiert:** `/docs/features/FC-004-verkaeuferschutz.md`
- Permission für Customer-Claim (`customer.claim`)
- @RequiresPermission Annotation hinzugefügt

### FC-012 Audit Trail ✅
**Datei aktualisiert:** `/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md`
- Neue Event Types für Permission-System:
  - PERMISSION_CHECK, PERMISSION_DENIED
  - ROLE_ASSIGNED, ROLE_REMOVED
  - DELEGATION_CREATED, DELEGATION_ACTIVATED
  - APPROVAL_REQUESTED, APPROVAL_DECISION

### FC-005 Xentral Integration ✅
**Datei aktualisiert:** `/docs/features/FC-005-xentral-integration.md`
- Permission für Sync-Trigger (`sync.trigger`)
- Scheduled Jobs benötigen Permissions

## 🔗 Weitere betroffene Features (noch zu aktualisieren)

### FC-003 E-Mail Integration
- E-Mail-Versand Permissions
- Template-Management Permissions

### FC-007 Chef-Dashboard
- Spezielle Executive-Permissions
- Keine Delegation möglich für kritische Metriken

### FC-010 Pipeline Scalability
- Bulk-Action Permissions
- Filter-Speicherung nur mit Permission

### FC-011 Pipeline-Cockpit
- Dashboard-Konfiguration Permissions
- Metriken-Zugriff nach Rolle

### FC-013 Activity & Notes
- Activity-Creation Permissions
- Private vs. Public Notes

### FC-014 Mobile/Tablet
- Mobile-spezifische Permission Checks
- Offline Permission Caching

## 💡 Wichtige Architektur-Entscheidungen

### 1. Permission Caching
- 5 Minuten Cache für User Permissions
- Caffeine Cache für Performance
- Invalidierung bei Änderungen

### 2. Non-Delegatable Permissions
```java
Set<String> NON_DELEGATABLE = Set.of(
    "system.manage_users",
    "system.manage_roles",
    "system.view_audit_log"
);
```

### 3. Breaking Changes
- Migration von @RolesAllowed zu @RequiresPermission
- Neue SecurityContextProvider Methoden
- UserPrincipal erweitert um Permission-Cache

### 4. Workflow Engine
- Flexible Definition von Approval-Prozessen
- Auto-Approve Thresholds
- Escalation Timeouts
- Notification Integration

## 🔐 Security Considerations

1. **Keine Delegation Chains** - Verhindert Sicherheitslücken
2. **Zwei-Faktor für Admin UI** - Kritische Aktionen geschützt
3. **IP-Whitelist für Admin** - Zusätzliche Absicherung
4. **Vollständiges Audit Trail** - Jede Permission-Nutzung geloggt

## 📈 Performance Impact

- Permission Checks: < 5ms durch Caching
- Minimal Impact auf bestehende Features
- Batch-Permission-Checks für Bulk Operations

## 🚀 Nächste Schritte

1. **FC-012 Audit Trail implementieren** (KRITISCH - Basis für FC-015)
2. **FC-015 Phase 1:** Permission System Backend (2 Tage)
3. **FC-015 Phase 2:** Delegation System (1 Tag)
4. **FC-015 Phase 3:** Approval Workflows (2 Tage)
5. **FC-015 Phase 4:** Management UI (1 Tag)
6. **Migration:** Bestehende @RolesAllowed zu @RequiresPermission

## 📝 Offene Fragen

1. **Performance bei vielen Permissions** - Load Tests nötig
2. **Mobile Offline Permissions** - Sync-Strategie definieren
3. **External User Permissions** - Gäste/Partner-Zugriff
4. **Workflow Customization** - Wie flexibel soll es sein?

## ✅ Definition of Done für FC-015

- [ ] Permission System funktioniert
- [ ] Delegation aktiviert sich automatisch
- [ ] Approval Workflows greifen
- [ ] Management UI komplett
- [ ] Migration Guide erstellt
- [ ] Performance < 5ms
- [ ] Tests > 90% Coverage
- [ ] Alle Features integriert