import { useState, useEffect } from 'react';
import { Box, Button, Typography, Tabs, Tab } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { CustomerOnboardingWizardModal } from '../features/customers/components/wizard/CustomerOnboardingWizardModal';
import { EmptyStateHero } from '../components/common/EmptyStateHero';
import { CustomerTable } from '../features/customers/components/CustomerTable';
import { CustomerListHeader } from '../features/customers/components/CustomerListHeader';
import { CustomerListSkeleton } from '../features/customers/components/CustomerListSkeleton';
import { DataHygieneDashboard } from '../features/customers/components/intelligence/DataHygieneDashboard';
import { DataFreshnessManager } from '../features/customers/components/intelligence/DataFreshnessManager';
import { useAuth } from '../contexts/AuthContext';
import { useCustomers } from '../features/customer/api/customerQueries';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { ActionToast } from '../components/notifications/ActionToast';
import { taskEngine } from '../services/taskEngine';
import { isFeatureEnabled } from '../config/featureFlags';
import type { Customer } from '../types/customer.types';

interface CustomersPageV2Props {
  openWizard?: boolean;
}

export function CustomersPageV2({ openWizard = false }: CustomersPageV2Props) {
  const [wizardOpen, setWizardOpen] = useState(openWizard);
  const [activeTab, setActiveTab] = useState(0);
  const { user } = useAuth();
  const navigate = useNavigate();

  // Listen for new customer event from sidebar
  useEffect(() => {
    const handleNewCustomer = () => setWizardOpen(true);
    window.addEventListener('freshplan:new-customer', handleNewCustomer);
    return () => window.removeEventListener('freshplan:new-customer', handleNewCustomer);
  }, []);

  // Verkäuferschutz: Nur eigene Kunden für Sales-Rolle
  const filter = user?.role === 'sales' ? { assignedTo: user.id } : undefined;

  // Use existing useCustomers hook with pagination
  const { data, isLoading, refetch } = useCustomers(0, 1000); // Get all for now
  const customers = data?.content || [];

  const handleCustomerCreated = async (customer: Customer) => {
    setWizardOpen(false);

    // Task Preview: Automatisch erste Aufgabe generieren
    let taskId: string | undefined;
    if (isFeatureEnabled('taskPreview')) {
      try {
        const tasks = await taskEngine.processEvent({
          type: 'customer-created',
          context: { customer, user },
        });
        taskId = tasks[0]?.id;
      } catch (error) {
        console.error('Task generation failed:', error);
        // Nicht blockierend - Customer wurde trotzdem angelegt
      }
    }

    // Erfolgs-Feedback mit Action
    toast.custom(
      <ActionToast
        message={`Kunde "${customer.name || customer.companyName}" erfolgreich angelegt!`}
        action={
          taskId
            ? {
                label: 'Aufgabe anzeigen',
                onClick: () => navigate(`/tasks/${taskId}`),
              }
            : undefined
        }
      />,
      {
        duration: 5000,
        position: 'top-right',
      }
    );

    // Liste aktualisieren
    await refetch();

    // Zur Detail-Seite navigieren
    navigate(`/customers/${customer.id}?highlight=new`);
  };

  if (isLoading) {
    return (
      <MainLayoutV2>
        <CustomerListSkeleton />
      </MainLayoutV2>
    );
  }

  return (
    <MainLayoutV2>
      <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        {/* Header mit Button - immer sichtbar */}
        <CustomerListHeader
          totalCount={customers.length}
          onAddCustomer={activeTab === 0 ? () => setWizardOpen(true) : undefined}
        />

        {/* Tab Navigation */}
        <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3 }}>
          <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
            <Tab label="Kundenliste" />
            <Tab label="Data Intelligence" />
            <Tab label="Data Freshness" />
          </Tabs>
        </Box>

        {/* Content Area */}
        <Box sx={{ flex: 1, overflow: 'auto', p: 3 }}>
          {activeTab === 0 &&
            (customers.length === 0 ? (
              <EmptyStateHero
                title="Noch keine Kunden"
                description="Legen Sie Ihren ersten Kunden an und starten Sie Ihre Erfolgsgeschichte!"
                illustration="/illustrations/empty-customers.svg"
                action={{
                  label: '✨ Ersten Kunden anlegen',
                  onClick: () => setWizardOpen(true),
                  variant: 'contained',
                  size: 'large',
                }}
                secondaryAction={{
                  label: 'Demo-Daten importieren',
                  onClick: () => navigate('/settings/import'),
                }}
              />
            ) : (
              <>
                {/* Filter Bar (später in Sprint 3) */}
                <CustomerTable
                  customers={customers}
                  onRowClick={customer => navigate(`/customers/${customer.id}`)}
                  highlightNew
                />
              </>
            ))}

          {activeTab === 1 && <DataHygieneDashboard />}

          {activeTab === 2 && <DataFreshnessManager />}
        </Box>

        {/* Wizard Modal/Drawer */}
        <CustomerOnboardingWizardModal
          open={wizardOpen}
          onClose={() => setWizardOpen(false)}
          onComplete={handleCustomerCreated}
        />
      </Box>
    </MainLayoutV2>
  );
}
