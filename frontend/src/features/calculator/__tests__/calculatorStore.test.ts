import { describe, it, expect } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useCalculatorStore } from '../store/calculatorStore';
import type { CalculatorInput, CalculatorResponse } from '../api/calculatorSchemas';

describe('Calculator Store', () => {
  beforeEach(() => {
    // Reset store state before each test
    const { result } = renderHook(() => useCalculatorStore());
    act(() => {
      result.current.resetCalculator();
    });
  });

  it('should have correct initial state', () => {
    const { result } = renderHook(() => useCalculatorStore());

    expect(result.current.currentInput).toEqual({
      orderValue: 25000,
      leadTime: 14,
      pickup: false,
      chain: false,
    });
    expect(result.current.lastResult).toBeNull();
    expect(result.current.isCalculating).toBe(false);
    expect(result.current.selectedScenario).toBeNull();
    expect(result.current.showAdvancedOptions).toBe(false);
    expect(result.current.calculationHistory).toEqual([]);
  });

  it('should update input correctly', () => {
    const { result } = renderHook(() => useCalculatorStore());

    act(() => {
      result.current.setInput({ orderValue: 50000 });
    });

    expect(result.current.currentInput.orderValue).toBe(50000);
    expect(result.current.currentInput.leadTime).toBe(14); // Should keep other values
    expect(result.current.selectedScenario).toBeNull(); // Should clear scenario
  });

  it('should update result correctly', () => {
    const { result } = renderHook(() => useCalculatorStore());

    const mockResult: CalculatorResponse = {
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

    act(() => {
      result.current.setResult(mockResult);
    });

    expect(result.current.lastResult).toEqual(mockResult);
  });

  it('should manage calculating state', () => {
    const { result } = renderHook(() => useCalculatorStore());

    act(() => {
      result.current.setIsCalculating(true);
    });

    expect(result.current.isCalculating).toBe(true);

    act(() => {
      result.current.setIsCalculating(false);
    });

    expect(result.current.isCalculating).toBe(false);
  });

  it('should load scenario correctly', () => {
    const { result } = renderHook(() => useCalculatorStore());

    const scenarioInput: CalculatorInput = {
      orderValue: 50000,
      leadTime: 30,
      pickup: true,
      chain: false,
    };

    act(() => {
      result.current.loadScenario(scenarioInput, 'optimal');
    });

    expect(result.current.currentInput).toEqual(scenarioInput);
    expect(result.current.selectedScenario).toBe('optimal');
    expect(result.current.lastResult).toBeNull(); // Should clear previous result
  });

  it('should add to calculation history', () => {
    const { result } = renderHook(() => useCalculatorStore());

    const input: CalculatorInput = {
      orderValue: 25000,
      leadTime: 14,
      pickup: false,
      chain: false,
    };

    const calculationResult: CalculatorResponse = {
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

    act(() => {
      result.current.addToHistory(input, calculationResult, 'geplant');
    });

    expect(result.current.calculationHistory).toHaveLength(1);
    expect(result.current.calculationHistory[0].input).toEqual(input);
    expect(result.current.calculationHistory[0].result).toEqual(calculationResult);
    expect(result.current.calculationHistory[0].scenarioName).toBe('geplant');
    expect(result.current.calculationHistory[0].timestamp).toBeInstanceOf(Date);
  });

  it('should limit calculation history to 10 entries', () => {
    const { result } = renderHook(() => useCalculatorStore());

    const input: CalculatorInput = {
      orderValue: 25000,
      leadTime: 14,
      pickup: false,
      chain: false,
    };

    const calculationResult: CalculatorResponse = {
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

    // Add 12 calculations
    for (let i = 0; i < 12; i++) {
      act(() => {
        result.current.addToHistory(
          { ...input, orderValue: 25000 + i * 1000 },
          { ...calculationResult, orderValue: 25000 + i * 1000 },
          `test-${i}`
        );
      });
    }

    // Should keep only last 10
    expect(result.current.calculationHistory).toHaveLength(10);

    // Most recent should be first
    expect(result.current.calculationHistory[0].input.orderValue).toBe(36000); // 25000 + 11*1000
    expect(result.current.calculationHistory[9].input.orderValue).toBe(27000); // 25000 + 2*1000
  });

  it('should reset calculator correctly', () => {
    const { result } = renderHook(() => useCalculatorStore());

    // Set up some state
    act(() => {
      result.current.setInput({ orderValue: 50000 });
      result.current.setSelectedScenario('optimal');
      result.current.setIsCalculating(true);
      result.current.setResult({
        orderValue: 50000,
        leadTime: 30,
        pickup: true,
        chain: false,
        baseDiscount: 9,
        earlyDiscount: 3,
        pickupDiscount: 2,
        chainDiscount: 0,
        totalDiscount: 14,
        discountAmount: 7000,
        savingsAmount: 7000,
        finalPrice: 43000,
      });
    });

    // Reset
    act(() => {
      result.current.resetCalculator();
    });

    // Should return to initial state
    expect(result.current.currentInput).toEqual({
      orderValue: 25000,
      leadTime: 14,
      pickup: false,
      chain: false,
    });
    expect(result.current.lastResult).toBeNull();
    expect(result.current.selectedScenario).toBeNull();
    expect(result.current.isCalculating).toBe(false);
  });

  it('should toggle advanced options', () => {
    const { result } = renderHook(() => useCalculatorStore());

    expect(result.current.showAdvancedOptions).toBe(false);

    act(() => {
      result.current.setShowAdvancedOptions(true);
    });

    expect(result.current.showAdvancedOptions).toBe(true);

    act(() => {
      result.current.setShowAdvancedOptions(false);
    });

    expect(result.current.showAdvancedOptions).toBe(false);
  });

  it('should clear scenario when input changes manually', () => {
    const { result } = renderHook(() => useCalculatorStore());

    // Load a scenario first
    act(() => {
      result.current.loadScenario(
        {
          orderValue: 50000,
          leadTime: 30,
          pickup: true,
          chain: false,
        },
        'optimal'
      );
    });

    expect(result.current.selectedScenario).toBe('optimal');

    // Manually change input
    act(() => {
      result.current.setInput({ orderValue: 30000 });
    });

    // Should clear selected scenario
    expect(result.current.selectedScenario).toBeNull();
  });
});
