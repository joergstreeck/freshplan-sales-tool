# 🎯 Audit Timeline Implementation Plan

**Datum:** 09.08.2025  
**Status:** 📋 BEREIT ZUR IMPLEMENTIERUNG  
**Feature:** FC-005 Contact Management UI / Audit Trail Integration

## 📊 Implementierungs-Übersicht

### Was ist bereits implementiert? ✅
1. **Backend Audit System** (PR #78 - MERGED)
   - AuditLog Entity
   - AuditService  
   - AuditRepository
   - Migration V212, V213, V214

2. **Frontend Audit Components** (PR #80 - MERGED)
   - UserAuditTimeline.tsx (379 Zeilen)
   - AuditAdminPage.tsx
   - Dashboard Components (12 Stück)
   
3. **Backend Dashboard Endpoints** (FERTIG)
   - `/api/audit/dashboard/metrics`
   - `/api/audit/dashboard/activity-chart`
   - `/api/audit/dashboard/critical-events`
   - `/api/audit/dashboard/compliance-alerts`

## 🚀 PR 3 - Aktuelle PR Scope

### Was machen wir JETZT in PR 3:

#### 1. CustomerDetailPage erstellen ✅
**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/pages/CustomerDetailPage.tsx`

```typescript
// Neue Komponente mit:
- Tabs für Übersicht, Kontakte, Aktivitäten, Änderungshistorie
- Role-based Visibility für Audit Tab
- Integration der UserAuditTimeline
- Responsive Design mit MUI Grid v2
```

#### 2. Routing einrichten ✅
**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/App.tsx`

```typescript
// Neue Route hinzufügen:
<Route path="/customers/:customerId" element={<CustomerDetailPage />} />
```

#### 3. Navigation von CustomerTable ✅
**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/CustomerTable.tsx`

```typescript
// Click-Handler anpassen:
onRowClick={(customer) => navigate(`/customers/${customer.id}`)}
```

#### 4. UserAuditTimeline einbinden ✅
- Import in CustomerDetailPage
- Als Tab-Content für berechtigte Rollen
- Mit entityType="customer" und entityId={customerId}

#### 5. Basic Tests schreiben ✅
**Neue Test-Dateien:**
- `CustomerDetailPage.test.tsx`
- `UserAuditTimeline.test.tsx` (erweitern)

### Was machen wir NICHT in PR 3 (später):

#### ❌ SmartContactCard Integration
- **Warum nicht:** Separate UX-Verbesserung
- **Wann:** PR 4

#### ❌ FAB mit Drawer
- **Warum nicht:** Sekundäre Priorität
- **Wann:** Sprint 4

#### ❌ Export-Funktionen aktivieren
- **Warum nicht:** Benötigt zusätzliche Security-Checks
- **Wann:** PR 4

#### ❌ Real-time Updates
- **Warum nicht:** WebSocket-Integration ist komplex
- **Wann:** Sprint 4

#### ❌ Performance-Optimierungen (Virtual Scrolling)
- **Warum nicht:** Erst bei größeren Datenmengen nötig
- **Wann:** Nach User-Feedback

## 📝 Code-Struktur für PR 3

### CustomerDetailPage.tsx Struktur:
```typescript
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Tabs, Tab, Typography, Paper } from '@mui/material';
import { 
  Info as InfoIcon,
  Contacts as ContactsIcon,
  Timeline as TimelineIcon,
  Assessment as AssessmentIcon
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useCustomer } from '../features/customer/api/customerQueries';
import { UserAuditTimeline } from '../features/audit/components/UserAuditTimeline';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

export function CustomerDetailPage() {
  const { customerId } = useParams<{ customerId: string }>();
  const [activeTab, setActiveTab] = useState(0);
  const { user } = useAuth();
  const navigate = useNavigate();
  
  // Prüfe Berechtigung für Audit Tab
  const canViewAudit = user?.roles?.some(role => 
    ['admin', 'manager', 'auditor'].includes(role)
  );
  
  const { data: customer, isLoading } = useCustomer(customerId);
  
  if (isLoading) return <LoadingSkeleton />;
  if (!customer) return <NotFound />;
  
  return (
    <MainLayoutV2>
      <Box sx={{ p: 3 }}>
        {/* Customer Header */}
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h4">{customer.companyName}</Typography>
          <Typography variant="subtitle1" color="text.secondary">
            {customer.industry} • {customer.city}
          </Typography>
        </Paper>
        
        {/* Tabs */}
        <Paper>
          <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
            <Tab label="Übersicht" icon={<InfoIcon />} />
            <Tab label="Kontakte" icon={<ContactsIcon />} />
            <Tab label="Aktivitäten" icon={<AssessmentIcon />} />
            {canViewAudit && (
              <Tab label="Änderungshistorie" icon={<TimelineIcon />} />
            )}
          </Tabs>
          
          {/* Tab Panels */}
          <TabPanel value={activeTab} index={0}>
            <CustomerOverview customer={customer} />
          </TabPanel>
          
          <TabPanel value={activeTab} index={1}>
            <CustomerContacts customerId={customerId} />
          </TabPanel>
          
          <TabPanel value={activeTab} index={2}>
            <CustomerActivities customerId={customerId} />
          </TabPanel>
          
          {canViewAudit && (
            <TabPanel value={activeTab} index={3}>
              <UserAuditTimeline
                entityType="customer"
                entityId={customerId}
                showExportButton={user.hasRole(['admin', 'auditor'])}
                maxItems={50}
              />
            </TabPanel>
          )}
        </Paper>
      </Box>
    </MainLayoutV2>
  );
}
```

## ✅ Definition of Done für PR 3

- [ ] CustomerDetailPage.tsx erstellt
- [ ] Routing konfiguriert
- [ ] Navigation von CustomerTable funktioniert
- [ ] UserAuditTimeline wird für berechtigte Rollen angezeigt
- [ ] Basis-Tests geschrieben
- [ ] MUI Grid v2 Syntax verwendet
- [ ] Responsive auf Mobile/Tablet/Desktop
- [ ] Keine Console-Errors
- [ ] Code-Review durchgeführt
- [ ] PR Description vollständig

## 📊 PR 4 - Nächste Schritte (SPÄTER)

### SmartContactCard Integration:
- Accordion mit letzten 5 Änderungen
- Kompakte Timeline-Ansicht
- Progressive Disclosure Pattern

### Performance & Export:
- Virtual Scrolling für große Listen
- CSV/PDF Export für Admins
- Caching-Strategie

## 🔗 Referenzen

### Navigation (absolut):
**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**→ Diskussion:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_TIMELINE_PLACEMENT_DISCUSSION.md`  
**→ Entscheidung:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_TIMELINE_TEAM_DECISION.md`  

### Implementierungs-Dokumente:
**→ Audit System:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_TRAIL_SYSTEM.md`  
**→ Admin Dashboard:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_ADMIN_DASHBOARD.md`  

---

**Status:** Bereit zur Implementierung in PR 3