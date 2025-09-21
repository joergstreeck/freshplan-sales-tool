# 🎯 FAB mit Drawer Integration

**Feature:** FC-005 Step3 - Contact Management UI  
**Status:** 📋 GEPLANT für Sprint 4  
**Priorität:** 🥉 Priorität 5  
**Geschätzter Aufwand:** 6-8 Stunden  

## 🧭 Navigation

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_EXPORT_FEATURES.md`  
**← Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**← Implementation Plan:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_TIMELINE_IMPLEMENTATION_PLAN.md`  
**← Performance:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PERFORMANCE_OPTIMIZATIONS.md`  

## 🎯 Vision

Ein globaler Floating Action Button (FAB) ermöglicht Power-Usern und Admins den schnellen Zugriff auf Audit-Informationen von jeder Stelle der Anwendung. Der FAB ist context-aware und zeigt automatisch relevante Audit-Daten basierend auf der aktuellen Seite.

## 📊 Feature-Übersicht

### Context-Aware FAB
- **Intelligente Erkennung:** Erkennt automatisch aktuelle Entity (Customer, Contact, etc.)
- **Adaptive Anzeige:** Zeigt relevante Timeline oder globale Historie
- **Quick Access:** Ein Klick zu allen Audit-Informationen
- **Unaufdringlich:** Versteckt sich beim Scrollen, erscheint bei Bedarf

### Drawer mit Timeline
- **Seitlicher Drawer:** 600px breit, von rechts einfahrend
- **Vollständige Timeline:** Nicht nur kompakte Ansicht
- **Filter & Suche:** Direkt im Drawer verfügbar
- **Export-Funktionen:** Für berechtigte User

## 🏗️ Technische Implementierung

### 1. Context Detection Service

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/services/auditContextService.ts`

```typescript
import { useLocation, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';

export interface AuditContext {
  entityType?: string;
  entityId?: string;
  userId?: string;
  contextLabel: string;
  hasContext: boolean;
}

/**
 * Service zur Erkennung des aktuellen Audit-Kontexts
 */
class AuditContextService {
  /**
   * Analysiert die aktuelle URL und extrahiert Entity-Informationen
   */
  detectContext(pathname: string, params: Record<string, string>): AuditContext {
    // Customer Detail Page
    if (pathname.match(/^\/customers\/[^\/]+$/)) {
      return {
        entityType: 'customer',
        entityId: params.customerId,
        contextLabel: 'Kunde',
        hasContext: true
      };
    }
    
    // Contact Detail Page
    if (pathname.match(/^\/contacts\/[^\/]+$/)) {
      return {
        entityType: 'contact',
        entityId: params.contactId,
        contextLabel: 'Kontakt',
        hasContext: true
      };
    }
    
    // Customer List mit Filter
    if (pathname === '/customers' && params.filter) {
      return {
        entityType: 'customer',
        contextLabel: 'Kundenliste (gefiltert)',
        hasContext: true
      };
    }
    
    // User Profile
    if (pathname.match(/^\/profile/)) {
      return {
        entityType: 'user',
        userId: params.userId || 'current',
        contextLabel: 'Benutzerprofil',
        hasContext: true
      };
    }
    
    // Dashboard
    if (pathname === '/dashboard' || pathname === '/') {
      return {
        contextLabel: 'Dashboard',
        hasContext: false
      };
    }
    
    // Settings
    if (pathname.match(/^\/settings/)) {
      return {
        entityType: 'settings',
        contextLabel: 'Einstellungen',
        hasContext: true
      };
    }
    
    // Default: Keine spezifische Entity
    return {
      contextLabel: 'Allgemein',
      hasContext: false
    };
  }
  
  /**
   * Erweiterte Kontext-Informationen laden
   */
  async enrichContext(context: AuditContext): Promise<AuditContext> {
    if (!context.hasContext) return context;
    
    try {
      // Lade zusätzliche Informationen basierend auf Entity-Typ
      if (context.entityType === 'customer' && context.entityId) {
        const customer = await this.loadCustomerInfo(context.entityId);
        context.contextLabel = `Kunde: ${customer.name}`;
      }
      
      if (context.entityType === 'contact' && context.entityId) {
        const contact = await this.loadContactInfo(context.entityId);
        context.contextLabel = `Kontakt: ${contact.name}`;
      }
    } catch (error) {
      console.error('Failed to enrich context:', error);
    }
    
    return context;
  }
  
  private async loadCustomerInfo(id: string) {
    // Mock - würde echte API aufrufen
    return { name: 'Beispiel GmbH' };
  }
  
  private async loadContactInfo(id: string) {
    // Mock - würde echte API aufrufen
    return { name: 'Max Mustermann' };
  }
}

export const auditContextService = new AuditContextService();

/**
 * React Hook für Audit Context
 */
export function useAuditContext(): AuditContext {
  const location = useLocation();
  const params = useParams();
  const [context, setContext] = useState<AuditContext>({
    contextLabel: 'Lade...',
    hasContext: false
  });
  
  useEffect(() => {
    const detectedContext = auditContextService.detectContext(
      location.pathname,
      params as Record<string, string>
    );
    
    // Async Enrichment
    auditContextService.enrichContext(detectedContext)
      .then(enriched => setContext(enriched));
  }, [location.pathname, params]);
  
  return context;
}
```

