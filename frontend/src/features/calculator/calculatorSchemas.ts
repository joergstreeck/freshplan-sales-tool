import { z } from 'zod';

// Single Source of Truth für Calculator Domain - basierend auf Legacy-Analyse

export const CalculatorInputSchema = z.object({
  orderValue: z
    .number()
    .min(0, 'Bestellwert muss mindestens 0€ sein')
    .max(1000000, 'Bestellwert darf maximal 1.000.000€ sein'),
  leadTime: z
    .number()
    .int('Vorlaufzeit muss eine ganze Zahl sein')
    .min(0, 'Vorlaufzeit muss mindestens 0 Tage sein')
    .max(365, 'Vorlaufzeit darf maximal 365 Tage sein'),
  pickup: z.boolean(),
  chain: z.boolean(),
});

export const CalculatorResponseSchema = z.object({
  // Input parameters (echo back)
  orderValue: z.number(),
  leadTime: z.number(),
  pickup: z.boolean(),
  chain: z.boolean(),

  // Calculated discounts
  baseDiscount: z.number(),
  earlyDiscount: z.number(),
  pickupDiscount: z.number(),
  chainDiscount: z.number(),
  totalDiscount: z.number(),

  // Calculated amounts
  discountAmount: z.number(),
  savingsAmount: z.number(),
  finalPrice: z.number(),
});

export const ScenarioSchema = z.object({
  name: z.string(),
  description: z.string(),
  orderValue: z.number(),
  leadTime: z.number(),
  pickup: z.boolean(),
  chain: z.boolean(),
});

export const DiscountRulesSchema = z.object({
  baseRules: z.array(
    z.object({
      min: z.number(),
      discount: z.number(),
    })
  ),
  earlyBookingRules: z.array(
    z.object({
      days: z.number(),
      discount: z.number(),
    })
  ),
  pickupDiscount: z.number(),
  maxTotalDiscount: z.number(),
});

// TypeScript types exported from schemas
export type CalculatorInput = z.infer<typeof CalculatorInputSchema>;
export type CalculatorResponse = z.infer<typeof CalculatorResponseSchema>;
export type Scenario = z.infer<typeof ScenarioSchema>;
export type DiscountRules = z.infer<typeof DiscountRulesSchema>;

// Predefined scenarios from Legacy-Analyse
export const PREDEFINED_SCENARIOS: Record<string, CalculatorInput> = {
  spontan: {
    orderValue: 8000,
    leadTime: 5,
    pickup: false,
    chain: false,
  },
  geplant: {
    orderValue: 32000,
    leadTime: 16,
    pickup: false,
    chain: false,
  },
  optimal: {
    orderValue: 50000,
    leadTime: 30,
    pickup: true,
    chain: false,
  },
  hotel: {
    orderValue: 25000,
    leadTime: 14,
    pickup: false,
    chain: false,
  },
  klinik: {
    orderValue: 65000,
    leadTime: 30,
    pickup: false,
    chain: true,
  },
  restaurant: {
    orderValue: 8500,
    leadTime: 7,
    pickup: true,
    chain: false,
  },
};

// Scenario descriptions für UI
export const SCENARIO_DESCRIPTIONS: Record<string, string> = {
  spontan: 'Kurzfristige Bestellung mit geringem Volumen',
  geplant: 'Mittelfristig geplante Standardbestellung',
  optimal: 'Perfekt geplante Bestellung mit Abholung',
  hotel: 'Typische Hotelbestellung mit mittlerem Volumen',
  klinik: 'Großvolumen-Bestellung für Klinikstandorte',
  restaurant: 'Restaurantbestellung mit Selbstabholung',
};
