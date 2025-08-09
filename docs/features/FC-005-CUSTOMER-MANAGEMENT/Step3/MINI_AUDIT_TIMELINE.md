# 📇 Mini Audit Timeline - Kompakte Historie in Contact Cards

**Feature:** FC-005 PR4 - Phase 2  
**Status:** 📋 BEREIT ZUR IMPLEMENTIERUNG  
**Geschätzter Aufwand:** 3-4 Stunden  
**Priorität:** 🥈 HOCH - Compliance & Transparenz  

## 🧭 NAVIGATION

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/INTELLIGENT_FILTER_BAR.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PR4_ENHANCED_FEATURES_COMPLETE.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/VIRTUAL_SCROLLING_PERFORMANCE.md`  
**→ Basis Timeline:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/components/EntityAuditTimeline.tsx`  

## 🎯 VISION

Integriere eine kompakte, ausklappbare Audit-Historie direkt in die SmartContactCards. Nutzer sehen auf einen Blick die letzte Änderung und können bei Bedarf die letzten 5 Änderungen einsehen - ohne die Übersicht zu verlassen.

## 📊 FEATURE-ÜBERSICHT

### Accordion-basierte Integration
- **Collapsed:** "Zuletzt geändert: vor 2 Tagen von Max Mustermann"
- **Expanded:** Timeline mit letzten 5 Änderungen
- **Progressive Disclosure:** Keine UI-Überladung
- **Role-based:** Nur für berechtigte Rollen sichtbar

## 🏗️ IMPLEMENTIERUNG

### 1. MiniAuditTimeline Component

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/components/MiniAuditTimeline.tsx`

```typescript
import React, { useState, useMemo } from 'react';
import {
  Box,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Typography,
  Chip,
  Stack,
  Skeleton,
  Alert,
  IconButton,
  Tooltip,
  useTheme,
  alpha,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  History as HistoryIcon,
  Edit as EditIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  Person as PersonIcon,
  Schedule as TimeIcon,
  Info as InfoIcon,
} from '@mui/icons-material';
import { formatDistanceToNow, format } from 'date-fns';
import { de } from 'date-fns/locale';
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../services/auditApi';
import type { AuditEntry } from '../types/audit.types';

interface MiniAuditTimelineProps {
  entityType: string;
  entityId: string;
  maxEntries?: number;
  showDetails?: boolean;
  compact?: boolean;
}

