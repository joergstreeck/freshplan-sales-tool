/**
 * Entity Audit Timeline Component
 *
 * Generic audit timeline for any entity type (customer, contact, etc.)
 * with filtering and export capabilities.
 *
 * Part of FC-005 Contact Management UI - PR 3.
 *
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_TIMELINE_IMPLEMENTATION_PLAN.md
 */

import React, { useState, useEffect } from 'react';
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
  Box,
  Paper as _Paper,
  Typography,
  Chip,
  IconButton,
  Tooltip as _Tooltip,
  TextField,
  MenuItem,
  Button,
  Skeleton,
  Alert,
  Collapse,
  Stack as _Stack,
  Card,
  CardContent,
} from '@mui/material';
import {
  Create as CreateIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Security as SecurityIcon,
  Download as DownloadIcon,
  FilterList as _FilterIcon,
  Refresh as RefreshIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  Person as PersonIcon,
  Visibility as ViewIcon,
  Lock as LockIcon,
  VpnKey as PermissionIcon,
} from '@mui/icons-material';
import { format, parseISO } from 'date-fns';
import { de } from 'date-fns/locale';

interface AuditEntry {
  id: string;
  timestamp: string;
  eventType: string;
  entityType: string;
  entityId: string;
  userId: string;
  userName: string;
  userRole: string;
  changeReason?: string;
  oldValue?: unknown;
  newValue?: unknown;
  ipAddress?: string;
  source?: string;
}

interface EntityAuditTimelineProps {
  entityType: string;
  entityId: string;
  showExportButton?: boolean;
  maxItems?: number;
  showFilters?: boolean;
  compact?: boolean;
  searchTerm?: string;
}

/**
 * Maps audit event types to icons and colors
 */
const getEventConfig = (eventType: string) => {
  const configs: Record<string, { icon: React.ReactElement; color: string; label: string }> = {
    CREATED: {
      icon: <CreateIcon />,
      color: 'success.main',
      label: 'Erstellt',
    },
    UPDATED: {
      icon: <EditIcon />,
      color: 'primary.main',
      label: 'Aktualisiert',
    },
    DELETED: {
      icon: <DeleteIcon />,
      color: 'error.main',
      label: 'Gelöscht',
    },
    VIEWED: {
      icon: <ViewIcon />,
      color: 'info.main',
      label: 'Angesehen',
    },
    PERMISSION_CHANGED: {
      icon: <PermissionIcon />,
      color: 'warning.main',
      label: 'Berechtigung geändert',
    },
    SECURITY_EVENT: {
      icon: <SecurityIcon />,
      color: 'error.main',
      label: 'Sicherheitsereignis',
    },
    LOGIN: {
      icon: <LockIcon />,
      color: 'success.main',
      label: 'Anmeldung',
    },
    LOGOUT: {
      icon: <LockIcon />,
      color: 'grey.600',
      label: 'Abmeldung',
    },
  };

  return (
    configs[eventType] || {
      icon: <EditIcon />,
      color: 'grey.600',
      label: eventType,
    }
  );
};

/**
 * Format changed fields for display
 */
const formatChangedFields = (oldValue: unknown, newValue: unknown): string[] => {
  if (!oldValue || !newValue) return [];

  // Type guard to ensure objects
  if (typeof oldValue !== 'object' || typeof newValue !== 'object') return [];

  const oldObj = oldValue as Record<string, unknown>;
  const newObj = newValue as Record<string, unknown>;

  const changes: string[] = [];
  const allKeys = new Set([...Object.keys(oldObj), ...Object.keys(newObj)]);

  allKeys.forEach(key => {
    if (oldObj[key] !== newObj[key]) {
      changes.push(`${key}: ${oldObj[key] || '-'} → ${newObj[key] || '-'}`);
    }
  });

  return changes;
};

