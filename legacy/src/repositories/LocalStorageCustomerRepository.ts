/**
 * LocalStorage implementation of Customer Repository
 * Handles customer data persistence in browser's localStorage
 */

import { ICustomerRepository } from './ICustomerRepository';
import type { CustomerData } from '../types';

export class LocalStorageCustomerRepository implements ICustomerRepository {
  private readonly STORAGE_KEY = 'freshplan_customerData';

  async save(data: CustomerData): Promise<void> {
    try {
      const serialized = JSON.stringify(data);
      localStorage.setItem(this.STORAGE_KEY, serialized);
    } catch (error) {
      console.error('[LocalStorageCustomerRepository] Save failed:', error);
      throw new Error('Failed to save customer data');
    }
  }

  async load(): Promise<CustomerData | null> {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (!stored) return null;
      
      return JSON.parse(stored) as CustomerData;
    } catch (error) {
      console.error('[LocalStorageCustomerRepository] Load failed:', error);
      return null;
    }
  }

  async clear(): Promise<void> {
    try {
      localStorage.removeItem(this.STORAGE_KEY);
    } catch (error) {
      console.error('[LocalStorageCustomerRepository] Clear failed:', error);
      throw new Error('Failed to clear customer data');
    }
  }

  async exists(): Promise<boolean> {
    return localStorage.getItem(this.STORAGE_KEY) !== null;
  }
}