import { useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNavigationStore } from '@/store/navigationStore';
import { navigationConfig } from '@/config/navigation.config';

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

      const activeItem = navigationConfig.find(item => item.id === activeMenuId);
      const currentIndex = navigationConfig.findIndex(item => item.id === activeMenuId);

      switch (event.key) {
        // Toggle sidebar with Ctrl/Cmd + B
        case 'b':
        case 'B':
          if (event.ctrlKey || event.metaKey) {
            event.preventDefault();
            toggleSidebar();
          }
          break;

        // Navigate up
        case 'ArrowUp':
          event.preventDefault();
          if (currentIndex > 0) {
            const prevItem = navigationConfig[currentIndex - 1];
            setActiveMenu(prevItem.id);
            navigate(prevItem.path);
          }
          break;

        // Navigate down
        case 'ArrowDown':
          event.preventDefault();
          if (currentIndex < navigationConfig.length - 1) {
            const nextItem = navigationConfig[currentIndex + 1];
            setActiveMenu(nextItem.id);
            navigate(nextItem.path);
          }
          break;

        // Expand/collapse submenu or navigate
        case 'ArrowRight':
          event.preventDefault();
          if (activeItem?.subItems && activeItem.subItems.length > 0) {
            if (expandedMenuId !== activeMenuId) {
              openSubmenu(activeMenuId);
            }
          }
          break;

        // Collapse submenu
        case 'ArrowLeft':
          event.preventDefault();
          if (expandedMenuId === activeMenuId) {
            closeAllSubmenus();
          }
          break;

        // Enter to navigate to active item
        case 'Enter':
          event.preventDefault();
          if (activeItem) {
            if (activeItem.hasOwnPage || !activeItem.subItems) {
              navigate(activeItem.path);
            } else if (activeItem.subItems) {
              toggleSubmenu(activeItem.id);
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
        case '9':
          if (event.ctrlKey || event.metaKey) {
            event.preventDefault();
            const index = parseInt(event.key) - 1;
            if (index < navigationConfig.length) {
              const item = navigationConfig[index];
              setActiveMenu(item.id);
              navigate(item.path);
            }
          }
          break;

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
    [
      activeMenuId,
      expandedMenuId,
      navigate,
      setActiveMenu,
      toggleSubmenu,
      openSubmenu,
      closeAllSubmenus,
      toggleSidebar,
    ]
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
  'Ctrl/Cmd + B': 'Sidebar ein-/ausklappen',
  '↑/↓': 'Navigation nach oben/unten',
  '←/→': 'Submenü schließen/öffnen',
  'Enter': 'Zum aktiven Menüpunkt navigieren',
  'Escape': 'Alle Submenüs schließen',
  'Ctrl/Cmd + 1-9': 'Schnelle Navigation zu Menüpunkt',
  '/': 'Suche fokussieren',
  'Ctrl/Cmd + Home': 'Zum Cockpit',
};