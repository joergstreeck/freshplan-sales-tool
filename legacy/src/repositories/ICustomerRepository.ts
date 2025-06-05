/**
 * Customer Repository Interface
 * Defines the contract for customer data persistence
 */

import type { CustomerData } from '../types';

export interface ICustomerRepository {
  save(data: CustomerData): Promise<void>;
  load(): Promise<CustomerData | null>;
  clear(): Promise<void>;
  exists(): Promise<boolean>;
}