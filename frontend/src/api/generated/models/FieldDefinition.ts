/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { FieldType } from './FieldType';
export type FieldDefinition = {
  fieldKey?: string;
  label?: string;
  type?: FieldType;
  required?: boolean;
  readonly?: boolean;
  enumSource?: string;
  placeholder?: string;
  helpText?: string;
  gridCols?: number;
  validationRules?: Array<string>;
  fields?: Array<FieldDefinition>;
  itemSchema?: FieldDefinition;
  showInWizard?: boolean;
  wizardStep?: number;
  wizardOrder?: number;
  wizardSectionId?: string;
  wizardSectionTitle?: string;
  showDividerAfter?: boolean;
  rows?: number;
  visibleWhenField?: string;
  visibleWhenValue?: string;
};
