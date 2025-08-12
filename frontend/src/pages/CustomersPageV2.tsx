import { useState, useEffect, useMemo } from 'react';
import { Box, Tabs, Tab } from '@mui/material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { CustomerOnboardingWizardModal } from '../features/customers/components/wizard/CustomerOnboardingWizardModal';
import { EmptyStateHero } from '../components/common/EmptyStateHero';
import { CustomerTable } from '../features/customers/components/CustomerTable';
import { VirtualizedCustomerTable } from '../features/customers/components/VirtualizedCustomerTable';
import { CustomerListHeader } from '../features/customers/components/CustomerListHeader';
import { CustomerListSkeleton } from '../features/customers/components/CustomerListSkeleton';
import { DataHygieneDashboard } from '../features/customers/components/intelligence/DataHygieneDashboard';
import { DataFreshnessManager } from '../features/customers/components/intelligence/DataFreshnessManager';
import { IntelligentFilterBar } from '../features/customers/components/filter/IntelligentFilterBar';
import { useAuth } from '../contexts/AuthContext';
import { useCustomers } from '../features/customer/api/customerQueries';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { ActionToast } from '../components/notifications/ActionToast';
import { taskEngine } from '../services/taskEngine';
import { isFeatureEnabled } from '../config/featureFlags';
import { useFocusListStore } from '../features/customer/store/focusListStore';
import type { Customer } from '../types/customer.types';
import type { FilterConfig, SortConfig } from '../features/customers/types/filter.types';

interface CustomersPageV2Props {
  openWizard?: boolean;
}

