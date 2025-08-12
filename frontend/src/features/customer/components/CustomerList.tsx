import React, { useState } from 'react';
import { useCustomers } from '../api/customerQueries';
import type { CustomerResponse } from '../types/customer.types';
import {
  customerTypeLabels,
  customerStatusLabels,
  industryLabels,
  customerStatusColors,
} from '../types/customer.types';
import { LoadingSpinner } from '../../../components/ui/LoadingSpinner';
import { UniversalExportButton } from '../../../components/export';
import './CustomerList.css';

export const CustomerList: React.FC = () => {
  const [currentPage, setCurrentPage] = useState(0);
  const [sortBy, setSortBy] = useState('companyName');
  const pageSize = 50; // Erhöht von 20 auf 50 für bessere Übersicht

  const { data, isLoading, isError, error } = useCustomers(currentPage, pageSize, sortBy);

  // Format currency
  const formatCurrency = (amount?: number) => {
    if (!amount) return '-';
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  };

  // Format date
  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  };

  // Handle sorting
  const handleSort = (field: string) => {
    setSortBy(field);
    setCurrentPage(0); // Reset to first page when sorting
  };

  // Render loading state
  if (isLoading) {
    return (
      <div className="customer-list-loading">
        <LoadingSpinner />
        <p>Kundendaten werden geladen...</p>
      </div>
    );
  }

  // Render error state
  if (isError) {
    return (
      <div className="customer-list-error">
        <div className="error-icon">⚠️</div>
        <h3>Fehler beim Laden der Kundendaten</h3>
        <p>{error instanceof Error ? error.message : 'Ein unerwarteter Fehler ist aufgetreten.'}</p>
      </div>
    );
  }

  // Render empty state
  if (!data?.content || data.content.length === 0) {
    return (
      <div className="customer-list-empty">
        <div className="empty-icon">📋</div>
        <h3>Keine Kunden gefunden</h3>
        <p>Es sind noch keine Kunden im System vorhanden.</p>
      </div>
    );
  }

  return (
    <div className="customer-list-container">
      {/* Header */}
      <div className="customer-list-header">
        <h1>Kundenliste</h1>
        <div className="customer-list-actions">
          <div className="customer-list-stats">
            <span className="stat-item">
              <strong>{data.totalElements}</strong> Kunden gesamt
            </span>
          </div>
          <UniversalExportButton
            entity="customers"
            buttonLabel="Liste exportieren"
            onExportComplete={format => {}}
          />
          <a href="/kundenmanagement/neu" className="btn-new-customer">
            + Neuer Kunde
          </a>
        </div>
      </div>

      {/* Table */}
      <div className="customer-list-table-wrapper">
        <table className="customer-list-table">
          <thead>
            <tr>
              <th
                className={`sortable ${sortBy === 'customerNumber' ? 'sorted' : ''}`}
                onClick={() => handleSort('customerNumber')}
              >
                Kundennummer
              </th>
              <th
                className={`sortable ${sortBy === 'companyName' ? 'sorted' : ''}`}
                onClick={() => handleSort('companyName')}
              >
                Firmenname
              </th>
              <th>Typ</th>
              <th>Branche</th>
              <th
                className={`sortable ${sortBy === 'status' ? 'sorted' : ''}`}
                onClick={() => handleSort('status')}
              >
                Status
              </th>
              <th>Jahresumsatz</th>
              <th
                className={`sortable ${sortBy === 'lastContactDate' ? 'sorted' : ''}`}
                onClick={() => handleSort('lastContactDate')}
              >
                Letzter Kontakt
              </th>
              <th>Risiko</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((customer: CustomerResponse) => (
              <tr key={customer.id} className={customer.atRisk ? 'at-risk' : ''}>
                <td className="customer-number">{customer.customerNumber}</td>
                <td className="company-name">
                  <strong>{customer.companyName}</strong>
                  {customer.tradingName && (
                    <span className="trading-name">{customer.tradingName}</span>
                  )}
                </td>
                <td>{customerTypeLabels[customer.customerType]}</td>
                <td>{customer.industry ? industryLabels[customer.industry] : '-'}</td>
                <td>
                  <span
                    className="status-badge"
                    style={{ backgroundColor: customerStatusColors[customer.status] }}
                  >
                    {customerStatusLabels[customer.status]}
                  </span>
                </td>
                <td className="currency">{formatCurrency(customer.expectedAnnualVolume)}</td>
                <td>{formatDate(customer.lastContactDate)}</td>
                <td>
                  <div className="risk-indicator">
                    <div
                      className="risk-bar"
                      style={{
                        width: `${customer.riskScore}%`,
                        backgroundColor:
                          customer.riskScore > 70
                            ? '#F44336'
                            : customer.riskScore > 40
                              ? '#FF9800'
                              : '#4CAF50',
                      }}
                    />
                    <span className="risk-score">{customer.riskScore}%</span>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="customer-list-pagination">
        <div className="pagination-info">
          Zeige {data.page * data.size + 1} -{' '}
          {Math.min((data.page + 1) * data.size, data.totalElements)} von {data.totalElements}{' '}
          Einträgen
        </div>
        <div className="pagination-controls">
          <button
            className="pagination-button"
            onClick={() => setCurrentPage(0)}
            disabled={data.first}
          >
            Erste
          </button>
          <button
            className="pagination-button"
            onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
            disabled={data.first}
          >
            Zurück
          </button>
          <span className="pagination-current">
            Seite {data.page + 1} von {data.totalPages}
          </span>
          <button
            className="pagination-button"
            onClick={() => setCurrentPage(prev => prev + 1)}
            disabled={data.last}
          >
            Weiter
          </button>
          <button
            className="pagination-button"
            onClick={() => setCurrentPage(data.totalPages - 1)}
            disabled={data.last}
          >
            Letzte
          </button>
        </div>
      </div>
    </div>
  );
};