### 2. Floating Action Button Component

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/components/AuditFAB.tsx`

```typescript
import { useState, useEffect } from 'react';
import {
  Fab,
  Zoom,
  Badge,
  Tooltip,
  useScrollTrigger,
  Box
} from '@mui/material';
import {
  History as HistoryIcon,
  Close as CloseIcon
} from '@mui/icons-material';
import { useAuth } from '../../../contexts/AuthContext';
import { useAuditContext } from '../services/auditContextService';
import { AuditDrawer } from './AuditDrawer';
import { useRecentAuditActivity } from '../hooks/useRecentAuditActivity';

interface AuditFABProps {
  hideOnScroll?: boolean;
  position?: {
    bottom?: number | string;
    right?: number | string;
  };
}

export function AuditFAB({
  hideOnScroll = true,
  position = { bottom: 24, right: 24 }
}: AuditFABProps) {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const { user } = useAuth();
  const context = useAuditContext();
  const { recentCount } = useRecentAuditActivity(context);
  
  // Scroll-basiertes Verstecken
  const trigger = useScrollTrigger({
    disableHysteresis: true,
    threshold: 100
  });
  
  const shouldShow = !hideOnScroll || !trigger;
  
  // Nur für berechtigte Rollen anzeigen
  const canViewAudit = user?.roles?.some(role => 
    ['admin', 'manager', 'auditor'].includes(role)
  );
  
  if (!canViewAudit) {
    return null;
  }
  
  // Dynamische Farbe basierend auf Aktivität
  const getFabColor = () => {
    if (recentCount > 10) return 'error';
    if (recentCount > 5) return 'warning';
    return 'primary';
  };
  
  // Animation für neue Aktivität
  const [pulse, setPulse] = useState(false);
  
  useEffect(() => {
    if (recentCount > 0) {
      setPulse(true);
      const timer = setTimeout(() => setPulse(false), 1000);
      return () => clearTimeout(timer);
    }
  }, [recentCount]);
  
  return (
    <>
      {/* Floating Action Button */}
      <Zoom in={shouldShow} timeout={300}>
        <Box
          sx={{
            position: 'fixed',
            ...position,
            zIndex: 1200
          }}
        >
          <Tooltip 
            title={`Audit Trail${context.hasContext ? ` - ${context.contextLabel}` : ''}`}
            placement="left"
          >
            <Badge 
              badgeContent={recentCount} 
              color={getFabColor() as any}
              max={99}
            >
              <Fab
                color={getFabColor() as any}
                onClick={() => setDrawerOpen(true)}
                sx={{
                  animation: pulse ? 'pulse 0.5s' : 'none',
                  '@keyframes pulse': {
                    '0%': { transform: 'scale(1)' },
                    '50%': { transform: 'scale(1.1)' },
                    '100%': { transform: 'scale(1)' }
                  },
                  transition: 'all 0.3s',
                  '&:hover': {
                    transform: 'scale(1.05)'
                  }
                }}
              >
                {drawerOpen ? <CloseIcon /> : <HistoryIcon />}
              </Fab>
            </Badge>
          </Tooltip>
        </Box>
      </Zoom>
      
      {/* Audit Drawer */}
      <AuditDrawer
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        context={context}
      />
    </>
  );
}
```

### 3. Audit Drawer Component

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/components/AuditDrawer.tsx`

