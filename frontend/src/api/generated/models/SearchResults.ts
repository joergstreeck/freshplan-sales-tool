/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { SearchMetadata } from './SearchMetadata';
import type { SearchResult } from './SearchResult';
export type SearchResults = {
  customers?: Array<SearchResult>;
  contacts?: Array<SearchResult>;
  totalCount?: number;
  executionTime?: number;
  metadata?: SearchMetadata;
};
