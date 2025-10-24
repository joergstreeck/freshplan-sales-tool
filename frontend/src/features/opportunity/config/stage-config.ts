/**
 * Opportunity Stage Configuration
 *
 * @module StageConfiguration
 * @description Zentrale Konfiguration für Opportunity Stages mit Enterprise-Features
 *              wie Performance-Optimierung, Type Safety und i18n-Support.
 * @since 2.0.0
 */

import { OpportunityStage, type IStageConfig } from '../types';

/**
 * Stage configuration with enterprise features
 * @const STAGE_CONFIGURATIONS
 */
export const STAGE_CONFIGURATIONS: ReadonlyArray<IStageConfig> = [
  {
    stage: OpportunityStage.NEW_LEAD,
    label: 'Lead',
    color: 'info.main',
    bgColor: 'background.paper',
    description: 'Neuer, unqualifizierter Lead',
    allowedNextStages: [OpportunityStage.QUALIFICATION, OpportunityStage.CLOSED_LOST],
    defaultProbability: 10,
    icon: 'person_add',
    sortOrder: 1,
    isActive: true,
  },
  {
    stage: OpportunityStage.QUALIFICATION,
    label: 'Qualifizierung',
    color: 'success.dark',
    bgColor: 'background.paper',
    description: 'Lead wird qualifiziert',
    allowedNextStages: [OpportunityStage.NEEDS_ANALYSIS, OpportunityStage.CLOSED_LOST],
    defaultProbability: 20,
    icon: 'fact_check',
    sortOrder: 2,
    isActive: true,
  },
  {
    stage: OpportunityStage.NEEDS_ANALYSIS,
    label: 'Bedarfsanalyse',
    color: 'success.main',
    bgColor: 'success.lighter',
    description: 'Bedarfsanalyse läuft',
    allowedNextStages: [OpportunityStage.PROPOSAL, OpportunityStage.CLOSED_LOST],
    defaultProbability: 40,
    icon: 'analytics',
    sortOrder: 3,
    isActive: true,
  },
  {
    stage: OpportunityStage.PROPOSAL,
    label: 'Angebot',
    color: 'warning.dark',
    bgColor: 'warning.lighter',
    description: 'Angebot wurde erstellt',
    allowedNextStages: [OpportunityStage.NEGOTIATION, OpportunityStage.CLOSED_LOST],
    defaultProbability: 60,
    icon: 'description',
    sortOrder: 4,
    isActive: true,
  },
  {
    stage: OpportunityStage.NEGOTIATION,
    label: 'Verhandlung',
    color: 'secondary.dark',
    bgColor: 'secondary.lighter',
    description: 'In Verhandlung',
    allowedNextStages: [OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST],
    defaultProbability: 80,
    icon: 'handshake',
    sortOrder: 5,
    isActive: true,
  },
  {
    stage: OpportunityStage.CLOSED_WON,
    label: 'Gewonnen',
    color: 'success.dark',
    bgColor: 'success.light',
    description: 'Erfolgreich abgeschlossen',
    allowedNextStages: [OpportunityStage.RENEWAL],
    defaultProbability: 100,
    icon: 'emoji_events',
    sortOrder: 6,
    isActive: false,
  },
  {
    stage: OpportunityStage.CLOSED_LOST,
    label: 'Verloren',
    color: 'error.dark',
    bgColor: 'error.light',
    description: 'Verloren',
    allowedNextStages: [],
    defaultProbability: 0,
    icon: 'cancel',
    sortOrder: 8,
    isActive: false,
  },
  {
    stage: OpportunityStage.RENEWAL,
    label: 'Verlängerung',
    color: 'warning.main',
    bgColor: 'warning.lighter',
    description: 'Vertragsverlängerung in Verhandlung',
    allowedNextStages: [OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST],
    defaultProbability: 75,
    icon: 'autorenew',
    sortOrder: 7,
    isActive: true,
  },
];

/**
 * Singleton Map instance for O(1) lookups
 */
let stageConfigMap: Map<OpportunityStage, IStageConfig> | null = null;

/**
 * Get stage configuration map (lazy initialization)
 * @returns {Map<OpportunityStage, IStageConfig>} Stage configuration map
 */
