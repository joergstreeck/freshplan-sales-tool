/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DuplicateAction } from './DuplicateAction';
export type ImportExecuteRequest = {
  mapping: Record<string, string>;
  duplicateAction?: DuplicateAction;
  source?: string;
  ignoreErrors?: boolean;
};
