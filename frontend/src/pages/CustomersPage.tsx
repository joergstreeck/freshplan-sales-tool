/**
 * CustomersPage - Clean Version
 *
 * Customer list page WITHOUT context switching.
 * Replaces CustomersPageV2 for customers-only context.
 * Created during Migration M2 (Sprint 2.1.7.7)
 *
 * @module CustomersPage
 * @since Sprint 2.1.7.7 (Migration M2)
 */

import { useState, useEffect, useMemo } from 'react';
import { Box, Tabs, Tab, Button, CircularProgress } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';

// Layout & Components
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { EntityListHeader } from '../features/shared/components/EntityListHeader';
import { ListSkeleton } from '../features/shared/components/ListSkeleton';
import { EmptyStateHero } from '../components/common/EmptyStateHero';
import { ActionToast } from '../components/notifications/ActionToast';

// Intelligence Dashboards
import { DataHygieneDashboard } from '../features/customers/components/intelligence/DataHygieneDashboard';
import { DataFreshnessManager } from '../features/customers/components/intelligence/DataFreshnessManager';

// Wizard
import { CustomerOnboardingWizardModal } from '../features/customers/components/wizard/CustomerOnboardingWizardModal';

// Shared Components (M1)
import { DataTable } from '../features/shared/components/data-table';
import { IntelligentFilterBar } from '../features/shared/components/IntelligentFilterBar';

// API & Stores
import { useCustomers, useCustomerSearchAdvanced } from '../features/customer/api/customerQueries';
import { useAuth } from '../contexts/AuthContext';
import { useFocusListStore } from '../features/customer/store/focusListStore';
import { taskEngine } from '../services/taskEngine';
import { useEnumOptions } from '../hooks/useEnumOptions';

// Types & Config
import type { CustomerResponse } from '../features/customer/types/customer.types';
import type { Customer } from '../types/customer.types';
import type {
  FilterConfig,
  SortConfig,
  ColumnConfig,
} from '../features/customers/types/filter.types';
import { getCustomerTableColumns } from '../features/customers/config/customerColumns';

interface CustomersPageProps {
  openWizard?: boolean;
  defaultFilter?: FilterConfig;
  title?: string;
  createButtonLabel?: string;
}

