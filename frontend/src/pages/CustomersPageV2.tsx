import { useState, useEffect, useMemo } from 'react';
import { Box, Tabs, Tab, Button } from '@mui/material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { CustomerOnboardingWizardModal } from '../features/customers/components/wizard/CustomerOnboardingWizardModal';
import LeadWizard from '../features/leads/LeadWizard';
import AddFirstContactDialog from '../features/leads/AddFirstContactDialog';
import DeleteLeadDialog from '../features/leads/DeleteLeadDialog';
import type { Lead } from '../features/leads/types';
import { EmptyStateHero } from '../components/common/EmptyStateHero';
import { CustomerTable } from '../features/customers/components/CustomerTable';
import { VirtualizedCustomerTable } from '../features/customers/components/VirtualizedCustomerTable';
import { CustomerListHeader } from '../features/customers/components/CustomerListHeader';
import { CustomerListSkeleton } from '../features/customers/components/CustomerListSkeleton';
import { DataHygieneDashboard } from '../features/customers/components/intelligence/DataHygieneDashboard';
import { DataFreshnessManager } from '../features/customers/components/intelligence/DataFreshnessManager';
import { LeadQualityDashboard } from '../features/leads/components/intelligence/LeadQualityDashboard';
import { LeadProtectionManager } from '../features/leads/components/intelligence/LeadProtectionManager';
import { IntelligentFilterBar } from '../features/customers/components/filter/IntelligentFilterBar';
import { useAuth } from '../contexts/AuthContext';
import { generateLeadUrl } from '../utils/slugify';
import { useCustomers, useCustomerSearchAdvanced } from '../features/customer/api/customerQueries';
import { useLeads } from '../features/leads/hooks/useLeads';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { ActionToast } from '../components/notifications/ActionToast';
import { taskEngine } from '../services/taskEngine';
import { isFeatureEnabled } from '../config/featureFlags';
import { useFocusListStore } from '../features/customer/store/focusListStore';
import { getTableColumnsForContext } from '../features/customers/components/filter/contextConfig';
import type { Customer } from '../types/customer.types';
import type {
  FilterConfig,
  SortConfig,
  ColumnConfig,
} from '../features/customers/types/filter.types';

interface CustomersPageV2Props {
  openWizard?: boolean;
  defaultFilter?: FilterConfig;
  title?: string;
  createButtonLabel?: string;
  context?: 'customers' | 'leads'; // Lifecycle Context
}

