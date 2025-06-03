// CUSTOMER MODULE CODE SAMPLES - Für KI-Partner ohne TypeScript-Support
// Dies sind die wichtigsten Code-Ausschnitte aus unserer CustomerModuleV2 Implementierung

// ============================================
// 1. DOMAIN LAYER - ICustomerRepository (Interface als Kommentare)
// ============================================
/*
interface ICustomerRepository {
  // Core CRUD Operations
  getCustomerById(id: string): Promise<CustomerData | null>;
  saveCustomer(customer: CustomerData): Promise<void>;
  updateCustomer(id: string, customer: Partial<CustomerData>): Promise<void>;
  deleteCustomer(id: string): Promise<void>;
  
  // Query Operations
  getAllCustomers(): Promise<CustomerData[]>;
  findCustomersByIndustry(industry: string): Promise<CustomerData[]>;
  findCustomersByType(type: 'neukunde' | 'bestandskunde'): Promise<CustomerData[]>;
  
  // Legacy Support
  getLegacyData(): Promise<any | null>;
  migrateLegacyData(data: any): Promise<void>;
}
*/

// ============================================
// 2. INFRASTRUCTURE LAYER - LocalStorageCustomerRepository
// ============================================
class LocalStorageCustomerRepository {
  constructor() {
    this.STORAGE_KEY = 'freshplanData';
  }

  async saveCustomer(customer) {
    try {
      // Preserve existing data structure for backward compatibility
      const existingData = await this.getLegacyData() || {};
      const dataToSave = {
        ...existingData,
        customer: {
          ...customer,
          lastUpdated: new Date().toISOString()
        }
      };
      
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(dataToSave));
      console.log('Customer data saved to localStorage');
    } catch (error) {
      console.error('Error saving customer data:', error);
      throw new Error(`Failed to save customer data: ${error.message}`);
    }
  }

  async getCustomerById(id) {
    const data = await this.getLegacyData();
    return data?.customer?.id === id ? data.customer : null;
  }

  async getAllCustomers() {
    const data = await this.getLegacyData();
    return data?.customer ? [data.customer] : [];
  }

  async deleteAllCustomers() {
    const data = await this.getLegacyData() || {};
    delete data.customer;
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(data));
  }

  async getLegacyData() {
    try {
      const data = localStorage.getItem(this.STORAGE_KEY);
      return data ? JSON.parse(data) : null;
    } catch (error) {
      console.error('Error reading legacy data:', error);
      return null;
    }
  }
}

// ============================================
// 3. SERVICE LAYER - CustomerServiceV2
// ============================================
class CustomerServiceV2 {
  constructor(repository, validator, eventBus) {
    this.repository = repository;
    this.validator = validator || new SimpleCustomerValidator();
    this.eventBus = eventBus || { emit: () => {}, on: () => {} };
  }

  async loadInitialCustomerData() {
    try {
      const legacyData = await this.repository.getLegacyData();
      return legacyData?.customer || null;
    } catch (error) {
      console.error('Error loading customer data:', error);
      return null;
    }
  }

  async saveCurrentCustomerData(customerData) {
    // Step 1: Validate
    const validationResult = await this.validator.validate(customerData);
    if (!validationResult.isValid) {
      throw new CustomerValidationError(
        'Validierung fehlgeschlagen',
        validationResult.errors
      );
    }

    // Step 2: Check business rules
    if (this.shouldShowPaymentWarning(customerData.customerStatus, customerData.paymentMethod)) {
      this.eventBus.emit('customer:creditCheckRequired', {
        customer: customerData,
        message: 'Für Neukunden ist Zahlung auf Rechnung erst nach Bonitätsprüfung möglich.'
      });
    }

    // Step 3: Save
    await this.repository.saveCustomer(customerData);
    console.log('Customer data saved successfully');

    // Step 4: Emit success event
    this.eventBus.emit('customer:saved', {
      customer: customerData,
      timestamp: Date.now()
    });
  }

  async clearAllCustomerData() {
    await this.repository.deleteAllCustomers();
    console.log('Customer data cleared successfully');
    this.eventBus.emit('customer:cleared');
  }

