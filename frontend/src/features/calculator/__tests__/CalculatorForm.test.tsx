import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CalculatorForm } from '../components/CalculatorForm';
import { calculatorApi } from '../api/calculatorApi';
import type { CalculatorResponse } from '../api/calculatorSchemas';

// Mock the calculator API
vi.mock('../api/calculatorApi', () => ({
  calculatorApi: {
    calculate: vi.fn(),
  },
}));

// Mock the calculator store
vi.mock('../store/calculatorStore', () => ({
  useCalculatorStore: vi.fn(() => ({
    currentInput: {
      orderValue: 25000,
      leadTime: 14,
      pickup: false,
      chain: false,
    },
    selectedScenario: null,
    isCalculating: false,
    setInput: vi.fn(),
    setResult: vi.fn(),
    setIsCalculating: vi.fn(),
    addToHistory: vi.fn(),
  })),
}));

function renderWithQueryClient(component: React.ReactNode) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(<QueryClientProvider client={queryClient}>{component}</QueryClientProvider>);
}

describe('CalculatorForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render form fields correctly', () => {
    renderWithQueryClient(<CalculatorForm />);

    expect(screen.getByLabelText(/bestellwert/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/vorlaufzeit/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/selbstabholung/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/kettenkunde/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /rabatt berechnen/i })).toBeInTheDocument();
  });

  it('should display default values from store', () => {
    renderWithQueryClient(<CalculatorForm />);

    const orderValueInput = screen.getByLabelText(/bestellwert/i) as HTMLInputElement;
    const leadTimeInput = screen.getByLabelText(/vorlaufzeit/i) as HTMLInputElement;

    expect(orderValueInput.value).toBe('25000');
    expect(leadTimeInput.value).toBe('14');
  });

  it('should call calculate API when form is submitted', async () => {
    const mockResponse: CalculatorResponse = {
      orderValue: 25000,
      leadTime: 14,
      pickup: false,
      chain: false,
      baseDiscount: 8,
      earlyDiscount: 2,
      pickupDiscount: 0,
      chainDiscount: 0,
      totalDiscount: 10,
      discountAmount: 2500,
      savingsAmount: 2500,
      finalPrice: 22500,
    };

    vi.mocked(calculatorApi.calculate).mockResolvedValueOnce(mockResponse);

    renderWithQueryClient(<CalculatorForm />);

    const calculateButton = screen.getByRole('button', { name: /rabatt berechnen/i });
    await userEvent.click(calculateButton);

    await waitFor(() => {
      expect(calculatorApi.calculate).toHaveBeenCalledWith({
        orderValue: 25000,
        leadTime: 14,
        pickup: false,
        chain: false,
      });
    });
  });

  it('should accept valid order value range', async () => {
    renderWithQueryClient(<CalculatorForm />);

    const orderValueInput = screen.getByLabelText(/bestellwert/i);

    // Test valid value
    await userEvent.clear(orderValueInput);
    await userEvent.type(orderValueInput, '50000');

    expect(orderValueInput).toHaveValue(50000);
  });

  it('should accept valid lead time range', async () => {
    renderWithQueryClient(<CalculatorForm />);

    const leadTimeInput = screen.getByLabelText(/vorlaufzeit/i);

    // Test valid value
    await userEvent.clear(leadTimeInput);
    await userEvent.type(leadTimeInput, '30');

    expect(leadTimeInput).toHaveValue(30);
  });

  it('should toggle switches correctly', async () => {
    renderWithQueryClient(<CalculatorForm />);

    const pickupSwitch = screen.getByLabelText(/selbstabholung/i);
    const chainSwitch = screen.getByLabelText(/kettenkunde/i);

    expect(pickupSwitch).not.toBeChecked();
    expect(chainSwitch).not.toBeChecked();

    await userEvent.click(pickupSwitch);
    await userEvent.click(chainSwitch);

    expect(pickupSwitch).toBeChecked();
    expect(chainSwitch).toBeChecked();
  });

  it('should handle API errors gracefully', async () => {
    vi.mocked(calculatorApi.calculate).mockRejectedValueOnce(new Error('API Error'));

    renderWithQueryClient(<CalculatorForm />);

    const calculateButton = screen.getByRole('button', { name: /rabatt berechnen/i });
    await userEvent.click(calculateButton);

    await waitFor(() => {
      // Check that error message is displayed in the UI
      expect(screen.getByText(/fehler bei der berechnung/i)).toBeInTheDocument();
      expect(screen.getByText(/api error/i)).toBeInTheDocument();
    });
  });

  it('should disable button during calculation', async () => {
    // Mock a delayed response
    vi.mocked(calculatorApi.calculate).mockImplementationOnce(
      () => new Promise(resolve => setTimeout(() => resolve({} as CalculatorResponse), 100))
    );

    renderWithQueryClient(<CalculatorForm />);

    const calculateButton = screen.getByRole('button', { name: /rabatt berechnen/i });
    await userEvent.click(calculateButton);

    // Button should be disabled and show loading text
    expect(calculateButton).toBeDisabled();
    expect(screen.getByText(/berechnung läuft/i)).toBeInTheDocument();
  });
});
