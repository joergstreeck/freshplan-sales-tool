import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { navigationConfig } from '@/config/navigation.config';
import { useNavigationStore } from '@/store/navigationStore';

export const useNavigationShortcuts = () => {
  const navigate = useNavigate();
  const { setActiveMenu } = useNavigationStore();

  useEffect(() => {
    const handleKeyPress = (event: KeyboardEvent) => {
      // Alt + 1-5 für Hauptnavigation
      if (event.altKey && event.key >= '1' && event.key <= '5') {
        event.preventDefault();
        const index = parseInt(event.key) - 1;
        const navItem = navigationConfig[index];
        
        if (navItem) {
          setActiveMenu(navItem.id);
          navigate(navItem.path);
        }
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [navigate, setActiveMenu]);
};