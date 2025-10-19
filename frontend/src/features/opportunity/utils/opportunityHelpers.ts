/**
 * Shared Opportunity Helper Functions
 *
 * Centralized utilities for Opportunity label mapping, icons, and colors.
 * (Copilot Code Review - DRY principle)
 *
 * @author FreshPlan Team
 * @since 2.0.0 (Sprint 2.1.7.1)
 */

import { OpportunityType, OpportunityStage } from '../types/opportunity.types';

/**
 * OpportunityType Label Mapping (German)
 */
export function getOpportunityTypeLabel(type?: OpportunityType): string {
  if (!type) return '';
  const labels: Record<OpportunityType, string> = {
    NEUGESCHAEFT: 'Neugeschäft',
    SORTIMENTSERWEITERUNG: 'Sortimentserweiterung',
    NEUER_STANDORT: 'Neuer Standort',
    VERLAENGERUNG: 'Vertragsverlängerung',
  };
  return labels[type] || type;
}

/**
 * OpportunityType Icon Mapping (Emojis)
 */
export function getOpportunityTypeIcon(type?: OpportunityType): string {
  if (!type) return '📊';
  const icons: Record<OpportunityType, string> = {
    NEUGESCHAEFT: '🆕',
    SORTIMENTSERWEITERUNG: '📈',
    NEUER_STANDORT: '📍',
    VERLAENGERUNG: '🔁',
  };
  return icons[type] || '📊';
}

/**
 * OpportunityType Color Mapping (FreshFoodz CI)
 */
export function getOpportunityTypeColor(type?: OpportunityType): string {
  if (!type) return 'primary.main'; // Primary Green
  const colors: Record<OpportunityType, string> = {
    NEUGESCHAEFT: 'primary.main', // Primary Green
    SORTIMENTSERWEITERUNG: 'secondary.main', // Secondary Blue
    NEUER_STANDORT: 'warning.main', // Orange
    VERLAENGERUNG: 'info.main', // Light Blue
  };
  return colors[type] || 'primary.main';
}

/**
 * OpportunityStage Label Mapping (German)
 */
export function getStageLabel(stage: OpportunityStage): string {
  const labels: Record<OpportunityStage, string> = {
    NEW_LEAD: 'Neuer Lead',
    QUALIFICATION: 'Qualifizierung',
    NEEDS_ANALYSIS: 'Bedarfsanalyse',
    PROPOSAL: 'Angebot',
    NEGOTIATION: 'Verhandlung',
    CLOSED_WON: 'Gewonnen',
    CLOSED_LOST: 'Verloren',
  };
  return labels[stage] || stage;
}

/**
 * OpportunityStage Color Mapping (FreshFoodz CI)
 */
export function getStageColor(stage: OpportunityStage): string {
  const colors: Record<OpportunityStage, string> = {
    NEW_LEAD: 'grey.300', // Light Gray
    QUALIFICATION: 'warning.main', // Orange
    NEEDS_ANALYSIS: 'info.main', // Blue
    PROPOSAL: 'warning.light', // Amber
    NEGOTIATION: 'secondary.light', // Purple
    CLOSED_WON: 'primary.main', // Primary Green
    CLOSED_LOST: 'error.main', // Red
  };
  return colors[stage] || 'grey.300';
}
