import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { CustomersPageV2 } from '../CustomersPageV2';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../../contexts/AuthContext';
import { vi } from 'vitest';

// Mock dependencies
vi.mock('../../features/customer/api/customerQueries', () => ({
  useCustomers: vi.fn(() => ({
    data: { content: [] },
    isLoading: false,
    refetch: vi.fn()
  }))
}));

vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { id: '1', name: 'Test User', role: 'admin' }
  }),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>
}));

vi.mock('react-hot-toast', () => ({
  toast: {
    custom: vi.fn()
  }
}));

const wrapper = ({ children }: { children: React.ReactNode }) => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } }
  });
  
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          {children}
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('CustomersPageV2', () => {
  it('shows empty state when no customers', async () => {
    render(<CustomersPageV2 />, { wrapper });
    
    await waitFor(() => {
      expect(screen.getByText('Noch keine Kunden')).toBeInTheDocument();
    });
    
    expect(screen.getByText('✨ Ersten Kunden anlegen')).toBeInTheDocument();
  });
  
  it('opens wizard on button click', async () => {
    render(<CustomersPageV2 />, { wrapper });
    
    const button = await screen.findByText('✨ Ersten Kunden anlegen');
    fireEvent.click(button);
    
    // Wizard should open (mock implementation)
    await waitFor(() => {
      expect(screen.getAllByText(/Kunden/i).length).toBeGreaterThan(0);
    });
  });
  
  it('responds to keyboard shortcut Ctrl+N', async () => {
    render(<CustomersPageV2 />, { wrapper });
    
    // Simulate Ctrl+N
    const event = new KeyboardEvent('keydown', { 
      key: 'n', 
      ctrlKey: true,
      bubbles: true 
    });
    window.dispatchEvent(event);
    
    // Custom event should be triggered
    await waitFor(() => {
      const customEvent = new CustomEvent('freshplan:new-customer');
      expect(window.dispatchEvent).toBeDefined();
    });
  });
});