export function CustomersPageV2({ openWizard = false }: CustomersPageV2Props) {
  const [wizardOpen, setWizardOpen] = useState(openWizard);
  const [activeTab, setActiveTab] = useState(0);
  const [filterConfig, setFilterConfig] = useState<FilterConfig>({});

  // Use focus list store for persistent column and sort configuration
  const {
    tableColumns,
    sortBy,
    setSortBy,
    // globalSearch,
    // activeFilters
  } = useFocusListStore();

  // Convert store columns to ColumnConfig format for compatibility
  const columnConfig = useMemo(
    () =>
      tableColumns
        .filter(col => col.visible)
        .sort((a, b) => a.order - b.order)
        .map(col => ({
          id: col.field,
          label: col.label,
          visible: col.visible,
        })),
    [tableColumns]
  );

  // Convert store sortBy to SortConfig format
  const sortConfig = useMemo(
    () => ({
      field: sortBy.field,
      direction: sortBy.ascending ? ('asc' as const) : ('desc' as const),
    }),
    [sortBy]
  );

  const { user } = useAuth();
  const navigate = useNavigate();

  // Open wizard when prop is true (on mount or prop change)
  useEffect(() => {
    setWizardOpen(openWizard);
  }, [openWizard]);

  // Listen for new customer event from sidebar
  useEffect(() => {
    const handleNewCustomer = () => setWizardOpen(true);
    window.addEventListener('freshplan:new-customer', handleNewCustomer);
    return () => window.removeEventListener('freshplan:new-customer', handleNewCustomer);
  }, []);

  // Verkäuferschutz: Nur eigene Kunden für Sales-Rolle
  const _filter = user?.role === 'sales' ? { assignedTo: user.id } : undefined;

  // Use existing useCustomers hook with pagination
  const { data, isLoading, refetch } = useCustomers(0, 1000); // Get all for now

  const customers = useMemo(() => data?.content || [], [data]);

  // Apply filters and sorting to customers
  const filteredCustomers = useMemo(() => {
    let filtered = [...customers];

    // Apply text search
    if (filterConfig.text) {
      const searchText = filterConfig.text.toLowerCase();
      filtered = filtered.filter(
        customer =>
          customer.companyName?.toLowerCase().includes(searchText) ||
          customer.customerNumber?.toLowerCase().includes(searchText) ||
          customer.industry?.toLowerCase().includes(searchText)
      );
    }

    // Apply status filter
    if (filterConfig.status && filterConfig.status.length > 0) {
      filtered = filtered.filter(customer => filterConfig.status?.includes(customer.status));
    }

    // Apply industry filter
    if (filterConfig.industry && filterConfig.industry.length > 0) {
      filtered = filtered.filter(customer => filterConfig.industry?.includes(customer.industry));
    }

    // Apply risk level filter
    if (filterConfig.riskLevel && filterConfig.riskLevel.length > 0) {
      filtered = filtered.filter(customer => {
        // Calculate risk level based on business logic
        const daysSinceContact = customer.lastContactDate
          ? Math.floor(
              (Date.now() - new Date(customer.lastContactDate).getTime()) / (1000 * 60 * 60 * 24)
            )
          : 999;

        let riskLevel = 'LOW';
        if (daysSinceContact > 90) riskLevel = 'HIGH';
        else if (daysSinceContact > 30) riskLevel = 'MEDIUM';

        return filterConfig.riskLevel?.includes(riskLevel);
      });
    }

    // Apply lastContactDays filter (für "Lange kein Kontakt")
    if (filterConfig.lastContactDays) {
      filtered = filtered.filter(customer => {
        const daysSinceContact = customer.lastContactDate
          ? Math.floor(
              (Date.now() - new Date(customer.lastContactDate).getTime()) / (1000 * 60 * 60 * 24)
            )
          : 999;
        return daysSinceContact >= filterConfig.lastContactDays;
      });
    }

    // Apply revenue range filter (für "Top-Kunden")
    if (filterConfig.revenueRange) {
      filtered = filtered.filter(customer => {
        const revenue = customer.expectedAnnualVolume || 0;
        const { min, max } = filterConfig.revenueRange || {};
        
        if (min !== null && min !== undefined && revenue < min) return false;
        if (max !== null && max !== undefined && revenue > max) return false;
        
        return true;
      });
    }

    // Apply createdDays filter (für "Neue Kunden")
    if (filterConfig.createdDays) {
      filtered = filtered.filter(customer => {
        const daysSinceCreated = customer.createdAt
          ? Math.floor(
              (Date.now() - new Date(customer.createdAt).getTime()) / (1000 * 60 * 60 * 24)
            )
          : 999;
        return daysSinceCreated <= filterConfig.createdDays;
      });
    }

    // Apply sorting
    if (sortConfig.field) {
      filtered.sort((a, b) => {
        const aValue = a[sortConfig.field as keyof Customer];
        const bValue = b[sortConfig.field as keyof Customer];

        if (aValue === null || aValue === undefined) return 1;
        if (bValue === null || bValue === undefined) return -1;

        const comparison = aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        return sortConfig.direction === 'asc' ? comparison : -comparison;
      });
    }

    return filtered;
  }, [customers, filterConfig, sortConfig]);

  const _handleExport = async (format: string) => {
    try {
      // Build query params from filter config
      const params = new URLSearchParams();
      if (filterConfig.status) {
        filterConfig.status.forEach(s => params.append('status', s));
      }
      if (filterConfig.industry && filterConfig.industry.length > 0) {
        params.append('industry', filterConfig.industry[0]);
      }

      const response = await fetch(`/api/export/customers/${format}?${params.toString()}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('authToken')}`,
        },
      });

      if (!response.ok) throw new Error('Export failed');

      // Special handling for PDF (HTML that opens in new tab for printing)
      if (format === 'pdf') {
        const htmlContent = await response.text();
        const newWindow = window.open('', '_blank');
        if (newWindow) {
          newWindow.document.write(htmlContent);
          newWindow.document.close();
          // Auto-trigger print dialog after a short delay
          setTimeout(() => {
            newWindow.print();
          }, 500);
          toast.success('PDF-Export geöffnet! Nutzen Sie die Druckfunktion zum Speichern als PDF.');
        } else {
          toast.error('Popup-Blocker verhindert das Öffnen des PDF-Exports');
        }
      } else {
        // Handle CSV, Excel, JSON as before
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        // Map format to correct file extension
        const fileExtension = format === 'excel' ? 'xlsx' : format;
        a.download = `customers_export_${new Date().toISOString().split('T')[0]}.${fileExtension}`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        toast.success(`Export als ${format.toUpperCase()} erfolgreich!`);
      }
    } catch (_error) { void _error;
      toast.error('Export fehlgeschlagen');
    }
  };

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
      } catch (_error) { void _error;
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
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, height: '100%' }}>
                {/* Intelligent Filter Bar */}
                <IntelligentFilterBar
                  onFilterChange={setFilterConfig}
                  onSortChange={(config: SortConfig) => {
                    setSortBy({
                      field: config.field,
                      ascending: config.direction === 'asc',
                    });
                  }}
                  totalCount={customers.length}
                  filteredCount={filteredCustomers.length}
                  loading={isLoading}
                />

                {/* Customer Table - Use virtualized version for > 20 items */}
                {filteredCustomers.length > 20 ? (
                  <VirtualizedCustomerTable
                    customers={filteredCustomers}
                    columns={columnConfig}
                    onRowClick={customer => navigate(`/customers/${customer.id}`)}
                    height={600}
                    rowHeight={72}
                  />
                ) : (
                  <CustomerTable
                    customers={filteredCustomers}
                    onRowClick={customer => navigate(`/customers/${customer.id}`)}
                    highlightNew
                    columns={columnConfig}
                  />
                )}
              </Box>
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
