import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { SettingsPage } from './SettingsPage';
import { useUserStore } from '../features/users/userStore';
import { useUser } from '../features/users/userQueries';
import { freshfoodzTheme } from '../theme/freshfoodz';

// Mock the modules
vi.mock('../features/users/userStore');
vi.mock('../features/users/userQueries');
vi.mock('../features/users/components/UserTableMUI', () => ({
  UserTableMUI: () => <div data-testid="user-table">User Table</div>
}));
vi.mock('../features/users/components/UserFormMUI', () => ({
  UserFormMUI: ({ onSuccess, onCancel }: any) => (
    <div data-testid="user-form">
      <button onClick={onSuccess}>Save</button>
      <button onClick={onCancel}>Cancel</button>
    </div>
  )
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ThemeProvider theme={freshfoodzTheme}>
          {children}
        </ThemeProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('SettingsPage', () => {
  const mockUserStore = {
    isCreateModalOpen: false,
    isEditModalOpen: false,
    selectedUserId: null,
    closeCreateModal: vi.fn(),
    closeEditModal: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (useUserStore as any).mockReturnValue(mockUserStore);
    (useUser as any).mockReturnValue({ data: null });
  });

  it('sollte die Seite mit Titel rendern', () => {
    render(<SettingsPage />, { wrapper: createWrapper() });
    
    // Hole den h4 Titel, nicht den Sidebar-Link
    const title = screen.getByRole('heading', { name: 'Einstellungen', level: 4 });
    expect(title).toBeInTheDocument();
  });

  it('sollte alle drei Tabs anzeigen', () => {
    render(<SettingsPage />, { wrapper: createWrapper() });
    
    expect(screen.getByText('Benutzerverwaltung')).toBeInTheDocument();
    expect(screen.getByText('Systemeinstellungen')).toBeInTheDocument();
    expect(screen.getByText('Sicherheit')).toBeInTheDocument();
  });

  it('sollte standardmäßig den Benutzerverwaltungs-Tab anzeigen', () => {
    render(<SettingsPage />, { wrapper: createWrapper() });
    
    expect(screen.getByTestId('user-table')).toBeInTheDocument();
  });

  it('sollte zwischen Tabs wechseln können', () => {
    render(<SettingsPage />, { wrapper: createWrapper() });
    
    // Zu Systemeinstellungen wechseln
    fireEvent.click(screen.getByText('Systemeinstellungen'));
    
    expect(screen.queryByTestId('user-table')).not.toBeInTheDocument();
    expect(screen.getByText('Hier können später allgemeine Systemeinstellungen konfiguriert werden.')).toBeInTheDocument();
  });

  it('sollte das Create-Formular anzeigen wenn isCreateModalOpen true ist', () => {
    (useUserStore as any).mockReturnValue({
      ...mockUserStore,
      isCreateModalOpen: true,
    });

    render(<SettingsPage />, { wrapper: createWrapper() });
    
    expect(screen.getByTestId('user-form')).toBeInTheDocument();
    expect(screen.queryByTestId('user-table')).not.toBeInTheDocument();
  });

  it('sollte das Edit-Formular anzeigen wenn isEditModalOpen true ist', () => {
    const mockUser = { id: '1', username: 'testuser' };
    (useUserStore as any).mockReturnValue({
      ...mockUserStore,
      isEditModalOpen: true,
      selectedUserId: '1',
    });
    (useUser as any).mockReturnValue({ data: mockUser });

    render(<SettingsPage />, { wrapper: createWrapper() });
    
    expect(screen.getByTestId('user-form')).toBeInTheDocument();
  });

  it('sollte Loading State anzeigen beim Editieren ohne geladene Daten', () => {
    (useUserStore as any).mockReturnValue({
      ...mockUserStore,
      isEditModalOpen: true,
      selectedUserId: '1',
    });
    (useUser as any).mockReturnValue({ data: null });

    render(<SettingsPage />, { wrapper: createWrapper() });
    
    expect(screen.getByText('Lade Benutzerdaten...')).toBeInTheDocument();
  });

  it('sollte Modals schließen bei erfolgreichem Formular-Submit', async () => {
    (useUserStore as any).mockReturnValue({
      ...mockUserStore,
      isCreateModalOpen: true,
    });

    render(<SettingsPage />, { wrapper: createWrapper() });
    
    fireEvent.click(screen.getByText('Save'));
    
    await waitFor(() => {
      expect(mockUserStore.closeCreateModal).toHaveBeenCalled();
    });
  });

  it('sollte responsive sein und Icons in Tabs anzeigen', () => {
    render(<SettingsPage />, { wrapper: createWrapper() });
    
    const tabs = screen.getAllByRole('tab');
    expect(tabs).toHaveLength(3);
    
    // Tabs sollten Icons haben
    tabs.forEach(tab => {
      expect(tab.querySelector('svg')).toBeInTheDocument();
    });
  });

  it('sollte ARIA-Labels korrekt setzen', () => {
    render(<SettingsPage />, { wrapper: createWrapper() });
    
    expect(screen.getByRole('tablist')).toHaveAttribute('aria-label', 'Einstellungen Tabs');
    
    const firstTab = screen.getByRole('tab', { name: /Benutzerverwaltung/i });
    expect(firstTab).toHaveAttribute('aria-controls', 'settings-tabpanel-0');
  });
});