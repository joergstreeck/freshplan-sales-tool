# 📍 FC-005 PR5: Navigation & Platform Architecture
**Die zentrale Plattform-Struktur für alle Features**

**Status:** 🚧 In Planung  
**Datum:** 10.08.2025  
**Author:** Claude & Jörg  
**Priorität:** ⭐⭐⭐⭐⭐ KRITISCH - Basis für alle weiteren Features

## 🎯 Executive Summary

PR5 etabliert die **zentrale Navigations- und Plattform-Architektur** als Fundament für alle aktuellen und zukünftigen Features. Diese Struktur implementiert:
- Rollenbasierte Navigation mit "Geführter Freiheit"
- Modulare Feature-Integration
- Granulare Rechteverwaltung
- Konsistente User Experience

## 📚 Zentrale Dokumenten-Verweise

### Übergeordnete Dokumente:
- [🏠 CLAUDE.md](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md) - Arbeitsrichtlinien
- [📋 CRM Master Plan V5](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)
- [🎯 Vision & Roadmap](/Users/joergstreeck/freshplan-sales-tool/VISION_AND_ROADMAP.md)
- [🔧 Way of Working](/Users/joergstreeck/freshplan-sales-tool/WAY_OF_WORKING.md)

### Feature-Konzepte:
- [FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/FC-005_TECH_CONCEPT.md)
- [FC-002 Sales Cockpit](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-002-SALES-COCKPIT/FC-002_TECH_CONCEPT.md)
- [FC-003 Audit System](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-003-AUDIT-SYSTEM/FC-003_TECH_CONCEPT.md)

## 🏗️ Die Plattform-Architektur

### Navigation Hierarchie:

```
FreshPlan Platform
├── 🔍 Header (Global)
│   ├── Global Search
│   ├── Notifications
│   ├── User Profile
│   └── Help/Support
│
└── 📱 Main Navigation (Sidebar)
    ├── 1. Mein Arbeitsplatz
    ├── 2. Vertrieb
    ├── 3. Insights
    └── 4. Administration
```

## 📋 Detaillierte Navigations-Struktur

### 1. 🏠 **Mein Arbeitsplatz** 
**Rollen:** `alle`  
**Beschreibung:** Personalisierter Einstiegspunkt für jeden Nutzer

#### 1.1 Mein Cockpit
- **Status:** ✅ Implementiert als [SalesCockpitV2](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/cockpit/components/SalesCockpitV2.tsx)
- **Features:** 3-Spalten Layout (MyDay, FocusList, ActionCenter)
- **Siehe:** [FC-002 Sales Cockpit Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-002-SALES-COCKPIT/FC-002_TECH_CONCEPT.md)

#### 1.2 Meine Aufgaben
- **Status:** 🔄 Teilweise (in MyDayColumn)
- **Geplant:** Dedizierte Task-Management Seite
- **Features:** Priorisierung, Delegation, Deadlines

#### 1.3 Meine Termine
- **Status:** 📅 Geplant für Q2/2025
- **Integration:** Outlook/Google Calendar
- **Features:** Meeting-Vorbereitung, Kundenhistorie

#### 1.4 Schnellzugriffe
- **Status:** 🆕 NEU in PR5
- **Features:** User-definierte Favoriten, Recent Items
- **Persistierung:** localStorage + Backend-Sync

### 2. 🎯 **Vertrieb**
**Rollen:** `sales`, `manager`, `admin`  
**Beschreibung:** Alle verkaufsrelevanten Funktionen

#### 2.1 Pipeline Management

##### 2.1.1 Verkaufschancen
- **Status:** ✅ Basis implementiert
- **Route:** `/opportunities`
- **Features:** Kanban-Board, Pipeline-Stages
- **Geplant:** [FC-016 Opportunity Scoring](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-016-OPPORTUNITY-SCORING/FC-016_TECH_CONCEPT.md)

##### 2.1.2 Deal-Tracking
- **Status:** 📅 Q2/2025
- **Features:** Win/Loss Analysis, Competitor Tracking

##### 2.1.3 Forecast
- **Status:** 📅 Q3/2025
- **Integration:** KI-basierte Vorhersagen
- **Siehe:** [FC-017 AI Predictions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-017-AI-PREDICTIONS/FC-017_TECH_CONCEPT.md)

#### 2.2 Kundenbetreuung

