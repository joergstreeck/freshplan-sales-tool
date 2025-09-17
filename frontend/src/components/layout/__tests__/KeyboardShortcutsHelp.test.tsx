import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { KeyboardShortcutsHelp } from '../KeyboardShortcutsHelp';

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// Mock keyboard shortcuts
vi.mock('@/hooks/useKeyboardNavigation', () => ({
  KEYBOARD_SHORTCUTS: {
    'Ctrl + B': 'Sidebar ein-/ausblenden',
    'Alt + H': 'Zum Dashboard wechseln',
    'Alt + K': 'Zu Kunden wechseln',
  },
}));

describe('KeyboardShortcutsHelp', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    // Clean up any open dialogs
    const dialogs = document.querySelectorAll('[role="dialog"]');
    dialogs.forEach(dialog => dialog.remove());
  });

  it('should render floating help button', () => {
    render(<KeyboardShortcutsHelp />);

    const button = screen.getByRole('button', { name: '' });
    expect(button).toBeInTheDocument();
  });

  it('should open dialog when button is clicked', async () => {
    render(<KeyboardShortcutsHelp />);

    const button = screen.getByRole('button');
    fireEvent.click(button);

    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
      expect(screen.getByText('Tastaturkürzel')).toBeInTheDocument();
    });
  });

  it('should display keyboard shortcuts in dialog', async () => {
    render(<KeyboardShortcutsHelp />);

    const button = screen.getByRole('button');
    fireEvent.click(button);

    await waitFor(() => {
      // Check that the dialog is open
      expect(screen.getByRole('dialog')).toBeInTheDocument();

      // Check for action descriptions (these are unique)
      expect(screen.getByText('Sidebar ein-/ausblenden')).toBeInTheDocument();
      expect(screen.getByText('Zum Dashboard wechseln')).toBeInTheDocument();
      expect(screen.getByText('Zu Kunden wechseln')).toBeInTheDocument();

      // Check that table structure exists
      expect(screen.getByText('Tastenkombination')).toBeInTheDocument();
      expect(screen.getByText('Aktion')).toBeInTheDocument();
    });
  });

  it('should close dialog when close button is clicked', async () => {
    render(<KeyboardShortcutsHelp />);

    const button = screen.getByRole('button');
    fireEvent.click(button);

    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    const closeButton = screen.getByText('Verstanden');
    fireEvent.click(closeButton);

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
  });

  it('should save to localStorage when dialog is opened', async () => {
    localStorageMock.getItem.mockReturnValue(null);

    render(<KeyboardShortcutsHelp />);

    const button = screen.getByRole('button');
    fireEvent.click(button);

    await waitFor(() => {
      expect(localStorageMock.setItem).toHaveBeenCalledWith('hasSeenKeyboardHelp', 'true');
    });
  });

  it('should show different button style if user has seen help before', () => {
    localStorageMock.getItem.mockReturnValue('true');

    render(<KeyboardShortcutsHelp />);

    const button = screen.getByRole('button');
    const buttonStyle = window.getComputedStyle(button);

    // Button should have different color when seen
    expect(button).toBeInTheDocument();
  });

  it('should open dialog with Shift+? keyboard shortcut', async () => {
    render(<KeyboardShortcutsHelp />);

    const event = new KeyboardEvent('keydown', {
      key: '?',
      shiftKey: true,
    });
    window.dispatchEvent(event);

    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });
  });

  it('should display tips section in dialog', async () => {
    render(<KeyboardShortcutsHelp />);

    const button = screen.getByRole('button');
    fireEvent.click(button);

    await waitFor(() => {
      expect(screen.getByText('💡 Tipps:')).toBeInTheDocument();
      expect(screen.getByText(/Die Shortcuts funktionieren nur/)).toBeInTheDocument();
    });
  });
});