export function EntityAuditTimeline({
  entityType,
  entityId,
  showExportButton = false,
  maxItems = 50,
  showFilters = true,
  compact = false,
  searchTerm = '',
}: EntityAuditTimelineProps) {
  const [entries, setEntries] = useState<AuditEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, _setError] = useState<string | null>(null);
  const [expandedItems, setExpandedItems] = useState<Set<string>>(new Set());
  const [filterType, setFilterType] = useState<string>('all');

  // Mock data for development - Replace with actual API call
  useEffect(() => {
    const mockEntries: AuditEntry[] = [
      {
        id: '1',
        timestamp: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
        eventType: 'UPDATED',
        entityType,
        entityId,
        userId: 'user-1',
        userName: 'Max Mustermann',
        userRole: 'manager',
        changeReason: 'Kontaktdaten aktualisiert',
        oldValue: { phone: '+49 123 456789' },
        newValue: { phone: '+49 123 456790' },
        ipAddress: '192.168.1.1',
        source: 'WEB',
      },
      {
        id: '2',
        timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
        eventType: 'CREATED',
        entityType,
        entityId,
        userId: 'user-2',
        userName: 'Anna Schmidt',
        userRole: 'sales',
        changeReason: 'Neuer Kunde angelegt',
        newValue: {
          companyName: 'Beispiel GmbH',
          city: 'Berlin',
          industry: 'IT',
        },
        ipAddress: '192.168.1.2',
        source: 'WEB',
      },
      {
        id: '3',
        timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
        eventType: 'VIEWED',
        entityType,
        entityId,
        userId: 'user-3',
        userName: 'Peter Müller',
        userRole: 'admin',
        ipAddress: '192.168.1.3',
        source: 'API',
      },
    ];

    // Simulate API call delay
    setTimeout(() => {
      setEntries(mockEntries);
      setLoading(false);
    }, 500);

    // TODO: Replace with actual API call
    // fetchAuditEntries(entityType, entityId)
    //   .then(data => {
    //     setEntries(data);
    //     setLoading(false);
    //   })
    //   .catch(err => {
    //     setError('Fehler beim Laden der Audit-Historie');
    //     setLoading(false);
    //   });
  }, [entityType, entityId]);

  const toggleExpanded = (id: string) => {
    const newExpanded = new Set(expandedItems);
    if (newExpanded.has(id)) {
      newExpanded.delete(id);
    } else {
      newExpanded.add(id);
    }
    setExpandedItems(newExpanded);
  };

  const handleExport = () => {
    // TODO: Implement export functionality
  };

  const filteredEntries = entries.filter(entry => {
    if (filterType !== 'all' && entry.eventType !== filterType) {
      return false;
    }
    if (searchTerm && !JSON.stringify(entry).toLowerCase().includes(searchTerm.toLowerCase())) {
      return false;
    }
    return true;
  });

  if (loading) {
    return (
      <Box>
        {[...Array(3)].map((_, i) => (
          <Box key={i} sx={{ mb: 2 }}>
            <Skeleton variant="text" width="30%" height={24} />
            <Skeleton variant="rectangular" height={60} sx={{ mt: 1 }} />
          </Box>
        ))}
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        {error}
        <Button size="small" onClick={() => window.location.reload()} sx={{ ml: 2 }}>
          Erneut versuchen
        </Button>
      </Alert>
    );
  }

  if (filteredEntries.length === 0) {
    return (
      <Alert severity="info">
        Keine Audit-Einträge gefunden.
        {filterType !== 'all' && (
          <Button size="small" onClick={() => setFilterType('all')} sx={{ ml: 2 }}>
            Filter zurücksetzen
          </Button>
        )}
      </Alert>
    );
  }

  return (
    <Box>
      {/* Filter and Export Bar */}
      {showFilters && (
        <Box sx={{ mb: 3, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <TextField
            select
            size="small"
            label="Ereignistyp"
            value={filterType}
            onChange={(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
              setFilterType(e.target.value)
            }
            sx={{ minWidth: 200 }}
          >
            <MenuItem value="all">Alle Ereignisse</MenuItem>
            <MenuItem value="CREATED">Erstellt</MenuItem>
            <MenuItem value="UPDATED">Aktualisiert</MenuItem>
            <MenuItem value="DELETED">Gelöscht</MenuItem>
            <MenuItem value="VIEWED">Angesehen</MenuItem>
            <MenuItem value="PERMISSION_CHANGED">Berechtigung geändert</MenuItem>
          </TextField>

          <Box sx={{ flex: 1 }} />

          {showExportButton && (
            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              onClick={handleExport}
              size="small"
            >
              Export
            </Button>
          )}

          <IconButton size="small" onClick={() => window.location.reload()}>
            <RefreshIcon />
          </IconButton>
        </Box>
      )}

      {/* Timeline */}
      <Timeline position={compact ? 'right' : 'alternate'}>
        {filteredEntries.slice(0, maxItems).map((entry, index) => {
          const config = getEventConfig(entry.eventType);
          const isExpanded = expandedItems.has(entry.id);
          const hasDetails = entry.oldValue || entry.newValue || entry.changeReason;

          return (
            <TimelineItem key={entry.id}>
              {!compact && (
                <TimelineOppositeContent color="text.secondary">
                  <Typography variant="body2">
                    {format(parseISO(entry.timestamp), 'dd.MM.yyyy', { locale: de })}
                  </Typography>
                  <Typography variant="caption">
                    {format(parseISO(entry.timestamp), 'HH:mm:ss', { locale: de })}
                  </Typography>
                </TimelineOppositeContent>
              )}

              <TimelineSeparator>
                <TimelineDot sx={{ bgcolor: config.color }}>{config.icon}</TimelineDot>
                {index < filteredEntries.length - 1 && <TimelineConnector />}
              </TimelineSeparator>

              <TimelineContent>
                <Card variant="outlined">
                  <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                      <Typography variant="subtitle2" fontWeight="medium">
                        {config.label}
                      </Typography>
                      {compact && (
                        <Typography variant="caption" color="text.secondary">
                          • {format(parseISO(entry.timestamp), 'dd.MM.yyyy HH:mm', { locale: de })}
                        </Typography>
                      )}
                      {hasDetails && (
                        <IconButton
                          size="small"
                          onClick={() => toggleExpanded(entry.id)}
                          sx={{ ml: 'auto' }}
                        >
                          {isExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                        </IconButton>
                      )}
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                      <PersonIcon fontSize="small" color="action" />
                      <Typography variant="body2">{entry.userName}</Typography>
                      <Chip label={entry.userRole} size="small" variant="outlined" />
                    </Box>

                    {entry.changeReason && (
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        "{entry.changeReason}"
                      </Typography>
                    )}

                    <Collapse in={isExpanded}>
                      {(entry.oldValue || entry.newValue) && (
                        <Box sx={{ mt: 2, p: 1, bgcolor: 'grey.50', borderRadius: 1 }}>
                          <Typography variant="caption" fontWeight="medium" gutterBottom>
                            Geänderte Felder:
                          </Typography>
                          {formatChangedFields(entry.oldValue, entry.newValue).map((change, i) => (
                            <Typography
                              key={i}
                              variant="caption"
                              display="block"
                              sx={{ fontFamily: 'monospace' }}
                            >
                              {change}
                            </Typography>
                          ))}
                        </Box>
                      )}

                      <Box sx={{ mt: 1, display: 'flex', gap: 2 }}>
                        {entry.ipAddress && (
                          <Typography variant="caption" color="text.secondary">
                            IP: {entry.ipAddress}
                          </Typography>
                        )}
                        {entry.source && (
                          <Typography variant="caption" color="text.secondary">
                            Quelle: {entry.source}
                          </Typography>
                        )}
                      </Box>
                    </Collapse>
                  </CardContent>
                </Card>
              </TimelineContent>
            </TimelineItem>
          );
        })}
      </Timeline>

      {filteredEntries.length > maxItems && (
        <Alert severity="info" sx={{ mt: 2 }}>
          Es werden nur die ersten {maxItems} Einträge angezeigt.
          {showExportButton && ' Nutzen Sie den Export für alle Einträge.'}
        </Alert>
      )}
    </Box>
  );
}