function getStageConfigMap(): Map<OpportunityStage, IStageConfig> {
  if (!stageConfigMap) {
    stageConfigMap = new Map(
      STAGE_CONFIGURATIONS.map(config => [config.stage, Object.freeze(config)])
    );
  }
  return stageConfigMap;
}

/**
 * Get stage configuration by stage enum
 * @param {OpportunityStage} stage - The stage to get config for
 * @returns {IStageConfig | undefined} Stage configuration or undefined
 */
export function getStageConfig(stage: OpportunityStage): IStageConfig | undefined {
  return getStageConfigMap().get(stage);
}

/**
 * Get stage configuration with fallback
 * @param {OpportunityStage} stage - The stage to get config for
 * @param {IStageConfig} fallback - Fallback configuration
 * @returns {IStageConfig} Stage configuration or fallback
 */
export function getStageConfigSafe(
  stage: OpportunityStage,
  fallback?: Partial<IStageConfig>
): IStageConfig {
  const config = getStageConfig(stage);
  if (config) {
    return config;
  }

  // Return fallback or default config
  return {
    stage,
    label: fallback?.label || 'Unknown',
    color: fallback?.color || 'grey.600',
    bgColor: fallback?.bgColor || '#F5F5F5',
    description: fallback?.description || 'Unknown stage',
    allowedNextStages: fallback?.allowedNextStages || [],
    defaultProbability: fallback?.defaultProbability || 0,
    icon: fallback?.icon || 'help',
    sortOrder: fallback?.sortOrder || 999,
    isActive: fallback?.isActive ?? false,
  };
}

/**
 * Get active stages for pipeline display
 * @returns {ReadonlyArray<IStageConfig>} Active stage configurations
 */
export function getActiveStages(): ReadonlyArray<IStageConfig> {
  return STAGE_CONFIGURATIONS.filter(config => config.isActive);
}

/**
 * Get closed stages
 * @returns {ReadonlyArray<IStageConfig>} Closed stage configurations
 */
export function getClosedStages(): ReadonlyArray<IStageConfig> {
  return STAGE_CONFIGURATIONS.filter(config => !config.isActive);
}

/**
 * Check if stage transition is allowed
 * @param {OpportunityStage} fromStage - Current stage
 * @param {OpportunityStage} toStage - Target stage
 * @returns {boolean} Whether transition is allowed
 */
export function isStageTransitionAllowed(
  fromStage: OpportunityStage,
  toStage: OpportunityStage
): boolean {
  const config = getStageConfig(fromStage);
  if (!config) {
    return false;
  }

  // Same stage is always allowed (no-op)
  if (fromStage === toStage) {
    return true;
  }

  return config.allowedNextStages.includes(toStage);
}

/**
 * Get stage by label (case-insensitive)
 * @param {string} label - Stage label
 * @returns {OpportunityStage | undefined} Stage enum or undefined
 */
export function getStageByLabel(label: string): OpportunityStage | undefined {
  const normalizedLabel = label.toLowerCase().trim();
  const config = STAGE_CONFIGURATIONS.find(c => c.label.toLowerCase() === normalizedLabel);
  return config?.stage;
}

/**
 * Stage validation result
 * @interface IStageValidationResult
 */
export interface IStageValidationResult {
  isValid: boolean;
  errors: string[];
}

/**
 * Validate stage value
 * @param {unknown} stage - Value to validate
 * @returns {IStageValidationResult} Validation result
 */
export function validateStage(stage: unknown): IStageValidationResult {
  const errors: string[] = [];

  if (!stage) {
    errors.push('Stage is required');
  } else if (typeof stage !== 'string') {
    errors.push('Stage must be a string');
  } else if (!Object.values(OpportunityStage).includes(stage as OpportunityStage)) {
    errors.push(`Invalid stage: ${stage}`);
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Get stage color for charts and visualizations
 * @param {OpportunityStage} stage - Stage enum
 * @returns {string} Hex color code
 */
export function getStageColor(stage: OpportunityStage): string {
  return getStageConfig(stage)?.color || 'grey.600';
}

/**
 * Get stage background color
 * @param {OpportunityStage} stage - Stage enum
 * @returns {string} Hex color code
 */
export function getStageBackgroundColor(stage: OpportunityStage): string {
  return getStageConfig(stage)?.bgColor || '#F5F5F5';
}

// Type exports for convenience
export type { IStageConfig };
export { OpportunityStage };