  shouldShowPaymentWarning(customerStatus, paymentMethod) {
    return customerStatus === 'neukunde' && paymentMethod === 'rechnung';
  }
}

// ============================================
// 4. VALIDATION - SimpleCustomerValidator
// ============================================
class SimpleCustomerValidator {
  async validate(data) {
    const errors = [];
    
    // Required field validation
    const requiredFields = [
      'companyName', 'legalForm', 'customerType', 'customerStatus',
      'industry', 'street', 'postalCode', 'city', 'contactName',
      'contactPhone', 'contactEmail', 'expectedVolume', 'paymentMethod'
    ];

    for (const field of requiredFields) {
      if (!data[field] || data[field].toString().trim() === '') {
        errors.push(`${field} ist ein Pflichtfeld`);
      }
    }

    // Format validation
    if (data.contactEmail && !this.isValidEmail(data.contactEmail)) {
      errors.push('Bitte geben Sie eine gültige E-Mail-Adresse ein');
    }

    if (data.postalCode && !this.isValidPostalCode(data.postalCode)) {
      errors.push('Die Postleitzahl muss 5 Ziffern haben');
    }

    if (data.contactPhone && !this.isValidPhone(data.contactPhone)) {
      errors.push('Bitte geben Sie eine gültige Telefonnummer ein');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  isValidPostalCode(plz) {
    return /^\d{5}$/.test(plz);
  }

  isValidPhone(phone) {
    return /^[\d\s\-\+\(\)]+$/.test(phone) && phone.length >= 6;
  }
}

// ============================================
// 5. UI LAYER - CustomerModuleV2 (vereinfacht)
// ============================================
class CustomerModuleV2 extends Module {
  async setup() {
    // Initialize repository and service
    const repository = new LocalStorageCustomerRepository();
    this.customerService = new CustomerServiceV2(repository, null, this.events);
    
    // Load saved data
    const savedCustomerData = await this.customerService.loadInitialCustomerData();
    if (savedCustomerData) {
      await this.populateForm(savedCustomerData);
    }
  }

  bindEvents() {
    // Save button
    const saveBtn = this.dom.$('.header-btn-save');
    if (saveBtn) {
      this.on(saveBtn, 'click', async () => {
        await this.handleSave();
      });
    }

    // Clear button
    const clearBtn = this.dom.$('.header-btn-clear');
    if (clearBtn) {
      this.on(clearBtn, 'click', async () => {
        await this.handleClear();
      });
    }

    // Chain customer toggle
    const chainSelect = this.dom.$('#chainCustomer');
    if (chainSelect) {
      this.on(chainSelect, 'change', (e) => {
        this.toggleLocationsTab(e.target.value === 'ja');
      });
    }

    // Payment method change (for warning)
    const paymentSelect = this.dom.$('#paymentMethod');
    if (paymentSelect) {
      this.on(paymentSelect, 'change', () => {
        this.checkPaymentWarning();
      });
    }

    // Currency formatting
    const volumeInput = this.dom.$('#expectedVolume');
    if (volumeInput) {
      this.on(volumeInput, 'input', (e) => {
        this.formatCurrency(e.target);
      });
    }
  }

  async handleSave() {
    try {
      const formData = this.collectFormData();
      await this.customerService.saveCurrentCustomerData(formData);
      alert('✓ Daten wurden erfolgreich gespeichert!');
    } catch (error) {
      if (error.name === 'CustomerValidationError') {
        alert(`Bitte überprüfen Sie Ihre Eingaben:\n\n${error.errors.join('\n')}`);
      } else {
        alert(`Fehler beim Speichern: ${error.message}`);
      }
    }
  }

  async handleClear() {
    if (confirm('Möchten Sie wirklich alle Kundendaten löschen?')) {
      await this.customerService.clearAllCustomerData();
      this.clearForm();
      alert('✓ Formular wurde geleert!');
    }
  }

  collectFormData() {
    return {
      companyName: this.dom.$('#companyName')?.value || '',
      legalForm: this.dom.$('#legalForm')?.value || '',
      customerType: this.dom.$('#customerType')?.value || '',
      customerStatus: this.dom.$('#customerStatus')?.value || '',
      industry: this.dom.$('#industry')?.value || '',
      street: this.dom.$('#street')?.value || '',
      postalCode: this.dom.$('#postalCode')?.value || '',
      city: this.dom.$('#city')?.value || '',
      contactName: this.dom.$('#contactName')?.value || '',
      contactPhone: this.dom.$('#contactPhone')?.value || '',
      contactEmail: this.dom.$('#contactEmail')?.value || '',
      expectedVolume: this.dom.$('#expectedVolume')?.value?.replace(/\./g, '') || '',
      paymentMethod: this.dom.$('#paymentMethod')?.value || ''
    };
  }

  formatCurrency(input) {
    let value = input.value.replace(/\./g, '');
    if (value && !isNaN(value)) {
      input.value = parseInt(value).toLocaleString('de-DE');
    }
  }
}

// ============================================
// 6. PROBLEMATISCHER CODE - Module Base Class
// ============================================
class Module {
  constructor() {
    this.events = window.FreshPlan?.events || new EventBus();
    this.dom = new DOMHelper();
    this.eventHandlers = new Map();
  }

  // PROBLEM: Diese Methode funktioniert in Tests, aber nicht in der App!
  on(target, event, handler) {
    if (!target) return;
    
    // Store for cleanup
    if (!this.eventHandlers.has(target)) {
      this.eventHandlers.set(target, new Map());
    }
    this.eventHandlers.get(target).set(event, handler);
    
    // Add listener
    target.addEventListener(event, handler);
  }

  emit(event, data) {
    this.events.emit(event, data);
  }

  async setup() {
    // Override in subclass
  }

  bindEvents() {
    // Override in subclass
  }

  subscribeToState() {
    // Override in subclass
  }
}

// ============================================
// 7. ERROR CLASSES
// ============================================
class CustomerValidationError extends Error {
  constructor(message, errors = []) {
    super(message);
    this.name = 'CustomerValidationError';
    this.errors = errors;
  }
}

// ============================================
// 8. TEST BEISPIELE (die funktionieren)
// ============================================
async function testCustomerService() {
  console.log('=== Testing CustomerServiceV2 ===');
  
  // Create instances
  const repository = new LocalStorageCustomerRepository();
  const service = new CustomerServiceV2(repository);
  
  // Test 1: Save valid data
  const validData = {
    companyName: 'Test GmbH',
    legalForm: 'gmbh',
    customerType: 'single',
    customerStatus: 'neukunde',
    industry: 'hotel',
    street: 'Teststraße 1',
    postalCode: '12345',
    city: 'Berlin',
    contactName: 'Max Mustermann',
    contactPhone: '0123456789',
    contactEmail: 'test@example.com',
    expectedVolume: '50000',
    paymentMethod: 'vorkasse'
  };
  
  try {
    await service.saveCurrentCustomerData(validData);
    console.log('✓ Valid data saved successfully');
  } catch (error) {
    console.error('✗ Save failed:', error);
  }
  
  // Test 2: Load saved data
  const loadedData = await service.loadInitialCustomerData();
  console.log('✓ Loaded data:', loadedData?.companyName);
  
  // Test 3: Invalid data
  try {
    await service.saveCurrentCustomerData({ companyName: '' });
    console.log('✗ Should have failed validation');
  } catch (error) {
    console.log('✓ Validation correctly rejected invalid data');
  }
}

// ============================================
// 9. DAS PROBLEM IN DER APP
// ============================================
/*
PROBLEM: In der echten App passiert folgendes:

1. CustomerModuleV2 wird korrekt instantiiert
2. setup() wird aufgerufen
3. bindEvents() wird aufgerufen
4. ABER: Die Event-Handler werden nie ausgeführt!

Vermutung:
- Legacy-Script überschreibt die Events
- DOM ist noch nicht ready
- Module base class hat ein Problem
- Race Condition mit anderen Modulen

Was funktioniert:
- Direkte Tests der Services
- Unit Tests
- Isolierte HTML-Seiten

Was NICHT funktioniert:
- Integration in die Haupt-App
- Event-Binding über Module base class
- E2E Tests
*/