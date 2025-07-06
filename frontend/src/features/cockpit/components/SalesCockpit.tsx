/**
 * Sales Cockpit - Die revolutionäre 3-Spalten-Oberfläche
 * 
 * Implementiert die Vision aus dem CRM_COMPLETE_MASTER_PLAN V4:
 * - Spalte 1: Mein Tag (Übersicht & Prioritäten)
 * - Spalte 2: Fokus-Liste (Dynamischer Arbeitsvorrat)
 * - Spalte 3: Aktions-Center (Kontextbezogene Arbeit)
 */

import { useEffect } from 'react';
import { useCockpitStore } from '../../../store/cockpitStore';
import { useAuth } from '../../../hooks/useAuth';
import { useDashboardData } from '../hooks/useSalesCockpit';
import { MyDayColumn } from './MyDayColumn';
import { FocusListColumn } from './FocusListColumn';
import { ActionCenterColumn } from './ActionCenterColumn';
import { CockpitHeader } from './CockpitHeader';
import { DashboardStats } from './DashboardStats';
import './SalesCockpit.css';

export function SalesCockpit() {
  const { 
    activeColumn, 
    isMobileMenuOpen,
    isCompactMode,
    setActiveColumn 
  } = useCockpitStore();
  
  const { userId } = useAuth();
  
  // Hole Dashboard-Daten für Header-Statistiken
  const { 
    data: dashboardData, 
    isLoading, 
    isError, 
    error 
  } = useDashboardData(userId || null);

  // Keyboard navigation
  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      if (e.altKey) {
        switch (e.key) {
          case '1':
            setActiveColumn('my-day');
            break;
          case '2':
            setActiveColumn('focus-list');
            break;
          case '3':
            setActiveColumn('action-center');
            break;
        }
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [setActiveColumn]);

  return (
    <div className={`sales-cockpit ${isCompactMode ? 'compact-mode' : ''}`}>
      <CockpitHeader />
      
      {/* Dashboard Statistiken */}
      {dashboardData?.statistics && (
        <div className="cockpit-stats-container">
          <DashboardStats 
            statistics={dashboardData.statistics}
            loading={isLoading}
            error={isError ? error : null}
          />
        </div>
      )}
      
      <main className="cockpit-main">
        <div 
        className={`cockpit-columns ${
          isMobileMenuOpen ? 'mobile-menu-open' : ''
        }`}
      >
          {/* Spalte 1: Mein Tag */}
          <div 
            className={`cockpit-column column-my-day ${
              activeColumn === 'my-day' ? 'active' : ''
            }`}
            onClick={() => setActiveColumn('my-day')}
          >
            <MyDayColumn />
          </div>

          {/* Spalte 2: Fokus-Liste */}
          <div 
            className={`cockpit-column column-focus-list ${
              activeColumn === 'focus-list' ? 'active' : ''
            }`}
            onClick={() => setActiveColumn('focus-list')}
          >
            <FocusListColumn />
          </div>

          {/* Spalte 3: Aktions-Center */}
          <div 
            className={`cockpit-column column-action-center ${
              activeColumn === 'action-center' ? 'active' : ''
            }`}
            onClick={() => setActiveColumn('action-center')}
          >
            <ActionCenterColumn />
          </div>
        </div>
      </main>

      {/* Mobile Navigation Hints */}
      <div className="mobile-nav-hints">
        <span>Alt+1: Mein Tag</span>
        <span>Alt+2: Fokus-Liste</span>
        <span>Alt+3: Aktions-Center</span>
      </div>
    </div>
  );
}