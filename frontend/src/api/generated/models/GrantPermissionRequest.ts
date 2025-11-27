/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { UUID } from './UUID';
export type GrantPermissionRequest = {
  userId?: UUID;
  permissionCode?: string;
  granted?: boolean;
  reason?: string;
};
