/**
 * LocalStorage implementation of ICustomerRepository
 * Maintains backward compatibility with legacy freshplanData structure
 */

import { ICustomerRepository } from '../../domain/repositories/ICustomerRepository';
import type { CustomerData } from '../../types';

export class LocalStorageCustomerRepository implements ICustomerRepository {
  private readonly STORAGE_KEY = 'freshplanData'; // Legacy key for backward compatibility
  private readonly CUSTOMERS_KEY = 'freshplan_customers'; // Future key for customer-only data
  
  /**
   * Save customer data - maintains legacy structure for now
   * Stores complete form data including calculator and locations
   */
  async saveCustomer(customer: CustomerData): Promise<void> {
    try {
      // Get existing data to preserve calculator and locations
      const existingData = await this.getLegacyData();
      
      // Merge customer data with existing data
      const dataToSave = {
        ...existingData,
        customer: {
          ...customer,
          // Ensure backward compatibility fields
          customerType: customer.customerType || customer.customerStatus,
        }
      };
      
      // Save to localStorage
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(dataToSave));
      
      // Also save to new structure for future migration
      await this.saveToNewStructure(customer);
      
    } catch (error) {
      console.error('Error saving customer data:', error);
      throw new Error('Failed to save customer data');
    }
  }
  
  /**
   * Get complete legacy data object including calculator, customer, locations
   */
  async getLegacyData(): Promise<any | null> {
    try {
      const savedData = localStorage.getItem(this.STORAGE_KEY);
      if (!savedData) return null;
      
      return JSON.parse(savedData);
    } catch (error) {
      console.error('Error loading legacy data:', error);
      return null;
    }
  }
  
  /**
   * Get customer by ID - stub for future implementation
   */
  async getCustomerById(_id: string): Promise<CustomerData | null> {
    // TODO: Implement when we have proper ID-based storage
    console.warn('getCustomerById not yet implemented, returning null');
    return Promise.resolve(null);
  }
  
  /**
   * Update customer - stub for future implementation
   */
  async updateCustomer(_id: string, _customer: Partial<CustomerData>): Promise<void> {
    // TODO: Implement when we have proper ID-based storage
    console.warn('updateCustomer not yet implemented');
    return Promise.resolve();
  }
  
  /**
   * Delete customer - stub for future implementation
   */
  async deleteCustomer(_id: string): Promise<void> {
    // TODO: Implement when we have proper ID-based storage
    console.warn('deleteCustomer not yet implemented');
    return Promise.resolve();
  }
  
  /**
   * Get all customers - currently returns single customer from legacy data
   */
  async getAllCustomers(): Promise<CustomerData[]> {
    const legacyData = await this.getLegacyData();
    if (legacyData?.customer) {
      return [legacyData.customer];
    }
    
    // Check new structure
    const newCustomers = await this.getFromNewStructure();
    return newCustomers;
  }
  
  /**
   * Find customers by industry - stub for future implementation
   */
  async findCustomersByIndustry(industry: string): Promise<CustomerData[]> {
    const allCustomers = await this.getAllCustomers();
    return allCustomers.filter(c => c.industry === industry);
  }
  
  /**
   * Find customers by type - stub for future implementation
   */
  async findCustomersByType(type: 'neukunde' | 'bestandskunde'): Promise<CustomerData[]> {
    const allCustomers = await this.getAllCustomers();
    return allCustomers.filter(c => c.customerStatus === type);
  }
  
  /**
   * Save all customers - stub for future implementation
   */
  async saveAllCustomers(_customers: CustomerData[]): Promise<void> {
    // TODO: Implement batch save
    console.warn('saveAllCustomers not yet implemented');
    return Promise.resolve();
  }
  
  /**
   * Delete all customers - clears localStorage
   */
  async deleteAllCustomers(): Promise<void> {
    localStorage.removeItem(this.STORAGE_KEY);
    localStorage.removeItem(this.CUSTOMERS_KEY);
  }
  
  /**
   * Migrate legacy data to new structure - stub for future implementation
   */
  async migrateLegacyData(_data: any): Promise<void> {
    // TODO: Implement migration logic
    console.warn('migrateLegacyData not yet implemented');
    return Promise.resolve();
  }
  
  // Private helper methods
  
  /**
   * Save to new customer-only structure for future migration
   */
  private async saveToNewStructure(customer: CustomerData): Promise<void> {
    try {
      // Generate ID if not present
      const customerWithId = {
        ...customer,
        id: customer.customerNumber || `customer_${Date.now()}`
      };
      
      // Get existing customers (unused for now - legacy behavior)
      // const existingCustomers = await this.getFromNewStructure();
      
      // For now, just replace the single customer (legacy behavior)
      const newCustomers = [customerWithId];
      
      localStorage.setItem(this.CUSTOMERS_KEY, JSON.stringify(newCustomers));
    } catch (error) {
      // Silent fail for new structure - not critical for legacy operation
      console.warn('Failed to save to new structure:', error);
    }
  }
  
  /**
   * Get customers from new structure
   */
  private async getFromNewStructure(): Promise<CustomerData[]> {
    try {
      const data = localStorage.getItem(this.CUSTOMERS_KEY);
      if (!data) return [];
      
      return JSON.parse(data);
    } catch (error) {
      console.warn('Failed to read from new structure:', error);
      return [];
    }
  }
}