/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { JsonObject } from './JsonObject';
import type { SettingsScope } from './SettingsScope';
import type { UUID } from './UUID';
/**
 * Setting with metadata and ETag
 */
export type SettingDto = {
  /**
   * Unique identifier
   */
  id?: UUID;
  /**
   * Setting scope
   */
  scope: SettingsScope;
  /**
   * Scope identifier
   */
  scopeId?: string;
  /**
   * Setting key
   */
  key: string;
  /**
   * Setting value as JSON
   */
  value: JsonObject;
  /**
   * Additional metadata
   */
  metadata?: JsonObject;
  /**
   * Entity tag for caching
   */
  etag?: string;
  /**
   * Version number for optimistic locking
   */
  version?: number;
  /**
   * Last update timestamp
   */
  updatedAt?: string;
};
