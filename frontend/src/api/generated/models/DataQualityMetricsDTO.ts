/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type DataQualityMetricsDTO = {
  totalContacts?: number;
  contactsWithInteractions?: number;
  averageInteractionsPerContact?: number;
  dataCompletenessScore?: number;
  contactsWithWarmthScore?: number;
  warmthScoreConfidence?: number;
  suggestionsAcceptanceRate?: number;
  predictionAccuracy?: number;
  freshContacts?: number;
  agingContacts?: number;
  staleContacts?: number;
  criticalContacts?: number;
  showDataCollectionHints?: boolean;
  criticalDataGaps?: Array<string>;
  improvementSuggestions?: Array<string>;
  overallDataQuality?: string;
  interactionCoverage?: number;
};
