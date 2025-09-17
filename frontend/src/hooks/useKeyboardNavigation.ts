import { useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNavigationStore } from '@/store/navigationStore';
import { navigationConfig } from '@/config/navigation.config';

/**
 * Hook for keyboard navigation in the application.
 * Provides keyboard shortcuts for common navigation actions.
 *
 * Performance optimized with:
 * - Stable event handler using refs to avoid re-renders
 * - Minimal dependencies for useCallback
 *
 * @returns Object containing active menu ID and collapsed state
 */
export const useKeyboardNavigation = () => {
  const navigate = useNavigate();
  const {
    activeMenuId,
    expandedMenuId,
    setActiveMenu,
    toggleSubmenu,
    openSubmenu,
    closeAllSubmenus,
    toggleSidebar,
    isCollapsed,
  } = useNavigationStore();

  // Stable refs to avoid re-creating handleKeyPress
  const navigationStoreRef = useRef({
    activeMenuId,
    expandedMenuId,
    setActiveMenu,
    toggleSubmenu,
    openSubmenu,
    closeAllSubmenus,
    toggleSidebar,
  });

  // Update ref when store values change
  useEffect(() => {
    navigationStoreRef.current = {
      activeMenuId,
      expandedMenuId,
      setActiveMenu,
      toggleSubmenu,
      openSubmenu,
      closeAllSubmenus,
      toggleSidebar,
    };
  }, [activeMenuId, expandedMenuId, setActiveMenu, toggleSubmenu, openSubmenu, closeAllSubmenus, toggleSidebar]);

  const handleKeyPress = useCallback(
    (event: KeyboardEvent) => {
      // Only handle if no input is focused
      const target = event.target as HTMLElement;
      if (
        target.tagName === 'INPUT' ||
        target.tagName === 'TEXTAREA' ||
        target.contentEditable === 'true'
      ) {
        return;
      }

      const store = navigationStoreRef.current;
      const activeItem = navigationConfig.find(item => item.id === store.activeMenuId);
      const currentIndex = navigationConfig.findIndex(item => item.id === store.activeMenuId);

      switch (event.key) {
        // Toggle sidebar with Ctrl/Cmd + B
        case 'b':
        case 'B':
          if (event.ctrlKey || event.metaKey) {
            event.preventDefault();
            store.toggleSidebar();
          } else if (event.altKey) {
            // Alt + B for Orders
            event.preventDefault();
            navigate('/orders');
          }
          break;

        // Alt + H for Dashboard/Cockpit
        case 'h':
        case 'H':
          if (event.altKey) {
            event.preventDefault();
            navigate('/cockpit');
          }
          break;

        // Alt + K for Customers
        case 'k':
        case 'K':
          if (event.altKey) {
            event.preventDefault();
            navigate('/customers');
          }
          break;

        // Alt + R for Calculator
        case 'r':
        case 'R':
          if (event.altKey) {
            event.preventDefault();
            navigate('/calculator');
          }
          break;

        // Navigate up
        case 'ArrowUp':
          event.preventDefault();
          if (currentIndex > 0) {
            const prevItem = navigationConfig[currentIndex - 1];
            store.setActiveMenu(prevItem.id);
            navigate(prevItem.path);
          }
          break;

        // Navigate down
        case 'ArrowDown':
          event.preventDefault();
          if (currentIndex < navigationConfig.length - 1) {
            const nextItem = navigationConfig[currentIndex + 1];
            store.setActiveMenu(nextItem.id);
            navigate(nextItem.path);
          }
          break;

        // Expand/collapse submenu or navigate
        case 'ArrowRight':
          event.preventDefault();
          if (activeItem?.subItems && activeItem.subItems.length > 0) {
            if (expandedMenuId !== activeMenuId) {
              store.openSubmenu(store.activeMenuId);
            }
          }
          break;

        // Collapse submenu
        case 'ArrowLeft':
          event.preventDefault();
          if (expandedMenuId === activeMenuId) {
            store.closeAllSubmenus();
          }
          break;

        // Enter to navigate to active item
        case 'Enter':
          event.preventDefault();
          if (activeItem) {
            if (activeItem.hasOwnPage || !activeItem.subItems) {
              navigate(activeItem.path);
            } else if (activeItem.subItems) {
              store.toggleSubmenu(activeItem.id);
            }
          }
          break;

        // Escape to close all submenus
        case 'Escape':
          event.preventDefault();
          closeAllSubmenus();
          break;

        // Quick navigation with number keys (1-9)
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9': {
          if (event.ctrlKey || event.metaKey) {
            event.preventDefault();
            const index = parseInt(event.key) - 1;
            if (index < navigationConfig.length) {
              const item = navigationConfig[index];
              store.setActiveMenu(item.id);
              navigate(item.path);
            }
          }
          break;
        }

        // Search with /
        case '/':
          event.preventDefault();
          // Focus search input if it exists
          const searchInput = document.querySelector(
            'input[placeholder*="Suche"]'
          ) as HTMLInputElement;
          if (searchInput) {
            searchInput.focus();
          }
          break;

        // Home key to go to cockpit
        case 'Home':
          if (event.ctrlKey || event.metaKey) {
            event.preventDefault();
            navigate('/cockpit');
          }
          break;
      }
    },
    [navigate] // Only depend on navigate, store values are in ref
  );

  useEffect(() => {
    window.addEventListener('keydown', handleKeyPress);
    return () => {
      window.removeEventListener('keydown', handleKeyPress);
    };
  }, [handleKeyPress]);

  return {
    activeMenuId,
    isCollapsed,
  };
};

// Export keyboard shortcuts for documentation
export const KEYBOARD_SHORTCUTS = {
  'Ctrl + B': 'Sidebar ein-/ausblenden',
  'Alt + H': 'Zum Dashboard wechseln',
  'Alt + K': 'Zu Kunden wechseln',
  'Alt + B': 'Zu Bestellungen wechseln',
  'Alt + R': 'Zum Rechner wechseln',
  '↑/↓': 'Navigation nach oben/unten',
  '←/→': 'Submenü schließen/öffnen',
  'Enter': 'Zum aktiven Menüpunkt navigieren',
  'Escape': 'Alle Submenüs schließen',
  'Ctrl + 1-9': 'Schnelle Navigation zu Menüpunkt',
  '/': 'Suche fokussieren',
  'Ctrl + Home': 'Zum Cockpit',
};