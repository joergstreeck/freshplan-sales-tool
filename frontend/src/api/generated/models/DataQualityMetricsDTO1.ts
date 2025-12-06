/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LocalDateTime } from './LocalDateTime';
export type DataQualityMetricsDTO1 = {
  totalContacts?: number;
  contactsWithInteractions?: number;
  contactsWithoutInteractions?: number;
  totalInteractions?: number;
  freshContacts?: number;
  agingContacts?: number;
  staleContacts?: number;
  criticalContacts?: number;
  dataCompletenessScore?: number;
  interactionCoverage?: number;
  averageInteractionsPerContact?: number;
  contactsWithWarmthScore?: number;
  overallDataQuality?: string;
  showDataCollectionHints?: boolean;
  criticalDataGaps?: Array<string>;
  improvementSuggestions?: Array<string>;
  lastUpdated?: LocalDateTime;
};
