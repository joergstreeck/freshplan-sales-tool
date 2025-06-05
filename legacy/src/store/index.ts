/**
 * Zustand Store - Modern State Management
 * Replaces the old StateManager with a more efficient solution
 */

// Type utilities
export type DeepPartial<T> = T extends object ? {
  [P in keyof T]?: DeepPartial<T[P]>;
} : T;

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
  CalculatorCalculation,
  LocationData,
  CustomerData
} from '../types';

// Store Actions
interface AppActions {
  // Calculator actions
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
  updateCustomer: (data: Partial<CustomerData>) => void;
  
  // Locations actions
  updateLocations: (locations: LocationData[]) => void;
  setTotalLocations: (total: number) => void;
  setCaptureDetails: (capture: boolean) => void;
  
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

// Combine state and actions
export type Store = AppState & AppActions;

// Initial state
const initialState: AppState = {
  app: {
    version: '3.0.0',
    initialized: false,
    environment: process.env.NODE_ENV === 'development' ? 'development' : 'production'
  },
  calculator: {
    orderValue: 15000,
    leadTime: 14,
    pickup: false,
    chain: false,
    calculation: null
  },
  customer: {
    data: null,
    customerType: 'single',
    industry: '',
    isDirty: false,
    isValid: true
  },
  settings: {
    salesperson: {
      name: '',
      email: '',
      phone: '',
      mobile: ''
    },
    defaults: {
      discount: 15,
      contractDuration: 24
    },
    integrations: {
      monday: { token: '', boardId: '' },
      email: { smtpServer: '', smtpEmail: '', smtpPassword: '' },
      xentral: { url: '', key: '' }
    }
  },
  profile: {
    data: null
  },
  pdf: {
    available: true,
    currentPDF: null,
    filename: null
  },
  i18n: {
    currentLanguage: 'de',
    availableLanguages: ['de', 'en'],
    autoUpdate: true
  },
  ui: {
    currentTab: 'demonstrator',
    loading: false,
    error: null,
    notifications: []
  },
  locations: {
    locations: [],
    totalLocations: 0,
    captureDetails: false
  }
};

// Create store with middleware
export const useStore = create<Store>()(
  devtools(
    persist(
      subscribeWithSelector(
        immer((set, get) => ({
          ...initialState,

          // Calculator actions
          setOrderValue: (value) => set((state) => {
            state.calculator.orderValue = value;
          }),

          setLeadTime: (value) => set((state) => {
            state.calculator.leadTime = value;
          }),

          setPickup: (pickup) => set((state) => {
            state.calculator.pickup = pickup;
          }),

          setChain: (chain) => set((state) => {
            state.calculator.chain = chain;
          }),

          updateCalculation: (calculation) => set((state) => {
            if (calculation !== undefined) {
              // If calculation is provided, use it directly
              state.calculator.calculation = calculation;
            } else {
              // Otherwise, calculate it
              const { orderValue, leadTime, pickup } = state.calculator;
              
              // Calculate discounts (same logic as before)
              const baseRules = [
                { min: 75000, discount: 10 },
                { min: 50000, discount: 9 },
                { min: 30000, discount: 8 },
                { min: 15000, discount: 6 },
                { min: 5000, discount: 3 },
                { min: 0, discount: 0 }
              ];

              const earlyRules = [
                { days: 30, discount: 3 },
                { days: 15, discount: 2 },
                { days: 10, discount: 1 },
                { days: 0, discount: 0 }
              ];

              let baseDiscount = 0;
              for (const rule of baseRules) {
                if (orderValue >= rule.min) {
                  baseDiscount = rule.discount;
                  break;
                }
              }

              let earlyDiscount = 0;
              for (const rule of earlyRules) {
                if (leadTime >= rule.days) {
                  earlyDiscount = rule.discount;
                  break;
                }
              }

              const pickupDiscount = (pickup && orderValue >= 5000) ? 2 : 0;
              const chainDiscount = 0; // Always 0

              const totalDiscount = baseDiscount + earlyDiscount + pickupDiscount + chainDiscount;
              const discountAmount = orderValue * (totalDiscount / 100);
              const finalPrice = orderValue - discountAmount;
              const savingsAmount = discountAmount;

              state.calculator.calculation = {
                baseDiscount,
                earlyDiscount,
                pickupDiscount,
                chainDiscount,
                totalDiscount,
                discountAmount,
                finalPrice,
                orderValue,
                leadTime,
                savingsAmount,
                pickup,
                chain: state.calculator.chain
              };
            }
          }),

          // Customer actions
          setCustomerData: (data) => set((state) => {
            if (!state.customer.data) {
              state.customer.data = {} as CustomerState['data'];
            }
            if (state.customer.data) {
              Object.assign(state.customer.data, data);
            }
            state.customer.isDirty = true;
          }),

          setCustomerType: (type) => set((state) => {
            state.customer.customerType = type;
            if (state.customer.data) {
              state.customer.data.customerType = type;
            }
          }),

          setIndustry: (industry) => set((state) => {
            state.customer.industry = industry;
            if (state.customer.data) {
              state.customer.data.industry = industry;
            }
          }),

          saveCustomer: () => set((state) => {
            state.customer.isDirty = false;
            // In a real app, this would also persist to backend
          }),

          clearCustomer: () => set((state) => {
            state.customer.data = null;
            state.customer.customerType = 'single';
            state.customer.industry = '';
            state.customer.isDirty = false;
            state.customer.isValid = true;
          }),
          
          updateCustomer: (data) => set((state) => {
            if (!state.customer.data) {
              state.customer.data = { 
                companyName: '', 
                contactName: '', 
                contactEmail: '', 
                contactPhone: '', 
                street: '', 
                postalCode: '', 
                city: '',
                industry: state.customer.industry,
                customerType: state.customer.customerType
              } as CustomerData;
            }
            Object.assign(state.customer.data, data);
            state.customer.isDirty = true;
          }),

          // Locations actions
          updateLocations: (locations) => set((state) => {
            state.locations.locations = locations;
          }),

          setTotalLocations: (total) => set((state) => {
            state.locations.totalLocations = total;
          }),

          setCaptureDetails: (capture) => set((state) => {
            state.locations.captureDetails = capture;
          }),

          // Settings actions
          updateSettings: (settings) => set((state) => {
            Object.assign(state.settings, settings);
          }),

          updateSalesperson: (info) => set((state) => {
            Object.assign(state.settings.salesperson, info);
          }),

          updateIntegration: (integration, data) => set((state) => {
            Object.assign(state.settings.integrations[integration], data);
          }),

          // Profile actions
          setProfileData: (data) => set((state) => {
            state.profile.data = data;
          }),

          generateProfile: () => set((_state) => {
            // Profile generation logic would go here
            console.log('Generating profile from customer data...');
          }),

          // PDF actions
          setPDFAvailable: (available) => set((state) => {
            state.pdf.available = available;
          }),

          setCurrentPDF: (pdf, filename) => set((state) => {
            state.pdf.currentPDF = pdf;
            state.pdf.filename = filename;
          }),

          clearPDF: () => set((state) => {
            state.pdf.currentPDF = null;
            state.pdf.filename = null;
          }),

          // i18n actions
          setLanguage: (language) => set((state) => {
            state.i18n.currentLanguage = language;
          }),

          toggleAutoUpdate: () => set((state) => {
            state.i18n.autoUpdate = !state.i18n.autoUpdate;
          }),

          // UI actions
          setCurrentTab: (tab) => set((state) => {
            state.ui.currentTab = tab;
          }),

          setLoading: (loading) => set((state) => {
            state.ui.loading = loading;
          }),

          setError: (error) => set((state) => {
            state.ui.error = error;
          }),

          clearError: () => set((state) => {
            state.ui.error = null;
          }),

          addNotification: (notification) => set((state) => {
            const newNotification: Notification = {
              ...notification,
              id: `notif-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
              timestamp: Date.now()
            };
            
            state.ui.notifications.push(newNotification);
            
            // Auto-remove after duration
            if (notification.duration && notification.duration > 0) {
              setTimeout(() => {
                get().removeNotification(newNotification.id);
              }, notification.duration);
            }
          }),

          removeNotification: (id) => set((state) => {
            state.ui.notifications = state.ui.notifications.filter((n: Notification) => n.id !== id);
          }),

          clearNotifications: () => set((state) => {
            state.ui.notifications = [];
          }),

          // Global actions
          reset: () => set(() => initialState),

          hydrate: () => {
            // This is called by the persist middleware
            set((state) => {
              state.app.initialized = true;
            });
          }
        }))
      ),
      {
        name: 'freshplan-store',
        version: 1,
        partialize: (state) => ({
          // Only persist certain parts of the state
          calculator: state.calculator,
          customer: state.customer,
          settings: state.settings,
          profile: state.profile,
          i18n: {
            currentLanguage: state.i18n.currentLanguage,
            autoUpdate: state.i18n.autoUpdate
          },
          locations: state.locations
        })
      }
    ),
    {
      name: 'FreshPlan Store'
    }
  )
);

// Selectors for common state slices
export const selectors = {
  // Calculator selectors
  getCalculation: (state: Store) => state.calculator.calculation,
  getTotalDiscount: (state: Store) => state.calculator.calculation?.totalDiscount ?? 0,
  getFinalPrice: (state: Store) => state.calculator.calculation?.finalPrice ?? 0,
  
  // Customer selectors
  getCustomerData: (state: Store) => state.customer.data,
  isCustomerValid: (state: Store) => state.customer.isValid && !state.customer.isDirty,
  getCustomerName: (state: Store) => state.customer.data?.companyName ?? '',
  
  // Settings selectors
  getSalesperson: (state: Store) => state.settings.salesperson,
  getDefaultDiscount: (state: Store) => state.settings.defaults.discount,
  
  // UI selectors
  getCurrentTab: (state: Store) => state.ui.currentTab,
  isLoading: (state: Store) => state.ui.loading,
  getNotifications: (state: Store) => state.ui.notifications,
  hasError: (state: Store) => state.ui.error !== null
};

// Computed values hook
export const useComputedValues = () => {
  const calculation = useStore(selectors.getCalculation);
  const customerData = useStore(selectors.getCustomerData);
  
  return {
    hasDiscount: calculation && calculation.totalDiscount > 0,
    isChainCustomer: customerData?.customerType === 'chain',
    canGenerateOffer: customerData !== null && customerData.companyName !== '',
    savingsPerYear: calculation ? calculation.discountAmount * 12 : 0
  };
};

// Action hooks for components
export const useCalculatorActions = () => {
  const setOrderValue = useStore(state => state.setOrderValue);
  const setLeadTime = useStore(state => state.setLeadTime);
  const setPickup = useStore(state => state.setPickup);
  const setChain = useStore(state => state.setChain);
  const updateCalculation = useStore(state => state.updateCalculation);
  
  return {
    updateOrderValue: (value: number) => {
      setOrderValue(value);
      updateCalculation();
    },
    updateLeadTime: (value: number) => {
      setLeadTime(value);
      updateCalculation();
    },
    updatePickup: (pickup: boolean) => {
      setPickup(pickup);
      updateCalculation();
    },
    updateChain: (chain: boolean) => {
      setChain(chain);
      updateCalculation();
    }
  };
};

// Notification hook
export const useNotifications = () => {
  const notifications = useStore(state => state.ui.notifications);
  const addNotification = useStore(state => state.addNotification);
  const removeNotification = useStore(state => state.removeNotification);
  
  return {
    notifications,
    success: (message: string, duration = 3000) => 
      addNotification({ type: 'success', message, duration }),
    error: (message: string, duration = 5000) => 
      addNotification({ type: 'error', message, duration }),
    warning: (message: string, duration = 4000) => 
      addNotification({ type: 'warning', message, duration }),
    info: (message: string, duration = 3000) => 
      addNotification({ type: 'info', message, duration }),
    remove: removeNotification
  };
};