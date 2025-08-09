# 📇 Smart Contact Card Audit Integration

**Feature:** FC-005 Step3 - Contact Management UI  
**Status:** 📋 GEPLANT für PR 4  
**Priorität:** 🥈 Priorität 2 (nach CustomerDetailPage)  
**Geschätzter Aufwand:** 4-6 Stunden  

## 🧭 Navigation

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**← Implementation Plan:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_TIMELINE_IMPLEMENTATION_PLAN.md`  
**→ Nächstes Feature:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PERFORMANCE_OPTIMIZATIONS.md`  
**→ Export Features:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_EXPORT_FEATURES.md`  

## 🎯 Vision

Erweitere die bestehenden Smart Contact Cards um eine kompakte, ausklappbare Audit-Historie. User sehen auf einen Blick die letzte Änderung und können bei Bedarf die letzten 5 Änderungen einsehen - ohne die Übersicht zu verlassen.

## 📊 Feature-Übersicht

### Accordion-basierte Integration
- **Collapsed State:** Zeigt nur "Letzte Änderung: vor 2 Tagen von Max Mustermann"
- **Expanded State:** Zeigt Timeline mit letzten 5 Änderungen
- **Progressive Disclosure:** Keine Überladung der Haupt-UI

### Kompakte Timeline-Ansicht
- **Mini-Timeline:** Reduzierte Darstellung für Karten-Kontext
- **Fokus auf Essentials:** Wer, Wann, Was (ohne Details)
- **Color-Coding:** Visuelle Unterscheidung der Aktionstypen

## 🏗️ Technische Implementierung

### 1. SmartContactCard Erweiterung

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/contacts/SmartContactCard.tsx`

```typescript
import { useState } from 'react';
import {
  Card,
  CardContent,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Typography,
  Chip,
  Box,
  Tooltip
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  History as HistoryIcon,
  Person as PersonIcon,
  Edit as EditIcon,
  Add as AddIcon,
  Delete as DeleteIcon
} from '@mui/icons-material';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';
import { useAuth } from '../../../../contexts/AuthContext';
import { MiniAuditTimeline } from '../../../audit/components/MiniAuditTimeline';
import type { Contact } from '../../../../types/contact.types';

interface SmartContactCardProps {
  contact: Contact;
  onEdit?: (contact: Contact) => void;
  onDelete?: (contact: Contact) => void;
  showAuditTrail?: boolean;
}

export function SmartContactCard({ 
  contact, 
  onEdit, 
  onDelete,
  showAuditTrail = true 
}: SmartContactCardProps) {
  const [auditExpanded, setAuditExpanded] = useState(false);
  const { user } = useAuth();
  
  // Nur für berechtigte Rollen anzeigen
  const canViewAudit = showAuditTrail && user?.roles?.some(role => 
    ['admin', 'manager', 'auditor'].includes(role)
  );
  
  // Icon für Aktionstyp
  const getActionIcon = (action: string) => {
    switch(action) {
      case 'CREATE': return <AddIcon fontSize="small" color="success" />;
      case 'UPDATE': return <EditIcon fontSize="small" color="primary" />;
      case 'DELETE': return <DeleteIcon fontSize="small" color="error" />;
      default: return <HistoryIcon fontSize="small" />;
    }
  };
  
  return (
    <Card sx={{ 
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      transition: 'box-shadow 0.3s',
      '&:hover': {
        boxShadow: 3
      }
    }}>
      <CardContent sx={{ flex: 1 }}>
        {/* Normale Contact Info */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="h6" gutterBottom>
            {contact.firstName} {contact.lastName}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {contact.role}
          </Typography>
          <Typography variant="body2">
            {contact.email}
          </Typography>
          <Typography variant="body2">
            {contact.phone}
          </Typography>
        </Box>
        
        {/* Beziehungs-Intelligence */}
        <Box sx={{ mb: 2 }}>
          {contact.isDecisionMaker && (
            <Chip 
              label="Entscheider" 
              size="small" 
              color="primary" 
              sx={{ mr: 1 }}
            />
          )}
          {contact.isPrimaryContact && (
            <Chip 
              label="Hauptkontakt" 
              size="small" 
              color="secondary" 
            />
          )}
        </Box>
        
        {/* Audit Trail Accordion - NUR wenn berechtigt */}
        {canViewAudit && (
          <Accordion 
            expanded={auditExpanded}
            onChange={(_, isExpanded) => setAuditExpanded(isExpanded)}
            sx={{ 
              mt: 'auto',
              boxShadow: 'none',
              '&:before': { display: 'none' },
              backgroundColor: 'grey.50'
            }}
          >
            <AccordionSummary
              expandIcon={<ExpandMoreIcon />}
              sx={{ 
                minHeight: 36,
                '& .MuiAccordionSummary-content': { my: 0.5 }
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <HistoryIcon fontSize="small" color="action" />
                <Typography variant="caption" color="text.secondary">
                  Letzte Änderung: {formatDistanceToNow(
                    new Date(contact.updatedAt || contact.createdAt),
                    { locale: de, addSuffix: true }
                  )}
                </Typography>
                {contact.updatedBy && (
                  <Typography variant="caption" fontWeight="medium">
                    von {contact.updatedBy}
                  </Typography>
                )}
              </Box>
            </AccordionSummary>
            
            <AccordionDetails sx={{ pt: 0 }}>
              <MiniAuditTimeline
                entityType="contact"
                entityId={contact.id}
                maxItems={5}
                compact={true}
              />
            </AccordionDetails>
          </Accordion>
        )}
      </CardContent>
      
      {/* Action Buttons */}
      <Box sx={{ p: 2, pt: 0, display: 'flex', gap: 1 }}>
        {onEdit && (
          <Button size="small" onClick={() => onEdit(contact)}>
            Bearbeiten
          </Button>
        )}
        {onDelete && user?.roles?.includes('admin') && (
          <Button 
            size="small" 
            color="error" 
            onClick={() => onDelete(contact)}
          >
            Löschen
          </Button>
        )}
      </Box>
    </Card>
  );
}
```

