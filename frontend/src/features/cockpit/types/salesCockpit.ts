/**
 * TypeScript-Typen für Sales Cockpit BFF-Daten
 *
 * Spiegelt die DTO-Struktur vom Backend wider
 */

/**
 * Hauptcontainer für alle Dashboard-Daten
 */
export interface SalesCockpitDashboard {
  todaysTasks: DashboardTask[];
  riskCustomers: RiskCustomer[];
  statistics: DashboardStatistics;
  alerts: DashboardAlert[];
}

/**
 * Eine Aufgabe im Dashboard
 */
export interface DashboardTask {
  id: string;
  title: string;
  description?: string;
  type: TaskType;
  priority: TaskPriority;
  customerId?: string;
  customerName?: string;
  dueDate?: string;
  completed: boolean;
}

export type TaskType = 'CALL' | 'EMAIL' | 'APPOINTMENT' | 'TODO' | 'FOLLOW_UP';
export type TaskPriority = 'HIGH' | 'MEDIUM' | 'LOW';

/**
 * Ein Risiko-Kunde
 */
export interface RiskCustomer {
  id: string;
  customerNumber: string;
  companyName: string;
  lastContactDate?: string;
  daysSinceLastContact: number;
  riskLevel: RiskLevel;
  riskReason: string;
  recommendedAction: string;
}

export type RiskLevel = 'HIGH' | 'MEDIUM' | 'LOW';

/**
 * Dashboard-Statistiken
 *
 * Sprint 2.1.7.4: Added prospects + conversionRate
 */
export interface DashboardStatistics {
  totalCustomers: number;
  activeCustomers: number;
  customersAtRisk: number;
  openTasks: number;
  overdueItems: number;
  prospects: number; // NEW: Status = PROSPECT
  conversionRate: number; // NEW: PROSPECT → AKTIV %
}

/**
 * KI-gestützter Alert
 */
export interface DashboardAlert {
  id: string;
  title: string;
  message: string;
  type: AlertType;
  severity: AlertSeverity;
  customerId?: string;
  customerName?: string;
  createdAt: string;
  actionLink?: string;
}

export type AlertType = 'OPPORTUNITY' | 'RISK' | 'REMINDER' | 'INFO';
export type AlertSeverity = 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO';
