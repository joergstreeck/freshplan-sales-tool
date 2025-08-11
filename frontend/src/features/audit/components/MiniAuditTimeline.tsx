/**
 * Mini Audit Timeline Component
 * 
 * Compact audit history display for integration in contact cards.
 * Shows last change in collapsed state, expands to show last 5 changes.
 * 
 * @module MiniAuditTimeline
 * @since FC-005 PR4
 */

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
  IconButton as _IconButton,
  Tooltip,
  Button,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineDot,
  TimelineConnector,
  TimelineContent,
  TimelineOppositeContent,
} from '@mui/lab';
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
  Email as EmailIcon,
  Phone as PhoneIcon,
  Business as BusinessIcon,
  LocationOn as LocationIcon,
} from '@mui/icons-material';
import { formatDistanceToNow, format } from 'date-fns';
import { de } from 'date-fns/locale';
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../services/auditApi';
import type { AuditEntry } from '../types/audit.types';
import { useAuth } from '../../../contexts/AuthContext';

interface MiniAuditTimelineProps {
  entityType: string;
  entityId: string;
  maxEntries?: number;
  showDetails?: boolean;
  compact?: boolean;
  onShowMore?: () => void;
}

/**
 * Compact audit timeline for embedding in cards
 */
export function MiniAuditTimeline({
  entityType,
  entityId,
  maxEntries = 5,
  showDetails = false,
  compact = true,
  onShowMore,
}: MiniAuditTimelineProps) {
  const theme = useTheme();
  const { user } = useAuth();
  const [expanded, setExpanded] = useState(false);
  
  // Props are now validated through TypeScript
  
  // Check if user has permission to view audit
  // In development with authBypass, always allow audit viewing
  const isDevelopment = import.meta.env.DEV;
  const canViewAudit = isDevelopment || user?.roles?.some(role => 
    ['admin', 'manager', 'auditor'].includes(role)
  );
  
  // Permissions checked via role-based access
  
  // Fetch audit data
  const { data: auditEntries, isLoading, error } = useQuery({
    queryKey: ['audit', entityType, entityId, maxEntries],
    queryFn: async () => {
      // Fetching audit trail with proper entity identification
      const result = await auditApi.getEntityAuditTrail(entityType, entityId, 0, maxEntries);
      console.log('Audit trail result:', result);
      return result;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes cache
    enabled: canViewAudit && !!entityId, // Also check that entityId exists
  });
  
  // Get last change summary
  const lastChange = useMemo(() => {
    if (!auditEntries?.content?.length) return null;
    const latest = auditEntries.content[0];
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
      'EMAIL_SENT': <EmailIcon fontSize="small" sx={{ color: theme.palette.info.main }} />,
      'PHONE_CALL': <PhoneIcon fontSize="small" sx={{ color: theme.palette.info.main }} />,
      'ADDRESS_CHANGED': <LocationIcon fontSize="small" sx={{ color: theme.palette.warning.main }} />,
      'STATUS_CHANGED': <BusinessIcon fontSize="small" sx={{ color: theme.palette.warning.main }} />,
    };
    return iconMap[action] || <HistoryIcon fontSize="small" />;
  };
  
  // Action type to color mapping
  const getActionColor = (action: string): 'success' | 'info' | 'warning' | 'error' | 'default' => {
    if (action.includes('CREATE') || action.includes('ADD')) return 'success';
    if (action.includes('UPDATE') || action.includes('SENT') || action.includes('CALL')) return 'info';
    if (action.includes('DELETE') || action.includes('REMOVE')) return 'error';
    if (action.includes('WARNING') || action.includes('CHANGED')) return 'warning';
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
      'EMAIL_SENT': 'E-Mail gesendet',
      'PHONE_CALL': 'Anruf getätigt',
      'ADDRESS_CHANGED': 'Adresse geändert',
      'STATUS_CHANGED': 'Status geändert',
    };
    return labelMap[action] || action;
  };
  
  // Format change details
  const formatChangeDetails = (changes: any): string => {
    if (!changes) return '';
    
    try {
      const changeObj = typeof changes === 'string' ? JSON.parse(changes) : changes;
      const fields = Object.keys(changeObj);
      
      if (fields.length === 0) return '';
      if (fields.length === 1) {
        const field = fields[0];
        const change = changeObj[field];
        if (change.from && change.to) {
          return `${field}: ${change.from} → ${change.to}`;
        }
        return `${field} geändert`;
      }
      
      return `${fields.length} Felder geändert`;
    } catch {
      return '';
    }
  };
  
  // Don't render if user doesn't have permission
  if (!canViewAudit) {
    return null;
  }
  
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
  if (!auditEntries?.content?.length) {
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
              : 'Keine Änderungen'}
          </Typography>
          {lastChange && (
            <Chip
              label={formatActionLabel(lastChange.action)}
              size="small"
              color={getActionColor(lastChange.action)}
              sx={{ height: 18, fontSize: '0.7rem' }}
            />
          )}
        </Stack>
      </AccordionSummary>
      
      <AccordionDetails sx={{ pt: 0, pb: 2 }}>
        {compact ? (
          // Compact view - simple list
          <Stack spacing={1}>
            {auditEntries.content.slice(0, maxEntries).map((entry: AuditEntry) => (
              <Stack 
                key={entry.id} 
                direction="row" 
                spacing={1} 
                alignItems="flex-start"
                sx={{
                  p: 1,
                  borderRadius: 1,
                  backgroundColor: alpha(theme.palette.background.default, 0.5),
                  '&:hover': {
                    backgroundColor: alpha(theme.palette.primary.main, 0.05),
                  },
                }}
              >
                {getActionIcon(entry.eventType)}
                <Box sx={{ flex: 1, minWidth: 0 }}>
                  <Typography variant="caption" sx={{ fontWeight: 500 }}>
                    {formatActionLabel(entry.eventType)}
                  </Typography>
                  {entry.changes && (
                    <Typography 
                      variant="caption" 
                      color="text.secondary"
                      sx={{ 
                        display: 'block',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {formatChangeDetails(entry.changes)}
                    </Typography>
                  )}
                  <Typography variant="caption" color="text.secondary">
                    {format(new Date(entry.timestamp), 'dd.MM.yyyy HH:mm', { locale: de })}
                    {' • '}
                    {entry.userName || 'System'}
                  </Typography>
                </Box>
              </Stack>
            ))}
          </Stack>
        ) : (
          // Detailed view - timeline
          <Timeline position="right" sx={{ p: 0, m: 0 }}>
            {auditEntries.content.slice(0, maxEntries).map((entry: AuditEntry, index: number) => (
              <TimelineItem key={entry.id} sx={{ minHeight: 60 }}>
                <TimelineOppositeContent sx={{ flex: 0.3, px: 1 }}>
                  <Typography variant="caption" color="text.secondary">
                    {format(new Date(entry.timestamp), 'HH:mm')}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" display="block">
                    {format(new Date(entry.timestamp), 'dd.MM.yy')}
                  </Typography>
                </TimelineOppositeContent>
                
                <TimelineSeparator>
                  <TimelineDot color={getActionColor(entry.eventType)}>
                    {getActionIcon(entry.eventType)}
                  </TimelineDot>
                  {index < auditEntries.content.length - 1 && <TimelineConnector />}
                </TimelineSeparator>
                
                <TimelineContent sx={{ px: 1 }}>
                  <Typography variant="body2" sx={{ fontWeight: 500 }}>
                    {formatActionLabel(entry.eventType)}
                  </Typography>
                  {entry.changes && (
                    <Typography variant="caption" color="text.secondary">
                      {formatChangeDetails(entry.changes)}
                    </Typography>
                  )}
                  <Typography variant="caption" color="text.secondary" display="block">
                    von {entry.userName || 'System'}
                  </Typography>
                </TimelineContent>
              </TimelineItem>
            ))}
          </Timeline>
        )}
        
        {/* Show more button */}
        {onShowMore && auditEntries.totalElements > maxEntries && (
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Button
              size="small"
              onClick={onShowMore}
              startIcon={<HistoryIcon />}
            >
              Vollständige Historie anzeigen ({auditEntries.totalElements} Einträge)
            </Button>
          </Box>
        )}
      </AccordionDetails>
    </Accordion>
  );
}