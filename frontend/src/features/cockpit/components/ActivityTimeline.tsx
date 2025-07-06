/**
 * Activity Timeline Komponente
 *
 * Zeigt die Aktivitäten und Tasks aus dem BFF an
 */
import { format, parseISO } from 'date-fns';
import { de } from 'date-fns/locale';
import type { DashboardTask, DashboardAlert } from '../types/salesCockpit';
import './ActivityTimeline.css';

interface ActivityTimelineProps {
  tasks: DashboardTask[];
  alerts: DashboardAlert[];
  loading?: boolean;
  error?: Error | null;
}

export function ActivityTimeline({ tasks, alerts, loading, error }: ActivityTimelineProps) {
  if (loading) {
    return (
      <div className="activity-timeline">
        <div className="timeline-loading">
          <div className="loading-spinner"></div>
          <p>Lade Aktivitäten...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="activity-timeline">
        <div className="timeline-error">
          <span className="error-icon">⚠️</span>
          <p>Fehler beim Laden der Aktivitäten</p>
          <p className="error-details">{error.message}</p>
        </div>
      </div>
    );
  }

  // Kombiniere Tasks und Alerts in eine Timeline
  const timelineItems = [
    ...tasks.map(task => ({
      id: task.id,
      type: 'task' as const,
      icon: getTaskIcon(task.type),
      title: task.title,
      description: task.description,
      time: task.dueDate,
      priority: task.priority,
      completed: task.completed,
    })),
    ...alerts.map(alert => ({
      id: alert.id,
      type: 'alert' as const,
      icon: getAlertIcon(alert.type),
      title: alert.title,
      description: alert.message,
      time: alert.createdAt,
      severity: alert.severity,
    })),
  ].sort((a, b) => {
    if (!a.time && !b.time) return 0; // both null, keep order
    if (!a.time) return 1; // a is null, send to back
    if (!b.time) return -1; // b is null, send to back
    return new Date(b.time).getTime() - new Date(a.time).getTime();
  });

  if (timelineItems.length === 0) {
    return (
      <div className="activity-timeline">
        <div className="timeline-empty">
          <span className="empty-icon">📋</span>
          <p>Keine Aktivitäten vorhanden</p>
        </div>
      </div>
    );
  }

  return (
    <div className="activity-timeline">
      {timelineItems.map(item => (
        <div
          key={item.id}
          className={`timeline-item ${item.type} ${
            item.type === 'task' && item.completed ? 'completed' : ''
          }`}
        >
          <span className="timeline-icon">{item.icon}</span>
          <div className="timeline-content">
            <div className="timeline-header">
              <h4 className="timeline-title">{item.title}</h4>
              {item.type === 'task' && (
                <span className={`task-priority priority-${item.priority?.toLowerCase()}`}>
                  {getPriorityLabel(item.priority)}
                </span>
              )}
              {item.type === 'alert' && (
                <span className={`alert-severity severity-${item.severity?.toLowerCase()}`}>
                  {getSeverityLabel(item.severity)}
                </span>
              )}
            </div>
            {item.description && <p className="timeline-description">{item.description}</p>}
            {item.time && <time className="timeline-time">{formatTimelineDate(item.time)}</time>}
          </div>
        </div>
      ))}
    </div>
  );
}

// Hilfsfunktionen
function getTaskIcon(type: string): string {
  const icons: Record<string, string> = {
    CALL: '📞',
    EMAIL: '📧',
    MEETING: '🤝',
    TODO: '✓',
    FOLLOW_UP: '🔄',
  };
  return icons[type] || '📋';
}

function getAlertIcon(type: string): string {
  const icons: Record<string, string> = {
    OPPORTUNITY: '💡',
    RISK: '⚠️',
    REMINDER: '🔔',
    INFO: 'ℹ️',
  };
  return icons[type] || '📢';
}

function getPriorityLabel(priority?: string): string {
  const labels: Record<string, string> = {
    HIGH: 'Hoch',
    MEDIUM: 'Mittel',
    LOW: 'Niedrig',
  };
  return labels[priority || ''] || priority || '';
}

function getSeverityLabel(severity?: string): string {
  const labels: Record<string, string> = {
    HIGH: 'Wichtig',
    MEDIUM: 'Mittel',
    LOW: 'Niedrig',
    INFO: 'Info',
  };
  return labels[severity || ''] || severity || '';
}

function formatTimelineDate(dateString: string): string {
  try {
    const date = parseISO(dateString);
    const now = new Date();
    const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60);

    if (diffInHours < 1) {
      return 'Vor wenigen Minuten';
    } else if (diffInHours < 24) {
      const hours = Math.floor(diffInHours);
      return `Vor ${hours} Stunde${hours !== 1 ? 'n' : ''}`;
    } else if (diffInHours < 48) {
      return 'Gestern';
    } else if (diffInHours < 168) {
      // 7 Tage
      const days = Math.floor(diffInHours / 24);
      return `Vor ${days} Tagen`;
    } else {
      return format(date, 'dd. MMM yyyy', { locale: de });
    }
  } catch {
    return dateString;
  }
}
