# 🔐 Administration - System Management Module

**📊 Modul Status:** 🟢 Teilweise Produktiv
**🎯 Owner:** DevOps Team + Development Team
**📱 Sidebar Position:** Administration (Admin-Bereich)
**🔗 Related Modules:** 06_einstellungen, 07_hilfe-support, 00_shared

## 🎯 Modul-Übersicht

Das Administration-Modul enthält alle system-kritischen Funktionen für Administratoren: von Audit-Logs über Benutzerverwaltung bis hin zu externen Integrationen und Compliance-Reports.

## 🗂️ Submodule

- **audit-dashboard/**: Audit Trail System (FC-012 - bereits funktional ✅)
- **benutzerverwaltung/**: User Management (bereits funktional ✅)
- **system/**: System-Monitoring und -Verwaltung
  - **api-status/**: API Health Checks (bereits funktional ✅)
  - **system-logs/**: Log-Management-Interface
  - **performance/**: Performance-Monitoring-Dashboard
  - **backup-recovery/**: Backup-Management-System
- **integration/**: Externe System-Integrationen
  - **ki-anbindungen/**: AI/ML Service-Connections
  - **xentral/**: Xentral ERP Integration
  - **email-service/**: E-Mail-Provider-Management
  - **payment-provider/**: Payment-Gateway-Configuration
  - **webhooks/**: Webhook-Management-System
  - **neue-integration/**: Integration-Framework
- **hilfe-konfiguration/**: Help-System-Configuration
  - **hilfe-system-demo/**: Help Demo Interface (bereits funktional ✅)
  - **tooltips-verwalten/**: Tooltip-Management
  - **touren-erstellen/**: Onboarding-Tour-Builder
  - **analytics/**: Help-System-Analytics
- **compliance-reports/**: DSGVO & Compliance (FC-018 Migration)

## 🔗 Dependencies

### Frontend Components
- AdminDashboard (Overview + Quick Actions)
- AuditLogViewer (Data Grid + Filtering)
- UserManagement (CRUD + Role Assignment)
- SystemMonitoring (Real-time Charts + Alerts)
- IntegrationManager (Configuration Forms + Testing)

### Backend Services
- AuditService (Logging + Search + Export)
- UserManagementService (CRUD + Security)
- SystemMonitoringService (Health Checks + Metrics)
- IntegrationService (External API Management)
- ComplianceService (DSGVO + Reporting)

### External APIs
- Keycloak (User Management + SSO)
- System Monitoring APIs (Prometheus/Grafana)
- External Integration Endpoints
- Compliance & Legal APIs

## 🚀 Quick Start für Entwickler

1. **Admin-Access:** Benötigt ADMIN-Rolle in Keycloak
2. **Frontend:** Navigate zu `/admin` für Dashboard
3. **Audit-Logs:** `/admin/audit` für FC-012 Audit Trail System
4. **API-Status:** `/admin/system/api-status` für System-Health
5. **Help-Demo:** `/admin/help-config/demo` für Help-System-Tests

## 🤖 Claude Notes

- **Produktive Features:** Audit Dashboard, User Management, API Status, Help Demo
- **Security-Critical:** Alle Admin-Features erfordern spezielle Berechtigungen
- **Integration-Hub:** Zentrale Stelle für alle externen System-Verbindungen
- **Compliance:** FC-018 DSGVO wird hier in compliance-reports/ migriert
- **Monitoring:** Performance und System-Health sind hier zentralisiert
- **Help-System:** Erweiterte Konfigurations-Möglichkeiten für Onboarding