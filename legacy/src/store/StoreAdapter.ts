/**
 * StoreAdapter - Bridge between Zustand store and legacy modules
 * Provides backward compatibility while migrating to Zustand
 */

import { useStore, type Store } from './index';
import EventBus from '../core/EventBus';
import type { StateChangeEvent } from '../core/StateManager';

export class StoreAdapter {
  private static instance: StoreAdapter;
  private pathListeners: Map<string, Set<(event: StateChangeEvent) => void>> = new Map();

  private constructor() {
    this.setupStoreSubscriptions();
  }

  static getInstance(): StoreAdapter {
    if (!StoreAdapter.instance) {
      StoreAdapter.instance = new StoreAdapter();
    }
    return StoreAdapter.instance;
  }

  /**
   * Get state value by path (legacy StateManager compatible)
   */
  get<T = any>(path?: string, defaultValue?: T): T {
    const state = useStore.getState();
    
    if (!path) return state as any;
    
    const keys = path.split('.');
    let value: any = state;
    
    for (const key of keys) {
      if (value && typeof value === 'object' && key in value) {
        value = value[key];
      } else {
        return defaultValue as T;
      }
    }
    
    return value as T;
  }

  /**
   * Set state value by path (legacy StateManager compatible)
   */
  set<T = any>(path: string, value: T): void {
    const keys = path.split('.');
    
    // Map to Zustand actions
    if (keys[0] === 'calculator') {
      this.handleCalculatorUpdate(keys.slice(1), value);
    } else if (keys[0] === 'customer') {
      this.handleCustomerUpdate(keys.slice(1), value);
    } else if (keys[0] === 'settings') {
      this.handleSettingsUpdate(keys.slice(1), value);
    } else if (keys[0] === 'ui') {
      this.handleUIUpdate(keys.slice(1), value);
    } else if (keys[0] === 'i18n') {
      this.handleI18nUpdate(keys.slice(1), value);
    } else {
      // For other paths, emit event for manual handling
      console.warn(`StoreAdapter: Unhandled path ${path}`);
      this.emitChange(path, this.get(path), value);
    }
  }

  /**
   * Subscribe to state changes (legacy StateManager compatible)
   */
  subscribe<T = any>(
    path: string, 
    callback: (event: StateChangeEvent<T>) => void
  ): () => void {
    // Add to local listeners
    if (!this.pathListeners.has(path)) {
      this.pathListeners.set(path, new Set());
    }
    this.pathListeners.get(path)!.add(callback);

    // Subscribe to Zustand store
    const unsubscribe = useStore.subscribe(
      (state) => this.getValueByPath(state, path),
      (newValue, oldValue) => {
        const event: StateChangeEvent<T> = {
          path,
          oldValue,
          newValue,
          state: useStore.getState()
        };
        callback(event);
      }
    );

    // Return unsubscribe function
    return () => {
      this.pathListeners.get(path)?.delete(callback);
      unsubscribe();
    };
  }

  /**
   * Save state (Zustand handles this automatically)
   */
  save(): void {
    // Zustand persist middleware handles this
    console.log('State saved automatically by Zustand');
  }

  /**
   * Clear all state
   */
  clear(): void {
    useStore.getState().reset();
  }

  /**
   * Initialize state (for compatibility)
   */
  init(_defaultState?: any): void {
    // Zustand handles initialization
    useStore.getState().hydrate();
    EventBus.emit('state:initialized', useStore.getState());
  }

  // Private helper methods

  private setupStoreSubscriptions(): void {
    // Subscribe to all state changes and emit events
    useStore.subscribe((state, prevState) => {
      // Find what changed and emit appropriate events
      this.detectChanges(prevState, state, '');
    });
  }

  private detectChanges(oldObj: any, newObj: any, path: string): void {
    const keys = new Set([...Object.keys(oldObj || {}), ...Object.keys(newObj || {})]);
    
    keys.forEach(key => {
      const currentPath = path ? `${path}.${key}` : key;
      const oldValue = oldObj?.[key];
      const newValue = newObj?.[key];
      
      if (oldValue !== newValue) {
        if (typeof newValue === 'object' && newValue !== null && !Array.isArray(newValue)) {
          // Recurse for nested objects
          this.detectChanges(oldValue || {}, newValue, currentPath);
        } else {
          // Emit change event
          this.emitChange(currentPath, oldValue, newValue);
        }
      }
    });
  }

