# FC-015: Erweiterte Rechte- und Rollenkonzept - Technisches Konzept

**Feature Code:** FC-015  
**Status:** 🟡 KONZEPT  
**Priorität:** HIGH (Compliance & Governance)  
**Geschätzter Aufwand:** 5-6 Tage  
**Basis:** FC-008 Security Foundation ✅

## 📋 Übersicht

Erweiterung der bestehenden Security Foundation (FC-008) um feingranulare Berechtigungen, konfigurierbare Rollen, Vertretungsregelungen und Freigabeprozesse für Enterprise-Governance.

## 🎯 Kernfunktionalität

### Feature-Set
1. **Feingranulare Permissions** (z.B. opportunity.move, contract.approve)
2. **Konfigurierbare Rollen** mit Permission-Sets
3. **Vertretungsregelungen** für Urlaub/Krankheit
4. **Freigabe-Workflows** für kritische Aktionen (inkl. Bonitätsprüfung)
5. **Permission Management UI** für Administratoren
6. **Bonitätsprüfungs-Workflow** mit Xentral & externen APIs

## 🔗 Abhängigkeiten & Integration

### Basis
- **FC-008 Security Foundation:** UserPrincipal erweitern

### Betroffene Features (ALLE!)
- **M4 Pipeline:** Permissions für Stage-Wechsel
- **FC-009 Renewal:** Contract-Approval-Workflow
- **FC-004 Verkäuferschutz:** Owner-Änderung nur mit Permission
- **FC-012 Audit:** Alle Permission-Checks auditieren
- **FC-005 Xentral:** Sync-Permissions & Zahlungshistorie für Bonitätsprüfung
- **M5 Customer:** Bonitätsprüfung & Kreditlimit-Verwaltung

## 📐 Architektur

### Permission Model
```typescript
// Feingranulare Permissions
enum Permission {
  // Opportunity Permissions
  OPPORTUNITY_VIEW = 'opportunity.view',
  OPPORTUNITY_CREATE = 'opportunity.create',
  OPPORTUNITY_EDIT = 'opportunity.edit',
  OPPORTUNITY_DELETE = 'opportunity.delete',
  OPPORTUNITY_CHANGE_STAGE = 'opportunity.change_stage',
  OPPORTUNITY_CHANGE_OWNER = 'opportunity.change_owner',
  
  // Contract Permissions
  CONTRACT_VIEW = 'contract.view',
  CONTRACT_APPROVE = 'contract.approve',
  CONTRACT_RENEW = 'contract.renew',
  CONTRACT_TERMINATE = 'contract.terminate',
  
  // Discount Permissions
  DISCOUNT_VIEW = 'discount.view',
  DISCOUNT_CREATE = 'discount.create',
  DISCOUNT_APPROVE_10 = 'discount.approve.10',
  DISCOUNT_APPROVE_20 = 'discount.approve.20',
  DISCOUNT_APPROVE_UNLIMITED = 'discount.approve.unlimited',
  
  // System Permissions
  SYNC_TRIGGER = 'sync.trigger',
  AUDIT_VIEW = 'audit.view',
  AUDIT_EXPORT = 'audit.export',
  USER_MANAGE = 'user.manage',
  ROLE_MANAGE = 'role.manage',
  
  // Email Permissions (FC-003)
  EMAIL_VIEW_ALL = 'email.view.all',
  EMAIL_VIEW_OWN = 'email.view.own',
  EMAIL_SEND = 'email.send',
  EMAIL_TEMPLATE_CREATE = 'email.template.create',
  EMAIL_TEMPLATE_EDIT = 'email.template.edit',
  EMAIL_TEMPLATE_DELETE = 'email.template.delete',
  EMAIL_TRACKING_VIEW = 'email.tracking.view',
  EMAIL_BULK_SEND = 'email.bulk.send'
}
```

### Role Configuration
```typescript
interface Role {
  id: string;
  name: string;
  description: string;
  permissions: Permission[];
  isSystem: boolean; // System-Rollen nicht editierbar
  maxDiscount?: number; // Rolle-spezifische Limits
}

// Vordefinierte Rollen
const SYSTEM_ROLES = {
  ADMIN: {
    name: 'Administrator',
    permissions: ['*'], // Alle Permissions
    isSystem: true
  },
  SALES_MANAGER: {
    name: 'Vertriebsleiter',
    permissions: [
      'opportunity.*',
      'contract.*',
      'discount.approve.20',
      'audit.view'
    ],
    maxDiscount: 20
  },
  SALES_REP: {
    name: 'Vertriebsmitarbeiter',
    permissions: [
      'opportunity.view',
      'opportunity.create',
      'opportunity.edit',
      'opportunity.change_stage',
      'contract.view',
      'discount.create'
    ],
    maxDiscount: 10
  }
};
```

## 📚 Detail-Dokumente

1. **Permission System:** [./FC-015/permission-system.md](./FC-015/permission-system.md)
2. **Delegation Model:** [./FC-015/delegation-vertretung.md](./FC-015/delegation-vertretung.md)
3. **Approval Workflows:** [./FC-015/approval-workflows.md](./FC-015/approval-workflows.md)
4. **Management UI:** [./FC-015/permission-management-ui.md](./FC-015/permission-management-ui.md)

## 🚀 Implementierungs-Phasen

### Phase 1: Permission System (2 Tage)
- Permission Entity & Repository
- Role-Permission Mapping
- UserPrincipal Extension
- Permission Check Service

### Phase 2: Delegation System (1 Tag)
- Delegation Entity
- Zeitbasierte Vertretungen
- Auto-Aktivierung/Deaktivierung
- Delegation UI

### Phase 3: Approval Workflows (2 Tage)
- Approval Request Entity
- Workflow Engine
- Notification Integration
- Escalation Logic

### Phase 4: Management UI (1 Tag)
- Role Management
- Permission Assignment
- Delegation Overview
- Audit Integration

## ⚠️ Technische Herausforderungen

1. **Performance bei Permission-Checks**
2. **Caching von User-Permissions**
3. **Konflikt-Resolution bei Delegationen**
4. **Workflow-State Management**
5. **Migration bestehender Rollen**

## 📊 Messbare Erfolge

- **Permission Check Performance:** < 5ms
- **Role Assignment Time:** < 30 Sekunden
- **Delegation Activation:** Automatisch zur definierten Zeit
- **Compliance Rate:** 100% auditierte Permission-Checks

## 🔄 Updates & Status

**24.07.2025:** Initiales Konzept basierend auf Enterprise-Anforderungen