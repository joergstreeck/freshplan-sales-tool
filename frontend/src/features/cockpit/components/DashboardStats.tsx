/**
 * Dashboard Statistiken Widget
 *
 * Zeigt die wichtigsten Sales-Kennzahlen aus dem BFF an
 */
import type { DashboardStatistics } from '../types/salesCockpit';
// CSS import removed - migrating to MUI sx props

interface DashboardStatsProps {
  statistics: DashboardStatistics;
  loading?: boolean;
  error?: Error | null;
}

export function DashboardStats({ statistics, loading, error }: DashboardStatsProps) {
  if (loading) {
    return (
      <div className="dashboard-stats">
        <div className="stats-loading">
          <div className="loading-spinner"></div>
          <p>Lade Statistiken...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-stats">
        <div className="stats-error">
          <div className="error-icon">⚠️</div>
          <p>Fehler beim Laden der Statistiken:</p>
          <p className="error-message">{error.message}</p>
        </div>
      </div>
    );
  }

  const statItems = [
    {
      key: 'totalCustomers',
      label: 'Kunden gesamt',
      value: statistics.totalCustomers,
      icon: '👥',
      color: 'blue',
    },
    {
      key: 'activeCustomers',
      label: 'Aktive Kunden',
      value: statistics.activeCustomers,
      icon: '✅',
      color: 'green',
    },
    {
      key: 'customersAtRisk',
      label: 'Risiko-Kunden',
      value: statistics.customersAtRisk,
      icon: '⚠️',
      color: 'orange',
    },
    {
      key: 'openTasks',
      label: 'Offene Aufgaben',
      value: statistics.openTasks,
      icon: '📋',
      color: 'purple',
    },
    {
      key: 'overdueItems',
      label: 'Überfällig',
      value: statistics.overdueItems,
      icon: '🔴',
      color: 'red',
    },
  ];

  return (
    <div className="dashboard-stats">
      <h3 className="stats-title">Dashboard Übersicht</h3>
      <div className="stats-grid">
        {statItems.map(item => (
          <div key={item.key} className={`stat-card stat-${item.color}`}>
            <div className="stat-icon">{item.icon}</div>
            <div className="stat-content">
              <div className="stat-value">{item.value}</div>
              <div className="stat-label">{item.label}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
