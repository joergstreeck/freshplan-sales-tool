/**
 * LeadsPage - Clean Version
 *
 * Lead list page WITHOUT context switching.
 * Created during Migration M3 (Sprint 2.1.7.7)
 *
 * @module LeadsPage
 * @since Sprint 2.1.7.7 (Migration M3)
 */

import { useState, useEffect, useMemo } from 'react';
import { Box, Tabs, Tab, IconButton, Tooltip } from '@mui/material';
import { Timeline as TimelineIcon, Pause as PauseIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';

// Layout & Components
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { EntityListHeader } from '../features/shared/components/EntityListHeader';
import { ListSkeleton } from '../features/shared/components/ListSkeleton';
import { EmptyStateHero } from '../components/common/EmptyStateHero';
import { ActionToast } from '../components/notifications/ActionToast';

// Lead-specific Intelligence Dashboards
import { LeadQualityDashboard } from '../features/leads/components/intelligence/LeadQualityDashboard';
import { LeadProtectionManager } from '../features/leads/components/intelligence/LeadProtectionManager';

// Lead-specific Wizards & Dialogs
import LeadWizard from '../features/leads/LeadWizard';
import AddFirstContactDialog from '../features/leads/AddFirstContactDialog';
import DeleteLeadDialog from '../features/leads/DeleteLeadDialog';
import StopTheClockDialog from '../features/leads/StopTheClockDialog';
import LeadActivityTimeline from '../features/leads/LeadActivityTimeline';

// Shared Components (M1)
import { DataTable } from '../features/shared/components/data-table';
import { IntelligentFilterBar } from '../features/shared/components/IntelligentFilterBar';

// API & Stores
import { useLeads } from '../features/leads/hooks/useLeads';
import { useAuth } from '../contexts/AuthContext';
import { useFocusListStore } from '../features/customer/store/focusListStore';
import { taskEngine } from '../services/taskEngine';

// Types & Config
import type { Lead } from '../features/leads/types';
import type { Customer } from '../types/customer.types';
import type {
  FilterConfig,
  SortConfig,
  ColumnConfig,
} from '../features/customers/types/filter.types';
import { getLeadTableColumns } from '../features/leads/config/leadColumns';

// Utils
import { generateLeadUrl } from '../utils/slugify';

interface LeadsPageProps {
  openWizard?: boolean;
  defaultFilter?: FilterConfig;
  title?: string;
  createButtonLabel?: string;
}

export default function LeadsPage({
  openWizard = false,
  defaultFilter = {},
  title = 'Lead-Management',
  createButtonLabel = 'Lead erfassen',
}: LeadsPageProps) {
  // State
  const [wizardOpen, setWizardOpen] = useState(openWizard);
  const [activeTab, setActiveTab] = useState(0);
  const [filterConfig, setFilterConfig] = useState<FilterConfig>(defaultFilter);
  const [activeColumns, setActiveColumns] = useState<ColumnConfig[]>([]);
  const [firstContactDialogOpen, setFirstContactDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [stopClockDialogOpen, setStopClockDialogOpen] = useState(false);
  const [timelineDialogOpen, setTimelineDialogOpen] = useState(false);
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null);

  // Hooks
  const { user } = useAuth();
  const navigate = useNavigate();
  const { sortBy, setSortBy } = useFocusListStore();

  // Column Configuration
  const tableColumns = useMemo(() => {
    const columns = getLeadTableColumns();

    // Apply user column preferences if available
    if (activeColumns.length > 0) {
      return columns.map(col => {
        const userCol = activeColumns.find(uc => uc.id === col.id);
        if (userCol) {
          return { ...col, visible: userCol.visible };
        }
        return col;
      });
    }

    return columns;
  }, [activeColumns]);

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

  // Listen for new lead event from sidebar
  useEffect(() => {
    const handleNewLead = () => setWizardOpen(true);
    window.addEventListener('freshplan:new-lead', handleNewLead);
    return () => window.removeEventListener('freshplan:new-lead', handleNewLead);
  }, []);

  // Data loading: Client-side only (Leads use client-side pagination)
  const { data: leadsData, isLoading, refetch } = useLeads();

  const leads = useMemo(() => {
    return leadsData || [];
  }, [leadsData]);

  // Apply client-side filtering (text search + all filters)
  const filteredLeads = useMemo(() => {
    let filtered = [...leads];

    // Text search
    if (filterConfig.text) {
      const searchText = filterConfig.text.toLowerCase();
      filtered = filtered.filter(
        lead =>
          lead.companyName?.toLowerCase().includes(searchText) ||
          lead.businessType?.toLowerCase().includes(searchText)
      );
    }

    // Status filter
    if (filterConfig.status?.length) {
      filtered = filtered.filter(l => filterConfig.status?.includes(l.status));
    }

    // Business Type filter (Lead-spezifisch statt industry!)
    if (filterConfig.industry?.length) {
      // Note: FilterConfig uses "industry" field, but Leads use "businessType"
      filtered = filtered.filter(l => filterConfig.industry?.includes(l.businessType));
    }

    // Lead Score filter (statt riskLevel!)
    if (filterConfig.riskLevel?.length) {
      filtered = filtered.filter(l => {
        if (l.leadScore === null || l.leadScore === undefined) return false;
        let scoreLevel = 'LOW';
        if (l.leadScore >= 80) scoreLevel = 'CRITICAL';
        else if (l.leadScore >= 60) scoreLevel = 'HIGH';
        else if (l.leadScore >= 40) scoreLevel = 'MEDIUM';
        return filterConfig.riskLevel?.includes(scoreLevel);
      });
    }

    // Contacts filter
    if (filterConfig.hasContacts !== null && filterConfig.hasContacts !== undefined) {
      filtered = filtered.filter(l => {
        const hasContacts = (l.contactsCount || 0) > 0;
        return filterConfig.hasContacts === hasContacts;
      });
    }

    // Last Contact Days filter
    if (filterConfig.lastContactDays) {
      filtered = filtered.filter(l => {
        if (!l.lastContactDate) return false;
        const daysSince = Math.floor(
          (Date.now() - new Date(l.lastContactDate).getTime()) / (1000 * 60 * 60 * 24)
        );
        return daysSince >= filterConfig.lastContactDays!;
      });
    }

    // Revenue Range filter
    if (filterConfig.revenueRange) {
      filtered = filtered.filter(l => {
        if (l.expectedAnnualVolume === null || l.expectedAnnualVolume === undefined) return false;
        const { min, max } = filterConfig.revenueRange || {};
        if (min !== null && min !== undefined && l.expectedAnnualVolume < min) return false;
        if (max !== null && max !== undefined && l.expectedAnnualVolume > max) return false;
        return true;
      });
    }

    // Created Days filter
    if (filterConfig.createdDays) {
      filtered = filtered.filter(l => {
        const daysSince = l.createdAt
          ? Math.floor((Date.now() - new Date(l.createdAt).getTime()) / (1000 * 60 * 60 * 24))
          : 999;
        return daysSince <= filterConfig.createdDays!;
      });
    }

    return filtered;
  }, [leads, filterConfig]);

  // Handle lead creation
  const handleLeadCreated = async (lead: Customer) => {
    // Create initial task
    const taskId = await taskEngine.createInitialCustomerTask({
      customerId: lead.id,
      customerName: lead.name || lead.companyName,
      createdBy: user?.name || 'System',
    });

    // Success toast with action
    toast.custom(
      <ActionToast
        message={`Lead "${lead.name || lead.companyName}" erfolgreich erfasst!`}
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

    // LEAD-SPEZIFISCH: Bleiben auf der Liste (NICHT Detail-Page wie bei Customers!)
    // Neuer Lead wird automatisch highlighted durch "NEW" Badge
  };

  // Handle row click - LEAD-SPEZIFISCH: Slug-Navigation!
  const handleRowClick = (lead: Lead) => {
    // Slug-basierte Navigation: /lead-generation/leads/baeckerei-mueller-123
    const url = generateLeadUrl(lead.companyName || 'lead', lead.id);
    navigate(url);
  };

  // Handle row edit click (for VORMERKUNG stage)
  const handleEditClick = (lead: Lead) => {
    // LEAD-SPEZIFISCH: VORMERKUNG Stage → AddFirstContactDialog
    // Note: Backend sends stage as string (e.g., "VORMERKUNG"), not enum number
    if (lead.stage === 0 || (lead.status as any) === 'REGISTERED') {
      setSelectedLead(lead);
      setFirstContactDialogOpen(true);
    } else {
      // Normal edit → navigate to detail
      handleRowClick(lead);
    }
  };

  // Handle row delete click
  const handleDeleteClick = (lead: Lead) => {
    setSelectedLead(lead);
    setDeleteDialogOpen(true);
  };

  // Handle sort change
  const handleSortChange = (config: SortConfig) => {
    setSortBy({
      field: config.field,
      ascending: config.direction === 'asc',
    });
  };

  // Handle timeline click
  const handleTimelineClick = (lead: Lead) => {
    setSelectedLead(lead);
    setTimelineDialogOpen(true);
  };

  // Handle stop-the-clock click
  const handleStopClockClick = (lead: Lead) => {
    setSelectedLead(lead);
    setStopClockDialogOpen(true);
  };

  // Custom actions renderer for lead-specific actions
  const renderLeadActions = (lead: Lead) => (
    <>
      <Tooltip title="Timeline anzeigen">
        <IconButton
          size="small"
          onClick={e => {
            e.stopPropagation();
            handleTimelineClick(lead);
          }}
          sx={{ color: 'info.main' }}
        >
          <TimelineIcon fontSize="small" />
        </IconButton>
      </Tooltip>
      <Tooltip title={lead.clockStoppedAt ? 'Fortsetzen' : 'Pausieren'}>
        <IconButton
          size="small"
          onClick={e => {
            e.stopPropagation();
            handleStopClockClick(lead);
          }}
          sx={{ color: lead.clockStoppedAt ? 'success.main' : 'warning.main' }}
        >
          <PauseIcon fontSize="small" />
        </IconButton>
      </Tooltip>
    </>
  );

  // Loading state
  if (isLoading && leads.length === 0) {
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
          totalCount={leads.length}
          title={title}
          createButtonLabel={createButtonLabel}
          onAddEntity={() => setWizardOpen(true)}
          entityType="lead"
        />

        {/* Tab Navigation - LEAD-SPEZIFISCH */}
        <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3 }}>
          <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
            <Tab label="Lead-Liste" />
            <Tab label="Lead-Qualität" />
            <Tab label="Schutzfristen" />
          </Tabs>
        </Box>

        {/* Content Area */}
        <Box sx={{ flex: 1, overflow: 'auto', p: 3 }}>
          {activeTab === 0 &&
            (leads.length === 0 ? (
              <EmptyStateHero
                title="Noch keine Leads"
                description="Erfassen Sie Ihren ersten Lead und starten Sie Ihre Neukundenakquise!"
                illustration="/illustrations/empty-leads.svg"
                action={{
                  label: '✨ Ersten Lead erfassen',
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
                  totalCount={leads.length}
                  filteredCount={filteredLeads.length}
                  loading={isLoading}
                  initialFilters={filterConfig}
                  context="leads"
                />

                {/* Lead Table */}
                <DataTable<Lead>
                  data={filteredLeads}
                  columns={tableColumns}
                  getRowId={lead => lead.id}
                  onRowClick={handleRowClick}
                  onEdit={handleEditClick}
                  onDelete={handleDeleteClick}
                  showActions
                  customActions={renderLeadActions}
                  highlightNew
                  loading={isLoading && filteredLeads.length > 0}
                  emptyMessage="Keine Leads gefunden"
                  sortConfig={sortConfig}
                  onSortChange={handleSortChange}
                />
              </Box>
            ))}

          {/* LEAD-SPEZIFISCHE TABS */}
          {activeTab === 1 && <LeadQualityDashboard />}
          {activeTab === 2 && <LeadProtectionManager />}
        </Box>

        {/* LEAD-SPEZIFISCHER WIZARD */}
        <LeadWizard
          open={wizardOpen}
          onClose={() => setWizardOpen(false)}
          onComplete={handleLeadCreated}
        />

        {/* LEAD-SPEZIFISCHE DIALOGE */}
        {selectedLead && (
          <>
            <AddFirstContactDialog
              open={firstContactDialogOpen}
              lead={selectedLead}
              onClose={() => {
                setFirstContactDialogOpen(false);
                setSelectedLead(null);
              }}
              onSuccess={async () => {
                setFirstContactDialogOpen(false);
                setSelectedLead(null);
                await refetch();
              }}
            />
            <DeleteLeadDialog
              open={deleteDialogOpen}
              lead={selectedLead}
              onClose={() => {
                setDeleteDialogOpen(false);
                setSelectedLead(null);
              }}
              onSuccess={async () => {
                setDeleteDialogOpen(false);
                setSelectedLead(null);
                await refetch();
              }}
            />
            <StopTheClockDialog
              open={stopClockDialogOpen}
              lead={selectedLead}
              onClose={() => {
                setStopClockDialogOpen(false);
                setSelectedLead(null);
              }}
              onSuccess={async () => {
                setStopClockDialogOpen(false);
                setSelectedLead(null);
                await refetch();
              }}
            />
            <LeadActivityTimeline
              open={timelineDialogOpen}
              lead={selectedLead}
              onClose={() => {
                setTimelineDialogOpen(false);
                setSelectedLead(null);
              }}
            />
          </>
        )}
      </Box>
    </MainLayoutV2>
  );
}
