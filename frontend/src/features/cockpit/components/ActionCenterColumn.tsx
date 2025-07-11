/**
 * Aktions-Center - Spalte 3 des Sales Cockpit
 *
 * Hier findet die kontextbezogene Arbeit statt
 * Bietet geführte Prozesse je nach Kundenstatus
 */

import { useCockpitStore } from '../../../store/cockpitStore';
// CSS import removed - migrating to MUI sx props

export function ActionCenterColumn() {
  const { selectedCustomer, activeProcess, setActiveProcess, selectCustomer } = useCockpitStore();

  const processes = [
    { id: 'new-customer', name: 'Neukunden-Akquise', icon: '🎯' },
    { id: 'offer', name: 'Angebot erstellen', icon: '📄' },
    { id: 'follow-up', name: 'Nachfassen', icon: '📞' },
    { id: 'renewal', name: 'Vertragsverlängerung', icon: '🔄' },
  ];

  if (!selectedCustomer) {
    return (
      <div className="action-center-column">
        <div className="column-header">
          <h2 className="column-title">Aktions-Center</h2>
        </div>

        <div className="column-content">
          <div className="column-empty">
            <div className="column-empty-icon">👈</div>
            <h3 className="column-empty-title">Kein Kunde ausgewählt</h3>
            <p className="column-empty-description">
              Wählen Sie einen Kunden aus der Fokus-Liste aus, um mit der Bearbeitung zu beginnen.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="action-center-column">
      <div className="column-header">
        <h2 className="column-title">Aktions-Center</h2>
        <div className="column-actions">
          <button className="btn-icon" onClick={() => selectCustomer(null)} title="Kunde schließen">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path
                fillRule="evenodd"
                d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                clipRule="evenodd"
              />
            </svg>
          </button>
        </div>
      </div>

      <div className="column-content">
        {/* Customer Header */}
        <div className="customer-header">
          <h3 className="customer-name">{selectedCustomer.companyName}</h3>
          <span className={`customer-status status-${selectedCustomer.status.toLowerCase()}`}>
            {selectedCustomer.status}
          </span>
        </div>

        {/* Process Selection */}
        <section className="process-section">
          <h3 className="section-title">Verfügbare Prozesse</h3>
          <div className="process-grid">
            {processes.map(process => (
              <button
                key={process.id}
                className={`process-card ${activeProcess === process.id ? 'active' : ''}`}
                onClick={() => setActiveProcess(process.id)}
              >
                <span className="process-icon">{process.icon}</span>
                <span className="process-name">{process.name}</span>
              </button>
            ))}
          </div>
        </section>

        {/* Active Process Content */}
        {activeProcess && (
          <section className="process-content">
            <div className="process-header">
              <h3 className="process-title">{processes.find(p => p.id === activeProcess)?.name}</h3>
              <button className="btn-text" onClick={() => setActiveProcess(null)}>
                Prozess beenden
              </button>
            </div>

            {/* Process-specific content would go here */}
            <div className="process-placeholder">
              <p>
                Hier werden die geführten Schritte für den Prozess "
                {processes.find(p => p.id === activeProcess)?.name}" angezeigt.
              </p>
              <p className="placeholder-note">
                Die detaillierte Prozess-Implementation erfolgt in Phase 2.
              </p>
            </div>
          </section>
        )}

        {/* Quick Actions */}
        <section className="quick-actions">
          <h3 className="section-title">Schnellaktionen</h3>
          <div className="action-buttons">
            <button className="action-btn">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z" />
              </svg>
              Anrufen
            </button>
            <button className="action-btn">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
              </svg>
              E-Mail
            </button>
            <button className="action-btn">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
              </svg>
              Notiz
            </button>
          </div>
        </section>

        {/* Activity Timeline Preview */}
        <section className="timeline-preview">
          <h3 className="section-title">Letzte Aktivitäten</h3>
          <div className="timeline-items">
            <div className="timeline-item">
              <span className="timeline-icon">📧</span>
              <div className="timeline-content">
                <p className="timeline-text">E-Mail gesendet: Angebot Q1-2025</p>
                <span className="timeline-date">Vor 2 Tagen</span>
              </div>
            </div>
            <div className="timeline-item">
              <span className="timeline-icon">📞</span>
              <div className="timeline-content">
                <p className="timeline-text">Telefonat: Budget-Besprechung</p>
                <span className="timeline-date">Vor 1 Woche</span>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