### 2. MiniAuditTimeline Component

**Neue Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/components/MiniAuditTimeline.tsx`

```typescript
import { useEffect, useState } from 'react';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineDot,
  TimelineConnector,
  TimelineContent,
  TimelineOppositeContent
} from '@mui/lab';
import {
  Typography,
  Box,
  Skeleton,
  Alert
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Security as SecurityIcon
} from '@mui/icons-material';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';
import { auditApi } from '../api/auditApi';
import type { AuditEntry } from '../types/audit.types';

interface MiniAuditTimelineProps {
  entityType: string;
  entityId: string;
  maxItems?: number;
  compact?: boolean;
}

export function MiniAuditTimeline({
  entityType,
  entityId,
  maxItems = 5,
  compact = true
}: MiniAuditTimelineProps) {
  const [entries, setEntries] = useState<AuditEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  useEffect(() => {
    loadAuditEntries();
  }, [entityType, entityId]);
  
  const loadAuditEntries = async () => {
    try {
      setLoading(true);
      const response = await auditApi.getEntityAudit(
        entityType, 
        entityId,
        { page: 0, size: maxItems }
      );
      setEntries(response.data);
    } catch (err) {
      setError('Fehler beim Laden der Historie');
      console.error('Failed to load audit entries:', err);
    } finally {
      setLoading(false);
    }
  };
  
  const getEventIcon = (eventType: string) => {
    switch(eventType) {
      case 'CREATED': return <AddIcon />;
      case 'UPDATED': return <EditIcon />;
      case 'DELETED': return <DeleteIcon />;
      case 'SECURITY': return <SecurityIcon />;
      default: return <EditIcon />;
    }
  };
  
  const getEventColor = (eventType: string) => {
    switch(eventType) {
      case 'CREATED': return 'success';
      case 'UPDATED': return 'primary';
      case 'DELETED': return 'error';
      case 'SECURITY': return 'warning';
      default: return 'grey';
    }
  };
  
  const getEventLabel = (eventType: string) => {
    switch(eventType) {
      case 'CREATED': return 'Erstellt';
      case 'UPDATED': return 'Aktualisiert';
      case 'DELETED': return 'Gelöscht';
      case 'SECURITY': return 'Sicherheit';
      default: return eventType;
    }
  };
  
  if (loading) {
    return (
      <Box>
        {[...Array(3)].map((_, i) => (
          <Skeleton key={i} height={40} sx={{ my: 0.5 }} />
        ))}
      </Box>
    );
  }
  
  if (error) {
    return (
      <Alert severity="error" sx={{ mt: 1 }}>
        {error}
      </Alert>
    );
  }
  
  if (entries.length === 0) {
    return (
      <Typography variant="caption" color="text.secondary">
        Keine Änderungshistorie vorhanden
      </Typography>
    );
  }
  
  return (
    <Timeline 
      position="right"
      sx={{ 
        m: 0,
        p: 0,
        '& .MuiTimelineItem-root:before': {
          display: compact ? 'none' : 'block',
          flex: compact ? 0 : 'auto'
        }
      }}
    >
      {entries.map((entry, index) => (
        <TimelineItem key={entry.id}>
          {!compact && (
            <TimelineOppositeContent 
              sx={{ py: 0.5 }}
              variant="caption"
              color="text.secondary"
            >
              {formatDistanceToNow(new Date(entry.timestamp), {
                locale: de,
                addSuffix: true
              })}
            </TimelineOppositeContent>
          )}
          
          <TimelineSeparator>
            <TimelineDot 
              color={getEventColor(entry.eventType)}
              sx={{ p: 0.5 }}
            >
              {getEventIcon(entry.eventType)}
            </TimelineDot>
            {index < entries.length - 1 && (
              <TimelineConnector sx={{ minHeight: 20 }} />
            )}
          </TimelineSeparator>
          
          <TimelineContent sx={{ py: 0.5, px: 2 }}>
            <Typography variant="caption" fontWeight="medium">
              {getEventLabel(entry.eventType)}
            </Typography>
            {compact && (
              <Typography variant="caption" color="text.secondary" display="block">
                {formatDistanceToNow(new Date(entry.timestamp), {
                  locale: de,
                  addSuffix: true
                })}
              </Typography>
            )}
            <Typography variant="caption" color="text.secondary" display="block">
              {entry.userName}
            </Typography>
            {entry.changeReason && !compact && (
              <Typography 
                variant="caption" 
                color="text.secondary"
                sx={{ fontStyle: 'italic' }}
              >
                "{entry.changeReason}"
              </Typography>
            )}
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
}
```

## 🎨 UX-Design-Prinzipien

### Progressive Disclosure
1. **Level 1:** Nur Datum der letzten Änderung sichtbar
2. **Level 2:** Klick öffnet Timeline mit 5 Einträgen
3. **Level 3:** "Mehr anzeigen" führt zu CustomerDetailPage

### Visual Hierarchy
- **Primär:** Contact-Informationen bleiben im Fokus
- **Sekundär:** Audit-Info dezent am unteren Rand
- **Tertiär:** Details nur bei explizitem Interesse

### Mobile-First
- Touch-optimierte Accordion-Controls
- Kompakte Timeline für kleine Screens
- Horizontales Scrolling vermeiden

## 🧪 Test-Strategie

### Unit Tests
```typescript
// SmartContactCard.test.tsx
describe('SmartContactCard Audit Integration', () => {
  it('should show audit accordion for authorized roles', () => {
    // Test mit manager role
  });
  
  it('should hide audit accordion for unauthorized roles', () => {
    // Test mit sales role
  });
  
  it('should expand/collapse audit timeline', () => {
    // Test accordion interaction
  });
});
```

### Integration Tests
```typescript
// MiniAuditTimeline.test.tsx
describe('MiniAuditTimeline', () => {
  it('should load and display audit entries', () => {
    // Mock API call und Darstellung prüfen
  });
  
  it('should limit entries to maxItems', () => {
    // Prüfe dass nur 5 Einträge angezeigt werden
  });
});
```

## 📊 Performance-Überlegungen

### Lazy Loading
- Audit-Daten erst bei Accordion-Expansion laden
- Caching der geladenen Daten für 5 Minuten
- Keine Prefetch um initiale Performance zu erhalten

### Bundle Size
- MiniAuditTimeline als separate Chunk
- Lazy Import nur wenn User berechtigt
- Tree-shaking für nicht genutzte MUI Lab Components

## 🔗 API-Integration

### Endpoint
```typescript
GET /api/audit/entity/{entityType}/{entityId}?page=0&size=5
```

### Response Caching
```typescript
// 5 Minuten Cache für Mini-Timeline
const CACHE_DURATION = 5 * 60 * 1000;
```

## ✅ Definition of Done

- [ ] SmartContactCard erweitert mit Audit Accordion
- [ ] MiniAuditTimeline Component implementiert
- [ ] Role-based Visibility funktioniert
- [ ] Performance optimiert (Lazy Loading)
- [ ] Mobile-responsive Design
- [ ] Unit & Integration Tests
- [ ] Dokumentation aktualisiert
- [ ] Code Review durchgeführt

## 📅 Zeitplan

**Geschätzte Dauer:** 4-6 Stunden
- SmartContactCard Anpassung: 2h
- MiniAuditTimeline Component: 2h
- Tests: 1h
- Polish & Review: 1h

---

**Status:** Bereit für Implementierung in PR 4  
**Abhängigkeiten:** PR 3 (CustomerDetailPage) muss gemerged sein