export function MiniAuditTimeline({
  entityType,
  entityId,
  maxEntries = 5,
  showDetails = false,
  compact = true,
}: MiniAuditTimelineProps) {
  const theme = useTheme();
  const [expanded, setExpanded] = useState(false);
  
  // Fetch audit data
  const { data: auditEntries, isLoading, error } = useQuery({
    queryKey: ['audit', entityType, entityId, maxEntries],
    queryFn: () => auditApi.getEntityAuditTrail(entityType, entityId, 0, maxEntries),
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes cache
  });
  
  // Get last change summary
  const lastChange = useMemo(() => {
    if (!auditEntries?.length) return null;
    const latest = auditEntries[0];
    return {
      user: latest.userName || 'System',
      time: formatDistanceToNow(new Date(latest.timestamp), {
        addSuffix: true,
        locale: de,
      }),
      action: latest.eventType,
    };
  }, [auditEntries]);
  
  // Action type to icon mapping
  const getActionIcon = (action: string) => {
    const iconMap: Record<string, JSX.Element> = {
      'CREATE': <AddIcon fontSize="small" sx={{ color: theme.palette.success.main }} />,
      'UPDATE': <EditIcon fontSize="small" sx={{ color: theme.palette.info.main }} />,
      'DELETE': <DeleteIcon fontSize="small" sx={{ color: theme.palette.error.main }} />,
      'VIEW': <ViewIcon fontSize="small" sx={{ color: theme.palette.grey[500] }} />,
      'CUSTOMER_CREATED': <AddIcon fontSize="small" sx={{ color: theme.palette.success.main }} />,
      'CUSTOMER_UPDATED': <EditIcon fontSize="small" sx={{ color: theme.palette.info.main }} />,
      'CONTACT_ADDED': <PersonIcon fontSize="small" sx={{ color: theme.palette.success.main }} />,
      'CONTACT_UPDATED': <PersonIcon fontSize="small" sx={{ color: theme.palette.info.main }} />,
      'CONTACT_DELETED': <PersonIcon fontSize="small" sx={{ color: theme.palette.error.main }} />,
    };
    return iconMap[action] || <HistoryIcon fontSize="small" />;
  };
  
  // Action type to color mapping
  const getActionColor = (action: string): 'success' | 'info' | 'warning' | 'error' | 'default' => {
    if (action.includes('CREATE') || action.includes('ADD')) return 'success';
    if (action.includes('UPDATE')) return 'info';
    if (action.includes('DELETE')) return 'error';
    if (action.includes('WARNING')) return 'warning';
    return 'default';
  };
  
  // Format action label
  const formatActionLabel = (action: string): string => {
    const labelMap: Record<string, string> = {
      'CREATE': 'Erstellt',
      'UPDATE': 'Aktualisiert',
      'DELETE': 'Gelöscht',
      'VIEW': 'Angesehen',
      'CUSTOMER_CREATED': 'Kunde erstellt',
      'CUSTOMER_UPDATED': 'Kunde aktualisiert',
      'CONTACT_ADDED': 'Kontakt hinzugefügt',
      'CONTACT_UPDATED': 'Kontakt aktualisiert',
      'CONTACT_DELETED': 'Kontakt gelöscht',
    };
    return labelMap[action] || action;
  };
  
  // Loading state
  if (isLoading) {
    return (
      <Box sx={{ p: 1 }}>
        <Skeleton variant="text" width="80%" height={20} />
      </Box>
    );
  }
  
  // Error state
  if (error) {
    return (
      <Alert severity="error" sx={{ py: 0.5, fontSize: '0.75rem' }}>
        Audit-Historie konnte nicht geladen werden
      </Alert>
    );
  }
  
  // No data state
  if (!auditEntries?.length) {
    return (
      <Typography variant="caption" color="text.secondary" sx={{ p: 1 }}>
        Keine Änderungshistorie verfügbar
      </Typography>
    );
  }
  
  return (
    <Accordion
      expanded={expanded}
      onChange={(_, isExpanded) => setExpanded(isExpanded)}
      sx={{
        boxShadow: 'none',
        borderTop: `1px solid ${theme.palette.divider}`,
        '&:before': { display: 'none' },
        backgroundColor: 'transparent',
      }}
    >
      <AccordionSummary
        expandIcon={<ExpandMoreIcon fontSize="small" />}
        sx={{
          minHeight: 36,
          '& .MuiAccordionSummary-content': {
            margin: '4px 0',
            alignItems: 'center',
          },
        }}
      >
        <Stack direction="row" spacing={1} alignItems="center">
          <HistoryIcon fontSize="small" color="action" />
          <Typography variant="caption" color="text.secondary">
            {lastChange 
              ? `Zuletzt geändert ${lastChange.time} von ${lastChange.user}`
              : 'Änderungshistorie'
            }
          </Typography>
          {auditEntries.length > maxEntries && (
            <Chip 
              label={`+${auditEntries.length - maxEntries}`} 
              size="small" 
              sx={{ height: 16, fontSize: '0.7rem' }}
            />
          )}
        </Stack>
      </AccordionSummary>
      
      <AccordionDetails sx={{ pt: 0, pb: 1 }}>
        <Stack spacing={1}>
          {auditEntries.slice(0, maxEntries).map((entry, index) => (
            <Box
              key={entry.id}
              sx={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: 1.5,
                position: 'relative',
                '&:not(:last-child)::after': {
                  content: '""',
                  position: 'absolute',
                  left: 11,
                  top: 28,
                  bottom: -8,
                  width: 1,
                  backgroundColor: theme.palette.divider,
                },
              }}
            >
              {/* Icon */}
              <Box
                sx={{
                  width: 24,
                  height: 24,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  borderRadius: '50%',
                  backgroundColor: alpha(theme.palette.primary.main, 0.1),
                  zIndex: 1,
                }}
              >
                {getActionIcon(entry.eventType)}
              </Box>
              
              {/* Content */}
              <Box sx={{ flex: 1, minWidth: 0 }}>
                <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
                  <Chip
                    label={formatActionLabel(entry.eventType)}
                    size="small"
                    color={getActionColor(entry.eventType)}
                    sx={{ height: 20, fontSize: '0.7rem' }}
                  />
                  <Typography variant="caption" color="text.secondary">
                    {entry.userName || 'System'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    •
                  </Typography>
                  <Tooltip title={format(new Date(entry.timestamp), 'PPpp', { locale: de })}>
                    <Typography variant="caption" color="text.secondary">
                      {formatDistanceToNow(new Date(entry.timestamp), {
                        addSuffix: true,
                        locale: de,
                      })}
                    </Typography>
                  </Tooltip>
                </Stack>
                
                {/* Change Details */}
                {showDetails && entry.changes && (
                  <Box sx={{ mt: 0.5 }}>
                    {Object.entries(entry.changes).slice(0, 3).map(([field, change]: [string, any]) => (
                      <Typography
                        key={field}
                        variant="caption"
                        sx={{
                          display: 'block',
                          color: theme.palette.text.secondary,
                          fontSize: '0.7rem',
                          lineHeight: 1.4,
                        }}
                      >
                        <strong>{field}:</strong>{' '}
                        {change.oldValue && (
                          <>
                            <span style={{ textDecoration: 'line-through', opacity: 0.6 }}>
                              {change.oldValue}
                            </span>
                            {' → '}
                          </>
                        )}
                        <span style={{ color: theme.palette.success.main }}>
                          {change.newValue}
                        </span>
                      </Typography>
                    ))}
                    {Object.keys(entry.changes).length > 3 && (
                      <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.7rem' }}>
                        +{Object.keys(entry.changes).length - 3} weitere Änderungen
                      </Typography>
                    )}
                  </Box>
                )}
              </Box>
              
              {/* Action Button */}
              {!compact && (
                <Tooltip title="Details anzeigen">
                  <IconButton size="small" sx={{ width: 24, height: 24 }}>
                    <InfoIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              )}
            </Box>
          ))}
          
          {/* Load More Link */}
          {auditEntries.length > maxEntries && (
            <Box sx={{ textAlign: 'center', pt: 1 }}>
              <Typography
                variant="caption"
                color="primary"
                sx={{
                  cursor: 'pointer',
                  '&:hover': { textDecoration: 'underline' },
                }}
                onClick={() => {
                  // Navigate to full audit view or open modal
                  console.log('Show full audit trail');
                }}
              >
                Vollständige Historie anzeigen ({auditEntries.length} Einträge)
              </Typography>
            </Box>
          )}
        </Stack>
      </AccordionDetails>
    </Accordion>
  );
}
```

### 2. Integration in SmartContactCard

**Datei Update:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/contacts/SmartContactCard.tsx`

