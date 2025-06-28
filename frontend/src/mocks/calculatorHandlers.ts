import { http, HttpResponse } from 'msw';
import type {
  CalculatorInput,
  CalculatorResponse,
  Scenario,
  DiscountRules,
} from '@/features/calculator/calculatorSchemas';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Mock calculation logic matching backend
function calculateDiscount(input: CalculatorInput): CalculatorResponse {
  // Base discount calculation
  let baseDiscount = 0;
  if (input.orderValue >= 75000) baseDiscount = 10;
  else if (input.orderValue >= 50000) baseDiscount = 9;
  else if (input.orderValue >= 30000) baseDiscount = 8;
  else if (input.orderValue >= 15000) baseDiscount = 6;
  else if (input.orderValue >= 5000) baseDiscount = 3;

  // Early booking discount
  let earlyDiscount = 0;
  if (input.leadTime >= 30) earlyDiscount = 3;
  else if (input.leadTime >= 15) earlyDiscount = 2;
  else if (input.leadTime >= 10) earlyDiscount = 1;

  // Pickup discount
  const pickupDiscount = input.pickup && input.orderValue >= 5000 ? 2 : 0;

  // Chain discount (always 0)
  const chainDiscount = 0;

  // Total discount (max 15%)
  const totalDiscount = Math.min(baseDiscount + earlyDiscount + pickupDiscount + chainDiscount, 15);

  // Calculate amounts
  const discountAmount = Math.round((input.orderValue * totalDiscount) / 100);
  const finalPrice = input.orderValue - discountAmount;

  return {
    // Echo input
    orderValue: input.orderValue,
    leadTime: input.leadTime,
    pickup: input.pickup,
    chain: input.chain,
    // Discounts
    baseDiscount,
    earlyDiscount,
    pickupDiscount,
    chainDiscount,
    totalDiscount,
    // Amounts
    discountAmount,
    savingsAmount: discountAmount,
    finalPrice,
  };
}

const mockScenarios: Scenario[] = [
  {
    name: 'spontan',
    description: 'Kurzfristige Bestellung mit geringem Volumen',
    orderValue: 8000,
    leadTime: 5,
    pickup: false,
    chain: false,
  },
  {
    name: 'geplant',
    description: 'Mittelfristig geplante Standardbestellung',
    orderValue: 32000,
    leadTime: 16,
    pickup: false,
    chain: false,
  },
  {
    name: 'optimal',
    description: 'Perfekt geplante Bestellung mit Abholung',
    orderValue: 50000,
    leadTime: 30,
    pickup: true,
    chain: false,
  },
  {
    name: 'hotel',
    description: 'Typische Hotelbestellung mit mittlerem Volumen',
    orderValue: 25000,
    leadTime: 14,
    pickup: false,
    chain: false,
  },
  {
    name: 'klinik',
    description: 'Großvolumen-Bestellung für Klinikstandorte',
    orderValue: 65000,
    leadTime: 30,
    pickup: false,
    chain: true,
  },
  {
    name: 'restaurant',
    description: 'Restaurantbestellung mit Selbstabholung',
    orderValue: 8500,
    leadTime: 7,
    pickup: true,
    chain: false,
  },
];

const mockRules: DiscountRules = {
  baseRules: [
    { min: 75000, discount: 10 },
    { min: 50000, discount: 9 },
    { min: 30000, discount: 8 },
    { min: 15000, discount: 6 },
    { min: 5000, discount: 3 },
    { min: 0, discount: 0 },
  ],
  earlyBookingRules: [
    { days: 30, discount: 3 },
    { days: 15, discount: 2 },
    { days: 10, discount: 1 },
    { days: 0, discount: 0 },
  ],
  pickupDiscount: 2,
  maxTotalDiscount: 15,
};

export const calculatorHandlers = [
  // Calculate discount
  http.post(`${API_URL}/api/calculator/calculate`, async ({ request }) => {
    const input = (await request.json()) as CalculatorInput;

    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 500));

    // Validate input
    if (input.orderValue < 0 || input.orderValue > 1000000) {
      return HttpResponse.json(
        { error: 'Bestellwert muss zwischen 0 und 1.000.000 EUR liegen' },
        { status: 400 }
      );
    }

    if (input.leadTime < 0 || input.leadTime > 365) {
      return HttpResponse.json(
        { error: 'Vorlaufzeit muss zwischen 0 und 365 Tagen liegen' },
        { status: 400 }
      );
    }

    const response = calculateDiscount(input);
    return HttpResponse.json(response);
  }),

  // Get all scenarios
  http.get(`${API_URL}/api/calculator/scenarios`, () => {
    return HttpResponse.json(mockScenarios);
  }),

  // Get specific scenario
  http.get(`${API_URL}/api/calculator/scenarios/:name`, ({ params }) => {
    const scenario = mockScenarios.find(s => s.name === params.name);

    if (!scenario) {
      return HttpResponse.json({ error: `Scenario ${params.name} not found` }, { status: 404 });
    }

    return HttpResponse.json(scenario);
  }),

  // Get discount rules
  http.get(`${API_URL}/api/calculator/rules`, () => {
    return HttpResponse.json(mockRules);
  }),
];
