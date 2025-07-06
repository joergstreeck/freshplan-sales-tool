/**
 * Cockpit Header - Navigation und globale Aktionen
 */

import { Link } from 'react-router-dom';
import { useCockpitStore } from '../../../store/cockpitStore';
import { useAuth } from '../../../contexts/AuthContext';
import './CockpitHeader.css';

export function CockpitHeader() {
  const { toggleMobileMenu, toggleCompactMode, isCompactMode } = useCockpitStore();
  const { user, logout } = useAuth();

  return (
    <header className="cockpit-header">
      <div className="header-content">
        {/* Logo und Titel */}
        <div className="header-brand">
          <button
            className="mobile-menu-toggle mobile-only"
            onClick={toggleMobileMenu}
            aria-label="Toggle menu"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path
                d="M3 12h18M3 6h18M3 18h18"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
              />
            </svg>
          </button>

          <Link to="/" className="brand-link">
            <img src="/freshfoodzlogo.png" alt="FreshFoodz Logo" className="brand-logo" />
            <div className="brand-text">
              <h1 className="brand-title">FreshPlan</h1>
              <span className="brand-subtitle">Sales Command Center</span>
            </div>
          </Link>
        </div>

        {/* Zentrale Navigation */}
        <nav className="header-nav desktop-only">
          <Link to="/cockpit" className="nav-link active">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z" />
            </svg>
            Cockpit
          </Link>
          <Link to="/opportunities" className="nav-link">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path d="M2 10a8 8 0 018-8v8h8a8 8 0 11-16 0z" />
              <path d="M12 2.252A8.014 8.014 0 0117.748 8H12V2.252z" />
            </svg>
            Opportunities
          </Link>
          <Link to="/reports" className="nav-link">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zM8 7a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zM14 4a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z" />
            </svg>
            Berichte
          </Link>
        </nav>

        {/* Rechte Seite: Actions */}
        <div className="header-actions">
          {/* Compact Mode Toggle */}
          <button
            className="header-action-btn desktop-only"
            onClick={toggleCompactMode}
            title={isCompactMode ? 'Normale Ansicht' : 'Kompakte Ansicht'}
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              {isCompactMode ? (
                <path d="M3 8V4m0 0h4M3 4l4 4m10 0V4m0 0h-4m4 0l-4 4M3 12v4m0 0h4m-4 0l4-4m10 4l-4-4m4 4v-4m0 4h-4" />
              ) : (
                <path d="M4 8V4m0 0h4M4 4l4 4m8-4v4m0-4h-4m4 0l-4 4M4 12v4m0 0h4m-4 0l4-4m8 0v4m0-4h-4m4 0l-4 4" />
              )}
            </svg>
          </button>

          {/* Search */}
          <button className="header-action-btn">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path
                fillRule="evenodd"
                d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
                clipRule="evenodd"
              />
            </svg>
          </button>

          {/* Notifications */}
          <button className="header-action-btn">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z" />
            </svg>
            <span className="notification-badge">3</span>
          </button>

          {/* User Menu */}
          <div className="user-menu">
            <button className="user-menu-btn">
              <span className="user-avatar">
                {user?.firstName?.[0]}
                {user?.lastName?.[0]}
              </span>
              <span className="user-name desktop-only">
                {user?.firstName} {user?.lastName}
              </span>
              <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                  clipRule="evenodd"
                />
              </svg>
            </button>

            <div className="user-menu-dropdown">
              <Link to="/profile" className="user-menu-item">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                  <path
                    fillRule="evenodd"
                    d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
                    clipRule="evenodd"
                  />
                </svg>
                Mein Profil
              </Link>
              <Link to="/settings" className="user-menu-item">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                  <path
                    fillRule="evenodd"
                    d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM8 10a2 2 0 100-4 2 2 0 000 4z"
                    clipRule="evenodd"
                  />
                </svg>
                Einstellungen
              </Link>
              <hr className="user-menu-divider" />
              <button onClick={logout} className="user-menu-item">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                  <path
                    fillRule="evenodd"
                    d="M3 3a1 1 0 00-1 1v8a1 1 0 001 1h5a1 1 0 001-1V4a1 1 0 00-1-1H3zm5 7a1 1 0 11-2 0V5a1 1 0 112 0v5zm3-6a1 1 0 011 1v6a1 1 0 01-1 1h-.5a.5.5 0 010-1H11V5h-.5a.5.5 0 010-1H11zm2.354 2.646a.5.5 0 010 .708l-2 2a.5.5 0 01-.708-.708L11.293 8l-1.647-1.646a.5.5 0 01.708-.708l2 2z"
                    clipRule="evenodd"
                  />
                </svg>
                Abmelden
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
