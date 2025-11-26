/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CardSection } from './CardSection';
export type CustomerCardSchema = {
  cardId?: string;
  title?: string;
  subtitle?: string;
  icon?: string;
  order?: number;
  sections?: Array<CardSection>;
  defaultCollapsed?: boolean;
};
