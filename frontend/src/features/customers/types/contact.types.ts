/**
 * Contact Type Definitions
 * 
 * Types for managing customer contacts and their relationships.
 * Supports Sprint 2 V2 with location responsibility mapping.
 */

export interface Contact {
  id: string;
  
  // Name fields
  salutation: 'Herr' | 'Frau' | 'Divers';
  title?: string;
  firstName: string;
  lastName: string;
  
  // Professional info
  position: string;
  decisionLevel: 'decision_maker' | 'influencer' | 'gatekeeper' | 'user';
  
  // Contact details
  email: string;
  phone?: string;
  mobile?: string;
  
  // Relationship
  isPrimary: boolean;
  notes?: string;
  
  // Timestamps
  createdAt?: Date;
  updatedAt?: Date;
}

export interface ContactWithResponsibility extends Contact {
  responsibilityScope: 'all' | 'specific';
  assignedLocationIds: string[];
}

export type DecisionLevel = Contact['decisionLevel'];
export type Salutation = Contact['salutation'];