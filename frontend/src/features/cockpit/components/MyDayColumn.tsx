/**
 * Mein Tag - Spalte 1 des Sales Cockpit
 * 
 * Zeigt proaktiv die wichtigsten Aufgaben, Termine und KI-gestützte Alarme
 * Beinhaltet die Triage-Inbox für nicht zugeordnete Kommunikation
 */

import { useState, useEffect } from 'react';
import { useCockpitStore } from '../../../store/cockpitStore';
import { mockTasks, mockTriageItems } from '../data/mockData';
import type { PriorityTask, TriageItem } from '../types';
import './MyDayColumn.css';

export function MyDayColumn() {
  const { showTriageInbox, toggleTriageInbox, setPriorityTasksCount } = useCockpitStore();
  const [tasks, setTasks] = useState<PriorityTask[]>([]);
  const [triageItems, setTriageItems] = useState<TriageItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Simuliere das Laden von Daten
    const timer = setTimeout(() => {
      setTasks(mockTasks);
      setTriageItems(mockTriageItems);
      setPriorityTasksCount(mockTasks.filter(t => t.priority === 'high').length);
      setLoading(false);
    }, 500);

    return () => clearTimeout(timer);
  }, [setPriorityTasksCount]);

  const getTaskIcon = (type: PriorityTask['type']) => {
    switch (type) {
      case 'call':
        return '📞';
      case 'email':
        return '✉️';
      case 'appointment':
        return '🗓️';
      case 'todo':
        return '✓';
      default:
        return '📋';
    }
  };

  const getPriorityClass = (priority: PriorityTask['priority']) => {
    return `priority-${priority}`;
  };

  const formatTime = (date?: Date) => {
    if (!date) return '';
    return new Intl.DateTimeFormat('de-DE', {
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  };

  if (loading) {
    return (
      <div className="column-loading">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  return (
    <div className="my-day-column">
      <div className="column-header">
        <h2 className="column-title">Mein Tag</h2>
        <div className="column-actions">
          <button 
            className="btn-icon" 
            title="Aktualisieren"
            aria-label="Aufgaben aktualisieren"
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clipRule="evenodd"/>
            </svg>
          </button>
        </div>
      </div>

      <div className="column-content">
        {/* KI-Empfehlung */}
        <div className="ai-recommendation">
          <div className="ai-icon">🤖</div>
          <div className="ai-content">
            <h3 className="ai-title">Nächste beste Aktion</h3>
            <p className="ai-text">
              Rufen Sie <strong>Müller GmbH</strong> an - das Angebot läuft heute ab 
              und die Abschlusswahrscheinlichkeit liegt bei 85%.
            </p>
            <button className="ai-action-btn">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"/>
              </svg>
              Jetzt anrufen
            </button>
          </div>
        </div>

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

        {/* Triage-Inbox */}
        <section className="triage-section">
          <button 
            className="triage-toggle"
            onClick={toggleTriageInbox}
            aria-expanded={showTriageInbox}
            aria-label={showTriageInbox ? 'Triage-Inbox ausblenden' : 'Triage-Inbox anzeigen'}
          >
            <h3 className="section-title">
              Triage-Inbox ({triageItems.length})
            </h3>
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
          
          {showTriageInbox && (
            <div className="triage-list">
              {triageItems.map(item => (
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