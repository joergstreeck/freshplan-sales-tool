/**
 * Fokus-Liste - Spalte 2 des Sales Cockpit
 * 
 * Die dynamische Arbeitsliste für Kunden
 * Ermöglicht Wechsel zwischen Listen-, Karten- und Kanban-Ansicht
 */

import { useCockpitStore } from '../../../store/cockpitStore';
import { FilterBar } from '../../customers/components/FilterBar';
import { CustomerCard } from '../../customers/components/CustomerCard';
import { CustomerList } from '../../../features/customer/components/CustomerList';
import { useCustomerSearch } from '../../customers/hooks/useCustomerSearch';
import { useFocusListStore } from '../../customers/store/focusListStore';
import type { Customer } from '../../customers/hooks/useCustomerSearch';
import './FocusListColumn.css';

export function FocusListColumn() {
  const { selectCustomer } = useCockpitStore();
  const { viewMode } = useFocusListStore();
  
  // Verwende den neuen Customer Search Hook
  const { data, isLoading, isError, error } = useCustomerSearch();

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
      </div>

      {/* Neue FilterBar Komponente */}
      <FilterBar />

      <div className="column-content">

        {/* Customer List based on view mode */}
        <div className="customer-list-wrapper">
          {/* Loading State */}
          {isLoading && (
            <div className="loading-state">
              <p>Lade Kunden...</p>
            </div>
          )}
          
          {/* Error State */}
          {isError && (
            <div className="error-state">
              <p>Fehler beim Laden der Kunden: {error?.message}</p>
            </div>
          )}
          
          {/* Data Display */}
          {data && !isLoading && (
            <>
              {viewMode === 'table' && (
                <CustomerList onCustomerSelect={handleCustomerSelect} />
              )}
              
              {viewMode === 'cards' && (
                <div className="cards-view">
                  <div className="customer-cards-grid">
                    {data.content.map(customer => (
                      <CustomerCard
                        key={customer.id}
                        customer={customer}
                        onClick={handleCustomerSelect}
                      />
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
          
          {/* Pagination Info */}
          {data && (
            <div className="pagination-info">
              <p>
                Zeige {data.content.length} von {data.totalElements} Kunden
                (Seite {data.page + 1} von {data.totalPages})
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}