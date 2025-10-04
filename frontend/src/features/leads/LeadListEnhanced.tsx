import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { listLeads } from './api';
import type { Lead, Problem } from './types';
import {
  CircularProgress,
  Alert,
  Button,
  Box,
  Typography,
  Card,
  CardContent,
  CardActions,
  IconButton,
  Tooltip,
  Stack,
} from '@mui/material';
import { Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import LeadWizard from './LeadWizard';
import LeadStageBadge from './LeadStageBadge';
import LeadProtectionBadge, { calculateProtectionInfo } from './LeadProtectionBadge';
import LeadSourceIcon from './LeadSourceIcon';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';

export default function LeadListEnhanced() {
  const { t } = useTranslation('leads');
  const [data, setData] = useState<Lead[] | null>(null);
  const [error, setError] = useState<Problem | null>(null);
  const [open, setOpen] = useState(false);

  const fetchLeads = async () => {
    try {
      const leads = await listLeads();
      console.log('📊 Leads from API:', leads); // DEBUG
      console.log('📊 First lead:', leads[0]); // DEBUG
      setData(leads);
      setError(null);
    } catch (err) {
      setError(err as Problem);
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
    fetchLeads();
  };

  const handleEdit = (leadId: string) => {
    // TODO: Open LeadWizard in edit mode
    console.log('Edit lead:', leadId);
  };

  const handleDelete = (leadId: string) => {
    // TODO: Delete confirmation dialog
    console.log('Delete lead:', leadId);
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
        <Stack spacing={2}>
          {data?.map(lead => (
            <Card key={lead.id} variant="outlined">
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                  <Box display="flex" alignItems="center" gap={1} flex={1}>
                    <LeadSourceIcon source={lead.source} />
                    <Box>
                      <Typography
                        variant="h6"
                        component="button"
                        onClick={() => handleEdit(lead.id)}
                        sx={{
                          fontWeight: 'bold',
                          cursor: 'pointer',
                          background: 'none',
                          border: 'none',
                          padding: 0,
                          color: 'primary.main',
                          textAlign: 'left',
                          '&:hover': {
                            textDecoration: 'underline',
                          },
                        }}
                      >
                        {lead.companyName}
                      </Typography>
                      {(lead.contactPerson || lead.email) && (
                        <Typography variant="body2" color="text.secondary" mt={0.5}>
                          {lead.contactPerson && `👤 ${lead.contactPerson}`}
                          {lead.contactPerson && lead.email && ' • '}
                          {lead.email && `📧 ${lead.email}`}
                        </Typography>
                      )}
                    </Box>
                  </Box>
                  <Stack direction="row" spacing={1}>
                    <LeadStageBadge stage={lead.stage} />
                    <LeadProtectionBadge protectionInfo={calculateProtectionInfo(lead)} />
                  </Stack>
                </Box>

                <Stack direction="row" spacing={2} flexWrap="wrap" alignItems="center">
                  {lead.city && (
                    <Typography variant="body2" color="text.secondary">
                      📍 {lead.city}
                    </Typography>
                  )}

                  {lead.businessType && (
                    <Typography variant="body2" color="text.secondary">
                      🏢 {lead.businessType}
                    </Typography>
                  )}

                  {lead.estimatedVolume && (
                    <Typography variant="body2" color="text.secondary">
                      💰 {new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(lead.estimatedVolume)}
                    </Typography>
                  )}

                  {lead.lastActivityAt && (
                    <Typography variant="body2" color="text.secondary">
                      ⏰ {formatDistanceToNow(new Date(lead.lastActivityAt), {
                        addSuffix: true,
                        locale: de,
                      })}
                    </Typography>
                  )}

                  {lead.createdAt && !lead.lastActivityAt && (
                    <Typography variant="body2" color="text.secondary">
                      📅 Erstellt {formatDistanceToNow(new Date(lead.createdAt), {
                        addSuffix: true,
                        locale: de,
                      })}
                    </Typography>
                  )}
                </Stack>
              </CardContent>

              <CardActions sx={{ justifyContent: 'flex-end', pt: 0 }}>
                <Tooltip title="Bearbeiten">
                  <IconButton
                    size="small"
                    onClick={() => handleEdit(lead.id)}
                    color="primary"
                  >
                    <EditIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Löschen">
                  <IconButton
                    size="small"
                    onClick={() => handleDelete(lead.id)}
                    color="error"
                  >
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </CardActions>
            </Card>
          ))}
        </Stack>
      )}

      <LeadWizard open={open} onClose={() => setOpen(false)} onCreated={handleLeadCreated} />
    </Box>
  );
}