  private emitChange(path: string, oldValue: any, newValue: any): void {
    const event: StateChangeEvent = {
      path,
      oldValue,
      newValue,
      state: useStore.getState()
    };
    
    // Emit to EventBus for backward compatibility
    EventBus.emit('state:changed', event);
    
    // Notify path-specific listeners
    this.notifyPathListeners(path, event);
  }

  private notifyPathListeners(path: string, event: StateChangeEvent): void {
    // Direct path listeners
    this.pathListeners.get(path)?.forEach(callback => {
      try {
        callback(event);
      } catch (error) {
        console.error('Error in state listener:', error);
      }
    });
    
    // Wildcard listeners
    this.pathListeners.forEach((callbacks, pattern) => {
      if (pattern !== path && this.matchPath(pattern, path)) {
        callbacks.forEach(callback => {
          try {
            callback(event);
          } catch (error) {
            console.error('Error in state listener:', error);
          }
        });
      }
    });
  }

  private matchPath(pattern: string, path: string): boolean {
    if (pattern === '*') return true;
    if (pattern === path) return true;
    
    const patternParts = pattern.split('.');
    const pathParts = path.split('.');
    
    for (let i = 0; i < patternParts.length; i++) {
      if (patternParts[i] === '*') return true;
      if (patternParts[i] !== pathParts[i]) return false;
    }
    
    return true;
  }

  private getValueByPath(obj: any, path: string): any {
    const keys = path.split('.');
    let value = obj;
    
    for (const key of keys) {
      if (value && typeof value === 'object' && key in value) {
        value = value[key];
      } else {
        return undefined;
      }
    }
    
    return value;
  }

  // Specific update handlers

  private handleCalculatorUpdate(keys: string[], value: any): void {
    const store = useStore.getState();
    
    switch (keys[0]) {
      case 'orderValue':
        store.setOrderValue(value);
        store.updateCalculation();
        break;
      case 'leadTime':
        store.setLeadTime(value);
        store.updateCalculation();
        break;
      case 'pickup':
        store.setPickup(value);
        store.updateCalculation();
        break;
      case 'chain':
        store.setChain(value);
        store.updateCalculation();
        break;
    }
  }

  private handleCustomerUpdate(keys: string[], value: any): void {
    const store = useStore.getState();
    
    if (keys[0] === 'data') {
      if (keys.length === 1) {
        // Setting entire customer data
        store.setCustomerData(value);
      } else {
        // Setting specific field
        const field = keys[1];
        store.setCustomerData({ [field]: value });
      }
    } else if (keys[0] === 'customerType') {
      store.setCustomerType(value);
    } else if (keys[0] === 'industry') {
      store.setIndustry(value);
    }
  }

  private handleSettingsUpdate(keys: string[], value: any): void {
    const store = useStore.getState();
    
    if (keys[0] === 'salesperson') {
      if (keys.length === 1) {
        store.updateSalesperson(value);
      } else {
        store.updateSalesperson({ [keys[1]]: value });
      }
    } else if (keys[0] === 'integrations' && keys.length >= 2) {
      const integration = keys[1] as keyof Store['settings']['integrations'];
      if (keys.length === 2) {
        store.updateIntegration(integration, value);
      } else {
        store.updateIntegration(integration, { [keys[2]]: value });
      }
    }
  }

  private handleUIUpdate(keys: string[], value: any): void {
    const store = useStore.getState();
    
    switch (keys[0]) {
      case 'currentTab':
        store.setCurrentTab(value);
        break;
      case 'loading':
        store.setLoading(value);
        break;
      case 'error':
        store.setError(value);
        break;
    }
  }

  private handleI18nUpdate(keys: string[], value: any): void {
    const store = useStore.getState();
    
    if (keys[0] === 'currentLanguage') {
      store.setLanguage(value);
    } else if (keys[0] === 'autoUpdate') {
      if (value !== store.i18n.autoUpdate) {
        store.toggleAutoUpdate();
      }
    }
  }
}

// Create singleton instance that mimics StateManager API
const adapter = StoreAdapter.getInstance();

export default {
  get: adapter.get.bind(adapter),
  set: adapter.set.bind(adapter),
  subscribe: adapter.subscribe.bind(adapter),
  save: adapter.save.bind(adapter),
  clear: adapter.clear.bind(adapter),
  init: adapter.init.bind(adapter),
  
  // Additional Zustand-specific exports
  useStore,
  getState: () => useStore.getState(),
  setState: useStore.setState
};