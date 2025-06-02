/**
 * Core type definitions for FreshPlan Sales Tool
 */

// State types
export interface AppState {
  app: AppConfig;
  calculator: CalculatorState;
  customer: CustomerState;
  settings: SettingsState;
  profile: ProfileState;
  pdf: PDFState;
  i18n: I18nState;
  ui: UIState;
  locations: LocationsState;
}

// Locations state
export interface LocationsState {
  locations: LocationData[];
  totalLocations: number;
  captureDetails: boolean;
}

export interface AppConfig {
  version: string;
  initialized: boolean;
  environment: 'development' | 'production';
  language?: Language;
}

// Calculator types
export interface CalculatorState {
  orderValue: number;
  leadTime: number;
  pickup: boolean;
  chain: boolean;
  calculation: CalculatorCalculation | null;
}

export interface CalculationResult {
  baseDiscount: number;
  earlyDiscount: number;
  pickupDiscount: number;
  chainDiscount: number;
  totalDiscount: number;
  discountAmount: number;
  finalPrice: number;
  orderValue: number;
  leadTime: number;
  savingsAmount: number;
}

export interface CalculatorCalculation extends CalculationResult {
  pickup: boolean;
  chain: boolean;
}

export interface DiscountRule {
  min: number;
  discount: number;
}

export interface EarlyBookingRule {
  days: number;
  discount: number;
}

// Customer types
export interface CustomerState {
  data: CustomerData | null;
  customerType: CustomerType;
  industry: Industry;
  isDirty: boolean;
  isValid: boolean;
}

export type CustomerType = 'single' | 'chain';

export type Industry = 
  | 'restaurant'
  | 'hotel'
  | 'krankenhaus'
  | 'altenheim'
  | 'betriebsrestaurant'
  | 'kette'
  | '';

export interface CustomerData {
  // Basic information
  companyName: string;
  legalForm?: 'gmbh' | 'ag' | 'gbr' | 'einzelunternehmen' | 'other';
  customerType: CustomerType | string; // Allow string for 'neukunde'/'bestandskunde'
  customerStatus?: 'neukunde' | 'bestandskunde'; // New field for customer status
  industry: Industry;
  
  // Registration data
  handelsregister?: string;
  ustId?: string;
  customerNumber?: string;
  companySize?: string;
  
  // Contact
  contactName: string;
  contactEmail: string;
  contactPhone: string;
  contactPosition?: string;
  
  // Address
  street: string;
  postalCode: string; // Changed from zipCode
  city: string;
  
  // Business data
  expectedVolume?: number;
  annualVolume?: number; // New field - erwartetes Jahresvolumen
  paymentTerms?: string;
  paymentMethod?: 'vorkasse' | 'bar' | 'rechnung'; // New field - Zahlungsart
  paymentHistory?: string;
  
  // Contract
  contractStart?: string;
  contractDuration?: number;
  contractEnd?: string;
  
  // Additional fields
  notes?: string; // New field - Notizen
  customField1?: string; // New field - Freifeld 1
  customField2?: string; // New field - Freifeld 2
  
  // Address alternative structure for integrations
  address?: {
    street?: string;
    zip?: string;
    city?: string;
    country?: string;
  };
  
  // Chain specific
  totalLocations?: number;
  avgOrderPerLocation?: number;
  
  // Industry specific
  rooms?: number; // Hotel
  occupancyRate?: number; // Hotel
  breakfastPrice?: number; // Hotel
  residents?: number; // Altenheim
  mealsPerDay?: number; // Altenheim
  beds?: number; // Klinik
  staffMeals?: number; // Klinik
  employees?: number; // Betriebsrestaurant
  utilizationRate?: number; // Betriebsrestaurant
  seats?: number; // Restaurant
  turnsPerDay?: number; // Restaurant
  operatingDays?: number; // Restaurant
  
  // Vending
  vendingInterest?: boolean;
  vendingLocations?: number;
  vendingDaily?: number;
  
  // Legacy field mappings
  zipCode?: string; // For backward compatibility
  
  // Chain customer
  chainCustomer?: 'ja' | 'nein';
  locations?: LocationData[];
}

/**
 * Location data for chain customers
 */
export interface LocationData {
  id: string;
  name: string;
  industry: string;
  
