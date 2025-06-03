/**
 * Customer Service V2 - Business logic layer for customer operations
 * Uses Repository Pattern for data access
 * Handles validation, business rules, and orchestrates data persistence
 */

import { ICustomerRepository } from '../domain/repositories/ICustomerRepository';
import { ICustomerValidator } from '../domain/validators/ICustomerValidator';
import type { CustomerData, ValidationResult, ValidationError } from '../types';
import EventBus from '../core/EventBus';

// Business Error class for customer-specific errors
export class CustomerValidationError extends Error {
  constructor(
    message: string,
    public readonly errors: ValidationError[]
  ) {
    super(message);
    this.name = 'CustomerValidationError';
  }
}

// Simple validator implementation for Phase 2
class SimpleCustomerValidator implements ICustomerValidator {
  async validate(data: CustomerData): Promise<ValidationResult> {
    const requiredResult = this.validateRequiredFields(data);
    const businessResult = this.validateBusinessRules(data);
    
    return {
      isValid: requiredResult.isValid && businessResult.isValid,
      errors: [...requiredResult.errors, ...businessResult.errors]
    };
  }
  
  validateRequiredFields(data: CustomerData): ValidationResult {
    const errors: ValidationError[] = [];
    
    // Define required fields based on legacy-script.ts
    const requiredFields = [
      { field: 'companyName', message: 'Firmenname' },
      { field: 'legalForm', message: 'Rechtsform' },
      { field: 'customerType', message: 'Kundentyp' },
      { field: 'industry', message: 'Branche' },
      { field: 'street', message: 'Straße und Hausnummer' },
      { field: 'postalCode', message: 'PLZ' },
      { field: 'city', message: 'Ort' },
      { field: 'contactName', message: 'Ansprechpartner Name' },
      { field: 'contactPhone', message: 'Telefon' },
      { field: 'contactEmail', message: 'E-Mail' },
      { field: 'expectedVolume', message: 'Erwartetes Jahresvolumen' },
      { field: 'paymentMethod', message: 'Zahlungsart' }
    ];
    
    requiredFields.forEach(({ field, message }) => {
      const value = (data as any)[field];
      if (!value || (typeof value === 'string' && value.trim() === '')) {
        errors.push({
          field,
          message: `${message} ist ein Pflichtfeld`,
          rule: 'required'
        });
      }
    });
    
    return {
      isValid: errors.length === 0,
      errors
    };
  }
  
  validateBusinessRules(data: CustomerData): ValidationResult {
    const errors: ValidationError[] = [];
    
    // Email validation
    if (data.contactEmail && !this.isValidEmail(data.contactEmail)) {
      errors.push({
        field: 'contactEmail',
        message: 'Ungültige E-Mail-Adresse',
        rule: 'email'
      });
    }
    
    // Postal code validation (German 5-digit)
    if (data.postalCode && !/^\d{5}$/.test(data.postalCode)) {
      errors.push({
        field: 'postalCode',
        message: 'PLZ muss 5-stellig sein',
        rule: 'pattern'
      });
    }
    
    // Phone number basic validation
    if (data.contactPhone && data.contactPhone.length < 10) {
      errors.push({
        field: 'contactPhone',
        message: 'Telefonnummer ist zu kurz',
        rule: 'min'
      });
    }
    
    return {
      isValid: errors.length === 0,
      errors
    };
  }
  
  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}

export class CustomerServiceV2 {
  private validator: ICustomerValidator;
  
  constructor(
    private readonly repository: ICustomerRepository,
    validator?: ICustomerValidator,
    private readonly eventBus: typeof EventBus = EventBus
  ) {
    this.validator = validator || new SimpleCustomerValidator();
  }
  
  /**
   * Load initial customer data from legacy storage
   * Used to populate form on application start
   */
  async loadInitialCustomerData(): Promise<CustomerData | null> {
    try {
      const legacyData = await this.repository.getLegacyData();
      
      if (!legacyData) {
        console.log('No saved customer data found');
        return null;
      }
      
      // Return customer portion of legacy data
      return legacyData.customer || null;
      
    } catch (error) {
      console.error('Error loading initial customer data:', error);
      return null;
    }
  }
  
  /**
   * Load complete legacy data including calculator and locations
   * Needed for maintaining compatibility during migration
   */
  async loadCompleteLegacyData(): Promise<any | null> {
    try {
      return await this.repository.getLegacyData();
    } catch (error) {
      console.error('Error loading legacy data:', error);
      return null;
    }
  }
  
