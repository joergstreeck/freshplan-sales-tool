// API Types for Cockpit (strict, no any)
export type Channel = 'DIRECT' | 'PARTNER';

export interface SummaryResponse {
  range: '7d' | '30d' | '90d';
  sampleSuccessRatePct: number;
  roiPipelineValue: number;
  partnerSharePct: number;
  channelMix: { DIRECT: number; PARTNER: number };
  atRiskCustomers: number;
  updatedAt: string;
}

export interface FiltersResponse {
  territories: string[];
  channels: Channel[];
}

export interface ROICalcRequest {
  mealsPerDay: number;
  daysPerMonth: number;
  laborMinutesSavedPerMeal: number;
  laborCostPerHour: number;
  foodCostPerMeal: number;
  wasteReductionPct: number;
  channel?: Channel;
  investment?: number;
}

export interface ROICalcResponse {
  monthlyLaborSavings: number;
  monthlyWasteSavings: number;
  totalMonthlySavings: number;
  paybackMonths?: number | null;
  assumptions: { partnerDiscountPct?: number; directUpliftPct?: number };
}
