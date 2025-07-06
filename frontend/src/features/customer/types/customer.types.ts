// Customer types based on backend DTOs

export interface CustomerResponse {
  id: string;
  customerNumber: string;
  companyName: string;
  tradingName?: string;
  legalForm?: string;
  customerType: CustomerType;
  industry?: Industry;
  classification?: Classification;
  parentCustomerId?: string;
  hierarchyType?: CustomerHierarchyType;
  childCustomerIds: string[];
  hasChildren: boolean;
  status: CustomerStatus;
  lifecycleStage?: CustomerLifecycleStage;
  partnerStatus?: PartnerStatus;
  expectedAnnualVolume?: number;
  actualAnnualVolume?: number;
  paymentTerms?: PaymentTerms;
  creditLimit?: number;
  deliveryCondition?: DeliveryCondition;
  riskScore: number;
  atRisk: boolean;
  lastContactDate?: string;
  nextFollowUpDate?: string;
  createdAt: string;
  createdBy: string;
  updatedAt?: string;
  updatedBy?: string;
  isDeleted: boolean;
  deletedAt?: string;
  deletedBy?: string;
}

export interface CustomerListResponse {
  content: CustomerResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// Enums
export enum CustomerType {
  UNTERNEHMEN = 'UNTERNEHMEN',
  EINZELUNTERNEHMEN = 'EINZELUNTERNEHMEN',
  FILIALE = 'FILIALE',
  OEFFENTLICH = 'OEFFENTLICH',
  SONSTIGES = 'SONSTIGES',
}

export enum Industry {
  HOTEL = 'HOTEL',
  RESTAURANT = 'RESTAURANT',
  CAFE = 'CAFE',
  BAR = 'BAR',
  KANTINE = 'KANTINE',
  CATERING = 'CATERING',
  BILDUNG = 'BILDUNG',
  GESUNDHEIT = 'GESUNDHEIT',
  EINZELHANDEL = 'EINZELHANDEL',
  SONSTIGES = 'SONSTIGES',
}

export enum Classification {
  A_KUNDE = 'A_KUNDE',
  B_KUNDE = 'B_KUNDE',
  C_KUNDE = 'C_KUNDE',
  NEUKUNDE = 'NEUKUNDE',
  VIP = 'VIP',
}

export enum CustomerStatus {
  LEAD = 'LEAD',
  PROSPECT = 'PROSPECT',
  AKTIV = 'AKTIV',
  RISIKO = 'RISIKO',
  INAKTIV = 'INAKTIV',
  ARCHIVIERT = 'ARCHIVIERT',
}

export enum CustomerHierarchyType {
  STANDALONE = 'STANDALONE',
  HEADQUARTER = 'HEADQUARTER',
  FILIALE = 'FILIALE',
  FRANCHISE = 'FRANCHISE',
}

export enum CustomerLifecycleStage {
  ACQUISITION = 'ACQUISITION',
  GROWTH = 'GROWTH',
  MAINTENANCE = 'MAINTENANCE',
  REACTIVATION = 'REACTIVATION',
}

export enum PartnerStatus {
  KEIN_PARTNER = 'KEIN_PARTNER',
  ANFRAGE = 'ANFRAGE',
  IN_PRUEFUNG = 'IN_PRUEFUNG',
  AKTIV = 'AKTIV',
  VERLAENGERUNG_FAELLIG = 'VERLAENGERUNG_FAELLIG',
  GEKUENDIGT = 'GEKUENDIGT',
}

export enum PaymentTerms {
  SOFORT = 'SOFORT',
  NETTO_7 = 'NETTO_7',
  NETTO_14 = 'NETTO_14',
  NETTO_30 = 'NETTO_30',
  NETTO_60 = 'NETTO_60',
  SONDERKONDITION = 'SONDERKONDITION',
}

export enum DeliveryCondition {
  STANDARD = 'STANDARD',
  EXPRESS = 'EXPRESS',
  SELBSTABHOLUNG = 'SELBSTABHOLUNG',
  TERMINLIEFERUNG = 'TERMINLIEFERUNG',
  KUEHLTRANSPORT = 'KUEHLTRANSPORT',
}

// Translations for display
export const customerTypeLabels: Record<CustomerType, string> = {
  [CustomerType.UNTERNEHMEN]: 'Unternehmen',
  [CustomerType.EINZELUNTERNEHMEN]: 'Einzelunternehmen',
  [CustomerType.FILIALE]: 'Filiale',
  [CustomerType.OEFFENTLICH]: 'Öffentlich',
  [CustomerType.SONSTIGES]: 'Sonstiges',
};

export const customerStatusLabels: Record<CustomerStatus, string> = {
  [CustomerStatus.LEAD]: 'Lead',
  [CustomerStatus.PROSPECT]: 'Prospect',
  [CustomerStatus.AKTIV]: 'Aktiv',
  [CustomerStatus.RISIKO]: 'Risiko',
  [CustomerStatus.INAKTIV]: 'Inaktiv',
  [CustomerStatus.ARCHIVIERT]: 'Archiviert',
};

export const industryLabels: Record<Industry, string> = {
  [Industry.HOTEL]: 'Hotel',
  [Industry.RESTAURANT]: 'Restaurant',
  [Industry.CAFE]: 'Café',
  [Industry.BAR]: 'Bar',
  [Industry.KANTINE]: 'Kantine',
  [Industry.CATERING]: 'Catering',
  [Industry.BILDUNG]: 'Bildung',
  [Industry.GESUNDHEIT]: 'Gesundheit',
  [Industry.EINZELHANDEL]: 'Einzelhandel',
  [Industry.SONSTIGES]: 'Sonstiges',
};

// Status color mappings
export const customerStatusColors: Record<CustomerStatus, string> = {
  [CustomerStatus.LEAD]: '#2196F3', // Blue
  [CustomerStatus.PROSPECT]: '#FF9800', // Orange
  [CustomerStatus.AKTIV]: '#4CAF50', // Green
  [CustomerStatus.RISIKO]: '#F44336', // Red
  [CustomerStatus.INAKTIV]: '#9E9E9E', // Gray
  [CustomerStatus.ARCHIVIERT]: '#607D8B', // Blue Gray
};