  // Address
  address: {
    street?: string;
    plz?: string;
    city?: string;
  };
  
  // Contact
  contact: {
    name?: string;
    phone?: string;
  };
  
  // Industry specific data
  industrySpecific: {
    size?: 'small' | 'medium' | 'large';
    // Hotel
    rooms?: number;
    breakfast?: boolean;
    restaurant?: boolean;
    roomservice?: boolean;
    banquet?: boolean;
    // Klinik
    beds?: number;
    privatePercent?: number;
    patientMeals?: boolean;
    staffMeals?: boolean;
    // Seniorenresidenz
    residents?: number;
    fullCatering?: boolean;
    partialCatering?: boolean;
    specialDiet?: boolean;
    // Betriebsrestaurant
    employees?: number;
    // Restaurant
    seats?: number;
    alacarte?: boolean;
    // Common service fields
    lunch?: boolean;
    dinner?: boolean;
  };
  
  // Additional fields
  bemerkungen?: string;
  vendingInterest?: string;
}

// Settings types
export interface SettingsState {
  salesperson: SalespersonInfo;
  defaults: DefaultSettings;
  integrations: Integrations;
}

export interface SalespersonInfo {
  name: string;
  email: string;
  phone: string;
  mobile: string;
}

export interface DefaultSettings {
  discount: number;
  contractDuration: number;
}

export interface Integrations {
  monday: MondayIntegration;
  email: EmailIntegration;
  xentral: XentralIntegration;
}

export interface MondayIntegration {
  token: string;
  boardId: string;
}

export interface EmailIntegration {
  smtpServer: string;
  smtpEmail: string;
  smtpPassword: string;
}

export interface XentralIntegration {
  url: string;
  key: string;
}

// Profile types
export interface ProfileState {
  data: ProfileData | null;
}

export interface ProfileData {
  customerData: CustomerData;
  industry?: IndustryConfig;
  analysis: ProfileAnalysis;
  recommendations: Recommendation[];
  strategy: SalesStrategy;
  generatedAt?: string;
}

export interface VolumeCalculation {
  monthly: number;
  annual: number;
  growth: number;
}

export interface ProfileAnalysis {
  estimatedVolume: number;
  monthlyVolume: number;
  potentialSavings: SavingsCalculation;
  growthPotential: number;
  vendingPotential: VendingPotential | null;
}

export interface SavingsCalculation {
  annual: number;
  monthly: number;
  percentage: number;
}

export interface VendingPotential {
  locations: number;
  dailyTransactions: number;
  monthlyRevenue: number;
  monthlyProfit: number;
}

export interface Recommendation {
  type: string;
  title: string;
  description: string;
}

export interface SalesStrategy {
  approach: string;
  keyPoints: string[];
  painPoints: string[];
}

// PDF types
export interface PDFState {
  available: boolean;
  currentPDF: any | null;
  filename: string | null;
}

// i18n types
export interface I18nState {
  currentLanguage: Language;
  availableLanguages: Language[];
  autoUpdate: boolean;
}

export type Language = 'de' | 'en';

export interface TranslationParams {
  [key: string]: string | number;
}

// UI types
export interface UIState {
  currentTab: TabName;
  loading: boolean;
  error: ErrorState | null;
  notifications: Notification[];
}

export type TabName = 
  | 'demonstrator'
  | 'customer'
  | 'profile'
  | 'offer'
  | 'settings';

export interface ErrorState {
  message: string;
  code?: string;
  details?: any;
}

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  timestamp: number;
  duration?: number;
}

// Event types
export interface AppEvent<T = any> {
  type: string;
  payload: T;
  timestamp: number;
  source?: string;
}

// Module types
export interface ModuleConfig {
  name: string;
  version?: string;
  dependencies?: string[];
}

// Industry configs
export interface IndustryConfig {
  name: string;
  avgPrices?: {
    standard: number;
    premium?: number;
  };
  specificFields?: string[];
}

// Validation types
export interface ValidationRule {
  required?: boolean;
  min?: number;
  max?: number;
  pattern?: RegExp;
  custom?: (value: any) => boolean | string;
}

export interface ValidationResult {
  isValid: boolean;
  errors: ValidationError[];
}

export interface ValidationError {
  field: string;
  message: string;
  rule?: string;
}