/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { LocalDateTime } from './LocalDateTime';
import type { TaskPriority } from './TaskPriority';
import type { TaskType } from './TaskType';
import type { UUID } from './UUID';
export type DashboardTask = {
  id?: UUID;
  title?: string;
  description?: string;
  type?: TaskType;
  priority?: TaskPriority;
  customerId?: UUID;
  customerName?: string;
  dueDate?: LocalDateTime;
  completed?: boolean;
};