export function CustomersPageV2({
  openWizard = false,
  defaultFilter = {},
  title,
  createButtonLabel,
  context = 'customers',
}: CustomersPageV2Props) {
  const [wizardOpen, setWizardOpen] = useState(openWizard);
  const [activeTab, setActiveTab] = useState(0);
  const [filterConfig, setFilterConfig] = useState<FilterConfig>(defaultFilter);
  const [activeColumns, setActiveColumns] = useState<ColumnConfig[]>([]);
  const [firstContactDialogOpen, setFirstContactDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null);

  // Use focus list store for sort configuration only
  const { sortBy, setSortBy } = useFocusListStore();

  // Get context-based column configuration
  const columnConfig = useMemo(() => {
    if (activeColumns.length > 0) {
      return activeColumns;
    }
    // Fallback: get default columns for context
    const defaultColumns = getTableColumnsForContext(context);
    return defaultColumns
      .filter(col => col.visible)
      .sort((a, b) => a.order - b.order)
      .map(col => ({
        id: col.field,
        label: col.label,
        visible: col.visible,
      }));
  }, [activeColumns, context]);

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

  // Build search request from filterConfig (WITHOUT text - text stays client-side!)
  const searchRequest = useMemo(() => {
    const filters: Array<{ field: string; operator: string; value: string | string[] }> = [];

    // Status filter
    if (filterConfig.status && filterConfig.status.length > 0) {
      filters.push({ field: 'status', operator: 'IN', value: filterConfig.status });
    }

    // Industry filter
    if (filterConfig.industry && filterConfig.industry.length > 0) {
      filters.push({ field: 'industry', operator: 'IN', value: filterConfig.industry });
    }

    // Risk level filter
    if (filterConfig.riskLevel && filterConfig.riskLevel.length > 0) {
      filterConfig.riskLevel.forEach(level => {
        if (level === 'CRITICAL') {
          filters.push({ field: 'riskScore', operator: 'GTE', value: '80' });
        } else if (level === 'HIGH') {
          filters.push({ field: 'riskScore', operator: 'GTE', value: '60' });
          filters.push({ field: 'riskScore', operator: 'LT', value: '80' });
        } else if (level === 'MEDIUM') {
          filters.push({ field: 'riskScore', operator: 'GTE', value: '30' });
          filters.push({ field: 'riskScore', operator: 'LT', value: '60' });
        } else if (level === 'LOW') {
          filters.push({ field: 'riskScore', operator: 'LT', value: '30' });
        }
      });
    }

    // Has contacts filter
    if (filterConfig.hasContacts !== null && filterConfig.hasContacts !== undefined) {
      filters.push({
        field: 'contactsCount',
        operator: filterConfig.hasContacts ? 'GT' : 'EQ',
        value: '0',
      });
    }

    // Last contact days filter
    if (filterConfig.lastContactDays) {
      const cutoffDate = new Date(Date.now() - filterConfig.lastContactDays * 24 * 60 * 60 * 1000);
      filters.push({
        field: 'lastContactDate',
        operator: 'LTE',
        value: cutoffDate.toISOString(),
      });
    }

    // Revenue range filter
    if (filterConfig.revenueRange) {
      const { min, max } = filterConfig.revenueRange;
      if (min !== null && min !== undefined) {
        filters.push({ field: 'expectedAnnualVolume', operator: 'GTE', value: min.toString() });
      }
      if (max !== null && max !== undefined) {
        filters.push({ field: 'expectedAnnualVolume', operator: 'LTE', value: max.toString() });
      }
    }

    // Created days filter
    if (filterConfig.createdDays) {
      const cutoffDate = new Date(Date.now() - filterConfig.createdDays * 24 * 60 * 60 * 1000);
      filters.push({
        field: 'createdAt',
        operator: 'GTE',
        value: cutoffDate.toISOString(),
      });
    }

    // Return search request WITHOUT globalSearch (text stays client-side)
    return {
      filters,
      sort: sortConfig.field
        ? {
            field: sortConfig.field,
            direction: sortConfig.direction === 'asc' ? ('ASC' as const) : ('DESC' as const),
          }
        : undefined,
    };
  }, [
    filterConfig.status,
    filterConfig.industry,
    filterConfig.riskLevel,
    filterConfig.hasContacts,
    filterConfig.lastContactDays,
    filterConfig.revenueRange,
    filterConfig.createdDays,
    sortConfig,
  ]);

  // Server-side search with pagination (only for structured filters)
  const [page, setPage] = useState(0);
  const pageSize = 50;

  // Use server-side search if any structured filter is active
  const hasStructuredFilters =
    (filterConfig.status && filterConfig.status.length > 0) ||
    (filterConfig.industry && filterConfig.industry.length > 0) ||
    (filterConfig.riskLevel && filterConfig.riskLevel.length > 0) ||
    filterConfig.hasContacts !== null ||
    filterConfig.lastContactDays !== null ||
    filterConfig.revenueRange !== null ||
    filterConfig.createdDays !== null;

  // Context-based data loading
  const leadsData = useLeads(); // For leads context
  const serverSideData = useCustomerSearchAdvanced(
    searchRequest,
    page,
    pageSize,
    hasStructuredFilters && context === 'customers' // Only enabled for customers with structured filters
  );
  const clientSideData = useCustomers(0, 1000, 'companyName'); // Fallback: Load all customers for client-side filtering

  // Use appropriate data source based on context
  const { data, isLoading, refetch } =
    context === 'leads' ? leadsData : hasStructuredFilters ? serverSideData : clientSideData;

  const customers = useMemo(() => {
    if (context === 'leads') {
      // Leads API returns array directly
      return Array.isArray(data) ? data : [];
    }
    // Customers API returns paginated response
    return data?.content || [];
  }, [data, context]);

  const hasMore = useMemo(() => {
    if (context === 'leads') return false; // No pagination for leads yet
    return hasStructuredFilters && data && !data.last;
  }, [hasStructuredFilters, data, context]);

  // Reset page when search request changes
  useEffect(() => {
    if (hasStructuredFilters) {
      setPage(0);
    }
  }, [searchRequest, hasStructuredFilters]);

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
        // Use riskScore if available
        if (customer.riskScore !== null && customer.riskScore !== undefined) {
          // Definierte Risiko-Level-Bereiche:
          // CRITICAL: 80-100
          // HIGH: 60-79
          // MEDIUM: 30-59
          // LOW: 0-29
          let riskLevel = 'LOW';
          if (customer.riskScore >= 80) {
            riskLevel = 'CRITICAL';
          } else if (customer.riskScore >= 60) {
            riskLevel = 'HIGH';
          } else if (customer.riskScore >= 30) {
            riskLevel = 'MEDIUM';
          }
          return filterConfig.riskLevel?.includes(riskLevel);
        }

        // No riskScore = exclude from risk filter (we don't calculate from lastContactDate anymore)
        return false;
      });
    }

    // Apply hasContacts filter (für "Mit/Ohne Kontakte")
    if (filterConfig.hasContacts !== null && filterConfig.hasContacts !== undefined) {
      filtered = filtered.filter(customer => {
        const hasContacts = (customer.contactsCount || 0) > 0;
        return filterConfig.hasContacts === hasContacts;
      });
    }

    // Apply lastContactDays filter (für "Lange kein Kontakt")
    if (filterConfig.lastContactDays) {
      filtered = filtered.filter(customer => {
        // Only consider customers with lastContactDate
        if (!customer.lastContactDate) return false;

        const daysSinceContact = Math.floor(
          (Date.now() - new Date(customer.lastContactDate).getTime()) / (1000 * 60 * 60 * 24)
        );
        return daysSinceContact >= filterConfig.lastContactDays;
      });
    }

    // Apply revenue range filter (für "Top-Kunden")
    if (filterConfig.revenueRange) {
      filtered = filtered.filter(customer => {
        // Only consider customers with expectedAnnualVolume
        if (customer.expectedAnnualVolume === null || customer.expectedAnnualVolume === undefined) {
          return false;
        }

        const revenue = customer.expectedAnnualVolume;
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

  // Sprint 2.1.7.2 D11: Derive selected customer from filteredCustomers
  const selectedCustomer = useMemo(
    () => filteredCustomers.find(c => c.id === selectedCustomerId) || null,
    [filteredCustomers, selectedCustomerId]
  );

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
    } catch (_error) {
      void _error;
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
      } catch (_error) {
        void _error;
        // Nicht blockierend - Customer wurde trotzdem angelegt
      }
    }

    // Erfolgs-Feedback mit Action
    const entityLabel = context === 'leads' ? 'Lead' : 'Kunde';
    toast.custom(
      <ActionToast
        message={`${entityLabel} "${customer.name || customer.companyName}" erfolgreich angelegt!`}
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

    // Sprint 2.1.7.2 D11: Navigate to Customer Detail Page
    if (context === 'customers') {
      navigate(`/customers/${customer.id}`);
    }
    // Bei Leads: Bleiben auf der Liste, neuer Lead wird highlighted
  };

  if (isLoading) {
    return (
      <MainLayoutV2 maxWidth="full">
        <CustomerListSkeleton />
      </MainLayoutV2>
    );
  }

  return (
    <MainLayoutV2 maxWidth="full">
      <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        {/* Header mit Button - immer sichtbar */}
        <CustomerListHeader
          totalCount={customers.length}
          title={title}
          createButtonLabel={createButtonLabel}
          onAddCustomer={() => setWizardOpen(true)}
        />

        {/* Tab Navigation */}
        <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3 }}>
          <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
            <Tab label={context === 'leads' ? 'Lead-Liste' : 'Kundenliste'} />
            <Tab label={context === 'leads' ? 'Lead-Qualität' : 'Datenqualität'} />
            <Tab label={context === 'leads' ? 'Schutzfristen' : 'Daten-Aktualität'} />
          </Tabs>
        </Box>

        {/* Content Area */}
        <Box sx={{ flex: 1, overflow: 'auto', p: 3 }}>
          {activeTab === 0 &&
            (customers.length === 0 ? (
              <EmptyStateHero
                title={context === 'leads' ? 'Noch keine Leads' : 'Noch keine Kunden'}
                description={
                  context === 'leads'
                    ? 'Erfassen Sie Ihren ersten Lead und starten Sie Ihre Neukundengewinnung!'
                    : 'Legen Sie Ihren ersten Kunden an und starten Sie Ihre Erfolgsgeschichte!'
                }
                illustration="/illustrations/empty-customers.svg"
                action={{
                  label:
                    context === 'leads' ? '✨ Ersten Lead erfassen' : '✨ Ersten Kunden anlegen',
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
                  onColumnChange={setActiveColumns}
                  totalCount={customers.length}
                  filteredCount={filteredCustomers.length}
                  loading={isLoading}
                  initialFilters={filterConfig}
                  context={context}
                />

                {/* Customer Table - Use virtualized version for > 20 items */}
                <Box>
                  {filteredCustomers.length > 20 ? (
                      <VirtualizedCustomerTable
                        customers={filteredCustomers}
                        columns={columnConfig}
                        onRowClick={customer => {
                          // Context-based behavior
                          if (context === 'customers') {
                            // Sprint 2.1.7.2 D11: Navigate to Customer Detail Page (Cockpit-Pattern)
                            navigate(`/customers/${customer.id}`);
                          } else if (context === 'leads') {
                            // Sprint 2.1.6 Phase 5+: Navigate to Lead Detail page with slug
                            navigate(generateLeadUrl(customer.companyName || 'lead', customer.id));
                          }
                        }}
                        height={600}
                        rowHeight={72}
                      />
                    ) : (
                      <CustomerTable
                        customers=  {filteredCustomers}
                        onRowClick={customer => {
                          // Context-based behavior
                          if (context === 'customers') {
                            // Sprint 2.1.7.2 D11: Navigate to Customer Detail Page (Cockpit-Pattern)
                            navigate(`/customers/${customer.id}`);
                          } else if (context === 'leads') {
                            // Sprint 2.1.6 Phase 5+: Navigate to Lead Detail page with slug
                            navigate(generateLeadUrl(customer.companyName || 'lead', customer.id));
                          }
                        }}
                        highlightNew
                        columns={columnConfig}
                        context={context}
                        showActions={true}
                        onEdit={customer => {
                          setSelectedLead(customer as unknown as Lead);
                          if (context === 'leads' && customer.leadStage === 'VORMERKUNG') {
                            setFirstContactDialogOpen(true);
                          } else if (context === 'leads') {
                            // Sprint 2.1.6 Phase 5+: Navigate to Lead Detail page with slug
                            navigate(generateLeadUrl(customer.companyName || 'lead', customer.id));
                          } else {
                            // Sprint 2.1.7.2 D11: Navigate to Customer Detail Page (Cockpit-Pattern)
                            navigate(`/customers/${customer.id}`);
                          }
                        }}
                        onDelete={customer => {
                          setSelectedLead(customer as unknown as Lead);
                          setDeleteDialogOpen(true);
                        }}
                      />
                    )}
                </Box>

                {/* Load More Button (only for server-side pagination) */}
                {hasMore && (
                  <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                    <Button
                      variant="outlined"
                      onClick={() => setPage(prev => prev + 1)}
                      disabled={isLoading}
                      size="large"
                    >
                      {isLoading
                        ? 'Lädt...'
                        : context === 'leads'
                          ? 'Weitere Leads laden'
                          : 'Weitere Kunden laden'}
                    </Button>
                  </Box>
                )}
              </Box>
            ))}

          {activeTab === 1 &&
            (context === 'leads' ? <LeadQualityDashboard /> : <DataHygieneDashboard />)}

          {activeTab === 2 &&
            (context === 'leads' ? <LeadProtectionManager /> : <DataFreshnessManager />)}
        </Box>

        {/* Wizard Modal/Drawer - Context-based */}
        {context === 'leads' ? (
          <LeadWizard
            open={wizardOpen}
            onClose={() => setWizardOpen(false)}
            onCreated={handleCustomerCreated}
          />
        ) : (
          <CustomerOnboardingWizardModal
            open={wizardOpen}
            onClose={() => setWizardOpen(false)}
            onComplete={handleCustomerCreated}
          />
        )}

        {/* AddFirstContact Dialog - Sprint 2.1.6 Phase 5 */}
        <AddFirstContactDialog
          open={firstContactDialogOpen}
          lead={selectedLead}
          onClose={() => {
            setFirstContactDialogOpen(false);
            setSelectedLead(null);
          }}
          onSuccess={() => {
            setFirstContactDialogOpen(false);
            setSelectedLead(null);
            refetch();
            toast.success('Erstkontakt erfolgreich dokumentiert!');
          }}
        />

        {/* DeleteLead Dialog */}
        <DeleteLeadDialog
          open={deleteDialogOpen}
          lead={selectedLead}
          onClose={() => {
            setDeleteDialogOpen(false);
            setSelectedLead(null);
          }}
          onSuccess={() => {
            setDeleteDialogOpen(false);
            setSelectedLead(null);
            refetch();
            toast.success('Lead erfolgreich gelöscht!');
          }}
        />
      </Box>
    </MainLayoutV2>
  );
}
