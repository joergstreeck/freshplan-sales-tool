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
  if (!type) return '#94C456'; // Primary Green
  const colors: Record<OpportunityType, string> = {
    NEUGESCHAEFT: '#94C456', // Primary Green
    SORTIMENTSERWEITERUNG: '#004F7B', // Secondary Blue
    NEUER_STANDORT: '#FF9800', // Orange
    VERLAENGERUNG: '#2196F3', // Light Blue
  };
  return colors[type] || '#94C456';
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
    NEW_LEAD: '#E0E0E0', // Light Gray
    QUALIFICATION: '#FF9800', // Orange
    NEEDS_ANALYSIS: '#2196F3', // Blue
    PROPOSAL: '#FFC107', // Amber
    NEGOTIATION: '#9C27B0', // Purple
    CLOSED_WON: '#94C456', // Primary Green
    CLOSED_LOST: '#F44336', // Red
  };
  return colors[stage] || '#E0E0E0';
}
