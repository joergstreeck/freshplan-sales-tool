/**
 * Zustand Store für das Sales Cockpit UI State Management
 * 
 * Verwaltet den globalen UI-Zustand für das 3-Spalten-Layout:
 * - Spalte 1: Mein Tag (Übersicht & Prioritäten)
 * - Spalte 2: Fokus-Liste (Dynamischer Arbeitsvorrat) 
 * - Spalte 3: Aktions-Center (Kontextbezogene Arbeit)
 */

import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';

export type ViewMode = 'list' | 'kanban' | 'cards';
export type CockpitColumn = 'my-day' | 'focus-list' | 'action-center';

interface SelectedCustomer {
  id: string;
  companyName: string;
  status: string;
}

interface CockpitState {
  // Layout State
  activeColumn: CockpitColumn;
  isMobileMenuOpen: boolean;
  isCompactMode: boolean;
  
  // Spalte 1: Mein Tag
  showTriageInbox: boolean;
  priorityTasksCount: number;
  
  // Spalte 2: Fokus-Liste
  viewMode: ViewMode;
  selectedCustomerId: string | null;
  selectedCustomer: SelectedCustomer | null;
  filterTags: string[];
  searchQuery: string;
  
  // Spalte 3: Aktions-Center
  activeProcess: string | null;
  isDirty: boolean;
  
  // Actions
  setActiveColumn: (column: CockpitColumn) => void;
  toggleMobileMenu: () => void;
  toggleCompactMode: () => void;
  
  // Mein Tag Actions
  toggleTriageInbox: () => void;
  setPriorityTasksCount: (count: number) => void;
  
  // Fokus-Liste Actions
  setViewMode: (mode: ViewMode) => void;
  selectCustomer: (customer: SelectedCustomer | null) => void;
  addFilterTag: (tag: string) => void;
  removeFilterTag: (tag: string) => void;
  clearFilterTags: () => void;
  setSearchQuery: (query: string) => void;
  
  // Aktions-Center Actions
  setActiveProcess: (process: string | null) => void;
  setDirty: (dirty: boolean) => void;
  
  // Global Actions
  resetUI: () => void;
}

export const useCockpitStore = create<CockpitState>()(
  devtools(
    persist(
      (set) => ({
        // Initial State
        activeColumn: 'focus-list',
        isMobileMenuOpen: false,
        isCompactMode: false,
        
        // Mein Tag
        showTriageInbox: true,
        priorityTasksCount: 0,
        
        // Fokus-Liste
        viewMode: 'list',
        selectedCustomerId: null,
        selectedCustomer: null,
        filterTags: [],
        searchQuery: '',
        
        // Aktions-Center
        activeProcess: null,
        isDirty: false,
        
        // Layout Actions
        setActiveColumn: (column) => set({ activeColumn: column }),
        toggleMobileMenu: () => set((state) => ({ 
          isMobileMenuOpen: !state.isMobileMenuOpen 
        })),
        toggleCompactMode: () => set((state) => ({ 
          isCompactMode: !state.isCompactMode 
        })),
        
        // Mein Tag Actions
        toggleTriageInbox: () => set((state) => ({ 
          showTriageInbox: !state.showTriageInbox 
        })),
        setPriorityTasksCount: (count) => set({ 
          priorityTasksCount: count 
        }),
        
        // Fokus-Liste Actions
        setViewMode: (mode) => set({ viewMode: mode }),
        selectCustomer: (customer) => set({ 
          selectedCustomer: customer,
          selectedCustomerId: customer?.id || null,
          activeColumn: customer ? 'action-center' : 'focus-list'
        }),
        addFilterTag: (tag) => set((state) => ({
          filterTags: [...new Set([...state.filterTags, tag])]
        })),
        removeFilterTag: (tag) => set((state) => ({
          filterTags: state.filterTags.filter(t => t !== tag)
        })),
        clearFilterTags: () => set({ filterTags: [] }),
        setSearchQuery: (query) => set({ searchQuery: query }),
        
        // Aktions-Center Actions
        setActiveProcess: (process) => set({ 
          activeProcess: process,
          isDirty: false 
        }),
        setDirty: (dirty) => set({ isDirty: dirty }),
        
        // Global Actions
        resetUI: () => set({
          activeColumn: 'focus-list',
          isMobileMenuOpen: false,
          selectedCustomerId: null,
          selectedCustomer: null,
          filterTags: [],
          searchQuery: '',
          activeProcess: null,
          isDirty: false
        })
      }),
      {
        name: 'freshplan-cockpit-storage',
        // Nur UI-relevante States persistieren
        partialize: (state) => ({
          viewMode: state.viewMode,
          isCompactMode: state.isCompactMode,
          showTriageInbox: state.showTriageInbox
        })
      }
    )
  )
);