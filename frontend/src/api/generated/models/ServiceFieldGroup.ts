/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { FieldDefinition } from './FieldDefinition';
export type ServiceFieldGroup = {
  /**
   * Group ID
   */
  id?: string;
  /**
   * Group title (German)
   */
  title?: string;
  /**
   * Group icon (emoji)
   */
  icon?: string;
  /**
   * Field definitions for this group
   */
  fields?: Array<FieldDefinition>;
};
