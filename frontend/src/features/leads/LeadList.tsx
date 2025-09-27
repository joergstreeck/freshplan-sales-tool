import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { listLeads } from './api';
import type { Lead, Problem } from './types';
import { CircularProgress, Alert, Button, Box, Typography } from '@mui/material';
import LeadCreateDialog from './LeadCreateDialog';

export default function LeadList() {
  const { t } = useTranslation('leads');
  const [data, setData] = useState<Lead[] | null>(null);
  const [error, setError] = useState<Problem | null>(null);
  const [open, setOpen] = useState(false);

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
            <Box key={lead.id} p={2} border={1} borderColor="divider" borderRadius={1} mb={1}>
              <Typography variant="h6">{lead.name}</Typography>
              {lead.email && (
                <Typography variant="body2" color="text.secondary">
                  {lead.email}
                </Typography>
              )}
              {lead.createdAt && (
                <Typography variant="caption" color="text.secondary">
                  Erstellt: {new Date(lead.createdAt).toLocaleDateString()}
                </Typography>
              )}
            </Box>
          ))}
        </Box>
      )}

      <LeadCreateDialog open={open} onClose={() => setOpen(false)} onCreated={handleLeadCreated} />
    </Box>
  );
}
