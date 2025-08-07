import { useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useHotkeys } from 'react-hotkeys-hook';

interface ShortcutConfig {
  key: string;
  description: string;
  action: () => void;
  preventDefault?: boolean;
}

export function useKeyboardShortcuts() {
  const navigate = useNavigate();

  const shortcuts: ShortcutConfig[] = [
    {
      key: 'ctrl+n, cmd+n',
      description: 'Neuen Kunden anlegen',
      action: () => {
        // Trigger global event
        window.dispatchEvent(new CustomEvent('freshplan:new-customer'));
      },
    },
    {
      key: 'ctrl+t, cmd+t',
      description: 'Neue Aufgabe',
      action: () => {
        window.dispatchEvent(new CustomEvent('freshplan:new-task'));
      },
    },
    {
      key: 'ctrl+k, cmd+k',
      description: 'Command Palette öffnen',
      action: () => {
        window.dispatchEvent(new CustomEvent('freshplan:command-palette'));
      },
    },
    {
      key: 'ctrl+/, cmd+/',
      description: 'Hilfe anzeigen',
      action: () => {
        window.dispatchEvent(new CustomEvent('freshplan:show-help'));
      },
    },
  ];

  // Register all shortcuts
  shortcuts.forEach(({ key, action, preventDefault = true }) => {
    useHotkeys(
      key,
      e => {
        if (preventDefault) e.preventDefault();
        action();
      },
      { enableOnFormTags: false }
    );
  });

  return { shortcuts };
}

// Global Provider Component
export function KeyboardShortcutsProvider({ children }: { children: React.ReactNode }) {
  useKeyboardShortcuts();
  return <>{children}</>;
}