```typescript
import { useState } from 'react';
import {
  Drawer,
  Box,
  Typography,
  IconButton,
  Divider,
  TextField,
  InputAdornment,
  Chip,
  ToggleButtonGroup,
  ToggleButton,
  Alert,
  Paper,
  Tabs,
  Tab
} from '@mui/material';
import {
  Close as CloseIcon,
  Search as SearchIcon,
  FilterList as FilterIcon,
  Download as DownloadIcon,
  ViewList as ListIcon,
  Timeline as TimelineIcon,
  Assessment as StatsIcon
} from '@mui/icons-material';
import { UserAuditTimeline } from './UserAuditTimeline';
import { VirtualizedAuditTimeline } from './VirtualizedAuditTimeline';
import { AuditStatistics } from './AuditStatistics';
import { AuditExportButton } from './AuditExportButton';
import { useAuth } from '../../../contexts/AuthContext';
import type { AuditContext } from '../services/auditContextService';

interface AuditDrawerProps {
  open: boolean;
  onClose: () => void;
  context: AuditContext;
}

export function AuditDrawer({ open, onClose, context }: AuditDrawerProps) {
  const [activeTab, setActiveTab] = useState(0);
  const [viewMode, setViewMode] = useState<'timeline' | 'list'>('timeline');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterOpen, setFilterOpen] = useState(false);
  const { user } = useAuth();
  
  const canExport = user?.roles?.some(role => 
    ['admin', 'auditor'].includes(role)
  );
  
  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      sx={{
        '& .MuiDrawer-paper': {
          width: { xs: '100%', sm: 600 },
          maxWidth: '100%'
        }
      }}
    >
      {/* Header */}
      <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" sx={{ flex: 1 }}>
            Audit Trail
          </Typography>
          <IconButton onClick={onClose} edge="end">
            <CloseIcon />
          </IconButton>
        </Box>
        
        {/* Context Info */}
        {context.hasContext && (
          <Alert severity="info" sx={{ mb: 2 }}>
            <Typography variant="body2">
              <strong>Kontext:</strong> {context.contextLabel}
            </Typography>
            {context.entityType && (
              <Chip 
                label={context.entityType} 
                size="small" 
                sx={{ mt: 0.5 }}
              />
            )}
          </Alert>
        )}
        
        {/* Search Bar */}
        <TextField
          fullWidth
          size="small"
          placeholder="Audit-Einträge durchsuchen..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
            endAdornment: (
              <InputAdornment position="end">
                <IconButton 
                  size="small"
                  onClick={() => setFilterOpen(!filterOpen)}
                  color={filterOpen ? 'primary' : 'default'}
                >
                  <FilterIcon />
                </IconButton>
              </InputAdornment>
            )
          }}
          sx={{ mb: 2 }}
        />
        
        {/* Filter Panel (ausklappbar) */}
        {filterOpen && (
          <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              Filter
            </Typography>
            {/* Filter-Optionen hier */}
          </Paper>
        )}
        
        {/* View Mode Toggle */}
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <ToggleButtonGroup
            value={viewMode}
            exclusive
            onChange={(_, newMode) => newMode && setViewMode(newMode)}
            size="small"
          >
            <ToggleButton value="timeline">
              <TimelineIcon sx={{ mr: 1 }} />
              Timeline
            </ToggleButton>
            <ToggleButton value="list">
              <ListIcon sx={{ mr: 1 }} />
              Liste
            </ToggleButton>
          </ToggleButtonGroup>
          
          {canExport && (
            <Box sx={{ ml: 'auto' }}>
              <AuditExportButton
                entityType={context.entityType}
                entityId={context.entityId}
                size="small"
              />
            </Box>
          )}
        </Box>
      </Box>
      
      {/* Tabs für verschiedene Ansichten */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
          <Tab label="Historie" icon={<TimelineIcon />} iconPosition="start" />
          <Tab label="Statistiken" icon={<StatsIcon />} iconPosition="start" />
        </Tabs>
      </Box>
      
      {/* Content */}
      <Box sx={{ flex: 1, overflow: 'auto' }}>
        {/* Tab 1: Historie */}
        {activeTab === 0 && (
          <>
            {context.hasContext ? (
              viewMode === 'timeline' ? (
                <UserAuditTimeline
                  entityType={context.entityType!}
                  entityId={context.entityId || context.userId!}
                  searchTerm={searchTerm}
                  compact={false}
                />
              ) : (
                <VirtualizedAuditTimeline
                  entityType={context.entityType!}
                  entityId={context.entityId || context.userId!}
                />
              )
            ) : (
              <Box sx={{ p: 3 }}>
                <Alert severity="info">
                  <Typography variant="body2">
                    Keine spezifische Entity ausgewählt. 
                    Navigieren Sie zu einem Kunden oder Kontakt, 
                    um dessen Audit-Historie zu sehen.
                  </Typography>
                </Alert>
                
                {/* Globale Recent Activity für Admins */}
                {user?.roles?.includes('admin') && (
                  <Box sx={{ mt: 3 }}>
                    <Typography variant="h6" gutterBottom>
                      Globale Aktivität
                    </Typography>
                    <UserAuditTimeline
                      searchTerm={searchTerm}
                      maxItems={20}
                      compact={false}
                    />
                  </Box>
                )}
              </Box>
            )}
          </>
        )}
        
        {/* Tab 2: Statistiken */}
        {activeTab === 1 && (
          <AuditStatistics
            entityType={context.entityType}
            entityId={context.entityId || context.userId}
          />
        )}
      </Box>
    </Drawer>
  );
}
```