  /**
   * Save current customer data with validation and business rules
   * Maintains compatibility with legacy data structure
   */
  async saveCurrentCustomerData(data: CustomerData): Promise<void> {
    try {
      // 1. Validate required fields and business rules
      const validationResult = await this.validator.validate(data);
      
      if (!validationResult.isValid) {
        throw new CustomerValidationError(
          'Validierung fehlgeschlagen',
          validationResult.errors
        );
      }
      
      // 2. Check business rule: Neukunde + Rechnung warning
      // Map customerType to customerStatus for compatibility
      const customerStatus = data.customerStatus || 
        (data.customerType === 'neukunde' ? 'neukunde' : 
         data.customerType === 'bestandskunde' ? 'bestandskunde' : 
         undefined);
      
      if (customerStatus === 'neukunde' && data.paymentMethod === 'rechnung') {
        // Emit warning event
        this.eventBus.emit('customer:creditCheckRequired', {
          customer: data,
          message: 'Für Neukunden ist Zahlung auf Rechnung erst nach Bonitätsprüfung möglich.'
        });
        
        // For now, also log to console (can be removed when UI handles the event)
        console.warn('Hinweis: Für Neukunden ist Zahlung auf Rechnung erst nach Bonitätsprüfung möglich.');
      }
      
      // 3. Save via repository
      await this.repository.saveCustomer(data);
      
      // 4. Emit success event
      this.eventBus.emit('customer:saved', {
        customer: data,
        timestamp: Date.now()
      });
      
      console.log('Customer data saved successfully');
      
    } catch (error) {
      // Re-throw validation errors as-is
      if (error instanceof CustomerValidationError) {
        throw error;
      }
      
      // Wrap other errors
      console.error('Error saving customer data:', error);
      throw new Error('Fehler beim Speichern der Kundendaten');
    }
  }
  
  /**
   * Clear all customer data
   * Removes customer data while preserving other data (calculator, etc.)
   */
  async clearAllCustomerData(): Promise<void> {
    try {
      // Get legacy data to preserve non-customer parts
      const legacyData = await this.repository.getLegacyData();
      
      if (legacyData) {
        // Remove customer data but keep other parts
        delete legacyData.customer;
        delete legacyData.locations;
        delete legacyData.locationDetailsList;
        
        // Save back without customer data
        localStorage.setItem('freshplanData', JSON.stringify(legacyData));
      }
      
      // Also clear from new structure
      await this.repository.deleteAllCustomers();
      
      // Emit cleared event
      this.eventBus.emit('customer:cleared', {
        timestamp: Date.now()
      });
      
      console.log('Customer data cleared successfully');
      
    } catch (error) {
      console.error('Error clearing customer data:', error);
      throw new Error('Fehler beim Löschen der Kundendaten');
    }
  }
  
  /**
   * Additional business methods
   */
  
  /**
   * Check if customer has all required fields for invoice payment
   */
  async checkRequiredFieldsForInvoice(customer: CustomerData): Promise<boolean> {
    const invoiceRequiredFields = [
      'companyName',
      'legalForm',
      'street',
      'postalCode',
      'city',
      'ustId',
      'handelsregister'
    ];
    
    return invoiceRequiredFields.every(field => {
      const value = (customer as any)[field];
      return value && (typeof value !== 'string' || value.trim() !== '');
    });
  }
  
  /**
   * Calculate customer potential based on industry and size
   */
  async calculateCustomerPotential(customer: CustomerData): Promise<number> {
    // Simple calculation based on industry
    // This would be more sophisticated in production
    const industryMultipliers: Record<string, number> = {
      'hotel': 1.5,
      'krankenhaus': 2.0,
      'seniorenresidenz': 1.8,
      'betriebsrestaurant': 1.3,
      'restaurant': 1.0
    };
    
    const baseVolume = customer.expectedVolume || customer.annualVolume || 0;
    const multiplier = industryMultipliers[customer.industry] || 1.0;
    
    return Math.round(baseVolume * multiplier);
  }
  
  /**
   * Get validation errors for display
   */
  getValidationErrors(data: CustomerData): ValidationError[] {
    const result = this.validator.validateRequiredFields(data);
    return result.errors;
  }
  
  /**
   * Check if payment method warning should be shown
   */
  shouldShowPaymentWarning(customerType: string, paymentMethod: string): boolean {
    return customerType === 'neukunde' && paymentMethod === 'rechnung';
  }
}