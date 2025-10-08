import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Card,
  CardHeader,
  CardContent,
  Typography,
  Button,
  Chip,
  Stack,
  Divider,
  Alert,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Edit as EditIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { LeadContactsCard } from '../features/leads/components/LeadContactsCard';
import { LeadEditDialog } from '../features/leads/components/LeadEditDialog';
import { ContactDialog } from '../features/leads/components/ContactDialog';
import { ActivityDialog } from '../features/leads/components/ActivityDialog';
import PreClaimBadge from '../features/leads/components/PreClaimBadge';
import BusinessPotentialCard from '../features/leads/components/BusinessPotentialCard';
import BusinessPotentialDialog from '../features/leads/components/BusinessPotentialDialog';
import LeadActivityTimeline from '../features/leads/LeadActivityTimeline';
import { toast } from 'react-hot-toast';
import { getLeadById, deleteLeadContact, setLeadContactAsPrimary } from '../features/leads/api';
import type { Lead, LeadContactDTO } from '../features/leads/types';
import { extractLeadIdFromSlug } from '../utils/slugify';

/**
 * Lead Detail Page - Sprint 2.1.6 Phase 5+
 *
 * Read-only Arbeitsoberfläche für Vertriebler:
 * - Stammdaten-Anzeige mit [Bearbeiten]-Dialog
 * - Multi-Contact Management mit Dialogs
 * - Activity Timeline mit [+ Neue Aktivität]
 * - Protection Status Badge
 *
 * Best Practice: Read-only View + Modal Dialogs für Bearbeitung
 */
