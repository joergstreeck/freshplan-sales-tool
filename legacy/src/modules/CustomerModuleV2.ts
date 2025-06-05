/**
 * Customer Module V2 - UI Controller for customer data management
 * Uses CustomerServiceV2 for business logic
 * Replaces legacy customer functionality from legacy-script.ts
 * Now with toast notifications instead of blocking alerts
 */

import Module from '../core/Module';
import { CustomerServiceV2, CustomerValidationError } from '../services/CustomerServiceV2';
import { LocalStorageCustomerRepository } from '../infrastructure/repositories/LocalStorageCustomerRepository';
import type { CustomerData, ValidationError } from '../types';
import { DebugLogger } from '../utils/debug';
import { toast } from '../utils/toast';

export default class CustomerModuleV2 extends Module {
  private service?: CustomerServiceV2;
  private formElements: Map<string, HTMLElement> = new Map();
  private buttonHandlers: Array<{ element: HTMLElement; handler: EventListener }> = [];

  constructor() {
    super('customer');
    DebugLogger.log('CustomerModuleV2', 'Constructor called');
  }

  async setup(): Promise<void> {
    DebugLogger.log('CustomerModuleV2', 'Setup started');
    
    // Initialize service with repository
    const repo = new LocalStorageCustomerRepository();
    this.service = new CustomerServiceV2(repo);
    this.setModuleState('service', this.service);
    
    // Cache form elements
    this.cacheFormElements();
    
    // Load initial data
    await this.loadInitialData();
    
    DebugLogger.log('CustomerModuleV2', 'Setup completed');
  }

  bindEvents(): void {
    DebugLogger.log('CustomerModuleV2', 'Binding events...');
    
    // Direct binding to buttons with capture phase for highest priority
    const saveBtn = document.querySelector('.header-btn-save') as HTMLElement;
    const clearBtn = document.querySelector('.header-btn-clear') as HTMLElement;
    
    if (saveBtn) {
      const saveHandler = async (e: Event) => {
        e.preventDefault();
        e.stopPropagation();
        DebugLogger.log('CustomerModuleV2', 'Save button clicked (direct binding)');
        await this.handleSave();
      };
      saveBtn.addEventListener('click', saveHandler, { capture: true });
      this.buttonHandlers.push({ element: saveBtn, handler: saveHandler });
      DebugLogger.log('CustomerModuleV2', 'Save button bound directly with capture');
    } else {
      DebugLogger.warn('CustomerModuleV2', 'Save button not found');
    }
    
    if (clearBtn) {
      const clearHandler = async (e: Event) => {
        e.preventDefault();
        e.stopPropagation();
        DebugLogger.log('CustomerModuleV2', 'Clear button clicked (direct binding)');
        await this.handleClear();
      };
      clearBtn.addEventListener('click', clearHandler, { capture: true });
      this.buttonHandlers.push({ element: clearBtn, handler: clearHandler });
      DebugLogger.log('CustomerModuleV2', 'Clear button bound directly with capture');
    } else {
      DebugLogger.warn('CustomerModuleV2', 'Clear button not found');
    }
    
    // Customer status/type change (for credit check warning)
    // Handle both #customerStatus and #customerType for compatibility
    this.on(document, 'change', '#customerStatus, #customerType', (e: Event) => {
      const select = e.target as HTMLSelectElement;
      DebugLogger.log('CustomerModuleV2', 'Customer status/type changed:', select.value);
      this.checkCreditRequirement();
    });
    
    // Payment method change
    this.on(document, 'change', '#paymentMethod', (e: Event) => {
      const select = e.target as HTMLSelectElement;
      DebugLogger.log('CustomerModuleV2', 'Payment method changed:', select.value);
      this.checkCreditRequirement();
    });
    
    DebugLogger.log('CustomerModuleV2', 'Events bound successfully');
  }

  subscribeToState(): void {
    // Subscribe to relevant state changes if needed
    DebugLogger.log('CustomerModuleV2', 'State subscriptions setup');
  }

  private cacheFormElements(): void {
    const form = document.getElementById('customerForm');
    if (!form) {
      DebugLogger.warn('CustomerModuleV2', 'Customer form not found');
      return;
    }
    
    // Cache commonly used elements
    const elementIds = [
      'companyName', 'legalForm', 'customerType', 'customerStatus',
      'industry', 'street', 'postalCode', 'city', 'contactName',
      'contactPhone', 'contactEmail', 'paymentMethod', 'chainCustomer',
      'expectedVolume', 'annualVolume'
    ];
    
    elementIds.forEach(id => {
      const element = document.getElementById(id);
      if (element) {
        this.formElements.set(id, element);
      }
    });
    
    DebugLogger.log('CustomerModuleV2', `Cached ${this.formElements.size} form elements`);
  }

  private async loadInitialData(): Promise<void> {
    try {
      const data = await this.service!.loadInitialCustomerData();
      if (data) {
        this.populateForm(data);
        DebugLogger.log('CustomerModuleV2', 'Initial data loaded');
      }
    } catch (error) {
      DebugLogger.error('CustomerModuleV2', 'Failed to load initial data:', error as Error);
    }
  }

