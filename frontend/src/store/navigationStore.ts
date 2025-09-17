import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface NavigationState {
  // Zustand
  activeMenuId: string | null;
  expandedMenuId: string | null;
  isCollapsed: boolean;
  recentlyVisited: string[];
  favorites: string[];

  // Actions
  setActiveMenu: (menuId: string) => void;
  toggleSubmenu: (menuId: string) => void;
  openSubmenu: (menuId: string) => void;
  closeAllSubmenus: () => void;
  toggleSidebar: () => void;
  addToRecentlyVisited: (path: string) => void;
  clearRecentlyVisited: () => void;
  toggleFavorite: (menuId: string) => void;
}

export const useNavigationStore = create<NavigationState>()(
  persist(
    set => ({
      activeMenuId: 'cockpit',
      expandedMenuId: null,
      isCollapsed: false,
      recentlyVisited: [],
      favorites: [],

      setActiveMenu: menuId => set({ activeMenuId: menuId }),

      toggleSubmenu: menuId =>
        set(state => ({
          // Simple accordion behavior: close if same menu, otherwise open new one
          expandedMenuId: state.expandedMenuId === menuId ? null : menuId,
        })),

      openSubmenu: menuId => set({ expandedMenuId: menuId }),

      closeAllSubmenus: () => set({ expandedMenuId: null }),

      toggleSidebar: () =>
        set(state => ({
          isCollapsed: !state.isCollapsed,
          expandedMenuId: null,
        })),

      addToRecentlyVisited: path =>
        set(state => {
          const updated = [path, ...state.recentlyVisited.filter(p => p !== path)];
          return { recentlyVisited: updated.slice(0, 5) };
        }),

      clearRecentlyVisited: () => set({ recentlyVisited: [] }),

      toggleFavorite: menuId =>
        set(state => ({
          favorites: state.favorites.includes(menuId)
            ? state.favorites.filter(id => id !== menuId)
            : [...state.favorites, menuId],
        })),
    }),
    {
      name: 'navigation-storage-v2', // Version bump to clear old state
      partialize: state => ({
        isCollapsed: state.isCollapsed,
        favorites: state.favorites,
        // NEVER persist expandedMenuId - always start fresh
      }),
    }
  )
);
