import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';

// Type für Filter-Werte
export type FilterValue = string | number | boolean | Date | string[] | number[] | [number, number] | null;

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
}

// Default Tabellen-Spalten
const DEFAULT_TABLE_COLUMNS: TableColumn[] = [
  { id: 'companyName', label: 'Kunde', field: 'companyName', visible: true, order: 0 },
  { id: 'customerNumber', label: 'Kundennummer', field: 'customerNumber', visible: false, order: 1 },
  { id: 'status', label: 'Status', field: 'status', visible: true, order: 2 },
  { id: 'riskScore', label: 'Risiko', field: 'riskScore', visible: true, order: 3, align: 'center' },
  { id: 'industry', label: 'Branche', field: 'industry', visible: true, order: 4 },
  { id: 'expectedAnnualVolume', label: 'Jahresumsatz', field: 'expectedAnnualVolume', visible: false, order: 5, align: 'right' },
  { id: 'lastContactDate', label: 'Letzter Kontakt', field: 'lastContactDate', visible: false, order: 6 },
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
        tableColumns: [...DEFAULT_TABLE_COLUMNS],
        selectedCustomerId: null,
        page: 0,
        pageSize: 20,

        // Derived State
        get filterCount() {
          return get().activeFilters.length;
        },
        
        get hasActiveFilters() {
          return get().globalSearch !== '' || get().activeFilters.length > 0;
        },
        
        get visibleTableColumns() {
          return get().tableColumns
            .filter(col => col.visible)
            .sort((a, b) => a.order - b.order);
        },

        // Search & Filter Actions
        setGlobalSearch: (search) => 
          set({ globalSearch: search, page: 0 }),

        addFilter: (filter) =>
          set((state) => ({
            activeFilters: [
              ...state.activeFilters,
              { ...filter, id: crypto.randomUUID() },
            ],
            page: 0,
          })),

        removeFilter: (filterId) =>
          set((state) => ({
            activeFilters: state.activeFilters.filter((f) => f.id !== filterId),
            page: 0,
          })),

        updateFilter: (filterId, updates) =>
          set((state) => ({
            activeFilters: state.activeFilters.map((f) =>
              f.id === filterId ? { ...f, ...updates } : f
            ),
            page: 0,
          })),

        clearAllFilters: () =>
          set({ globalSearch: '', activeFilters: [], page: 0 }),

        toggleQuickFilter: (field, value) => {
          const state = get();
          
          // Prüfe ZUERST ob der Filter bereits aktiv ist
          const isActive = state.activeFilters.length === 1 && 
            ((field === 'riskScore' && value === '>70' && 
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
                activeFilters: [{
                  id: crypto.randomUUID(),
                  field: 'riskScore',
                  operator: FilterOperator.GREATER_THAN,
                  value: 70,
                }],
                page: 0
              });
            } else {
              set({
                activeFilters: [{
                  id: crypto.randomUUID(),
                  field,
                  operator: FilterOperator.EQUALS,
                  value,
                }],
                page: 0
              });
            }
          }
        },

        hasFilter: (field, value) => {
          const filters = get().activeFilters;
          if (field === 'riskScore' && value === '>70') {
            return filters.some(
              (f) => f.field === 'riskScore' && 
                     f.operator === FilterOperator.GREATER_THAN && 
                     f.value === 70
            );
          }
          return filters.some((f) => f.field === field && f.value === value);
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

          set((state) => ({
            savedViews: [...state.savedViews, newView],
            currentViewId: newView.id,
          }));
        },

        loadSavedView: (viewId) => {
          const view = get().savedViews.find((v) => v.id === viewId);
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

        deleteSavedView: (viewId) =>
          set((state) => ({
            savedViews: state.savedViews.filter((v) => v.id !== viewId),
            currentViewId: state.currentViewId === viewId ? null : state.currentViewId,
          })),

        updateCurrentView: () => {
          const state = get();
          if (!state.currentViewId) return;

          set((state) => ({
            savedViews: state.savedViews.map((v) =>
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
        setViewMode: (mode) => set({ viewMode: mode }),
        
        setSortBy: (sort) => set({ sortBy: sort, page: 0 }),
        
        setPage: (page) => set({ page }),
        
        setPageSize: (pageSize) => set({ pageSize, page: 0 }),
        
        // Table Column Actions
        toggleColumnVisibility: (columnId) => 
          set((state) => ({
            tableColumns: state.tableColumns.map(col =>
              col.id === columnId ? { ...col, visible: !col.visible } : col
            )
          })),
          
        setColumnOrder: (columnIds) => 
          set((state) => ({
            tableColumns: state.tableColumns.map(col => ({
              ...col,
              order: columnIds.indexOf(col.id)
            }))
          })),
          
        resetTableColumns: () => 
          set({ tableColumns: [...DEFAULT_TABLE_COLUMNS] }),
        
        // Selection Actions
        setSelectedCustomer: (customerId) => set({ selectedCustomerId: customerId }),

        // API Request Builder
        getSearchRequest: () => {
          const state = get();
          const request: CustomerSearchRequest = {};

          if (state.globalSearch) {
            request.globalSearch = state.globalSearch;
          }

          if (state.activeFilters.length > 0) {
            request.filters = state.activeFilters.map((f) => ({
              field: f.field,
              operator: f.operator,
              value: f.value,
              combineWith: f.combineWith || 'AND',
            }));
          }

          request.sort = {
            field: state.sortBy.field,
            ascending: state.sortBy.ascending,
          };

          return request;
        },
      }),
      {
        name: 'focus-list-store',
        partialize: (state) => ({
          savedViews: state.savedViews,
          viewMode: state.viewMode,
          pageSize: state.pageSize,
          tableColumns: state.tableColumns,
        }),
      }
    )
  )
);