/**
 * Customer Module - TypeScript version
 * Handles customer data management with validation
 */

import Module from '../core/Module';
import { useStore } from '../store';
import { 
  validateEmail, 
  validatePhone, 
  validatePostalCode 
} from '../utils/validation';
import { CreditCheckService } from '../services/CreditCheckService';
import type { 
  CustomerData, 
  Industry, 
  ValidationRule,
  ValidationResult 
} from '../types';
import type { CreditCheckRequest, CreditCheckResponse } from '../services/CreditCheckService';


export default class CustomerModule extends Module {
  private form: HTMLFormElement | null = null;
  private autosaveTimer: NodeJS.Timeout | null = null;
  private validationRules: Map<string, ValidationRule> = new Map();
  private readonly AUTOSAVE_DELAY = 1000; // 1 second
  private creditCheckService: CreditCheckService;
  private creditCheckResponse: CreditCheckResponse | null = null;

  constructor() {
    super('customer');
    this.creditCheckService = CreditCheckService.getInstance();
  }

  async setup(): Promise<void> {
    console.log('[CustomerModule] Setting up...');
    
    // Get form element
    this.form = this.dom.$('#customerForm');
    console.log('[CustomerModule] Form element:', this.form);
    
    if (!this.form) {
      console.warn('[CustomerModule] Customer form not found during setup - will use event delegation');
    } else {
      // Setup form (add autosave attributes)
      this.buildForm();
      
      // Load saved data
      this.loadSavedData();
    }
    
    // Setup validation rules (always do this)
    this.setupValidationRules();
    
    console.log('[CustomerModule] Setup complete');
  }

  bindEvents(): void {
    console.log('[CustomerModule] Binding events...');

    // Use document-level event delegation for better reliability
    // This ensures events work even if elements are not immediately visible

    // Industry change
    this.on(document, 'change', '#industry', (e: Event) => {
      const select = e.target as HTMLSelectElement;
      this.handleIndustryChange(select.value as Industry);
    });

    // Form inputs with autosave - use delegation on document
    this.on(document, 'input', '#customerForm input, #customerForm select, #customerForm textarea', (e: Event) => {
      const input = e.target as HTMLInputElement;
      if (input.dataset.autosave === 'true') {
        this.handleInputChange(input);
      }
    });

    // Form submission - use delegation
    this.on(document, 'submit', '#customerForm', (e: Event) => {
      e.preventDefault();
      this.saveCustomerData();
    });

    // Credit check button - use delegation
    this.on(document, 'click', '#requestCreditCheck', (e: Event) => {
      console.log('[CustomerModule] Credit check button clicked');
      e.preventDefault();
      this.performCreditCheck();
    });

    // Management approval button - use delegation
    this.on(document, 'click', '#requestManagement', (e: Event) => {
      console.log('[CustomerModule] Management button clicked');
      e.preventDefault();
      this.requestManagementApproval();
    });

    // Clear form button - use delegation
    this.on(document, 'click', '#clearForm', (e: Event) => {
      console.log('[CustomerModule] Clear form button clicked');
      e.preventDefault();
      this.clearCustomerData();
    });

    // Customer type change for new/existing - use delegation
    this.on(document, 'change', '#customerType', (e: Event) => {
      const select = e.target as HTMLSelectElement;
      this.handleCustomerTypeChange(select.value);
    });

    console.log('[CustomerModule] Events bound successfully using delegation');
  }

  subscribeToState(): void {
    // React to external customer type changes
    useStore.subscribe(
      (state) => state.customer.customerType,
      (type) => {
        // Handle customer type changes from external sources
        const customerTypeSelect = this.dom.$('#customerType') as HTMLSelectElement;
        if (customerTypeSelect && type) {
          // Map store type to select value
          const selectValue = type === 'single' || type === 'chain' ? 'bestandskunde' : type;
          customerTypeSelect.value = selectValue;
        }
      }
    );

    // React to form reset from other modules
    this.events.on('app:reset', () => {
      this.clearCustomerData();
    });
  }

