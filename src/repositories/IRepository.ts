/**
 * Repository Pattern Interfaces für FreshPlan 2.0
 * Ermöglicht nahtlosen Wechsel zwischen LocalStorage und API
 */

export interface IRepository<T> {
  findById(id: string): Promise<T | null>;
  findAll(): Promise<T[]>;
  save(entity: T): Promise<T>;
  delete(id: string): Promise<void>;
}

export interface ICustomerRepository extends IRepository<Customer> {
  findByEmail(email: string): Promise<Customer | null>;
  findByTenant(tenantId: string): Promise<Customer[]>;
  searchByName(query: string): Promise<Customer[]>;
}

export interface ICalculationRepository extends IRepository<Calculation> {
  findByCustomer(customerId: string): Promise<Calculation[]>;
  findByDateRange(start: Date, end: Date): Promise<Calculation[]>;
}

// Domain Models
export interface Customer {
  id: string;
  tenantId?: string;
  companyName: string;
  legalForm: string;
  customerType: 'neukunde' | 'bestandskunde';
  industry: string;
  createdAt: Date;
  updatedAt: Date;
  createdBy?: string;
  // ... rest of customer fields
}

export interface Calculation {
  id: string;
  customerId: string;
  orderValue: number;
  leadTime: number;
  isPickup: boolean;
  totalDiscount: number;
  createdAt: Date;
  // ... rest of calculation fields
}