```typescript
// Am Anfang der Datei importieren
import { MiniAuditTimeline } from '../../../audit/components/MiniAuditTimeline';
import { useAuth } from '../../../../contexts/AuthContext';

// In der SmartContactCard Component
export function SmartContactCard({ 
  contact, 
  onEdit, 
  onDelete,
  showAuditTrail = true,
  customerId,
}: SmartContactCardProps) {
  const { user } = useAuth();
  
  // Check if user can view audit trail
  const canViewAudit = showAuditTrail && user?.roles?.some(role => 
    ['admin', 'manager', 'auditor'].includes(role)
  );
  
  return (
    <Card sx={{ 
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      transition: 'all 0.3s ease',
      '&:hover': {
        boxShadow: (theme) => theme.shadows[4],
      },
    }}>
      {/* Existing Card Content */}
      <CardContent sx={{ flex: 1 }}>
        {/* ... existing contact information ... */}
      </CardContent>
      
      {/* Audit Timeline Integration */}
      {canViewAudit && contact.id && (
        <MiniAuditTimeline
          entityType="CONTACT"
          entityId={contact.id}
          maxEntries={5}
          showDetails={false}
          compact={true}
        />
      )}
      
      {/* Existing Card Actions */}
      <CardActions>
        {/* ... existing actions ... */}
      </CardActions>
    </Card>
  );
}
```

