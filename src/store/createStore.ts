/**
 * Store Factory for creating isolated store instances
 * Useful for testing and multiple app instances
 */

import { create } from 'zustand';
import { devtools, persist, subscribeWithSelector } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import type { 
  AppState, 
  CustomerState, 
  SettingsState,
  ProfileState,
  I18nState,
  UIState,
  Notification,
  CalculatorCalculation
} from '@/types';

// Import initial state and actions from main store
import { initialState } from './initialState';
import { createActions } from './actions';

export interface StoreState extends AppState {
  // Actions are included in the state
  setOrderValue: (value: number) => void;
  setLeadTime: (value: number) => void;
  setPickup: (pickup: boolean) => void;
  setChain: (chain: boolean) => void;
  updateCalculation: (calculation?: CalculatorCalculation | null) => void;
  
  // Customer actions
  setCustomerData: (data: Partial<CustomerState['data']>) => void;
  setCustomerType: (type: CustomerState['customerType']) => void;
  setIndustry: (industry: CustomerState['industry']) => void;
  saveCustomer: () => void;
  clearCustomer: () => void;
  
  // Settings actions
  updateSettings: (settings: Partial<SettingsState>) => void;
  updateSalesperson: (info: Partial<SettingsState['salesperson']>) => void;
  updateIntegration: <K extends keyof SettingsState['integrations']>(
    integration: K,
    data: Partial<SettingsState['integrations'][K]>
  ) => void;
  
  // Profile actions
  setProfileData: (data: ProfileState['data']) => void;
  generateProfile: () => void;
  
  // PDF actions
  setPDFAvailable: (available: boolean) => void;
  setCurrentPDF: (pdf: any, filename: string) => void;
  clearPDF: () => void;
  
  // i18n actions
  setLanguage: (language: I18nState['currentLanguage']) => void;
  toggleAutoUpdate: () => void;
  
  // UI actions
  setCurrentTab: (tab: UIState['currentTab']) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: UIState['error']) => void;
  clearError: () => void;
  addNotification: (notification: Omit<Notification, 'id' | 'timestamp'>) => void;
  removeNotification: (id: string) => void;
  clearNotifications: () => void;
  
  // Global actions
  reset: () => void;
  hydrate: () => void;
}

export interface StoreOptions {
  persist?: boolean;
  devtools?: boolean;
  name?: string;
}

/**
 * Create a new store instance
 */
export const createStore = (options: StoreOptions = {}) => {
  const {
    persist: enablePersist = false,
    devtools: enableDevtools = false,
    name = 'freshplan-store'
  } = options;

  // Build store with middlewares
  const storeBuilder = (set: any, get: any) => ({
    ...initialState,
    ...createActions(set, get)
  });

  // Apply middlewares conditionally
  if (enablePersist && enableDevtools) {
    return create<StoreState>()(
      subscribeWithSelector(
        persist(
          devtools(
            immer(storeBuilder),
            { name }
          ),
          {
            name,
            partialize: (state) => ({
              calculator: state.calculator,
              customer: state.customer,
              settings: state.settings,
              profile: state.profile,
              i18n: state.i18n
            })
          }
        )
      )
    );
  } else if (enablePersist) {
    return create<StoreState>()(
      subscribeWithSelector(
        persist(
          immer(storeBuilder),
          {
            name,
            partialize: (state) => ({
              calculator: state.calculator,
              customer: state.customer,
              settings: state.settings,
              profile: state.profile,
              i18n: state.i18n
            })
          }
        )
      )
    );
  } else if (enableDevtools) {
    return create<StoreState>()(
      subscribeWithSelector(
        devtools(
          immer(storeBuilder),
          { name }
        )
      )
    );
  } else {
    return create<StoreState>()(
      subscribeWithSelector(
        immer(storeBuilder)
      )
    );
  }
};

/**
 * Create a test store with no persistence
 */
export const createTestStore = () => {
  return createStore({
    persist: false,
    devtools: false,
    name: `test-store-${Date.now()}`
  });
};