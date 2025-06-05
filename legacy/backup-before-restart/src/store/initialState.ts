/**
 * Initial state for the store
 */

import type { AppState } from '@/types';

export const initialState: AppState = {
  app: {
    version: '3.0.0',
    initialized: false,
    environment: process.env.NODE_ENV as 'development' | 'production',
    language: 'de'
  },
  
  calculator: {
    orderValue: 5000,
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
      monday: {
        token: '',
        boardId: ''
      },
      email: {
        smtpServer: '',
        smtpEmail: '',
        smtpPassword: ''
      },
      xentral: {
        url: '',
        key: ''
      }
    }
  },
  
  profile: {
    data: null
  },
  
  pdf: {
    available: false,
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