  private async handleSave(): Promise<void> {
    try {
      const formData = this.collectFormData();
      await this.service!.saveCurrentCustomerData(formData);
      
      // Emit event for other modules BEFORE toast
      this.events.emit('customer:saved', { data: formData });
      
      // Also emit on window for Playwright tests BEFORE toast
      window.dispatchEvent(new CustomEvent('customer:saved', {
        detail: { data: formData }
      }));
      
      // Show success message AFTER events
      alert('Kundendaten erfolgreich gespeichert!');
      
    } catch (error) {
      if (error instanceof CustomerValidationError) {
        this.showValidationErrors(error.errors);
      } else {
        alert('Fehler beim Speichern: ' + (error as Error).message);
      }
    }
  }

  private async handleClear(): Promise<void> {
    if (!confirm('Möchten Sie alle Kundendaten löschen?')) {
      return;
    }
    
    try {
      await this.service!.clearAllCustomerData();
      this.clearForm();
      
      // Emit event for other modules BEFORE any UI feedback
      this.events.emit('customer:cleared');
      
      // Also emit on window for Playwright tests BEFORE any UI feedback
      window.dispatchEvent(new CustomEvent('customer:cleared'));
      
      // Log success
      console.log('Kundendaten wurden gelöscht');
      
      DebugLogger.log('CustomerModuleV2', 'Customer data cleared');
    } catch (error) {
      alert('Fehler beim Löschen: ' + (error as Error).message);
    }
  }

  private checkCreditRequirement(): void {
    // Check both customerStatus and customerType for compatibility
    const customerStatus = (this.formElements.get('customerStatus') as HTMLSelectElement)?.value ||
                          (this.formElements.get('customerType') as HTMLSelectElement)?.value;
    const paymentMethod = (this.formElements.get('paymentMethod') as HTMLSelectElement)?.value;
    
    if (customerStatus === 'neukunde' && paymentMethod === 'rechnung') {
      alert('Hinweis: Für Neukunden ist Zahlung auf Rechnung erst nach Bonitätsprüfung möglich.');
      
      // Emit event
      this.events.emit('customer:creditCheckRequired', {
        customerStatus,
        paymentMethod
      });
      
      // Also emit on window for Playwright tests
      window.dispatchEvent(new CustomEvent('customer:creditCheckRequired', {
        detail: { customerStatus, paymentMethod }
      }));
    }
  }

  private collectFormData(): CustomerData {
    const getValue = (id: string): string => {
      const element = this.formElements.get(id) as HTMLInputElement | HTMLSelectElement;
      return element?.value || '';
    };
    
    return {
      companyName: getValue('companyName'),
      legalForm: getValue('legalForm') as CustomerData['legalForm'],
      customerType: getValue('customerType') as CustomerData['customerType'],
      customerStatus: getValue('customerStatus') as CustomerData['customerStatus'],
      industry: getValue('industry') as CustomerData['industry'],
      street: getValue('street'),
      postalCode: getValue('postalCode'),
      city: getValue('city'),
      contactName: getValue('contactName'),
      contactPhone: getValue('contactPhone'),
      contactEmail: getValue('contactEmail'),
      paymentMethod: getValue('paymentMethod') as CustomerData['paymentMethod'],
      chainCustomer: getValue('chainCustomer') as CustomerData['chainCustomer'],
      // Add other fields as needed
      expectedVolume: parseInt(getValue('expectedVolume')) || 50000, // Default value
      annualVolume: parseInt(getValue('annualVolume')) || 50000
    };
  }

  private populateForm(data: CustomerData): void {
    Object.entries(data).forEach(([key, value]) => {
      const element = this.formElements.get(key);
      if (element) {
        if (element instanceof HTMLInputElement || element instanceof HTMLSelectElement) {
          element.value = String(value);
        }
      }
    });
  }

  private clearForm(): void {
    const form = document.getElementById('customerForm') as HTMLFormElement;
    if (form) {
      form.reset();
    }
  }

  private showValidationErrors(errors: ValidationError[]): void {
    const messages = errors.map(e => `${e.message}`).join('\n');
    alert('Bitte korrigieren Sie folgende Fehler:\n\n' + messages);
  }

  async destroy(): Promise<void> {
    // Remove direct button handlers
    this.buttonHandlers.forEach(({ element, handler }) => {
      element.removeEventListener('click', handler, { capture: true });
    });
    this.buttonHandlers = [];
    
    this.formElements.clear();
    await super.destroy();
  }

  /**
   * Cleanup implementation required by abstract Module class
   */
  async cleanup(): Promise<void> {
    // Remove direct button handlers
    this.buttonHandlers.forEach(({ element, handler }) => {
      element.removeEventListener('click', handler, { capture: true });
    });
    this.buttonHandlers = [];
    
    this.formElements.clear();
  }

  /**
   * Helper methods for toast notifications
   */
  protected showSuccess(msg: string): void { toast(msg, 'success'); }
  protected showWarning(msg: string): void { toast(msg, 'warning'); }
  protected showError(msg: string): void   { toast(msg, 'error'); }
  protected showInfo(msg: string): void    { toast(msg, 'info'); }
}