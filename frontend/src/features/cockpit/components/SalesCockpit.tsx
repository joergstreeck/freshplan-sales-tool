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
  console.log('SalesCockpit rendering...');
  
  try {
    const { 
      activeColumn, 
      isMobileMenuOpen,
      isCompactMode,
      setActiveColumn 
    } = useCockpitStore();
    
    const { userId } = useAuth();
    console.log('Auth userId:', userId);
    
    // Hole Dashboard-Daten für Header-Statistiken
    const { 
      data: dashboardData, 
      isLoading, 
      isError, 
      error 
    } = useDashboardData(userId || null);
    console.log('Dashboard data:', { isLoading, isError, data: dashboardData });

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
    <div className={`sales-cockpit ${isCompactMode ? 'compact-mode' : ''}`} style={{ minHeight: '100vh', backgroundColor: '#f0f0f0' }}>
      <div style={{ padding: '20px', backgroundColor: 'white', border: '2px solid red' }}>
        <h1 style={{ color: 'black', fontSize: '24px' }}>Sales Cockpit Debug</h1>
        <p style={{ color: 'blue' }}>UserId: {userId || 'No userId'}</p>
        <p style={{ color: 'green' }}>Loading: {isLoading ? 'Yes' : 'No'}</p>
        <p style={{ color: 'red' }}>Error: {isError ? error?.message : 'No error'}</p>
        <p style={{ color: 'purple' }}>Data available: {dashboardData ? 'YES' : 'NO'}</p>
      </div>
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
  } catch (err) {
    console.error('SalesCockpit render error:', err);
    return (
      <div style={{ padding: '20px', color: 'red' }}>
        <h1>Error in SalesCockpit</h1>
        <pre>{err instanceof Error ? err.message : 'Unknown error'}</pre>
      </div>
    );
  }
}