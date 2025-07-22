# 🎯 FC-009: Rollenbasierte Rechteverwaltung - Menüpunkt-basierte Permissions

**Feature Code:** FC-009  
**Erstellt:** 16.07.2025  
**Status:** 📋 Konzept & Planung  
**Priorität:** HIGH - Blockiert alle rollenspezifischen Features  

## 🚀 Vision

**"Ein Tool, ein Einstieg - maximale Flexibilität durch intelligente Rechteverwaltung"**

Implementierung einer menüpunkt-basierten Rechteverwaltung mit inhärenten Capabilities, die geführte Workflows und rollenspezifische Features nahtlos integriert.

## 🎯 Kernprinzipien

### 1. Menüpunkt = Permission Boundary
- **Ein Permission-Check pro Menüpunkt**
- **Capabilities definieren verfügbare Features**
- **Guided Processes passen sich automatisch an Rolle an**

### 2. Geführte Freiheit bleibt erhalten
- **Workflows führen User basierend auf ihren Rechten**
- **Features werden ein-/ausgeblendet statt blockiert**
- **Kontext-sensitive Hilfe je nach Erfahrungslevel**

### 3. Entwickler-freundliche Architektur
- **Einfache Permission-Checks**
- **Skalierbare Erweiterung**
- **Klare Trennung zwischen UI und Business Logic**

## 🏗️ Technische Architektur

### Permission-Model
```typescript
interface MenuPermission {
  menuItem: string;           // "kundenverwaltung", "benutzerverwaltung"
  capabilities: string[];     // ["read", "write", "export", "advanced_insights"]
  guidedProcesses: string[];  // ["basic_workflow", "advanced_workflow"]
  uiLevel: "basic" | "advanced" | "expert"; // UI-Komplexität
}

interface UserRole {
  name: string;
  permissions: MenuPermission[];
  description: string;
}
```

### Rolle-Definition
```typescript
const ROLES: Record<string, UserRole> = {
  sales: {
    name: "Sales Representative",
    permissions: [
      {
        menuItem: "kundenverwaltung",
        capabilities: ["read", "write", "call_log"],
        guidedProcesses: ["basic_sales_workflow"],
        uiLevel: "basic"
      }
    ]
  },
  
  manager: {
    name: "Sales Manager", 
    permissions: [
      {
        menuItem: "kundenverwaltung",
        capabilities: ["read", "write", "export", "bonitaet", "advanced_reports"],
        guidedProcesses: ["basic_sales_workflow", "manager_insights"],
        uiLevel: "advanced"
      },
      {
        menuItem: "benutzerverwaltung",
        capabilities: ["read", "write", "team_management"],
        guidedProcesses: ["team_setup"],
        uiLevel: "advanced"
      }
    ]
  },
  
  admin: {
    name: "System Administrator",
    permissions: [
      {
        menuItem: "kundenverwaltung", 
        capabilities: ["read", "write", "export", "bonitaet", "advanced_reports", "data_migration"],
        guidedProcesses: ["basic_sales_workflow", "manager_insights", "admin_tools"],
        uiLevel: "expert"
      },
      {
        menuItem: "benutzerverwaltung",
        capabilities: ["read", "write", "team_management", "role_assignment", "system_config"],
        guidedProcesses: ["team_setup", "role_management"],
        uiLevel: "expert"
      },
      {
        menuItem: "system_administration",
        capabilities: ["api_test", "logs", "monitoring", "backup"],
        guidedProcesses: ["system_maintenance"],
        uiLevel: "expert"
      }
    ]
  }
};
```

## 🎨 UI/UX Implementation

### Sidebar-Navigation
```typescript
// Nur erlaubte Menüpunkte anzeigen
const SidebarMenu = () => {
  const { userRole } = useAuth();
  const allowedMenus = userRole.permissions.map(p => p.menuItem);
  
  return (
    <Sidebar>
      {MENU_ITEMS
        .filter(item => allowedMenus.includes(item.id))
        .map(item => <MenuItem key={item.id} {...item} />)
      }
    </Sidebar>
  );
};
```

### Feature-basierte Komponenten
```typescript
const CustomerActions = () => {
  const { hasCapability } = usePermissions("kundenverwaltung");
  
  return (
    <ActionPanel>
      {hasCapability("write") && <EditButton />}
      {hasCapability("export") && <ExportButton />}
      {hasCapability("bonitaet") && <CreditCheckButton />}
      {hasCapability("advanced_reports") && <AdvancedReportsButton />}
    </ActionPanel>
  );
};
```

### Guided Workflows
```typescript
const CustomerWorkflow = () => {
  const { getGuidedProcesses, getUILevel } = usePermissions("kundenverwaltung");
  const processes = getGuidedProcesses();
  const uiLevel = getUILevel();
  
  return (
    <WorkflowContainer complexity={uiLevel}>
      {processes.includes("basic_sales_workflow") && <BasicSalesSteps />}
      {processes.includes("manager_insights") && <ManagerInsights />}
      {processes.includes("admin_tools") && <AdminTools />}
    </WorkflowContainer>
  );
};
```

## 🔧 Implementation Plan

### Phase 1: Foundation (3 Tage)
**Ziel:** Permission-System-Grundlage

