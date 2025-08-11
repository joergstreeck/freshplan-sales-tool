import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Alert,
  AlertTitle,
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemSecondaryAction,
  CircularProgress,
  Tooltip,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
  Schedule as ScheduleIcon,
  Assignment as TaskIcon,
  Update as UpdateIcon,
  CheckCircle as CheckCircleIcon,
  Group as GroupIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { contactInteractionApi } from '../../services/contactInteractionApi';
import { DataFreshnessIndicator } from './DataFreshnessIndicator';
import type { Contact } from '../../types/contact.types';

/**
 * Erweiterte Komponente für Data Freshness Management.
 *
 * Implementiert proaktive Update-Kampagnen und Bulk-Update-Funktionalität
 * basierend auf dem Data Strategy Intelligence Konzept.
 */
export const DataFreshnessManager: React.FC = () => {
  const [selectedLevel, setSelectedLevel] = useState<'aging' | 'stale' | 'critical' | null>(null);
  const [bulkUpdateDialog, setBulkUpdateDialog] = useState(false);

  const queryClient = useQueryClient();

  // Load freshness statistics
  const { data: statistics, isLoading: statsLoading } = useQuery({
    queryKey: ['freshness-statistics'],
    queryFn: contactInteractionApi.getFreshnessStatistics,
    refetchInterval: 2 * 60 * 1000, // Refresh every 2 minutes
  });

  // Load contacts needing updates
  const { data: contactsNeedingUpdate, isLoading: contactsLoading } = useQuery({
    queryKey: ['contacts-needing-update'],
    queryFn: contactInteractionApi.getContactsNeedingUpdate,
    enabled: true,
    refetchInterval: 5 * 60 * 1000, // Refresh every 5 minutes
  });

  // Load specific level contacts
  const { data: levelContacts, isLoading: levelLoading } = useQuery({
    queryKey: ['contacts-by-level', selectedLevel],
    queryFn: () =>
      selectedLevel
        ? contactInteractionApi.getContactsByFreshnessLevel(selectedLevel)
        : Promise.resolve([]),
    enabled: !!selectedLevel,
  });

  // Manual hygiene check mutation
  const hygieneCheckMutation = useMutation({
    mutationFn: contactInteractionApi.triggerHygieneCheck,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['freshness-statistics'] });
      queryClient.invalidateQueries({ queryKey: ['contacts-needing-update'] });
      queryClient.invalidateQueries({ queryKey: ['dataQualityMetrics'] });
    },
  });

  const handleTriggerHygieneCheck = () => {
    hygieneCheckMutation.mutate();
  };

  const handleLevelClick = (level: 'aging' | 'stale' | 'critical') => {
    setSelectedLevel(level);
  };

  const handleContactUpdate = (contact: Contact) => {
    // Navigate to contact edit page
    window.open(`/customers/edit/${contact.id}`, '_blank');
  };

  const handleBulkUpdate = () => {
    setBulkUpdateDialog(true);
  };

  if (statsLoading || contactsLoading) {
    return (
      <Box display="flex" justifyContent="center" p={4}>
        <CircularProgress />
      </Box>
    );
  }

  const _totalContacts = statistics
    ? statistics.FRESH + statistics.AGING + statistics.STALE + statistics.CRITICAL
    : 0;

  const criticalCount = contactsNeedingUpdate?.critical?.length || 0;
  const staleCount = contactsNeedingUpdate?.stale?.length || 0;
  const totalNeedingUpdate = criticalCount + staleCount;

  return (
    <Box>
      {/* Header with Actions */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" component="h2">
          Data Freshness Management
        </Typography>
        <Box>
          <Tooltip title="Manuelle Datenprüfung starten">
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={handleTriggerHygieneCheck}
              disabled={hygieneCheckMutation.isPending}
              sx={{ mr: 1 }}
            >
              {hygieneCheckMutation.isPending ? 'Prüfe...' : 'Daten prüfen'}
            </Button>
          </Tooltip>
          <Button
            variant="contained"
            startIcon={<TaskIcon />}
            onClick={handleBulkUpdate}
            disabled={totalNeedingUpdate === 0}
          >
            Bulk Update ({totalNeedingUpdate})
          </Button>
        </Box>
      </Box>

      {/* Alert for Critical Contacts */}
      {criticalCount > 0 && (
        <Alert severity="error" sx={{ mb: 3 }}>
          <AlertTitle>Dringender Handlungsbedarf</AlertTitle>
          {criticalCount} Kontakte wurden über ein Jahr nicht aktualisiert und benötigen sofortige
          Überprüfung.
          <Button
            size="small"
            color="inherit"
            onClick={() => handleLevelClick('critical')}
            sx={{ ml: 2 }}
          >
            Kontakte anzeigen
          </Button>
        </Alert>
      )}

      {/* Statistics Overview */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 3 }}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <CheckCircleIcon color="success" fontSize="large" />
                <Box>
                  <Typography variant="h4" color="success.main">
                    {statistics?.FRESH || 0}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    Aktuelle Kontakte
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <Card sx={{ cursor: 'pointer' }} onClick={() => handleLevelClick('aging')}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <ScheduleIcon color="info" fontSize="large" />
                <Box>
                  <Typography variant="h4" color="info.main">
                    {statistics?.AGING || 0}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    Bald veraltet (90-180 Tage)
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <Card sx={{ cursor: 'pointer' }} onClick={() => handleLevelClick('stale')}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <WarningIcon color="warning" fontSize="large" />
                <Box>
                  <Typography variant="h4" color="warning.main">
                    {statistics?.STALE || 0}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    Veraltet (180-365 Tage)
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <Card sx={{ cursor: 'pointer' }} onClick={() => handleLevelClick('critical')}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <ErrorIcon color="error" fontSize="large" />
                <Box>
                  <Typography variant="h4" color="error.main">
                    {statistics?.CRITICAL || 0}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    Kritisch (&gt;365 Tage)
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Level Detail Dialog */}
      <Dialog open={!!selectedLevel} onClose={() => setSelectedLevel(null)} maxWidth="md" fullWidth>
        <DialogTitle>
          Kontakte:{' '}
          {selectedLevel === 'aging'
            ? 'Bald veraltet'
            : selectedLevel === 'stale'
              ? 'Veraltet'
              : 'Kritisch'}
        </DialogTitle>
        <DialogContent>
          {levelLoading ? (
            <Box display="flex" justifyContent="center" p={2}>
              <CircularProgress />
            </Box>
          ) : (
            <List>
              {levelContacts?.map(contact => (
                <ListItem key={contact.id} divider>
                  <ListItemIcon>
                    <GroupIcon />
                  </ListItemIcon>
                  <ListItemText
                    primary={`${contact.firstName} ${contact.lastName}`}
                    secondary={
                      <Box>
                        <Typography variant="body2" color="textSecondary">
                          {contact.position} - {contact.email}
                        </Typography>
                        <Typography variant="body2" color="textSecondary">
                          Zuletzt aktualisiert:{' '}
                          {contact.updatedAt
                            ? new Date(contact.updatedAt).toLocaleDateString('de-DE')
                            : 'Unbekannt'}
                        </Typography>
                      </Box>
                    }
                  />
                  <ListItemSecondaryAction>
                    <DataFreshnessIndicator
                      contact={contact}
                      variant="chip"
                      showUpdateButton={false}
                    />
                    <Button
                      size="small"
                      startIcon={<UpdateIcon />}
                      onClick={() => handleContactUpdate(contact)}
                      sx={{ ml: 1 }}
                    >
                      Bearbeiten
                    </Button>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSelectedLevel(null)}>Schließen</Button>
        </DialogActions>
      </Dialog>

      {/* Bulk Update Dialog */}
      <Dialog
        open={bulkUpdateDialog}
        onClose={() => setBulkUpdateDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Bulk Update</DialogTitle>
        <DialogContent>
          <Typography variant="body1" gutterBottom>
            Möchten Sie alle {totalNeedingUpdate} Kontakte markieren, die ein Update benötigen?
          </Typography>
          <Box mt={2}>
            <Typography variant="body2" color="textSecondary">
              • {staleCount} veraltete Kontakte (180-365 Tage)
            </Typography>
            <Typography variant="body2" color="textSecondary">
              • {criticalCount} kritische Kontakte (&gt;365 Tage)
            </Typography>
          </Box>
          <Alert severity="info" sx={{ mt: 2 }}>
            Diese Funktion wird in einer späteren Version implementiert. Verwenden Sie vorerst die
            Einzelbearbeitung.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBulkUpdateDialog(false)}>Abbrechen</Button>
          <Button variant="contained" disabled>
            Markieren (Coming Soon)
          </Button>
        </DialogActions>
      </Dialog>

      {/* Success Message */}
      {hygieneCheckMutation.isSuccess && (
        <Alert severity="success" sx={{ mt: 2 }}>
          Datenprüfung erfolgreich abgeschlossen. Alle Metriken wurden aktualisiert.
        </Alert>
      )}

      {/* Error Message */}
      {hygieneCheckMutation.error && (
        <Alert severity="error" sx={{ mt: 2 }}>
          Fehler bei der Datenprüfung. Bitte versuchen Sie es später erneut.
        </Alert>
      )}
    </Box>
  );
};