### 4. Integration in MainLayout

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/components/layout/MainLayoutV2.tsx`

```typescript
// In MainLayoutV2.tsx hinzufügen:

import { AuditFAB } from '../../features/audit/components/AuditFAB';

export function MainLayoutV2({ children }: MainLayoutProps) {
  const { user } = useAuth();
  
  // FAB nur für berechtigte User anzeigen
  const showAuditFAB = user?.roles?.some(role => 
    ['admin', 'manager', 'auditor'].includes(role)
  );
  
  return (
    <Box sx={{ display: 'flex', height: '100vh' }}>
      {/* Sidebar */}
      <SidebarV2 />
      
      {/* Main Content */}
      <Box sx={{ flex: 1, overflow: 'auto' }}>
        {children}
      </Box>
      
      {/* Audit FAB - Global verfügbar */}
      {showAuditFAB && (
        <AuditFAB 
          hideOnScroll={true}
          position={{ bottom: 24, right: 24 }}
        />
      )}
    </Box>
  );
}
```

## 🎨 UX-Design-Prinzipien

### Position & Verhalten
- **Standard-Position:** Rechts unten (24px Abstand)
- **Auto-Hide:** Versteckt sich beim Scrollen nach unten
- **Auto-Show:** Erscheint beim Scrollen nach oben
- **Mobile:** Angepasste Position für kleine Screens

### Visual Feedback
- **Badge:** Zeigt Anzahl neuer Einträge
- **Farbe:** Ändert sich basierend auf Aktivität
- **Animation:** Pulse-Effekt bei neuen Einträgen

### Drawer Design
- **Breite:** 600px Desktop, 100% Mobile
- **Slide-In:** Von rechts einfahrend
- **Overlay:** Semi-transparent backdrop

## 🧪 Test-Strategie

### Context Detection Tests
```typescript
describe('AuditContextService', () => {
  it('should detect customer context from URL', () => {
    const context = service.detectContext('/customers/123', { customerId: '123' });
    expect(context.entityType).toBe('customer');
    expect(context.entityId).toBe('123');
  });
  
  it('should return no context for dashboard', () => {
    const context = service.detectContext('/dashboard', {});
    expect(context.hasContext).toBe(false);
  });
});
```

### FAB Behavior Tests
```typescript
describe('AuditFAB', () => {
  it('should hide on scroll down', () => {
    // Test scroll behavior
  });
  
  it('should show badge with recent count', () => {
    // Test badge anzeige
  });
  
  it('should open drawer on click', () => {
    // Test drawer opening
  });
});
```

## 📊 Performance-Überlegungen

### Lazy Loading
- FAB Component wird lazy geladen
- Drawer Content erst bei Öffnung
- Timeline Daten on-demand

### Caching
- Context wird gecacht (5 Min)
- Recent Activity Count wird gecacht
- Drawer State wird preserved

## ✅ Definition of Done

- [ ] Context Detection Service implementiert
- [ ] Audit FAB Component erstellt
- [ ] Drawer mit Timeline integriert
- [ ] Search & Filter funktioniert
- [ ] Export im Drawer verfügbar
- [ ] Mobile-responsive Design
- [ ] Auto-hide beim Scrollen
- [ ] Badge mit Activity Count
- [ ] Tests geschrieben
- [ ] Performance optimiert

## 📅 Zeitplan

**Geschätzte Dauer:** 6-8 Stunden
- Context Service: 2h
- FAB Component: 2h
- Drawer Implementation: 3h
- Tests & Polish: 1h

---

**Status:** Bereit für Sprint 4  
**Abhängigkeiten:** PR 3 & 4 müssen abgeschlossen sein