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
  toggleSidebar: () => void;
  addToRecentlyVisited: (path: string) => void;
  clearRecentlyVisited: () => void;
  toggleFavorite: (menuId: string) => void;
}

export const useNavigationStore = create<NavigationState>()(
  persist(
    (set) => ({
      activeMenuId: 'cockpit',
      expandedMenuId: null,
      isCollapsed: false,
      recentlyVisited: [],
      favorites: [],
      
      setActiveMenu: (menuId) => set({ activeMenuId: menuId }),
      
      toggleSubmenu: (menuId) => set((state) => ({
        expandedMenuId: state.expandedMenuId === menuId ? null : menuId
      })),
      
      toggleSidebar: () => set((state) => ({ 
        isCollapsed: !state.isCollapsed,
        expandedMenuId: null
      })),
      
      addToRecentlyVisited: (path) => set((state) => {
        const updated = [path, ...state.recentlyVisited.filter(p => p !== path)];
        return { recentlyVisited: updated.slice(0, 5) };
      }),
      
      clearRecentlyVisited: () => set({ recentlyVisited: [] }),
      
      toggleFavorite: (menuId) => set((state) => ({
        favorites: state.favorites.includes(menuId)
          ? state.favorites.filter(id => id !== menuId)
          : [...state.favorites, menuId]
      })),
    }),
    {
      name: 'navigation-storage',
      partialize: (state) => ({
        isCollapsed: state.isCollapsed,
        favorites: state.favorites,
        expandedMenuId: state.expandedMenuId, // Persist expanded menu state
      }),
    }
  )
);