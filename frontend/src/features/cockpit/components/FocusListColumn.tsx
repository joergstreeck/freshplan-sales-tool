/**
 * Fokus-Liste - Spalte 2 des Sales Cockpit
 * 
 * Die dynamische Arbeitsliste für Kunden
 * Ermöglicht Wechsel zwischen Listen-, Karten- und Kanban-Ansicht
 */

import { useState } from 'react';
import { useCockpitStore } from '../../../store/cockpitStore';
import { CustomerList } from '../../../features/customer/components/CustomerList';
import type { Customer } from '../types';
import './FocusListColumn.css';

export function FocusListColumn() {
  const { 
    viewMode, 
    setViewMode, 
    filterTags,
    addFilterTag,
    removeFilterTag,
    searchQuery,
    setSearchQuery,
    selectCustomer
  } = useCockpitStore();

  const [savedViews] = useState([
    { id: '1', name: 'Aktive Kunden', count: 42 },
    { id: '2', name: 'Neue Leads', count: 8 },
    { id: '3', name: 'Risiko-Kunden', count: 3 },
    { id: '4', name: 'Diese Woche', count: 15 }
  ]);

  const handleCustomerSelect = (customer: Customer) => {
    selectCustomer({
      id: customer.id,
      companyName: customer.companyName,
      status: customer.status
    });
  };

  return (
    <div className="focus-list-column">
      <div className="column-header">
        <h2 className="column-title">Fokus-Liste</h2>
        <div className="column-actions">
          {/* View Mode Switcher */}
          <div className="view-mode-switcher">
            <button
              className={`view-mode-btn ${viewMode === 'list' ? 'active' : ''}`}
              onClick={() => setViewMode('list')}
              title="Listenansicht"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clipRule="evenodd"/>
              </svg>
            </button>
            <button
              className={`view-mode-btn ${viewMode === 'cards' ? 'active' : ''}`}
              onClick={() => setViewMode('cards')}
              title="Kartenansicht"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M5 3a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2V5a2 2 0 00-2-2H5zM5 11a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2v-2a2 2 0 00-2-2H5zM11 5a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V5zM13 11a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2v-2a2 2 0 00-2-2h-2z"/>
              </svg>
            </button>
            <button
              className={`view-mode-btn ${viewMode === 'kanban' ? 'active' : ''}`}
              onClick={() => setViewMode('kanban')}
              title="Kanban-Ansicht"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M7 3a1 1 0 000 2h6a1 1 0 100-2H7zM4 7a1 1 0 011-1h10a1 1 0 110 2H5a1 1 0 01-1-1zM2 11a2 2 0 012-2h12a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2v-4z"/>
              </svg>
            </button>
          </div>

          {/* More Actions */}
          <button className="btn-icon" title="Filter">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M3 3a1 1 0 011-1h12a1 1 0 011 1v3a1 1 0 01-.293.707L12 11.414V15a1 1 0 01-.293.707l-2 2A1 1 0 018 17v-5.586L3.293 6.707A1 1 0 013 6V3z" clipRule="evenodd"/>
            </svg>
          </button>
        </div>
      </div>

      <div className="column-content">
        {/* Search Bar */}
        <div className="search-section">
          <div className="search-input-wrapper">
            <svg className="search-icon" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd"/>
            </svg>
            <input
              type="text"
              className="search-input"
              placeholder="Kunden suchen..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        {/* Active Filters */}
        {filterTags.length > 0 && (
          <div className="active-filters">
            {filterTags.map(tag => (
              <span key={tag} className="filter-tag">
                {tag}
                <button
                  className="filter-tag-remove"
                  onClick={() => removeFilterTag(tag)}
                >
                  ×
                </button>
              </span>
            ))}
            <button
              className="btn-text btn-sm"
              onClick={() => filterTags.forEach(tag => removeFilterTag(tag))}
            >
              Alle entfernen
            </button>
          </div>
        )}

        {/* Saved Views */}
        <div className="saved-views">
          <h3 className="section-title">Gespeicherte Ansichten</h3>
          <div className="view-list">
            {savedViews.map(view => (
              <button
                key={view.id}
                className="saved-view-btn"
                onClick={() => addFilterTag(view.name)}
              >
                <span className="view-name">{view.name}</span>
                <span className="view-count">{view.count}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Customer List based on view mode */}
        <div className="customer-list-wrapper">
          {viewMode === 'list' && (
            <CustomerList onCustomerSelect={handleCustomerSelect} />
          )}
          
          {viewMode === 'cards' && (
            <div className="cards-view">
              <p className="placeholder-text">
                Kartenansicht wird in Phase 2 implementiert
              </p>
            </div>
          )}
          
          {viewMode === 'kanban' && (
            <div className="kanban-view">
              <p className="placeholder-text">
                Kanban-Ansicht wird in Phase 2 implementiert
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}