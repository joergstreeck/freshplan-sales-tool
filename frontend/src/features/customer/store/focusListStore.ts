import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';

// Type für Filter-Werte
export type FilterValue =
  | string
  | number
  | boolean
  | Date
  | string[]
  | number[]
  | [number, number]
  | null;

export interface FilterCriteria {
  id: string;
  field: string;
  operator: FilterOperator;
  value: FilterValue;
  combineWith?: 'AND' | 'OR';
}

export enum FilterOperator {
  EQUALS = 'EQUALS',
  NOT_EQUALS = 'NOT_EQUALS',
  GREATER_THAN = 'GREATER_THAN',
  LESS_THAN = 'LESS_THAN',
  GREATER_THAN_OR_EQUAL = 'GREATER_THAN_OR_EQUAL',
  LESS_THAN_OR_EQUAL = 'LESS_THAN_OR_EQUAL',
  CONTAINS = 'CONTAINS',
  STARTS_WITH = 'STARTS_WITH',
  ENDS_WITH = 'ENDS_WITH',
  IN = 'IN',
  NOT_IN = 'NOT_IN',
  BETWEEN = 'BETWEEN',
  IS_NULL = 'IS_NULL',
  IS_NOT_NULL = 'IS_NOT_NULL',
}

export interface SortCriteria {
  field: string;
  ascending: boolean;
}

export interface SmartSortOption {
  id: string;
  label: string;
  description: string;
  category: 'priority' | 'business' | 'activity' | 'custom';
  sorts: SortCriteria[];
  icon?: string;
}

export interface TableColumn {
  id: string;
  label: string;
  field: string;
  visible: boolean;
  order: number;
  align?: 'left' | 'center' | 'right';
  minWidth?: number;
}

export interface SavedView {
  id: string;
  name: string;
  description?: string;
  filters: FilterCriteria[];
  globalSearch: string;
  sort: SortCriteria;
  viewMode: 'cards' | 'table';
  tableColumns?: TableColumn[];
  createdAt: Date;
}

interface FocusListStore {
  // Filter State
  globalSearch: string;
  activeFilters: FilterCriteria[];
  savedViews: SavedView[];
  currentViewId: string | null;

  // View State
  viewMode: 'cards' | 'table';
  sortBy: SortCriteria;
  smartSortId: string | null;
  tableColumns: TableColumn[];

  // Selection State
  selectedCustomerId: string | null;

  // Pagination
  page: number;
  pageSize: number;

  // Derived State
  filterCount: number;
  hasActiveFilters: boolean;
  visibleTableColumns: TableColumn[];

  // Actions - Search & Filter
  setGlobalSearch: (search: string) => void;
  addFilter: (filter: Omit<FilterCriteria, 'id'>) => void;
  removeFilter: (filterId: string) => void;
  updateFilter: (filterId: string, updates: Partial<FilterCriteria>) => void;
  clearAllFilters: () => void;
  toggleQuickFilter: (field: string, value: FilterValue) => void;
  hasFilter: (field: string, value: FilterValue) => boolean;

  // Actions - View Management
  saveCurrentView: (name: string, description?: string) => void;
  loadSavedView: (viewId: string) => void;
  deleteSavedView: (viewId: string) => void;
  updateCurrentView: () => void;

  // Actions - Display
  setViewMode: (mode: 'cards' | 'table') => void;
  setSortBy: (sort: SortCriteria) => void;
  setSmartSort: (smartSortId: string) => void;
  setPage: (page: number) => void;
  setPageSize: (pageSize: number) => void;

  // Actions - Table Columns
  toggleColumnVisibility: (columnId: string) => void;
  setColumnOrder: (columnIds: string[]) => void;
  resetTableColumns: () => void;

  // Actions - Selection
  setSelectedCustomer: (customerId: string | null) => void;

  // API Request Builder
  getSearchRequest: () => CustomerSearchRequest;
}

// Type für API Request
export interface CustomerSearchRequest {
  globalSearch?: string;
  filters?: Array<{
    field: string;
    operator: string;
    value: FilterValue;
    combineWith?: string;
  }>;
  sort?: {
    field: string;
    ascending: boolean;
  };
  multiSort?: Array<{
    field: string;
    ascending: boolean;
  }>;
}

