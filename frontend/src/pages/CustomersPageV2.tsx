import { useState } from 'react';
import { Box, Button, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { CustomerOnboardingWizard } from '../features/customers/components/wizard/CustomerOnboardingWizardWrapper';
import { EmptyStateHero } from '../components/common/EmptyStateHero';
import { CustomerTable } from '../features/customers/components/CustomerTable';
import { CustomerListHeader } from '../features/customers/components/CustomerListHeader';
import { CustomerListSkeleton } from '../features/customers/components/CustomerListSkeleton';
import { useAuth } from '../contexts/AuthContext';
import { useCustomers } from '../features/customer/api/customerQueries';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { ActionToast } from '../components/notifications/ActionToast';
import { taskEngine } from '../services/taskEngine';
import { isFeatureEnabled } from '../config/featureFlags';
import type { Customer } from '../types/customer.types';

export function CustomersPageV2() {
  const [wizardOpen, setWizardOpen] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();
  
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
          context: { customer, user }
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
        action={taskId ? {
          label: "Aufgabe anzeigen",
          onClick: () => navigate(`/tasks/${taskId}`)
        } : undefined}
      />,
      {
        duration: 5000,
        position: 'top-right'
      }
    );
    
    // Liste aktualisieren
    await refetch();
    
    // Zur Detail-Seite navigieren
    navigate(`/customers/${customer.id}?highlight=new`);
  };

  if (isLoading) {
    return <CustomerListSkeleton />;
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header mit Button - immer sichtbar */}
      <CustomerListHeader 
        totalCount={customers.length}
        onAddCustomer={() => setWizardOpen(true)}
      />
      
      {/* Content Area */}
      <Box sx={{ flex: 1, overflow: 'auto', p: 3 }}>
        {customers.length === 0 ? (
          <EmptyStateHero 
            title="Noch keine Kunden"
            description="Legen Sie Ihren ersten Kunden an und starten Sie Ihre Erfolgsgeschichte!"
            illustration="/illustrations/empty-customers.svg"
            action={{
              label: "✨ Ersten Kunden anlegen",
              onClick: () => setWizardOpen(true),
              variant: "contained",
              size: "large"
            }}
            secondaryAction={{
              label: "Demo-Daten importieren",
              onClick: () => navigate('/settings/import')
            }}
          />
        ) : (
          <>
            {/* Filter Bar (später in Sprint 3) */}
            <CustomerTable 
              customers={customers}
              onRowClick={(customer) => navigate(`/customers/${customer.id}`)}
              highlightNew
            />
          </>
        )}
      </Box>
      
      {/* Wizard Modal/Drawer */}
      <CustomerOnboardingWizard
        open={wizardOpen}
        onClose={() => setWizardOpen(false)}
        onComplete={handleCustomerCreated}
      />
    </Box>
  );
}