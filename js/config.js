// =============================
// FreshPlan Configuration
// =============================

// Error Handler
window.addEventListener('error', function(e) {
    console.warn('Fehler aufgefangen:', e.message);
    return true; // Verhindert Standard-Fehlerbehandlung
});

// In-Memory Storage for Claude.ai Environment
const memoryStorage = {
    data: {},
    getItem: function(key) {
        return this.data[key] || null;
    },
    setItem: function(key, value) {
        this.data[key] = value;
        return true;
    },
    removeItem: function(key) {
        delete this.data[key];
    }
};

// Use memoryStorage instead of localStorage
const storage = memoryStorage;

// Global Configuration
const config = {
    // Storage Keys
    storageKeys: {
        customerData: 'freshplanCustomerData',
        settings: 'freshplanSettings',
        autosave: 'freshplanAutosave'
    },
    
    // Discount Rules
    discountRules: {
        base: [
            { min: 75000, discount: 10 },
            { min: 50000, discount: 9 },
            { min: 30000, discount: 8 },
            { min: 15000, discount: 6 },
            { min: 5000, discount: 3 },
            { min: 0, discount: 0 }
        ],
        earlyBooking: [
            { min: 30, max: 44, discount: 3 },
            { min: 15, max: 29, discount: 2 },
            { min: 10, max: 14, discount: 1 },
            { min: 0, max: 9, discount: 0 }
        ],
        pickup: {
            minOrderValue: 5000,
            discount: 2
        },
        maxTotalDiscount: 15
    },
    
    // Industry Configurations
    industries: {
        hotel: {
            name: 'Hotel',
            avgPrices: {
                roomService: 25,
                breakfast: 8,
                restaurant: 35
            },
            utilization: {
                roomService: 0.1,
                restaurant: 0.6
            }
        },
        altenheim: {
            name: 'Alten-/Pflegeheim',
            avgPrices: {
                standard: 4.5,
                special: 5.5
            },
            ratios: {
                standard: 0.7,
                special: 0.3
            }
        },
        krankenhaus: {
            name: 'Krankenhaus/Klinik',
            avgPrices: {
                standard: 12,
                private: 25,
                staff: 6
            },
            staffRatio: 2.5
        },
        betriebsrestaurant: {
            name: 'Betriebsrestaurant',
            avgPrice: 7.5,
            operatingDays: 250
        },
        restaurant: {
            name: 'Restaurant/Caterer',
            avgPrices: {
                regular: 8,
                event: 20
            },
            operatingDays: 310,
            eventsPerYear: 52
        }
    },
    
    // Vending Configuration
    vending: {
        avgPrice: 10,
        operatingDays: 250
    },
    
    // Debounce Delays
    debounce: {
        calculation: 150,
        autosave: 1000,
        validation: 300
    },
    
    // Animation Durations
    animation: {
        fast: 300,
        medium: 500,
        slow: 1000
    },
    
    // Message Display Duration
    messageDuration: 3000
};

// Global State
const state = {
    currentLang: 'de',
    customerData: {},
    discountCalculation: {},
    chainLocations: [],
    calculatedRevenue: 0,
    generatedPdf: null,
    autosaveTimer: null,
    tabProgress: {
        demonstrator: false,
        customer: false,
        profile: false,
        offer: false
    },
    uploadedDocuments: {
        partnership: null,
        rabatt: null,
        agb: null
    }
};

// Export for use in other modules
window.FreshPlan = window.FreshPlan || {};
window.FreshPlan.config = config;
window.FreshPlan.state = state;
window.FreshPlan.storage = storage;