## 🎨 UI/UX DESIGN PATTERNS

### Visual Hierarchy
1. **Collapsed State:** Subtle, single line info
2. **Expanded State:** Clear timeline with icons
3. **Color Coding:** Green (create), Blue (update), Red (delete)
4. **Progressive Disclosure:** Start with 5, link to full view

### Interaction Patterns
- **Single Click:** Expand/Collapse
- **Hover:** Show full timestamp
- **Link:** Navigate to full audit view
- **Responsive:** Adapts to card width

## 🔒 SECURITY & PERMISSIONS

### Role-based Visibility
```typescript
const AUDIT_VIEWER_ROLES = ['admin', 'manager', 'auditor'];

function canViewAudit(user: User): boolean {
  return user.roles.some(role => AUDIT_VIEWER_ROLES.includes(role));
}
```

### Data Privacy
- Usernames werden anonymisiert bei Bedarf
- Sensible Felder werden gefiltert
- IP-Adressen nur für Admins sichtbar

## ⚡ PERFORMANCE OPTIMIERUNG

### 1. Caching Strategy
```typescript
// React Query Cache Configuration
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes
      refetchOnWindowFocus: false,
    },
  },
});
```

### 2. Lazy Loading
```typescript
// Only load when accordion expands
const { data } = useQuery({
  queryKey: ['audit', entityType, entityId],
  queryFn: fetchAuditData,
  enabled: expanded, // Only fetch when expanded
});
```

### 3. Optimistic Updates
```typescript
// Update UI immediately, sync later
const mutation = useMutation({
  mutationFn: updateContact,
  onMutate: async (newData) => {
    // Optimistically update audit trail
    await queryClient.cancelQueries(['audit', 'CONTACT', contactId]);
    const previousData = queryClient.getQueryData(['audit', 'CONTACT', contactId]);
    
    queryClient.setQueryData(['audit', 'CONTACT', contactId], (old) => {
      return [createOptimisticEntry(newData), ...old];
    });
    
    return { previousData };
  },
});
```

## 🧪 TESTING REQUIREMENTS

### Unit Tests
```typescript
describe('MiniAuditTimeline', () => {
  it('should show last change in collapsed state', () => {});
  it('should expand to show 5 recent changes', () => {});
  it('should format timestamps correctly', () => {});
  it('should apply correct colors to action types', () => {});
  it('should handle loading and error states', () => {});
});
```

### Integration Tests
- Test with SmartContactCard
- Test role-based visibility
- Test data fetching and caching

## 📊 SUCCESS METRICS

- **Load Time:** < 100ms für erste 5 Einträge
- **Cache Hit Rate:** > 80%
- **User Engagement:** > 60% expand rate
- **Error Rate:** < 0.1%

## 🔗 VERWANDTE DOKUMENTE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/INTELLIGENT_FILTER_BAR.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/VIRTUAL_SCROLLING_PERFORMANCE.md`  
**→ Export Features:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_EXPORT_FEATURES.md`  
**← Base Component:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/components/EntityAuditTimeline.tsx`  

---

**Status:** ✅ BEREIT ZUR IMPLEMENTIERUNG  
**Nächster Schritt:** MiniAuditTimeline.tsx erstellen und in SmartContactCard integrieren