##### 2.2.1 Alle Kunden
- **Status:** ✅ Implementiert
- **Route:** `/customers`
- **Features:** [IntelligentFilterBar](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/filter/IntelligentFilterBar.tsx), [Virtual Scrolling](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/VirtualizedCustomerTable.tsx)
- **Siehe:** [FC-005 Step3 PR4](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PR4_ENHANCED_FEATURES.md)

##### 2.2.2 Neuer Kunde
- **Status:** ✅ Basis implementiert
- **Route:** `/customers/new`
- **Features:** Multi-Step Wizard, Duplikat-Check
- **Geplant:** Auto-Complete aus Handelsregister

##### 2.2.3 Aktivitäten
- **Status:** ✅ Timeline implementiert
- **Features:** [Activity Timeline](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/timeline/ActivityTimeline.tsx)
- **Geplant:** Bulk-Actions, Templates

##### 2.2.4 Kontakte & Beziehungen
- **Status:** ✅ SmartContactCards
- **Features:** [Contact Management](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/contacts/SmartContactCard.tsx)
- **Siehe:** [FC-005 Step2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step2/CONTACT_MANAGEMENT_SPEC.md)

#### 2.3 Lead Generation

##### 2.3.1 Lead-Erfassung
- **Status:** 📅 Q2/2025
- **Features:** Web-Forms, QR-Codes, Import-Wizard
- **Integration:** LinkedIn, XING

##### 2.3.2 Kampagnen
- **Status:** 📅 Q3/2025
- **Features:** Multi-Channel, A/B Testing
- **Integration:** Mailchimp, HubSpot

##### 2.3.3 E-Mail Integration
- **Status:** 📅 Q2/2025
- **Features:** Outlook/Gmail Integration
- **Siehe:** [FC-014 Email Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-014-EMAIL-INTEGRATION/FC-014_TECH_CONCEPT.md)

### 3. 📊 **Insights**
**Rollen:** `sales` (eigene), `manager` (team), `admin` (alle)  
**Beschreibung:** Datengetriebene Entscheidungen

#### 3.1 Dashboard
- **Status:** 🔄 Basis in SalesCockpitV2
- **Geplant:** Interaktive KPI-Dashboards
- **Features:** Drill-Down, Custom Widgets

#### 3.2 Berichte

##### 3.2.1 Umsatzanalyse
- **Status:** 📅 Q1/2025
- **Features:** YoY Comparison, Trends
- **Export:** [Universal Export Framework](/Users/joergstreeck/freshplan-sales-tool/docs/features/EXPORT_SOLUTION_UPDATE.md)

##### 3.2.2 Kundenentwicklung
- **Status:** 📅 Q2/2025
- **Features:** Churn-Analysis, LTV
- **KI:** Predictive Analytics

##### 3.2.3 Team-Performance
- **Status:** 📅 Q2/2025
- **Features:** Leaderboards, Aktivitäts-Metriken
- **Gamification:** Badges, Achievements

#### 3.3 Prognosen
- **Status:** 📅 Q3/2025
- **Features:** ML-basierte Vorhersagen
- **Integration:** TensorFlow.js

### 4. ⚙️ **Administration**
**Rollen:** `manager` (teilweise), `admin` (voll), `auditor` (audit)  
**Beschreibung:** System-Verwaltung und Kontrolle

#### 4.1 Benutzerverwaltung
- **Status:** ✅ Implementiert
- **Route:** `/admin/users`
- **Features:** [UserTable](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/users/components/UserTableMUI.tsx), Rollen-Zuweisung
- **Auth:** Keycloak Integration

#### 4.2 Audit & Compliance
- **Status:** ✅ Implementiert
- **Route:** `/admin/audit`
- **Features:** [Audit Dashboard](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/admin/AuditDashboard.tsx)
- **Siehe:** [FC-003 Audit System](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-003-AUDIT-SYSTEM/FC-003_TECH_CONCEPT.md)

##### 4.2.1 Audit Dashboard
- Real-time Monitoring
- User Activity Tracking
- System Events

##### 4.2.2 Sicherheitsereignisse
- Failed Login Attempts
- Permission Violations
- Data Access Logs

##### 4.2.3 Compliance Reports
- DSGVO/GDPR Reports
- Data Retention
- Access Control Matrix

#### 4.3 Systemkonfiguration

##### 4.3.1 Workflows
- **Status:** 📅 Q3/2025
- **Features:** Visual Workflow Builder
- **Engine:** Camunda/Activiti

##### 4.3.2 Feldanpassungen
- **Status:** 📅 Q2/2025
- **Features:** Custom Fields, Validierungen
- **UI:** Drag & Drop Field Builder

