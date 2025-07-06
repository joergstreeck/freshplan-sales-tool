/**
 * Type definitions for Sales Cockpit
 */

export interface Customer {
  id: string;
  companyName: string;
  status: 'active' | 'inactive' | 'prospect';
  contactPerson?: string;
  email?: string;
  phone?: string;
  lastContact?: Date;
  revenue?: number;
}

export interface PriorityTask {
  id: string;
  type: 'alert' | 'appointment' | 'call' | 'todo';
  title: string;
  description?: string;
  customerId?: string;
  customerName?: string;
  priority: 'high' | 'medium' | 'low';
  dueDate?: Date;
  completed: boolean;
}

export interface TriageItem {
  id: string;
  type: 'email' | 'call' | 'message';
  subject: string;
  from: string;
  content?: string;
  receivedAt: Date;
  processed: boolean;
}

export interface Activity {
  id: string;
  type: 'email' | 'call' | 'meeting' | 'note';
  description: string;
  date: Date;
  customerId: string;
  userId?: string;
}

export interface SavedView {
  id: string;
  name: string;
  filters: CustomerFilter;
  count: number;
}

export interface CustomerFilter {
  status?: Customer['status'][];
  search?: string;
  tags?: string[];
  lastContactDays?: number;
  revenueRange?: {
    min?: number;
    max?: number;
  };
}

export type CockpitColumn = 'my-day' | 'focus-list' | 'action-center';
export type ViewMode = 'list' | 'cards' | 'kanban';
export type ProcessType = 'new-customer' | 'offer' | 'follow-up' | 'renewal';