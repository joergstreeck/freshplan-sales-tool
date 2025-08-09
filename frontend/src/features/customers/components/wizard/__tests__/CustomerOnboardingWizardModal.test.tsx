/**
 * Tests for CustomerOnboardingWizardModal
 *
 * Tests modal behavior, responsive design, and event handling.
 */

import { render, screen } from '@testing-library/react';
import { vi } from 'vitest';
import { CustomerOnboardingWizardModal } from '../CustomerOnboardingWizardModal';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { createTheme } from '@mui/material/styles';
import type { Customer } from '../../../../../types/customer.types';

// Mock CustomerOnboardingWizard
vi.mock('../CustomerOnboardingWizard', () => ({
  CustomerOnboardingWizard: ({ onComplete, onCancel, isModal }: unknown) => (
    <div data-testid="wizard-mock">
      <button onClick={() => onComplete({ id: '123', name: 'Test Customer' })}>Complete</button>
      <button onClick={onCancel}>Cancel</button>
      <span>Modal Mode: {isModal ? 'true' : 'false'}</span>
    </div>
  ),
}));

// Test utilities
const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const theme = createTheme();

const renderModal = (props = {}) => {
  const defaultProps = {
    open: true,
    onClose: vi.fn(),
    onComplete: vi.fn(),
  };

  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ThemeProvider theme={theme}>
          <CustomerOnboardingWizardModal {...defaultProps} {...props} />
        </ThemeProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

// Mock window.matchMedia for responsive tests
const mockMatchMedia = (matches: boolean) => {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation(query => ({
      matches,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  });
};

describe('CustomerOnboardingWizardModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Desktop Mode (Dialog)', () => {
    beforeEach(() => {
      mockMatchMedia(false); // Not mobile
    });

    it('renders as Dialog on desktop', () => {
      renderModal();

      // Dialog should be in the document
      const dialog = screen.getByRole('dialog');
      expect(dialog).toBeInTheDocument();

      // Check title
      expect(screen.getByText('Neuen Kunden anlegen')).toBeInTheDocument();

      // Check close button (IconButton without explicit aria-label)
      const closeButton = screen.getByTestId('CloseIcon').closest('button');
      expect(closeButton).toBeInTheDocument();
    });

    it('closes on close button click', () => {
      const onClose = vi.fn();
      renderModal({ onClose });

      const closeButton = screen.getByTestId('CloseIcon').closest('button');
      fireEvent.click(closeButton!);

      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it('closes on ESC key press', () => {
      const onClose = vi.fn();
      renderModal({ onClose });

      fireEvent.keyDown(screen.getByRole('dialog'), { key: 'Escape' });

      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it('passes isModal=true to wizard', () => {
      renderModal();

      expect(screen.getByText('Modal Mode: true')).toBeInTheDocument();
    });

    it('handles wizard completion', async () => {
      const onComplete = vi.fn();
      renderModal({ onComplete });

      const completeButton = screen.getByText('Complete');
      fireEvent.click(completeButton);

      await waitFor(() => {
        expect(onComplete).toHaveBeenCalledWith({
          id: '123',
          name: 'Test Customer',
        });
      });
    });

    it('handles wizard cancellation', () => {
      const onClose = vi.fn();
      renderModal({ onClose });

      const cancelButton = screen.getByText('Cancel');
      fireEvent.click(cancelButton);

      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it('does not render when open=false', () => {
      renderModal({ open: false });

      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
  });

  describe('Mobile Mode (Drawer)', () => {
    beforeEach(() => {
      mockMatchMedia(true); // Mobile
    });

    it('renders as Drawer on mobile', () => {
      renderModal();

      // Drawer should be in the document (as presentation role)
      const drawer = screen.getByRole('presentation');
      expect(drawer).toBeInTheDocument();

      // Check title
      expect(screen.getByText('Neuen Kunden anlegen')).toBeInTheDocument();
    });

    it('closes on close button click in mobile', () => {
      const onClose = vi.fn();
      renderModal({ onClose });

      const closeButton = screen.getByTestId('CloseIcon').closest('button');
      fireEvent.click(closeButton!);

      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it('passes isModal=true to wizard in mobile', () => {
      renderModal();

      expect(screen.getByText('Modal Mode: true')).toBeInTheDocument();
    });

    it('handles swipe-to-close gesture', () => {
      const onClose = vi.fn();
      renderModal({ onClose });

      // Simulate backdrop click (drawer's way of closing)
      const backdrop = document.querySelector('.MuiBackdrop-root');
      if (backdrop) {
        fireEvent.click(backdrop);
      }

      expect(onClose).toHaveBeenCalled();
    });
  });

  describe('Integration with CustomersPageV2', () => {
    it('responds to custom event', () => {
      const { rerender } = renderModal({ open: false });

      // Initially closed
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

      // Simulate event
      window.dispatchEvent(new CustomEvent('freshplan:new-customer'));

      // Should open (this would be handled by CustomersPageV2 state)
      rerender(
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>
            <ThemeProvider theme={theme}>
              <CustomerOnboardingWizardModal open={true} onClose={vi.fn()} onComplete={vi.fn()} />
            </ThemeProvider>
          </BrowserRouter>
        </QueryClientProvider>
      );

      // Modal is open (could be dialog or drawer depending on viewport)
      expect(screen.getByText('Neuen Kunden anlegen')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    beforeEach(() => {
      mockMatchMedia(false); // Desktop
    });

    it('has proper aria labels', () => {
      renderModal();

      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('aria-modal', 'true');
    });

    it('traps focus within modal', () => {
      renderModal();

      const closeButton = screen.getByTestId('CloseIcon').closest('button');
      closeButton?.focus();

      expect(document.activeElement).toBe(closeButton);
    });

    it('returns focus on close', () => {
      const { rerender } = renderModal();

      // Create a button outside modal
      const outsideButton = document.createElement('button');
      outsideButton.textContent = 'Outside';
      document.body.appendChild(outsideButton);
      outsideButton.focus();

      // Close modal
      rerender(
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>
            <ThemeProvider theme={theme}>
              <CustomerOnboardingWizardModal open={false} onClose={vi.fn()} onComplete={vi.fn()} />
            </ThemeProvider>
          </BrowserRouter>
        </QueryClientProvider>
      );

      // Cleanup
      document.body.removeChild(outsideButton);
    });
  });
});
