/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { JsonObject } from './JsonObject';
import type { SettingsScope } from './SettingsScope';
/**
 * Request to create a new setting
 */
export type SettingCreateDto = {
  scope: SettingsScope;
  scopeId?: string;
  key: string;
  value: JsonObject;
  metadata?: JsonObject;
};
