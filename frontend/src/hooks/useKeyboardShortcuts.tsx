import { useHotkeys } from 'react-hotkeys-hook';

interface ShortcutConfig {
  key: string;
  description: string;
  action: () => void;
  preventDefault?: boolean;
}

export function useKeyboardShortcuts() {

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

  // Register shortcuts one by one (React Hooks rules)
  // We need to call hooks at the top level, not in loops
  useHotkeys(shortcuts[0].key, e => {
    if (shortcuts[0].preventDefault !== false) e.preventDefault();
    shortcuts[0].action();
  }, { enableOnFormTags: false });

  useHotkeys(shortcuts[1].key, e => {
    if (shortcuts[1].preventDefault !== false) e.preventDefault();
    shortcuts[1].action();
  }, { enableOnFormTags: false });

  useHotkeys(shortcuts[2].key, e => {
    if (shortcuts[2].preventDefault !== false) e.preventDefault();
    shortcuts[2].action();
  }, { enableOnFormTags: false });

  useHotkeys(shortcuts[3].key, e => {
    if (shortcuts[3].preventDefault !== false) e.preventDefault();
    shortcuts[3].action();
  }, { enableOnFormTags: false });

  return { shortcuts };
}

// Global Provider Component
export function KeyboardShortcutsProvider({ children }: { children: React.ReactNode }) {
  useKeyboardShortcuts();
  return <>{children}</>;
}
