import { useState, useEffect } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import {
  Box,
  Paper,
  Tabs,
  Tab,
  Typography,
  Skeleton,
  Alert,
  AlertTitle,
  Breadcrumbs,
  Link,
  Chip,
  Button,
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Stack,
  CircularProgress,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Info as InfoIcon,
  Contacts as ContactsIcon,
  Timeline as TimelineIcon,
  Assessment as AssessmentIcon,
  Edit as EditIcon,
  Business as BusinessIcon,
  LocationOn as LocationIcon,
  Category as CategoryIcon,
  PersonAdd as PersonAddIcon,
  TrendingUp as TrendingUpIcon,
  HourglassEmpty as HourglassEmptyIcon,
  CheckCircle as CheckCircleIcon,
  NaturePeople as NaturePeopleIcon,
} from '@mui/icons-material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { useAuth } from '../contexts/AuthContext';
import { useCustomerDetails } from '../features/customer/hooks/useCustomerDetails';
import { EntityAuditTimeline } from '../features/audit/components/EntityAuditTimeline';
import { ContactGridContainer } from '../features/customers/components/contacts/ContactGridContainer';
import { ContactFormDialog } from '../features/customers/components/contacts/ContactFormDialog';
import { CustomerFieldThemeProvider } from '../features/customers/theme/CustomerFieldThemeProvider';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { customerApi, type RevenueMetrics } from '../features/customer/api/customerApi';
import type { Customer, CustomerContact } from '../features/customer/types/customer.types';
import type { ContactAction } from '../features/customers/components/contacts/ContactGridContainer';
import CreateOpportunityForCustomerDialog from '../features/opportunity/components/CreateOpportunityForCustomerDialog';
import { CustomerOpportunitiesList } from '../features/customers/components/CustomerOpportunitiesList';
import { CustomerOnboardingWizardModal } from '../features/customers/components/wizard/CustomerOnboardingWizardModal';
import { RevenueMetricsWidget } from '../features/customers/components/RevenueMetricsWidget';
import { PaymentBehaviorIndicator } from '../features/customers/components/PaymentBehaviorIndicator';
import { ChurnRiskAlert } from '../features/customers/components/ChurnRiskAlert';
import { httpClient } from '../lib/apiClient';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`customer-tabpanel-${index}`}
      aria-labelledby={`customer-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

function a11yProps(index: number) {
  return {
    id: `customer-tab-${index}`,
    'aria-controls': `customer-tabpanel-${index}`,
  };
}

// Helper function for seasonal pattern display (Sprint 2.1.7.4)
const MONTH_NAMES = [
  'Jan',
  'Feb',
  'Mär',
  'Apr',
  'Mai',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Okt',
  'Nov',
  'Dez',
];

function getSeasonalPatternLabel(
  pattern: string | null | undefined,
  months: number[] | null | undefined
): string {
  if (!months || months.length === 0) return 'Nicht konfiguriert';

  const monthNames = months.map(m => MONTH_NAMES[m - 1]).join(', ');
  return monthNames;
}

export function CustomerDetailPage() {
  const { customerId } = useParams<{ customerId: string }>();
  const [searchParams] = useSearchParams();
  const [activeTab, setActiveTab] = useState(0);
  const { user } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // Opportunity Dialog State
  const [showOpportunityDialog, setShowOpportunityDialog] = useState(false);
  const [opportunityCount, setOpportunityCount] = useState(0);

  // Edit Dialog State
  const [showEditWizard, setShowEditWizard] = useState(false);

  // Activation Dialog State (Sprint 2.1.7.4)
  const [showActivateDialog, setShowActivateDialog] = useState(false);
  const [orderNumber, setOrderNumber] = useState('');
  const [activationSuccess, setActivationSuccess] = useState(false);
  const [activationError, setActivationError] = useState<string | null>(null);

  // Get highlightContact parameter for deep-linking
  const highlightContactId = searchParams.get('highlightContact');

  // Fetch customer data
  const { data: customer, isLoading, error } = useCustomerDetails(customerId);

  // Fetch revenue metrics from Xentral (Sprint 2.1.7.2)
  const {
    data: revenueMetrics,
    isLoading: loadingMetrics,
    error: metricsError,
  } = useQuery({
    queryKey: ['customer-revenue-metrics', customerId],
    queryFn: () => customerApi.getRevenueMetrics(customerId!),
    enabled: !!customer?.xentralCustomerId, // Only fetch if customer has Xentral-ID
    retry: 1,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Check if user can view audit trail
  const canViewAudit = user?.roles?.some(role => ['admin', 'manager', 'auditor'].includes(role));

  // Auto-switch to contacts tab if highlightContact is present
  useEffect(() => {
    if (highlightContactId) {
      setActiveTab(1); // Assuming contacts tab is index 1
    }
  }, [highlightContactId]);

  // Scroll to top on mount
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  // Handle tab change
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  // Handle customer activation (Sprint 2.1.7.4)
  const handleActivateCustomer = async () => {
    if (!customerId) return;

    try {
      setActivationError(null);
      await customerApi.activateCustomer(customerId, orderNumber || undefined);

      // Success! Refresh customer data
      queryClient.invalidateQueries({ queryKey: ['customer', customerId] });

      // Show success message
      setActivationSuccess(true);
      setShowActivateDialog(false);
      setOrderNumber('');

      // Hide success message after 5 seconds
      setTimeout(() => setActivationSuccess(false), 5000);
    } catch (err) {
      const error = err as { response?: { data?: { message?: string } } };
      setActivationError(error.response?.data?.message || 'Fehler beim Aktivieren des Kunden');
    }
  };

  // Loading state
  if (isLoading) {
    return (
      <MainLayoutV2>
        <Box sx={{ p: 3 }}>
          <Skeleton variant="text" width={200} height={32} sx={{ mb: 2 }} />
          <Paper sx={{ p: 3, mb: 3 }}>
            <Skeleton variant="text" width="40%" height={40} />
            <Skeleton variant="text" width="30%" height={24} sx={{ mt: 1 }} />
          </Paper>
          <Paper>
            <Skeleton variant="rectangular" height={48} />
            <Skeleton variant="rectangular" height={400} sx={{ mt: 2 }} />
          </Paper>
        </Box>
      </MainLayoutV2>
    );
  }

  // Error state
  if (error || !customer) {
    return (
      <MainLayoutV2>
        <Box sx={{ p: 3 }}>
          <Alert severity="error">
            Kunde konnte nicht geladen werden.
            <Button size="small" onClick={() => navigate('/customers')} sx={{ ml: 2 }}>
              Zur Kundenliste
            </Button>
          </Alert>
        </Box>
      </MainLayoutV2>
    );
  }

  return (
    <MainLayoutV2>
      <Box sx={{ p: 3 }}>
        {/* Breadcrumbs */}
        <Breadcrumbs sx={{ mb: 2 }}>
          <Link
            component="button"
            underline="hover"
            color="inherit"
            onClick={() => navigate('/dashboard')}
          >
            Dashboard
          </Link>
          <Link
            component="button"
            underline="hover"
            color="inherit"
            onClick={() => navigate('/customers')}
          >
            Kunden
          </Link>
          <Typography color="text.primary">{customer.companyName}</Typography>
        </Breadcrumbs>

        {/* Customer Header */}
        <Paper sx={{ p: 3, mb: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <Box>
              <Typography variant="h4" gutterBottom>
                {customer.companyName}
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 2 }}>
                {customer.industry && (
                  <Chip
                    icon={<CategoryIcon />}
                    label={customer.industry}
                    size="small"
                    variant="outlined"
                  />
                )}
                {customer.city && (
                  <Chip
                    icon={<LocationIcon />}
                    label={`${customer.postalCode || ''} ${customer.city}`.trim()}
                    size="small"
                    variant="outlined"
                  />
                )}
                {customer.customerType && (
                  <Chip
                    icon={<BusinessIcon />}
                    label={customer.customerType}
                    size="small"
                    color={customer.customerType === 'premium' ? 'primary' : 'default'}
                    variant="outlined"
                  />
                )}
              </Box>
              <Typography variant="body2" color="text.secondary">
                Erstellt{' '}
                {formatDistanceToNow(new Date(customer.createdAt), {
                  addSuffix: true,
                  locale: de,
                })}
                {customer.updatedAt && customer.updatedAt !== customer.createdAt && (
                  <>
                    {' '}
                    • Aktualisiert{' '}
                    {formatDistanceToNow(new Date(customer.updatedAt), {
                      addSuffix: true,
                      locale: de,
                    })}
                  </>
                )}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button
                variant="outlined"
                startIcon={<ArrowBackIcon />}
                onClick={() => navigate('/customers')}
              >
                Zurück
              </Button>
              {customer.status === 'AKTIV' && (
                <Button
                  variant="outlined"
                  color="primary"
                  startIcon={<TrendingUpIcon />}
                  onClick={() => setShowOpportunityDialog(true)}
                >
                  Neue Opportunity erstellen
                </Button>
              )}
              <Button
                variant="contained"
                startIcon={<EditIcon />}
                onClick={() => setShowEditWizard(true)}
              >
                Bearbeiten
              </Button>
            </Box>
          </Box>
        </Paper>

        {/* Success Message (Sprint 2.1.7.4) */}
        {activationSuccess && (
          <Alert severity="success" sx={{ mb: 3 }} onClose={() => setActivationSuccess(false)}>
            <AlertTitle>Kunde erfolgreich aktiviert!</AlertTitle>
            Der Kundenstatus wurde von PROSPECT → AKTIV geändert.
          </Alert>
        )}

        {/* PROSPECT Status Alert (Sprint 2.1.7.4) */}
        {customer.status === 'PROSPECT' && (
          <Alert severity="info" sx={{ mb: 3 }}>
            <AlertTitle>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <HourglassEmptyIcon />
                Kunde wartet auf erste Bestellung
              </Box>
            </AlertTitle>

            <Typography variant="body2" sx={{ mb: 2 }}>
              <strong>{customer.companyName}</strong> wurde aus einer gewonnenen Opportunity
              konvertiert.
              <br />
              Sobald die erste Bestellung geliefert wurde, können Sie den Kunden als AKTIV
              markieren.
            </Typography>

            <Stack direction="row" spacing={2} alignItems="center">
              <Button
                variant="contained"
                color="success"
                startIcon={<CheckCircleIcon />}
                onClick={() => setShowActivateDialog(true)}
              >
                Erste Bestellung geliefert → AKTIV markieren
              </Button>

              <Typography variant="caption" color="text.secondary">
                (Wird automatisch via Xentral-Integration erfolgen - Sprint 2.1.7.2)
              </Typography>
            </Stack>
          </Alert>
        )}

        {/* Seasonal Business Indicator (Sprint 2.1.7.4) */}
        {customer.isSeasonalBusiness && (
          <Alert severity="info" icon={<NaturePeopleIcon />} sx={{ mb: 2 }}>
            <AlertTitle>Saisonbetrieb</AlertTitle>
            <Typography variant="body2">
              Aktive Monate:{' '}
              {getSeasonalPatternLabel(customer.seasonalPattern, customer.seasonalMonths)}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Churn-Monitoring ist außerhalb der Saison pausiert
            </Typography>
          </Alert>
        )}

        {/* Revenue Metrics Section (Sprint 2.1.7.2) */}
        {customer.xentralCustomerId && (
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Umsatzdaten (Xentral)
            </Typography>

            {loadingMetrics ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                <CircularProgress />
              </Box>
            ) : metricsError ? (
              <Alert severity="warning">
                Umsatzdaten konnten nicht geladen werden. Bitte versuchen Sie es später erneut.
              </Alert>
            ) : revenueMetrics ? (
              <>
                {/* Revenue Cards */}
                <Grid container spacing={2} sx={{ mb: 2 }}>
                  <Grid size={{ xs: 12, md: 4 }}>
                    <RevenueMetricsWidget
                      title="Umsatz (30 Tage)"
                      value={revenueMetrics.revenue30Days}
                      color="success"
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 4 }}>
                    <RevenueMetricsWidget
                      title="Umsatz (90 Tage)"
                      value={revenueMetrics.revenue90Days}
                      color="info"
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 4 }}>
                    <RevenueMetricsWidget
                      title="Umsatz (365 Tage)"
                      value={revenueMetrics.revenue365Days}
                      color="primary"
                    />
                  </Grid>
                </Grid>

                {/* Payment Behavior Indicator */}
                <PaymentBehaviorIndicator
                  behavior={revenueMetrics.paymentBehavior}
                  averageDaysToPay={revenueMetrics.averageDaysToPay}
                />

                {/* Churn Risk Alert */}
                <ChurnRiskAlert
                  lastOrderDate={revenueMetrics.lastOrderDate}
                  churnThresholdDays={customer.churnThresholdDays}
                />
              </>
            ) : null}
          </Paper>
        )}

        {/* No Xentral Connection Warning */}
        {!customer.xentralCustomerId && customer.status === 'AKTIV' && (
          <Alert severity="warning" sx={{ mb: 3 }}>
            <AlertTitle>Keine Xentral-Verknüpfung vorhanden</AlertTitle>
            <Typography variant="body2">
              Für diesen Kunden können keine Umsatzdaten angezeigt werden, da keine Verknüpfung zu
              Xentral besteht.
            </Typography>
          </Alert>
        )}

        {/* Tabs */}
        <Paper>
          <Tabs
            value={activeTab}
            onChange={handleTabChange}
            variant="scrollable"
            scrollButtons="auto"
            sx={{ borderBottom: 1, borderColor: 'divider' }}
          >
            <Tab label="Übersicht" icon={<InfoIcon />} iconPosition="start" {...a11yProps(0)} />
            <Tab label="Kontakte" icon={<ContactsIcon />} iconPosition="start" {...a11yProps(1)} />
            <Tab
              label={`Verkaufschancen${opportunityCount > 0 ? ` (${opportunityCount})` : ''}`}
              icon={<TrendingUpIcon />}
              iconPosition="start"
              {...a11yProps(2)}
            />
            <Tab
              label="Aktivitäten"
              icon={<AssessmentIcon />}
              iconPosition="start"
              {...a11yProps(3)}
            />
            {canViewAudit && (
              <Tab
                label="Änderungshistorie"
                icon={<TimelineIcon />}
                iconPosition="start"
                {...a11yProps(4)}
              />
            )}
          </Tabs>

          {/* Tab Panels */}
          <TabPanel value={activeTab} index={0}>
            <CustomerOverview customer={customer} />
          </TabPanel>

          <TabPanel value={activeTab} index={1}>
            <CustomerContacts customerId={customerId!} highlightContactId={highlightContactId} />
          </TabPanel>

          <TabPanel value={activeTab} index={2}>
            <CustomerOpportunitiesList
              customerId={customerId!}
              onCountChange={setOpportunityCount}
            />
          </TabPanel>

          <TabPanel value={activeTab} index={3}>
            <CustomerActivities customerId={customerId!} />
          </TabPanel>

          {canViewAudit && (
            <TabPanel value={activeTab} index={4}>
              <EntityAuditTimeline
                entityType="customer"
                entityId={customerId!}
                showExportButton={user?.roles?.some(role => ['admin', 'auditor'].includes(role))}
                maxItems={50}
                showFilters={true}
              />
            </TabPanel>
          )}
        </Paper>

        {/* Create Opportunity Dialog */}
        <CreateOpportunityForCustomerDialog
          open={showOpportunityDialog}
          onClose={() => setShowOpportunityDialog(false)}
          customer={customer}
          onSuccess={() => {
            setShowOpportunityDialog(false);
            // Switch to Opportunities tab to show the new opportunity
            setActiveTab(2);
          }}
        />

        {/* Edit Customer Wizard Modal */}
        <CustomerOnboardingWizardModal
          open={showEditWizard}
          onClose={() => setShowEditWizard(false)}
          onComplete={() => {
            setShowEditWizard(false);
            // Refresh customer data - useCustomerDetails should auto-refetch
            window.location.reload(); // Simple refresh for now, can be optimized with query invalidation
          }}
          customerId={customerId}
          initialData={customer}
          editMode={true}
        />

        {/* Activation Dialog (Sprint 2.1.7.4) */}
        <Dialog
          open={showActivateDialog}
          onClose={() => {
            setShowActivateDialog(false);
            setActivationError(null);
            setOrderNumber('');
          }}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <CheckCircleIcon color="success" />
              Kunde als AKTIV markieren
            </Box>
          </DialogTitle>

          <DialogContent>
            <Typography sx={{ mb: 2 }}>
              Wurde die erste Bestellung für <strong>{customer.companyName}</strong> erfolgreich
              geliefert?
            </Typography>

            <TextField
              fullWidth
              label="Bestellnummer (optional)"
              value={orderNumber}
              onChange={e => setOrderNumber(e.target.value)}
              helperText="Für Audit-Trail - wird im System protokolliert"
              sx={{ mt: 2 }}
            />

            {activationError && (
              <Alert severity="error" sx={{ mt: 2 }}>
                {activationError}
              </Alert>
            )}

            <Alert severity="success" sx={{ mt: 2 }}>
              <AlertTitle>Was passiert:</AlertTitle>
              <ul style={{ margin: '8px 0', paddingLeft: '20px' }}>
                <li>
                  Status wird von <strong>PROSPECT</strong> → <strong>AKTIV</strong> geändert
                </li>
                <li>Kunde erscheint in Dashboard als "Aktiver Kunde"</li>
                <li>Aktion wird im Audit-Log protokolliert</li>
              </ul>
            </Alert>
          </DialogContent>

          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button
              onClick={() => {
                setShowActivateDialog(false);
                setActivationError(null);
                setOrderNumber('');
              }}
            >
              Abbrechen
            </Button>
            <Button
              variant="contained"
              color="success"
              onClick={handleActivateCustomer}
              startIcon={<CheckCircleIcon />}
            >
              Ja, als AKTIV markieren
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </MainLayoutV2>
  );
}

// Customer Overview Component
function CustomerOverview({ customer }: { customer: Customer }) {
  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, md: 6 }}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Unternehmensdaten
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <DetailRow label="Firma" value={customer.companyName} />
            <DetailRow label="Branche" value={customer.industry} />
            <DetailRow label="Kundentyp" value={customer.customerType} />
            <DetailRow label="Website" value={customer.website} />
            <DetailRow label="Mitarbeiter" value={customer.employeeCount} />
          </Box>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, md: 6 }}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Adresse
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <DetailRow label="Straße" value={customer.street} />
            <DetailRow label="PLZ" value={customer.postalCode} />
            <DetailRow label="Stadt" value={customer.city} />
            <DetailRow label="Land" value={customer.country || 'Deutschland'} />
          </Box>
        </Paper>
      </Grid>

      {customer.notes && (
        <Grid size={12}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Notizen
            </Typography>
            <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
              {customer.notes}
            </Typography>
          </Paper>
        </Grid>
      )}
    </Grid>
  );
}

// Helper component for detail rows
function DetailRow({ label, value }: { label: string; value?: string | number }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
      <Typography variant="body2" color="text.secondary">
        {label}:
      </Typography>
      <Typography variant="body2" fontWeight="medium">
        {value || '-'}
      </Typography>
    </Box>
  );
}

// Customer Contacts with Smart Contact Cards
function CustomerContacts({
  customerId,
  highlightContactId,
}: {
  customerId: string;
  highlightContactId?: string | null;
}) {
  const queryClient = useQueryClient();

  // Contact Dialog State (Sprint 2.1.7.2 D9.3)
  const [showContactDialog, setShowContactDialog] = useState(false);
  const [editingContact, setEditingContact] = useState<CustomerContact | null>(null);

  // Fetch contacts for this customer
  const {
    data: contacts,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['customer-contacts', customerId],
    queryFn: async () => {
      // Use real API to fetch contacts
      return customerApi.getCustomerContacts(customerId);
    },
    enabled: !!customerId,
  });

  // Auto-scroll to highlighted contact
  useEffect(() => {
    if (highlightContactId && contacts) {
      // Wait for DOM to be ready
      setTimeout(() => {
        const element = document.getElementById(`contact-${highlightContactId}`);
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'center' });

          // Add highlight animation
          element.classList.add('highlight-animation');

          // Remove animation class after animation completes
          setTimeout(() => {
            element.classList.remove('highlight-animation');
          }, 3000);
        }
      }, 100);
    }
  }, [highlightContactId, contacts]);

  // Handle contact submit (Sprint 2.1.7.2 D9.3)
  const handleContactSubmit = async (contactData: Partial<CustomerContact>) => {
    try {
      if (editingContact) {
        // Update existing contact
        await httpClient.put(
          `/api/customers/${customerId}/contacts/${editingContact.id}`,
          contactData
        );
      } else {
        // Create new contact
        await httpClient.post(`/api/customers/${customerId}/contacts`, contactData);
      }

      // Refresh contacts list
      queryClient.invalidateQueries({ queryKey: ['customer-contacts', customerId] });

      // Close dialog
      setShowContactDialog(false);
      setEditingContact(null);
    } catch (error) {
      console.error('Failed to save contact:', error);
      // TODO: Show error toast/notification
      alert('Fehler beim Speichern des Kontakts');
    }
  };

  // Handle contact actions
  const handleContactAction = (action: ContactAction) => {
    switch (action.type) {
      case 'add':
        // Open dialog for new contact (Sprint 2.1.7.2 D9.3)
        setEditingContact(null);
        setShowContactDialog(true);
        break;
      case 'edit':
        // Open dialog for editing existing contact (Sprint 2.1.7.2 D9.3)
        if (action.contact) {
          setEditingContact(action.contact);
          setShowContactDialog(true);
        }
        break;
      case 'delete':
        // Show delete confirmation dialog
        if (confirm('Möchten Sie diesen Kontakt wirklich löschen?')) {
          // TODO: Implement actual delete API call
          alert('Kontakt löschen - API-Aufruf wird noch implementiert');
        }
        break;
      case 'setPrimary':
        // Update primary contact
        // TODO: Implement API call to set primary contact
        alert('Als Hauptkontakt setzen - API-Aufruf wird noch implementiert');
        break;
      case 'quickAction':
        // Handle quick actions (email, phone, etc.)
        if (action.action === 'email' && action.contact?.email) {
          window.location.href = `mailto:${action.contact.email}`;
        } else if (action.action === 'phone' && action.contact?.phone) {
          window.location.href = `tel:${action.contact.phone}`;
        } else if (action.action === 'whatsapp' && action.contact?.mobile) {
          window.open(`https://wa.me/${action.contact.mobile.replace(/\D/g, '')}`, '_blank');
        }
        break;
      default:
    }
  };

  // Create warmth data map from actual contacts
  // TODO: Replace with real warmth data from backend API
  const warmthData = new Map(
    contacts?.map((contact: CustomerContact) => {
      // Calculate warmth based on lastContactDate if available
      const daysSinceContact = contact.lastContactDate
        ? Math.floor(
            (Date.now() - new Date(contact.lastContactDate).getTime()) / (1000 * 60 * 60 * 24)
          )
        : 999;

      let temperature: 'HOT' | 'WARM' | 'COOLING' | 'COLD';
      let score: number;

      // Use deterministic scores based on days since contact
      // These should eventually come from the backend
      if (daysSinceContact <= 7) {
        temperature = 'HOT';
        score = 90; // Fixed score for HOT contacts
      } else if (daysSinceContact <= 30) {
        temperature = 'WARM';
        score = 70; // Fixed score for WARM contacts
      } else if (daysSinceContact <= 60) {
        temperature = 'COOLING';
        score = 50; // Fixed score for COOLING contacts
      } else {
        temperature = 'COLD';
        score = 30; // Fixed score for COLD contacts
      }

      return [
        contact.id,
        {
          temperature,
          score,
          lastInteraction: contact.lastContactDate ? new Date(contact.lastContactDate) : new Date(),
          interactionCount: 1, // Default to 1 until backend provides real data
        },
      ];
    }) || []
  );

  if (isLoading) {
    return (
      <Box>
        <Skeleton variant="rectangular" height={200} sx={{ mb: 2 }} />
        <Skeleton variant="rectangular" height={200} />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error">
        Fehler beim Laden der Kontakte. Bitte versuchen Sie es später erneut.
      </Alert>
    );
  }

  if (!contacts || contacts.length === 0) {
    return (
      <Box sx={{ textAlign: 'center', py: 4 }}>
        <ContactsIcon sx={{ fontSize: 64, color: 'text.disabled', mb: 2 }} />
        <Typography variant="h6" color="text.secondary" gutterBottom>
          Keine Kontakte vorhanden
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Fügen Sie den ersten Kontakt für diesen Kunden hinzu.
        </Typography>
        <Button
          variant="contained"
          startIcon={<PersonAddIcon />}
          onClick={() => handleContactAction({ type: 'add' })}
        >
          Kontakt hinzufügen
        </Button>
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">
          {contacts.length} {contacts.length === 1 ? 'Kontakt' : 'Kontakte'}
        </Typography>
        <Button
          variant="contained"
          startIcon={<PersonAddIcon />}
          onClick={() => handleContactAction({ type: 'add' })}
        >
          Kontakt hinzufügen
        </Button>
      </Box>

      <ContactGridContainer
        contacts={contacts}
        warmthData={warmthData}
        onContactAction={handleContactAction}
        viewMode="grid"
        showFilters={true}
        useSmartCards={true}
        highlightContactId={highlightContactId}
        customerId={customerId}
      />

      {/* Contact Form Dialog (Sprint 2.1.7.2 D9.3) */}
      <CustomerFieldThemeProvider>
        <ContactFormDialog
          open={showContactDialog}
          onClose={() => {
            setShowContactDialog(false);
            setEditingContact(null);
          }}
          onSubmit={handleContactSubmit}
          contact={editingContact}
        />
      </CustomerFieldThemeProvider>
    </Box>
  );
}

// Placeholder for Customer Activities
function CustomerActivities({ _customerId }: { _customerId: string }) {
  return (
    <Box>
      <Alert severity="info" sx={{ mb: 2 }}>
        Das Aktivitäten-Tracking wird in Sprint 4 implementiert.
      </Alert>
      <Typography variant="body2" color="text.secondary">
        Hier werden zukünftig alle Aktivitäten und Interaktionen mit dem Kunden angezeigt.
      </Typography>
    </Box>
  );
}
