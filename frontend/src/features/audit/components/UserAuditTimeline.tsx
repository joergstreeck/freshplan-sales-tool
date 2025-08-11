/**
 * User Audit Timeline Component
 * 
 * Displays audit history for a specific user with filtering and export capabilities.
 * Part of FC-005 Contact Management UI - PR 3.
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_TRAIL_SYSTEM.md
 */

import React, { useState } from 'react';
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
  Paper,
  Typography,
  IconButton,
  Tooltip,
  TextField,
  MenuItem,
  Button,
  Skeleton,
  Alert,
  Avatar,
  Collapse,
  Stack,
} from '@mui/material';
import {
  Create as CreateIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Security as SecurityIcon,
  Person as PersonIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  LocationOn as LocationIcon,
  Business as BusinessIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  Download as DownloadIcon,
  FilterList as FilterIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { format, formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';
import { useQuery } from '@tanstack/react-query';
import type { AuditLog, AuditFilters } from '../types';
import { auditApi } from '../services/auditApi';

interface UserAuditTimelineProps {
  userId: string;
  userName?: string;
  dateRange?: {
    from: Date;
    to: Date;
  };
  maxItems?: number;
  showFilters?: boolean;
  onExport?: () => void;
}

/**
 * Maps audit actions to icons and colors
 */
const getActionConfig = (action: string) => {
  const configs: Record<string, { icon: React.ReactNode; color: string }> = {
    CREATE: { icon: <CreateIcon />, color: 'success' },
    UPDATE: { icon: <EditIcon />, color: 'info' },
    DELETE: { icon: <DeleteIcon />, color: 'error' },
    LOGIN: { icon: <SecurityIcon />, color: 'primary' },
    EXPORT: { icon: <DownloadIcon />, color: 'warning' },
    VIEW: { icon: <PersonIcon />, color: 'default' },
    EMAIL_SENT: { icon: <EmailIcon />, color: 'primary' },
    CALL_LOGGED: { icon: <PhoneIcon />, color: 'success' },
  };
  return configs[action] || { icon: <BusinessIcon />, color: 'default' };
};

/**
 * Formats entity type for display
 */
const formatEntityType = (entityType: string): string => {
  const typeMap: Record<string, string> = {
    CUSTOMER: 'Kunde',
    CONTACT: 'Kontakt',
    OPPORTUNITY: 'Opportunity',
    USER: 'Benutzer',
    DOCUMENT: 'Dokument',
    NOTE: 'Notiz',
    TASK: 'Aufgabe',
  };
  return typeMap[entityType] || entityType;
};

export const UserAuditTimeline: React.FC<UserAuditTimelineProps> = ({
  userId,
  userName,
  _dateRange,
  maxItems = 50,
  showFilters = true,
  onExport,
}) => {
  const [expandedItems, setExpandedItems] = useState<Set<string>>(new Set());
  const [actionFilter, setActionFilter] = useState<string>('ALL');
  const [entityFilter, setEntityFilter] = useState<string>('ALL');

  // Fetch user audit logs
  const {
    data: auditLogs,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['userAuditLogs', userId, _dateRange, actionFilter, entityFilter],
    queryFn: async () => {
      const filters: AuditFilters = {
        userId,
        limit: maxItems,
      };

      if (_dateRange) {
        filters.from = dateRange.from.toISOString();
        filters.to = dateRange.to.toISOString();
      }

      if (actionFilter !== 'ALL') {
        filters.action = actionFilter;
      }

      if (entityFilter !== 'ALL') {
        filters.entityType = entityFilter;
      }

      return auditApi.getAuditLogs(filters);
    },
    refetchInterval: 30000, // Auto-refresh every 30 seconds
  });

  const toggleExpanded = (logId: string) => {
    setExpandedItems((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(logId)) {
        newSet.delete(logId);
      } else {
        newSet.add(logId);
      }
      return newSet;
    });
  };

  const handleExport = () => {
    if (onExport) {
      onExport();
    } else {
      // Default export implementation
      const dataStr = JSON.stringify(auditLogs, null, 2);
      const dataUri = 'data:application/json;charset=utf-8,' + encodeURIComponent(dataStr);
      const exportFileDefaultName = `audit-logs-${userId}-${Date.now()}.json`;

      const linkElement = document.createElement('a');
      linkElement.setAttribute('href', dataUri);
      linkElement.setAttribute('download', exportFileDefaultName);
      linkElement.click();
    }
  };

  if (error) {
    return (
      <Alert severity="error">
        Fehler beim Laden der Audit-Logs: {(error as Error).message}
      </Alert>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box display="flex" alignItems="center" gap={2}>
            <Avatar sx={{ bgcolor: '#004F7B' }}>
              <PersonIcon />
            </Avatar>
            <Box>
              <Typography variant="h6" sx={{ fontFamily: 'Antonio, sans-serif' }}>
                Aktivitätsverlauf
              </Typography>
              {userName && (
                <Typography variant="body2" color="text.secondary">
                  {userName}
                </Typography>
              )}
            </Box>
          </Box>

          <Box display="flex" gap={1}>
            <Tooltip title="Aktualisieren">
              <span>
                <IconButton onClick={() => refetch()} disabled={isLoading}>
                  <RefreshIcon />
                </IconButton>
              </span>
            </Tooltip>
            <Tooltip title="Exportieren">
              <span>
                <IconButton onClick={handleExport} disabled={!auditLogs || auditLogs.length === 0}>
                  <DownloadIcon />
                </IconButton>
              </span>
            </Tooltip>
          </Box>
        </Box>

        {/* Filters */}
        {showFilters && (
          <Box display="flex" gap={2} mt={2}>
            <TextField
              select
              size="small"
              label="Aktion"
              value={actionFilter}
              onChange={(e) => setActionFilter(e.target.value)}
              sx={{ minWidth: 150 }}
            >
              <MenuItem value="ALL">Alle Aktionen</MenuItem>
              <MenuItem value="CREATE">Erstellt</MenuItem>
              <MenuItem value="UPDATE">Bearbeitet</MenuItem>
              <MenuItem value="DELETE">Gelöscht</MenuItem>
              <MenuItem value="VIEW">Angesehen</MenuItem>
              <MenuItem value="EXPORT">Exportiert</MenuItem>
            </TextField>

            <TextField
              select
              size="small"
              label="Entität"
              value={entityFilter}
              onChange={(e) => setEntityFilter(e.target.value)}
              sx={{ minWidth: 150 }}
            >
              <MenuItem value="ALL">Alle Entitäten</MenuItem>
              <MenuItem value="CUSTOMER">Kunden</MenuItem>
              <MenuItem value="CONTACT">Kontakte</MenuItem>
              <MenuItem value="OPPORTUNITY">Opportunities</MenuItem>
              <MenuItem value="DOCUMENT">Dokumente</MenuItem>
            </TextField>
          </Box>
        )}
      </Paper>

      {/* Timeline */}
      <Paper sx={{ p: 2 }}>
        {isLoading ? (
          <Box>
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} variant="rectangular" height={80} sx={{ mb: 2 }} />
            ))}
          </Box>
        ) : auditLogs && auditLogs.length > 0 ? (
          <Timeline position="alternate">
            {auditLogs.map((log, index) => {
              const actionConfig = getActionConfig(log.action);
              const isExpanded = expandedItems.has(log.id);

              return (
                <TimelineItem key={log.id}>
                  <TimelineOppositeContent
                    sx={{ m: 'auto 0' }}
                    align={index % 2 === 0 ? 'right' : 'left'}
                    variant="body2"
                    color="text.secondary"
                  >
                    <Typography variant="caption">
                      {format(new Date(log.occurredAt || log.timestamp), 'dd.MM.yyyy')}
                    </Typography>
                    <br />
                    <Typography variant="caption">
                      {format(new Date(log.occurredAt || log.timestamp), 'HH:mm:ss')}
                    </Typography>
                    <br />
                    <Typography variant="caption" sx={{ fontStyle: 'italic' }}>
                      {formatDistanceToNow(new Date(log.occurredAt || log.timestamp), {
                        addSuffix: true,
                        locale: de,
                      })}
                    </Typography>
                  </TimelineOppositeContent>

                  <TimelineSeparator>
                    <TimelineConnector sx={{ bgcolor: 'grey.300' }} />
                    <TimelineDot color={actionConfig.color as unknown}>
                      {actionConfig.icon}
                    </TimelineDot>
                    <TimelineConnector sx={{ bgcolor: 'grey.300' }} />
                  </TimelineSeparator>

                  <TimelineContent sx={{ py: '12px', px: 2 }}>
                    <Paper
                      elevation={1}
                      sx={{
                        p: 2,
                        cursor: 'pointer',
                        '&:hover': { bgcolor: 'action.hover' },
                      }}
                      onClick={() => toggleExpanded(log.id)}
                    >
                      <Box display="flex" justifyContent="space-between" alignItems="center">
                        <Box>
                          <Typography variant="subtitle2" component="span">
                            {log.action}
                          </Typography>
                          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                            {formatEntityType(log.entityType)} • {log.entityId}
                          </Typography>
                        </Box>
                        <IconButton size="small">
                          {isExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                        </IconButton>
                      </Box>

                      <Collapse in={isExpanded}>
                        <Box sx={{ mt: 2, pt: 2, borderTop: 1, borderColor: 'divider' }}>
                          {/* Additional Details */}
                          <Stack spacing={1}>
                            {log.ipAddress && (
                              <Typography variant="body2">
                                <strong>IP-Adresse:</strong> {log.ipAddress}
                              </Typography>
                            )}
                            {log.userAgent && (
                              <Typography variant="body2">
                                <strong>User Agent:</strong> {log.userAgent}
                              </Typography>
                            )}
                            {log.changes && (
                              <Box>
                                <Typography variant="body2" fontWeight="bold">
                                  Änderungen:
                                </Typography>
                                <Box
                                  sx={{
                                    mt: 1,
                                    p: 1,
                                    bgcolor: 'grey.50',
                                    borderRadius: 1,
                                    fontFamily: 'monospace',
                                    fontSize: '0.85rem',
                                  }}
                                >
                                  <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                                    {JSON.stringify(log.changes, null, 2)}
                                  </pre>
                                </Box>
                              </Box>
                            )}
                          </Stack>
                        </Box>
                      </Collapse>
                    </Paper>
                  </TimelineContent>
                </TimelineItem>
              );
            })}
          </Timeline>
        ) : (
          <Box textAlign="center" py={4}>
            <Typography color="text.secondary">
              Keine Aktivitäten im ausgewählten Zeitraum gefunden
            </Typography>
          </Box>
        )}
      </Paper>
    </Box>
  );
};