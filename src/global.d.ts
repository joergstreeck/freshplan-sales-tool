/**
 * Global type declarations for legacy window functions
 * These will be removed as we migrate to modules
 */

declare global {
  interface Window {
    // Legacy functions that are still referenced
    updateCalculator?: () => void;
    updateLocationIndustryFields?: () => void;
    switchTab?: (tabName: string) => void;
    
    // Legacy data structures
    freshplanData?: any;
    
    // Other legacy globals
    jsPDF?: any;
  }
}

export {};