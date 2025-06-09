import { useMutation, useQuery } from '@tanstack/react-query';
import { calculatorApi } from './calculatorApi';
import type {
  CalculatorInput,
  CalculatorResponse,
  Scenario,
  DiscountRules,
} from './calculatorSchemas';

// Query keys für React Query
export const calculatorKeys = {
  all: ['calculator'] as const,
  scenarios: () => [...calculatorKeys.all, 'scenarios'] as const,
  scenario: (name: string) => [...calculatorKeys.scenarios(), name] as const,
  rules: () => [...calculatorKeys.all, 'rules'] as const,
  calculations: () => [...calculatorKeys.all, 'calculations'] as const,
};

// Calculate discount mutation
export function useCalculateDiscount() {
  return useMutation<CalculatorResponse, Error, CalculatorInput>({
    mutationFn: (input: CalculatorInput) => calculatorApi.calculate(input),
    // Optimistic updates könnten hier hinzugefügt werden
  });
}

// Get all scenarios query
export function useScenarios() {
  return useQuery<Scenario[], Error>({
    queryKey: calculatorKeys.scenarios(),
    queryFn: () => calculatorApi.getScenarios(),
    staleTime: 1000 * 60 * 10, // 10 minutes - scenarios change rarely
  });
}

// Get specific scenario query
export function useScenario(name: string) {
  return useQuery<Scenario, Error>({
    queryKey: calculatorKeys.scenario(name),
    queryFn: () => calculatorApi.getScenario(name),
    enabled: !!name,
    staleTime: 1000 * 60 * 10, // 10 minutes
  });
}

// Get discount rules query
export function useDiscountRules() {
  return useQuery<DiscountRules, Error>({
    queryKey: calculatorKeys.rules(),
    queryFn: () => calculatorApi.getDiscountRules(),
    staleTime: 1000 * 60 * 30, // 30 minutes - rules change very rarely
  });
}