// Default Tabellen-Spalten
const DEFAULT_TABLE_COLUMNS: TableColumn[] = [
  { id: 'companyName', label: 'Kunde', field: 'companyName', visible: true, order: 0 },
  {
    id: 'customerNumber',
    label: 'Kundennummer',
    field: 'customerNumber',
    visible: false,
    order: 1,
  },
  { id: 'status', label: 'Status', field: 'status', visible: true, order: 2 },
  {
    id: 'riskScore',
    label: 'Risiko',
    field: 'riskScore',
    visible: true,
    order: 3,
    align: 'center',
  },
  { id: 'industry', label: 'Branche', field: 'industry', visible: true, order: 4 },
  {
    id: 'expectedAnnualVolume',
    label: 'Jahresumsatz',
    field: 'expectedAnnualVolume',
    visible: false,
    order: 5,
    align: 'right',
  },
  {
    id: 'lastContactDate',
    label: 'Letzter Kontakt',
    field: 'lastContactDate',
    visible: false,
    order: 6,
  },
  { id: 'assignedTo', label: 'Betreuer', field: 'assignedTo', visible: false, order: 7 },
  { id: 'actions', label: 'Aktionen', field: 'actions', visible: true, order: 8, align: 'right' },
];

export const useFocusListStore = create<FocusListStore>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial State
        globalSearch: '',
        activeFilters: [],
        savedViews: [],
        currentViewId: null,
        viewMode: 'cards',
        sortBy: { field: 'lastContactDate', ascending: false },
        smartSortId: 'revenue-high-to-low',
        tableColumns: [...DEFAULT_TABLE_COLUMNS],
        selectedCustomerId: null,
        page: 0,
        pageSize: 50, // Erhöht für bessere Übersicht aller Testkunden

        // Derived State
        get filterCount() {
          return get().activeFilters.length;
        },

        get hasActiveFilters() {
          return get().globalSearch !== '' || get().activeFilters.length > 0;
        },

        get visibleTableColumns() {
          return get()
            .tableColumns.filter(col => col.visible)
            .sort((a, b) => a.order - b.order);
        },

        // Search & Filter Actions
        setGlobalSearch: search => set({ globalSearch: search, page: 0 }),

        addFilter: filter =>
          set(state => ({
            activeFilters: [...state.activeFilters, { ...filter, id: crypto.randomUUID() }],
            page: 0,
          })),

        removeFilter: filterId =>
          set(state => ({
            activeFilters: state.activeFilters.filter(f => f.id !== filterId),
            page: 0,
          })),

        updateFilter: (filterId, updates) =>
          set(state => ({
            activeFilters: state.activeFilters.map(f =>
              f.id === filterId ? { ...f, ...updates } : f
            ),
            page: 0,
          })),

        clearAllFilters: () => set({ globalSearch: '', activeFilters: [], page: 0 }),

        toggleQuickFilter: (field, value) => {
          const state = get();

          // Prüfe ZUERST ob der Filter bereits aktiv ist
          const isActive =
            state.activeFilters.length === 1 &&
            ((field === 'riskScore' &&
              value === '>70' &&
              state.activeFilters[0].field === 'riskScore' &&
              state.activeFilters[0].operator === FilterOperator.GREATER_THAN &&
              state.activeFilters[0].value === 70) ||
              (field !== 'riskScore' &&
                state.activeFilters[0].field === field &&
                state.activeFilters[0].value === value));

          if (isActive) {
            // Filter ist aktiv -> deaktivieren (alle Filter löschen)
            set({ activeFilters: [], page: 0 });
          } else {
            // Filter ist nicht aktiv -> aktivieren (alle anderen löschen, neuen setzen)
            if (field === 'riskScore' && value === '>70') {
              set({
                activeFilters: [
                  {
                    id: crypto.randomUUID(),
                    field: 'riskScore',
                    operator: FilterOperator.GREATER_THAN,
                    value: 70,
                  },
                ],
                page: 0,
              });
            } else {
              set({
                activeFilters: [
                  {
                    id: crypto.randomUUID(),
                    field,
                    operator: FilterOperator.EQUALS,
                    value,
                  },
                ],
                page: 0,
              });
            }
          }
        },

        hasFilter: (field, value) => {
          const filters = get().activeFilters;
          if (field === 'riskScore' && value === '>70') {
            return filters.some(
              f =>
                f.field === 'riskScore' &&
                f.operator === FilterOperator.GREATER_THAN &&
                f.value === 70
            );
          }
          return filters.some(f => f.field === field && f.value === value);
        },

        // View Management Actions
        saveCurrentView: (name, description) => {
          const state = get();
          const newView: SavedView = {
            id: crypto.randomUUID(),
            name,
            description,
            filters: [...state.activeFilters],
            globalSearch: state.globalSearch,
            sort: { ...state.sortBy },
            viewMode: state.viewMode,
            createdAt: new Date(),
          };

          set(state => ({
            savedViews: [...state.savedViews, newView],
            currentViewId: newView.id,
          }));
        },

        loadSavedView: viewId => {
          const view = get().savedViews.find(v => v.id === viewId);
          if (view) {
            set({
              globalSearch: view.globalSearch,
              activeFilters: [...view.filters],
              sortBy: { ...view.sort },
              viewMode: view.viewMode,
              currentViewId: viewId,
              page: 0,
            });
          }
        },

        deleteSavedView: viewId =>
          set(state => ({
            savedViews: state.savedViews.filter(v => v.id !== viewId),
            currentViewId: state.currentViewId === viewId ? null : state.currentViewId,
          })),

        updateCurrentView: () => {
          const state = get();
          if (!state.currentViewId) return;

          set(state => ({
            savedViews: state.savedViews.map(v =>
              v.id === state.currentViewId
                ? {
                    ...v,
                    filters: [...state.activeFilters],
                    globalSearch: state.globalSearch,
                    sort: { ...state.sortBy },
                    viewMode: state.viewMode,
                  }
                : v
            ),
          }));
        },

        // Display Actions
        setViewMode: mode => set({ viewMode: mode }),

        setSortBy: sort => set({ sortBy: sort, smartSortId: null, page: 0 }),

        setSmartSort: smartSortId => {
          const smartSort = getSmartSortById(smartSortId);
          if (smartSort) {
            set({
              sortBy: smartSort.sorts[0], // Use first sort criterion for backward compatibility
              smartSortId,
              page: 0,
            });
          }
        },

        setPage: page => set({ page }),

        setPageSize: pageSize => set({ pageSize, page: 0 }),

        // Table Column Actions
        toggleColumnVisibility: columnId =>
          set(state => ({
            tableColumns: state.tableColumns.map(col =>
              col.id === columnId ? { ...col, visible: !col.visible } : col
            ),
          })),

        setColumnOrder: columnIds =>
          set(state => ({
            tableColumns: state.tableColumns.map(col => ({
              ...col,
              order: columnIds.indexOf(col.id),
            })),
          })),

        resetTableColumns: () => set({ tableColumns: [...DEFAULT_TABLE_COLUMNS] }),

        // Selection Actions
        setSelectedCustomer: customerId => set({ selectedCustomerId: customerId }),

        // API Request Builder
        getSearchRequest: () => {
          const state = get();
          const request: CustomerSearchRequest = {};

          if (state.globalSearch) {
            request.globalSearch = state.globalSearch;
          }

          if (state.activeFilters.length > 0) {
            request.filters = state.activeFilters.map(f => ({
              field: f.field,
              operator: f.operator,
              value: f.value,
              combineWith: f.combineWith || 'AND',
            }));
          }

          // If smart sort is active, use multi-sort; otherwise use single sort
          if (state.smartSortId) {
            const smartSort = getSmartSortById(state.smartSortId);
            if (smartSort && smartSort.sorts.length > 1) {
              request.multiSort = smartSort.sorts.map(s => ({
                field: s.field,
                ascending: s.ascending,
              }));
            } else {
              request.sort = {
                field: state.sortBy.field,
                ascending: state.sortBy.ascending,
              };
            }
          } else {
            request.sort = {
              field: state.sortBy.field,
              ascending: state.sortBy.ascending,
            };
          }

          return request;
        },
      }),
      {
        name: 'focus-list-store',
        partialize: state => ({
          savedViews: state.savedViews,
          viewMode: state.viewMode,
          pageSize: state.pageSize,
          tableColumns: state.tableColumns,
        }),
      }
    )
  )
);

