/**
 * FreshPlan Configuration
 */

export const config = {
    // App Version
    version: '1.0.0',
    
    // API Endpoints (if needed in future)
    api: {
        baseUrl: '',
        endpoints: {
            customers: '/api/customers',
            orders: '/api/orders',
            products: '/api/products'
        }
    },
    
    // Discount Configuration
    discounts: {
        base: [
            { min: 0, max: 4999, rate: 0 },
            { min: 5000, max: 9999, rate: 3 },
            { min: 10000, max: 19999, rate: 6 },
            { min: 20000, max: 49999, rate: 9 },
            { min: 50000, max: Infinity, rate: 12 }
        ],
        leadTime: [
            { min: 0, max: 6, rate: 0 },
            { min: 7, max: 13, rate: 1 },
            { min: 14, max: 29, rate: 2 },
            { min: 30, max: Infinity, rate: 3 }
        ],
        pickup: 2,
        chain: 3,
        maxTotal: 20
    },
    
    // Default Values
    defaults: {
        orderValue: 15000,
        leadTime: 14,
        currency: 'EUR',
        language: 'de'
    },
    
    // Validation Rules
    validation: {
        email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        phone: /^[\d\s\-\+\(\)]+$/,
        postalCode: /^\d{5}$/,
        minOrderValue: 0,
        maxOrderValue: 1000000,
        minLeadTime: 0,
        maxLeadTime: 365
    },
    
    // Storage Keys
    storage: {
        prefix: 'freshplan_',
        keys: {
            calculator: 'calculatorData',
            customer: 'customerData',
            profile: 'profileData',
            settings: 'settings',
            language: 'preferredLanguage'
        }
    },
    
    // UI Configuration
    ui: {
        animationDuration: 300,
        autoSaveDelay: 1000,
        notificationDuration: 5000,
        debounceDelay: 250,
        throttleDelay: 100
    },
    
    // Feature Flags
    features: {
        pdfExport: true,
        dataImportExport: true,
        multiLanguage: true,
        autoSave: true,
        offlineMode: true
    },
    
    // Company Information
    company: {
        name: 'FreshFoodz GmbH',
        website: 'https://www.freshfoodz.de',
        support: 'support@freshfoodz.de',
        phone: '+49 123 456789'
    }
};

// Export as default
export default config;