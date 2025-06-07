import type {
  CalculatorInput,
  CalculatorResponse,
  Scenario,
  DiscountRules,
} from './calculatorSchemas';
import {
  CalculatorInputSchema,
  CalculatorResponseSchema,
  ScenarioSchema,
  DiscountRulesSchema,
} from './calculatorSchemas';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

class CalculatorApi {
  private getAuthToken(): string | null {
    // Get token from localStorage or auth context
    return localStorage.getItem('auth_token');
  }

  private async request<T>(endpoint: string, options?: RequestInit): Promise<T> {
    const token = this.getAuthToken();

    const response = await fetch(`${API_URL}/api/calculator${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options?.headers,
      },
      ...options,
    });

    if (!response.ok) {
      let errorMessage = `Calculator API Error: ${response.status}`;
      try {
        const errorBody = await response.json();
        errorMessage = errorBody.message || errorBody.error || errorMessage;
      } catch {
        // Keep original message if JSON parsing fails
        errorMessage = `${errorMessage} ${response.statusText}`;
      }
      throw new ApiError(errorMessage, response.status);
    }

    return response.json();
  }

  async calculate(input: CalculatorInput): Promise<CalculatorResponse> {
    // Validate input before sending
    const validatedInput = CalculatorInputSchema.parse(input);

    const response = await this.request<CalculatorResponse>('/calculate', {
      method: 'POST',
      body: JSON.stringify(validatedInput),
    });

    // Validate response from backend
    return CalculatorResponseSchema.parse(response);
  }

  async getScenarios(): Promise<Scenario[]> {
    const response = await this.request<Scenario[]>('/scenarios');

    // Validate each scenario
    return response.map(scenario => ScenarioSchema.parse(scenario));
  }

  async getScenario(name: string): Promise<Scenario> {
    const response = await this.request<Scenario>(`/scenarios/${encodeURIComponent(name)}`);

    return ScenarioSchema.parse(response);
  }

  async getDiscountRules(): Promise<DiscountRules> {
    const response = await this.request<DiscountRules>('/rules');

    return DiscountRulesSchema.parse(response);
  }
}

export const calculatorApi = new CalculatorApi();
