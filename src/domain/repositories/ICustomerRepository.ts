/**
 * Repository interface for customer data access
 * Defines the contract for all customer data operations
 */

import type { CustomerData } from '../../types';

export interface ICustomerRepository {
  // Core CRUD Operations
  getCustomerById(id: string): Promise<CustomerData | null>;
  saveCustomer(customer: CustomerData): Promise<void>;
  updateCustomer(id: string, customer: Partial<CustomerData>): Promise<void>;
  deleteCustomer(id: string): Promise<void>;
  
  // Query Operations
  getAllCustomers(): Promise<CustomerData[]>;
  findCustomersByIndustry(industry: string): Promise<CustomerData[]>;
  findCustomersByType(type: 'neukunde' | 'bestandskunde'): Promise<CustomerData[]>;
  
  // Bulk Operations
  saveAllCustomers(customers: CustomerData[]): Promise<void>;
  deleteAllCustomers(): Promise<void>;
  
  // Legacy Support - Phase 2 migration helpers
  getLegacyData(): Promise<any | null>;
  migrateLegacyData(data: any): Promise<void>;
}