export default function CustomersPage({
  openWizard = false,
  defaultFilter = {},
  title = 'Kunden-Management',
  createButtonLabel = 'Neuer Kunde',
}: CustomersPageProps) {
  // State
  const [wizardOpen, setWizardOpen] = useState(openWizard);
  const [activeTab, setActiveTab] = useState(0);
  const [filterConfig, setFilterConfig] = useState<FilterConfig>(defaultFilter);
  const [activeColumns, setActiveColumns] = useState<ColumnConfig[]>([]);
  const [page, setPage] = useState(0);

  // Hooks
  const { user } = useAuth();
  const navigate = useNavigate();
  const { sortBy, setSortBy } = useFocusListStore();

  // Server-Driven Enums (1x Hook Call für die ganze Tabelle!)
  const { data: businessTypeOptions } = useEnumOptions('/api/enums/business-types');

  // Create fast lookup map (O(1) statt O(n) mit .find())
  const businessTypeLabels = useMemo(() => {
    if (!businessTypeOptions) return {};
    return businessTypeOptions.reduce(
      (acc, item) => {
        acc[item.value] = item.label;
        return acc;
      },
      {} as Record<string, string>
    );
  }, [businessTypeOptions]);

  // Column Configuration mit Server-Driven Labels
  const tableColumns = useMemo(() => {
    const columns = getCustomerTableColumns();

    // Apply user column preferences if available
    const columnsWithPreferences =
      activeColumns.length > 0
        ? columns.map(col => {
            const userCol = activeColumns.find(uc => uc.id === col.id);
            if (userCol) {
              return { ...col, visible: userCol.visible };
            }
            return col;
          })
        : columns;

    // Override industry column with Server-Driven labels
    // Note: customer.industry is DEPRECATED, should migrate to customer.businessType
    return columnsWithPreferences.map(col => {
      if (col.id === 'industry') {
        return {
          ...col,
          render: (customer: CustomerResponse) =>
            businessTypeLabels[customer.industry || ''] || customer.industry || '-',
        };
      }
      return col;
    });
  }, [activeColumns, businessTypeLabels]);

  // Sort Configuration
  const sortConfig = useMemo(
    () => ({
      field: sortBy.field,
      direction: sortBy.ascending ? ('asc' as const) : ('desc' as const),
    }),
    [sortBy]
  );

  // Open wizard when prop changes
  useEffect(() => {
    setWizardOpen(openWizard);
  }, [openWizard]);

  // Listen for new customer event from sidebar
  useEffect(() => {
    const handleNewCustomer = () => setWizardOpen(true);
    window.addEventListener('freshplan:new-customer', handleNewCustomer);
    return () => window.removeEventListener('freshplan:new-customer', handleNewCustomer);
  }, []);

  // Note: Verkäuferschutz (Sales filter by assignedTo) is handled in backend query

  // Build search request from filterConfig
  const searchRequest = useMemo(() => {
    const filters: Array<{ field: string; operator: string; value: string | string[] }> = [];

    if (filterConfig.status?.length) {
      filters.push({ field: 'status', operator: 'IN', value: filterConfig.status });
    }

    if (filterConfig.industry?.length) {
      filters.push({ field: 'industry', operator: 'IN', value: filterConfig.industry });
    }

    if (filterConfig.riskLevel?.length) {
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

    if (filterConfig.hasContacts !== null && filterConfig.hasContacts !== undefined) {
      filters.push({
        field: 'contactsCount',
        operator: filterConfig.hasContacts ? 'GT' : 'EQ',
        value: '0',
      });
    }

    if (filterConfig.lastContactDays) {
      const cutoffDate = new Date(Date.now() - filterConfig.lastContactDays * 24 * 60 * 60 * 1000);
      filters.push({
        field: 'lastContactDate',
        operator: 'LTE',
        value: cutoffDate.toISOString(),
      });
    }

    if (filterConfig.revenueRange) {
      const { min, max } = filterConfig.revenueRange;
      if (min !== null && min !== undefined) {
        filters.push({ field: 'expectedAnnualVolume', operator: 'GTE', value: min.toString() });
      }
      if (max !== null && max !== undefined) {
        filters.push({ field: 'expectedAnnualVolume', operator: 'LTE', value: max.toString() });
      }
    }

    if (filterConfig.createdDays) {
      const cutoffDate = new Date(Date.now() - filterConfig.createdDays * 24 * 60 * 60 * 1000);
      filters.push({
        field: 'createdAt',
        operator: 'GTE',
        value: cutoffDate.toISOString(),
      });
    }

    return {
      filters,
      sort: sortConfig.field
        ? {
            field: sortConfig.field,
            direction: sortConfig.direction === 'asc' ? ('ASC' as const) : ('DESC' as const),
          }
        : undefined,
    };
  }, [filterConfig, sortConfig]);

  // Determine if structured filters are active
  // WICHTIG: Boolean() um sicherzustellen, dass enabled ein Boolean ist (React Query Requirement)
  const hasStructuredFilters = Boolean(
    filterConfig.status?.length ||
      filterConfig.industry?.length ||
      filterConfig.riskLevel?.length ||
      filterConfig.hasContacts !== null ||
      filterConfig.lastContactDays !== null ||
      filterConfig.revenueRange !== null ||
      filterConfig.createdDays !== null
  );

  // Data loading: Server-side if structured filters, otherwise client-side
  const pageSize = 50;
  const serverSideData = useCustomerSearchAdvanced(
    searchRequest,
    page,
    pageSize,
    hasStructuredFilters
  );
  const clientSideData = useCustomers(0, 1000, 'companyName');

  const { data, isLoading, refetch } = hasStructuredFilters ? serverSideData : clientSideData;

  const customers = useMemo(() => {
    return data?.content || [];
  }, [data]);

  const hasMore = useMemo(() => {
    return hasStructuredFilters && data && !data.last;
  }, [hasStructuredFilters, data]);

  // Reset page when search changes
  useEffect(() => {
    if (hasStructuredFilters) {
      setPage(0);
    }
  }, [searchRequest, hasStructuredFilters]);

  // Apply client-side filtering (text search + fallback for non-server filters)
  const filteredCustomers = useMemo(() => {
    let filtered = [...customers];

    // Text search (client-side only)
    if (filterConfig.text) {
      const searchText = filterConfig.text.toLowerCase();
      filtered = filtered.filter(
        customer =>
          customer.companyName?.toLowerCase().includes(searchText) ||
          customer.customerNumber?.toLowerCase().includes(searchText) ||
          customer.industry?.toLowerCase().includes(searchText)
      );
    }

    // Fallback filters (when not using server-side search)
    if (!hasStructuredFilters) {
      if (filterConfig.status?.length) {
        filtered = filtered.filter(c => filterConfig.status?.includes(c.status));
      }

      if (filterConfig.industry?.length) {
        filtered = filtered.filter(c => filterConfig.industry?.includes(c.industry));
      }

      if (filterConfig.riskLevel?.length) {
        filtered = filtered.filter(c => {
          if (c.riskScore === null || c.riskScore === undefined) return false;
          let riskLevel = 'LOW';
          if (c.riskScore >= 80) riskLevel = 'CRITICAL';
          else if (c.riskScore >= 60) riskLevel = 'HIGH';
          else if (c.riskScore >= 30) riskLevel = 'MEDIUM';
          return filterConfig.riskLevel?.includes(riskLevel);
        });
      }

      if (filterConfig.hasContacts !== null && filterConfig.hasContacts !== undefined) {
        filtered = filtered.filter(c => {
          const hasContacts = (c.contactsCount || 0) > 0;
          return filterConfig.hasContacts === hasContacts;
        });
      }

      if (filterConfig.lastContactDays) {
        filtered = filtered.filter(c => {
          if (!c.lastContactDate) return false;
          const daysSince = Math.floor(
            (Date.now() - new Date(c.lastContactDate).getTime()) / (1000 * 60 * 60 * 24)
          );
          return daysSince >= filterConfig.lastContactDays!;
        });
      }

      if (filterConfig.revenueRange) {
        filtered = filtered.filter(c => {
          if (c.expectedAnnualVolume === null || c.expectedAnnualVolume === undefined) return false;
          const { min, max } = filterConfig.revenueRange || {};
          if (min !== null && min !== undefined && c.expectedAnnualVolume < min) return false;
          if (max !== null && max !== undefined && c.expectedAnnualVolume > max) return false;
          return true;
        });
      }

      if (filterConfig.createdDays) {
        filtered = filtered.filter(c => {
          const daysSince = c.createdAt
            ? Math.floor((Date.now() - new Date(c.createdAt).getTime()) / (1000 * 60 * 60 * 24))
            : 999;
          return daysSince <= filterConfig.createdDays!;
        });
      }
    }

    return filtered;
  }, [customers, filterConfig, hasStructuredFilters]);

  // Apply client-side sorting (after filtering)
  const sortedCustomers = useMemo(() => {
    const sorted = [...filteredCustomers];

    if (!sortConfig.field) return sorted;

    sorted.sort((a, b) => {
      const aValue = a[sortConfig.field as keyof CustomerResponse];
      const bValue = b[sortConfig.field as keyof CustomerResponse];

      // Handle null/undefined values
      if (aValue == null && bValue == null) return 0;
      if (aValue == null) return 1;
      if (bValue == null) return -1;

      // Compare values
      let comparison = 0;
      if (typeof aValue === 'string' && typeof bValue === 'string') {
        comparison = aValue.localeCompare(bValue);
      } else if (typeof aValue === 'number' && typeof bValue === 'number') {
        comparison = aValue - bValue;
      } else {
        // Fallback: convert to string and compare
        comparison = String(aValue).localeCompare(String(bValue));
      }

      return sortConfig.direction === 'asc' ? comparison : -comparison;
    });

    return sorted;
  }, [filteredCustomers, sortConfig]);

  // Handle customer creation
  const handleCustomerCreated = async (customer: Customer) => {
    // Create initial task
    const taskId = await taskEngine.createInitialCustomerTask({
      customerId: customer.id,
      customerName: customer.name || customer.companyName,
      createdBy: user?.name || 'System',
    });

    // Success toast with action
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

    // Refresh list
    await refetch();

    // Navigate to customer detail page (UUID-based)
    navigate(`/customer-management/customers/${customer.id}`);
  };

  // Handle row click
  const handleRowClick = (customer: CustomerResponse) => {
    navigate(`/customer-management/customers/${customer.id}`);
  };

  // Handle sort change
  const handleSortChange = (config: SortConfig) => {
    setSortBy({
      field: config.field,
      ascending: config.direction === 'asc',
    });
  };

  // Loading state
  if (isLoading && customers.length === 0) {
    return (
      <MainLayoutV2 maxWidth="full">
        <ListSkeleton />
      </MainLayoutV2>
    );
  }

  return (
    <MainLayoutV2 maxWidth="full">
      <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        {/* Header */}
        <EntityListHeader
          totalCount={customers.length}
          title={title}
          createButtonLabel={createButtonLabel}
          onAddEntity={() => setWizardOpen(true)}
          entityType="customer"
        />

        {/* Tab Navigation */}
        <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3 }}>
          <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
            <Tab label="Kundenliste" />
            <Tab label="Datenqualität" />
            <Tab label="Daten-Aktualität" />
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
                {/* Filter Bar */}
                <IntelligentFilterBar
                  onFilterChange={setFilterConfig}
                  onSortChange={handleSortChange}
                  onColumnChange={setActiveColumns}
                  totalCount={customers.length}
                  filteredCount={filteredCustomers.length}
                  loading={isLoading}
                  initialFilters={filterConfig}
                  context="customers"
                />

                {/* Customer Table */}
                <DataTable<CustomerResponse>
                  data={sortedCustomers}
                  columns={tableColumns}
                  getRowId={customer => customer.id}
                  onRowClick={handleRowClick}
                  highlightNew
                  loading={isLoading && sortedCustomers.length > 0}
                  emptyMessage="Keine Kunden gefunden"
                />

                {/* Load More Button (server-side pagination) */}
                {hasMore && (
                  <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                    <Button
                      variant="outlined"
                      onClick={() => setPage(prev => prev + 1)}
                      disabled={isLoading}
                      size="large"
                      startIcon={isLoading && <CircularProgress size={16} />}
                    >
                      {isLoading ? 'Lädt...' : 'Weitere Kunden laden'}
                    </Button>
                  </Box>
                )}
              </Box>
            ))}

          {activeTab === 1 && <DataHygieneDashboard />}
          {activeTab === 2 && <DataFreshnessManager />}
        </Box>

        {/* Customer Wizard */}
        <CustomerOnboardingWizardModal
          open={wizardOpen}
          onClose={() => setWizardOpen(false)}
          onComplete={handleCustomerCreated}
        />
      </Box>
    </MainLayoutV2>
  );
}
