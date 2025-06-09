import { create } from 'zustand';
import type { CalculatorInput, CalculatorResponse } from '../api/calculatorSchemas';

const MAX_HISTORY_ENTRIES = 10;

interface CalculatorState {
  // Current calculation input
  currentInput: CalculatorInput;

  // Last calculation result
  lastResult: CalculatorResponse | null;

  // UI state
  isCalculating: boolean;
  selectedScenario: string | null;
  showAdvancedOptions: boolean;

  // Calculation history (for this session)
  calculationHistory: Array<{
    input: CalculatorInput;
    result: CalculatorResponse;
    timestamp: Date;
    scenarioName?: string;
  }>;

  // Actions
  setInput: (input: Partial<CalculatorInput>) => void;
  setResult: (result: CalculatorResponse) => void;
  setIsCalculating: (isCalculating: boolean) => void;
  setSelectedScenario: (scenario: string | null) => void;
  setShowAdvancedOptions: (show: boolean) => void;
  addToHistory: (input: CalculatorInput, result: CalculatorResponse, scenarioName?: string) => void;
  resetCalculator: () => void;
  loadScenario: (scenarioInput: CalculatorInput, scenarioName: string) => void;
}

const initialInput: CalculatorInput = {
  orderValue: 25000,
  leadTime: 14,
  pickup: false,
  chain: false,
};

export const useCalculatorStore = create<CalculatorState>(set => ({
  // Initial state
  currentInput: initialInput,
  lastResult: null,
  isCalculating: false,
  selectedScenario: null,
  showAdvancedOptions: false,
  calculationHistory: [],

  // Actions
  setInput: input =>
    set(state => ({
      currentInput: { ...state.currentInput, ...input },
      selectedScenario: null, // Clear scenario when input changes manually
    })),

  setResult: result => set({ lastResult: result }),

  setIsCalculating: isCalculating => set({ isCalculating }),

  setSelectedScenario: scenario => set({ selectedScenario: scenario }),

  setShowAdvancedOptions: show => set({ showAdvancedOptions: show }),

  addToHistory: (input, result, scenarioName) =>
    set(state => ({
      calculationHistory: [
        {
          input,
          result,
          timestamp: new Date(),
          scenarioName,
        },
        ...state.calculationHistory.slice(0, MAX_HISTORY_ENTRIES - 1),
      ],
    })),

  resetCalculator: () =>
    set({
      currentInput: initialInput,
      lastResult: null,
      selectedScenario: null,
      isCalculating: false,
    }),

  loadScenario: (scenarioInput, scenarioName) =>
    set({
      currentInput: scenarioInput,
      selectedScenario: scenarioName,
      lastResult: null, // Clear previous result when loading scenario
    }),
}));
