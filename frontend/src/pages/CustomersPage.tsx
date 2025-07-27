import React from 'react';
import { CustomerList } from '@/features/customer/components';
import { AuthenticatedLayout } from '@/components/layout/AuthenticatedLayout';

const CustomersPage: React.FC = () => {
  // Temporär: Debug-Modus für direkten Zugriff (entfernen nach Auth-Fix)
  const isDev = import.meta.env.DEV;
  
  if (isDev) {
    // Development: Zeige CustomerList direkt ohne Auth-Check
    return (
      <div style={{ padding: '20px', minHeight: '100vh' }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <h1>🚧 Dev-Modus: Kundenverwaltung</h1>
          <p style={{ color: '#666', marginBottom: '20px' }}>
            Direktzugriff für Development (Auth umgangen)
          </p>
          <CustomerList />
        </div>
      </div>
    );
  }
  
  return (
    <AuthenticatedLayout>
      <div className="page-container">
        <CustomerList />
      </div>
    </AuthenticatedLayout>
  );
};

export default CustomersPage;
