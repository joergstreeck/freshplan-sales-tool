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
  Breadcrumbs,
  Link,
  Chip,
  Button,
  Grid
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
  PersonAdd as PersonAddIcon
} from '@mui/icons-material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { useAuth } from '../contexts/AuthContext';
import { useCustomerDetails } from '../features/customer/hooks/useCustomerDetails';
import { EntityAuditTimeline } from '../features/audit/components/EntityAuditTimeline';
import { ContactGridContainer } from '../features/customers/components/contacts/ContactGridContainer';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';
import { useQuery } from '@tanstack/react-query';
import { customerApi } from '../features/customer/api/customerApi';
import type { CustomerContact } from '../features/customer/types';
import type { ContactAction } from '../features/customers/components/contacts/ContactGridContainer';

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

export function CustomerDetailPage() {
  const { customerId } = useParams<{ customerId: string }>();
  const [searchParams] = useSearchParams();
  const [activeTab, setActiveTab] = useState(0);
  const { user } = useAuth();
  const navigate = useNavigate();
  
  // Get highlightContact parameter for deep-linking
  const highlightContactId = searchParams.get('highlightContact');

  // Fetch customer data
  const { data: customer, isLoading, error } = useCustomerDetails(customerId);

  // Check if user can view audit trail
  const canViewAudit = user?.roles?.some(role =>
    ['admin', 'manager', 'auditor'].includes(role)
  );

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
                Erstellt {formatDistanceToNow(new Date(customer.createdAt), {
                  addSuffix: true,
                  locale: de,
                })}
                {customer.updatedAt && customer.updatedAt !== customer.createdAt && (
                  <> • Aktualisiert {formatDistanceToNow(new Date(customer.updatedAt), {
                    addSuffix: true,
                    locale: de,
                  })}</>
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
              <Button
                variant="contained"
                startIcon={<EditIcon />}
                onClick={() => navigate(`/customers/${customerId}/edit`)}
              >
                Bearbeiten
              </Button>
            </Box>
          </Box>
        </Paper>

        {/* Tabs */}
        <Paper>
          <Tabs
            value={activeTab}
            onChange={handleTabChange}
            variant="scrollable"
            scrollButtons="auto"
            sx={{ borderBottom: 1, borderColor: 'divider' }}
          >
            <Tab
              label="Übersicht"
              icon={<InfoIcon />}
              iconPosition="start"
              {...a11yProps(0)}
            />
            <Tab
              label="Kontakte"
              icon={<ContactsIcon />}
              iconPosition="start"
              {...a11yProps(1)}
            />
            <Tab
              label="Aktivitäten"
              icon={<AssessmentIcon />}
              iconPosition="start"
              {...a11yProps(2)}
            />
            {canViewAudit && (
              <Tab
                label="Änderungshistorie"
                icon={<TimelineIcon />}
                iconPosition="start"
                {...a11yProps(3)}
              />
            )}
          </Tabs>

          {/* Tab Panels */}
          <TabPanel value={activeTab} index={0}>
            <CustomerOverview customer={customer} />
          </TabPanel>

          <TabPanel value={activeTab} index={1}>
            <CustomerContacts 
              customerId={customerId!} 
              highlightContactId={highlightContactId}
            />
          </TabPanel>

          <TabPanel value={activeTab} index={2}>
            <CustomerActivities customerId={customerId!} />
          </TabPanel>

          {canViewAudit && (
            <TabPanel value={activeTab} index={3}>
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
      </Box>
    </MainLayoutV2>
  );
}

// Customer Overview Component
function CustomerOverview({ customer }: { customer: any }) {
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
  highlightContactId 
}: { 
  customerId: string;
  highlightContactId?: string | null;
}) {
  // Fetch contacts for this customer
  const { data: contacts, isLoading, error } = useQuery({
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

  // Handle contact actions
  const handleContactAction = (action: ContactAction) => {
    console.log('Contact action:', action);
    
    switch (action.type) {
      case 'add':
        // Navigate to add contact form
        console.log('Add new contact');
        break;
      case 'edit':
        // Navigate to edit contact form
        console.log('Edit contact:', action.contactId);
        break;
      case 'delete':
        // Show delete confirmation dialog
        console.log('Delete contact:', action.contactId);
        break;
      case 'setPrimary':
        // Update primary contact
        console.log('Set primary contact:', action.contactId);
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
        console.log('Unknown action:', action);
    }
  };

  // Create warmth data map from actual contacts
  // TODO: Replace with real warmth data from backend API
  const warmthData = new Map(
    contacts?.map((contact: CustomerContact) => {
      // Calculate warmth based on lastContactDate if available
      const daysSinceContact = contact.lastContactDate 
        ? Math.floor((Date.now() - new Date(contact.lastContactDate).getTime()) / (1000 * 60 * 60 * 24))
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
      
      return [contact.id, {
        temperature,
        score,
        lastInteraction: contact.lastContactDate ? new Date(contact.lastContactDate) : new Date(),
        interactionCount: 1, // Default to 1 until backend provides real data
      }];
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
      />
    </Box>
  );
}

// Placeholder for Customer Activities
function CustomerActivities({ customerId }: { customerId: string }) {
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