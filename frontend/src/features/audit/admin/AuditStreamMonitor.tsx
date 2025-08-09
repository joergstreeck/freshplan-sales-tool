import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  Typography,
  List,
  ListItem,
  Chip,
  IconButton,
  Badge,
  Tooltip,
  Switch,
  FormControlLabel,
  Alert,
  CircularProgress,
  Fade,
  Divider,
  Avatar,
} from '@mui/material';
import {
  PlayArrow as PlayIcon,
  Pause as PauseIcon,
  Clear as ClearIcon,
  FilterList as FilterIcon,
  Info as InfoIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
  CheckCircle as CheckIcon,
  Person as PersonIcon,
  Security as SecurityIcon,
  Storage as StorageIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';

interface AuditStreamEntry {
  id: string;
  timestamp: Date;
  userId: string;
  userName?: string;
  action: string;
  entityType: string;
  entityId: string;
  severity?: 'info' | 'warning' | 'error' | 'success';
  ipAddress?: string;
  details?: string;
  isDsgvoRelevant?: boolean;
  isCritical?: boolean;
}

interface AuditStreamMonitorProps {
  maxEntries?: number;
  autoScroll?: boolean;
  filterCritical?: boolean;
  onEntryClick?: (entry: AuditStreamEntry) => void;
  height?: number;
}

export const AuditStreamMonitor: React.FC<AuditStreamMonitorProps> = ({
  maxEntries = 50,
  autoScroll = true,
  filterCritical = false,
  onEntryClick,
  height = 400,
}) => {
  const [entries, setEntries] = useState<AuditStreamEntry[]>([]);
  const [isPaused, setIsPaused] = useState(false);
  const [isConnected, setIsConnected] = useState(false);
  const [showOnlyCritical, setShowOnlyCritical] = useState(filterCritical);
  const [newEntriesCount, setNewEntriesCount] = useState(0);
  const listRef = useRef<HTMLDivElement>(null);
  const wsRef = useRef<WebSocket | null>(null);

  // Simulate WebSocket connection for real-time events
  useEffect(() => {
    // In production, this would be a real WebSocket connection
    const connectWebSocket = () => {
      // Simulate connection
      setIsConnected(true);

      // Simulate incoming events
      const interval = setInterval(
        () => {
          if (!isPaused) {
            const mockEntry: AuditStreamEntry = {
              id: `audit-${Date.now()}`,
              timestamp: new Date(),
              userId: `user-${Math.floor(Math.random() * 100)}`,
              userName: ['Max Mustermann', 'Anna Schmidt', 'Peter Weber'][
                Math.floor(Math.random() * 3)
              ],
              action: ['CREATE', 'UPDATE', 'DELETE', 'READ', 'LOGIN', 'EXPORT'][
                Math.floor(Math.random() * 6)
              ],
              entityType: ['Customer', 'Order', 'Product', 'User'][Math.floor(Math.random() * 4)],
              entityId: `entity-${Math.floor(Math.random() * 1000)}`,
              severity: Math.random() > 0.8 ? 'warning' : Math.random() > 0.95 ? 'error' : 'info',
              ipAddress: `192.168.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
              isDsgvoRelevant: Math.random() > 0.7,
              isCritical: Math.random() > 0.9,
            };

            setEntries(prev => {
              const newEntries = [mockEntry, ...prev].slice(0, maxEntries);
              if (prev.length > 0) {
                setNewEntriesCount(count => count + 1);
              }
              return newEntries;
            });
          }
        },
        Math.random() * 3000 + 2000
      ); // Random interval between 2-5 seconds

      return () => clearInterval(interval);
    };

    const cleanup = connectWebSocket();

    return () => {
      cleanup();
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, [isPaused, maxEntries]);

  // Auto-scroll effect
  useEffect(() => {
    if (autoScroll && !isPaused && listRef.current) {
      listRef.current.scrollTop = 0;
    }
  }, [entries, autoScroll, isPaused]);

  // Reset new entries count when unpaused
  useEffect(() => {
    if (!isPaused) {
      setNewEntriesCount(0);
    }
  }, [isPaused]);

  const getActionIcon = (action: string) => {
    switch (action) {
      case 'CREATE':
        return <CheckIcon fontSize="small" color="success" />;
      case 'UPDATE':
        return <InfoIcon fontSize="small" color="info" />;
      case 'DELETE':
        return <ErrorIcon fontSize="small" color="error" />;
      case 'LOGIN':
        return <SecurityIcon fontSize="small" color="primary" />;
      case 'EXPORT':
        return <StorageIcon fontSize="small" color="warning" />;
      default:
        return <InfoIcon fontSize="small" />;
    }
  };

  const getSeverityColor = (severity?: string) => {
    switch (severity) {
      case 'error':
        return 'error';
      case 'warning':
        return 'warning';
      case 'success':
        return 'success';
      default:
        return 'default';
    }
  };

  const filteredEntries = showOnlyCritical
    ? entries.filter(e => e.isCritical || e.severity === 'error' || e.severity === 'warning')
    : entries;

  const handleClear = () => {
    setEntries([]);
    setNewEntriesCount(0);
  };

  return (
    <Paper sx={{ height, display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <Box
        sx={{
          p: 2,
          borderBottom: 1,
          borderColor: 'divider',
          bgcolor: 'background.paper',
        }}
      >
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box display="flex" alignItems="center" gap={1}>
            <Typography
              variant="h6"
              sx={{
                fontFamily: 'Antonio, sans-serif',
                fontWeight: 'bold',
              }}
            >
              Live Activity Stream
            </Typography>

            {/* Connection Status */}
            <Chip
              icon={isConnected ? <CheckIcon /> : <ErrorIcon />}
              label={isConnected ? 'Verbunden' : 'Getrennt'}
              color={isConnected ? 'success' : 'error'}
              size="small"
              variant="outlined"
            />

            {/* New entries badge */}
            {isPaused && newEntriesCount > 0 && (
              <Fade in>
                <Chip
                  label={`${newEntriesCount} neue Events`}
                  color="primary"
                  size="small"
                  sx={{
                    animation: 'pulse 1.5s infinite',
                    '@keyframes pulse': {
                      '0%': { opacity: 1 },
                      '50%': { opacity: 0.5 },
                      '100%': { opacity: 1 },
                    },
                  }}
                />
              </Fade>
            )}
          </Box>

          <Box display="flex" alignItems="center" gap={1}>
            {/* Filter Toggle */}
            <FormControlLabel
              control={
                <Switch
                  checked={showOnlyCritical}
                  onChange={e => setShowOnlyCritical(e.target.checked)}
                  size="small"
                />
              }
              label="Nur kritisch"
              sx={{
                '& .MuiFormControlLabel-label': {
                  fontSize: '0.875rem',
                  fontFamily: 'Poppins, sans-serif',
                },
              }}
            />

            {/* Control Buttons */}
            <Tooltip title={isPaused ? 'Fortsetzen' : 'Pausieren'}>
              <IconButton
                onClick={() => setIsPaused(!isPaused)}
                color={isPaused ? 'primary' : 'default'}
              >
                <Badge badgeContent={newEntriesCount} color="error">
                  {isPaused ? <PlayIcon /> : <PauseIcon />}
                </Badge>
              </IconButton>
            </Tooltip>

            <Tooltip title="Leeren">
              <IconButton onClick={handleClear}>
                <ClearIcon />
              </IconButton>
            </Tooltip>
          </Box>
        </Box>
      </Box>

      {/* Stream Content */}
      <Box
        ref={listRef}
        sx={{
          flex: 1,
          overflow: 'auto',
          bgcolor: 'background.default',
        }}
      >
        {filteredEntries.length === 0 ? (
          <Box
            display="flex"
            alignItems="center"
            justifyContent="center"
            height="100%"
            flexDirection="column"
            gap={2}
          >
            {isConnected ? (
              <>
                <CircularProgress sx={{ color: '#94C456' }} />
                <Typography color="text.secondary">Warte auf Events...</Typography>
              </>
            ) : (
              <Alert severity="error">Keine Verbindung zum Event Stream</Alert>
            )}
          </Box>
        ) : (
          <List sx={{ p: 0 }}>
            {filteredEntries.map((entry, index) => (
              <React.Fragment key={entry.id}>
                <ListItem
                  onClick={() => onEntryClick?.(entry)}
                  sx={{
                    py: 1.5,
                    px: 2,
                    cursor: onEntryClick ? 'pointer' : 'default',
                    transition: 'background-color 0.2s',
                    '&:hover': onEntryClick
                      ? {
                          bgcolor: 'action.hover',
                        }
                      : {},
                    animation: index === 0 && !isPaused ? 'slideIn 0.3s ease-out' : 'none',
                    '@keyframes slideIn': {
                      from: {
                        transform: 'translateY(-20px)',
                        opacity: 0,
                      },
                      to: {
                        transform: 'translateY(0)',
                        opacity: 1,
                      },
                    },
                  }}
                >
                  <Box display="flex" gap={2} alignItems="flex-start" width="100%">
                    {/* Time */}
                    <Typography
                      variant="caption"
                      sx={{
                        minWidth: 60,
                        fontFamily: 'monospace',
                        color: 'text.secondary',
                      }}
                    >
                      {format(entry.timestamp, 'HH:mm:ss')}
                    </Typography>

                    {/* Action Icon */}
                    <Box sx={{ mt: 0.25 }}>{getActionIcon(entry.action)}</Box>

                    {/* Main Content */}
                    <Box flex={1}>
                      <Box display="flex" gap={1} alignItems="center" flexWrap="wrap">
                        {/* User */}
                        <Chip
                          icon={<PersonIcon />}
                          label={entry.userName || entry.userId}
                          size="small"
                          variant="outlined"
                          sx={{ height: 24 }}
                        />

                        {/* Action */}
                        <Chip
                          label={entry.action}
                          size="small"
                          color={getSeverityColor(entry.severity)}
                          sx={{ height: 24 }}
                        />

                        {/* Entity */}
                        <Typography variant="body2">
                          {entry.entityType} #{entry.entityId}
                        </Typography>

                        {/* Badges */}
                        {entry.isDsgvoRelevant && (
                          <Chip
                            label="DSGVO"
                            size="small"
                            color="info"
                            variant="outlined"
                            sx={{ height: 20 }}
                          />
                        )}

                        {entry.isCritical && (
                          <Chip
                            icon={<WarningIcon />}
                            label="Kritisch"
                            size="small"
                            color="error"
                            sx={{ height: 20 }}
                          />
                        )}
                      </Box>

                      {/* Details */}
                      {entry.details && (
                        <Typography
                          variant="caption"
                          color="text.secondary"
                          sx={{ mt: 0.5, display: 'block' }}
                        >
                          {entry.details}
                        </Typography>
                      )}

                      {/* IP Address */}
                      {entry.ipAddress && (
                        <Typography
                          variant="caption"
                          sx={{
                            color: 'text.disabled',
                            fontFamily: 'monospace',
                          }}
                        >
                          IP: {entry.ipAddress}
                        </Typography>
                      )}
                    </Box>
                  </Box>
                </ListItem>
                {index < filteredEntries.length - 1 && <Divider />}
              </React.Fragment>
            ))}
          </List>
        )}
      </Box>

      {/* Footer Stats */}
      <Box
        sx={{
          p: 1,
          borderTop: 1,
          borderColor: 'divider',
          bgcolor: 'background.paper',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Typography variant="caption" color="text.secondary">
          {filteredEntries.length} von {entries.length} Events angezeigt
        </Typography>

        <Typography variant="caption" color="text.secondary">
          Max. {maxEntries} Events im Speicher
        </Typography>
      </Box>
    </Paper>
  );
};
