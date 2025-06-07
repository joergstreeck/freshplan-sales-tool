// Zustand store for User UI state (not server data!)
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import type { UserFilter } from '../api/userSchemas';

interface UserUIState {
  // UI state for user management
  isCreateModalOpen: boolean;
  isEditModalOpen: boolean;
  selectedUserId: string | null;

  // Filter state
  activeFilters: UserFilter;

  // Table/List state
  sortBy: 'username' | 'email' | 'createdAt';
  sortOrder: 'asc' | 'desc';

  // Search state
  searchTerm: string;

  // Actions for modals
  openCreateModal: () => void;
  closeCreateModal: () => void;

  openEditModal: (userId: string) => void;
  closeEditModal: () => void;

  // Actions for filters
  setActiveFilters: (filters: UserFilter) => void;
  clearFilters: () => void;

  // Actions for sorting
  setSorting: (sortBy: UserUIState['sortBy'], sortOrder: UserUIState['sortOrder']) => void;

  // Actions for search
  setSearchTerm: (term: string) => void;
  clearSearch: () => void;

  // Reset all UI state
  resetUIState: () => void;
}

const initialState = {
  isCreateModalOpen: false,
  isEditModalOpen: false,
  selectedUserId: null,
  activeFilters: {},
  sortBy: 'username' as const,
  sortOrder: 'asc' as const,
  searchTerm: '',
};

export const useUserStore = create<UserUIState>()(
  devtools(
    set => ({
      ...initialState,

      // Modal actions
      openCreateModal: () => set({ isCreateModalOpen: true }),
      closeCreateModal: () => set({ isCreateModalOpen: false }),

      openEditModal: (userId: string) =>
        set({
          isEditModalOpen: true,
          selectedUserId: userId,
        }),
      closeEditModal: () =>
        set({
          isEditModalOpen: false,
          selectedUserId: null,
        }),

      // Filter actions
      setActiveFilters: (filters: UserFilter) => set({ activeFilters: filters }),
      clearFilters: () => set({ activeFilters: {} }),

      // Sorting actions
      setSorting: (sortBy, sortOrder) => set({ sortBy, sortOrder }),

      // Search actions
      setSearchTerm: (searchTerm: string) => set({ searchTerm }),
      clearSearch: () => set({ searchTerm: '' }),

      // Reset all
      resetUIState: () => set(initialState),
    }),
    {
      name: 'user-ui-store', // DevTools store name
    }
  )
);