// Smart Sort Optionen - ÜBERARBEITET: Klarere, intuitive Sales-Sortierung
export const SMART_SORT_OPTIONS: SmartSortOption[] = [
  // === HARDFACTS - Geschäftskritisch ===
  {
    id: 'revenue-high-to-low',
    label: 'Umsatz: Hoch → Niedrig',
    description: 'Größte Kunden zuerst (Jahresumsatz)',
    category: 'business',
    icon: '💰',
    sorts: [
      { field: 'expectedAnnualVolume', ascending: false },
      { field: 'companyName', ascending: true },
    ],
  },
  {
    id: 'revenue-low-to-high',
    label: 'Umsatz: Niedrig → Hoch',
    description: 'Kleinste Kunden zuerst (Potenzial identifizieren)',
    category: 'business',
    icon: '💡',
    sorts: [
      { field: 'expectedAnnualVolume', ascending: true },
      { field: 'companyName', ascending: true },
    ],
  },
  {
    id: 'risk-critical-first',
    label: 'Risiko-Kunden ZUERST',
    description: 'Gefährdete Kunden retten (Risiko-Score hoch)',
    category: 'priority',
    icon: '🚨',
    sorts: [
      { field: 'riskScore', ascending: false },
      { field: 'expectedAnnualVolume', ascending: false },
    ],
  },
  {
    id: 'contracts-expiring',
    label: 'Auslaufende Verträge',
    description: 'Nächste Follow-Ups / Vertragsverlängerungen',
    category: 'priority',
    icon: '⏰',
    sorts: [
      { field: 'nextFollowUpDate', ascending: true },
      { field: 'expectedAnnualVolume', ascending: false },
    ],
  },

  // === AKTIVITÄT - Verkaufsaktivität ===
  {
    id: 'last-contact-oldest',
    label: 'Längster Kontakt-Stopp',
    description: 'Kunden die am längsten nicht kontaktiert wurden',
    category: 'activity',
    icon: '🕒',
    sorts: [
      { field: 'lastContactDate', ascending: true },
      { field: 'expectedAnnualVolume', ascending: false },
    ],
  },
  {
    id: 'last-contact-recent',
    label: 'Neueste Kontakte',
    description: 'Letzte Aktivitäten zuerst',
    category: 'activity',
    icon: '📞',
    sorts: [
      { field: 'lastContactDate', ascending: false },
      { field: 'companyName', ascending: true },
    ],
  },
  {
    id: 'new-customers-first',
    label: 'Neue Kunden zuerst',
    description: 'Frisch angelegt - Momentum nutzen',
    category: 'activity',
    icon: '🌟',
    sorts: [
      { field: 'createdAt', ascending: false },
      { field: 'expectedAnnualVolume', ascending: false },
    ],
  },

  // === STATUS-BASIERT - Pipeline Management ===
  {
    id: 'hot-leads-first',
    label: 'Heiße Leads zuerst',
    description: 'Prospects mit höchstem Potenzial',
    category: 'priority',
    icon: '🔥',
    sorts: [
      { field: 'status', ascending: true }, // PROSPECT, LEAD vor AKTIV
      { field: 'expectedAnnualVolume', ascending: false },
      { field: 'lastContactDate', ascending: true },
    ],
  },
  {
    id: 'active-customers-first',
    label: 'Aktive Kunden zuerst',
    description: 'Bestehende Kunden für Upselling/Cross-selling',
    category: 'business',
    icon: '✅',
    sorts: [
      { field: 'status', ascending: false }, // AKTIV zuerst
      { field: 'expectedAnnualVolume', ascending: false },
    ],
  },

  // === STANDARD - Klassisch ===
  {
    id: 'alphabetical',
    label: 'A-Z (Alphabetisch)',
    description: 'Firmenname von A bis Z',
    category: 'custom',
    icon: '📋',
    sorts: [{ field: 'companyName', ascending: true }],
  },
  {
    id: 'customer-number',
    label: 'Kundennummer',
    description: 'Nach Kundennummer sortiert',
    category: 'custom',
    icon: '#️⃣',
    sorts: [{ field: 'customerNumber', ascending: true }],
  },
];

// Helper function to get smart sort by ID
export const getSmartSortById = (id: string): SmartSortOption | undefined => {
  return SMART_SORT_OPTIONS.find(option => option.id === id);
};

// Helper function to get smart sort options by category
export const getSmartSortsByCategory = (
  category: SmartSortOption['category']
): SmartSortOption[] => {
  return SMART_SORT_OPTIONS.filter(option => option.category === category);
};