##### 4.3.3 E-Mail Templates
- **Status:** 📅 Q2/2025
- **Features:** MJML-basierte Templates
- **Variables:** Mustache/Handlebars

##### 4.3.4 API & Integrationen
- **Status:** 🔄 Teilweise
- **Features:** Webhook-Management, OAuth2
- **Dokumentation:** Swagger/OpenAPI

## 🔐 Rechteverwaltung-Matrix

### Rollen-Definition:

```typescript
enum Role {
  SALES = 'sales',        // Vertriebsmitarbeiter
  MANAGER = 'manager',    // Team-Leiter
  ADMIN = 'admin',        // System-Administrator
  AUDITOR = 'auditor',    // Compliance/Audit
  VIEWER = 'viewer'       // Nur-Lese-Zugriff
}
```

### Berechtigungs-Matrix:

| Bereich | Sales | Manager | Admin | Auditor | Viewer |
|---------|-------|---------|-------|---------|--------|
| **Mein Arbeitsplatz** |
| - Mein Cockpit | ✅ | ✅ | ✅ | ✅ | ✅ |
| - Eigene Aufgaben | ✅ | ✅ | ✅ | ✅ | 👁️ |
| - Team-Aufgaben | ❌ | ✅ | ✅ | 👁️ | ❌ |
| **Vertrieb** |
| - Eigene Kunden | ✅ | ✅ | ✅ | 👁️ | 👁️ |
| - Alle Kunden | 👁️ | ✅ | ✅ | 👁️ | 👁️ |
| - Kunde anlegen | ✅ | ✅ | ✅ | ❌ | ❌ |
| - Kunde löschen | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Insights** |
| - Eigene Reports | ✅ | ✅ | ✅ | ✅ | 👁️ |
| - Team Reports | ❌ | ✅ | ✅ | ✅ | ❌ |
| - Globale Reports | ❌ | ❌ | ✅ | ✅ | ❌ |
| **Administration** |
| - Benutzerverwaltung | ❌ | 👁️ | ✅ | ❌ | ❌ |
| - Audit Dashboard | ❌ | 👁️ | ✅ | ✅ | ❌ |
| - System-Konfiguration | ❌ | ❌ | ✅ | ❌ | ❌ |

**Legende:** ✅ Vollzugriff | 👁️ Nur Lesen | ❌ Kein Zugriff

### Granulare Berechtigungen:

```typescript
interface Permission {
  resource: string;           // z.B. 'customer'
  action: Action;             // CRUD operations
  scope: Scope;              // own|team|all
  conditions?: Condition[];   // Zusätzliche Bedingungen
}

type Action = 'create' | 'read' | 'update' | 'delete';
type Scope = 'own' | 'team' | 'all';

interface Condition {
  field: string;
  operator: 'eq' | 'ne' | 'gt' | 'lt' | 'in' | 'contains';
  value: any;
}

// Beispiel: Sales kann nur eigene aktive Kunden bearbeiten
const salesCustomerPermission: Permission = {
  resource: 'customer',
  action: 'update',
  scope: 'own',
  conditions: [
    { field: 'status', operator: 'eq', value: 'active' },
    { field: 'locked', operator: 'eq', value: false }
  ]
};
```

## 🚀 Implementierungs-Roadmap

### Phase 1: Foundation (Sprint 0-1) ✅
- [x] Basis-Navigation
- [x] Rollen-System (Keycloak)
- [x] Sales Cockpit
- [x] Customer Management

### Phase 2: PR5 Core (JETZT)
- [ ] Navigation-Struktur implementieren
- [ ] Rechteverwaltung-Service
- [ ] Schnellzugriffe/Favoriten
- [ ] Navigation Guards
- [ ] Breadcrumbs

### Phase 3: Enhancement (Q1/2025)
- [ ] Progressive Disclosure
- [ ] Contextual Help
- [ ] Keyboard Shortcuts
- [ ] Search Enhancement

### Phase 4: Intelligence (Q2/2025)
- [ ] Smart Assistants
- [ ] Predictive Navigation
- [ ] Usage Analytics
- [ ] A/B Testing Framework

## 💻 Technische Implementierung

### Frontend-Komponenten:

