import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { UniversalExportButton } from './UniversalExportButton';

// Mock fetch API
global.fetch = vi.fn();

// Mock URL.createObjectURL and URL.revokeObjectURL
global.URL.createObjectURL = vi.fn(() => 'blob:mock-url');
global.URL.revokeObjectURL = vi.fn();

// Mock document.createElement for download link
const mockClick = vi.fn();
const mockRemove = vi.fn();
const originalCreateElement = document.createElement.bind(document);

describe('UniversalExportButton', () => {
  const mockOnExportComplete = vi.fn();
  const mockOnExportError = vi.fn();

  const defaultProps = {
    entity: 'customers',
    buttonLabel: 'Exportieren',
    onExportComplete: mockOnExportComplete,
    onExportError: mockOnExportError,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    
    // Mock createElement for anchor element
    document.createElement = vi.fn((tagName: string) => {
      const element = originalCreateElement(tagName);
      if (tagName === 'a') {
        element.click = mockClick;
        element.remove = mockRemove;
      }
      return element;
    });
  });

  afterEach(() => {
    document.createElement = originalCreateElement;
  });

  describe('Rendering', () => {
    it('should render export button with label', () => {
      render(<UniversalExportButton {...defaultProps} />);
      
      expect(screen.getByText('Exportieren')).toBeInTheDocument();
    });

    it('should render with custom button variant', () => {
      render(<UniversalExportButton {...defaultProps} buttonVariant="outlined" />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      expect(button).toHaveClass('MuiButton-outlined');
    });

    it('should render with custom button color', () => {
      render(<UniversalExportButton {...defaultProps} buttonColor="secondary" />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      expect(button).toHaveClass('MuiButton-colorSecondary');
    });

    it('should be disabled when disabled prop is true', () => {
      render(<UniversalExportButton {...defaultProps} disabled={true} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      expect(button).toBeDisabled();
    });
  });

  describe('Export Menu', () => {
    it('should open menu when button is clicked', async () => {
      const user = userEvent.setup();
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      expect(screen.getByRole('menu')).toBeInTheDocument();
    });

    it('should show all export format options', async () => {
      const user = userEvent.setup();
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      expect(screen.getByText('CSV Export')).toBeInTheDocument();
      expect(screen.getByText('Excel Export (.xlsx)')).toBeInTheDocument();
      expect(screen.getByText('JSON Export')).toBeInTheDocument();
      expect(screen.getByText('PDF Export')).toBeInTheDocument();
      expect(screen.getByText('HTML Export')).toBeInTheDocument();
    });

    it('should close menu when clicking outside', async () => {
      const user = userEvent.setup();
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      expect(screen.getByRole('menu')).toBeInTheDocument();
      
      // Click outside
      await user.click(document.body);
      
      await waitFor(() => {
        expect(screen.queryByRole('menu')).not.toBeInTheDocument();
      });
    });

    it('should close menu after selecting format', async () => {
      const user = userEvent.setup();
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        blob: async () => new Blob(['test'], { type: 'text/csv' }),
      });
      
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      await user.click(csvOption);
      
      await waitFor(() => {
        expect(screen.queryByRole('menu')).not.toBeInTheDocument();
      });
    });
  });

  describe('Export Functionality', () => {
    describe('CSV Export', () => {
      it('should export as CSV when CSV option is selected', async () => {
        const user = userEvent.setup();
        const mockBlob = new Blob(['test,data'], { type: 'text/csv' });
        (global.fetch as any).mockResolvedValueOnce({
          ok: true,
          blob: async () => mockBlob,
        });
        
        render(<UniversalExportButton {...defaultProps} />);
        
        const button = screen.getByRole('button', { name: /exportieren/i });
        await user.click(button);
        
        const csvOption = screen.getByText('CSV Export');
        await user.click(csvOption);
        
        await waitFor(() => {
          expect(global.fetch).toHaveBeenCalledWith('/api/v2/export/customers/csv');
          expect(mockOnExportComplete).toHaveBeenCalledWith('csv');
        });
      });

      it('should trigger download with correct filename', async () => {
        const user = userEvent.setup();
        (global.fetch as any).mockResolvedValueOnce({
          ok: true,
          blob: async () => new Blob(['test'], { type: 'text/csv' }),
        });
        
        render(<UniversalExportButton {...defaultProps} />);
        
        const button = screen.getByRole('button', { name: /exportieren/i });
        await user.click(button);
        
        const csvOption = screen.getByText('CSV Export');
        await user.click(csvOption);
        
        await waitFor(() => {
          const anchorElement = document.createElement('a');
          expect(anchorElement.download).toMatch(/customers.*\.csv$/);
        });
      });
    });

    describe('Excel Export', () => {
      it('should export as Excel when Excel option is selected', async () => {
        const user = userEvent.setup();
        const mockBlob = new Blob(['excel'], { 
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        });
        (global.fetch as any).mockResolvedValueOnce({
          ok: true,
          blob: async () => mockBlob,
        });
        
        render(<UniversalExportButton {...defaultProps} />);
        
        const button = screen.getByRole('button', { name: /exportieren/i });
        await user.click(button);
        
        const excelOption = screen.getByText('Excel Export (.xlsx)');
        await user.click(excelOption);
        
        await waitFor(() => {
          expect(global.fetch).toHaveBeenCalledWith('/api/v2/export/customers/excel');
          expect(mockOnExportComplete).toHaveBeenCalledWith('excel');
        });
      });
    });

    describe('JSON Export', () => {
      it('should export as JSON when JSON option is selected', async () => {
        const user = userEvent.setup();
        const mockBlob = new Blob(['{"test":"data"}'], { type: 'application/json' });
        (global.fetch as any).mockResolvedValueOnce({
          ok: true,
          blob: async () => mockBlob,
        });
        
        render(<UniversalExportButton {...defaultProps} />);
        
        const button = screen.getByRole('button', { name: /exportieren/i });
        await user.click(button);
        
        const jsonOption = screen.getByText('JSON Export');
        await user.click(jsonOption);
        
        await waitFor(() => {
          expect(global.fetch).toHaveBeenCalledWith('/api/v2/export/customers/json');
          expect(mockOnExportComplete).toHaveBeenCalledWith('json');
        });
      });
    });

    describe('PDF Export', () => {
      it('should export as PDF when PDF option is selected', async () => {
        const user = userEvent.setup();
        const mockBlob = new Blob(['pdf'], { type: 'application/pdf' });
        (global.fetch as any).mockResolvedValueOnce({
          ok: true,
          blob: async () => mockBlob,
        });
        
        render(<UniversalExportButton {...defaultProps} />);
        
        const button = screen.getByRole('button', { name: /exportieren/i });
        await user.click(button);
        
        const pdfOption = screen.getByText('PDF Export');
        await user.click(pdfOption);
        
        await waitFor(() => {
          expect(global.fetch).toHaveBeenCalledWith('/api/v2/export/customers/pdf');
          expect(mockOnExportComplete).toHaveBeenCalledWith('pdf');
        });
      });
    });

    describe('HTML Export', () => {
      it('should export as HTML when HTML option is selected', async () => {
        const user = userEvent.setup();
        const mockBlob = new Blob(['<html></html>'], { type: 'text/html' });
        (global.fetch as any).mockResolvedValueOnce({
          ok: true,
          blob: async () => mockBlob,
        });
        
        render(<UniversalExportButton {...defaultProps} />);
        
        const button = screen.getByRole('button', { name: /exportieren/i });
        await user.click(button);
        
        const htmlOption = screen.getByText('HTML Export');
        await user.click(htmlOption);
        
        await waitFor(() => {
          expect(global.fetch).toHaveBeenCalledWith('/api/v2/export/customers/html');
          expect(mockOnExportComplete).toHaveBeenCalledWith('html');
        });
      });
    });
  });

  describe('Loading State', () => {
    it('should show loading indicator during export', async () => {
      const user = userEvent.setup();
      let resolvePromise: (value: any) => void;
      const promise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      
      (global.fetch as any).mockReturnValueOnce(promise);
      
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      await user.click(csvOption);
      
      // Should show loading state
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
      
      // Resolve the promise
      resolvePromise!({
        ok: true,
        blob: async () => new Blob(['test'], { type: 'text/csv' }),
      });
      
      await waitFor(() => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      });
    });

    it('should disable button during export', async () => {
      const user = userEvent.setup();
      let resolvePromise: (value: any) => void;
      const promise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      
      (global.fetch as any).mockReturnValueOnce(promise);
      
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      await user.click(csvOption);
      
      // Button should be disabled
      expect(button).toBeDisabled();
      
      // Resolve the promise
      resolvePromise!({
        ok: true,
        blob: async () => new Blob(['test'], { type: 'text/csv' }),
      });
      
      await waitFor(() => {
        expect(button).not.toBeDisabled();
      });
    });
  });

  describe('Error Handling', () => {
    it('should handle network errors', async () => {
      const user = userEvent.setup();
      const consoleError = console.error;
      console.error = vi.fn();
      
      (global.fetch as any).mockRejectedValueOnce(new Error('Network error'));
      
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      await user.click(csvOption);
      
      await waitFor(() => {
        expect(mockOnExportError).toHaveBeenCalledWith(
          'csv',
          expect.any(Error)
        );
      });
      
      console.error = consoleError;
    });

    it('should handle HTTP error responses', async () => {
      const user = userEvent.setup();
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
      });
      
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      await user.click(csvOption);
      
      await waitFor(() => {
        expect(mockOnExportError).toHaveBeenCalled();
      });
    });

    it('should show error snackbar on export failure', async () => {
      const user = userEvent.setup();
      (global.fetch as any).mockRejectedValueOnce(new Error('Export failed'));
      
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      await user.click(csvOption);
      
      await waitFor(() => {
        expect(screen.getByText(/export fehlgeschlagen/i)).toBeInTheDocument();
      });
    });
  });

  describe('Custom Configuration', () => {
    it('should use custom export formats when provided', async () => {
      const user = userEvent.setup();
      const customFormats = ['csv', 'json'] as const;
      
      render(
        <UniversalExportButton 
          {...defaultProps} 
          exportFormats={customFormats}
        />
      );
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      expect(screen.getByText('CSV Export')).toBeInTheDocument();
      expect(screen.getByText('JSON Export')).toBeInTheDocument();
      expect(screen.queryByText('Excel Export')).not.toBeInTheDocument();
      expect(screen.queryByText('PDF Export')).not.toBeInTheDocument();
      expect(screen.queryByText('HTML Export')).not.toBeInTheDocument();
    });

    it('should use custom API endpoint when provided', async () => {
      const user = userEvent.setup();
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        blob: async () => new Blob(['test'], { type: 'text/csv' }),
      });
      
      render(
        <UniversalExportButton 
          {...defaultProps} 
          apiEndpoint="/custom/export"
        />
      );
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      await user.click(csvOption);
      
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith('/custom/export/customers/csv');
      });
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA attributes', () => {
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      expect(button).toHaveAttribute('aria-haspopup', 'true');
    });

    it('should be keyboard navigable', async () => {
      const user = userEvent.setup();
      render(<UniversalExportButton {...defaultProps} />);
      
      // Tab to button
      await user.tab();
      const button = screen.getByRole('button', { name: /exportieren/i });
      expect(button).toHaveFocus();
      
      // Open menu with Enter
      await user.keyboard('{Enter}');
      expect(screen.getByRole('menu')).toBeInTheDocument();
      
      // Navigate menu with arrow keys
      await user.keyboard('{ArrowDown}');
      const firstOption = screen.getByText('CSV Export');
      expect(firstOption.closest('li')).toHaveFocus();
    });

    it('should announce export completion to screen readers', async () => {
      const user = userEvent.setup();
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        blob: async () => new Blob(['test'], { type: 'text/csv' }),
      });
      
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      await user.click(csvOption);
      
      await waitFor(() => {
        const alert = screen.getByRole('alert');
        expect(alert).toHaveTextContent(/export erfolgreich/i);
      });
    });
  });

  describe('Performance', () => {
    it('should cleanup blob URLs after download', async () => {
      const user = userEvent.setup();
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        blob: async () => new Blob(['test'], { type: 'text/csv' }),
      });
      
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      await user.click(csvOption);
      
      await waitFor(() => {
        expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:mock-url');
      });
    });

    it('should not make duplicate requests during export', async () => {
      const user = userEvent.setup();
      (global.fetch as any).mockResolvedValue({
        ok: true,
        blob: async () => new Blob(['test'], { type: 'text/csv' }),
      });
      
      render(<UniversalExportButton {...defaultProps} />);
      
      const button = screen.getByRole('button', { name: /exportieren/i });
      await user.click(button);
      
      const csvOption = screen.getByText('CSV Export');
      
      // Click multiple times quickly
      await user.click(csvOption);
      await user.click(csvOption);
      
      await waitFor(() => {
        // Should only make one request
        expect(global.fetch).toHaveBeenCalledTimes(1);
      });
    });
  });
});