  /**
   * Build form UI - now just adds event listeners to existing HTML
   */
  private buildForm(): void {
    if (!this.form) return;

    // Add data-autosave attributes to all form inputs for autosave functionality
    const inputs = this.form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
      if (!input.hasAttribute('readonly')) {
        input.setAttribute('data-autosave', 'true');
      }
    });
  }

  /**
   * Setup validation rules
   */
  private setupValidationRules(): void {
    this.validationRules.set('companyName', { required: true });
    this.validationRules.set('legalForm', { required: true });
    this.validationRules.set('customerType', { required: true });
    this.validationRules.set('industry', { required: true });
    this.validationRules.set('ustId', { required: true });
    this.validationRules.set('contactName', { required: true });
    this.validationRules.set('contactEmail', { 
      required: true, 
      custom: validateEmail 
    });
    this.validationRules.set('contactPhone', { 
      required: true,
      custom: validatePhone 
    });
    this.validationRules.set('street', { required: true });
    this.validationRules.set('postalCode', { 
      required: true, 
      custom: validatePostalCode 
    });
    this.validationRules.set('city', { required: true });
    this.validationRules.set('expectedVolume', { required: true });
  }

  /**
   * Load saved customer data
   */
  private loadSavedData(): void {
    const state = useStore.getState();
    const data = state.customer.data;
    
    if (!data) {
      // Set defaults - nothing needed since form has defaults
      return;
    }

    // Load all form fields
    Object.entries(data).forEach(([key, value]) => {
      const field = this.dom.$(`#${key}`) as HTMLInputElement;
      if (field) {
        if (field.type === 'checkbox') {
          field.checked = Boolean(value);
        } else {
          field.value = String(value || '');
        }
      }
    });

    // Update customer type UI based on loaded data
    if (data.customerType) {
      this.handleCustomerTypeChange(data.customerType);
    }
    
    // Set industry (without generating specific fields for now)
    if (data.industry) {
      const industrySelect = this.dom.$('#industry') as HTMLSelectElement;
      if (industrySelect) {
        industrySelect.value = data.industry;
      }
    }
  }

  /**
   * Handle input changes
   */
  private handleInputChange(input: HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement): void {
    const { id, value, type } = input;
    const isCheckbox = type === 'checkbox';
    const fieldValue = isCheckbox ? (input as HTMLInputElement).checked : value;

    // Validate field
    const isValid = this.validateField(id, fieldValue);
    
    // Update store with partial data
    const store = useStore.getState();
    store.setCustomerData({ [id]: fieldValue });

    // Schedule autosave
    this.scheduleAutosave();

    // Update validation state
    if (!isValid) {
      store.customer.isValid = false;
    }
  }

  /**
   * Validate single field
   */
  private validateField(fieldId: string, value: any): boolean {
    const rule = this.validationRules.get(fieldId);
    if (!rule) return true;

    const field = this.dom.$(`#${fieldId}`) as HTMLInputElement;
    if (!field) return true;

    let isValid = true;
    let errorMessage = '';

    // Required check
    if (rule.required && !String(value).trim()) {
      isValid = false;
      errorMessage = 'Dieses Feld ist erforderlich';
    }
    // Custom validation
    else if (rule.custom && value) {
      const result = rule.custom(value);
      if (typeof result === 'string') {
        isValid = false;
        errorMessage = result;
      } else {
        isValid = result;
        if (!isValid) {
          errorMessage = this.getValidationMessage(fieldId);
        }
      }
    }

    // Update UI
    this.setFieldValidation(field, isValid, errorMessage);
    return isValid;
  }

  /**
   * Get validation error message
   */
  private getValidationMessage(fieldId: string): string {
    const messages: Record<string, string> = {
      contactEmail: 'Bitte geben Sie eine gültige E-Mail-Adresse ein',
      contactPhone: 'Bitte geben Sie eine gültige Telefonnummer ein',
      zipCode: 'Bitte geben Sie eine gültige Postleitzahl ein (5 Ziffern)'
    };
    return messages[fieldId] || 'Ungültiger Wert';
  }

  /**
   * Set field validation UI
   */
  private setFieldValidation(field: HTMLElement, isValid: boolean, errorMessage: string): void {
    const formGroup = field.closest('.form-group');
    if (!formGroup) return;

    // Remove existing error
    const existingError = formGroup.querySelector('.error-message');
    if (existingError) existingError.remove();

    if (isValid) {
      this.dom.removeClass(field, 'error');
    } else {
      this.dom.addClass(field, 'error');
      
      // Add error message
      const error = this.dom.create('span', {
        class: 'error-message'
      }, errorMessage);
      
      formGroup.appendChild(error);
    }
  }


  /**
   * Handle industry change
   */
  private handleIndustryChange(industry: Industry): void {
    const store = useStore.getState();
    store.setIndustry(industry);
    // Industry-specific fields can be added later if needed
  }



  /**
   * Schedule autosave
   */
  private scheduleAutosave(): void {
    if (this.autosaveTimer) {
      clearTimeout(this.autosaveTimer);
    }

    this.autosaveTimer = this.setTimeout(() => {
      this.autosave();
    }, this.AUTOSAVE_DELAY);
  }

  /**
   * Autosave data
   */
  private autosave(): void {
    const store = useStore.getState();
    store.saveCustomer();
    console.log('Customer data autosaved');
  }

  /**
   * Validate entire form
   */
  private validateForm(): ValidationResult {
    const errors: Array<{ field: string; message: string }> = [];
    let isValid = true;

    // Get current form data
    const formData = this.getFormData();

    // Validate each field with rules
    this.validationRules.forEach((_rule, fieldId) => {
      const value = formData[fieldId as keyof CustomerData];
      const fieldValid = this.validateField(fieldId, value);
      
      if (!fieldValid) {
        isValid = false;
        errors.push({
          field: fieldId,
          message: this.getValidationMessage(fieldId)
        });
      }
    });

    return { isValid, errors };
  }

  /**
   * Get form data
   */
  private getFormData(): Partial<CustomerData> {
    const data: Partial<CustomerData> = {};
    
    // Collect all form inputs
    const inputs = this.form?.querySelectorAll('input, select, textarea') || [];
    
    inputs.forEach((input) => {
      const field = input as HTMLInputElement;
      if (field.id) {
        if (field.type === 'checkbox') {
          data[field.id as keyof CustomerData] = field.checked as any;
        } else if (field.type === 'number') {
          data[field.id as keyof CustomerData] = parseInt(field.value) as any;
        } else {
          data[field.id as keyof CustomerData] = field.value as any;
        }
      }
    });

    return data;
  }

  /**
   * Save customer data
   */
  private saveCustomerData(): void {
    const validation = this.validateForm();
    
    if (!validation.isValid) {
      this.showError('Bitte füllen Sie alle erforderlichen Felder korrekt aus');
      
      // Focus first error field
      const firstError = validation.errors[0];
      if (firstError) {
        const field = this.dom.$(`#${firstError.field}`) as HTMLElement;
        field?.focus();
      }
      return;
    }

    // Save via store
    const store = useStore.getState();
    store.saveCustomer();
    
    this.showSuccess('Kundendaten gespeichert');
    this.emit('saved');
    
    // Navigate to profile if appropriate
    if (store.customer.data?.companyName) {
      this.events.emit('app:navigate', 'profile');
    }
  }

  /**
   * Clear customer data
   */
  private clearCustomerData(): void {
    if (!confirm('Möchten Sie wirklich alle Kundendaten löschen?')) {
      return;
    }

    // Clear store
    useStore.getState().clearCustomer();
    
    // Reset form
    this.form?.reset();
    
    // Reset credit check status
    this.creditCheckResponse = null;
    this.updateCreditCheckStatus();
    
    // Hide new customer alert
    const alert = this.dom.$('#newCustomerAlert');
    if (alert) {
      this.dom.addClass(alert, 'hidden');
    }
    
    this.showSuccess('Kundendaten gelöscht');
    this.emit('cleared');
  }

  /**
   * Public API
   */
  
  getCustomerData(): CustomerData | null {
    return useStore.getState().customer.data;
  }

  isValid(): boolean {
    return this.validateForm().isValid;
  }

  focusFirstError(): void {
    const firstError = this.form?.querySelector('.error') as HTMLElement;
    firstError?.focus();
  }

  /**
   * Handle customer type change (new/existing)
   */
  private handleCustomerTypeChange(type: string): void {
    const alert = this.dom.$('#newCustomerAlert');
    if (!alert) return;
    
    if (type === 'neukunde') {
      this.dom.removeClass(alert, 'hidden');
      // Reset credit check status
      this.creditCheckResponse = null;
      this.updateCreditCheckStatus();
    } else {
      this.dom.addClass(alert, 'hidden');
    }
    
    // Update store
    useStore.getState().setCustomerData({ customerType: type });
  }

  /**
   * Perform credit check via Allianz Trade
   */
  private async performCreditCheck(): Promise<void> {
    // Validate required fields first
    const validation = this.validateForm();
    if (!validation.isValid) {
      this.showError('Bitte füllen Sie alle erforderlichen Felder aus, bevor Sie eine Bonitätsprüfung anfordern.');
      return;
    }
    
    const formData = this.getFormData();
    
    // Show loading state
    const form = this.dom.$('#customerForm');
    if (form) {
      this.dom.addClass(form, 'form-loading');
    }
    
    try {
      const request: CreditCheckRequest = {
        companyName: formData.companyName || '',
        legalForm: formData.legalForm || '',
        registrationNumber: formData.handelsregister,
        vatId: formData.ustId || '',
        address: {
          street: formData.street || '',
          postalCode: formData.postalCode || '',
          city: formData.city || '',
          country: 'DE'
        },
        industry: formData.industry || '',
        companySize: formData.companySize,
        expectedVolume: parseFloat(formData.expectedVolume?.toString() || '0'),
        contactPerson: {
          name: formData.contactName || '',
          position: formData.contactPosition,
          email: formData.contactEmail || '',
          phone: formData.contactPhone || ''
        }
      };
      
      this.creditCheckResponse = await this.creditCheckService.checkCredit(request);
      this.updateCreditCheckStatus();
      
      // Handle result
      if (this.creditCheckResponse.status === 'approved') {
        this.showSuccess('Bonitätsprüfung erfolgreich! Kreditlimit: €' + 
          this.creditCheckResponse.creditLimit?.toLocaleString('de-DE'));
      } else if (this.creditCheckResponse.status === 'review') {
        this.showWarning('Bonitätsprüfung erfordert Überprüfung durch die Geschäftsleitung.');
      } else if (this.creditCheckResponse.status === 'rejected') {
        this.showError('Bonitätsprüfung abgelehnt: ' + this.creditCheckResponse.message);
      }
      
    } catch (error) {
      this.showError('Fehler bei der Bonitätsprüfung. Bitte versuchen Sie es später erneut.');
      console.error('Credit check error:', error);
    } finally {
      if (form) {
        this.dom.removeClass(form, 'form-loading');
      }
    }
  }

  /**
   * Request management approval for new customer
   */
  private async requestManagementApproval(): Promise<void> {
    const formData = this.getFormData();
    const expectedVolume = parseFloat(formData.expectedVolume?.toString() || '0');
    
    // Validate basic data
    if (!formData.companyName || !formData.contactEmail) {
      this.showError('Bitte füllen Sie mindestens Firmenname und E-Mail aus.');
      return;
    }
    
    try {
      const request: CreditCheckRequest = {
        companyName: formData.companyName || '',
        legalForm: formData.legalForm || '',
        registrationNumber: formData.handelsregister,
        vatId: formData.ustId || '',
        address: {
          street: formData.street || '',
          postalCode: formData.postalCode || '',
          city: formData.city || '',
          country: 'DE'
        },
        industry: formData.industry || '',
        companySize: formData.companySize,
        expectedVolume: expectedVolume,
        contactPerson: {
          name: formData.contactName || '',
          position: formData.contactPosition,
          email: formData.contactEmail || '',
          phone: formData.contactPhone || ''
        }
      };
      
      const result = await this.creditCheckService.requestManagementApproval({
        customerData: request,
        reason: 'Neukunde - Freigabe für Zahlung auf Rechnung erforderlich',
        requestedBy: 'Vertrieb',
        requestedLimit: expectedVolume
      });
      
      if (result.success) {
        this.showSuccess(result.message);
        // Update UI to show pending status
        const statusEl = this.dom.$('#creditStatus');
        if (statusEl) {
          statusEl.textContent = 'Prüfung angefordert';
          this.dom.addClass(statusEl, 'warning');
        }
      } else {
        this.showError(result.message);
      }
      
    } catch (error) {
      this.showError('Fehler beim Senden der Anfrage. Bitte versuchen Sie es erneut.');
      console.error('Management approval error:', error);
    }
  }

  /**
   * Update credit check status display
   */
  private updateCreditCheckStatus(): void {
    const statusEl = this.dom.$('#creditStatus');
    const limitEl = this.dom.$('#creditLimit');
    const ratingEl = this.dom.$('#creditRating');
    
    if (!statusEl || !limitEl || !ratingEl) return;
    
    if (!this.creditCheckResponse) {
      statusEl.textContent = 'Nicht geprüft';
      statusEl.className = 'status-value';
      limitEl.textContent = '-';
      ratingEl.textContent = '-';
      return;
    }
    
    // Update status
    const statusMap: Record<string, { text: string; class: string }> = {
      approved: { text: 'Genehmigt', class: 'positive' },
      rejected: { text: 'Abgelehnt', class: 'negative' },
      review: { text: 'Prüfung erforderlich', class: 'warning' },
      error: { text: 'Fehler', class: 'negative' }
    };
    
    const status = statusMap[this.creditCheckResponse.status];
    statusEl.textContent = status.text;
    statusEl.className = `status-value ${status.class}`;
    
    // Update limit
    if (this.creditCheckResponse.creditLimit) {
      limitEl.textContent = '€' + this.creditCheckResponse.creditLimit.toLocaleString('de-DE');
      limitEl.className = 'status-value positive';
    } else {
      limitEl.textContent = '-';
      limitEl.className = 'status-value';
    }
    
    // Update rating
    if (this.creditCheckResponse.rating) {
      const ratingInfo = this.creditCheckService.getRatingDescription(this.creditCheckResponse.rating);
      ratingEl.textContent = `${this.creditCheckResponse.rating}/10 (${ratingInfo.text})`;
      ratingEl.className = `status-value ${ratingInfo.color === 'success' ? 'positive' : ratingInfo.color === 'danger' ? 'negative' : 'warning'}`;
    } else {
      ratingEl.textContent = '-';
      ratingEl.className = 'status-value';
    }
    
    // Update payment terms recommendation
    const paymentTermsEl = this.dom.$('#paymentTerms') as HTMLSelectElement;
    if (paymentTermsEl && this.creditCheckService.needsSpecialPaymentTerms(this.creditCheckResponse)) {
      // Force to prepayment for rejected/high-risk customers
      paymentTermsEl.value = '0';
      
      // Add warning message
      const recommendedTerms = this.creditCheckService.getRecommendedPaymentTerms(this.creditCheckResponse);
      this.showWarning(`Empfohlene Zahlungsbedingungen: ${recommendedTerms}`);
    }
  }

  /**
   * Cleanup module resources
   */
  cleanup(): void {
    // Module cleanup is handled by base class
  }
}