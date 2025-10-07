import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { listLeads } from './api';
import type { Lead, Problem } from './types';
import { CircularProgress, Alert, Button, Box, Typography, IconButton, Chip } from '@mui/material';
import { Pause, PlayArrow, Timeline } from '@mui/icons-material';
import LeadCreateDialog from './LeadCreateDialog';
import LeadScoreIndicator from './LeadScoreIndicator';
import LeadStatusWorkflow from './LeadStatusWorkflow';
import StopTheClockDialog from './StopTheClockDialog';
import LeadActivityTimeline from './LeadActivityTimeline';
import { useAuth } from '../../hooks/useAuth';

export default function LeadList() {
  const { t } = useTranslation('leads');
  const { hasRole } = useAuth();
  const [data, setData] = useState<Lead[] | null>(null);
  const [error, setError] = useState<Problem | null>(null);
  const [open, setOpen] = useState(false);
  const [stopClockDialogOpen, setStopClockDialogOpen] = useState(false);
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null);
  const [showTimeline, setShowTimeline] = useState<number | null>(null);

  // RBAC: Stop-the-Clock nur für ADMIN und MANAGER
  const canStopClock = hasRole('ADMIN') || hasRole('MANAGER');

  const fetchLeads = async () => {
    try {
      const leads = await listLeads();
      setData(leads);
      setError(null);
    } catch (err) {
      setError(err);
    }
  };

  useEffect(() => {
    let mounted = true;
    fetchLeads().then(() => {
      if (!mounted) return;
    });
    return () => {
      mounted = false;
    };
  }, []);

  const handleLeadCreated = () => {
    setOpen(false);
    fetchLeads(); // Refresh list after creating
  };

  const handleStopClock = (lead: Lead) => {
    setSelectedLead(lead);
    setStopClockDialogOpen(true);
  };

  const handleStopClockSuccess = () => {
    setStopClockDialogOpen(false);
    setSelectedLead(null);
    fetchLeads(); // Refresh list after stop/resume
  };

  if (!data && !error) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
        <CircularProgress role="progressbar" aria-label="Laden" />
      </Box>
    );
  }

  if (error) {
    return (
      <Box p={2}>
        <Alert severity="error">
          {error.title ?? 'Fehler'}
          {error.detail ? ` – ${error.detail}` : ''}
        </Alert>
      </Box>
    );
  }

  return (
    <Box p={2}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h4" component="h1">
          Lead-Management
        </Typography>
        <Button variant="contained" onClick={() => setOpen(true)}>
          {t('create.button')}
        </Button>
      </Box>

      {data && data.length === 0 ? (
        <Box textAlign="center" py={4}>
          <Typography variant="body1" color="text.secondary" mb={2}>
            {t('list.empty')}
          </Typography>
          <Button variant="contained" onClick={() => setOpen(true)}>
            {t('create.button')}
          </Button>
        </Box>
      ) : (
        <Box>
          {data?.map(lead => (
            <Box key={lead.id} p={2} border={1} borderColor="divider" borderRadius={1} mb={2}>
              {/* Header mit Name, Score und Actions */}
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Box flex={1}>
                  <Typography variant="h6">{lead.companyName || lead.name}</Typography>
                  {lead.email && (
                    <Typography variant="body2" color="text.secondary">
                      {lead.email}
                    </Typography>
                  )}
                </Box>

                {/* Lead Score Indicator */}
                <Box mr={2}>
                  <LeadScoreIndicator score={lead.leadScore} size="medium" />
                </Box>

                {/* Stop-the-Clock Button (Admin/Manager only) */}
                {canStopClock && (
                  <IconButton
                    onClick={() => handleStopClock(lead)}
                    color={lead.clockStoppedAt ? 'success' : 'warning'}
                    title={lead.clockStoppedAt ? 'Schutzfrist fortsetzen' : 'Schutzfrist pausieren'}
                  >
                    {lead.clockStoppedAt ? <PlayArrow /> : <Pause />}
                  </IconButton>
                )}

                {/* Timeline Toggle */}
                <IconButton
                  onClick={() => setShowTimeline(showTimeline === lead.id ? null : lead.id)}
                  color={showTimeline === lead.id ? 'primary' : 'default'}
                  title="Aktivitäten anzeigen"
                >
                  <Timeline />
                </IconButton>
              </Box>

              {/* Status Workflow */}
              <Box mb={2}>
                <LeadStatusWorkflow currentStatus={lead.status || 'REGISTERED'} orientation="horizontal" size="small" />
              </Box>

              {/* Clock Stopped Info */}
              {lead.clockStoppedAt && (
                <Box mb={2}>
                  <Chip
                    label={`⏸️ Pausiert: ${lead.stopReason || 'Kein Grund angegeben'}`}
                    color="warning"
                    size="small"
                  />
                </Box>
              )}

              {/* Activity Timeline (expandable) */}
              {showTimeline === lead.id && (
                <Box mt={2} p={2} bgcolor="background.paper" borderRadius={1}>
                  <LeadActivityTimeline leadId={lead.id} />
                </Box>
              )}

              {/* Metadata */}
              {lead.createdAt && (
                <Typography variant="caption" color="text.secondary" display="block" mt={1}>
                  Erstellt: {new Date(lead.createdAt).toLocaleDateString('de-DE')}
                </Typography>
              )}
            </Box>
          ))}
        </Box>
      )}

      <LeadCreateDialog open={open} onClose={() => setOpen(false)} onCreated={handleLeadCreated} />

      <StopTheClockDialog
        open={stopClockDialogOpen}
        lead={selectedLead}
        onClose={() => setStopClockDialogOpen(false)}
        onSuccess={handleStopClockSuccess}
      />
    </Box>
  );
}
