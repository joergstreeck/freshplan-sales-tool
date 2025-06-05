/**
 * Domain Events für FreshPlan 2.0
 * Event-Driven Architecture Foundation
 */

export abstract class DomainEvent {
  public readonly occurredAt: Date;
  public readonly eventId: string;
  public readonly userId?: string;
  public readonly tenantId?: string;

  constructor() {
    this.occurredAt = new Date();
    this.eventId = crypto.randomUUID();
    // User/Tenant aus Context
  }

  abstract get eventType(): string;
}

// Customer Events
export class CustomerCreatedEvent extends DomainEvent {
  constructor(
    public readonly customerId: string,
    public readonly customerData: any
  ) {
    super();
  }

  get eventType() {
    return 'customer.created';
  }
}

export class CustomerUpdatedEvent extends DomainEvent {
  constructor(
    public readonly customerId: string,
    public readonly changes: any,
    public readonly previousData?: any
  ) {
    super();
  }

  get eventType() {
    return 'customer.updated';
  }
}

// Calculation Events
export class CalculationPerformedEvent extends DomainEvent {
  constructor(
    public readonly calculationId: string,
    public readonly customerId: string,
    public readonly result: {
      orderValue: number;
      totalDiscount: number;
      finalPrice: number;
    }
  ) {
    super();
  }

  get eventType() {
    return 'calculation.performed';
  }
}

// Sales Activity Events
export class CustomerVisitScheduledEvent extends DomainEvent {
  constructor(
    public readonly visitId: string,
    public readonly customerId: string,
    public readonly scheduledAt: Date,
    public readonly notes?: string
  ) {
    super();
  }

  get eventType() {
    return 'visit.scheduled';
  }
}

export class CustomerVisitCompletedEvent extends DomainEvent {
  constructor(
    public readonly visitId: string,
    public readonly outcome: 'successful' | 'rescheduled' | 'cancelled',
    public readonly notes?: string,
    public readonly nextActions?: string[]
  ) {
    super();
  }

  get eventType() {
    return 'visit.completed';
  }
}

// Lead Events
export class LeadQualifiedEvent extends DomainEvent {
  constructor(
    public readonly leadId: string,
    public readonly score: number,
    public readonly qualificationCriteria: Record<string, any>
  ) {
    super();
  }

  get eventType() {
    return 'lead.qualified';
  }
}

// Offline Sync Events
export class DataSyncRequiredEvent extends DomainEvent {
  constructor(
    public readonly entityType: string,
    public readonly entityId: string,
    public readonly localVersion: number,
    public readonly remoteVersion?: number
  ) {
    super();
  }

  get eventType() {
    return 'sync.required';
  }
}

export class DataConflictDetectedEvent extends DomainEvent {
  constructor(
    public readonly entityType: string,
    public readonly entityId: string,
    public readonly localData: any,
    public readonly remoteData: any,
    public readonly resolution?: 'local' | 'remote' | 'merge'
  ) {
    super();
  }

  get eventType() {
    return 'sync.conflict';
  }
}