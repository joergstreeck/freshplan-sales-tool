/**
 * Customer Detail Page
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * Volle Seite mit MainLayoutV2 (Header + Sidebar + Theme V2)
 * Navigation: /customers → /customers/:customerId
 *
 * Tabs:
 * - "Firma" (3 Cards: company_profile, locations, classification)
 * - "Geschäft" (4 Cards: business_data, contracts, pain_points, products)
 * - "Verlauf" (disabled - Phase 4)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useState } from 'react';
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
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Business as BusinessIcon,
  Store as StoreIcon,
  TrendingUp as TrendingUpIcon,
  Timeline as TimelineIcon,
  Edit as EditIcon,
} from '@mui/icons-material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { useCustomerDetails } from '../features/customer/hooks/useCustomerDetails';
import { CustomerDetailTabFirma } from '../features/customers/components/detail/CustomerDetailTabFirma';
import { CustomerDetailTabGeschaeft } from '../features/customers/components/detail/CustomerDetailTabGeschaeft';
import { CustomerDetailTabVerlauf } from '../features/customers/components/detail/CustomerDetailTabVerlauf';
import { CustomerOnboardingWizardModal } from '../features/customers/components/wizard/CustomerOnboardingWizardModal';
import { CustomerActionButtons } from '../features/customers/components/detail/CustomerActionButtons';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';

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
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  // Read tab from URL parameter (default: 0)
  const initialTab = parseInt(searchParams.get('tab') || '0', 10);
  const [activeTab, setActiveTab] = useState(initialTab);

  // Edit Dialog State
  const [showEditWizard, setShowEditWizard] = useState(false);

  // Fetch customer data
  const { data: customer, isLoading, error } = useCustomerDetails(customerId);

  // Handle tab change
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  // Loading state
  if (isLoading) {
    return (
      <MainLayoutV2 maxWidth="xl">
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
      <MainLayoutV2 maxWidth="xl">
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
    <MainLayoutV2 maxWidth="xl">
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
                {/* Status Chip */}
                <Chip
                  label={customer.status || 'AKTIV'}
                  color={customer.status === 'AKTIV' ? 'success' : 'default'}
                  size="small"
                />
                {/* Revenue Chip */}
                {customer.expectedAnnualVolume && (
                  <Chip
                    icon={<TrendingUpIcon />}
                    label={`Jahresumsatz: ${new Intl.NumberFormat('de-DE', {
                      style: 'currency',
                      currency: 'EUR',
                      minimumFractionDigits: 0,
                      maximumFractionDigits: 0,
                    }).format(customer.expectedAnnualVolume)}`}
                    size="small"
                    variant="outlined"
                    color="primary"
                  />
                )}
                {/* Industry Chip */}
                {customer.industry && (
                  <Chip
                    icon={<BusinessIcon />}
                    label={customer.industry}
                    size="small"
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

        {/* Action Buttons (Cockpit-Pattern - Sprint 2.1.7.2 D11) */}
        <CustomerActionButtons customer={customer} onEdit={() => setShowEditWizard(true)} />

        {/* Tabs */}
        <Paper>
          <Tabs
            value={activeTab}
            onChange={handleTabChange}
            variant="scrollable"
            scrollButtons="auto"
            sx={{ borderBottom: 1, borderColor: 'divider' }}
          >
            <Tab label="Firma" icon={<StoreIcon />} iconPosition="start" {...a11yProps(0)} />
            <Tab
              label="Geschäft"
              icon={<TrendingUpIcon />}
              iconPosition="start"
              {...a11yProps(1)}
            />
            <Tab label="Verlauf" icon={<TimelineIcon />} iconPosition="start" {...a11yProps(2)} />
          </Tabs>

          {/* Tab Panels */}
          <TabPanel value={activeTab} index={0}>
            <CustomerDetailTabFirma customerId={customerId!} />
          </TabPanel>

          <TabPanel value={activeTab} index={1}>
            <CustomerDetailTabGeschaeft customerId={customerId!} />
          </TabPanel>

          <TabPanel value={activeTab} index={2}>
            <CustomerDetailTabVerlauf customerId={customerId!} />
          </TabPanel>
        </Paper>

        {/* Edit Customer Wizard Modal */}
        <CustomerOnboardingWizardModal
          open={showEditWizard}
          onClose={() => setShowEditWizard(false)}
          onComplete={() => {
            setShowEditWizard(false);
            // Refresh customer data
            window.location.reload();
          }}
          customerId={customerId}
          initialData={customer}
          editMode={true}
        />
      </Box>
    </MainLayoutV2>
  );
}
