/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AlertSeverity } from './AlertSeverity';
import type { AlertType } from './AlertType';
import type { LocalDateTime } from './LocalDateTime';
import type { UUID } from './UUID';
export type DashboardAlert = {
  id?: UUID;
  title?: string;
  message?: string;
  type?: AlertType;
  severity?: AlertSeverity;
  customerId?: UUID;
  customerName?: string;
  createdAt?: LocalDateTime;
  actionLink?: string;
};
