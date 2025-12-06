import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardHeader,
  CardContent,
  Typography,
  Button,
  Chip,
  Stack,
  Divider,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  IconButton,
  useTheme,
  Alert,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import {
  ArrowBack as ArrowBackIcon,
  Add as AddIcon,
  ExpandMore as ExpandMoreIcon,
  TrendingUp as TrendingUpIcon,
  Group as GroupIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { LeadContactsCard } from '../features/leads/components/LeadContactsCard';
import { LeadEditDialog } from '../features/leads/components/LeadEditDialog';
import { ContactDialog } from '../features/leads/components/ContactDialog';
import { ActivityDialog } from '../features/leads/components/ActivityDialog';
import PreClaimBadge from '../features/leads/components/PreClaimBadge';
import BusinessPotentialCard from '../features/leads/components/BusinessPotentialCard';
import BusinessPotentialDialog from '../features/leads/components/BusinessPotentialDialog';
import CreateOpportunityDialog from '../features/opportunity/components/CreateOpportunityDialog';
import {
  LeadScoreSummaryCard,
  ScoreAccordion,
  RevenueScoreForm,
  FitScoreDisplay,
  PainScoreForm,
  EngagementScoreForm,
  LeadBasicDataAccordion,
  LeadActivityTimelineGrouped,
  LeadOpportunitiesList,
} from '../features/leads/components';
import { toast } from 'react-hot-toast';
import {
  getLeadById,
  deleteLeadContact,
  setLeadContactAsPrimary,
  updateLead,
} from '../features/leads/api';
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
  const theme = useTheme();
  const [lead, setLead] = useState<Lead | null>(null);
  const [loading, setLoading] = useState(true);

  // Dialog States
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [contactDialogOpen, setContactDialogOpen] = useState(false);
  const [editingContact, setEditingContact] = useState<LeadContactDTO | null>(null);

  // Sprint 2.1.7.1: Opportunity Count State
  const [opportunityCount, setOpportunityCount] = useState(0);
  const [activityDialogOpen, setActivityDialogOpen] = useState(false);
  const [businessPotentialDialogOpen, setBusinessPotentialDialogOpen] = useState(false);
  const [showOpportunityDialog, setShowOpportunityDialog] = useState(false);

  // Accordion State - Main Sections (default: basicData expanded)
  const [expandedSection, setExpandedSection] = useState<string | false>('basicData');

  // Lead Scoring Sub-Accordions State
  const [expandedScoringAccordion, setExpandedScoringAccordion] = useState<string | false>(false);

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

  // Main Section Accordion Handler
  const handleSectionChange =
    (section: string) => (event: React.SyntheticEvent, isExpanded: boolean) => {
      setExpandedSection(isExpanded ? section : false);
    };

  // Lead Scoring Sub-Accordion Handler
  const handleScoringAccordionChange =
    (accordion: string) => (event: React.SyntheticEvent, isExpanded: boolean) => {
      setExpandedScoringAccordion(isExpanded ? accordion : false);
    };

  const handleUpdate = async (updates: Partial<Lead>) => {
    if (!lead) return;

    try {
      // Backend now returns updated scores in response (Sprint 2.1.6+ auto-scoring)
      const updatedLead = await updateLead(lead.id, updates);
      setLead(updatedLead);
      toast.success('Lead erfolgreich aktualisiert');
    } catch (error: unknown) {
      console.error('Failed to update lead:', error);

      // ETag Conflict (412 Precondition Failed) or Concurrent Modification (409 Conflict)
      if (error?.status === 412 || error?.status === 409) {
        try {
          const refreshedLead = await getLeadById(lead.id);
          setLead(refreshedLead);
          toast.error(
            'Daten wurden zwischenzeitlich geändert. Die Seite wurde aktualisiert. Bitte versuchen Sie es erneut.',
            { duration: 5000 }
          );
        } catch (reloadError) {
          console.error('Failed to reload lead after conflict:', reloadError);
          toast.error('Fehler beim Neuladen der Daten');
        }
      } else {
        // Generic error
        toast.error('Fehler beim Aktualisieren des Leads');

        // Fallback: Reload lead to ensure fresh data
        try {
          const refreshedLead = await getLeadById(lead.id);
          setLead(refreshedLead);
        } catch (reloadError) {
          console.error('Failed to reload lead:', reloadError);
        }
      }
    }
  };

  if (loading) {
    return (
      <MainLayoutV2>
        <Box sx={{ py: 4 }}>
          <Typography>Lädt...</Typography>
        </Box>
      </MainLayoutV2>
    );
  }

  if (!lead) {
    return null;
  }

  const contacts: LeadContactDTO[] = lead.contacts || [];

  return (
    <MainLayoutV2>
      <Box sx={{ py: 4 }}>
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
              <Typography variant="h4" component="h1" gutterBottom>
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
                          ? theme.palette.primary.main
                          : lead.leadScore >= 40
                            ? theme.palette.warning.main
                            : theme.palette.error.main,
                      color: 'white',
                    }}
                  />
                )}
              </Stack>
            </Box>

            <Stack direction="row" spacing={2} alignItems="center">
              <PreClaimBadge lead={lead} />
              {/* Sprint 2.1.7.1: Lead → Opportunity Conversion Button */}
              {(lead.status === 'QUALIFIED' || lead.status === 'ACTIVE') &&
                lead.status !== 'CONVERTED' && (
                  <Button
                    variant="contained"
                    color="primary"
                    startIcon={<TrendingUpIcon />}
                    onClick={() => setShowOpportunityDialog(true)}
                    sx={{
                      bgcolor: theme.palette.primary.main,
                      '&:hover': { bgcolor: theme.palette.primary.dark },
                    }}
                  >
                    In Opportunity konvertieren
                  </Button>
                )}
            </Stack>
          </Box>
        </Box>

        {/* Sprint 2.1.7.1: Converted Badge */}
        {lead.status === 'CONVERTED' && (
          <Alert severity="success" icon={<CheckCircleIcon />} sx={{ mb: 3 }}>
            Lead wurde zu Opportunity konvertiert am{' '}
            {new Date(lead.updatedAt).toLocaleDateString('de-DE')}
            {' → Siehe Verkaufschancen unten'}
          </Alert>
        )}

        <Grid container spacing={3}>
          {/* Left Column: Main Content */}
          <Grid size={{ xs: 12, lg: 8 }}>
            {/* Lead Score Summary Card - ALWAYS VISIBLE */}
            <LeadScoreSummaryCard
              leadScore={lead.leadScore}
              painScore={lead.painScore}
              revenueScore={lead.revenueScore}
              fitScore={lead.fitScore}
              engagementScore={lead.engagementScore}
            />

            {/* Stammdaten Accordion */}
            <LeadBasicDataAccordion
              lead={lead}
              expanded={expandedSection === 'basicData'}
              onChange={handleSectionChange('basicData')}
              onEdit={() => setEditDialogOpen(true)}
            />

            {/* Vertriebsintelligenz Accordion */}
            <BusinessPotentialCard
              lead={lead}
              expanded={expandedSection === 'businessPotential'}
              onChange={handleSectionChange('businessPotential')}
              onEdit={() => setBusinessPotentialDialogOpen(true)}
            />

            {/* Lead Scoring Accordion */}
            <Accordion
              expanded={expandedSection === 'scoring'}
              onChange={handleSectionChange('scoring')}
              sx={{ mb: 2, border: '1px solid', borderColor: 'divider' }}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', gap: 1 }}>
                  <TrendingUpIcon color="primary" />
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="h6">Lead Scoring</Typography>
                    {expandedSection !== 'scoring' && (
                      <Box sx={{ display: 'flex', gap: 1.5, mt: 0.5 }}>
                        <Typography variant="body2" color="text.secondary">
                          💰 {lead.revenueScore || 0}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          🎯 {lead.fitScore || 0}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          ⚠️ {lead.painScore || 0}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          🤝 {lead.engagementScore || 0}
                        </Typography>
                      </Box>
                    )}
                  </Box>
                </Box>
              </AccordionSummary>

              <AccordionDetails>
                {/* Revenue Score Sub-Accordion */}
                <ScoreAccordion
                  title="Umsatzpotenzial"
                  icon="💰"
                  score={lead.revenueScore}
                  weight={25}
                  expanded={expandedScoringAccordion === 'revenue'}
                  onChange={handleScoringAccordionChange('revenue')}
                >
                  <RevenueScoreForm lead={lead} onUpdate={handleUpdate} />
                </ScoreAccordion>

                {/* Fit Score Sub-Accordion */}
                <ScoreAccordion
                  title="ICP Fit"
                  icon="🎯"
                  score={lead.fitScore}
                  weight={25}
                  expanded={expandedScoringAccordion === 'fit'}
                  onChange={handleScoringAccordionChange('fit')}
                >
                  <FitScoreDisplay lead={lead} />
                </ScoreAccordion>

                {/* Pain Score Sub-Accordion */}
                <ScoreAccordion
                  title="Pain Points"
                  icon="⚠️"
                  score={lead.painScore}
                  weight={25}
                  expanded={expandedScoringAccordion === 'pain'}
                  onChange={handleScoringAccordionChange('pain')}
                >
                  <PainScoreForm lead={lead} onUpdate={handleUpdate} />
                </ScoreAccordion>

                {/* Engagement Score Sub-Accordion */}
                <ScoreAccordion
                  title="Beziehungsebene"
                  icon="🤝"
                  score={lead.engagementScore}
                  weight={25}
                  expanded={expandedScoringAccordion === 'engagement'}
                  onChange={handleScoringAccordionChange('engagement')}
                >
                  <EngagementScoreForm lead={lead} onUpdate={handleUpdate} />
                </ScoreAccordion>
              </AccordionDetails>
            </Accordion>

            {/* Kontakte Accordion */}
            <Accordion
              expanded={expandedSection === 'contacts'}
              onChange={handleSectionChange('contacts')}
              sx={{ mb: 2, border: '1px solid', borderColor: 'divider' }}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', gap: 1 }}>
                  <GroupIcon color="primary" />
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="h6">Kontakte ({contacts.length})</Typography>
                    {expandedSection !== 'contacts' && (
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                        {contacts.length > 0
                          ? contacts
                              .slice(0, 2)
                              .map(c => `${c.firstName} ${c.lastName}`)
                              .join(' • ')
                          : 'Noch keine Kontakte erfasst'}
                      </Typography>
                    )}
                  </Box>
                  <IconButton
                    size="small"
                    component="div"
                    onClick={e => {
                      e.stopPropagation();
                      setEditingContact(null);
                      setContactDialogOpen(true);
                    }}
                    sx={{ cursor: 'pointer' }}
                  >
                    <AddIcon fontSize="small" />
                  </IconButton>
                </Box>
              </AccordionSummary>

              <AccordionDetails>
                <LeadContactsCard
                  contacts={contacts}
                  embedded={true}
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
              </AccordionDetails>
            </Accordion>

            {/* Sprint 2.1.7.1: Opportunities Section */}
            <Accordion
              expanded={expandedSection === 'opportunities'}
              onChange={handleSectionChange('opportunities')}
              sx={{ mb: 2, border: '1px solid', borderColor: 'divider' }}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', gap: 1 }}>
                  <TrendingUpIcon color="primary" />
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="h6">Verkaufschancen ({opportunityCount})</Typography>
                    {expandedSection !== 'opportunities' && opportunityCount === 0 && (
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                        Noch keine Opportunities erstellt
                      </Typography>
                    )}
                  </Box>
                </Box>
              </AccordionSummary>

              <AccordionDetails>
                <LeadOpportunitiesList leadId={lead.id} onCountChange={setOpportunityCount} />
              </AccordionDetails>
            </Accordion>
          </Grid>

          {/* Right Column: Activity Timeline */}
          <Grid size={{ xs: 12, lg: 4 }}>
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
                      bgcolor: theme.palette.primary.main,
                      '&:hover': { bgcolor: theme.palette.primary.dark },
                    }}
                  >
                    Neue Aktivität
                  </Button>
                }
              />
              <Divider />
              <CardContent>
                <LeadActivityTimelineGrouped leadId={lead.id} />
              </CardContent>
            </Card>
          </Grid>
        </Grid>

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

        {/* Sprint 2.1.7.1: Create Opportunity Dialog */}
        <CreateOpportunityDialog
          open={showOpportunityDialog}
          lead={lead}
          onClose={() => setShowOpportunityDialog(false)}
          onSuccess={() => {
            setShowOpportunityDialog(false);
            // Reload lead to update status
            loadLead();
            toast.success('Opportunity erfolgreich erstellt! 🎉');
          }}
        />
      </Box>
    </MainLayoutV2>
  );
}