export function LeadDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const [lead, setLead] = useState<Lead | null>(null);
  const [loading, setLoading] = useState(true);

  // Dialog States
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [contactDialogOpen, setContactDialogOpen] = useState(false);
  const [editingContact, setEditingContact] = useState<LeadContactDTO | null>(null);
  const [activityDialogOpen, setActivityDialogOpen] = useState(false);
  const [businessPotentialDialogOpen, setBusinessPotentialDialogOpen] = useState(false);

  // Extract Lead-ID from slug (e.g. "mueller-gmbh-123" → "123")
  const leadId = slug ? extractLeadIdFromSlug(slug) : null;

  useEffect(() => {
    if (!leadId) {
      navigate('/lead-generation/leads');
      return;
    }

    loadLead();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [leadId]);

  const loadLead = async () => {
    if (!leadId) return;

    setLoading(true);
    try {
      const data = await getLeadById(leadId);
      setLead(data);
    } catch (error) {
      console.error('Failed to load lead:', error);
      toast.error('Lead konnte nicht geladen werden');
      navigate('/lead-generation/leads');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <MainLayoutV2>
        <Container maxWidth="xl" sx={{ py: 4 }}>
          <Typography>Lädt...</Typography>
        </Container>
      </MainLayoutV2>
    );
  }

  if (!lead) {
    return null;
  }

  const contacts: LeadContactDTO[] = lead.contacts || [];

  return (
    <MainLayoutV2>
      <Container maxWidth="xl" sx={{ py: 4 }}>
        {/* Header */}
        <Box sx={{ mb: 3 }}>
          <Button
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate('/lead-generation/leads')}
            sx={{ mb: 2 }}
          >
            Zurück zur Liste
          </Button>

          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Box>
              <Typography variant="h4" gutterBottom>
                {lead.companyName}
              </Typography>
              <Stack direction="row" spacing={1} flexWrap="wrap">
                <Chip label={lead.status} color="primary" size="small" />
                {lead.stage !== undefined && (
                  <Chip
                    label={
                      lead.stage === 0
                        ? 'Vormerkung'
                        : lead.stage === 1
                          ? 'Registrierung'
                          : 'Qualifiziert'
                    }
                    size="small"
                    variant="outlined"
                  />
                )}
                {lead.leadScore !== undefined && (
                  <Chip
                    label={`Score: ${lead.leadScore}`}
                    size="small"
                    sx={{
                      bgcolor:
                        lead.leadScore >= 70
                          ? '#94C456'
                          : lead.leadScore >= 40
                            ? '#FF9800'
                            : '#F44336',
                      color: 'white',
                    }}
                  />
                )}
              </Stack>
            </Box>

            <Box>
              <PreClaimBadge lead={lead} />
            </Box>
          </Box>
        </Box>

        <Box sx={{ display: 'flex', gap: 3, flexDirection: { xs: 'column', md: 'row' } }}>
          {/* Left Column: Lead Info + Contacts */}
          <Box sx={{ flex: '1 1 66%', minWidth: 0 }}>
            {/* Stammdaten Card - READ ONLY */}
            <Card sx={{ mb: 3 }}>
              <CardHeader
                title="Stammdaten"
                action={
                  <Button
                    startIcon={<EditIcon />}
                    onClick={() => setEditDialogOpen(true)}
                    variant="outlined"
                    size="small"
                  >
                    Bearbeiten
                  </Button>
                }
              />
              <Divider />
              <CardContent>
                <Box
                  sx={{
                    display: 'grid',
                    gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
                    gap: 2,
                  }}
                >
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Firmenname
                    </Typography>
                    <Typography variant="body1">{lead.companyName}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Website
                    </Typography>
                    <Typography variant="body1">{lead.website || '—'}</Typography>
                  </Box>
                  <Box sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}>
                    <Typography variant="caption" color="text.secondary">
                      Straße
                    </Typography>
                    <Typography variant="body1">{lead.street || '—'}</Typography>
                  </Box>
                  <Box sx={{ gridColumn: { xs: '1', sm: 'span 1' } }}>
                    <Typography variant="caption" color="text.secondary">
                      PLZ
                    </Typography>
                    <Typography variant="body1">{lead.postalCode || '—'}</Typography>
                  </Box>
                  <Box sx={{ gridColumn: { xs: '1', sm: 'span 1' } }}>
                    <Typography variant="caption" color="text.secondary">
                      Stadt
                    </Typography>
                    <Typography variant="body1">{lead.city || '—'}</Typography>
                  </Box>
                </Box>

                {/* Legacy Contact Info (Backward Compatibility) */}
                {(lead.contactPerson || lead.email || lead.phone) && (
                  <Box sx={{ mt: 3 }}>
                    <Alert severity="info" sx={{ mb: 2 }}>
                      <strong>Hinweis:</strong> Dieser Lead verwendet das alte Kontakt-Format.
                      Bitte migrieren Sie zu strukturierten Kontakten.
                    </Alert>
                    <Typography variant="subtitle2" gutterBottom>
                      Legacy Kontakt
                    </Typography>
                    {lead.contactPerson && (
                      <Typography variant="body2" color="text.secondary">
                        Name: {lead.contactPerson}
                      </Typography>
                    )}
                    {lead.email && (
                      <Typography variant="body2" color="text.secondary">
                        E-Mail: {lead.email}
                      </Typography>
                    )}
                    {lead.phone && (
                      <Typography variant="body2" color="text.secondary">
                        Telefon: {lead.phone}
                      </Typography>
                    )}
                  </Box>
                )}
              </CardContent>
            </Card>

            {/* Business Potential Card */}
            <BusinessPotentialCard
              lead={lead}
              onEdit={() => setBusinessPotentialDialogOpen(true)}
            />

            {/* Contacts Card */}
            <LeadContactsCard
              contacts={contacts}
              onAdd={() => {
                setEditingContact(null);
                setContactDialogOpen(true);
              }}
              onEdit={contact => {
                setEditingContact(contact);
                setContactDialogOpen(true);
              }}
              onDelete={async contactId => {
                if (!window.confirm('Möchten Sie diesen Kontakt wirklich löschen?')) {
                  return;
                }
                try {
                  await deleteLeadContact(lead.id, contactId);
                  toast.success('Kontakt erfolgreich gelöscht');
                  loadLead();
                } catch (error) {
                  console.error('Failed to delete contact:', error);
                  toast.error('Fehler beim Löschen des Kontakts');
                }
              }}
              onSetPrimary={async contactId => {
                try {
                  await setLeadContactAsPrimary(lead.id, contactId);
                  toast.success('Hauptkontakt erfolgreich gesetzt');
                  loadLead();
                } catch (error) {
                  console.error('Failed to set primary contact:', error);
                  toast.error('Fehler beim Setzen des Hauptkontakts');
                }
              }}
            />
          </Box>

          {/* Right Column: Activity Timeline */}
          <Box sx={{ flex: '1 1 33%', minWidth: 0 }}>
            <Card>
              <CardHeader
                title="Aktivitäten"
                action={
                  <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => setActivityDialogOpen(true)}
                    size="small"
                    sx={{
                      bgcolor: '#94C456',
                      '&:hover': { bgcolor: '#7FB03E' },
                    }}
                  >
                    Neue Aktivität
                  </Button>
                }
              />
              <Divider />
              <CardContent sx={{ p: 0 }}>
                <LeadActivityTimeline leadId={lead.id} />
              </CardContent>
            </Card>
          </Box>
        </Box>

        {/* Dialogs */}
        <LeadEditDialog
          open={editDialogOpen}
          onClose={() => setEditDialogOpen(false)}
          lead={lead}
          onSave={loadLead}
        />
        <ContactDialog
          open={contactDialogOpen}
          onClose={() => {
            setContactDialogOpen(false);
            setEditingContact(null);
          }}
          leadId={lead.id}
          contact={editingContact}
          onSave={loadLead}
        />
        <ActivityDialog
          open={activityDialogOpen}
          onClose={() => setActivityDialogOpen(false)}
          leadId={lead.id}
          onSave={loadLead}
        />
        <BusinessPotentialDialog
          open={businessPotentialDialogOpen}
          onClose={() => setBusinessPotentialDialogOpen(false)}
          lead={lead}
          onSave={loadLead}
        />
      </Container>
    </MainLayoutV2>
  );
}