```typescript
// Navigation Service
class NavigationService {
  private menuItems: MenuItem[];
  private userPermissions: Permission[];
  
  getVisibleMenuItems(user: User): MenuItem[] {
    return this.menuItems.filter(item => 
      this.hasPermission(user, item.requiredPermission)
    );
  }
  
  hasPermission(user: User, permission: Permission): boolean {
    // Komplexe Permissions-Logik
  }
}

// Navigation Component
const MainNavigation: React.FC = () => {
  const { user } = useAuth();
  const navigation = useNavigation();
  const visibleItems = navigation.getVisibleItems(user);
  
  return (
    <Sidebar>
      {visibleItems.map(section => (
        <NavSection key={section.id}>
          <NavSectionTitle>{section.title}</NavSectionTitle>
          {section.items.map(item => (
            <NavItem 
              key={item.id}
              to={item.path}
              icon={item.icon}
              badge={item.badge}
            >
              {item.label}
            </NavItem>
          ))}
        </NavSection>
      ))}
    </Sidebar>
  );
};
```

### Backend-Services:

```java
@ApplicationScoped
public class NavigationService {
    @Inject
    PermissionService permissionService;
    
    public List<MenuItem> getNavigationForUser(String userId) {
        User user = userService.findById(userId);
        List<Permission> permissions = permissionService.getPermissionsForUser(user);
        
        return menuItems.stream()
            .filter(item -> hasAccess(item, permissions))
            .collect(Collectors.toList());
    }
}

@Entity
public class UserFavorite {
    @Id
    private UUID id;
    
    @ManyToOne
    private User user;
    
    private String menuItemId;
    private Integer position;
    private LocalDateTime addedAt;
}
```

## 📊 Metriken & KPIs

### Navigation Efficiency:
- **Click Depth:** Durchschnittliche Klicks zum Ziel
- **Time to Task:** Zeit bis Aufgabe erledigt
- **Error Rate:** Fehlklicks/Sackgassen

### Feature Adoption:
- **Discovery Rate:** Neue Features entdeckt
- **Usage Frequency:** Nutzungshäufigkeit pro Feature
- **Retention:** Wiederkehrende Nutzung

### User Satisfaction:
- **NPS:** Net Promoter Score
- **CSAT:** Customer Satisfaction Score
- **Support Tickets:** Navigation-bezogene Tickets

## 🔗 Verwandte Dokumente

### Implementierungs-Details:
- [Navigation Component](/Users/joergstreeck/freshplan-sales-tool/frontend/src/components/navigation/MainNavigation.tsx) *(zu erstellen)*
- [Permission Service](/Users/joergstreeck/freshplan-sales-tool/backend/src/main/java/de/freshplan/services/PermissionService.java) *(zu erstellen)*
- [Navigation Guards](/Users/joergstreeck/freshplan-sales-tool/frontend/src/guards/NavigationGuard.tsx) *(zu erstellen)*

### Feature-Integrationen:
- [FC-002 Sales Cockpit](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-002-SALES-COCKPIT/FC-002_TECH_CONCEPT.md)
- [FC-003 Audit System](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-003-AUDIT-SYSTEM/FC-003_TECH_CONCEPT.md)
- [FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/FC-005_TECH_CONCEPT.md)

### Standards & Guidelines:
- [Frontend Standards](/Users/joergstreeck/freshplan-sales-tool/docs/technical/FRONTEND_BACKEND_SPECIFICATION.md)
- [Security Guidelines](/Users/joergstreeck/freshplan-sales-tool/docs/guides/SECURITY_GUIDELINES.md)
- [UX Principles](/Users/joergstreeck/freshplan-sales-tool/docs/guides/UX_PRINCIPLES.md)

## ✅ Definition of Done

- [ ] Navigation-Struktur implementiert
- [ ] Alle Rollen-Permissions definiert
- [ ] Navigation Guards aktiv
- [ ] Favoriten-System funktioniert
- [ ] Breadcrumbs implementiert
- [ ] Mobile-responsive
- [ ] Keyboard-Navigation
- [ ] A11y-compliant
- [ ] Unit-Tests (>80%)
- [ ] E2E-Tests für kritische Pfade
- [ ] Performance < 100ms Render
- [ ] Dokumentation komplett

## 📝 Notizen

Diese Plattform-Architektur ist das **Herzstück** unseres Systems. Alle Features müssen sich hier einsortieren und die definierten Standards befolgen. Bei Änderungen an der Navigation-Struktur ist ein Architecture Decision Record (ADR) erforderlich.

---

**Nächster Schritt:** [PR5 Implementation Tasks](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-NAVIGATION-PLATFORM/PR5_IMPLEMENTATION_TASKS.md)