#### Tag 1: Backend Permission Framework
- [ ] `PermissionService` erstellen
- [ ] Role-Definitionen in Database/Config
- [ ] JWT Token um Rollen erweitern
- [ ] Permission-Middleware für API-Endpoints

#### Tag 2: Frontend Permission Context
- [ ] `PermissionContext` und Hooks erstellen
- [ ] `usePermissions(menuItem)` Hook
- [ ] `ProtectedRoute` Komponente
- [ ] Permission-Utils für UI-Checks

#### Tag 3: Sidebar-Integration
- [ ] Dynamische Menü-Generierung
- [ ] Role-basierte Menü-Filterung
- [ ] Navigation-Guards implementieren
- [ ] Tests für Permission-System

### Phase 2: Feature Integration (4 Tage)
**Ziel:** Bestehende Features rollenbasiert machen

#### Tag 4-5: Kundenverwaltung umstellen
- [ ] Capability-Checks in Customer-Komponenten
- [ ] Export-Feature nur für entsprechende Rollen
- [ ] Advanced-Features ausblenden/einblenden
- [ ] Workflow-Anpassungen implementieren

#### Tag 6-7: Benutzerverwaltung implementieren
- [ ] User-Management-Komponenten erstellen
- [ ] Role-Assignment-Interface (nur Admin)
- [ ] Team-Management für Manager
- [ ] Bulk-Operations nach Capabilities

### Phase 3: Advanced Features (3 Tage)
**Ziel:** Guided Processes & UI-Level

#### Tag 8: Guided Workflows
- [ ] Workflow-Engine implementieren
- [ ] Role-basierte Prozess-Definitionen
- [ ] Context-sensitive Hilfen
- [ ] Progress-Tracking

#### Tag 9: UI-Level Adaptierung
- [ ] Basic/Advanced/Expert UI-Modi
- [ ] Progressive Disclosure
- [ ] Tooltips und Onboarding je Level
- [ ] Keyboard-Shortcuts für Experten

#### Tag 10: Testing & Polish
- [ ] Umfassende Permission-Tests
- [ ] Role-Switching im Dev-Mode
- [ ] Performance-Optimierung
- [ ] Dokumentation vervollständigen

## 🧪 Test-Strategie

### Permission-Tests
```typescript
describe('Permission System', () => {
  it('should show only allowed menu items for sales role', () => {
    renderWithRole('sales');
    expect(screen.getByText('Kundenverwaltung')).toBeInTheDocument();
    expect(screen.queryByText('Benutzerverwaltung')).not.toBeInTheDocument();
  });
  
  it('should enable export for manager role', () => {
    renderCustomerPageWithRole('manager');
    expect(screen.getByRole('button', { name: 'Export' })).toBeEnabled();
  });
  
  it('should show advanced workflow for admin', () => {
    renderWithRole('admin');
    expect(screen.getByText('Admin Tools')).toBeInTheDocument();
  });
});
```

### Integration-Tests
- [ ] Role-basierte API-Zugriffe
- [ ] Menu-Navigation zwischen Rollen
- [ ] Feature-Verfügbarkeit nach Login
- [ ] Workflow-Durchläufe pro Rolle

## 📊 Success Metrics

### Entwickler-Experience
- **Permission-Check-Zeit:** < 1ms
- **Setup-Zeit für neue Rolle:** < 30 Min
- **Code-Zeilen für Permission:** < 5 pro Feature

### User-Experience  
- **Menü-Load-Zeit:** < 100ms
- **Feature-Discovery:** 90% finden relevante Features in < 30s
- **Workflow-Completion:** 20% schneller durch Guided Processes

### System-Performance
- **Memory-Overhead:** < 50MB für Permission-System
- **Database-Queries:** Keine zusätzlichen Queries pro Request
- **Bundle-Size:** < 20KB für Permission-Logic

## 🔄 Migration Strategy

### Bestehende Features
1. **Backwards Compatibility:** Alle Features bleiben funktional
2. **Gradual Rollout:** Feature-für-Feature umstellen
3. **Fallback-Mode:** Bei Permission-Fehlern → Standard-Verhalten

### Deployment
1. **Role-Definitionen:** Als JSON-Config deploybar
2. **Feature-Flags:** Schrittweise Aktivierung
3. **Monitoring:** Permission-Usage-Analytics

## 📚 Related Documents

- **Master Plan:** `/docs/CRM_COMPLETE_MASTER_PLAN_V5.md`
- **Security Foundation:** `/docs/features/ACTIVE/01_security_foundation/README.md`
- **UI/UX Konzept:** Frontend-Architektur-Sektion
- **Implementation Plan:** Dieser Plan wird als FC-009 in Master Plan integriert

## 🎯 Next Steps

1. **Master Plan Update:** FC-009 als High-Priority Feature hinzufügen
2. **Security Foundation:** Permission-System als Teil der Auth-Integration
3. **Landing Page Elimination:** Nach Permission-System implementiert
4. **Team Review:** Konzept mit Jörg besprechen und finalisieren

---

**Dieser Plan ist VERBINDLICH und wird als FC-009 in alle relevanten Planungsdokumente integriert.**