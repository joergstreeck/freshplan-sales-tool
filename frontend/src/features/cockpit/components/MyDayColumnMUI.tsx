/**
 * Mein Tag - Spalte 1 des Sales Cockpit (MUI Version)
 *
 * Zeigt proaktiv die wichtigsten Aufgaben, Termine und KI-gestützte Alarme
 * Beinhaltet die Triage-Inbox für nicht zugeordnete Kommunikation
 */

import { useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Stack,
  IconButton,
  Button,
  CircularProgress,
  Alert,
  AlertTitle,
  Chip,
  Collapse,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import PhoneIcon from '@mui/icons-material/Phone';
import EmailIcon from '@mui/icons-material/Email';
import EventIcon from '@mui/icons-material/Event';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import WarningIcon from '@mui/icons-material/Warning';
import NotificationsIcon from '@mui/icons-material/Notifications';
import InfoIcon from '@mui/icons-material/Info';
import { useCockpitStore } from '../../../store/cockpitStore';
import { useAuth } from '../../../hooks/useAuth';
import { useDashboardData } from '../hooks/useSalesCockpit';
// Mock data imports removed - should use real data from API
import type { DashboardTask, DashboardAlert } from '../types/salesCockpit';
import type { PriorityTask } from '../types';

export function MyDayColumnMUI() {
  const { showTriageInbox, toggleTriageInbox, setPriorityTasksCount } = useCockpitStore();
  const { userId } = useAuth();

  // Hole Dashboard-Daten via BFF
  const { data: dashboardData, isLoading, isError, error, refetch } = useDashboardData(userId);

  // Update Priority Task Count when data changes
  useEffect(() => {
    if (dashboardData?.todaysTasks) {
      const highPriorityCount = dashboardData.todaysTasks.filter(
        task => task.priority === 'HIGH'
      ).length;
      setPriorityTasksCount(highPriorityCount);
    }
  }, [dashboardData?.todaysTasks, setPriorityTasksCount]);

  const getTaskIcon = (type: DashboardTask['type']) => {
    switch (type) {
      case 'CALL':
        return <PhoneIcon fontSize="small" />;
      case 'EMAIL':
        return <EmailIcon fontSize="small" />;
      case 'APPOINTMENT':
        return <EventIcon fontSize="small" />;
      case 'TODO':
        return <CheckCircleIcon fontSize="small" />;
      case 'FOLLOW_UP':
        return <AutorenewIcon fontSize="small" />;
      default:
        return <CheckCircleIcon fontSize="small" />;
    }
  };

  const getPriorityColor = (priority: DashboardTask['priority']): 'error' | 'warning' | 'info' => {
    switch (priority) {
      case 'HIGH':
        return 'error';
      case 'MEDIUM':
        return 'warning';
      default:
        return 'info';
    }
  };

  const getAlertIcon = (type: DashboardAlert['type']) => {
    switch (type) {
      case 'OPPORTUNITY':
        return <AttachMoneyIcon />;
      case 'RISK':
        return <WarningIcon />;
      case 'REMINDER':
        return <NotificationsIcon />;
      case 'INFO':
        return <InfoIcon />;
      default:
        return <InfoIcon />;
    }
  };

  const formatTime = (dateString?: string) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('de-DE', {
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
  };

  // Loading State
  if (isLoading) {
    return (
      <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
          <Typography variant="h6" sx={{ color: 'primary.main' }}>
            Mein Tag
          </Typography>
        </Box>
        <Box
          sx={{
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <CircularProgress size={40} />
          <Typography sx={{ mt: 2 }} color="text.secondary">
            Lade Dashboard-Daten...
          </Typography>
        </Box>
      </Card>
    );
  }

  // Error State
  if (isError) {
    return (
      <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
          <Typography variant="h6" sx={{ color: 'primary.main' }}>
            Mein Tag
          </Typography>
        </Box>
        <Box sx={{ p: 2 }}>
          <Alert
            severity="error"
            action={
              <Button color="inherit" size="small" onClick={() => refetch()}>
                Erneut versuchen
              </Button>
            }
          >
            <AlertTitle>Fehler beim Laden der Daten</AlertTitle>
            {error?.message || 'Unbekannter Fehler beim Abrufen der Dashboard-Daten'}
          </Alert>
        </Box>
      </Card>
    );
  }

  // Extract data from BFF response
  const bffTasks = dashboardData?.todaysTasks || [];
  const alerts = dashboardData?.alerts || [];
  const todaysAlerts = alerts.filter(
    alert => new Date(alert.createdAt).toDateString() === new Date().toDateString()
  );

  // Use empty array if there's an error or no data
  const tasks: PriorityTask[] =
    bffTasks.length > 0
      ? bffTasks.map(task => ({
          id: task.id,
          title: task.title,
          customerName: task.customerName,
          type: task.type.toLowerCase() as PriorityTask['type'],
          priority: task.priority.toLowerCase() as PriorityTask['priority'],
          dueDate: task.dueDate ? new Date(task.dueDate) : undefined,
          completed: false,
        }))
      : isError || !dashboardData
        ? []
        : [];

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <Box
        sx={{
          p: 2,
          borderBottom: 1,
          borderColor: 'divider',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Typography variant="h6" sx={{ color: 'primary.main' }}>
          Mein Tag
        </Typography>
        <IconButton
          size="small"
          onClick={() => refetch()}
          title="Aktualisieren"
          aria-label="Aufgaben aktualisieren"
        >
          <RefreshIcon />
        </IconButton>
      </Box>

      {/* Content */}
      <Box sx={{ flex: 1, overflow: 'auto', p: 2 }}>
        <Stack spacing={3}>
          {/* KI-Alerts von BFF */}
          {todaysAlerts.length > 0 && (
            <Box>
              <Typography variant="subtitle1" gutterBottom sx={{ fontWeight: 'medium' }}>
                Wichtige Hinweise
              </Typography>
              <Stack spacing={1}>
                {todaysAlerts.slice(0, 3).map(alert => (
                  <Card
                    key={alert.id}
                    variant="outlined"
                    sx={{
                      borderColor:
                        alert.severity === 'HIGH'
                          ? 'error.main'
                          : alert.severity === 'MEDIUM'
                            ? 'warning.main'
                            : 'primary.main',
                      borderWidth: 2,
                    }}
                  >
                    <CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}>
                      <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                        <Box
                          sx={{
                            color:
                              alert.severity === 'HIGH'
                                ? 'error.main'
                                : alert.severity === 'MEDIUM'
                                  ? 'warning.main'
                                  : 'primary.main',
                          }}
                        >
                          {getAlertIcon(alert.type)}
                        </Box>
                        <Box sx={{ flex: 1 }}>
                          <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                            {alert.title}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {alert.message}
                          </Typography>
                          {alert.customerName && (
                            <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
                              <Chip label={alert.customerName} size="small" />
                            </Typography>
                          )}
                          {alert.actionLink && (
                            <Button size="small" sx={{ mt: 1 }}>
                              Aktion ausführen
                            </Button>
                          )}
                        </Box>
                      </Box>
                    </CardContent>
                  </Card>
                ))}
              </Stack>
            </Box>
          )}

          {/* Prioritäts-Aufgaben */}
          <Box>
            <Typography variant="subtitle1" gutterBottom sx={{ fontWeight: 'medium' }}>
              Aufgaben ({tasks.length})
            </Typography>
            <Stack spacing={1}>
              {tasks.map(task => (
                <Card key={task.id}>
                  <CardContent
                    sx={{
                      py: 1.5,
                      '&:last-child': { pb: 1.5 },
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.5,
                    }}
                  >
                    <Box sx={{ color: getPriorityColor(task.priority) }}>
                      {getTaskIcon(task.type)}
                    </Box>
                    <Box sx={{ flex: 1 }}>
                      <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                        {task.title}
                      </Typography>
                      {task.customerName && (
                        <Typography variant="caption" color="text.secondary">
                          {task.customerName}
                        </Typography>
                      )}
                    </Box>
                    {task.dueDate && (
                      <Typography variant="caption" color="text.secondary">
                        {formatTime(task.dueDate.toISOString())}
                      </Typography>
                    )}
                  </CardContent>
                </Card>
              ))}
            </Stack>
          </Box>

          {/* Triage-Inbox - Vorerst Mock-Daten */}
          <Box>
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                mb: 1,
              }}
            >
              <Typography variant="subtitle1" sx={{ fontWeight: 'medium' }}>
                Posteingang (0)
              </Typography>
              <IconButton
                size="small"
                onClick={toggleTriageInbox}
                aria-expanded={showTriageInbox}
                aria-label={showTriageInbox ? 'Posteingang ausblenden' : 'Posteingang anzeigen'}
              >
                <ExpandMoreIcon
                  sx={{
                    transform: showTriageInbox ? 'rotate(180deg)' : 'rotate(0deg)',
                    transition: 'transform 0.2s',
                  }}
                />
              </IconButton>
            </Box>

            <Collapse in={showTriageInbox}>
              <Stack spacing={1}>
                {/* TODO: Replace with real triage items from API */}
                {[].map((item: unknown) => (
                  <Card key={item.id}>
                    <CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}>
                      <Box
                        sx={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          mb: 0.5,
                        }}
                      >
                        <Typography variant="caption" sx={{ fontWeight: 'medium' }}>
                          {item.from}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(item.receivedAt).toLocaleTimeString('de-DE', {
                            hour: '2-digit',
                            minute: '2-digit',
                          })}
                        </Typography>
                      </Box>
                      <Typography variant="body2" gutterBottom>
                        {item.subject}
                      </Typography>
                      {item.content && (
                        <Typography
                          variant="caption"
                          color="text.secondary"
                          sx={{
                            display: '-webkit-box',
                            WebkitLineClamp: 2,
                            WebkitBoxOrient: 'vertical',
                            overflow: 'hidden',
                          }}
                        >
                          {item.content}
                        </Typography>
                      )}
                      <Box sx={{ mt: 1, display: 'flex', gap: 1 }}>
                        <Button size="small" variant="outlined">
                          Zuordnen
                        </Button>
                        <Button size="small" variant="outlined">
                          Als Interessent
                        </Button>
                      </Box>
                    </CardContent>
                  </Card>
                ))}
              </Stack>
            </Collapse>
          </Box>
        </Stack>
      </Box>
    </Card>
  );
}
