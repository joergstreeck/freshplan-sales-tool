/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { Instant } from './Instant';
import type { UUID } from './UUID';
export type UserResponse = {
  id?: UUID;
  username?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  enabled?: boolean;
  roles?: Array<string>;
  xentralSalesRepId?: string;
  createdAt?: Instant;
  updatedAt?: Instant;
  fullName?: string;
};
