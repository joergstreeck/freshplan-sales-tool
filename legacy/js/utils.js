/**
 * FreshPlan Utility Functions
 */

import { translations } from './translations.js';

// Initialize language settings
export function initializeLanguage() {
    const savedLang = localStorage.getItem('preferredLanguage') || 'de';
    window.appState.language = savedLang;
    
    const languageSelect = document.getElementById('languageSelect');
    if (languageSelect) {
        languageSelect.value = savedLang;
    }
    
    updatePageLanguage(savedLang);
}

// Update page language
export function updatePageLanguage(lang) {
    document.documentElement.lang = lang;
    
    // Update all elements with data-i18n attribute
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        const translation = getTranslation(key, lang);
        
        if (translation) {
            if (element.tagName === 'INPUT' && element.hasAttribute('placeholder')) {
                element.placeholder = translation;
            } else {
                element.textContent = translation;
            }
        }
    });
    
    // Update document title
    document.title = getTranslation('app.title', lang) || 'FreshPlan Sales Tool';
}

// Get translation by key
export function getTranslation(key, lang = window.appState.language) {
    const keys = key.split('.');
    let value = translations[lang];
    
    for (const k of keys) {
        value = value?.[k];
    }
    
    // Fallback to German if translation not found
    if (!value && lang !== 'de') {
        value = getTranslation(key, 'de');
    }
    
    return value || key;
}

// Format currency
export function formatCurrency(amount, currency = 'EUR') {
    return new Intl.NumberFormat(window.appState.language === 'de' ? 'de-DE' : 'en-US', {
        style: 'currency',
        currency: currency,
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(amount);
}

// Format date
export function formatDate(date, format = 'medium') {
    const d = new Date(date);
    const locale = window.appState.language === 'de' ? 'de-DE' : 'en-US';
    
    switch (format) {
        case 'short':
            return d.toLocaleDateString(locale);
        case 'long':
            return d.toLocaleDateString(locale, { 
                weekday: 'long', 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric' 
            });
        default:
            return d.toLocaleDateString(locale, { 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric' 
            });
    }
}

// Validate email
export function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// Validate phone number
export function validatePhone(phone) {
    const re = /^[\d\s\-\+\(\)]+$/;
    return re.test(phone) && phone.replace(/\D/g, '').length >= 10;
}

// Debounce function
export function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Throttle function
export function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// Deep clone object
export function deepClone(obj) {
    if (obj === null || typeof obj !== 'object') return obj;
    if (obj instanceof Date) return new Date(obj.getTime());
    if (obj instanceof Array) return obj.map(item => deepClone(item));
    if (obj instanceof Object) {
        const clonedObj = {};
        for (const key in obj) {
            if (obj.hasOwnProperty(key)) {
                clonedObj[key] = deepClone(obj[key]);
            }
        }
        return clonedObj;
    }
}

// Generate unique ID
export function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substring(2);
}

// Save to localStorage with error handling
export function saveToStorage(key, data) {
    try {
        localStorage.setItem(key, JSON.stringify(data));
        return true;
    } catch (e) {
        console.error('Error saving to localStorage:', e);
        return false;
    }
}

// Load from localStorage with error handling
export function loadFromStorage(key, defaultValue = null) {
    try {
        const data = localStorage.getItem(key);
        return data ? JSON.parse(data) : defaultValue;
    } catch (e) {
        console.error('Error loading from localStorage:', e);
        return defaultValue;
    }
}

// Export data as JSON
export function exportData() {
    const data = {
        version: '1.0',
        exportDate: new Date().toISOString(),
        calculator: window.appState.calculatorData,
        customer: window.appState.customerData,
        profile: window.appState.profileData,
        settings: window.appState.settings
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `freshplan-export-${formatDate(new Date(), 'short').replace(/\//g, '-')}.json`;
    a.click();
    URL.revokeObjectURL(url);
}

// Import data from JSON
export function importData(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        
        reader.onload = (e) => {
            try {
                const data = JSON.parse(e.target.result);
                
                // Validate data structure
                if (!data.version || !data.exportDate) {
                    throw new Error('Invalid data format');
                }
                
                // Import data
                if (data.calculator) window.appState.calculatorData = data.calculator;
                if (data.customer) window.appState.customerData = data.customer;
                if (data.profile) window.appState.profileData = data.profile;
                if (data.settings) window.appState.settings = data.settings;
                
                // Save to localStorage
                saveToStorage('calculatorData', data.calculator);
                saveToStorage('customerData', data.customer);
                saveToStorage('profileData', data.profile);
                saveToStorage('settings', data.settings);
                
                resolve(data);
            } catch (error) {
                reject(error);
            }
        };
        
        reader.onerror = () => reject(new Error('Failed to read file'));
        reader.readAsText(file);
    });
}