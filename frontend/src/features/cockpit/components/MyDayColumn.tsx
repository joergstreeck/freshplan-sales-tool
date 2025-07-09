/**
 * Mein Tag - Spalte 1 des Sales Cockpit
 * 
 * Zeigt proaktiv die wichtigsten Aufgaben, Termine und KI-gestützte Alarme
 * Beinhaltet die Triage-Inbox für nicht zugeordnete Kommunikation
 */

import { useEffect } from 'react';
import { useCockpitStore } from '../../../store/cockpitStore';
import { useAuth } from '../../../hooks/useAuth';
import { useDashboardData } from '../hooks/useSalesCockpit';
import { mockTriageItems, mockTasks } from '../data/mockData';
import type { DashboardTask, DashboardAlert } from '../types/salesCockpit';
import type { PriorityTask } from '../types';
// CSS import removed - migrating to MUI sx props

export function MyDayColumn() {
  const { showTriageInbox, toggleTriageInbox, setPriorityTasksCount } = useCockpitStore();
  const { userId } = useAuth();
  
  // Hole Dashboard-Daten via BFF
  const { 
    data: dashboardData, 
    isLoading, 
    isError, 
    error,
    refetch 
  } = useDashboardData(userId);

  // Update Priority Task Count when data changes
  useEffect(() => {
    if (dashboardData?.todaysTasks) {
      const highPriorityCount = dashboardData.todaysTasks.filter(
        task => task.priority === 'HIGH'
      ).length;
      setPriorityTasksCount(highPriorityCount);
    }
  }, [dashboardData?.todaysTasks, setPriorityTasksCount]);

  const getTaskIcon = (type: DashboardTask['type']) => {
    switch (type) {
      case 'CALL':
        return '📞';
      case 'EMAIL':
        return '✉️';
      case 'APPOINTMENT':
        return '🗓️';
      case 'TODO':
        return '✓';
      case 'FOLLOW_UP':
        return '🔄';
      default:
        return '📋';
    }
  };

  const getPriorityClass = (priority: DashboardTask['priority']) => {
    return `priority-${priority.toLowerCase()}`;
  };

  const getAlertIcon = (type: DashboardAlert['type']) => {
    switch (type) {
      case 'OPPORTUNITY':
        return '💰';
      case 'RISK':
        return '⚠️';
      case 'REMINDER':
        return '🔔';
      case 'INFO':
        return 'ℹ️';
      default:
        return '📋';
    }
  };

  const formatTime = (dateString?: string) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('de-DE', {
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  };

  // Loading State
  if (isLoading) {
    return (
      <div className="my-day-column">
        <div className="column-header">
          <h2 className="column-title">Mein Tag</h2>
        </div>
        <div className="column-loading">
          <div className="loading-spinner"></div>
          <p>Lade Dashboard-Daten...</p>
        </div>
      </div>
    );
  }

  // Error State
  if (isError) {
    return (
      <div className="my-day-column">
        <div className="column-header">
          <h2 className="column-title">Mein Tag</h2>
        </div>
        <div className="column-error">
          <div className="error-icon">⚠️</div>
          <h3>Fehler beim Laden der Daten</h3>
          <p className="error-message">
            {error?.message || 'Unbekannter Fehler beim Abrufen der Dashboard-Daten'}
          </p>
          <button 
            className="retry-button" 
            onClick={() => refetch()}
            type="button"
          >
            🔄 Erneut versuchen
          </button>
        </div>
      </div>
    );
  }

  // Extract data from BFF response or use mock data as fallback
  const bffTasks = dashboardData?.todaysTasks || [];
  const alerts = dashboardData?.alerts || [];
  const todaysAlerts = alerts.filter(alert => 
    new Date(alert.createdAt).toDateString() === new Date().toDateString()
  );
  
  // Use mock tasks if there's an error or no data
  const tasks: PriorityTask[] = bffTasks.length > 0 
    ? bffTasks.map(task => ({
        id: task.id,
        title: task.title,
        customerName: task.customerName,
        type: task.type.toLowerCase() as PriorityTask['type'],
        priority: task.priority.toLowerCase() as PriorityTask['priority'],
        dueDate: task.dueDate ? new Date(task.dueDate) : undefined,
        completed: false
      }))
    : (isError || !dashboardData) ? mockTasks : [];

  return (
    <div className="my-day-column">
      <div className="column-header">
        <h2 className="column-title">Mein Tag</h2>
        <div className="column-actions">
          <button 
            className="btn-icon" 
            title="Aktualisieren"
            aria-label="Aufgaben aktualisieren"
            onClick={() => refetch()}
            type="button"
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clipRule="evenodd"/>
            </svg>
          </button>
        </div>
      </div>

      <div className="column-content">
        {/* KI-Alerts von BFF */}
        {todaysAlerts.length > 0 && (
          <section className="alerts-section">
            <h3 className="section-title">Aktuelle Benachrichtigungen</h3>
            {todaysAlerts.slice(0, 3).map(alert => (
              <div key={alert.id} className={`alert-item alert-${alert.severity.toLowerCase()}`}>
                <div className="alert-icon">{getAlertIcon(alert.type)}</div>
                <div className="alert-content">
                  <h4 className="alert-title">{alert.title}</h4>
                  <p className="alert-message">{alert.message}</p>
                  {alert.customerName && (
                    <span className="alert-customer">{alert.customerName}</span>
                  )}
                  {alert.actionLink && (
                    <button className="alert-action-btn" type="button">
                      Aktion ausführen
                    </button>
                  )}
                </div>
              </div>
            ))}
          </section>
        )}

        {/* Prioritäts-Aufgaben */}
        <section className="tasks-section">
          <h3 className="section-title">Prioritäts-Aufgaben ({tasks.length})</h3>
          <div className="task-list">
            {tasks.map(task => (
              <div key={task.id} className={`task-item ${getPriorityClass(task.priority)}`}>
                <span className="task-icon">{getTaskIcon(task.type)}</span>
                <div className="task-content">
                  <h4 className="task-title">{task.title}</h4>
                  {task.customerName && (
                    <span className="task-customer">{task.customerName}</span>
                  )}
                </div>
                {task.dueDate && (
                  <span className="task-time">{formatTime(task.dueDate)}</span>
                )}
              </div>
            ))}
          </div>
        </section>

        {/* Triage-Inbox - Vorerst Mock-Daten */}
        <section className="triage-section">
          <div className="section-header">
            <h3 className="section-title">
              Triage-Inbox ({mockTriageItems.length})
            </h3>
            <button 
              className="btn-icon"
              onClick={toggleTriageInbox}
              aria-expanded={showTriageInbox}
              aria-label={showTriageInbox ? 'Triage-Inbox ausblenden' : 'Triage-Inbox anzeigen'}
              type="button"
            >
              <svg 
                width="20" 
                height="20" 
                viewBox="0 0 20 20" 
                fill="currentColor"
                className={`toggle-icon ${showTriageInbox ? 'expanded' : ''}`}
              >
                <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd"/>
              </svg>
            </button>
          </div>
          
          {showTriageInbox && (
            <div className="triage-list">
              {mockTriageItems.map(item => (
                <div key={item.id} className="triage-item">
                  <div className="triage-header">
                    <span className="triage-from">{item.from}</span>
                    <span className="triage-time">
                      {new Date(item.receivedAt).toLocaleTimeString('de-DE', {
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </span>
                  </div>
                  <h4 className="triage-subject">{item.subject}</h4>
                  {item.content && (
                    <p className="triage-preview">{item.content}</p>
                  )}
                  <div className="triage-actions">
                    <button className="triage-action">Zuordnen</button>
                    <button className="triage-action">Als Lead</button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}