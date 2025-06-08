import { OriginalCalculator } from '../features/calculator/components/OriginalCalculator';
import '../styles/legacy/layout.css';
import '../styles/legacy/header-logo.css';

export function OriginalCalculatorPage() {
  return (
    <div className="container">
      {/* Header mit Logo wie im Original */}
      <header className="header">
        <div className="header-content">
          <div className="logo-container">
            <img 
              src="/freshfoodzlogo.png" 
              alt="FreshPlan Logo" 
              className="logo"
            />
          </div>
          <div className="tagline">So einfach, schnell und lecker!</div>
        </div>
      </header>

      {/* Tab Navigation wie im Original */}
      <nav className="nav">
        <div className="nav-tabs">
          <button className="nav-tab active" data-tab="calculator">
            <span>Rabattrechner</span>
          </button>
          <button className="nav-tab" data-tab="customer" disabled>
            <span>Kundendaten</span>
          </button>
          <button className="nav-tab" data-tab="creditcheck" disabled>
            <span>Bonitätsprüfung</span>
          </button>
          <button className="nav-tab" data-tab="profile" disabled>
            <span>Profil</span>
          </button>
          <button className="nav-tab" data-tab="offer" disabled>
            <span>Angebot</span>
          </button>
          <button className="nav-tab" data-tab="settings" disabled>
            <span>Einstellungen</span>
          </button>
        </div>
        <div className="nav-actions">
          <button className="btn btn-secondary" disabled>
            <span>Formular leeren</span>
          </button>
          <button className="btn btn-primary" disabled>
            <span>Speichern</span>
          </button>
        </div>
      </nav>

      {/* Main Content */}
      <main className="main">
        <div className="tab-content">
          <div id="calculator" className="tab-panel active">
            <OriginalCalculator />
          </div>
        </div>
      </main>
    </div>
  );
}